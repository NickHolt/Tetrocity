package testing;

import java.util.HashSet;

import util.TetriminoFactory;

public class TetriminoFactoryTest implements TestSuite {
    
    public boolean getLUIDTest() {
        TetriminoFactory tf = new TetriminoFactory(null, 0);
        HashSet<Integer> hs = new HashSet<Integer>();
        
        for(int i = 0; i < 300; i++){
            int ID = tf.getUniqueID();
            if (!hs.add(ID) || ID >= 300) {
                return false;
            }
        }
        
        return true;
    }
    
    public boolean freeIDTest() {
        TetriminoFactory tf = new TetriminoFactory(null, 0);
        
        for(int i = 0; i < 65; i++){ 
            tf.getUniqueID();
        }
        tf.freeID(0);
        tf.freeID(31);
        tf.freeID(32);
        tf.freeID(47);
        tf.freeID(64);
        
        if(!(tf.getUniqueID() == 0
                && tf.getUniqueID() == 31
                && tf.getUniqueID() == 32
                && tf.getUniqueID() == 47
                && tf.getUniqueID() == 64)) {
            return false;
        }
        
        
        tf = new TetriminoFactory(null, 0);
        tf.getUniqueID();
        
        boolean errorCaught = false;
        try {
            tf.freeID(1); //1 hasn't been used, should error
        } catch (IllegalArgumentException e) {
            errorCaught = true;
        }
        if (!errorCaught) {
            return false;
        }
    
        errorCaught = false;
        try {
            tf.freeID(-1);
        } catch (IllegalArgumentException e) {
            errorCaught = true;
        }
        if (!errorCaught) {
            return false;
        }
        
        errorCaught = false;
        try {
            tf.freeID(100);
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
        if (!getLUIDTest()) {
            System.out.println("TetriminoFactoryTest.getLUIDTest() failed.");
        }
        if (!freeIDTest()) {
            System.out.println("TetriminoFactoryTest.freeIDTest() failed.");
        }

    }

}
