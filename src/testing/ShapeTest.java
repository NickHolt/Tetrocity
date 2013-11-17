package testing;

import java.util.Arrays;

import model.Shape;

public class ShapeTest implements TestSuite {
    
    public boolean instantiationTest() {
        Shape shape;
        int[][] matrix = new int[3][4];
        matrix[0] = new int[]{0, 0, 0, 0};
        matrix[1] = new int[]{1, 0, 1, 0};
        matrix[2] = new int[]{0, 0, 0, 0};
        
        boolean errorCaught = false;
        try {
            shape = new Shape(matrix, 2);
        } catch (IllegalArgumentException e) {
            errorCaught = true;
        }
        if (!errorCaught) {
            return false;
        }
        
        matrix = new int[4][4];
        matrix[0] = new int[]{0, 0, 0, 0};
        matrix[1] = new int[]{1, 0, 1, 0};
        matrix[2] = new int[]{0, 0, 0, 0};
        matrix[3] = new int[]{0, 0, 0, 0};
        errorCaught = false;
        try {
            shape = new Shape(matrix, 2);
        } catch (IllegalArgumentException e) {
            errorCaught = true;
        }
        if (!errorCaught) {
            return false;
        }
        
        matrix = new int[4][4];
        matrix[0] = new int[]{1, 0, 0, 0};
        matrix[1] = new int[]{1, 0, 0, 0};
        matrix[2] = new int[]{0, 1, 0, 0};
        matrix[3] = new int[]{0, 0, 0, 0};
        errorCaught = false;
        try {
            shape = new Shape(matrix, 3);
        } catch (IllegalArgumentException e) {
            errorCaught = true;
        }
        if (!errorCaught) {
            return false;
        }
        
        matrix = new int[4][4];
        matrix[0] = new int[]{1, 0, 0, 0};
        matrix[1] = new int[]{-1, 0, 0, 0};
        matrix[2] = new int[]{0, 0, 0, 0};
        matrix[3] = new int[]{0, 0, 0, 0};
        errorCaught = false;
        try {
            shape = new Shape(matrix, 2);
        } catch (IllegalArgumentException e) {
            errorCaught = true;
        }
        if (!errorCaught) {
            return false;
        }
        
        matrix = new int[4][4];
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
        
        errorCaught = false;
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
        matrix[0] = new int[]{1, 0, 0, 0};
        matrix[1] = new int[]{1, 0, 0, 0};
        matrix[2] = new int[]{1, 0, 0, 0};
        matrix[3] = new int[]{1, 1, 0, 0};
        
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
        coordinates = shape.getAbsoluteMatrixCoordinates(10, 10);
        target[0] = new int[]{10, 10};
        target[1] = new int[]{11, 10};
        target[2] = new int[]{12, 10};
        target[3] = new int[]{13, 10};
        target[4] = new int[]{13, 11};
        
        for (int i = 0; i < target.length; i++) {
            if (!Arrays.equals(target[i], coordinates[i])) {
                return false;
            }
        }
        
        return true;
    }
    
    public boolean measureTest() {
        //Also tests Shape#removeBlock()
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
    
    public boolean getRotationalCoordinateTest() {
        int[][] matrix = new int[4][4];
        matrix[0] = new int[]{1, 0, 0, 0};
        matrix[1] = new int[]{1, 0, 0, 0};
        matrix[2] = new int[]{1, 0, 0, 0};
        matrix[3] = new int[]{1, 1, 0, 0};
        Shape shape = new Shape(matrix, 5);
        
        return Arrays.equals(shape.getRotationalCoordinate(), new int[]{1, 0});
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

    @Override
    public void printAll() {
        System.out.println("ShapeTest.instantiationTest():");
        System.out.println(instantiationTest());
        System.out.println("ShapeTest.getMatrixCoordinatesTest():");
        System.out.println(getMatrixCoordinatesTest());
        System.out.println("ShapeTest.measureTest():");
        System.out.println(measureTest());
        System.out.println("ShapeTest.getRotationalCoordinateTest()");
        System.out.println(getRotationalCoordinateTest());
        System.out.println("ShapeTest.getBottomRowTest():");
        System.out.println(getBottomRowTest());
    }

}
