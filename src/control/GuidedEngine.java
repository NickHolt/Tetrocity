package control;

import gui.StorePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JLabel;

import model.Board;
import model.Tetrimino;
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
    /* The expected number of Tetriminoes between any two max-length straight line pieces. */
    public static final int STRAIGHT_LINE_EXPECTED_SPACING = 10;
    /* The factor by which the drop speed is multiplied as levels increase */
    public static final float DROP_SPEED_INCREASE_FACTOR = 1.15f;
    /* The base width of the JFrame is given by 20*ROW_RATIO*Max_tetrimino_length. 
     * In a normal game of Tetris, width = 400. */
    public static final int STANDARD_WIDTH_RATIO = 30;
    
    private int mLevel, mLinesClearedThisLevel;
    private boolean mIsPaused, mIsHalted;
    private Board mBoard;
    private TetriminoFactory mTetriminoFactory;
    private Player mPlayer;    
    private GuidedLevelParameters mGuidedLevelParameters;
    private JLabel mScoreBar;
    private StorePanel mStorePanel;
    
    /** A new GuidedEngine for a game of Tetrocity. Instantiating an Engine will set the game parameters.
     * @throws GameOverException 
     */
    public GuidedEngine() {
        mGuidedLevelParameters = new GuidedLevelParameters();

        mLevel = 1;
        mLinesClearedThisLevel = 0;
        mIsPaused = false;
        
        try {
            mTetriminoFactory = new TetriminoFactory(mGuidedLevelParameters.
                    getLevelLiveTetriminoLengthRange(1), (int) System.currentTimeMillis(),
                    STRAIGHT_LINE_EXPECTED_SPACING);
        } catch (GameOverException e) {
            System.out.println("FATAL ERROR.");
            System.exit(0);
        }
        
        
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
                mBoard.shiftAllLiveTetriminoes(Direction.SOUTH);
                dropTime = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() >= logicTime + logicPeriod) {
                if (mLinesClearedThisLevel >= mGuidedLevelParameters.getNextLevelLinesCleared(mLevel)) {
                    try {
                        mLinesClearedThisLevel = 0;
                        mLevel++; //update level
                        dropPeriod = (1 / (float) (INITIAL_DROP_SPEED * 
                                Math.pow(DROP_SPEED_INCREASE_FACTOR, 
                                        mGuidedLevelParameters.getLevelDropFactor(mLevel)))) * 1000;
                        mTetriminoFactory.
                            setLengthRange(mGuidedLevelParameters.getLevelLiveTetriminoLengthRange(mLevel));
                        
                        mBoard.clearTetriminoQueue();
                    } catch (GameOverException e) {
                        gameOver();
                    }
                } 
                
                try {
                    doGameLogic();
                } catch (GameOverException e) {
                    gameOver();
                }
                
                mScoreBar.setText("Level: " + mLevel + ", Score: " + String.valueOf(mPlayer.getScore()));
                logicTime = System.currentTimeMillis();
            }
        }
    }
    
    private void constructGUI() {
        int rows = (int) ((GuidedLevelParameters.MAX_TETRIMINO_LENGTH - 1) * ROW_RATIO),
                cols = (int) ((GuidedLevelParameters.MAX_TETRIMINO_LENGTH - 1) * COLUMN_RATIO),
                buffer = GuidedLevelParameters.MAX_TETRIMINO_LENGTH;
        mBoard = new Board(rows, cols, buffer);
                add(mBoard, BorderLayout.CENTER);
        
        mScoreBar = new JLabel("Level: 1, Score: " + String.valueOf(mPlayer.getScore()));
        add(mScoreBar, BorderLayout.SOUTH);
        
        mStorePanel = new StorePanel(mTetriminoFactory.getLengthRange()[1], mBoard);
                
        float width = STANDARD_WIDTH_RATIO * cols,
                height = (int) (width * rows / cols);
        
        mStorePanel.setPreferredSize(
                new Dimension((int) GuidedLevelParameters.MAX_TETRIMINO_LENGTH * STANDARD_WIDTH_RATIO
                        , (int) height));
        //add(mStorePanel);

                        
        setSize((int) width, (int) height);
        setTitle("Tetrocity");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void doGameLogic() throws GameOverException {
        fillBoardQueue();

        interpretInput(mPlayer.getMoveKeyCode());
        
        int rowsCleared = mBoard.clearRows();
        updateScore(rowsCleared);
        mLinesClearedThisLevel += rowsCleared;
        
        Tetrimino topLiveTetrimino = mBoard.getTopLiveTetrimino();
        if (topLiveTetrimino == null ||
                (mBoard.numLiveTetriminoes() != 0 &&
                topLiveTetrimino.getRootCoordinate()[0] - mBoard.getBuffer() >=
                mGuidedLevelParameters.getLevelLiveTetriminoSpacing(mLevel))) {
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
        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_KP_UP) {
            //do nothing (for now)
        } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_KP_RIGHT) {
            mBoard.shiftTetrimino(Direction.EAST);
        } else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_KP_DOWN) {
            mBoard.shiftTetrimino(Direction.SOUTH);
        } else if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_KP_LEFT) {
            mBoard.shiftTetrimino(Direction.WEST);
        } else if (keyCode == KeyEvent.VK_SPACE) {
            mBoard.dropTetrimino();
        } else if (keyCode == KeyEvent.VK_SHIFT) {
            try {
                mBoard.storeTetrimino();
                //mStorePanel.updateGrid();
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
        return (long) (Math.pow(linesCleared, 1.3) * mLevel); //1.3 ~ log_4_(6)
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
            System.out.print(""); //This fixes a bug and I don't know why. 
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
        mScoreBar.setText("GAME OVER! Level: " + mLevel + ", Score: " + mPlayer.getScore());
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
            } else if (mIsPausedListener && e.getKeyCode() == KeyEvent.VK_R) {
                restart();
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
     *   4) The number of lines needed to progress to the next level. 
     *   
     *  These values are represented in 5-element float arrays, which are hard-coded into this class. 
     *  
     *  For example, level 1 in a game of Tetrocity has the level-array: [1.0, 26, 3, 3, 2]. 
     * Thus, level 1:
     *   1) Has a drop speed of 1.0 row/second.
     *   2) Has a live Tetrimino spacing of mBoardRows + 1 (i.e. only 1 live Tetrimino active at a time)
     *   3) Has a live Tetrimino length-range of [3, 3] (i.e. only 3)
     *   4) Requires 2 cleared lines to progress to level 2. 
     * 
     * @author Nick Holt
     */
    private class GuidedLevelParameters {
        public static final int MAX_TETRIMINO_LENGTH = 6;
        
        private float[][] mLevelParameters;

        public GuidedLevelParameters() {
            mLevelParameters = new float[50][5];
            mLevelParameters[0] = new float[]{1, 26, 3, 3, 2};
            mLevelParameters[1] = new float[]{1, 26, 3, 4, 2};
            mLevelParameters[2] = new float[]{2, 24, 3, 4, 2};
            mLevelParameters[3] = new float[]{2, 24, 3, 4, 3};
            mLevelParameters[4] = new float[]{3, 23, 3, 4, 3};
            mLevelParameters[5] = new float[]{3, 23, 3, 4, 3};
            mLevelParameters[6] = new float[]{4, 21, 3, 4, 4};
            mLevelParameters[7] = new float[]{4, 21, 3, 4, 4};
            mLevelParameters[8] = new float[]{5, 20, 3, 4, 4};
            mLevelParameters[9] = new float[]{5, 20, 3, 4, 4};
            
            mLevelParameters[10] = new float[]{3, 25, 3, 5, 5};
            mLevelParameters[11] = new float[]{3, 25, 3, 5, 5};
            mLevelParameters[12] = new float[]{4, 23, 3, 5, 5};
            mLevelParameters[13] = new float[]{4, 23, 3, 5, 6};
            mLevelParameters[14] = new float[]{5, 21, 3, 5, 6};
            mLevelParameters[15] = new float[]{5, 21, 3, 5, 6};
            mLevelParameters[16] = new float[]{6, 19, 3, 5, 7};
            mLevelParameters[17] = new float[]{6, 19, 3, 5, 7};
            mLevelParameters[18] = new float[]{7, 18, 3, 5, 7};
            mLevelParameters[19] = new float[]{7, 18, 3, 5, 7};
            
            mLevelParameters[20] = new float[]{5, 24, 4, 5, 8};
            mLevelParameters[21] = new float[]{5, 24, 4, 5, 8};
            mLevelParameters[22] = new float[]{6, 22, 4, 5, 8};
            mLevelParameters[23] = new float[]{6, 22, 4, 5, 9};
            mLevelParameters[24] = new float[]{7, 20, 4, 5, 9};
            mLevelParameters[25] = new float[]{7, 20, 4, 5, 9};
            mLevelParameters[26] = new float[]{8, 18, 4, 5, 10};
            mLevelParameters[27] = new float[]{8, 18, 4, 5, 10};
            mLevelParameters[28] = new float[]{9, 16, 4, 5, 10};
            mLevelParameters[29] = new float[]{9, 16, 4, 5, 10};
            
            mLevelParameters[30] = new float[]{7, 22, 5, 5, 11};
            mLevelParameters[31] = new float[]{7, 22, 5, 5, 11};
            mLevelParameters[32] = new float[]{8, 20, 5, 5, 11};
            mLevelParameters[33] = new float[]{8, 20, 5, 5, 11};
            mLevelParameters[34] = new float[]{9, 19, 5, 5, 11};
            mLevelParameters[35] = new float[]{9, 19, 5, 5, 12};
            mLevelParameters[36] = new float[]{10, 17, 5, 5, 12};
            mLevelParameters[37] = new float[]{10, 17, 5, 5, 12};
            mLevelParameters[38] = new float[]{11, 15, 5, 5, 12};
            mLevelParameters[39] = new float[]{11, 15, 5, 5, 12};
            
            mLevelParameters[40] = new float[]{9, 20, 6, 6, 13};
            mLevelParameters[41] = new float[]{9, 20, 6, 6, 13};
            mLevelParameters[42] = new float[]{10, 18, 6, 6, 13};
            mLevelParameters[43] = new float[]{10, 18, 6, 6, 13};
            mLevelParameters[44] = new float[]{11, 17, 6, 6, 13};
            mLevelParameters[45] = new float[]{11, 17, 6, 6, 14};
            mLevelParameters[46] = new float[]{12, 15, 6, 6, 14};
            mLevelParameters[47] = new float[]{12, 15, 6, 6, 14};
            mLevelParameters[48] = new float[]{13, 13, 6, 6, 14};
            mLevelParameters[49] = new float[]{13, 13, 6, 6, 14};
        }
        
        public float getLevelDropFactor(int level) throws GameOverException {
            if (level > mLevelParameters.length || level < 1) {
                throw new GameOverException("Level (" + level + ") not valid.");
            } else {
                return mLevelParameters[level - 1][0];
            }
        }
        
        public int getLevelLiveTetriminoSpacing(int level) throws GameOverException {
            if (level > mLevelParameters.length || level < 1) {
                throw new GameOverException("Level (" + level + ") not valid.");
            } else {
                return (int) mLevelParameters[level - 1][1];
            }
        }
        
        public int[] getLevelLiveTetriminoLengthRange(int level) throws GameOverException {
            if (level > mLevelParameters.length || level < 1) {
                throw new GameOverException("Level (" + level + ") not valid.");
            } else {
                return new int[]{(int) mLevelParameters[level - 1][2], (int) mLevelParameters[level - 1][3]};
            }
        }
        
        /**
         * @return The number of lines that need to be cleared before you can progress
         * to the next level.
         */
        public int getNextLevelLinesCleared(int level) {
            if (level > mLevelParameters.length || level < 1) {
                throw new IllegalArgumentException("Level (" + level + ") not valid.");
            } else {
                return (int) mLevelParameters[level - 1][4];
            }
        }
    }
}
