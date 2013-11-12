package util;

import model.Shape;

/** A pseudo-random generator of Tetrimino piece information for a game of Tetrocity. To 
 * instantiate a Generator, a seed and Tetrimino block-length range must be
 * provided. Note that two Generators of the same seed and block-length range will
 * generate an identical sequence of Tetrimino pieces. 
 * 
 *  A Generator instance will also be able to assign Tetrimino IDs that are
 * unique to all Tetriminoes currently in the game. As such, when a Tetrimino
 * is deleted entirely, it is necessary to notify the Generator that that ID
 * is now free to use, via {@link ShapeGenerator#freeID()}. 
 * 
 * 
 * @author Nick Holt
 *
 */
public class TetriminoFactory {
    
    /** Returns a pseudo-random Tetrimino Shape for a given LENGTH 
     * by use of the provided SEED. 
     * 
     * @param length The number of Blocks this layout will order.  
     * @param seed The seed to generate random behavior. 
     * @return A new, pseudo-randomly constructed Tetrimino. 
     */
    public static Shape getRandomShape(byte length, int seed) {
        //TODO
        return new Shape(null, 0);
    }
    
    public int getID() {
        //TODO
        return 0;
    }

}
