package util;

import java.util.ArrayList;

import model.Shape;
import model.Tetrimino;

/** A pseudo-random generator of {@link Tetrimino} piece information for a game of Tetrocity. To 
 * instantiate a TetriminoFactory, a seed and Tetrimino block-length range must be
 * provided. Note that two Factories of the same seed and block-length range will
 * generate an identical sequence of Tetrimino pieces. 
 * 
 *  A TetriminoFactory can provide pseudorandom {@link Shape} objects, but also 
 *  pseudorandom Tetrimino objects if a root coordinate is provided. 
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
    private int [] mLengthRange;
    private int mSeed;
    /* TetriminoFactories use length 32 bit-strings to maintain locally unique IDs.
     * In short, if the ith bit (little-endian)  is 1, then i is an available
     * ID. If a new ID is requested and all bit-strings are 0, a new bit-string
     * is added. The jth bit of the ith bit-string is ID 32*i + j. 
     * Integers are used instead of Longs since Java bitwise operations
     * are messy on Long types. 
     */
    private ArrayList<Integer> mIDs; 

    /** A new TetriminoFactory. 
     * 
     * @param lengthRange The range of Tetrimino lengths which this TetriminoFactory 
     * will generate.
     * @param seed The seed of all pseudorandom operations of this TetriminoFactory.
     */
    public TetriminoFactory(int[] lengthRange, int seed) {
        mLengthRange = lengthRange;
        mSeed = seed;
        mIDs = new ArrayList<Integer>();
    }
    
    /**
     * @return A new {@link Shape} instance randomly generated using this
     * TetriminoFactory's length range and seed. 
     */
    public static Shape getRandomShape() {
        //TODO
        return new Shape(null, 0);
    }
    
    /** Returns a locally unique ID. These IDs are neither random nor 
     * non-predictable. 
     * 
     * @return A new locally unique ID. 
     */
    public int getUniqueID() {
        int mask;
        int bitString;
        for (int i = 0; i < mIDs.size(); i++) {
            mask = 0x1;
            bitString = mIDs.get(i);
            
            if (bitString != 0) { //There is an available ID in the bit-string
                for (int j = 0; j < 32; j++) {
                    if ((bitString & mask) != 0) {
                        mIDs.set(i, bitString & (mask ^ -1)); //zero the bit: ID is used
                        return 32 * i + j; 
                    }
                    mask <<= 1;
                }
            }
        }
        /* No ID's available, make new bit-string */
        mIDs.add(0xFFFFFFFE); //0b111...110, since we're going to use first bit for the ID
        return (mIDs.size() - 1)* 32; //The ID we just said we were going to use
    }
    
    /** Returns a Tetrimino with a pseudorandom shape and locally unique ID. 
     * 
     * @param rootCoordinate
     * @return
     */
    public Tetrimino getRandomTetrimino(int[] rootCoordinate) {
        //TODO
        return null;
    }
    
    /** Marks an ID as free to use, so that it may be assigned to future 
     * Tetriminoes.
     * 
     *  This method is dangerous. If you attempt to free an ID that has not been
     * distributed yet, or an invalid ID, an exception will be raised. Care should
     * be used in ensuring the ID being freed was in fact previously in use. 
     * 
     * @param ID The ID to mark as free. 
     */
    public void freeID(int ID) {
        if (ID < 0) {
            throw new IllegalArgumentException("Tried to free a negative "
                    + "TetriminoFactory ID: " + ID);
        } else if (ID / 32 >= mIDs.size()) {
            //This ID wasn't distributed anyway, so this should not be reached
            //if this method is used properly.
            throw new IllegalArgumentException("ID: " + ID + " outside of buffer range.");
        }
                
        if ((mIDs.get(ID / 32) & (0x1 << (ID % 32))) != 0) { //I.e. the ID was not used yet
            throw new IllegalArgumentException("ID: " + ID + " has not been used yet.");
        }
        
        mIDs.set(ID / 32,  //ID / 32 is the index of the bit-string that contains the ID
                mIDs.get(ID / 32) | (0x1 << (ID % 32))); //Set the relevant ID bit to 1
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
     * @return The seed used by this TetriminoFactory's pseudorandom processes.  
     */
    public int getSeed() {
        return mSeed;
    }

    /**
     * @param The seed to be used by this TetriminoFactory's pseudorandom processes.
     */
    public void setSeed(int mSeed) {
        this.mSeed = mSeed;
    }

}
