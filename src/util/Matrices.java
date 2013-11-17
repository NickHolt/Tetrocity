package util;

import java.util.HashSet;

import testing.Debug;

/** A utility class for matrix operations required by other Tetrocity classes. 
 * 
 * @author Nick Holt
 *
 */
public class Matrices {
    
    /** Pads a matrix with a ring of zeroes. 
     * 
     * @param matrix The matrix to pad. 
     * @return The padded matrix. 
     */
    public static int[][] padMatrix(int[][] matrix) {
        int newRows = matrix.length + 2, newCols = matrix[0].length + 2;
        int[][] paddedMatrix = new int[newRows][newCols];
        
        for (int i = 1; i < newRows - 1; i++) {
            for (int j = 1; j < newCols - 1; j++) {
                paddedMatrix[i][j] = matrix[i - 1][j - 1];
            }
        }
        
        Debug.print(3, "Matrix padded");
        return paddedMatrix;
    }

    /** A crude method for printing out matrices. Used mainly for debugging. 
     * 
     * @param matrix The matrix to be printed. 
     */
    public static void printMatrix(int[][] matrix) {
        int rows = matrix.length, cols = matrix[0].length;
        for (int i = 0; i < rows; i++) {
            
            System.out.print("[");
            for (int j = 0; j < cols; j++) {
                System.out.print(matrix[i][j] + ", ");
            }
            System.out.println("]");
        }
    }
    
    /** Removes as much "padding" (i.e. full rows and/or columns of 0's that surround
     * the matrix) as possible.
     * 
     * @return The shrunken matrix. 
     */
    public static int[][] shrink(int[][] matrix) {
        int rows = matrix.length, cols = matrix[0].length;
        HashSet<Integer> nonPaddedCols = new HashSet<Integer>()
                , nonPaddedRows = new HashSet<Integer>(); //indices of non-padded cols/rows
        /* Find which rows to keep. */
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (matrix[i][j] != 0) {
                    nonPaddedRows.add(i);
                    nonPaddedCols.add(j);
                }
            }
        }
        
        int firstCol = Integer.MAX_VALUE, lastCol = 0, //Bounds of non-padded matrix
                firstRow = Integer.MAX_VALUE, lastRow = 0; //This preserves internal 0 rows/cols. 
        for (int row : nonPaddedRows) {
            if (row < firstRow) {
                firstRow = row;
            }
            if (row > lastRow) {
                lastRow = row;
            }
        }
        for (int col : nonPaddedCols) {
            if (col < firstCol) {
                firstCol = col;
            }
            if (col > lastCol) {
                lastCol = col;
            }
        }
        
        int[][] shrunkMatrix = new int[lastRow - firstRow + 1][lastCol - firstCol + 1];
        for (int i = firstRow; i <= lastRow; i++) {
            for (int j = firstCol; j <= lastCol; j++) {
                shrunkMatrix[i - firstRow][j - firstCol] = matrix[i][j];
            }
        }
        return shrunkMatrix;
    }
}
