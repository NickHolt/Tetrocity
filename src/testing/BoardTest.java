package testing;

import java.util.Arrays;

import model.Board;
import model.Shape;
import model.Tetrimino;

public class BoardTest implements TestSuite {

    public boolean instantiationTest() {
        Board board = new Board(10, 11, 12);
        if (!Arrays.equals(board.getGridDimensions(), new int[]{22, 11})) {
            return false;
        }
        
        boolean errorCaught = false;
        try {
            board = new Board(0, 11, 12);
        } catch (IllegalArgumentException e) {
            errorCaught = true;
        }
        if (!errorCaught) {
            return false;
        }
        
        errorCaught = false;
        try {
            board = new Board(10, 0, 12);
        } catch (IllegalArgumentException e) {
            errorCaught = true;
        }
        if (!errorCaught) {
            return false;
        }
        
        errorCaught = false;
        try {
            board = new Board(10, 11, -1);
        } catch (IllegalArgumentException e) {
            errorCaught = true;
        }
        if (!errorCaught) {
            return false;
        }
        
        return true;
    }
    
    public boolean getPlacementCoordinateTest() {
        Board board = new Board(10, 10, 6);
        
        int[][] matrix = new int[4][4];
        matrix[0] = new int[]{1, 0, 0, 0};
        matrix[1] = new int[]{1, 0, 0, 0};
        matrix[2] = new int[]{1, 0, 0, 0};
        matrix[3] = new int[]{1, 1, 0, 0};
        Shape shape = new Shape(matrix);
        Tetrimino tetrimino = new Tetrimino(shape, 11);
                
        if (!Arrays.equals(board.getPlacementCoordinate(tetrimino), new int[]{2, 4})) {
            return false;
        }            
        
        matrix = new int[4][4];
        matrix[0] = new int[]{1, 0, 1, 0};
        matrix[1] = new int[]{0, 1, 0, 1};
        matrix[2] = new int[]{1, 0, 1, 0};
        matrix[3] = new int[]{0, 1, 0, 1};
        shape = new Shape(matrix);
        tetrimino = new Tetrimino(shape, 11);
                
        if (!Arrays.equals(board.getPlacementCoordinate(tetrimino), new int[]{2, 3})) {
            return false;
        }
        
        board = new Board(4, 4, 6);
        
        if (!Arrays.equals(board.getPlacementCoordinate(tetrimino), new int[]{2, 0})) {
            return false;
        }
        
        return true;
    }

    @Override
    public void printFails() {
        if (!instantiationTest()) {
            System.out.println("BoardTest.instantiationTest() failed.");
        }
        if (!getPlacementCoordinateTest()) {
            System.out.println("BoardTest.getPlacementCoordinateTest() failed.");
        }
    }

}
