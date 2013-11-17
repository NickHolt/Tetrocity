package testing;

import java.util.Arrays;

import model.Shape;

public class ShapeTest implements TestSuite {
    
    public boolean instantiationTest() {
        Shape shape;
        
        int[][] matrix = new int[4][4];
        matrix[0] = new int[]{1, 0, 0, 0};
        matrix[1] = new int[]{0, 0, 0, 0};
        matrix[2] = new int[]{0, 0, 0, 0};
        matrix[3] = new int[]{0, 0, 0, 0};
        try {
            shape = new Shape(matrix, 1);
        } catch (IllegalArgumentException e) {
            return false;
        }
        matrix = new int[4][4];
        matrix[0] = new int[]{1, 0, 0, 0};
        matrix[1] = new int[]{1, 0, 0, 0};
        matrix[2] = new int[]{1, 0, 0, 0};
        matrix[3] = new int[]{1, 1, 0, 0};
        try {
            shape = new Shape(matrix, 5);
        } catch (IllegalArgumentException e) {
            return false;
        }

        if (!(shape.getHeight() == 4 
                && shape.getWidth() == 2
                && shape.getLength() == 5)) {
            return false;
        }
        
        boolean errorCaught = false;
        try {
            shape = new Shape(matrix, 10);
        } catch (IllegalArgumentException e) {
            errorCaught = true;
        }
        if (!errorCaught) {
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
        
        Shape shape = new Shape(matrix, 5);
        
        int[][] coordinates = shape.getRelativeMatrixCoordinates();
        int[][] target = new int[5][2];
        target[0] = new int[]{0, 0};
        target[1] = new int[]{1, 0};
        target[2] = new int[]{2, 0};
        target[3] = new int[]{3, 0};
        target[4] = new int[]{3, 1};
        
        for (int i = 0; i < target.length; i++) {
            if (!Arrays.equals(target[i], coordinates[i])) {
                return false;
            }
        }
        
        return true;
    }
    
    public boolean measureTest() {
        int[][] matrix = new int[4][4];
        matrix[0] = new int[]{1, 0, 0, 0};
        matrix[1] = new int[]{1, 0, 0, 0};
        matrix[2] = new int[]{1, 0, 0, 0};
        matrix[3] = new int[]{1, 1, 0, 0};
        
        Shape shape = new Shape(matrix, 5);
        
        shape.deleteBlock(2, 0);
        
        return shape.getLength() == 4
                && shape.getWidth() == 2
                && shape.getHeight() == 4;
    }
    
    public boolean getBottomRowTest() {
        int[][] matrix = new int[4][4];
        matrix[0] = new int[]{1, 0, 0, 0};
        matrix[1] = new int[]{1, 0, 0, 0};
        matrix[2] = new int[]{1, 0, 0, 0};
        matrix[3] = new int[]{1, 1, 0, 0};
        
        Shape shape = new Shape(matrix, 5);
        
        int[][] bottomRow = shape.getBottomRow();
        int[][] target = new int[2][2];
        target[0] = new int[]{3, 0};
        target[1] = new int[]{3, 1};
        
        for (int i = 0; i < target.length; i++) {
            if (!Arrays.equals(bottomRow[i], target[i])) {
                return false;   
            }
        }
        
        return true;
    }
    
    public boolean rotateClockwiseTest() {
        int[][] matrix = new int[4][4];
        matrix[0] = new int[]{1, 0, 0, 0};
        matrix[1] = new int[]{1, 0, 0, 0};
        matrix[2] = new int[]{1, 0, 0, 0};
        matrix[3] = new int[]{1, 1, 0, 0};
        
        Shape shape = new Shape(matrix, 5);
        shape.rotateClockwise();
        
        int[][] coordinates = shape.getRelativeMatrixCoordinates();
        int[][] target = new int[5][2];
        target[0] = new int[]{0, 3};
        target[1] = new int[]{0, 2};
        target[2] = new int[]{0, 1};
        target[3] = new int[]{0, 0};
        target[4] = new int[]{1, 0};
        
        for (int i = 0; i < target.length; i++) {
            if (!Arrays.equals(coordinates[i], target[i])) {
                return false;   
            }
        }
        
        matrix = new int[3][3];
        matrix[0] = new int[]{0, 1, 0};
        matrix[1] = new int[]{1, 1, 1};
        matrix[2] = new int[]{0, 1, 0};
        
        shape = new Shape(matrix, 5);
        shape.rotateClockwise();
        
        coordinates = shape.getRelativeMatrixCoordinates();
        target = new int[5][2];
        target[0] = new int[]{1, 2};
        target[1] = new int[]{0, 1};
        target[2] = new int[]{1, 1};
        target[3] = new int[]{2, 1};
        target[4] = new int[]{1, 0};
        
        for (int i = 0; i < target.length; i++) {
            if (!Arrays.equals(coordinates[i], target[i])) {
                return false;   
            }
        }
        
        return true;
    }
    
    public boolean rotateCounterClockwiseTest() {
        int[][] matrix = new int[4][4];
        matrix[0] = new int[]{1, 0, 0, 0};
        matrix[1] = new int[]{1, 0, 0, 0};
        matrix[2] = new int[]{1, 0, 0, 0};
        matrix[3] = new int[]{1, 1, 0, 0};
        
        Shape shape = new Shape(matrix, 5);
        shape.rotateCounterClockwise();
        
        int[][] coordinates = shape.getRelativeMatrixCoordinates();
        int[][] target = new int[5][2];
        target[0] = new int[]{1, 0};
        target[1] = new int[]{1, 1};
        target[2] = new int[]{1, 2};
        target[3] = new int[]{1, 3};
        target[4] = new int[]{0, 3};
        
        for (int i = 0; i < target.length; i++) {
            if (!Arrays.equals(coordinates[i], target[i])) {
                return false;   
            }
        }
        
        matrix = new int[3][3];
        matrix[0] = new int[]{0, 1, 0};
        matrix[1] = new int[]{1, 1, 1};
        matrix[2] = new int[]{0, 1, 0};
        
        shape = new Shape(matrix, 5);
        shape.rotateCounterClockwise();
        
        coordinates = shape.getRelativeMatrixCoordinates();
        target = new int[5][2];
        target[0] = new int[]{1, 0};
        target[1] = new int[]{2, 1};
        target[2] = new int[]{1, 1};
        target[3] = new int[]{0, 1};
        target[4] = new int[]{1, 2};
        
        for (int i = 0; i < target.length; i++) {
            if (!Arrays.equals(coordinates[i], target[i])) {
                return false;   
            }
        }
        
        return true;
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
        if (!getBottomRowTest()) {
            System.out.println("ShapeTest.getBottomRowTest() failed.");
        }
        if (!rotateClockwiseTest()) {
            System.out.println("ShapeTest.rotateClockwiseTest() failed.");
        }
        if (!rotateCounterClockwiseTest()) {
            System.out.println("ShapeTest.rotateCounterClockwiseTest() failed.");
        }
    }
}
