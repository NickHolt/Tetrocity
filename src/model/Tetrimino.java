package model;

import java.util.ArrayList;

import testing.Debug;
import util.Direction;

/** A singular game piece for a game of Tetrocity. A Tetrimino is a 
 * grouping of one or more Blocks, along with a Shape describing their 
 * relative matrix placements and Tetrimino dimensional information. 
 *  A Block knows only of its hosting Tetrimino, its coordinate position, and
 * optionally, its identifier. Updating a Block's matrix-coordinate position can be 
 * done through a limited set of methods that ensure Block movement adheres
 * to the Tetrocity game mode's rules.
 * 
 * @author Nick Holt
 *
 */
public class Tetrimino {    
    private int mID;
    private boolean mLive;
    private Shape mShape;
    private int[] mRootCoordinate;
    
    /** A new Tetrimino whose top-leftmost Block is located at the provided
     * root coordinate.
     * 
     * @param shape The Shape that will describe the Block ordering.
     * @param row The row of the root coordinate.
     * @param col The column of the root coordinate.
     * @param ID The ID to be assigned to all Blocks in this Tetrimino. 
     */
    public Tetrimino(Shape shape, int row, int col, int ID) {
        mLive = false;
        mID = ID;
        mShape = shape;
        mRootCoordinate = new int[]{row, col};
    }   
    
    /** The primary means of updating a Tetrimino's coordinate position. Simply
     *  shifts the Tetrimino's coordinate position one unit it the specified
     *  direction by shifting all constituent Blocks in that direction.
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
    
    /** Deletes the bottom row of Blocks from this Tetrimino. 
     * 
     */
    public void deleteRow() {
        //TODO
    }
    
    /** Deletes the Block, if present, located at matrix-coordinate position
     * (row, col) from this Tetrimino. 
     * 
     * @param row The row coordinate of the Block to be deleted. 
     * @param col The column coordinate of the Block to be deleted. 
     */
    private void deleteBlock(int row, int col) {
        //TODO
        mShape.deleteBlock(row, col);
        
        
        Debug.print(1, "Block deleted at (" + row + ", " + col + ")");
    }
    
    /** Rotates this Tetrimino piece 90 degrees clockwise about its rotational 
     * coordinate.
     */
    public void rotateClockwise() {
        //TODO SHOULD DO THIS VIA SHAPE!!! THEN NOTIFY BLOCKS!!!
        //It can just randomly assign its Blocks the coordinates provided by
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
     * Block matrix-coordinates on an infinitely-sized matrix, if the top-leftmost 
     * Block has the matrix position given by this Tetrimino's root coordinate.
     * 
     *  Note that matrix indexing begins at 0. 
     * 
     * @return A list of Block matrix-coordinates. 
     */
    public int[][] getBlockCoordinates() {
        Debug.print(2, "Tetrimino (ID: " + mID + ") Block coordinates requested.");
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
     * @return the length of this Shape, defined to be the total number of Blocks. 
     */
    public int getLength() {
        return mShape.getLength();
    }
    
    /**
     * @return the width of this Shape, defined to be the horizontal span
     * of the Blocks. 
     */
    public int getWidth() {
        return mShape.getWidth();
    }
    
    /**
     * @return the height of this Shape, defined to be the vertical span
     * of the Blocks. 
     */
    public int getHeight() {
        return mShape.getHeight();
    }
}
