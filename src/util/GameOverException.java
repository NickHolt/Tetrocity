package util;

/** An exception indicated the end of game state has been reached. This state is achieved
 * when a new Tetrimino cannot be placed in the usual manner without colliding with an already
 * present Tetrimino.
 * 
 * @author Nick Holt
 *
 */
public class GameOverException extends Exception {
    private static final long serialVersionUID = 1L;

    public GameOverException() {
    }

    public GameOverException(String arg0) {
        super(arg0);
    }
}
