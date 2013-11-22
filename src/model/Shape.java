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
 *  At its core, a Shape primarily manipulates the coordinate data, as this representation is more
 * useful for external objects. A Shape also tracks Tetrimino dimensional information, such as the width,
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
    
    /** A new Shape object whose block matrix, as per the documentation, is give
     * by the matrix. This method is less efficient than {@link Shape#Shape(int[][], int)}.
     * 
     * @param matrix The matrix describing the ordering of the blocks.  
     */
    public Shape(int[][] matrix) {       
        //"Draw the smallest possible box"
        matrix = Matrices.shrink(matrix);
        ArrayList<int[]> tmpCoordinates = new ArrayList<int[]>();
        
        int rows = matrix.length, cols = matrix[0].length;
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (matrix[i][j] == 1) {
                    tmpCoordinates.add(new int[]{i, j}); 
                }
            }
        }
        
        int numBlocks = tmpCoordinates.size();
        mCoordinates = new int[numBlocks][2];
        for (int i = 0; i < numBlocks; i++) {
            mCoordinates[i] = tmpCoordinates.get(i);
        }
        
        measure(); //generate remaining member variables. 

        Debug.print(2, "New shape sucessfully created.");
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
    
    /**  
     * Calculate dimensional member variables and anchor coordinates and refresh
     * the shape matrix to match the current coordinate set. 
     */
    private void measure() {
        /* Performs three passes over the current coordinate set:
         * 1) Calculate dimensional data
         * 2) Calculate new matrix
         * 3) Calculate anchor blocks
         * 
         * This method runs in O(3N), where N is the number of coordinates. Note that
         * before the introduction of the matrix calculation, this method ran in O(N^2).
         */
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
        
        Debug.print(2, "Shape measured.");
    }
    
    /** Rotates this Shape 90 degrees clockwise.
     */
    public void rotateClockwise() {
        /* A Shape rotation is not about any specific point, as the "smallest
         * possible shape-matrix" idea must hold both before and after a rotation.
         * A valid rotation looks as follows:
         * 
         * ****    ******
         * *10* -> *1111*
         * *10*    *1000*
         * *10*    ******
         * *11*
         * ****
         * 
         * Fortunately this makes the transformation simple. Every coordinate
         * (r, c) will undergo the following mapping:
         *       (r, c) -> (c, mHeight - 1 - r)
         *       
         * To see this, consider a block initially in column c. This means that
         * the horizontal distance between that block and the leftmost block is c. 
         * After this rotation, the leftmost block becomes the topmost block, and
         * the distance between the two blocks is still c. Since the topmost block
         * is in row 0, the original block's new row is c. 
         * 
         * You can apply similar logic to see the column mapping c -> mHeight - r -1,
         * keeping in mind that the bottom block is always in row (mHeight - 1). 
         */
        
        for (int i = 0; i < mCoordinates.length; i++) {
            mCoordinates[i] = new int[]{mCoordinates[i][1],
                    mHeight - 1 - mCoordinates[i][0]};
        }   
        measure();
    }
    
    /** Rotates this Shape 90 degrees counter-clockwise about its rotational 
     * coordinate.
     */
    public void rotateCounterClockwise() {
        /* For a similar explanation on why this works, see rotateClockwise()
         */
        
        for (int i = 0; i < mCoordinates.length; i++) {
            mCoordinates[i] = new int[]{mWidth - 1 - mCoordinates[i][1],
                    mCoordinates[i][0]};
        }   
        measure();
    }
    
    /** Returns a list of [row, column] matrix-coordinates representing the
     * block matrix-coordinates on an infinitely-sized matrix. These coordinates
     * are in NO PARTICULAR ORDER. 
     * 
     *  Note that matrix indexing begins at 0. 
     *
     * @return A list of relative block matrix-coordinates. 
     */
    public int[][] getRelativeMatrixCoordinates() {
        return mCoordinates;
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
    
    /** Checks if the shape matrix is a valid description of a Tetrimino 
     * piece of the any length. For a piece to be valid, every constituent
     * block must share an edge with another (unless the length is 1). 
     * 
     *  This method assumes the matrix is a valid matrix. That is, every
     *  row has the same number of elements. 
     * 
     * @param matrix The shape matrix to validate. 
     * @return True IFF the matrix if a valid description of Tetrimino piece 
     * of length LENGTH. 
     */
    public static boolean isValidTetriminoMatrix(int[][] matrix) {
        int rows = matrix.length, cols = matrix[0].length;
        matrix = Matrices.padMatrix(matrix);
        int count = 0;
        
        for (int i = 1; i < rows + 1; i++) { //ranges adjust for padding
            for (int j = 1; j < cols + 1; j++) {
                if (matrix[i][j] != 0) {
                    count++;
                    
                    if (count != 1 //Block must have adjacent block, unless it's length 1
                            && matrix[i - 1][j] == 0
                            && matrix[i][j + 1] == 0
                            && matrix[i + 1][j] == 0
                            && matrix[i][j - 1] == 0) {
                        return false;
                    }
                }
            }
        }
        
        Debug.print(3, "Tetrimino validity checked.");
        return true;
    }
}
