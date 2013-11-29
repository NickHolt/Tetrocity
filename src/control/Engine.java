package control;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import audio.SoundEffect;
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
public class Engine extends JFrame{
    private static final long serialVersionUID = 1L;
    /* The number of times per second logical operations are performed. */
    public static final int LOGICAL_FPS = 60;
    /* E.g. number of grid rows = max_length * ROW_RATIO */
    public static final float ROW_RATIO = 5.0f, COLUMN_RATIO = 2.5f;
    /* The initial Tetrimino drop speed. */
    public static final float INITIAL_DROP_SPEED = 1.1f; //blocks / second
    /* The expected number of Tetriminoes between any two max-length straight line pieces. */
    public static final int STRAIGHT_LINE_EXPECTED_SPACING = 10;
    /* The factor by which the drop speed is multiplied as levels increase */
    public static final float DROP_SPEED_INCREASE_FACTOR = 1.1f;
    /* This value may be tweaked to scale all components proportionally. */
    private static final int BASE_WIDTH_FACTOR = 20;
    /* Ability constants. */
    public static final int BOOST_DURATION = 3;
    public static final int BOOST_COOLDOWN = 25;
    public static final int STRAIGHT_LINE_ABILITY_COOLDOWN = 60;
    public static final int ZERO_GRAVITY_DURATION = 5;
    public static final int ZERO_GRAVITY_COOLDOWN = 120;
    public static final int CLEAR_GRID_COOLDOWN = 300;
    /* Game components. */
    private Board mBoard;
    private TetriminoFactory mTetriminoFactory;
    private Player mPlayer;    
    private GuidedLevelParameters mLevelParameters;
    private Board.SidePanel mSidePanel;
    private ImagePanel mAbilityPanel0, mAbilityPanel1, mAbilityPanel2, mAbilityPanel3;
    private JLabel mScoreBar;
    /* Game states. */
    private int mLevel, mLinesClearedThisLevel;
    private boolean mIsWelcoming, mIsPaused, mIsHalted, mSoundEffectsEnabled;
    /* Ability states. */
    private boolean mBoostEnabled, mBoostAvailable, mBoostUnlocked;
    private long mBoostPreviousTime;
    private boolean mLinePieceAbilityAvailable, mLinePieceAbilityUnlocked;
    private long mLinePiecePreviousTime;
    private boolean mZeroGravityEnabled, mZeroGravityAvailable, mZeroGravityUnlocked;
    private long mZeroGravityPreviousTime;
    private boolean mClearGridAvailable, mClearGridUnlocked;
    private long mClearGridPreviousTime;
    
    
    /** A new GuidedEngine for a game of Tetrocity. Instantiating an Engine will set the game parameters,
     * however the GUI will not be constructed until {@link Engine#run()} is called.
     * 
     *  A new Engine begins a game of Tetrocity at level 1, with a score of 0 and all abilities 
     * locked. 
     */
    public Engine() {
        mLevelParameters = new GuidedLevelParameters();

        mLevel = 1;
        mLinesClearedThisLevel = 0;
        mIsPaused = false;
        mSoundEffectsEnabled = true;
        mBoostEnabled = mBoostAvailable = mBoostUnlocked =
                mLinePieceAbilityAvailable = mLinePieceAbilityUnlocked =
                mZeroGravityEnabled = mZeroGravityAvailable = mZeroGravityUnlocked =
                mClearGridAvailable = mClearGridUnlocked = false;
                
        mBoostPreviousTime = mLinePiecePreviousTime = mZeroGravityPreviousTime
                = mClearGridPreviousTime = 0;
        
        mTetriminoFactory = new TetriminoFactory(mLevelParameters.
                getLevelLiveTetriminoLengthRange(mLevel), (int) System.currentTimeMillis(),
                STRAIGHT_LINE_EXPECTED_SPACING);
        
        
        mPlayer = new Player();
        
        addKeyListener(mPlayer);
    }
    
    /** Begin the game of Tetrocity. This method facilitates the timing of the logical operations
     * of the Engine. This method firsts construct the GUI and presents the player the welcome
     * message. Then, the following operations are performed:
     * * Drop pieces periodically as dictated by the {@link Engine#INITIAL_DROP_SPEED} and level increase 
     * factor.
     * * Check if the player has cleared enough lines to progress to the next level. If so,
     * update level parameters.
     * * Perform logical operations, such as line clearing and queue filling periodically as dictated by 
     * the {@link Engine#LOGICAL_FPS}.
     * * End the game if a {@link GameOverException} is observed.
     */
    public void run() {        
        constructGUI();
        welcome();
        SoundEffect.GAME_START.play();
        
        /* Run the game. */
        float dropPeriod = (1 / 
                ((float) INITIAL_DROP_SPEED * mLevelParameters.getLevelDropFactor(mLevel))) * 1000,
                logicPeriod = (1 / (float) LOGICAL_FPS) * 1000;
                        
        double dropTime = 0, 
                logicTime = 0; //The last system time these operations ran
                
        while (true) {
            if (!mZeroGravityEnabled 
                    && System.currentTimeMillis() >= dropTime + dropPeriod) { //drop Tetriminoes
                mBoard.shiftAllLiveTetriminoes(Direction.SOUTH);
                dropTime = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() >= logicTime + logicPeriod) { //update level
                if (mLinesClearedThisLevel >= mLevelParameters.getNextLevelLinesCleared(mLevel)) {
                    mLinesClearedThisLevel = 0;
                    mLevel++;
                    dropPeriod = (1 / (float) (INITIAL_DROP_SPEED * 
                            Math.pow(DROP_SPEED_INCREASE_FACTOR, 
                                    mLevelParameters.getLevelDropFactor(mLevel)))) * 1000;
                    mTetriminoFactory.
                        setLengthRange(mLevelParameters.getLevelLiveTetriminoLengthRange(mLevel));    
                    
                    if (mLevel == 10) { //unlock abilities (note these levels cannot be skipped)
                        setBoostGraphic();
                        mBoostUnlocked = true;
                    } else if (mLevel == 20) {
                        setStraightLineGraphic();
                        mLinePieceAbilityUnlocked = true;
                    } else if (mLevel == 30) {
                        setZeroGravityGraphic();
                        mZeroGravityUnlocked = true;
                    } else if (mLevel == 40) {
                        setClearGridGraphic();
                        mClearGridUnlocked = true;
                    }
                    
                    if (mSoundEffectsEnabled) {
                        SoundEffect.LEVEL_UP.play();
                    }
                }   
                
                doGameLogic();
                
                setLevelAndScoreMessage();
                logicTime = System.currentTimeMillis();
            }
        }
    }
    
    /***********************/
    /* Logical Operations. */
    /***********************/

    /** Performs the period game logic functions of this Engine. These roles are, in this order:
     * *Ensure the {@link Board}'s Tetrimino queue is full.
     * *Attempt to clear rows and update the player's score as needed.
     * *Check if there currently is a live Tetrimino on the screen or the spacing between two
     * is less than that given by the level parameters. If not, put a Tetrimino on the board.
     * *If putting a Tetrimino on the Board caused a GameOverException, run the game over protocol.
     * *Update ability states
     * *Interpret one player input
     * 
     */
    private void doGameLogic() {
        fillBoardQueue();
        
        int rowsCleared = mBoard.clearRows();
        updateScore(rowsCleared);
        mLinesClearedThisLevel += rowsCleared;
        
        if (rowsCleared > 0) {
            if (rowsCleared <= GuidedLevelParameters.MAX_TETRIMINO_LENGTH / 3) {
                SoundEffect.CLEAR_SINGLE.play();
            } else if (rowsCleared <= 2 * GuidedLevelParameters.MAX_TETRIMINO_LENGTH / 3) {
                SoundEffect.CLEAR_DOUBLE.play();
            } else {
                SoundEffect.CLEAR_TRIPLE.play();
            }
        }
        
        
        Tetrimino topLiveTetrimino = mBoard.getTopLiveTetrimino();
        if (topLiveTetrimino == null ||
                (mBoard.numLiveTetriminoes() != 0 &&
                topLiveTetrimino.getRootCoordinate()[0] - mBoard.getBuffer() >=
                mLevelParameters.getLevelLiveTetriminoSpacing(mLevel))) {
            try {
                mBoard.putTetrimino();
                
                if (mSoundEffectsEnabled) {
                    SoundEffect.BLIP.play();
                }
            } catch (GameOverException e) {
                gameOver();
            }
        }
        
        if (mBoard.numLiveTetriminoes() == 0) {
            try {
                mBoard.putTetrimino();
                
                if (mSoundEffectsEnabled) {
                    SoundEffect.BLIP.play();
                }
            } catch (GameOverException e) {
                gameOver();
            }
        }      
        
        updateAbilityStates();
        interpretInput(mPlayer.getMoveKeyCode());
    }
    
    /** Update the player's score given the number of lines cleared. 
     * 
     * @param linesCleared The number of lines cleared.
     */
    private void updateScore(int linesCleared) {
        mPlayer.addToScore(scoreLinesCleared(linesCleared));
    }

    /** Uses current game information to interpret the corresponding increase in score after
     * a number of lines have been cleared.
     * 
     *  The equation used is linesCleared^1.3 * e^(3*level/max_level). 
     *  
     *  The first term rewards multiple lines cleared at once with an exponentially weighted 
     * bonus. The second term causes higher levels to reward exponentially higher scores in 
     * a similar fashion. The particular constants used in this equation were determined via
     * an informal analysis. 
     * 
     * @param linesCleared The number of lines cleared.
     * @return The amount of score to add. 
     */
    private double scoreLinesCleared(int linesCleared) {
        double baseScore = Math.pow(linesCleared, 1.3) 
                * Math.exp(3 * mLevel / (float) GuidedLevelParameters.MAX_LEVEL);
        if (mBoostEnabled) {
            return 2 * baseScore;
        } else {
            return baseScore;
        }
    }

    /** Uses current game information to interpret the corresponding increase in score after
     * a Tetrimino piece was dropped a number of lines. 
     * 
     *  The equation used is linesDropped / (total_rows - max_length) * e^(level / max_level).
     * This equation was determined by an informal analysis. See 
     * {@link Engine#scoreLinesCleared} for a similar explanation of reasoning. 
     * 
     * @param linesDropped The number of lines dropped.
     * @return The amount of score to add. 
     */
    private double scoreLinesDropped(int linesDropped) {
        double baseScore = linesDropped / (double) (mBoard.getGridDimensions()[0] 
                        - GuidedLevelParameters.MAX_TETRIMINO_LENGTH)
                * Math.exp(mLevel / (double) GuidedLevelParameters.MAX_LEVEL);
        if (mBoostEnabled) {
            return 2 * baseScore;
        } else {
            return baseScore;
        }
    }

    /** Updates ability states. This method checks the last time the ability was used, 
     * and determines if the ability is either active, ready, or on cooldown. Graphic
     * information and ability state variables are updated accordingly. 
     */
    private void updateAbilityStates() {
        if (mBoostUnlocked) {
            long elapsedTime = System.currentTimeMillis() - mBoostPreviousTime;
            
            if (elapsedTime > BOOST_COOLDOWN * 1000) {
                mBoostEnabled = false;
                mBoostAvailable = true;
                mAbilityPanel0.setTopText("A");
                mAbilityPanel0.setBottomText("Boost Score");
            } else if (elapsedTime < BOOST_DURATION * 1000) { //ability is still active
                mBoostEnabled = true;
                mBoostAvailable = false;
                mAbilityPanel0.setTopText("");
                mAbilityPanel0.setBottomText("*" + String.valueOf((int) (Math.ceil(BOOST_DURATION - 
                        (elapsedTime / (float) 1000)))) + "*");
            } else { //inactive, on cooldown
                mBoostEnabled = false;
                mBoostAvailable = false;
                mAbilityPanel0.setTopText("");
                mAbilityPanel0.setBottomText(String.valueOf((int) (Math.ceil((float) BOOST_COOLDOWN - 
                        (elapsedTime / (float) 1000)))));
            }
        }
        if (mLinePieceAbilityUnlocked) {
            long elapsedTime = System.currentTimeMillis() - mLinePiecePreviousTime;
            
            if (elapsedTime > STRAIGHT_LINE_ABILITY_COOLDOWN * 1000) {
                mLinePieceAbilityAvailable = true;
                mAbilityPanel1.setTopText("S");
                mAbilityPanel1.setBottomText("Line Piece");
            } else {
                mLinePieceAbilityAvailable = false;
                mAbilityPanel1.setTopText("");
                mAbilityPanel1.setBottomText(String.valueOf((int) 
                        (Math.ceil((float) STRAIGHT_LINE_ABILITY_COOLDOWN - (elapsedTime / (float) 1000)))));
            }
        }
        if (mZeroGravityUnlocked) {
            long elapsedTime = System.currentTimeMillis() - mZeroGravityPreviousTime;
            
            if (elapsedTime > ZERO_GRAVITY_COOLDOWN * 1000) {
                mZeroGravityEnabled = false;
                mZeroGravityAvailable = true;
                mAbilityPanel2.setTopText("D");
                mAbilityPanel2.setBottomText("Zero Gravity");
            } else if (elapsedTime < ZERO_GRAVITY_DURATION * 1000) {
                mZeroGravityEnabled = true;
                mZeroGravityAvailable = false;
                mAbilityPanel2.setTopText("");
                mAbilityPanel2.setBottomText("*" + String.valueOf((int) Math.ceil((ZERO_GRAVITY_DURATION - 
                        (elapsedTime / (float) 1000)))) + "*");
            } else {
                mZeroGravityEnabled = false;
                mZeroGravityAvailable = false;
                mAbilityPanel2.setTopText("");
                mAbilityPanel2.setBottomText(String.valueOf((int) (Math.ceil((float) ZERO_GRAVITY_COOLDOWN - 
                        (elapsedTime / (float) 1000)))));
            }
        }
        if (mClearGridUnlocked) {
            long elapsedTime = System.currentTimeMillis() - mClearGridPreviousTime;
            
            if (elapsedTime > CLEAR_GRID_COOLDOWN * 1000) {
                mClearGridAvailable = true;
                mAbilityPanel3.setTopText("F");
                mAbilityPanel3.setBottomText("Clear Grid");
            } else {
                mClearGridAvailable = false;
                mAbilityPanel3.setTopText("");
                mAbilityPanel3.setBottomText(String.valueOf((int) (Math.ceil((float) CLEAR_GRID_COOLDOWN - 
                        (elapsedTime / (float) 1000)))));
            }
        }
    }

    /** Interpret a KeyEvent keyCode and respond as dictated by the game rules.
     * 
     * @param keyCode The key code to interpret. 
     */
    private void interpretInput(int keyCode) {
        if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_KP_RIGHT) {
            mBoard.shiftTetrimino(Direction.EAST);
            if (mSoundEffectsEnabled) {
                SoundEffect.BLIP.play();
            }
        } else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_KP_DOWN) {
            mBoard.shiftTetrimino(Direction.SOUTH);
            if (mSoundEffectsEnabled) {
                SoundEffect.SOFT_DROP.play();
            }
        } else if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_KP_LEFT) {
            mBoard.shiftTetrimino(Direction.WEST);
            if (mSoundEffectsEnabled) {
                SoundEffect.BLIP.play();
            }
        } else if (keyCode == KeyEvent.VK_SPACE) {
            mPlayer.addToScore(scoreLinesDropped(mBoard.dropTetrimino()));
            if (mSoundEffectsEnabled) {
                SoundEffect.HARD_DROP.play();
            }
        } else if (keyCode == KeyEvent.VK_SHIFT) {
            try {
                boolean storeSuccess = mBoard.storeTetrimino();
                
                if (mSoundEffectsEnabled) {
                    if (storeSuccess) {
                        SoundEffect.STORE.play();
                    } else {
                        SoundEffect.ROTATE_FAIL.play();
                    }
                }
            } catch (GameOverException e) {
                gameOver();
            }
        } else if (keyCode == KeyEvent.VK_Z) {
            boolean rotateSuccess = mBoard.rotateTetriminoCounterClockwise();
            
            if (mSoundEffectsEnabled) {
                if (rotateSuccess) {
                    SoundEffect.ROTATE.play();
                } else {
                    SoundEffect.ROTATE_FAIL.play();
                }
            }
        } else if (keyCode == KeyEvent.VK_X || keyCode == KeyEvent.VK_UP 
                || keyCode == KeyEvent.VK_KP_UP) {
            boolean rotateSuccess = mBoard.rotateTetriminoClockwise();
            
            if (mSoundEffectsEnabled) {
                if (rotateSuccess) {
                    SoundEffect.ROTATE.play();
                } else {
                    SoundEffect.ROTATE_FAIL.play();
                }
            }
        } else if (keyCode == KeyEvent.VK_ESCAPE) {
            if (mSoundEffectsEnabled) {
                SoundEffect.BLIP.play();
            }
            
            pause();
        } else if (keyCode == KeyEvent.VK_R) {
            if (mSoundEffectsEnabled) {
                SoundEffect.BLIP.play();
            }
            
            restart();
        } else if (keyCode == KeyEvent.VK_A){
            if (mBoostUnlocked && mBoostAvailable) {
                mBoostEnabled = true;
                mBoostPreviousTime = System.currentTimeMillis();
                
                if (mSoundEffectsEnabled) {
                    SoundEffect.SCORE_BOOST.play();
                }
            }
        } else if (keyCode == KeyEvent.VK_S){
            if (mLinePieceAbilityUnlocked && mLinePieceAbilityAvailable) {
                mBoard.storeTetrimino(mTetriminoFactory.getRandomMaxLengthStraightLineTetrimino());
                mLinePiecePreviousTime = System.currentTimeMillis();
                
                if (mSoundEffectsEnabled) {
                    SoundEffect.LINE_PIECE.play();
                }
            }
        } else if (keyCode == KeyEvent.VK_D){
            if (mZeroGravityUnlocked && mZeroGravityAvailable) {
                mZeroGravityEnabled = true;
                mZeroGravityPreviousTime = System.currentTimeMillis();
                
                if (mSoundEffectsEnabled) {
                    SoundEffect.ZERO_GRAVITY.play();
                }
            }
        } else if (keyCode == KeyEvent.VK_F) {
            if (mClearGridUnlocked && mClearGridAvailable) {
                mBoard.emptyGrid();
                mClearGridPreviousTime = System.currentTimeMillis();
                
                if (mSoundEffectsEnabled) {
                    SoundEffect.CLEAR_GRID.play();
                }
            }
        } else if (keyCode == KeyEvent.VK_M) {
            mSoundEffectsEnabled = !mSoundEffectsEnabled;
        }
    }

    /** Ensures that this Engine's {@link Board} Tetrimino queue is full.
     */
    private void fillBoardQueue() {
        while (mBoard.queueTooSmall()) {
            mBoard.enqueueTetrimino(mTetriminoFactory.getRandomTetrimino());
        }
    }
    
    /*************************/
    /* Game state protocols. */
    /*************************/
    
    /** Runs the welcome protocol. This simply hides all components except for the JLabel,
     * displays the appropriate message, and waits for the player to press the appropriate
     * key to start the game.
     */
    private void welcome() {
        mIsWelcoming = true;
        LoopKeyListener welcomeKeyListener = new LoopKeyListener(LoopKeyListener.WELCOME);
        removeKeyListener(mPlayer);
        addKeyListener(welcomeKeyListener);
        
        setWelcomeMessage();
        
        mBoard.setVisible(false);
        mSidePanel.setVisible(false);
        mAbilityPanel0.setVisible(false);
        mAbilityPanel1.setVisible(false);
        mAbilityPanel2.setVisible(false);
        mAbilityPanel3.setVisible(false);

        
        while (mIsWelcoming) {
            System.out.print("");
        }
        
        removeKeyListener(welcomeKeyListener);
        addKeyListener(mPlayer);
        
        mBoard.setVisible(true);
        mSidePanel.setVisible(true);
        mAbilityPanel0.setVisible(true);
        mAbilityPanel1.setVisible(true);
        mAbilityPanel2.setVisible(true);
        mAbilityPanel3.setVisible(true);
    }
    
    /** Runs the pause protocol. This simply hides all components except for the JLabel,
     * displays the appropriate message, and waits for the player to press the appropriate
     * key to resume the game.
     */
    private void pause() {
        mIsPaused = true;
        LoopKeyListener pausedKeyListener = new LoopKeyListener(LoopKeyListener.PAUSED);
        removeKeyListener(mPlayer);
        addKeyListener(pausedKeyListener);
        
        setPausedMessage();
        
        mBoard.setVisible(false);
        mSidePanel.setVisible(false);
        mAbilityPanel0.setVisible(false);
        mAbilityPanel1.setVisible(false);
        mAbilityPanel2.setVisible(false);
        mAbilityPanel3.setVisible(false);

        long timePaused = System.currentTimeMillis();
        while (mIsPaused) {
            System.out.print(""); //This fixes a bug and I don't know why. 
        }
        long totalTimePaused = System.currentTimeMillis() - timePaused;
        
        /* Update cooldowns to prevent cheating. */
        if (mBoostUnlocked) {
            mBoostPreviousTime += totalTimePaused;
        }
        if (mLinePieceAbilityUnlocked) {
            mLinePiecePreviousTime += totalTimePaused;
        }
        if (mZeroGravityUnlocked) {
            mZeroGravityPreviousTime += totalTimePaused;
        }
        if (mClearGridUnlocked) {
            mClearGridPreviousTime += totalTimePaused;
        }
        
        removeKeyListener(pausedKeyListener);
        addKeyListener(mPlayer);
        
        mBoard.setVisible(true);
        mSidePanel.setVisible(true);
        mAbilityPanel0.setVisible(true);
        mAbilityPanel1.setVisible(true);
        mAbilityPanel2.setVisible(true);
        mAbilityPanel3.setVisible(true);
    }
    
    /** Runs the welcome protocol. This resets all state variables and components. 
     */
    private void restart() {
        mLevel = 1;
        mLinesClearedThisLevel = 0;
        mPlayer.resetScore();
        mBoard.restartBoard();
        
        mBoostEnabled = mBoostAvailable = mBoostUnlocked =
                mLinePieceAbilityAvailable = mLinePieceAbilityUnlocked =
                mZeroGravityEnabled = mZeroGravityAvailable = mZeroGravityUnlocked =
                mClearGridAvailable = mClearGridUnlocked = false;
                
        mBoostPreviousTime = mLinePiecePreviousTime = mZeroGravityPreviousTime
                = mClearGridPreviousTime = 0;
        
        Image lockImage = Toolkit.getDefaultToolkit().getImage(getClass()
                .getResource("/resources/images/lock.png"));     
        
        mAbilityPanel0.setImage(lockImage);
        mAbilityPanel0.setTopText("");
        mAbilityPanel0.setBottomText("Level 10");
        mAbilityPanel1.setImage(lockImage);
        mAbilityPanel1.setTopText("");
        mAbilityPanel1.setBottomText("Level 20");
        mAbilityPanel2.setImage(lockImage);
        mAbilityPanel2.setTopText("");
        mAbilityPanel2.setBottomText("Level 30");
        mAbilityPanel3.setImage(lockImage);
        mAbilityPanel3.setTopText("");
        mAbilityPanel3.setBottomText("Level 40");
        
        mTetriminoFactory.
            setLengthRange(mLevelParameters.getLevelLiveTetriminoLengthRange(mLevel)); 
        fillBoardQueue();
        
        mIsPaused = false;
        mIsHalted = false;        
    }
    
    /** Runs the halt protocol. This simply displays the appropriate message, and waits for the 
     * player to press the appropriate key to start a new game.
     */
    private void halt() {
        removeKeyListener(mPlayer);
        
        LoopKeyListener loopKeyListener = new LoopKeyListener(LoopKeyListener.HALTED);
        addKeyListener(loopKeyListener);
        mIsHalted = true;
        while(mIsHalted) {
            //Loop forever
            System.out.print("");
        }
        
        removeKeyListener(loopKeyListener);
        addKeyListener(mPlayer);
        restart();
    }
    
    /** Runs the game over protocol. This simply displays the appropriate message, sound effect,
     * and then halts. 
     * 
     *  This protocol is run when the game is over.
     */
    private void gameOver() {
        SoundEffect.GAME_OVER.play();
        setGameOverMessage();
        halt();
    }
    
    /********************/
    /* GUI Manipulation */
    /********************/
    
    /** Creates all components of the GUI, alone with the appropriate initial text field values.
     * This Engine is a JFrame whose size is determined by 
     * {@link GuidedLevelParameters#MAX_TETRIMINO_LENGTH}, {@link Engine#ROW_RATIO}, 
     * {@link Engine#COLUMN_RATIO} and {@link Engine#BASE_WIDTH_FACTOR}.
     * 
     *  The layout of the JFrame is a {@link GridBagLayout} with 7 components: the {@link Board.SidePanel},
     * the {@link Board} itself, 4 {@link ImagePanel}s representing the ability graphics, and 
     * a JLabel to display game statistics such as score and level.
     */
    private void constructGUI() {
        int rows = (int) ((GuidedLevelParameters.MAX_TETRIMINO_LENGTH - 1) * ROW_RATIO),
                cols = (int) ((GuidedLevelParameters.MAX_TETRIMINO_LENGTH - 1) * COLUMN_RATIO),
                buffer = GuidedLevelParameters.MAX_TETRIMINO_LENGTH;
        mBoard = new Board(rows, cols, buffer, GuidedLevelParameters.MAX_TETRIMINO_LENGTH);
                
        mSidePanel = mBoard.getSidePanel();        
        
        mScoreBar = new JLabel();
        setLevelAndScoreMessage();
        
        Image lockImage = Toolkit.getDefaultToolkit().getImage(getClass()
                .getResource("/resources/images/lock.png"));     
        
        mAbilityPanel0 = new ImagePanel(lockImage); //Everything begins locked
        mAbilityPanel0.setBottomText("Level 10");
        mAbilityPanel1 = new ImagePanel(lockImage);
        mAbilityPanel1.setBottomText("Level 20");
        mAbilityPanel2 = new ImagePanel(lockImage);
        mAbilityPanel2.setBottomText("Level 30");
        mAbilityPanel3 = new ImagePanel(lockImage);
        mAbilityPanel3.setBottomText("Level 40");
                          
        float width = 2 * BASE_WIDTH_FACTOR * cols,
                height = (int) (width * rows / cols);
                        
        setSize((int) (width * 1.25), (int) (height * 0.73)); //tweaking for windows taskbar
        setTitle("Tetrocity");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
        setFocusable(true);
    
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        c.weighty = 1;
        c.anchor = GridBagConstraints.LINE_START;
                
        c.fill = GridBagConstraints.BOTH;
        c.gridy = 0;
        c.gridx = 0;
        c.gridheight = 4;
        c.gridwidth = 1;
        c.weightx = 0.2;
        add(mSidePanel, c);
        
        c.gridy = 0;
        c.gridx = 1;
        c.gridheight = 4;
        c.gridwidth = 1;
        c.weightx = 0.4;
        add(mBoard, c);
        
        c.gridy = 0;
        c.gridx = 2;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.weightx = 0.1;
        add(mAbilityPanel0, c);
        
        c.gridy = 1;
        c.gridx = 2;
        add(mAbilityPanel1, c);
        
        c.gridy = 2;
        c.gridx = 2;
        add(mAbilityPanel2, c);
        
        c.gridy = 3;
        c.gridx = 2;
        add(mAbilityPanel3, c);
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 3;
        c.weighty = 0;
        c.weightx = 0;
        c.anchor = GridBagConstraints.PAGE_END;
        c.gridy = 4;
        c.gridx = 0;
        add(mScoreBar, c); 
    }

    /** Sets the first ImagePanel's graphic to represent the boost ability. 
     */
    private void setBoostGraphic() {
        Image image = Toolkit.getDefaultToolkit().getImage(getClass()
                .getResource("/resources/images/boost_score.png"));  
        
        mAbilityPanel0.setImage(image);
        mAbilityPanel0.setBottomText("Score Boost");
    }

    /** Sets the second ImagePanel's graphic to represent the new straight-line ability. 
     */
    private void setStraightLineGraphic() {
        Image image = Toolkit.getDefaultToolkit().getImage(getClass()
                .getResource("/resources/images/line_piece.png"));   
        
        mAbilityPanel1.setImage(image);
        mAbilityPanel1.setBottomText("Line Piece");
    }

    /** Sets the third ImagePanel's graphic to represent the zero-gravity ability. 
     */
    private void setZeroGravityGraphic() {
        Image image = Toolkit.getDefaultToolkit().getImage(getClass()
                .getResource("/resources/images/zero_gravity.png"));  
        
        mAbilityPanel2.setImage(image);
        mAbilityPanel2.setBottomText("Zero Gravity");
    }

    /** Sets the fourth ImagePanel's graphic to represent the clear-grid ability. 
     */
    private void setClearGridGraphic() {
        Image image = Toolkit.getDefaultToolkit().getImage(getClass()
                .getResource("/resources/images/clear_grid.png"));  
        
        mAbilityPanel3.setImage(image);
        mAbilityPanel3.setBottomText("Clear Grid");
    }

    /** Sets the welcome message on this Engine's score bar. 
     */
    private void setWelcomeMessage() {
        mScoreBar.setText("Welcome to Tetrocity! Press 'Space' to begin. Good luck!");
    }

    /** Sets the standard level and score message on this Engine's score bar. 
     */
    private void setLevelAndScoreMessage() {
        mScoreBar.setText("Level: " + mLevel + ", Score: " + (int) mPlayer.getScore());
    }

    /** Sets the paused message on this Engine's score bar. 
     */
    private void setPausedMessage() {
        mScoreBar.setText("PAUSED. Press 'Escape' to get back in the game!");
    }

    /** Sets the game over message on this Engine's score bar. 
     */
    private void setGameOverMessage() {
        String text = "GAME OVER! Level: " + mLevel + ", Score: " + (int) mPlayer.getScore() + ". ";
        
        if (mLevel < 10) {
            text += "Keep practicing!";
        } else if (mLevel < 20) {
            text += "Not bad!";
        } else if (mLevel < 30) {
            text += "Nice one!";
        } else if (mLevel < 40) {
            text += "Impressive!";
        } else if (mLevel < 50) {
            text += "Holy crap!";
        } else {
            text += "You are a god.";
        }
        mScoreBar.setText(text);
    }
    
    /****************************/
    /* Private utility classes. */
    /****************************/

    /** The LoopKeyListener class is used when processes need to halt until the player presses
     * a particular key. Internally, this class is a key listener that sets member boolean values
     * corresponding to the game state. When that boolean is switched, a loop elsewhere will break,
     * allowing the game to resume normal functions.
     * 
     *  A LoopKeyListener can be used for one of the 3 such states in a game of Tetrocity:
     * the welcome screen state, the paused state, and the halted state.
     * 
     * @author Nick Holt
     *
     */
    private class LoopKeyListener implements KeyListener {
        public static final int WELCOME = 0;
        public static final int PAUSED = 1;
        public static final int HALTED = 2;
        
        private int mState;

        public LoopKeyListener(int state) {
            mState = state;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();

            if (mState == WELCOME) {
                if (keyCode == KeyEvent.VK_6){ //debug / cheat code 
                        mIsWelcoming = false;
                        
                        setBoostGraphic();
                        mBoostUnlocked = true;
                        
                        setStraightLineGraphic();
                        mLinePieceAbilityUnlocked = true;
                        
                        setZeroGravityGraphic();
                        mZeroGravityUnlocked = true;
                        
                        setClearGridGraphic();
                        mClearGridUnlocked = true;
                } else if (keyCode == KeyEvent.VK_SPACE) {
                    if (mSoundEffectsEnabled) {
                        SoundEffect.BLIP.play();
                    }
                    
                    mIsWelcoming = false;
                } else if (keyCode == KeyEvent.VK_M) {
                    mSoundEffectsEnabled = !mSoundEffectsEnabled;
                }
            } else if (mState == PAUSED) {
                if (keyCode == KeyEvent.VK_ESCAPE) {
                    if (mSoundEffectsEnabled) {
                        SoundEffect.BLIP.play();
                    }
                    
                    mIsPaused = false;
                } else if (keyCode == KeyEvent.VK_R) {
                    if (mSoundEffectsEnabled) {
                        SoundEffect.BLIP.play();
                    }
                    
                    restart();
                } else if (keyCode == KeyEvent.VK_M) {
                    mSoundEffectsEnabled = !mSoundEffectsEnabled;
                }
            } else if (mState == HALTED) {
                if (keyCode == KeyEvent.VK_R){
                    if (mSoundEffectsEnabled) {
                        SoundEffect.BLIP.play();
                    }
                    
                    restart();
                } else if (keyCode == KeyEvent.VK_M) {
                    mSoundEffectsEnabled = !mSoundEffectsEnabled;
                }
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
     *   2) Has a live Tetrimino spacing of 26 (i.e. only 1 live Tetrimino active at a time)
     *   3) Has a live Tetrimino length-range of [3, 3] (i.e. only 3)
     *   4) Requires 2 cleared lines to progress to level 2. 
     * 
     * @author Nick Holt
     */
    private class GuidedLevelParameters {
        public static final int MAX_TETRIMINO_LENGTH = 6;
        public static final int MAX_LEVEL = 50;
        
        private float[][] mLevelParameters;

        public GuidedLevelParameters() {
            mLevelParameters = new float[MAX_LEVEL][5];
            mLevelParameters[0] = new float[]{1, 26, 3, 3, 2};
            mLevelParameters[1] = new float[]{1, 26, 3, 4, 2};
            mLevelParameters[2] = new float[]{1, 24, 3, 4, 2};
            mLevelParameters[3] = new float[]{2, 24, 3, 4, 3};
            mLevelParameters[4] = new float[]{2, 23, 3, 4, 3};
            mLevelParameters[5] = new float[]{3, 23, 3, 4, 3};
            mLevelParameters[6] = new float[]{3, 21, 3, 4, 4};
            mLevelParameters[7] = new float[]{3, 21, 3, 4, 4};
            mLevelParameters[8] = new float[]{4, 20, 3, 4, 4};
            mLevelParameters[9] = new float[]{4, 20, 3, 4, 4};
            
            mLevelParameters[10] = new float[]{2, 25, 3, 5, 5};
            mLevelParameters[11] = new float[]{2, 25, 3, 5, 5};
            mLevelParameters[12] = new float[]{2, 23, 3, 5, 5};
            mLevelParameters[13] = new float[]{3, 23, 3, 5, 6};
            mLevelParameters[14] = new float[]{3, 21, 3, 5, 6};
            mLevelParameters[15] = new float[]{4, 21, 3, 5, 6};
            mLevelParameters[16] = new float[]{4, 19, 3, 5, 7};
            mLevelParameters[17] = new float[]{4, 19, 3, 5, 7};
            mLevelParameters[18] = new float[]{5, 18, 3, 5, 7};
            mLevelParameters[19] = new float[]{5, 18, 3, 5, 7};
            
            mLevelParameters[20] = new float[]{3, 24, 4, 5, 8};
            mLevelParameters[21] = new float[]{3, 24, 4, 5, 8};
            mLevelParameters[22] = new float[]{3, 22, 4, 5, 8};
            mLevelParameters[23] = new float[]{4, 22, 4, 5, 9};
            mLevelParameters[24] = new float[]{4, 20, 4, 5, 9};
            mLevelParameters[25] = new float[]{5, 20, 4, 5, 9};
            mLevelParameters[26] = new float[]{5, 18, 4, 5, 10};
            mLevelParameters[27] = new float[]{5, 18, 4, 5, 10};
            mLevelParameters[28] = new float[]{6, 16, 4, 5, 10};
            mLevelParameters[29] = new float[]{6, 16, 4, 5, 10};
            
            mLevelParameters[30] = new float[]{4, 22, 5, 5, 11};
            mLevelParameters[31] = new float[]{4, 22, 5, 5, 11};
            mLevelParameters[32] = new float[]{4, 20, 5, 5, 11};
            mLevelParameters[33] = new float[]{5, 20, 5, 5, 11};
            mLevelParameters[34] = new float[]{5, 19, 5, 5, 11};
            mLevelParameters[35] = new float[]{6, 19, 5, 5, 12};
            mLevelParameters[36] = new float[]{6, 17, 5, 5, 12};
            mLevelParameters[37] = new float[]{6, 17, 5, 5, 12};
            mLevelParameters[38] = new float[]{7, 15, 5, 5, 12};
            mLevelParameters[39] = new float[]{7, 15, 5, 5, 12};
            
            mLevelParameters[40] = new float[]{5, 20, 6, 6, 13};
            mLevelParameters[41] = new float[]{5, 20, 6, 6, 13};
            mLevelParameters[42] = new float[]{5, 18, 6, 6, 13};
            mLevelParameters[43] = new float[]{6, 18, 6, 6, 13};
            mLevelParameters[44] = new float[]{6, 17, 6, 6, 13};
            mLevelParameters[45] = new float[]{7, 17, 6, 6, 14};
            mLevelParameters[46] = new float[]{7, 15, 6, 6, 14};
            mLevelParameters[47] = new float[]{7, 15, 6, 6, 14};
            mLevelParameters[48] = new float[]{8, 13, 6, 6, 14};
            mLevelParameters[49] = new float[]{8, 13, 6, 6, 14};
        }
        
        /**
         * @return The drop speed increase factor for a given level.
         */
        public float getLevelDropFactor(int level) {
            if (level <= MAX_LEVEL) {
                return mLevelParameters[level - 1][0];
            } else {
                return mLevelParameters[MAX_LEVEL - 1][0];
            }
        }
        
        /**
         * @return The spacing between live Tetrimino pieces for a given level.
         */
        public int getLevelLiveTetriminoSpacing(int level) {
            if (level <= MAX_LEVEL) {
                return (int) mLevelParameters[level - 1][1];
            } else {
                return (int) mLevelParameters[MAX_LEVEL - 1][1];
            }
        }
        
        /**
         * @return The range of lengths of live Tetrimino pieces for a given level.
         */
        public int[] getLevelLiveTetriminoLengthRange(int level) {
            if (level <= MAX_LEVEL) {
                return new int[]{(int) mLevelParameters[level - 1][2],
                        (int) mLevelParameters[level - 1][3]};
            } else {
                return new int[]{(int) mLevelParameters[MAX_LEVEL - 1][2],
                        (int) mLevelParameters[MAX_LEVEL - 1][3]};
            }
        }
        
        /**
         * @return The number of lines that need to be cleared before you can progress
         * to the next level.
         */
        public int getNextLevelLinesCleared(int level) {
            if (level < MAX_LEVEL) {
                return (int) mLevelParameters[level - 1][4];
            } else {
                return Integer.MAX_VALUE; //Can never progress past level 50
            }
        }
    }
    
    /** An ImagePanel is a simply JPanel component that contains an image and a JLabel only. Both
     * of these components can be set and updated by the appropriate methods.
     * The JLabel is placed under the drawn image.
     * 
     * @author Nick Holt
     */
    private class ImagePanel extends JPanel {
        private static final long serialVersionUID = 1L;
        /* The image to display. */
        Image mImage;
        /* The text to display above the image. */
        String mTopText;
        /* The text to display below the image. */
        String mBottomText;
        
        /** A new ImagePanel that contains the specified image. The text of this ImagePanel will
         * be the empty string.
         * 
         * @param image The image of this ImagePanel
         */
        public ImagePanel(Image image) {
            setImage(image);
            setTopText("");
            setBottomText("");
            
            setSize(imageWidth(), imageHeight());
            setVisible(true);
            repaint();
        }
        
        /************/
        /* Getters. */
        /************/
        
        /**
         * @return The height, in pixels, of the image.
         */
        public int imageHeight() {
            return mImage.getHeight(null);
        }
        
        /**
         * @return The width, in pixels, of the image.
         */
        public int imageWidth() {
            return mImage.getWidth(null);
        }
        
        /************/
        /* Setters. */
        /************/

        /**
         * @param image The image to display.
         */
        public void setImage(Image image) {
            mImage = image;
            repaint();
        }
        
        /**
         * @param text The text to display above the image.
         */
        public void setTopText(String text) {
            mTopText = text;
            repaint();
        }
        
        /**
         * @param text The text to display below the image. 
         */
        public void setBottomText(String text) {
            mBottomText = text;
            repaint();
        }
        
        /*********************/
        /* GUI manipulation. */
        /*********************/
        
        /** Paint the image, text, and borders.
         */
        public void paint(Graphics g) {
            super.paint(g);
            
            int[] imageDimensions = new int[]{mImage.getHeight(null), mImage.getWidth(null)};
            
            //Draw image
            g.drawImage(mImage, (int) (getWidth() - imageDimensions[1]) / 2,
                    (int) (getHeight() - imageDimensions[0]) / 2, null);
            
            //Draw centered text
            FontMetrics fm   = g.getFontMetrics(g.getFont());
            Rectangle2D rect = fm.getStringBounds(mTopText, g);
            
            g.drawString(mTopText, (int) (getWidth() - rect.getWidth()) / 2
                    , fm.getAscent() + 8);
            
            rect = fm.getStringBounds(mBottomText, g);

            g.drawString(mBottomText, (int) (getWidth()  - rect.getWidth())  / 2
                    , (int) (getHeight() - fm.getAscent()));

            //Draw borders
            g.setColor(Color.BLACK);
            g.drawLine(0, 0, 0, (int) getHeight() -1);
            g.drawLine(0, (int) getHeight() - 1,
                       (int) getWidth() - 1, (int) getHeight() - 1);
        }
    }
}