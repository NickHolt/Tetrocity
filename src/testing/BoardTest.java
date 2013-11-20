package testing;

import java.util.Arrays;

import model.Board;

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

    @Override
    public void printFails() {
        if (!instantiationTest()) {
            System.out.println("BoardTest.instantiationTest() failed.");
        }
    }

}
