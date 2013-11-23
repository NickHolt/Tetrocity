package control;

/** A game of Tetrocity. This object provides an interface for parsing command-line input, which
 * can then be interpreted into the relevant parameters required by {@link NormalEngine}. 
 * 
 * @author Nick Holt
 *
 */
public class Tetrocity {
    /* The recognized command-line arguments. */
    public static final String options = "--lowerLengthBound= --upperLengthBound --rules --controls "
            + "--default";

    /** This method is used to construct the relevant game parameters. As of this build,
     * the player may only specify the range of Tetrimino lengths. All other parameters will be
     * generated automatically. 
     * 
     * @param args The lower and upper Tetrimino length bounds, separated by whitespace. 
     */
    public static void main(String[] args) {
    }

}
