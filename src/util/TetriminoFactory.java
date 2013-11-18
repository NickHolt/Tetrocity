package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import testing.Debug;
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
    private Random mRandom;
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
        mRandom = new Random(seed);
        mRandom.nextInt();  //Prime the RNG to avoid bad behavior. 
        mIDs = new ArrayList<Integer>();
        
        Debug.print(2, "New TetriminoFactory instantiated.");
    }
    
    /** A new random shape matrix that describes a valid Tetrimino shape,
     * for a length randomly selected from this TetriminoFactory's length
     * range. 
     * 
     * @return The random shape matrix. 
     */
    public int[][] getRandomShapeMatrix() {
        /* This method works by starting with a blank 2L + 1 by 2L + 1 matrix, where L 
         * is the randomly selected Tetrimino length. 2L is used to ensure even a straight
         * line piece will fit, and the + 1 provides necessary buffering. 
         * Note that the Shape constructor automatically shrinks the matrix, so we don't 
         * need to worry about extra padding.
         * 
         * A block in placed in the middle, and the 4 adjacent coordinates are 
         * added to the respective HashSets. Of these elements, one is selected 
         * randomly to be the new block location. The 4 adjacent coordinates of 
         * that are then added (the HashSet ensures coordinates aren't repeated). 
         * This process is repeated until there are no remaining block to place. 
         * 
         * This code is really ugly. Sorry. It's late. 
         */
        int length = mRandom.nextInt(mLengthRange[1] - mLengthRange[0] + 1) 
                //length is the same if that argument is a power of 2...
                + mLengthRange[0], //lowerBound <= length <= upperBound
                blocksLeft = length;
        
        int[][] matrix = new int[2 * length + 1][2 * length + 1];
        
        ArrayList<int[]> eligibleCoords = new ArrayList<int[]>();
        
        /* Place block in middle and set up algorithm. We assume length > 0 */
        matrix[length][length] = 1;
        blocksLeft--;
        
        eligibleCoords.add(new int[]{length - 1, length});
        eligibleCoords.add(new int[]{length, length + 1});
        eligibleCoords.add(new int[]{length + 1, length});
        eligibleCoords.add(new int[]{length, length - 1});
        
        int row = 0, col = 0, randomIndex;
        int[] randomCoord, top, right, bottom, left;
        while (blocksLeft > 0) {
            /* Find a random coordinate. */
            randomIndex = mRandom.nextInt(eligibleCoords.size());
            randomCoord = 
                    eligibleCoords.get(randomIndex);
            eligibleCoords.remove(randomIndex);
            
            row = randomCoord[0]; 
            col = randomCoord[1];
                
            matrix[row][col] = 1;
            
            top = new int[]{row - 1, col};
            right = new int[]{row, col + 1};
            bottom = new int[]{row + 1, col};
            left = new int[]{row, col - 1};
                    
            /* Add new eligible coordinates if not already present and space
               hasn't been used already. */
            if (!containsCoordinate(eligibleCoords, top)
                    && matrix[top[0]][top[1]] != 1) {
                eligibleCoords.add(top);
            }
            if (!containsCoordinate(eligibleCoords, right)
                    && matrix[right[0]][right[1]] != 1) {
                eligibleCoords.add(right);
            }
            if (!containsCoordinate(eligibleCoords, bottom)
                    && matrix[bottom[0]][bottom[1]] != 1) {
                eligibleCoords.add(bottom);
            }
            if (!containsCoordinate(eligibleCoords, left)
                    && matrix[left[0]][left[1]] != 1) {
                eligibleCoords.add(left);
            }
            
            blocksLeft--;
        }
        
        Debug.print(2, "New random shape matrix generated.");
        return matrix;
    }
    
    /**
     * @return A new {@link Shape} instance randomly generated using this
     * TetriminoFactory's length range and seed.
     * 
     *  Note that different rotational orientations of the same shape may
     * be generated by this method. However, since each shape has the 
     * same number of rotational orientations (4), the distribution
     * of different shapes will still be uniform. 
     */
    public Shape getRandomShape() {
        return new Shape(getRandomShapeMatrix());
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
        
        Debug.print(2, "New ID requested.");
        return (mIDs.size() - 1)* 32; //The ID we just said we were going to use
    }
    
    /** Returns a Tetrimino with a pseudorandom shape and locally unique ID. 
     * 
     * @param rootCoordinate The root coordinate of the Tetrimino object. 
     * @return The new Tetrimino object. 
     */
    public Tetrimino getRandomTetrimino(int[] rootCoordinate) {
        
        Debug.print(1, "New Tetrimino requeted from Factory.");
        return new Tetrimino(getRandomShape(), rootCoordinate, getUniqueID()); 
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
        
        Debug.print(2, "ID " + ID + " freed.");
    }

    /**
     * @return The range of Shape lengths used by this TetriminoFactory. 
     */
    public int[] getLengthRange() {
        return mLengthRange;
    }

    /**
     * @param mLengthRange The range of Shape lengths to be used by this 
     * TetriminoFactory.
     */
    public void setLengthRange(int [] lengthRange) {
        mLengthRange = lengthRange;
    }

    /**
     * @return The Random boject used by this TetriminoFactory's pseudorandom processes.  
     */
    public Random getRandom() {
        return mRandom;
    }

    /**
     * @param The seed to be used by this TetriminoFactory's pseudorandom processes.
     */
    public void setRandom(int seed) {
        mRandom = new Random(seed);
    }
    
    /** Checks if an ArrayList of coordinates contains a given coordinate. Coordinates
     * are considered equal if their individual elements are equal. 
     * 
     * @param coordinateSet The ArrayList of coordinates. 
     * @param coordinate The coordinate to check. 
     * @return True IFF COORDINATESET contains COORDINATE. 
     */
    private static boolean containsCoordinate(ArrayList<int[]> coordinateSet
            , int[] targetCoordinate) {
        for (int[] coordinate : coordinateSet) {
            if (Arrays.equals(coordinate, targetCoordinate)) {
                return true;
            }
        }
        return false;
    }
}
