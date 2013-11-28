package control;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.ArrayBlockingQueue;

/** The player in a game of Tetrocity. The main role of the player class is provide
 * a convenient interface for the Engine to process input. It is the player's class duty
 * to accept all keyboard input and, if possible, convert it to a recognizable control
 * via {@link Control}.
 * 
 * A player also keeps track of their score. 
 * 
 * @author Nick Holt
 */
public class Player implements KeyListener {
    public static final int MAX_MOVES = 3;
    private ArrayBlockingQueue<Integer> mMoveKeyCodes;
    
    private double mScore;
    
    /** A new Player for a game of Tetrocity, who is capable of presenting at most
     * {@link Player#MAX_MOVES} moves at a time, with a score of 0.
     */
    public Player() {
        mMoveKeyCodes = new ArrayBlockingQueue<Integer>(MAX_MOVES);
        mScore = 0;        
    }

    @Override
    public void keyPressed(KeyEvent e) {
        mMoveKeyCodes.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
    
    /**
     * @return This player's score.
     */
    public double getScore() {
        return mScore;
    }
    
    /** 
     * @param toAdd The amount to add to this player's score. 
     */
    public void addToScore(double toAdd) {
        mScore += toAdd;
    }
    
    /** Reset this player's score.
     */
    public void resetScore() {
        mScore = 0;
    }
    
    /**
     * @return The first move key code in the Player's move Queue. Returns
     * {@link KeyEvent#KEY_LOCATION_UNKNOWN} if there is no move to return. 
     */
    public int getMoveKeyCode() {
        if (mMoveKeyCodes.size() == 0) {
            return KeyEvent.KEY_LOCATION_UNKNOWN;
        }
        
        return mMoveKeyCodes.poll();
    }
}
