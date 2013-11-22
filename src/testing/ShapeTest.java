package testing;

import java.util.Arrays;

import util.Direction;
import model.Shape;

public class ShapeTest implements TestSuite {
    
    public boolean instantiationTest() {
        Shape shape;
        
        int[][] matrix = new int[4][4];
        matrix[0] = new int[]{1, 0, 0, 0};
        matrix[1] = new int[]{1, 0, 0, 0};
        matrix[2] = new int[]{1, 0, 0, 0};
        matrix[3] = new int[]{1, 1, 0, 0};
        try {
            shape = new Shape(matrix);
        } catch (IllegalArgumentException e) {
            return false;
        }
        
        matrix = new int[4][4];
        matrix[0] = new int[]{1, 0, 0, 0};
        matrix[1] = new int[]{0, 0, 0, 0};
        matrix[2] = new int[]{0, 0, 0, 0};
        matrix[3] = new int[]{0, 0, 0, 0};
        try {
            shape = new Shape(matrix);
        } catch (IllegalArgumentException e) {
            return false;
        }
        matrix = new int[4][4];
        matrix[0] = new int[]{1, 0, 0, 0};
        matrix[1] = new int[]{1, 0, 0, 0};
        matrix[2] = new int[]{1, 0, 0, 0};
        matrix[3] = new int[]{1, 1, 0, 0};
        try {
            shape = new Shape(matrix);
        } catch (IllegalArgumentException e) {
            return false;
        }

        if (!(shape.getHeight() == 4 
                && shape.getWidth() == 2
                && shape.getLength() == 5)) {
            return false;
        }
        
        return true;
    }

    public boolean getMatrixCoordinatesTest() {
        int[][] matrix = new int[4][4];
        matrix[0] = new int[]{1, 0};
        matrix[1] = new int[]{1, 0};
        matrix[2] = new int[]{1, 0};
        matrix[3] = new int[]{1, 1};
        
        Shape shape = new Shape(matrix);
        
        int[][] coordinates = shape.getRelativeMatrixCoordinates();
        int[][] target = new int[5][2];
        target[0] = new int[]{0, 0};
        target[1] = new int[]{1, 0};
        target[2] = new int[]{2, 0};
        target[3] = new int[]{3, 0};
        target[4] = new int[]{3, 1};
        
        if (!Arrays.deepEquals(target, coordinates)) {
            return false;
        }
        
        return true;
    }
    
    public boolean measureTest() {
        int[][] matrix = new int[4][4];
        matrix[0] = new int[]{1, 0, 0, 0};
        matrix[1] = new int[]{1, 0, 0, 0};
        matrix[2] = new int[]{1, 0, 0, 0}; //0, 0, 0, 0
        matrix[3] = new int[]{1, 1, 0, 0};
        
        Shape shape = new Shape(matrix);
        
        shape.deleteBlock(2, 0);
                
        if (!(shape.getLength() == 4
                && shape.getWidth() == 2
                && shape.getHeight() == 4)) {
            return false;
        }
        
        return true;
    }
    
    public boolean rotateClockwiseTest() {
        int[][] matrix = new int[4][4];
        matrix[0] = new int[]{1, 0, 0, 0};
        matrix[1] = new int[]{1, 0, 0, 0};
        matrix[2] = new int[]{1, 0, 0, 0};
        matrix[3] = new int[]{1, 1, 0, 0};
        
        Shape shape = new Shape(matrix);
        shape.rotateClockwise();
        
        int[][] coordinates = shape.getRelativeMatrixCoordinates();
        int[][] target = new int[5][2];
        target[0] = new int[]{0, 3};
        target[1] = new int[]{0, 2};
        target[2] = new int[]{0, 1};
        target[3] = new int[]{0, 0};
        target[4] = new int[]{1, 0};
        
        if (!Arrays.deepEquals(coordinates, target)) {
            return false;
        }
        
        matrix = new int[3][3];
        matrix[0] = new int[]{0, 1, 0};
        matrix[1] = new int[]{1, 1, 1};
        matrix[2] = new int[]{0, 1, 0};
        
        shape = new Shape(matrix);
        shape.rotateClockwise();
        
        coordinates = shape.getRelativeMatrixCoordinates();
        target = new int[5][2];
        target[0] = new int[]{1, 2};
        target[1] = new int[]{0, 1};
        target[2] = new int[]{1, 1};
        target[3] = new int[]{2, 1};
        target[4] = new int[]{1, 0};
        
        if (!Arrays.deepEquals(coordinates, target)) {
            return false;
        }
        
        return true;
    }
    
    public boolean rotateCounterClockwiseTest() {
        int[][] matrix = new int[4][4];
        matrix[0] = new int[]{1, 0, 0, 0};
        matrix[1] = new int[]{1, 0, 0, 0};
        matrix[2] = new int[]{1, 0, 0, 0};
        matrix[3] = new int[]{1, 1, 0, 0};
        
        Shape shape = new Shape(matrix);
        shape.rotateCounterClockwise();
        
        int[][] coordinates = shape.getRelativeMatrixCoordinates();
        int[][] target = new int[5][2];
        target[0] = new int[]{1, 0};
        target[1] = new int[]{1, 1};
        target[2] = new int[]{1, 2};
        target[3] = new int[]{1, 3};
        target[4] = new int[]{0, 3};
        
        if (!Arrays.deepEquals(coordinates, target)) {
            return false;
        }
        
        matrix = new int[3][3];
        matrix[0] = new int[]{0, 1, 0};
        matrix[1] = new int[]{1, 1, 1};
        matrix[2] = new int[]{0, 1, 0};
        
        shape = new Shape(matrix);
        shape.rotateCounterClockwise();
        
        coordinates = shape.getRelativeMatrixCoordinates();
        target = new int[5][2];
        target[0] = new int[]{1, 0};
        target[1] = new int[]{2, 1};
        target[2] = new int[]{1, 1};
        target[3] = new int[]{0, 1};
        target[4] = new int[]{1, 2};
        
        if (!Arrays.deepEquals(coordinates, target)) {
            return false;
        }
        
        return true;
    }
    
    public boolean isValidMatrixTest() {
        int[][] matrix0 = new int[4][4];
        matrix0[0] = new int[]{1, 0, 0, 0};
        matrix0[1] = new int[]{1, 0, 0, 0};
        matrix0[2] = new int[]{1, 0, 0, 0};
        matrix0[3] = new int[]{1, 1, 0, 0};
        
        int[][] matrix1 = new int[4][4];
        matrix1[0] = new int[]{1, 0, 0, 0};
        matrix1[1] = new int[]{1, 0, 0, 0};
        matrix1[2] = new int[]{1, 0, 1, 0};
        matrix1[3] = new int[]{1, 1, 0, 0};
        
        int[][] matrix2 = new int[4][4];
        matrix2[0] = new int[]{1, 0, 0, 0};
        matrix2[1] = new int[]{2, 0, 0, 0};
        matrix2[2] = new int[]{3, 0, 0, 0};
        matrix2[3] = new int[]{4, 5, 0, 0};
                
        int[][] matrix3 = new int[1][1];
        matrix3[0] = new int[]{1};
        
        return Shape.isValidTetriminoMatrix(matrix0)
                && !Shape.isValidTetriminoMatrix(matrix1)
                && Shape.isValidTetriminoMatrix(matrix2)
                && Shape.isValidTetriminoMatrix(matrix3)
                && Shape.isValidTetriminoMatrix(matrix3);
    }
    
    public void printFails() {
        if (!instantiationTest()) {
            System.out.println("ShapeTest.instantiationTest() failed.");
        }
        if (!getMatrixCoordinatesTest()) {
            System.out.println("ShapeTest.getMatrixCoordinatesTest() failed.");
        }
        if (!measureTest()) {
            System.out.println("ShapeTest.measureTest() failed.");
        }
        if (!rotateClockwiseTest()) {
            System.out.println("ShapeTest.rotateClockwiseTest() failed.");
        }
        if (!rotateCounterClockwiseTest()) {
            System.out.println("ShapeTest.rotateCounterClockwiseTest() failed.");
        }
        if (!isValidMatrixTest()) {
            System.out.println("ShapeTest.isValidMatrixTest() failed.");
        }
    }
}
