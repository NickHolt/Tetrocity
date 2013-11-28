package model;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

import javax.swing.JPanel;

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
public class Board extends JPanel{
    private static final long serialVersionUID = 1L;

    /* The capacity of the Queue this Board will use to track upcoming Tetriminoes. */
    public static final int FULL_QUEUE_SIZE = 3;
    
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
    /* The coordinate set that represents the position of the bottom-most live Tetrimino if it were
     * dropped */
    private int[][] mLiveTetriminoProjectionCoordinates;
    /* The stored Tetrimino. */
    private Tetrimino mStoredTetrimino;
    /* The Queue of upcoming Tetriminoes. Live Tetriminoes are taken from the Queue. */
    private ArrayBlockingQueue<Tetrimino> mTetriminoQueue;
    /* This Board's side panel. */
    private SidePanel mSidePanel;
    
    /* A set of Tetrimino colors. */
    public static final Color[] sColorSet = new Color[]{Color.BLUE, Color.CYAN,
        Color.DARK_GRAY, Color.GRAY, Color.GREEN, Color.MAGENTA, Color.ORANGE, 
        Color.PINK, Color.RED, Color.YELLOW};

    
    /** A new Board for a game of Tetrocity. A board is a (BUFFER + ROWS) x COLS matrix 
     * (grid) on which the game is played. A buffer is used to provide a number of
     * non-visible rows, so that a piece can be placed in the buffer region without
     * it instantaneously appearing on screen. 
     * 
     * @param rows The number of visible rows in this Board.
     * @param cols The number of visible columns in this Board.
     * @param buffer The number of non-visible rows in this Board. 
     */
    public Board(int rows, int cols, int buffer, int panelWidth) {
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
        
        mSidePanel = new SidePanel(panelWidth, this);
                
        setVisible(true);
        repaint();        
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
        
        repaint();
    }
    
    /** Removes all dead Tetriminoes from the grid.
     */
    public void emptyGrid() {
        for (int i = 0; i < mGrid.length; i++) {
            for (int j = 0; j < mGrid[0].length; j++) {
                mGrid[i][j] = -1;
            }
        }
        
        refreshGrid();
        repaint();
    }
    
    /** Restarting this board has the effect of emptying all grid coordinates, live Tetriminoes,
     * queue Tetriminoes, and the storage piece.
     * 
     */
    public void restartBoard() {
        mLiveTetriminoCoordinates = new HashMap<Integer, int[][]>();
        mLiveTetriminoes = new ArrayList<Tetrimino>();
        mTetriminoQueue = new ArrayBlockingQueue<Tetrimino>(FULL_QUEUE_SIZE);
        mStoredTetrimino = null;
        
        for (int i = 0; i < mGrid.length; i++) {
            for (int j = 0; j < mGrid[0].length; j++) {
                mGrid[i][j] = -1;
            }
        }

        refreshGrid();
        repaint();
        
        mSidePanel.updateQueuePieces();
        mSidePanel.updateStoragePiece();
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
        
        repaint();
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
        
        repaint();
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
    public void shiftAllLiveTetriminoes(Direction shiftDirection) {
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
        
        generateLiveTetriminoProjectionCoordinates();
                
        refreshGrid();
        repaint();
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
    public void shiftTetrimino(Direction shiftDirection) {
        int newRow = -1, newCol = -1, tetriminoID;
        boolean shiftFailed;
        Tetrimino bottomLiveTetrimino = getBottomLiveTetrimino();
        
        if (bottomLiveTetrimino != null) {
            int[][] coordinates = bottomLiveTetrimino.getCoordinates();
            tetriminoID = bottomLiveTetrimino.getID();
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
                killTetrimino(bottomLiveTetrimino); //kill the Tetrimino
            } else if (!shiftFailed) {
                bottomLiveTetrimino.shift(shiftDirection);
            }
            //Note non-south shift failures simply do nothing for that Tetrimino
            
            generateLiveTetriminoProjectionCoordinates();
                    
            refreshGrid();
            repaint();
        }
    }
    
    /** "Drops" the bottom-most live Tetrimino. That is: sets its root coordinate's row to 
     * the largest possible such that a collision does not occur during its trajectory. 
     * 
     *  Once the Tetrimino is dropped, it is killed.
     *  
     *  @return the number of lines the bottom-most Tetrimino was dropped.
     */
    public int dropTetrimino() {
        Tetrimino bottomLiveValidTetrimino = getBottomLiveTetrimino();
        int dropVal = generateLiveTetriminoProjectionCoordinates();
        
        int[] rootCoordinate = bottomLiveValidTetrimino.getRootCoordinate();
        bottomLiveValidTetrimino.setRootRow(rootCoordinate[0] + dropVal);
                        
        refreshGrid();
        mSidePanel.updateQueuePieces();
        repaint();
        
        killTetrimino(bottomLiveValidTetrimino);
        
        return dropVal;
    }
    
    /** Generates the projection coordinates of the bottom-most live Tetrimino. That is, the
     * coordinates it would have if it were dropped as far as possible. As added utility, this
     * method will return the number of lines the piece could be shifted south to achieve the
     * projection coordinates.
     * 
     * @return The number of South shifts needed to achieve the projection coordinates.
     */
    public int generateLiveTetriminoProjectionCoordinates() {
        Tetrimino bottomLiveValidTetrimino = getBottomLiveTetrimino();
        int dropVal = 0;

        if (bottomLiveValidTetrimino != null) {
            int[] rootCoord = bottomLiveValidTetrimino.getRootCoordinate();
            boolean maxFound = false;
            
            while (!maxFound) {
                dropVal++;
                
                if (hasCollision(bottomLiveValidTetrimino, 
                        new int[]{rootCoord[0] + dropVal, rootCoord[1]})) {
                    maxFound = true;
                    break;
                }          
            }
            dropVal--; //One step back was maximum
            
            Tetrimino dummyTetrimino = new Tetrimino(bottomLiveValidTetrimino.getShape()
                    , bottomLiveValidTetrimino.getID(), new int[]{rootCoord[0] + dropVal,
                    rootCoord[1]});
            
            mLiveTetriminoProjectionCoordinates = dummyTetrimino.getCoordinates();
        }
        
        return dropVal;
    }
    
    /** Stores the bottom-most Tetrimino that has not been previously stored. If a Tetrimino 
     * already exists in storage, it will be placed back on grid with the same root coordinate,
     * unless a collision is detected. If a collision is detected, all adjacent root coordinates
     * with equal or lesser row will be attempted. If all attempts fail, the storage will not occur.
     * 
     *  The attempt priority for root coordinate is: same > up > up-left > up-right > left > right.
     * 
     * @throws GameOverException thrown if the space where the new, previously stored Tetrimino
     * is already occupied. This should not happen by game design.
     */
    public void storeTetrimino() throws GameOverException {
        Tetrimino bottomLiveValidTetrimino = getBottomLiveTetrimino();
        
        if (bottomLiveValidTetrimino != null 
                &&!bottomLiveValidTetrimino.hasBeenStored()) {
            int[] rootCoordinate = bottomLiveValidTetrimino.getRootCoordinate();
            removeTetrimino(bottomLiveValidTetrimino);
            
            Tetrimino tmp = mStoredTetrimino;
            
            mStoredTetrimino = bottomLiveValidTetrimino;
            mStoredTetrimino.markStored();
            
            if (tmp != null) { //Attempt to put
                if (!hasCollision(tmp, rootCoordinate)) {
                    putTetrimino(tmp, rootCoordinate);
                } else if (rootCoordinate[0] - 1 > 0
                        && !hasCollision(tmp, new int[]{rootCoordinate[0] - 1, rootCoordinate[1]})) {
                    putTetrimino(tmp, new int[]{rootCoordinate[0] - 1, rootCoordinate[1]});
                } else if (rootCoordinate[0] - 1 > 0
                        && rootCoordinate[1] - 1 > 0
                        && !hasCollision(tmp, new int[]{rootCoordinate[0] - 1, rootCoordinate[1] - 1})) {
                    putTetrimino(tmp, new int[]{rootCoordinate[0] - 1, rootCoordinate[1] - 1});
                } else if (rootCoordinate[0] - 1 > 0
                        && rootCoordinate[1] + 1 > 0
                        && !hasCollision(tmp, new int[]{rootCoordinate[0] - 1, rootCoordinate[1] + 1})) {
                    putTetrimino(tmp, new int[]{rootCoordinate[0] - 1, rootCoordinate[1] + 1});
                } else if (rootCoordinate[0] > 0
                        && rootCoordinate[1] - 1 > 0
                        && !hasCollision(tmp, new int[]{rootCoordinate[0], rootCoordinate[1] - 1})) {
                    putTetrimino(tmp, new int[]{rootCoordinate[0], rootCoordinate[1] - 1});
                } else if (rootCoordinate[0] > 0
                        && rootCoordinate[1] + 1 > 0
                        && !hasCollision(tmp, new int[]{rootCoordinate[0], rootCoordinate[1] + 1})) {
                    putTetrimino(tmp, new int[]{rootCoordinate[0], rootCoordinate[1] + 1});
                }
            }
            
            mSidePanel.updateStoragePiece();
            generateLiveTetriminoProjectionCoordinates();
        }         
    }
    
    /** Replace the Tetrimino currently in storage with the input Tetrimino. This will
     * completely delete the stored Tetrimino.
     */
    public void storeTetrimino(Tetrimino tetrimino) {
        mStoredTetrimino = tetrimino;
        mStoredTetrimino.markStored();
        mSidePanel.updateStoragePiece();
        
        generateLiveTetriminoProjectionCoordinates();
    }
    
    /** Attempts to rotate the bottom live Tetrimino piece clockwise. This method implements fluid
     * rotation. If a collision is detected after rotation, this method will attempt to shift the
     * Tetrimino piece West an check if there is still a collision. It will do this a number
     * of times equal to the width of the Tetrimino piece. If there are still collisions after
     * the full number of shifts has been exhausted, the rotation will fail and piece will not
     * be moved (or rather, it will be moved back to its original location). 
     * 
     *  Fluid rotation causes the Tetrimino piece to be "pushed" to the left in the event that
     * an eastward collision occurs. This prevents "piece locking" and greatly improves the
     * fluidity and intuitiveness of the controls. 
     * 
     *  It should be noted that because of how the rotation algorithm works (see 
     * {@link Shape#rotateClockwise()}), it is only ever necessary to push a Tetrimino to the left.
     */    
    public void rotateTetriminoClockwise() {
        Tetrimino bottomLiveValidTetrimino = getBottomLiveTetrimino();

        if (bottomLiveValidTetrimino != null) {
            bottomLiveValidTetrimino.rotateClockwise();
            
            int width = bottomLiveValidTetrimino.getShape().getWidth(),
                    shiftVal = 0;
            boolean shiftSuccess = false;
            while (shiftVal < width) {
                if (!hasCollision(bottomLiveValidTetrimino)) {
                    shiftSuccess = true;
                    break;
                } else {
                    shiftVal++;
                    bottomLiveValidTetrimino.shift(Direction.WEST);
                }
            }
            
            if (!shiftSuccess) {
                bottomLiveValidTetrimino.setRootColumn(bottomLiveValidTetrimino.getRootCoordinate()[1]
                        + shiftVal); //shift it back
                bottomLiveValidTetrimino.rotateCounterClockwise(); //rotate it back
            } else {
                generateLiveTetriminoProjectionCoordinates();

                refreshGrid();
                repaint();
            }
        }
    }
    
    /** Attempts to rotate the bottom live Tetrimino piece counter-clockwise. This method implements 
     * fluid rotation. If a collision is detected after rotation, this method will attempt to shift the
     * Tetrimino piece West an check if there is still a collision. It will do this a number
     * of times equal to the width of the Tetrimino piece. If there are still collisions after
     * the full number of shifts has been exhausted, the rotation will fail and piece will not
     * be moved (or rather, it will be moved back to its original location). 
     * 
     *  Fluid rotation causes the Tetrimino piece to be "pushed" to the left in the event that
     * an eastward collision occurs. This prevents "piece locking" and greatly improves the
     * fluidity and intuitiveness of the controls. 
     * 
     *  It should be noted that because of how the rotation algorithm works (see 
     * {@link Shape#rotateCounterClockwise()}), it is only ever necessary to push a Tetrimino to the 
     * left.
     */      
    public void rotateTetriminoCounterClockwise() {
        Tetrimino bottomLiveValidTetrimino = getBottomLiveTetrimino();

        if (bottomLiveValidTetrimino != null) {
            bottomLiveValidTetrimino.rotateCounterClockwise();
            
            int width = bottomLiveValidTetrimino.getShape().getWidth(),
                    shiftVal = 0;
            boolean shiftSuccess = false;
            while (shiftVal < width) {
                if (!hasCollision(bottomLiveValidTetrimino)) {
                    shiftSuccess = true;
                    break;
                } else {
                    shiftVal++;
                    bottomLiveValidTetrimino.shift(Direction.WEST);
                }
            }
            
            if (!shiftSuccess) {
                bottomLiveValidTetrimino.setRootColumn(bottomLiveValidTetrimino.getRootCoordinate()[1]
                        + shiftVal); //shift it back
                bottomLiveValidTetrimino.rotateClockwise(); //rotate it back
            } else {
                generateLiveTetriminoProjectionCoordinates();

                refreshGrid();
                repaint();
            }
        }
    }
    
    /**
     * @return The bottom-most live Tetrimino.
     */
    public Tetrimino getBottomLiveTetrimino() {
        if (mLiveTetriminoes.size() == 0) {
            return null;
        }
        
        Tetrimino bottomLiveValidTetrimino = mLiveTetriminoes.get(0);
        
        for (Tetrimino liveTetrimino : mLiveTetriminoes) {
            if (liveTetrimino.getBottomRow() > bottomLiveValidTetrimino.getBottomRow()) {
                bottomLiveValidTetrimino = liveTetrimino;
            }
        }
        
        return bottomLiveValidTetrimino;
    }
    
    /**
     * @return The topmost live Tetrimino.
     */
    public Tetrimino getTopLiveTetrimino() {
        if (mLiveTetriminoes.size() == 0) {
            return null;
        }
        
        Tetrimino topLiveValidTetrimino = mLiveTetriminoes.get(0);
        
        for (Tetrimino liveTetrimino : mLiveTetriminoes) {
            if (liveTetrimino.getTopRow() < topLiveValidTetrimino.getTopRow()) {
                topLiveValidTetrimino = liveTetrimino;
            }
        }
        
        return topLiveValidTetrimino;
    }
    
    /** Removes the Tetrimino from the list of live Tetriminoes. A "dead" Tetrimino will
     * continue to exist on the grid until it is cleared completely though normal gameplay.
     * 
     * @param tetrimino The Tetrimino to kill
     */
    public void killTetrimino(Tetrimino tetrimino) {
        mLiveTetriminoes.remove(tetrimino);
        mLiveTetriminoCoordinates.remove(tetrimino.getID());
        
        generateLiveTetriminoProjectionCoordinates();
    }
    
    /** Removes the Tetrimino from the grid and the list of live Tetriminoes. 
     * 
     * @param tetrimino The Tetrimino to remove.
     */
    public void removeTetrimino(Tetrimino tetrimino) {
        int[][] coords = tetrimino.getCoordinates();
        for (int[] coord : coords) {
            mGrid[coord[0]][coord[1]] = -1;
        }
        
        killTetrimino(tetrimino);

        repaint();
    }
    
    public Tetrimino getStoredTetrimino() {
        return mStoredTetrimino;
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
        
        mSidePanel.updateQueuePieces();
    }
    
    private ArrayBlockingQueue<Tetrimino> getTetriminoQueue() {
        return mTetriminoQueue;
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
        if (hasCollision(tetrimino, rootCoordinate)) {
            throw new GameOverException("Collision detected. Game over.");
        } else {
            tetrimino.setRootCoordinate(rootCoordinate);
            
            mLiveTetriminoes.add(tetrimino);
            
            generateLiveTetriminoProjectionCoordinates();
            
            refreshGrid();
            repaint();            
        }
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
                
        return result.toString();
    }
    
    /** Checks if a collision would occur if the Tetrimino were placed on this board at its
     * existing root coordinate.
     * 
     * @param tetrimino The Tetrimino to be placed.
     * @return True IFF no collision would occur. 
     */
    public boolean hasCollision(Tetrimino tetrimino) {
        if (tetrimino == null) {
            return false;
        }
        
        int[][] coordinates = tetrimino.getCoordinates();
        for (int[] coord : coordinates) {
            if (coord[0] < 0
                    || coord[0] >= mGrid.length
                    || coord[1] < 0
                    || coord[1] >= mGrid[0].length
                    || !(mGrid[coord[0]][coord[1]] == -1 
                    || mGrid[coord[0]][coord[1]] == tetrimino.getID())) {
                return true;
            }
        }
        
        return false;
    }
    
    /** Checks if a collision would occur if the Tetrimino were placed on this board at the
     * given root coordinate. 
     * 
     * @param tetrimino The Tetrimino to be placed.
     * @param rootCoordinate The desired root coordinate of the Tetrimino.
     * @return True IFF no collision would occur. 
     */
    public boolean hasCollision(Tetrimino tetrimino, int[] rootCoordinate) {
        if (tetrimino == null) {
            return false;
        }
        
        Tetrimino dummyTetrimino = new Tetrimino(tetrimino.getShape(), tetrimino.getID());
        dummyTetrimino.setRootCoordinate(rootCoordinate);
        
        int[][] coordinates = dummyTetrimino.getCoordinates();
        for (int[] coord : coordinates) {
            if (coord[0] < 0
                    || coord[0] >= mGrid.length
                    || coord[1] < 0
                    || coord[1] >= mGrid[0].length
                    || !(mGrid[coord[0]][coord[1]] == -1 
                    || mGrid[coord[0]][coord[1]] == dummyTetrimino.getID())) {
                return true;
            }
        }
        
        return false;
    }
    
    public SidePanel getSidePanel() {
        return mSidePanel;
    }
    
    public float squareHeight() {
        return  (float) getHeight() / (float) (mGrid.length - mBuffer);
    }
    
    public float squareWidth() {
        return (float) getWidth() / (float) mGrid[0].length;
    }
    
    public void paint(Graphics g) {
        super.paint(g);
        drawGrid(g);
                
        for (int i = mBuffer; i < mGrid.length; i++) {
            for (int j = 0; j < mGrid[0].length; j++) {
                if (mGrid[i][j] != -1) {
                    drawSquare(g, j * squareWidth(),
                            (i - mBuffer)  * squareHeight(), mGrid[i][j]);
                }
            }
        }
        
        for (int[] projection : mLiveTetriminoProjectionCoordinates) {
            drawOpaqueSquare(g, projection[1] * squareWidth(),
                    (projection[0] - mBuffer) * squareHeight());
        }        
    }
    
    private void drawGrid(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
                
        int width = (int) getWidth(),
                height = (int) getHeight();
        for (int i = 0; i < height / squareHeight(); i++) {
            g.drawLine(0, (int) (squareHeight() * (i + 1)), width, (int) (squareHeight() * (i + 1)));
        }
        for (int i = 0; i < width / squareWidth(); i++) {
            g.drawLine((int) (squareWidth() * (i + 1)), 0, (int) (squareWidth() * (i + 1)), height);
        }
        
        g.setColor(Color.BLACK);
        g.drawLine(0, (int) getHeight() - 1, (int) getWidth(),
                (int) getHeight() - 1);
    }
    
    private void drawSquare(Graphics g, float x, float y, int tetriminoID) {
        Color color = sColorSet[tetriminoID % sColorSet.length];
        g.setColor(color);
        g.fillRect((int) x + 1, (int) y + 1, (int) (squareWidth() - 1), (int) (squareHeight() - 1));
        
        g.setColor(color.brighter());
        g.drawLine((int) x, (int) (y + squareHeight() - 1), (int) x, (int) y);
        g.drawLine((int) x, (int) y, (int) (x + squareWidth() - 1), (int) y);
        
        g.setColor(color.darker());
        g.drawLine((int) x + 1, (int) (y + squareHeight() - 1),
                (int) (x + squareWidth() - 1), (int) (y + squareHeight() - 1));
        g.drawLine((int) (x + squareWidth() - 1), (int) (y + squareHeight() - 1), 
                (int) (x + squareWidth() - 1), (int) y + 1);
    }
    
    /** Draws an opaque, grey square at the provided coordinates. 
     */
    private void drawOpaqueSquare(Graphics g, float x, float y) {
        Color baseColor = Color.GRAY;
        Color color = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue()
                , baseColor.getAlpha() / 3);
        g.setColor(color);
        g.fillRect((int) x + 1, (int) y + 1, (int) (squareWidth() - 1), (int) (squareHeight() - 1));
        
        g.setColor(color.brighter());
        g.drawLine((int) x, (int) (y + squareHeight() - 1), (int) x, (int) y);
        g.drawLine((int) x, (int) y, (int) (x + squareWidth() - 1), (int) y);        
        g.drawLine((int) x + 1, (int) (y + squareHeight() - 1),
                (int) (x + squareWidth() - 1), (int) (y + squareHeight() - 1));
        g.drawLine((int) (x + squareWidth() - 1), (int) (y + squareHeight() - 1), 
                (int) (x + squareWidth() - 1), (int) y + 1);
    }
    
    /** A Board's side panel in a game of Tetrocity. A SidePanel is JPanel that contains
     * the storage and queue graphical information. A SidePanel can only be instantiated by its 
     * hosting {@link Board}. 
     * 
     *  Internally, a SidePanel is a grid identical to one used by a Board. The grid is split
     * into a number of equal-length subsections equal to the full queue size + 1. The bottom
     * subsection is used for the storage piece, and the rest are used for the queue. The first
     * piece in the queue will be placed in the top subsection. 
     * 
     *  It should be noted that if the width or Board height are not adequate to represent
     * the full queue and storage piece on screen, which will have a 1:1 size ratio with the 
     * pieces on the Board, then proper behavior is not guaranteed. In fact, it will almost
     * certainly fail.
     * 
     * @author Nick Holt
     *
     */
    public class SidePanel extends JPanel{
        private static final long serialVersionUID = 1L;
        int[][] mSideGrid;
        int mSectionHeight;
        Board mBoard;
        
        /** A new SidePanel for a Board in a game of Tetrocity. The constructor is private,
         * ensuring only a Board is capable of instantiating it such that this object will
         * have access to the proper information. 
         * 
         *  A SidePanel must have the same number of rows as its hosting Board. The width,
         * however, is variable. 
         * 
         * @param gridDimensions The dimensions 
         * @param board
         */
        private SidePanel(int width, Board board) {
            mSideGrid = new int[board.getGrid().length - board.getBuffer()][width];
            
            for (int i = 0; i < mSideGrid.length; i++) {
                for (int j = 0; j < mSideGrid[0].length; j++) {
                    mSideGrid[i][j] = -1;
                }
            }
            
            mSectionHeight = mSideGrid.length / (FULL_QUEUE_SIZE + 1); //queue + storage piece
            mBoard = board;
            
            setVisible(true);
            repaint(); //paint grid
        }
        
        /** Updates this SidePanel's grid with the current storage piece. 
         */
        public void updateStoragePiece() {
            emptyStorageSideGridSegment();
                        
            Tetrimino storedTetrimino = mBoard.getStoredTetrimino();

            if (storedTetrimino != null) {
                int tetriminoHeight = storedTetrimino.getShape().getHeight(),
                        tetriminoWidth = storedTetrimino.getShape().getWidth();
                
                int row = mSideGrid.length - (mSectionHeight + tetriminoHeight) / 2,
                        col = (mSideGrid[0].length - tetriminoWidth) / 2;
                
                int tetriminoID = storedTetrimino.getID();
                Tetrimino dummyTetrimino = new Tetrimino(storedTetrimino.getShape(),
                        tetriminoID, new int[]{row, col});
                
                int[][] coordinates = dummyTetrimino.getCoordinates();
                for (int[] coord : coordinates) {
                    mSideGrid[coord[0]][coord[1]] = tetriminoID;
                }
            }
            
            repaint();
        }
        
        public void updateQueuePieces() {
            emptyQueueSideGridSegment();

            ArrayBlockingQueue<Tetrimino> queue = copyTetriminoQueue(mBoard.getTetriminoQueue());
            
            Tetrimino tetrimino, dummyTetrimino;
            int tetriminoHeight, tetriminoWidth, tetriminoID, row, col;
            int[][] coordinates;
            int queueSize = queue.size();
            for (int i = 0; i < queueSize; i++) {
                tetrimino = queue.poll();
                
                tetriminoHeight = tetrimino.getShape().getHeight();
                tetriminoWidth = tetrimino.getShape().getWidth();
                tetriminoID = tetrimino.getID();
                
                row = (i + 1) * mSectionHeight - (mSectionHeight + tetriminoHeight) / 2;
                col = (mSideGrid[0].length - tetriminoWidth) / 2;
                                
                dummyTetrimino = new Tetrimino(tetrimino.getShape(), tetriminoID, new int[]{row, col});
                
                coordinates = dummyTetrimino.getCoordinates();
                for (int[] coord : coordinates) {
                    mSideGrid[coord[0]][coord[1]] = tetriminoID;
                }
            }
            
            repaint();
        }
        
        public int[][] getSideGrid() {
            return mSideGrid;
        }
        
        public void emptyQueueSideGridSegment() {
            for (int i = 0; i < (mSideGrid.length - 1) - mSectionHeight; i++) {
                for (int j = 0; j < mSideGrid[0].length; j++) {
                    mSideGrid[i][j] = -1;
                }
            }
        }
        
        public void emptyStorageSideGridSegment() {
            for (int i = (mSideGrid.length - 1) - mSectionHeight; i < mSideGrid.length; i++) {
                for (int j = 0; j < mSideGrid[0].length; j++) {
                    mSideGrid[i][j] = -1;
                }
            }
        }
        
        /** Copies the input Tetrimino Queue to a new Queue, while leaving the input
         * Queue in the same state it was passed in. 
         * 
         * @return The Queue to copy. 
         */
        private ArrayBlockingQueue<Tetrimino> copyTetriminoQueue(ArrayBlockingQueue<Tetrimino> queue) {
            int queueSize = queue.size();
            ArrayBlockingQueue<Tetrimino> copy;
            
            if (queueSize != 0) {
                copy = new ArrayBlockingQueue<Tetrimino>(queueSize);
                
                Tetrimino tetrimino;
                for (int i = 0; i < queueSize; i++) {
                    tetrimino = queue.poll();
                    copy.add(tetrimino);
                    queue.add(tetrimino);
                }
            } else {
                copy = queue;
            }
            
            
            return copy;
        }
        
        /** Returns a string object representing the state of this Board. The
         * string will contain the grid only. 
         */
        public String toString() {
            StringBuffer result = new StringBuffer();
            int gridHeight = mSideGrid.length,
                    gridWidth = mSideGrid[0].length;
            for (int i = 0; i < gridWidth + 2; i++) {
                result.append('*');
            }
            result.append('\n');
            
            for (int i = 0; i < gridHeight; i++) {
                result.append('*');
                
                for (int j = 0; j < gridWidth; j++) {
                    if (mSideGrid[i][j] == -1) {
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
                    
            return result.toString();
        }
        
        public float squareHeight() {
            return  (float) getHeight() / (float) (mSideGrid.length);
        }
        
        public float squareWidth() {
            return (float) getWidth() / (float) mSideGrid[0].length;
        }
        
        public void paint(Graphics g) {
            super.paint(g);
            drawGrid(g);

            for (int i = 0; i < mSideGrid.length; i++) {
                for (int j = 0; j < mSideGrid[0].length; j++) {
                    if (mSideGrid[i][j] != -1) {
                        drawSquare(g, j * squareWidth(),
                                i  * squareHeight(), mSideGrid[i][j]);
                    }
                }
            }
        }
        
        private void drawGrid(Graphics g) {
            g.setColor(Color.LIGHT_GRAY);
                    
            int width = (int) getWidth(),
                    height = (int) getHeight();
            for (int i = 0; i < height / squareHeight(); i++) {
                g.drawLine(0, (int) (squareHeight() * (i + 1)), width, (int) (squareHeight() * (i + 1)));
            }
            for (int i = 0; i < width / squareWidth(); i++) {
                g.drawLine((int) (squareWidth() * (i + 1)), 0, (int) (squareWidth() * (i + 1)), height);
            }
            
            g.setColor(Color.BLACK);
            g.drawLine((int) getWidth() - 1, 0,
                    (int) getWidth() - 1, (int) getHeight() - 1);
            g.drawLine(0, (int) (getHeight() - mSectionHeight * squareHeight()), 
                    (int) getWidth() - 1, 
                    (int) (getHeight() - mSectionHeight * squareHeight()));
            g.drawLine(0, (int) getHeight() - 1, (int) getWidth(),
                    (int) getHeight() - 1);
        }
        
        private void drawSquare(Graphics g, float x, float y, int tetriminoID) {
            Color color;
            color = sColorSet[tetriminoID % sColorSet.length];
            
            g.setColor(color);
            g.fillRect((int) x + 1, (int) y + 1, (int) (squareWidth() - 1), 
                    (int) (squareHeight() - 1));
            
            g.setColor(color.brighter());
            g.drawLine((int) x, (int) (y + squareHeight() - 1), (int) x, (int) y);
            g.drawLine((int) x, (int) y, (int) (x + squareWidth() - 1), (int) y);
            
            g.setColor(color.darker());
            g.drawLine((int) x + 1, (int) (y + squareHeight() - 1),
                    (int) (x + squareWidth() - 1), (int) (y + squareHeight() - 1));
            g.drawLine((int) (x + squareWidth() - 1), (int) (y + squareHeight() - 1), 
                    (int) (x + squareWidth() - 1), (int) y + 1);
        }
    }
}