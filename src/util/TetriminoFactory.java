package util;

import model.Shape;
import model.Tetrimino;

/** A pseudo-random generator of {@link Tetrimino} piece information for a game of Tetrocity. To 
 * instantiate a TetriminoFactory, a seed and Tetrimino block-length range must be
 * provided. Note that two Factories of the same seed and block-length range will
 * generate an identical sequence of Tetrimino pieces. 
 * 
 *  A TetriminoFactory can provide random {@link Shape} objects, but also random Tetrimino
 * objects if a root coordinate is provided. 
 * 
 *  A TetriminoFactory instance will also be able to assign Tetrimino IDs that are
 * unique to all Tetriminoes currently in the game. As such, when a Tetrimino
 * is deleted entirely, it is necessary to notify the TetriminoFactory that that ID
 * is now free to use, via {@link TetriminoFactory#freeID()}. A TetriminoFactory
 * can only generate IDs unique that instantiation. As such, only one generator
 * should be used for a given game of Tetrocity (indeed you should only need one). 
 * 
 * 
 * @author Nick Holt
 *
 */
public class TetriminoFactory {
    public static final int DEFAULT_LOAD = 100;
    
    private int [] mLengthRange;
    private int mSeed;
    private int[] mIDs; //ID available IFF mIDs[ID] == 1

    /** A new TetriminoFactory. 
     * 
     * @param lengthRange The range of Tetrimino lengths which this TetriminoFactory 
     * will generate.
     * @param seed The seed of all random operations of this TetriminoFactory.
     * @param initialLoad The initial scope of this TetriminoFactory's ID generation.a
     */
    public TetriminoFactory(int[] lengthRange, int seed, int initialLoad) {
        //TODO
    }
    
    /** A new TetriminoFactory with initial load DEFAULT_LOAD. 
     * 
     * @param lengthRange The range of Tetrimino lengths which this TetriminoFactory 
     * will generate.
     * @param seed The seed of all random operations of this TetriminoFactory.
     */
    public TetriminoFactory(int[] lengthRange, int seed) {
        this(lengthRange, seed, DEFAULT_LOAD);
    }
    
    /**
     * @return A new {@link Shape} instance randomly generated using this
     * TetriminoFactory's length range and seed. 
     */
    public static Shape getRandomShape() {
        //TODO
        return new Shape(null, 0);
    }
    
    public int getRandomUniqueID() {
        //TODO
        return 0;
    }
    
    public Tetrimino getRandomTetrimino(int[] rootCoordinate) {
        //TODO
        return null;
    }
    
    public void freeID(int ID) {
        //TODO
    }

    /**
     * @return The range of Shape lengths used by this TetriminoFactory. 
     */
    public int [] getLengthRange() {
        return mLengthRange;
    }

    /**
     * @param mLengthRange The range of Shape lengths to be used by this 
     * TetriminoFactory.
     */
    public void setLengthRange(int [] mLengthRange) {
        this.mLengthRange = mLengthRange;
    }

    /**
     * @return The random seed used by this TetriminoFactory. 
     */
    public int getSeed() {
        return mSeed;
    }

    /**
     * @param The random seed to be used by this TetriminoFactory.
     */
    public void setSeed(int mSeed) {
        this.mSeed = mSeed;
    }

}
