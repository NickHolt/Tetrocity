package control;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JLabel;

import model.Board;
import util.Direction;
import util.GameOverException;
import util.TetriminoFactory;

/** The fundamental controller for a game of Tetrocity. It is the job of the GuidedEngine to construct the
 * GUI, and facilitate the game through the use of timers and parsing player input via the Player class.
 * It is also the job of the Engine to interpret certain game events, and decide how the player score
 * should be updated as a result. 
 *  
 * @author Nick Holt
 *
 */
public class GuidedEngine extends JFrame{
    private static final long serialVersionUID = 1L;
    public static final int LOGICAL_FPS = 60;
    /* E.g. number of grid rows = max_length * ROW_RATIO */
    public static final float ROW_RATIO = 5.0f, COLUMN_RATIO = 2.5f;
    /* The initial Tetrimino drop speed. */
    public static final float INITIAL_DROP_SPEED = 1.1f; //blocks / second
    /* The factor by which the drop speed is multiplied as levels increase */
    public static final float DROP_SPEED_INCREASE_FACTOR = 1.15f;
    /* The base width of the JFrame is given by 20*ROW_RATIO*Max_tetrimino_length. 
     * In a normal game of Tetris, width = 400. */
    public static final int STANDARD_WIDTH_RATIO = 30;
    
    private int mLevel;
    private boolean mIsPaused, mIsHalted;
    private Board mBoard;
    private TetriminoFactory mTetriminoFactory;
    private Player mPlayer;    
    private GuidedLevelParameters mGuidedLevelParameters;
    private JLabel mScoreBar;
    
    /** A new GuidedEngine for a game of Tetrocity. Instantiating an Engine will set the game parameters.
     */
    public GuidedEngine() {
        mGuidedLevelParameters = new GuidedLevelParameters();

        mLevel = 1;
        mIsPaused = false;
        mTetriminoFactory = new TetriminoFactory(mGuidedLevelParameters.
                getLevelLiveTetriminoLengthRange(1), (int) System.currentTimeMillis());
        
        mPlayer = new Player();
        
        setFocusable(true);
        addKeyListener(mPlayer);
    }
    
    /** Begin the game of Tetrocity. The game will continue until either the JFrame is closed or 
     * a GameOverException is raised (to be changed, obviously).
     */
    public void begin() {
        constructGUI();
        
        /* Run the game. */
        float dropPeriod = (1 / (float) INITIAL_DROP_SPEED) * 1000, //in milliseconds
                logicPeriod = (1 / (float) LOGICAL_FPS) * 1000;
                
        double dropTime = 0, 
                logicTime = 0; //The last system time these operations ran
        
        while (true) {
            if (System.currentTimeMillis() >= dropTime + dropPeriod) {
                mBoard.shiftLiveTetriminoes(Direction.SOUTH);
                dropTime = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() >= logicTime + logicPeriod) {
                if (mPlayer.getScore() > mGuidedLevelParameters.getMaximumLevelScore(mLevel)) { 
                    mLevel++; //update level
                    dropPeriod = (1 / (float) (INITIAL_DROP_SPEED * 
                            Math.pow(DROP_SPEED_INCREASE_FACTOR, 
                                    mGuidedLevelParameters.getLevelDropFactor(mLevel)))) * 1000;
                    mTetriminoFactory.
                        setLengthRange(mGuidedLevelParameters.getLevelLiveTetriminoLengthRange(mLevel));
                    
                    mBoard.clearTetriminoQueue();
                }
                
                doGameLogic();
                
                mScoreBar.setText("Level: " + mLevel + ", Score: " + String.valueOf(mPlayer.getScore()));
                logicTime = System.currentTimeMillis();
            }          
        }
    }
    
    private void constructGUI() {
        int rows = (int) (GuidedLevelParameters.MAX_TETRIMINO_LENGTH * ROW_RATIO),
                cols = (int) (GuidedLevelParameters.MAX_TETRIMINO_LENGTH * COLUMN_RATIO),
                buffer = GuidedLevelParameters.MAX_TETRIMINO_LENGTH;
        mBoard = new Board(rows, cols, buffer);
        
        mScoreBar = new JLabel("Level: 1, Score: " + String.valueOf(mPlayer.getScore()));
        add(mScoreBar, BorderLayout.SOUTH);
        add(mBoard);
        
        float width = STANDARD_WIDTH_RATIO * cols,
                height = (int) (width * (rows - buffer) / cols);
                        
        setSize((int) width, (int) height);
        setTitle("Tetrocity");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void doGameLogic() {
        fillBoardQueue();

        
        interpretInput(mPlayer.getMoveKeyCode());
        updateScore(mBoard.clearRows()); //clear rows and update score
        
        
        if (mBoard.numLiveTetriminoes() != 0 &&
                mBoard.getTopLiveTetrimino().getRootCoordinate()[0] - mBoard.getBuffer() >=
                mGuidedLevelParameters.getLevelLiveTetriminoSpacing(mLevel)) {
            try {
                mBoard.putTetrimino();
            } catch (GameOverException e) {
                gameOver();
            }
        }
        
        if (mBoard.numLiveTetriminoes() == 0) {
            try {
                mBoard.putTetrimino();
            } catch (GameOverException e) {
                gameOver();
            }
        }        
    }
    
    /** Interpret a KeyEvent keyCode and respond as dictated by the game rules.
     * 
     * @param keyCode The key code to interpret. 
     */
    public void interpretInput(int keyCode) {        
        if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_KP_RIGHT) {
            mBoard.shiftLiveTetriminoes(Direction.EAST);
        } else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_KP_DOWN) {
            mBoard.shiftLiveTetriminoes(Direction.SOUTH);
        } else if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_KP_LEFT) {
            mBoard.shiftLiveTetriminoes(Direction.WEST);
        } else if (keyCode == KeyEvent.VK_SPACE) {
            mBoard.dropTetrimino();
        } else if (keyCode == KeyEvent.VK_SHIFT) {
            try {
                mBoard.storeTetrimino();
            } catch (GameOverException e) {
                gameOver();
            }
        } else if (keyCode == KeyEvent.VK_Z) {
            mBoard.rotateTetriminoCounterClockwise();
        } else if (keyCode == KeyEvent.VK_X) {
            mBoard.rotateTetriminoClockwise();
        } else if (keyCode == KeyEvent.VK_ESCAPE) {
            pause();
        } else if (keyCode == KeyEvent.VK_R) {
            restart();
        }            
    }

    /** Uses current game information to interpret the corresponding increase in score. 
     * 
     * @param linesCleared The number of lines cleared.
     * @return The amount of score to add. 
     */
    public long interpretScore(int linesCleared) {
        return linesCleared * linesCleared * mLevel;
    }
    
    public void fillBoardQueue() {
        while (mBoard.queueTooSmall()) {
            mBoard.enqueueTetrimino(mTetriminoFactory.getRandomTetrimino());
        }
    }
    
    /** Update the player's score given the number of lines cleared. 
     * 
     * @param linesCleared The number of lines cleared.
     */
    private void updateScore(int linesCleared) {
        mPlayer.addToScore(interpretScore(linesCleared));
    }
    
    private void pause() {
        mIsPaused = true;
        LoopKeyListener pausedKeyListener = new LoopKeyListener(true);
        removeKeyListener(mPlayer);
        addKeyListener(pausedKeyListener);
        
        mScoreBar.setText("PAUSED.");
        
        while (mIsPaused) {
            System.out.println(""); //This fixes a bug and I don't know why. 
        }
        
        removeKeyListener(pausedKeyListener);
        addKeyListener(mPlayer);
        
        mScoreBar.setText(String.valueOf(mPlayer.getScore()));
    }
    
    private void restart() {
        //TODO This is clumsy. Make it better.
        setVisible(false);
        new GuidedEngine().begin();
    }
    
    private void halt() {
        removeKeyListener(mPlayer);
        addKeyListener(new LoopKeyListener(false));
        mIsHalted = true;
        while(mIsHalted) {
            //Loop forever
            System.out.print("");
        }
        
        restart();
    }
    
    private void gameOver() {
        mScoreBar.setText("GAME OVER. Your score: " + mPlayer.getScore() + ". Press 'r' to play again!");
        halt();
    }
    
    private class LoopKeyListener implements KeyListener {
        boolean mIsPausedListener; //else is HaltListener
        
        public LoopKeyListener(boolean isPausedListener) {
            mIsPausedListener = isPausedListener;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (mIsPausedListener && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                mIsPaused = false;
            } else if (e.getKeyCode() == KeyEvent.VK_R) {
                mIsHalted = false;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }
    }
    
    /** The level parameters in a guided game of Tetrocity. A level is defined by 4 parameters:
     *   1) The drop speed increase factor of the live Tetriminoes.
     *   2) The spacing of the live Tetriminoes. 
     *   3) The length range of the live Tetriminoes.
     *   4) The score needed to progress to the next level. 
     *   
     *  These values are represented in 5-element float arrays, which are hard-coded into this class. 
     *  
     *  For example, level 1 in a game of Tetrocity has the level-array: [1.0, mBoardRows + 1, 3, 4, 20]. 
     * Thus, level 1:
     *   1) Has a drop speed of 1.0 row/second.
     *   2) Has a live Tetrimino spacing of mBoardRows + 1 (i.e. only 1 live Tetrimino active at a time)
     *   3) Has a live Tetrimino length-range of [3, 4]
     *   4) Requires 20 points to proceed to level 2. 
     * 
     * @author Nick Holt
     */
    private class GuidedLevelParameters {
        public static final int MAX_TETRIMINO_LENGTH = 5;
        
        private float[][] mLevelParameters;

        public GuidedLevelParameters() {
            mLevelParameters = new float[14][5];
            mLevelParameters[0] = new float[]{1, 26, 2, 3, 2};
            mLevelParameters[1] = new float[]{2, 26, 3, 4, 5};
            mLevelParameters[2] = new float[]{3, 26, 2, 4, 20};
            mLevelParameters[3] = new float[]{4, 21, 2, 4, 60};
            mLevelParameters[4] = new float[]{4, 21, 3, 4, 110};
            mLevelParameters[5] = new float[]{5, 16, 3, 4, 170};
            mLevelParameters[6] = new float[]{5, 21, 3, 5, 240};
            mLevelParameters[7] = new float[]{6, 16, 3, 5, 320}; //maybe put something here
            mLevelParameters[8] = new float[]{6, 12, 3, 5, 410};
            mLevelParameters[9] = new float[]{7, 12, 3, 5, 510};
            mLevelParameters[10] = new float[]{7, 10, 3, 5, 520};
            mLevelParameters[11] = new float[]{8, 10, 3, 5, 640};
            mLevelParameters[12] = new float[]{8, 6, 3, 5, 770};
            mLevelParameters[13] = new float[]{9, 6, 3, 5, 910};
        }
        
        public float getLevelDropFactor(int level) {
            if (level > mLevelParameters.length || level < 1) {
                throw new IllegalArgumentException("Level (" + level + ") not valid.");
            } else {
                return mLevelParameters[level - 1][0];
            }
        }
        
        public int getLevelLiveTetriminoSpacing(int level) {
            if (level > mLevelParameters.length || level < 1) {
                throw new IllegalArgumentException("Level (" + level + ") not valid.");
            } else {
                return (int) mLevelParameters[level - 1][1];
            }
        }
        
        public int[] getLevelLiveTetriminoLengthRange(int level) {
            if (level > mLevelParameters.length || level < 1) {
                throw new IllegalArgumentException("Level (" + level + ") not valid.");
            } else {
                return new int[]{(int) mLevelParameters[level - 1][2], (int) mLevelParameters[level - 1][3]};
            }
        }
        
        /**
         * @return The highest score possible until the next level should be activated.
         */
        public int getMaximumLevelScore(int level) {
            if (level > mLevelParameters.length || level < 1) {
                throw new IllegalArgumentException("Level (" + level + ") not valid.");
            } else {
                return (int) mLevelParameters[level - 1][4];
            }
        }
    }
}
