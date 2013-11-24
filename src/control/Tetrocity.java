package control;

import util.CommandArgs;

/** A game of Tetrocity. This object provides an interface for parsing command-line input, which
 * can then be interpreted into the relevant parameters required by {@link NormalEngine}. 
 * 
 * @author Nick Holt
 *
 */
public class Tetrocity {
    /* The recognized command-line arguments. */
    public static final String OPTIONS = "--lowerLengthBound= --upperLengthBound --rules --controls "
            + "--normal";

    /** This method is used to construct the relevant game parameters. As of this build,
     * the player may only specify the range of Tetrimino lengths. All other parameters will be
     * generated automatically. 
     * 
     * @param args The lower and upper Tetrimino length bounds, separated by whitespace. 
     */
    public static void main(String[] args) {
        CommandArgs cArgs = new CommandArgs(OPTIONS, args);
        if (!cArgs.ok()) {
            usage(); //Print usage instructions if input was not correct.
        }
        
        int lowerBound = -1, upperBound = -1;
        boolean gameIsNormal = false;
        
        /* Process command line arguments. */
        if (cArgs.containsKey("--rules")) {
            rules();
        }
        if (cArgs.containsKey("--controls")) {
            controls();
        }
        if (cArgs.containsKey("--normal")) {
            gameIsNormal = true;
        }
        if (!cArgs.containsKey("--lowerLengthBound=") || !cArgs.containsKey("--upperLengthBound=")) {
            usage(); //these are required parameters
        } else {
            lowerBound = cArgs.getInt("--lowerLengthBound");
            upperBound = cArgs.getInt("--upperLengthBound");
        }
        
        if (gameIsNormal) {
            NormalEngine normalEngine = new NormalEngine(new int[]{lowerBound, upperBound},
                    (int) System.currentTimeMillis());
            
            normalEngine.begin();
        }
    }
    
    public static void usage() {
        String usage = " Tetrocity is initiated from the command line with 5 options, 2 of which are "
                + "required. These options are:\n"
                + "* --lowerLengthBound=L. A required field giving the smallest length "
                + "Tetrimino pieces may be.\n"
                + "* --upperLengthBound=U. A required field giving the largest length "
                + "Tetrimino pieces may be.\n"
                + "* --rules. An optional field that prints the game rules.\n"
                + "* --controls. An optional field that prints the game controls.\n"
                + "* --normal. An optional field that specifies the game mode is normal.\n";
        
        System.out.println(usage);
        }
    
    public static void rules() {
        String rules = " Tetrocity is at heart, a Tetris clone with a slew of cool new features."
                + " Ever play Tetris? If yes: congratulations! You know how to play to Tetrocity."
                + " Head on into the game and wreak some havok. If not: here's the rules:\n"
                + "* You control a number (usually just one) of geometric pieces called Tetriminoes.\n"
                + "* Your goal is to stack these Tetrimineoes in the most efficient way possible, "
                + "and fill as many rows as you can.\n"
                + "* The more rows you clear at once, the more points you get!\n"
                + " That's the jist of it. Make sure to check out the controls too. Have fun!";
        
        System.out.println(rules);
    }
    
    public static void controls() {
        String controls = "-CONTROLS-\n"
                + "UP/RIGHT/DOWN/LEFT ARROW KEYS : Shift Tetrimino\n"
                + "Z : Rotate Tetrimino CounterClockwise\n"
                + "C : Rotate Tetrimino Clockwise\n"
                + "SHIFT : Store Tetrimino"
                + "ESCAPE : Pause game / access menu.";
        
        System.out.println(controls);
    }
}
