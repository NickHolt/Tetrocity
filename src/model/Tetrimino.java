package model;

import testing.Debug;
import util.Direction;

/** A singular game piece for a game of Tetrocity. A Tetrimino is a 
 * grouping of one or more blocks, with a Shape describing their 
 * relative matrix placements and Tetrimino dimensional information.
 *  The constituent blocks of a Tetrimino piece do not have classes of their
 * own. Instead, they are represented by matrix-coordinate positions. As such,
 * a Tetrimino is essentially a {@link Shape} with an ID an absolute matrix
 * position. All shape information, such as the relative placement of
 * the Tetrimino's constituent blocks are known by the Tetrimino's Shape. 
 * That being said, all in-game manipulation is done through the Tetrimino;
 * its Shape is not accessible to outside objects. 
 * 
 * @author Nick Holt
 *
 */
public class Tetrimino {    
    private int mID;
    private boolean mLive;
    private Shape mShape;
    private int[] mRootCoordinate;
    
    /** A new Tetrimino whose top-leftmost block is located at the provided
     * root coordinate.
     * 
     * @param shape The Shape that will describe the block ordering.
     * @param row The row of the root coordinate.
     * @param col The column of the root coordinate.
     * @param ID The ID to be assigned to all blocks in this Tetrimino. 
     */
    public Tetrimino(Shape shape, int row, int col, int ID) {
        mLive = false;
        mID = ID;
        mShape = shape;
        mRootCoordinate = new int[]{row, col};
    }   
    
    /** The primary means of updating a Tetrimino's coordinate position. Simply
     *  shifts the Tetrimino's coordinate position one unit it the specified
     *  direction by shifting all constituent blocks in that direction.
     * 
     * @param direction The direction to shift the Tetrimino. 
     */
    public void shift(Direction direction) {
        if (direction == Direction.NORTH) {
            mRootCoordinate[0] -= 1;
        } else if (direction == Direction.EAST) {
            mRootCoordinate[1] += 1;
        } else if (direction == Direction.SOUTH){
            mRootCoordinate[0] += 1;
        } else if (direction == Direction.WEST) {
            mRootCoordinate[1] -= 1;
        }
        
        Debug.print(2, "Tetrimino " + mID + " shifted " + direction);
    }
    
    /** Deletes the bottom row of blocks from this Tetrimino. 
     * 
     */
    public void deleteRow() {
        //TODO
    }
    
    /** Deletes the block, if present, located at matrix-coordinate position
     * (row, col) from this Tetrimino. 
     * 
     * @param row The row coordinate of the block to be deleted. 
     * @param col The column coordinate of the block to be deleted. 
     */
    private void deleteBlock(int row, int col) {
        //TODO
        mShape.deleteBlock(row, col);
        
        
        Debug.print(1, "block deleted at (" + row + ", " + col + ")");
    }
    
    /** Rotates this Tetrimino piece 90 degrees clockwise about its rotational 
     * coordinate.
     */
    public void rotateClockwise() {
        //TODO SHOULD DO THIS VIA SHAPE!!! THEN NOTIFY blockS!!!
        //It can just randomly assign its blocks the coordinates provided by
        //Shape. They're all the same anyway.
        mShape.rotateClockwise();
        //int[][] newCoords = mShape.getC
    }
    
    /** Rotates this Tetrimino piece 90 degrees counter-clockwise about its rotational 
     * coordinate.
     */
    public void rotateCounterClockwise() {
        //TODO
    }
    
    /** Returns a list of [row, column] matrix-coordinates representing the
     * block matrix-coordinates on an infinitely-sized matrix, if the top-leftmost 
     * block has the matrix position given by this Tetrimino's root coordinate.
     * 
     *  Note that matrix indexing begins at 0. 
     * 
     * @return A list of block matrix-coordinates. 
     */
    public int[][] getblockCoordinates() {
        Debug.print(2, "Tetrimino (ID: " + mID + ") block coordinates requested.");
        return mShape.getCoordinates(mRootCoordinate[0], mRootCoordinate[1]);
    }

    /**
     * @return the ID of this Tetrimino. 
     */
    public int getID() {
        return mID;
    }
    
    /**
     * @return the Shape first used to construct this Tetrimino.
     */
    public Shape getShape() {
        return mShape;
    }
    
    /**
     * @return The root coordinate of this Tetrimino piece. 
     */
    public int[] getRootCoordinate() {
        return mRootCoordinate;
    }
    
    public void setRootCoordinate(int[] rootCoordinate) {
        mRootCoordinate = rootCoordinate;
    }
    
    /** 
     * @return True IFF this Tetrimino is live. 
     */
    public boolean isLive() {
        return mLive;
    }
    
    
    /** Sets the live status of this Tetrimino.
     */
    public void setLive(boolean isLive) {
        mLive = isLive;
    }
    
    /**
     * @return the length of this Shape, defined to be the total number of blocks. 
     */
    public int getLength() {
        return mShape.getLength();
    }
    
    /**
     * @return the width of this Shape, defined to be the horizontal span
     * of the blocks. 
     */
    public int getWidth() {
        return mShape.getWidth();
    }
    
    /**
     * @return the height of this Shape, defined to be the vertical span
     * of the blocks. 
     */
    public int getHeight() {
        return mShape.getHeight();
    }
}
