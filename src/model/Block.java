package model;

import testing.Debug;
import util.Direction;

/** A singular building block for a {@link Tetrimino} piece in a game of Tetrocity. A Block
 * will have a hosting Tetrimino that will group it with zero or more Blocks.
 * 
 *  This class should never be instantiated by an non-Tetrimino entity. This is done
 * to provide type-safety and to ensure that all Blocks of the hosting Tetrimino
 * have identical identifiers (including null). As such, to obtain a single Block, 
 * one should instead create a single block-length Tetrimino piece. 
 * 
 *  To instantiate a Block, an initial matrix-coordinate position and Id must be provided. 
 * Again, this should all be handled exclusively through Tetrimino internal logic. 
 * Note that none of these value may be changed after instantiation.
 * 
 * @author Nick Holt
 *
 */
public class Block {
    private Tetrimino mTetrimino;
    private int[] mCoordinate;
    private int mID;
    
    /** A new Block instance.
     * 
     * @param tetrimino This Block's hosting Tetrimino. 
     * @param xPos The initial x-coordinate of this Block. 
     * @param yPos The initial y-coordinate of this Block. 
     */
    public Block(Tetrimino tetrimino, int row, int col, int ID) {

        mTetrimino = tetrimino;
        mCoordinate = new int[]{row, col};
        mID = ID;
        
        Debug.print(1, "Block created. ID: " + ID + ". Initial Matrix-Coordinate: ["
                + row + ", " + col + "].");
    }
    
    /** The primary means of updating a Block's coordinate position. Simply
     *  shifts the Block's coordinate position one unit it the specified
     *  direction. 
     * 
     * @param direction The direction to shift the Block. 
     */
    public void shift(Direction direction) {
        if (direction == Direction.NORTH) {
            setRow(getRow() - 1); 
        } else if (direction == Direction.EAST) {
            setColumn(getColumn() + 1);
        } else if (direction == Direction.SOUTH){
            setRow(getRow() + 1);
        } else if (direction == Direction.WEST) {
            setColumn(getColumn() - 1);
        }
        
        Debug.print(2, "Block " + mID + " shifted " + direction.toString());
    }
    
    /**
     * @return This Block's hosting Tetrimino. 
     */
    public Tetrimino getTetrimino() {
        return mTetrimino;
    }
    
    /**
     * @return This Block's matrix row. 
     */
    public int getRow() {
        return mCoordinate[0];
    }
    
    /** 
     * @param xPos The new row of this Block.
     */
    private void setRow(int row) {
        mCoordinate[0] = row; 
    }
    
    /**
     * @param yPos The new column of this Block.
     */
    private void setColumn(int column) {
        mCoordinate[1] = column; 
    }
    
    /** Set the matrix coordinate of this Block. 
     * 
     * @param coordinate The matrix-coordate. 
     */
    public void setCoordinate(int[] coordinate) {
        setRow(coordinate[0]); 
        setColumn(coordinate[1]);
    }
    
    /**
     * @return This Block's matrix column. 
     */
    public int getColumn() {
        return mCoordinate[1];
    }
    
    /** 
     * @return This Block's ID.
     */
    public int getId() {
        return mID;
    }

}
