package testing;

import model.*;

public class TetriminoTest implements TestSuite {
    
    public boolean instantiationTest() {
        int[][] matrix = new int[4][4];
        matrix[0] = new int[]{1, 0, 0, 0};
        matrix[1] = new int[]{1, 0, 0, 0};
        matrix[2] = new int[]{1, 0, 0, 0};
        matrix[3] = new int[]{1, 1, 0, 0};
        Shape shape = new Shape(matrix, 5);
        Tetrimino tetrimino = new Tetrimino(shape, 0, 0, 11);
        
        if (tetrimino.isLive()
                || tetrimino.getID() != 11) {
            return false;
        }
        
        return true;
    }

    @Override
    public void printAll() {
        System.out.println("TestSuite.instantiationTest():");
        System.out.println(instantiationTest());

    }

}
