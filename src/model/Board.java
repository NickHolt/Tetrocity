package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

import testing.Debug;
import util.Direction;
import util.GameOverException;
import control.Engine;

/** A game board in a game of Tetrocity. A Board knows only of the {@link Tetrimino}
 * pieces currently in play. This includes the dead Tetriminoes and the live
 * Tetrimino. Board dimensions must be provided on instantiation. 
 * 
 *   A Board consists of a grid, of which a specified number of rows is visible. The
 *  non-visible rows sit on top of the visible rows, and are called the buffer region. 
 *  A buffer region allows a Tetrimino piece to be placed on the Board outside of
 *  visible range, so that it doesn't "instantaneously appear". If the longest
 *  possible Tetrimino in a given game of Tetrocity has a length of L, then the
 *  number of buffer rows should also be L. 
 *  
 *   A Board is responsible for tracking the Tetrimino queue, as well as the
 *  stored Tetrimino. It is not responsible for tracking player score. It will
 *  communicate the relevant information to the {@link Engine}, which will then
 *  deal with it. 
 * 
 *  When prompted to do so, a Board is capable of examining its state and reacting
 * appropriately. For example, if a Game calls update() on the Board, it will
 * scan for filled rows and request each Tetrimino occupying that row to delete
 * the appropriate blocks. It will then communicate that information to the
 * game Engine, which can then determine how to update the score. 
 * 
 * @author Nick Holt
 *
 */
public class Board {
    /* The capacity of the Queue this Board will use to track upcoming Tetriminoes. */
    public static final int FULL_QUEUE_SIZE = 5;
    
    /* A grid of Tetrimino IDs representing the game board. -1 is an empty space. */
    private int[][] mGrid;
    /* A HashMap of live Tetrimino IDs to that Tetrimino's last known coordinates. This
     * is a space-time tradeoff such that the Board does not have to scan its entire grid
     * to find the last known coordinates. 
     * This variable also serves a secondary purpose for looking up live Tetrimino IDs. */
    private HashMap<Integer, int[][]> mLiveTetriminoCoordinates;
    /* The number of non-visible buffer rows. */
    private int mBuffer;
    /* All currently live Tetriminoes (i.e. ones that the player controls). */
    private ArrayList<Tetrimino> mLiveTetriminoes;
    /* The stored Tetrimino. */
    private Tetrimino mStoredTetrimino;
    /* The Queue of upcoming Tetriminoes. Live Tetriminoes are taken from the Queue. */
    private ArrayBlockingQueue<Tetrimino> mTetriminoQueue;
    
    /** A new Board for a game of Tetrocity. A board is a (BUFFER + ROWS) x COLS matrix 
     * (grid) on which the game is played. A buffer is used to provide a number of
     * non-visible rows, so that a piece can be placed in the buffer region without
     * it instantaneously appearing on screen. 
     * 
     * @param rows The number of visible rows in this Board.
     * @param cols The number of visible columns in this Board.
     * @param buffer The number of non-visible rows in this Board. 
     */
    public Board(int rows, int cols, int buffer) {
        if (rows < 1
                || cols < 1
                || buffer < 0) {
            throw new IllegalArgumentException("The provided grid information "
                    + "(rows = " + rows + ", cols = " + cols + ", buffer = " + buffer
                    + ") is invalid.");
        }
        
        mGrid = new int[rows + buffer][cols];
        mBuffer = buffer;
        for (int i = 0; i < mGrid.length; i++) {
            for (int j = 0; j < mGrid[0].length; j++) {
                mGrid[i][j] = -1; //initialize grid to empty
            }
        }
        
        mLiveTetriminoes = new ArrayList<Tetrimino>();
        mLiveTetriminoCoordinates = new HashMap<Integer, int[][]>();
        mTetriminoQueue = new ArrayBlockingQueue<Tetrimino>(FULL_QUEUE_SIZE);
        
        Debug.print(1, "New Board instantiated.");
    }
    
    /** Given the Tetrimino, returns a grid coordinate such that the root coordinate of the
     * Tetrimino may be set to it and:
     *  i) Be centered on the grid.
     *  ii) If possible, have its bottom block(s) align with the bottom of the buffer region.
     *  
     *  If a Tetrimino is too large to have its bottom aligned, its top row coordinate will be 0.
     * Note that a placement coordinate will never contain a row such that the bottom row
     * of the Tetrimino begins more than one space above the buffer region.
     * 
     * Note that the Tetrimino's width and height must be less than or equal to that of the 
     *  board or behavior is unpredictable. 
     * 
     * @param tetrimino The Tetrimino to be placed.
     * @return The placement coordinate.
     */
    public int[] getPlacementCoordinate(Tetrimino tetrimino) {
        int col = mGrid[0].length / 2 - tetrimino.getShape().getWidth() / 2,
                row;
        
        if (tetrimino.getShape().getHeight() > mBuffer) {
            row = 0; 
        } else {
            row = mBuffer - tetrimino.getShape().getHeight();
        }
                
        Debug.print(3, "Board#getPlacementCoordinate successfully generated.");
        return new int[]{row, col};
    }
    
    /** Updates the grid with the current coordinate positions of all live Tetriminoes.
     * This method should be called every time movement is applied to any live Tetrimino. 
     */
    public void refreshGrid() {
        int tetriminoID;
        int[][] oldCoordinates, newCoordinates;
        for (Tetrimino tetrimino : mLiveTetriminoes) {
            tetriminoID = tetrimino.getID();
            
            oldCoordinates = mLiveTetriminoCoordinates.get(tetriminoID);
            
            if (oldCoordinates != null) { //Since we might not have tracked it yet
                for (int[] oldCoord : oldCoordinates) {
                    mGrid[oldCoord[0]][oldCoord[1]] = -1; //Empty old coordinate positions
                }
            }
                        
            newCoordinates = tetrimino.getCoordinates();
            for (int[] newCoord : newCoordinates) {
                mGrid[newCoord[0]][newCoord[1]] = tetriminoID; //Set new coordinate positions
            }
            
            mLiveTetriminoCoordinates.put(tetriminoID, newCoordinates);
        }
        
        Debug.print(3, "Grid updated.");
    }
    
    /** Attempt to clear filled rows. 
     * 
     *  A row is filled when there exists a dead block in every position on that row. 
     * Rows are checked from the top down. If a row is found to be full, every
     * block present in that row is deleted from the grid, and all dead blocks 
     * above it are shifted accordingly. 
     * 
     *  The return value is the number of lines cleared by the clearRows() call. 
     * 
     * @return The number of lines cleared. 
     */
    public int clearRows() {
        int rowsCleared = 0;
        int[] row;
        boolean isFilled;
        
        for (int i = 0; i < mGrid.length; i++) {
            row = mGrid[i];
            isFilled = true;
            
            for (int j = 0; j < row.length; j++) {
                if (row[j] == -1 || mLiveTetriminoCoordinates.containsKey(row[j])) {
                    isFilled = false;
                    break;
                }
            }
            
            if (isFilled) {
                dropDeadBlocks(i);
                rowsCleared++;
            }
        }
        
        Debug.print(1, rowsCleared + " rows cleared.");
        return rowsCleared;
    }
    
    /** Shifts every dead block contained in a row < ROW one coordinate position down. Note
     * that this will overwrite ROW.
     * 
     *  Live Tetrimino blocks are not shifted.
     * 
     *  If the input is 0, each element of row 0 will be set to -1. 
     * 
     * @param row The shift border. 
     */
    private void dropDeadBlocks(int rowBorder) {
        if (rowBorder < 0) {
            throw new IllegalArgumentException("The provided row: " + rowBorder + " was negative");
        }
        
        if (rowBorder == 0) {
            for (int i = 0; i < mGrid[0].length; i++) {
                if (!mLiveTetriminoCoordinates.containsKey(mGrid[0][i])) {
                    mGrid[0][i] = -1;
                }
            }
        }
        
        int[] curr, above;
        for (int i = rowBorder; i > 0; i--) {
            curr = mGrid[i];
            above = mGrid[i - 1];
            
            for (int j = 0; j < curr.length; j++) {
                if (!mLiveTetriminoCoordinates.containsKey(curr[j])){
                    if (!mLiveTetriminoCoordinates.containsKey(above[j])) {
                        curr[j] = above[j];
                    } else {
                        curr[j] = -1;
                    }   
                }
            }
        }
        
        Debug.print(2, "Grid dropped.");
    }
    
    /** Attempt to shift all live Tetriminoes one coordinate position towards the
     * provided direction. A shift failure occurs in one of two ways:
     * 1) The Tetrimino attempts to shift off of the grid or into the buffer region.
     * 2) Another Tetrimino already occupies that space. 
     * 
     *  If a failure is found for a live Tetrimino, that Tetrimino
     * will not move. Furthermore, if shiftDirection == Direction.SOUTH and a Tetrimino
     * shift fails, the Tetrimino will be marked dead.
     * 
     * @param direction The direction to shift all live Tetriminoes in. 
     */
    public void shiftLiveTetriminoes(Direction shiftDirection) {
        int[][] coordinates;
        int newRow = -1, newCol = -1, tetriminoID;
        boolean shiftFailed;
        ArrayList<Tetrimino> toKill = new ArrayList<Tetrimino>();
        for (Tetrimino tetrimino : mLiveTetriminoes) {
            coordinates = tetrimino.getCoordinates();
            tetriminoID = tetrimino.getID();
            shiftFailed = false; //innocent until proven guilty
            
            for (int[] coord : coordinates) {
                if (shiftDirection == Direction.NORTH) {
                    newRow = coord[0] - 1;
                    newCol = coord[1];
                } else if (shiftDirection == Direction.EAST) {
                    newRow = coord[0];
                    newCol = coord[1] + 1;
                } else if (shiftDirection == Direction.SOUTH) {
                    newRow = coord[0] + 1;
                    newCol = coord[1];
                } else if (shiftDirection == Direction.WEST) {
                    newRow = coord[0];
                    newCol = coord[1] - 1;
                }
                
                if ((newRow < mBuffer
                        && shiftDirection == Direction.NORTH) //can't shift up into buffer
                        || newRow >= mGrid.length
                        || newCol < 0
                        || newCol >= mGrid[0].length
                        || !(mGrid[newRow][newCol] == -1 //can't shift into occupied space
                        || mGrid[newRow][newCol] == tetriminoID)) {
                    shiftFailed = true;
                    break;
                }
            }
            
            if (shiftFailed && shiftDirection == Direction.SOUTH) {
                toKill.add(tetrimino); //kill the Tetrimino
            } else if (!shiftFailed) {
                tetrimino.shift(shiftDirection);
            }
            //Note non-south shift failures simply do nothing for that Tetrimino
        }
        
        for (Tetrimino deadTetrimino : toKill) {
            killTetrimino(deadTetrimino);
        }
                
        refreshGrid();
        Debug.print(1, "Live Tetriminoes shifted " + shiftDirection);
    }
    
    /** Stores the bottom-most Tetrimino that has not been previously stored. If a Tetrimino 
     * already exists in storage, it will be placed back on the top of the grid and marked as live. 
     * @throws GameOverException thrown if the space where the new, previously stored Tetrimino
     * is already occupied. This should not happen by game design.
     */
    public void storeTetrimino() throws GameOverException {
        Tetrimino bottomLiveValidTetrimino = mLiveTetriminoes.get(0);
        
        for (Tetrimino liveTetrimino : mLiveTetriminoes) {
            if (liveTetrimino.getBottomRow() > bottomLiveValidTetrimino.getBottomRow()) {
                bottomLiveValidTetrimino = liveTetrimino;
            }
        }
        
        if (!bottomLiveValidTetrimino.hasBeenStored()) {
            if (mStoredTetrimino != null) {
                putTetrimino(mStoredTetrimino);
            }
            
            removeTetrimino(bottomLiveValidTetrimino);
            mStoredTetrimino = bottomLiveValidTetrimino;
            bottomLiveValidTetrimino.markStored();
            
            refreshGrid();
            
            Debug.print(1, "Tetrimino stored.");
        } else {
            //Do nothing
            Debug.print(1, "Tetrimino storage rejected.");
        }
    }
    
    /** Removes the Tetrimino from the list of live Tetriminoes. A "dead" Tetrimino will
     * continue to exist on the grid until it is cleared completely though normal gameplay.
     * 
     * @param tetrimino The Tetrimino to kill
     */
    public void killTetrimino(Tetrimino tetrimino) {
        mLiveTetriminoes.remove(tetrimino);
        mLiveTetriminoCoordinates.remove(tetrimino.getID());
    }
    
    /** Removes the Tetrimino from the grid and kills it. 
     * 
     * @param tetrimino The Tetrimino to remove.
     */
    public void removeTetrimino(Tetrimino tetrimino) {
        int[][] coords = tetrimino.getCoordinates();
        for (int[] coord : coords) {
            mGrid[coord[0]][coord[1]] = -1;
        }
        
        killTetrimino(tetrimino);
    }
    
    /**
     * @return An ArrayList of all currently live Tetriminoes.
     */
    public ArrayList<Tetrimino> getLiveTetriminoes() {
        return mLiveTetriminoes;
    }
    
    /**
     * @return The number of live Tetriminoes on this Board.
     */
    public int numLiveTetriminoes() {
        return mLiveTetriminoes.size();
    }
    
    /** Add's a Tetrimino to this Board's Tetrimino Queue. 
     * 
     * @param tetrimino The Tetrimino to add to the Queue. 
     */
    public void enqueueTetrimino(Tetrimino tetrimino) {
        mTetriminoQueue.add(tetrimino);
    }
    
    /** Removes a Tetrimino from the Queue and adds it to the grid. When
     * a Tetrimino is added to the grid, the root coordinate is chosen via
     * {@link Board#getPlacementCoordinate(Tetrimino)}.
     * 
     * @throws GameOverException thrown when the target grid spaces are already occupied.
     */
    public void putTetrimino() throws GameOverException {
        Tetrimino tetrimino = mTetriminoQueue.poll();
        putTetrimino(tetrimino, getPlacementCoordinate(tetrimino));
    }
    
    /** Removes a Tetrimino from the Queue and adds it to the grid at the given 
     * root coordinate. 
     * @throws GameOverException thrown when the target grid spaces are already occupied.
     */
    public void putTetrimino(int[] rootCoordinate) throws GameOverException {
        putTetrimino(mTetriminoQueue.poll(), rootCoordinate);
    }
    
    /** Bypass the Queue and add the Tetrimino to the grid. When
     * a Tetrimino is added to the grid, the root coordinate is chosen via
     * {@link Board#getPlacementCoordinate(Tetrimino)}.
     * @throws GameOverException thrown when the target grid spaces are already occupied.
     */
    public void putTetrimino(Tetrimino tetrimino) throws GameOverException {
        putTetrimino(tetrimino, getPlacementCoordinate(tetrimino));
    }
    
    /** Bypass the Queue and add the Tetrimino to the grid at the given root coordinate. If it is 
     * found that the grid spaces are already occupied, a GameOverException will be thrown. 
     * 
     * @throws GameOverException thrown when the target grid spaces are already occupied.
     */
    public void putTetrimino(Tetrimino tetrimino, int[] rootCoordinate) throws GameOverException {
        tetrimino.setRootCoordinate(rootCoordinate);
        
        int[][] coordinates = tetrimino.getCoordinates();
        
        for (int[] coord : coordinates) {
            if (mGrid[coord[0]][coord[1]] != -1) {
                Debug.print(3, "Board#getPlacementCoordinate could not be found. Game is over.");
                
                throw new GameOverException("Grid element (" + coord[0] + ", " + coord[1] + ") was "
                        + "already occupied. Game over.");
            }
        }
        
        mLiveTetriminoes.add(tetrimino);
        
        refreshGrid();
        
        Debug.print(2, "New Tetrimino placed on the grid.");
    }
    
    /**
     * @return True IFF the Queue of Tetriminoes is smaller than the expected
     * value given by {@link Board#FULL_QUEUE_SIZE};
     */
    public boolean queueTooSmall() {
        return mTetriminoQueue.size() < FULL_QUEUE_SIZE;
    }
    
    /**
     * @return The current grid. 
     */
    public int[][] getGrid() {
        return mGrid;
    }
    
    /**
     * @return The number of non-visible buffer rows used by this Board.
     */
    public int getBuffer() {
        return mBuffer;
    }
    
    /**
     * @return The dimensions of the Grid, including the buffer region.  
     */
    public int[] getGridDimensions() {
        return new int[]{mGrid.length, mGrid[0].length};
    }
    
    /** Returns a string object representing the state of this Board. The
     * string will contain the grid only. 
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        int gridHeight = mGrid.length,
                gridWidth = mGrid[0].length;
        for (int i = 0; i < gridWidth + 2; i++) {
            result.append('*');
        }
        result.append('\n');
        
        for (int i = mBuffer; i < gridHeight; i++) {
            result.append('*');
            
            for (int j = 0; j < gridWidth; j++) {
                if (mGrid[i][j] == -1) {
                    result.append(' ');
                } else {
                    result.append('#');
                }
            }
            result.append("*\n");
        }
        
        for (int i = 0; i < gridWidth + 2; i++) {
            result.append('*');
        }
                
        Debug.print(3, "Board string generated.");
        return result.toString();
    }
}
