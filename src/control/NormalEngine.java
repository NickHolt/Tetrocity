package control;

import java.awt.event.KeyEvent;

import javax.swing.JPanel;

import model.Board;
import util.Direction;
import util.GameOverException;
import util.TetriminoFactory;

/** The fundamental controller for a game of Tetrocity. It is the job of the Engine to construct the
 * GUI, and facilitate the game through the use of timers and parsing player input via the Player class.
 * It is also the job of the Engine to interpret certain game events, and decide how the player score
 * should be updated as a result. 
 * 
 * 
 * @author Nick Holt
 *
 */
public class NormalEngine extends JPanel{
    private static final long serialVersionUID = 1L;
    public static final int LOGICAL_FPS = 60;
    public static final int VISUAL_FPS = 20;
    /* E.g. number of grid rows = max_length * ROW_RATIO */
    public static final float ROW_RATIO = 2.5f, COLUMN_RATIO = 5.0f;
    /* The initial Tetrimino drop speed. */
    public static final float INITIAL_DROP_SPEED = 1.0f; //blocks / second
    /* The initial number of live Tetriminoes. */
    public static final int INITIAL_LIVE_TETRIMINOES = 1; //blocks / second
    
    private int mLevel;
    private int mLiveTetriminoes;
    private float mDropSpeed;
    private Board mBoard;
    private TetriminoFactory mTetriminoFactory;
    private Player mPlayer;
    
    /** A new Engine for a game of Tetrocity. Instantiating an Engine will set the game parameters.
     */
    public NormalEngine(int[] gridDimensions, int[] tetriminoLengthRange, int seed) {
        mLevel = 1;
        mLiveTetriminoes = INITIAL_LIVE_TETRIMINOES;
        mDropSpeed = INITIAL_DROP_SPEED;
        mBoard = new Board((int) (gridDimensions[0] * ROW_RATIO), 
                (int) (gridDimensions[1] * COLUMN_RATIO),
                tetriminoLengthRange[1]);
        mTetriminoFactory = new TetriminoFactory(tetriminoLengthRange, seed);
        mPlayer = new Player(); //TODO
    }
    
    /** Begin the game of Tetrocity. The game will continue until either the JFrame is closed or 
     * a GameOverException is raised (to be changed, obviously).
     */
    public void begin() {
        //TODO PUT JRAME STUFF IN!
        float dropPeriod = 1 / mDropSpeed,
                logicPeriod = 1 / LOGICAL_FPS,
                visualPeriod = 1 / VISUAL_FPS;
        
        float dropTime, logicTime, visualTime; //How many seconds ago things were processed
        dropTime = logicTime = visualTime = System.currentTimeMillis() / 1000; //never processed
        while (true) {
            if (dropTime >= dropPeriod) {
                mBoard.shiftLiveTetriminoes(Direction.SOUTH);
                dropTime -= System.currentTimeMillis() / 1000;
            }
            if (logicTime >= logicPeriod) {
                interpretInput(mPlayer.getMoveKeyCode());
                updateScore(mBoard.clearRows()); //clear rows and update score
                
                while (mBoard.numLiveTetriminoes() < mLiveTetriminoes) { //should only loop once
                    try {
                        mBoard.putTetrimino();
                    } catch (GameOverException e) {
                        gameOver();
                    }
                }
                
                while (mBoard.queueTooSmall()) { //replenish Tetrimino queue
                    mBoard.enqueueTetrimino(mTetriminoFactory.getRandomTetrimino());
                }
                
                logicTime -= System.currentTimeMillis() / 1000;
            }
            if (visualTime >= visualPeriod) {
                //TODO
                visualTime -= System.currentTimeMillis() / 1000;
            }
        }
    }
    
    /** Interpret a KeyEvent keyCode and respond as dictated by the game rules.
     * 
     * @param keyCode The key code to interpret. 
     */
    public void interpretInput(int keyCode) {
        //TODO
        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_KP_UP) {
            mBoard.shiftLiveTetriminoes(Direction.NORTH);
        } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_KP_RIGHT) {
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
        }
    }

    /** Uses current game information to interpret the corresponding increase in score. 
     * 
     * @param linesCleared The number of lines cleared.
     * @return The amount of score to add. 
     */
    public long interpretScore(int linesCleared) {
        //TODO
        return linesCleared * linesCleared * mLevel;
    }
    
    /** Update the player's score given the number of lines cleared. 
     * 
     * @param linesCleared The number of lines cleared.
     */
    private void updateScore(int linesCleared) {
        mPlayer.addToScore(interpretScore(linesCleared));
    }
    
    private void pause() {
        //TODO
    }
    
    private void gameOver() {
        //TODO
    }
}
