package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

import testing.Debug;
import util.Direction;
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
    public static final int FULL_QUEUE_SIZE = 6;
    /* A grid of Tetrimino IDs representing the game board. -1 is an empty space. */
    private int[][] mGrid;
    /* A HashMap of live Tetrimino IDs to that Tetrimino's last known coordinates. */
    private HashMap<Integer, int[][]> mLiveTetriminoCoordinates;
    /* The number of non-visible buffer rows. */
    private final int mBuffer;
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
        
        mLiveTetriminoCoordinates = new HashMap<Integer, int[][]>();
        mTetriminoQueue = new ArrayBlockingQueue<Tetrimino>(FULL_QUEUE_SIZE);
        
        Debug.print(1, "New Board instantiated.");
    }
    
    /** Given the Tetrimino, returns a grid coordinate such that the root coordinate of the
     * Tetrimino may be set to it and:
     *  i) Be centered on the grid.
     *  ii) If possible, have its bottom block(s) align with the bottom of the buffer region. 
     * 
     * @param tetrimino The Tetrimino to be placed.
     * @return The placement coordinate.
     */
    public int[] getPlacementCoordinate(Tetrimino tetrimino) {
        //TODO
        return null;
    }
    
    /** Updates the grid with the current coordinate positions of all live Tetriminoes.
     * This method should be called every time movement is applied to any live Tetrimino. 
     */
    public void updateGrid() {
        int tetriminoID;
        int[][] oldCoordinates, newCoordinates;
        for (Tetrimino tetrimino : mLiveTetriminoes) {
            tetriminoID = tetrimino.getID();
            
            oldCoordinates = mLiveTetriminoCoordinates.get(tetriminoID);
            for (int[] oldCoord : oldCoordinates) {
                mGrid[oldCoord[0]][oldCoord[1]] = -1; //Empty old coordinate positions
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
     *  A row is filled when there exists a block in every position on that row. 
     * Rows are checked from the bottom up. If a row is found to be full, every
     * block present in that row is deleted from the grid, and all dead blocks 
     * above it are shifted accordingly. 
     * 
     *  The return value is the number of lines cleared by the clearRows() call. 
     * 
     * @return The number of lines cleared. 
     */
    public int clearRows() {
        //TODO
        return 0;
    }
    
    /** If possible, drops all live Tetriminoes one coordinate space south. That is,
     * it adds one to the row coordinate of every live Tetrimino. If this is not possible,
     * and there exists a block beneath any of the Tetrimino's anchor blocks, the Tetrimino 
     * will be removed from the list of live Tetriminoes.
     */
    public void dropLiveTetriminoes() {
        int[][] anchorCoordinates;
        for (Tetrimino tetrimino : mLiveTetriminoes) {
            anchorCoordinates = tetrimino.getAnchorCoordinates();
            for (int[] anchor : anchorCoordinates) {
                if (mGrid[anchor[0] + 1][anchor[1]] != -1) { //there is no empty space beneath
                    mLiveTetriminoes.remove(tetrimino); //"mark as dead"
                    break;
                }
            }
            tetrimino.shift(Direction.SOUTH); //garbage collector will take care of dead Tetriminoes
        }                                     //shift them anyway to simplify code
        
        updateGrid();
        Debug.print(2, "Live Tetriminoes dropped.");
    }
    
    /** Stores the bottom-most Tetrimino. If a Tetrimino already exists in storage,
     * it will be placed back on the top of the grid and marked as live. 
     */
    public void storeTetrimino() {
        //TODO
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
     */
    public void putTetrimino() {
        //TODO
        //make sure you update the HashMap
    }
    
    /** Bypass the Queue and add the Tetrimino to the grid. When
     * a Tetrimino is added to the grid, the root coordinate is chosen via
     * {@link Board#getPlacementCoordinate(Tetrimino)}.
     */
    public void putTetrimino(Tetrimino tetrimino) {
        //TODO
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
     * string will contain the grid, queue, and stored Tetrimino. 
     */
    public String toString() {
        //TODO
        return super.toString();
    }
}
