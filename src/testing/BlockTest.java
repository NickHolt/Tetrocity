package testing;

import util.Direction;
import model.Block;

public class BlockTest implements TestSuite {
    
    public boolean instantiationTest() {
        Block block = new Block(null, 1, 2, 3);
        return block.getRow() == 1 && block.getColumn() == 2
                && block.getId() == 3;
    }
    
    public boolean shiftTest() {
        Block block = new Block(null, 1, 1, 0);
        
        block.shift(Direction.NORTH);
        
        if (!(block.getRow() == 0
                && block.getColumn() == 1)) {
            return false;
        };
        
        block.shift(Direction.EAST);
        if(!(block.getRow() == 0
                && block.getColumn() == 2)) {
            return false;
        };
        
        block.shift(Direction.SOUTH);
        if(!(block.getRow() == 1
                && block.getColumn() == 2)) {
            return false;
        };
        
        block.shift(Direction.WEST);
        if(!(block.getRow() == 1
                && block.getColumn() == 1)) {
            return false;
        };
        
        return true;
    }

    @Override
    public void printAll() {
        System.out.println("BlockTest.instantiationTest():");
        System.out.println(instantiationTest());
        System.out.println("BlockTest.shiftTest():");
        System.out.println(shiftTest());
    }

}
