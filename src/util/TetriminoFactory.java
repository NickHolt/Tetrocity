package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import model.Shape;
import model.Tetrimino;

/** A pseudo-random generator of {@link Tetrimino} piece information for a game of Tetrocity. To 
 * instantiate a TetriminoFactory, a seed and Tetrimino block-length range must be
 * provided. Note that two Factories of the same seed and block-length range will
 * generate an identical sequence of Tetrimino pieces.
 * 
 *  This class does not generate all Tetriminoes with equal probability. All blocks are
 * placed purely at random when generating the Tetriminoes shape, and thus pieces with higher 
 * rotational symmetry are less likely to be produced, if we consider the rotational variations 
 * of a Tetrimino shape to be 'equal'. In fact, a piece with full rotational symmetry is four 
 * times less likely to be generated than a piece with no rotational symmetry. 
 * 
 *  This is desired behavior. It is assumed that pieces with full rotational symmetry 
 * are 'harder' pieces, as they cannot be rotated to fit different shapes. Thus, 'harder'
 * pieces are generated less frequently. The straight-line piece is an exception to this rule,
 * and the probabilistic issues of their generation is addressed further below. 
 * 
 *  A TetriminoFactory instance will also be able to assign Tetrimino IDs that are
 * unique to all Tetriminoes currently in the game. As such, when a Tetrimino
 * is deleted entirely, it is necessary to notify the TetriminoFactory that that ID
 * is now free to use, via {@link TetriminoFactory#freeID()}. A TetriminoFactory
 * can only generate IDs unique that instantiation. As such, only one generator
 * should be used for a given game of Tetrocity (indeed you should only need one). 
 * 
 *  TetriminoFactories allow for a probabilistic model of straight-line generation. If 
 * desired, an expected spacing S may be provided. This means that during random
 * Tetrimino (not Shape) generation, a maximum-length straight line piece will be generated
 * with probability 1 / S. In order to address high variance, a max-length straight line
 * Tetrimino will be generated with probability 1 if a maximum-length straight line piece
 * was not chosen for the last {@link TetriminoFactory#STRAIGHT_LINE_SPACING_VARIANCE} * S
 * piece generations. 
 * 
 * @author Nick Holt
 *
 */
public class TetriminoFactory {
    /* The minimum variance factor in maximum-length straight line Tetrimino generation. */
    private static final float STRAIGHT_LINE_SPACING_VARIANCE = 1.25f;
    /* The range of lengths of generated Tetriminoes. */
    private int [] mLengthRange;
    /* This TetriminoFactory'r random number generator. */
    private Random mRandom;
    /* The expected number of Tetrimino generations that occur before it is determined that
     * a maximum-length Tetrimino piece should be produced. */
    private int mStraightLineSpacing;
    /* The number of Tetrimino generations that have occured since the last decision to 
     * produce a maximum-length Tetrimino piece. */
    private int mLastStraightLinePieceProduced;
    /* The array used to determine available IDs. */
    private ArrayList<Integer> mIDs; 

    /** A new TetriminoFactory. This TetriminoFactory will not consider straight-line
     * spacing during Tetrimino generation.
     * 
     * @param lengthRange The range of Tetrimino lengths which this TetriminoFactory 
     * will generate.
     * @param seed The seed of all pseudorandom operations of this TetriminoFactory.
     */
    public TetriminoFactory(int[] lengthRange, int seed) {
        this(lengthRange, seed, -1);
    }
    
    /** A new TetriminoFactory. This TetriminoFactory will consider straight-line
     * spacing during Tetrimino generation.
     * 
     * @param lengthRange The range of Tetrimino lengths which this TetriminoFactory 
     * will generate.
     * @param seed The seed of all pseudorandom operations of this TetriminoFactory.
     * @param straightLineSpacing The MINIMIM expected number of Tetriminoes in between 
     * straight-line pieces. 
     */
    public TetriminoFactory(int[] lengthRange, int seed, int straightLineSpacing) {
        mLengthRange = lengthRange;
        mRandom = new Random(seed);
        mIDs = new ArrayList<Integer>();
        mStraightLineSpacing = straightLineSpacing;
        mLastStraightLinePieceProduced = 0;        
    }
    
    /************/
    /* Getters. */
    /************/
    
    /** Returns a instance unique ID. These IDs are neither random nor 
     * non-predictable. 
     * 
     *  TetriminoFactories use length 32 bit-strings to maintain instance unique IDs.
     * In short, if the ith bit (little-endian)  is 1, then i is an available
     * ID. If a new ID is requested and all bit-strings are 0, a new bit-string
     * is added. The jth bit of the ith bit-string is ID 32*i + j. 
     * 
     * @return A new instance unique ID. 
     */
    public int getUniqueID() {
        int mask, bitString;
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

    /** A new random shape matrix that describes a valid Tetrimino shape,
     * for a length randomly selected from this TetriminoFactory's length
     * range. 
     * 
     *  WARNING: The matrix generated by this method does NOT fit the 
     * "smallest possible matrix" representation that Shape objects use.
     * Shape objects automatically shrink input matrices, so it is not done
     * here in order to save an extra call to {@link Matrices#shrink()}. 
     * 
     * @return The random shape matrix. 
     */
    private int[][] getRandomShapeMatrix() {
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
         */
        int[] lengths = new int[mLengthRange[1] - mLengthRange[0] + 1]; //I know this is dumb, it fixes a
        for (int i = 0; i < lengths.length; i++) {                      //bug with Java Random#nextInt
            lengths[i] = i + mLengthRange[0];
        }
        int random = mRandom.nextInt();
        random <<= 1; random >>>= 1; //make positive

        int length = lengths[random % lengths.length],
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
        
        int row = 0, col = 0;
        int[] randomCoord, top, right, bottom, left;
        while (blocksLeft > 0) {
            /* Find a random coordinate. */
            random = mRandom.nextInt();
            random <<= 1; random >>>= 1; //make positive
            
            random = (int) (random % eligibleCoords.size());
            randomCoord = 
                    eligibleCoords.get(random);
            eligibleCoords.remove(random);
            
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
        
        return matrix;
    }
    
    /**
     * @return A new {@link Shape} instance randomly generated using this
     * TetriminoFactory's length range and seed.
     */
    private Shape getRandomShape() {
        return new Shape(getRandomShapeMatrix());
    }
    
    /**
     * @return A straight line Tetrimino of maximum length. The Tetrimino's orientation
     * will be chosen randomly. 
     */
    public Tetrimino getRandomMaxLengthStraightLineTetrimino() {
        int[][] matrix = new int[1][mLengthRange[1]]; //Straight line in a row
        for (int i = 0; i < matrix[0].length; i++) {
            matrix[0][i] = 1;
        }
                
        Tetrimino straightLineTetrimino = new Tetrimino(new Shape(matrix), getUniqueID());
        
        if (mRandom.nextInt() % 2 == 1) { //Make it a column with .5 probability
            straightLineTetrimino.rotateClockwise();
        }
        
        return straightLineTetrimino;
    }

    /** Returns a Tetrimino with a pseudorandom shape and instance unique ID. If 
     * the straight-line Tetrimino spacing was specified, this method will first ensure
     * that a straight-line piece was returned in the last straight_line_spacing * 
     * {@link TetriminoFactory#STRAIGHT_LINE_SPACING_VARIANCE} Tetrimino productions. 
     * If not, a straight line piece will be returned with 100% certainty. This functionality 
     * addresses the large standard deviation observed when the straight-line production is 
     * left to probability alone. 
     * 
     *  Furthermore, a straight line piece will be produced with probability 1 / 
     * straight_line_spacing. Otherwise, a random Tetrimino piece (potentially a 
     * straight-line Tetrimino) will be produced.
     * 
     * @return The new Tetrimino object. 
     */
    public Tetrimino getRandomTetrimino() {        
        if (mStraightLineSpacing > 0) {
            if (mLastStraightLinePieceProduced >= mStraightLineSpacing * STRAIGHT_LINE_SPACING_VARIANCE
                    || mRandom.nextInt(mStraightLineSpacing) == 0) {
                mLastStraightLinePieceProduced = 0;
                return getRandomMaxLengthStraightLineTetrimino();
            }
        }
        
        mLastStraightLinePieceProduced++;
        
        return new Tetrimino(getRandomShape(), getUniqueID()); 
    }
    
    /**
     * @return The range of Shape lengths used by this TetriminoFactory. 
     */
    public int[] getLengthRange() {
        return mLengthRange;
    }
    
    /***********/
    /* Setters */
    /***********/

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

    public void setStraightLineSpacing(int straightLineSpacing) {
        mStraightLineSpacing = straightLineSpacing;
    }

    /**
     * @param mLengthRange The range of Shape lengths to be used by this 
     * TetriminoFactory.
     */
    public void setLengthRange(int[] lengthRange) {
        mLengthRange = lengthRange;
    }
    
    /**********************************/
    /* Miscellaneous utility methods. */
    /**********************************/
    
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
