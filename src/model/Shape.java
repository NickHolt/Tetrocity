package model;

import java.util.ArrayList;
import java.util.Arrays;

import testing.Debug;
import util.Matrices;

/** 
 * A description of a {@link Tetrimino}'s shape. That is, the ordering of the Tetrimino's
 * constituent blocks. A Shape description is simply a LxL matrix where L is the 
 * total number of blocks, where each matrix element represents the presence of a block.
 * Any integer != 0 states that a block exists at that position, while a 0 states the 
 * opposite.
 * 
 *  Example: an "L" shape Tetrimino in the original game of Tetris:
 * 
 * ******
 * *1000*
 * *1000*
 * *1100*
 * *0000*
 * ****** 
 * 
 *  Would be represented by the following matrix:
 *  
 *  [[1, 0, 0, 0]
 *  ,[1, 0, 0, 0]
 *  ,[1, 1, 0, 0]
 *  ,[0, 0, 0, 0]].
 *  
 *  A Shape is a RELATIVE description. It knows nothing of a Tetrimino's board position.
 * Coordinates are assigned as such: given  the block layout, draw the smallest possible 
 * box around the shape. This box is the matrix to which this Shape's coordinate output 
 * refers. For example, the shape:
 * 
 * *******
 * *00000*
 * *01000*
 * *01000*
 * *01100*
 * *00000*
 * *******
 * 
 *  will be converted to:
 *  
 * ****
 * *10*
 * *10*
 * *11*
 * ****
 * 
 *  The coordinates of that shape will be: {(0, 0), (1, 0), (2, 0), (2, 1)}. 
 *  
 *  A Shape also tracks Tetrimino dimensional information, such as the width,
 * height, and length of the piece. As a result, it is critical that a Tetrimino
 * update its Shape when blocks are deleted.
 *  
 * @author Nick Holt
 *
 */
public class Shape {
    private int[][] mCoordinates;
    private int mLength;
    private int mWidth;
    private int mHeight;
    
    /** A new Shape object whose block matrix, as per the documentation, is MATRIX.
     * LENGTH is the total number of blocks that this Shape orders. 
     * 
     * @param matrix The matrix describing the ordering of the blocks.  
     * @param length The total number of blocks. 
     */
    public Shape(int[][] matrix, int length) {       
        //"Draw the smallest possible box"
        matrix = Matrices.shrink(matrix);
        
        int rows = matrix.length, cols = matrix[0].length;
        mCoordinates = new int[length][2];
        
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (matrix[i][j] == 1) {
                    mCoordinates[count] = new int[]{i, j}; 
                    count++;
                }
            }
        }
        
        if (count != length) {
            throw new IllegalArgumentException("The provided length (" + length + ") "
                    + "did not match the number of blocks found (" + count + ")");
        }
        
        measure(); //generate remaining member variables. 
        
        Debug.print(2, "New shape sucessfully created.");
    }
    
    /** Returns a list of [row, column] matrix-coordinates representing the
     * block matrix-coordinates on an infinitely-sized matrix
     * 
     *  Note that matrix indexing begins at 0. 
     *
     * @return A list of relative block matrix-coordinates. 
     */
    public int[][] getRelativeMatrixCoordinates() {
        return mCoordinates;
    }
    
    /** Returns a list of [row, column] matrix-coordinates representing the
     * block matrix-coordinates on an infinitely-sized matrix, if the top-leftmost 
     * block has the matrix position [firstRow, firstCol].
     * 
     *  Note that matrix indexing begins at 0. 
     * 
     * @param firstRow The row of the top-leftmost block. 
     * @param firstCol The column of the top-leftmost block. 
     * @return A list of block matrix-coordinates. 
     */
    public int[][] getAbsoluteMatrixCoordinates(int firstRow, int firstCol) {
        
        int[][] result = new int[mLength][2];
        for(int i = 0; i < mLength; i++) {
            result[i] = new int[]{mCoordinates[i][0] + firstRow
                    , mCoordinates[i][1] + firstCol};
        }
        
        Debug.print(2, "Matrix coordinates generated.");
        return result;
    }
    
    /** Informs this Shape that the block positioned at matrix coordinate (ROW, COL).
     *  It is critical to note that the ROW, COL used on this method must refer to the
     * relative coordinate matrix used to construct this Shape.
     *  This method should not be used by any other class except for {@link Tetrimino},
     * via {@link Tetrimino#deleteBlock}.
     * 
     * @param row The row of the deleted block. 
     * @param col The column of the deleted block. 
     */
    public void deleteBlock(int row, int col) {
        int[][] newCoords = new int[mCoordinates.length - 1][2];
        int[] badCoord = new int[]{row, col};
        
        int i = 0;
        for (int[] coord : mCoordinates) {
            if (!Arrays.equals(coord, badCoord)) {
                newCoords[i] = coord;
                i++;
            }
        }
        
        mCoordinates = newCoords;
        measure(); //Re-calculate dimensional data
    }
    
    /** Deletes the bottom row of blocks from this Shape. 
     */
    public void deleteRow() {
        int[][] bottomRow = getBottomRow();
        for (int[] coord : bottomRow) {
            deleteBlock(coord[0], coord[1]);
        }
        
        measure(); //Re-calculate dimensional data
    }
    
    /** Return a list of relative matrix-coordinates representing the 
     * block locations present in the bottom row of this Shape, where the
     * top-leftmost block is located at (FIRSTROW, FIRSTCOL). 
     * 
     * @param firstRow The row of the top-leftmost block. 
     * @param firstCol The column of the top-leftmost block. 
     * @return
     */
    public int[][] getBottomRow() {
        ArrayList<int[]> bottomRow = new ArrayList<int[]>();
        for (int[] coord : mCoordinates) {
            if (coord[0] == mHeight - 1) { //block is at the bottom
                bottomRow.add(new int[]{coord[0], coord[1]});
            }
        }
        
        int[][] result = new int[bottomRow.size()][2];
        for (int i = 0; i < bottomRow.size(); i++) {
            result[i] = bottomRow.get(i);
        }
        
        Debug.print(3, "Shape#getBottomRow called.");
        return result;
    }
    
    /**  
     * Calculate dimensional member variables based on block coordinates.
     */
    private void measure() {
        mLength = mHeight = mWidth = 0; 
        
        for (int[] coord : mCoordinates) {
            mLength++;
            if (coord[0] > mHeight) {
                mHeight = coord[0];
            }
            if (coord[1] > mWidth) {
                mWidth = coord[1];
            }
        }
        mHeight++; //adjust for 0-indexing
        mWidth++; 
        
        Debug.print(3, "Shape#measure() completed.");
    }
    
    /** Rotates this Shape 90 degrees clockwise about its rotational 
     * coordinate.
     */
    public void rotateClockwise() {
        //TODO SHOULD DO THIS VIA SHAPE!!! THEN NOTIFY blockS!!!
        //It can just randomly assign its blocks the coordinates provided by
        //Shape. They're all the same anyway. 
    }
    
    /** Rotates this Shape 90 degrees counter-clockwise about its rotational 
     * coordinate.
     */
    public void rotateCounterClockwise() {
        //TODO
    }
    
    /** Find the coordinate point around which to rotate this Shape. 
     * 
     * @return The rotational coordinate. 
     */
    public int[] getRotationalCoordinate() {
        int row = getHeight() / 2 - 1, col = getWidth() / 2 - 1; 
                                                // - 1 to adjust for 0-indexing
        Debug.print(3, "Rotational coordinate found: (" + row + ", " + col + ").");
        return new int[]{row, col};
    }
    
    /**
     * @return the length of this Shape, defined to be the total number of blocks. 
     */
    public int getLength() {
        return mLength;
    }
    
    /**
     * @return the width of this Shape, defined to be the horizontal span
     * of the blocks. 
     */
    public int getWidth() {
        return mWidth;
    }
    
    /**
     * @return the height of this Shape, defined to be the vertical span
     * of the blocks. 
     */
    public int getHeight() {
        return mHeight;
    }
}
