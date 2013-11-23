package model;

import testing.Debug;
import util.Direction;

/** A singular game piece for a game of Tetrocity. A Tetrimino is a 
 * grouping of one or more blocks, with a Shape describing their 
 * relative matrix placements and Tetrimino dimensional information.
 * 
 *  The constituent blocks of a Tetrimino piece do not have classes of their
 * own. Instead, they are represented by matrix-coordinate positions. As such,
 * a Tetrimino is essentially a {@link Shape} with an ID an absolute matrix
 * position. All shape information, such as the relative placement of
 * the Tetrimino's constituent blocks are known by the Tetrimino's Shape.
 * 
 *  In order to provide the absolute matrix position information, a root 
 * coordinate may be provided. A root coordinate is found as such: the row
 * of the root coordinate is the top row of the Tetrimino, and the column is 
 * the leftmost column of the Tetrimino. Alternatively, it is the coordinate
 * of the top-left corner of the smallest possible box that encapsulates the 
 * entirety of the Tetrimino. 
 * 
 *  If a root coordinate is not provided, e.g. the Tetrimino is still in queue, it
 * is critical that it be set before any interaction with the object. 
 * 
 * @author Nick Holt
 *
 */
public class Tetrimino {    
    private final int mID;
    private boolean mBeenStored;
    private final Shape mShape;
    private int[] mRootCoordinate;
    
    /** A new Tetrimino whose relative block ordering is described by SHAPE, 
     * and ID is equal to ID. 
     * 
     *  WARNING: Until the root coordinate is set via {@link Tetrimino#setRootCoordinate(int[])},
     * all positional methods will throw exceptions. It is critical this is set before any
     * interaction with this Tetrimino.
     * 
     * @param shape The Shape that will describe the block ordering.
     * @param ID The ID to be assigned to all blocks in this Tetrimino. 
     */
    public Tetrimino(Shape shape, int ID) {
        mID = ID;
        mShape = shape;
    }   
        
    /** A new Tetrimino whose relative block ordering is described by SHAPE, 
     * root coordinate is set by ROOTCOORDINATE, and ID is equal to ID. 
     * 
     * @param shape The Shape that will describe the block ordering.
     * @param rootCoordinate The root coordinate of this Tetrimino.
     * @param ID The ID to be assigned to all blocks in this Tetrimino. 
     */
    public Tetrimino(Shape shape, int ID, int[] rootCoordinate) {
        mID = ID;
        mShape = shape;
        mRootCoordinate = rootCoordinate;
    }   
    
    /** Shifts the Tetrimimo's coordinate position (i.e. all of its block positions)
     * one unit in the given DIRECTION. 
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
    
    /** Deletes the block, if present, located at matrix-coordinate position
     * (row, col) from this Tetrimino. 
     * 
     * @param row The row coordinate of the block to be deleted. 
     * @param col The column coordinate of the block to be deleted. 
     */
    public void deleteBlock(int row, int col) {
        mShape.deleteBlock(row - mRootCoordinate[0], col - mRootCoordinate[1]);        
    }
    
    /** Rotates this Tetrimino piece 90 degrees clockwise about its rotational 
     * coordinate.
     */
    public void rotateClockwise() {
        Debug.print(2, "Tetrimino " + mID + "rotated clockwise."); //THIS SLOWS CONTROL DOWN!
        mShape.rotateClockwise();
    }
    
    /** Rotates this Tetrimino piece 90 degrees counter-clockwise about its rotational 
     * coordinate.
     */
    public void rotateCounterClockwise() {
        Debug.print(2, "Tetrimino " + mID + "rotated counterclockwise.");
        mShape.rotateCounterClockwise();
    }
    
    /** Returns a list of [row, column] matrix-coordinates representing this Tetrimino's
     * block locations on the matrix rooted on by the root coordinate. 
     * 
     * @return A list of block matrix-coordinates. 
     */
    public int[][] getCoordinates() {
        int length = mShape.getLength();
        int[][] relativeCoordinates = mShape.getRelativeMatrixCoordinates();
        int[][] result = new int[length][2];
        for(int i = 0; i < length; i++) {
            result[i] = new int[]{relativeCoordinates[i][0] + mRootCoordinate[0]
                    , relativeCoordinates[i][1] + mRootCoordinate[1]};
        }
        
        Debug.print(2, "Tetrimino (ID: " + mID + ") coordinates generated.");
        return result;
    }
    
    /**
     * @return The largest occupied row of this Tetrimino. 
     */
    public int getBottomRow() {
        return mRootCoordinate[0] + getShape().getHeight() - 1;
    }
    
    /**
     * @return True IFF there are no more blocks in this Tetrimino. 
     */
    public boolean isEmpty() {
        return getCoordinates().length == 0;
    }

    /**
     * @return the ID of this Tetrimino. 
     */
    public int getID() {
        return mID;
    }
    
    /**
     * @return the Shape first used to construct this Tetrimino. The Shape contains
     * all dimensional information, such as Tetrimino height and width. 
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
     * @return True IFF this Tetrimino has ever been stored. 
     */
    public boolean hasBeenStored() {
        return mBeenStored;
    }

    /** Mark this Tetrimino as having been stored. 
     */
    public void markStored() {
        mBeenStored = true;
    }
}
