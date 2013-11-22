package testing;

import java.util.Arrays;

import util.Direction;
import model.*;

public class TetriminoTest implements TestSuite {
    
    public boolean instantiationTest() {
        int[][] matrix = new int[4][4];
        matrix[0] = new int[]{1, 0, 0, 0};
        matrix[1] = new int[]{1, 0, 0, 0};
        matrix[2] = new int[]{1, 0, 0, 0};
        matrix[3] = new int[]{1, 1, 0, 0};
        Shape shape = new Shape(matrix);
        Tetrimino tetrimino = new Tetrimino(shape, new int[]{1, 2}, 11);
        
        if (tetrimino.getID() != 11
                || !Arrays.equals(tetrimino.getRootCoordinate(), new int[]{1, 2})) {
            return false;
        }
        
        return true;
    }
    
    public boolean shiftTest() {
        int[][] matrix = new int[4][4];
        matrix[0] = new int[]{1, 0, 0, 0};
        matrix[1] = new int[]{1, 0, 0, 0};
        matrix[2] = new int[]{1, 0, 0, 0};
        matrix[3] = new int[]{1, 1, 0, 0};
        Shape shape = new Shape(matrix);
        Tetrimino tetrimino = new Tetrimino(shape, new int[]{10, 10}, 11);
        
        tetrimino.shift(Direction.NORTH);
        if (!Arrays.equals(tetrimino.getRootCoordinate(), new int[] {9, 10})) {
            return false;
        }
        tetrimino.shift(Direction.EAST);
        if (!Arrays.equals(tetrimino.getRootCoordinate(), new int[] {9, 11})) {
            return false;
        }
        tetrimino.shift(Direction.SOUTH);
        if (!Arrays.equals(tetrimino.getRootCoordinate(), new int[] {10, 11})) {
            return false;
        }
        tetrimino.shift(Direction.WEST);
        if (!Arrays.equals(tetrimino.getRootCoordinate(), new int[] {10, 10})) {
            return false;
        }
        
        return true;
    }
    
    public boolean getCoordinatesTest() {
        int[][] matrix = new int[4][4];
        matrix[0] = new int[]{0, 0, 0, 0};
        matrix[1] = new int[]{0, 1, 0, 0};
        matrix[2] = new int[]{1, 1, 1, 0};
        matrix[3] = new int[]{0, 1, 0, 0};
        Shape shape = new Shape(matrix);
        Tetrimino tetrimino = new Tetrimino(shape, new int[]{10, 10}, 11);
        
        int[][] target = new int[5][2];
        target[0] = new int[]{10, 11};
        target[1] = new int[]{11, 10};
        target[2] = new int[]{11, 11};
        target[3] = new int[]{11, 12};
        target[4] = new int[]{12, 11};
        
        if (!Arrays.deepEquals(tetrimino.getCoordinates(), target)) {
            return false;
        }
        
        return true;
    }
    
    public boolean deleteBlockTest() {
        int[][] matrix = new int[4][4];
        matrix[0] = new int[]{1, 0};
        matrix[1] = new int[]{1, 0};
        matrix[2] = new int[]{1, 0};
        matrix[3] = new int[]{1, 1};
        Shape shape = new Shape(matrix);
        Tetrimino tetrimino = new Tetrimino(shape, new int[]{10, 10}, 11);
        
        tetrimino.deleteBlock(13, 11);
        
        int[][] target = new int[4][2];
        target[0] = new int[]{10, 10};
        target[1] = new int[]{11, 10};
        target[2] = new int[]{12, 10};
        target[3] = new int[]{13, 10};
        
        if (!Arrays.deepEquals(tetrimino.getCoordinates(), target)) {
            return false;
        }
        
        return true;
    }
    
    public boolean getAnchorCoordinatesTest() {
        int[][] matrix = new int[4][4];
        matrix[0] = new int[]{1, 0};
        matrix[1] = new int[]{1, 0};
        matrix[2] = new int[]{1, 0};
        matrix[3] = new int[]{1, 1};
        Shape shape = new Shape(matrix);
        Tetrimino tetrimino = new Tetrimino(shape, new int[]{10, 10}, 11);
        
        int[][] target = new int[2][2];
        target[0] = new int[]{13, 10};
        target[1] = new int[]{13, 11};

        return Arrays.deepEquals(tetrimino.getAnchorCoordinates(Direction.SOUTH), target);
    }
    
    public boolean isEmptyTest() {
        int[][] matrix = new int[2][2];
        matrix[0] = new int[]{1, 0};
        matrix[1] = new int[]{1, 0};
        Shape shape = new Shape(matrix);
        Tetrimino tetrimino = new Tetrimino(shape, new int[]{10, 10}, 11);
        
        tetrimino.deleteBlock(10, 10);
        tetrimino.deleteBlock(11, 10);
        
        return tetrimino.isEmpty();
    }
    
    public boolean getBottomRowTest() {
        int[][] matrix = new int[4][4];
        matrix[0] = new int[]{1, 0};
        matrix[1] = new int[]{1, 0};
        matrix[2] = new int[]{1, 0};
        matrix[3] = new int[]{1, 1};
        Shape shape = new Shape(matrix);
        Tetrimino tetrimino = new Tetrimino(shape, new int[]{10, 10}, 11);
        
        return tetrimino.getBottomRow() == 13;
    }
    
    @Override
    public void printFails() {
        if (!instantiationTest()) {
            System.out.println("TetriminoTest.instantiationTest() failed.");
        }
        if (!shiftTest()) {
            System.out.println("TetriminoTest.shiftTest() failed.");
        }
        if (!getCoordinatesTest()) {
            System.out.println("TetriminoTest.getCoordinatesTest() failed.");
        }
        if (!deleteBlockTest()) {
            System.out.println("TetriminoTest.deleteBlockTest() failed.");
        }
        if (!getAnchorCoordinatesTest()) {
            System.out.println("TetriminoTest.getAnchorCoordinatesTest() failed.");
        }
        if (!isEmptyTest()) {
            System.out.println("TetriminoTest.isEmptyTest() failed.");
        }
        if (!getBottomRowTest()) {
            System.out.println("TetriminoTest.getBottomRow() failed.");
        }
    }

}
