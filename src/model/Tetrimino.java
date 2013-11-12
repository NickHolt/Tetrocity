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
    private ArrayList<Block> mBlocks;
    private int mID;
    private boolean mLive;
    private Shape mShape;
    
    /** A new Tetrimino whose top-leftmost Block is located at the provided
     * coordinates. 
     * 
     * @param shape The Shape that will describe the initial Block ordering.
     * @param ID The ID to be assigned to all Blocks in this Tetrimino. 
     */
    public Tetrimino(Shape shape, int row, int col, int ID) {
        mLive = false;
        mID = ID;
        
        mBlocks = new ArrayList<Block>();
        int[][] coordinates = shape.getMatrixCoordinates(row, col);
        
        for (int[] coordinate : coordinates) {
            mBlocks.add(new Block(this, coordinate[0], coordinate[1], getID()));
        }
    }   
    
    /** The primary means of updating a Tetrimino's coordinate position. Simply
     *  shifts the Tetrimino's coordinate position one unit it the specified
     *  direction by shifting all constituent Blocks in that direction.
     * 
     * @param direction The direction to shift the Tetrimino. 
     */
    public void shift(Direction direction) {
        for (Block b : mBlocks) {
            b.shift(direction);
        }
        
        Debug.print(2, "Tetrimino " + mID + " shifted " + direction);
    }
    
    public void deleteRow() {
        //TODO
    }
    
    private void deleteBlock(int row, int col) {
        //TODO
//        mShape.deleteBlock(row, col);
    }
    

    /** Find the coordinate point around which to rotate this Tetrimino. 
     * 
     * @return The rotational coordinate. 
     */
    private int[] findRotationalAxis() {
        //TODO
        return new int[]{0, 0};
    }
    
    /**
     * @return the Blocks currently contained by this Tetrimino. 
     */
    public Block[] getBlocks() {        
        return (Block[]) mBlocks.toArray();
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
