package testing;

import util.Matrices;

public class MatricesTest implements TestSuite {
    
    public boolean padMatrixTest() {
        int[][] matrix = new int[3][4];
        matrix[0] = new int[]{1, 2, 3, 4};
        matrix[1] = new int[]{5, 6, 7, 8};
        matrix[2] = new int[]{9, 10, 11, 12};
        
        int[][] paddedMatrix = Matrices.padMatrix(matrix);
        int[][] target = new int[5][6];
        target[0] = new int[]{0, 0, 0, 0, 0, 0};
        target[1] = new int[]{0, 1, 2, 3, 4, 0};
        target[2] = new int[]{0, 5, 6, 7, 8, 0};
        target[3] = new int[]{0, 9, 10, 11, 12, 0};
        target[4] = new int[]{0, 0, 0, 0, 0, 0};
        
        for (int i = 0; i < target.length; i++) {
            for (int j = 0; j < target[0].length; j++) {
                if (target[i][j] != paddedMatrix[i][j]) {
                    return false;
                }
            }
        }
               
        return true;
    }

    @Override
    public void printAll() {
        System.out.println("MatricesTest.padMatrixTest():");
        System.out.println(padMatrixTest());
    }

}
