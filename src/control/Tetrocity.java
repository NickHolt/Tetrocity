package control;


/** A game of Tetrocity. This object serves as an entry point to run the game, and 
 * simply creates a new {@link Engine} and runs it within its main method.
 * 
 * @author Nick Holt
 */
public class Tetrocity {
    public static void main(String[] args) {
        new Engine().run();
    }
}
