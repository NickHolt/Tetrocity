package control;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.ArrayBlockingQueue;

/** The player in a game of Tetrocity. The main role of the player class is provide
 * a convenient interface for the Engine to process input. It is the player's class duty
 * to accept all keyboard input and, if possible, convert it to a recognizable control
 * via {@link Control}.
 * 
 * A player also keeps track of their score in a game of Tetrocity. 
 * 
 * @author Nick Holt
 *
 */
public class Player implements KeyListener {
    public static final int MAX_MOVES = 3;
    private ArrayBlockingQueue<Integer> mMoveKeyCodes;
    
    private long mScore;
    
    public Player() {
        mMoveKeyCodes = new ArrayBlockingQueue<Integer>(MAX_MOVES);
        mScore = 0;        
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // TODO Auto-generated method stub
        mMoveKeyCodes.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Do nothing?
    }
    
    /**
     * @return This player's score.
     */
    public long getScore() {
        return mScore;
    }
    
    /** 
     * @param toAdd The amount to add to this player's score. 
     */
    public void addToScore(long toAdd) {
        mScore += toAdd;
    }
    
    /** Reset this player's score.
     */
    public void resetScore() {
        mScore = 0;
    }
    
    /**
     * @return The first move key code in the Player's move Queue. -1 if there is no move to return.
     */
    public int getMoveKeyCode() {
        if (mMoveKeyCodes.size() == 0) {
            return -1;
        }
        
        return mMoveKeyCodes.poll();
    }
}
