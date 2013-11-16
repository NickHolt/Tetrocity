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
        Shape shape = new Shape(matrix, 5);
        Tetrimino tetrimino = new Tetrimino(shape, 1, 2, 11);
        
        if (tetrimino.isLive()
                || tetrimino.getID() != 11
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
        Shape shape = new Shape(matrix, 5);
        Tetrimino tetrimino = new Tetrimino(shape, 10, 10, 11);
        
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
        //TODO
        return false;
    }

    @Override
    public void printAll() {
        System.out.println("TetriminoTest.instantiationTest():");
        System.out.println(instantiationTest());
        System.out.println("TetriminoTest.shiftTest():");
        System.out.println(shiftTest());
    }

}
