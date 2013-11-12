package util;

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
        int rows = matrix.length, cols;
        for (int i = 0; i < rows; i++) {
            cols = matrix[i].length;
            
            System.out.print("[");
            for (int j = 0; j < cols; j++) {
                System.out.print(matrix[i][j] + ", ");
            }
            System.out.println("]");
        }
    }
}
