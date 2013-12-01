package testing;

/** A class used for managing debugging outputs. Any debug print statement 
 * of debug level greater than or equal that value will be printed to System.out in 
 * the form: "Debug (DEBUG_LEVEL): MESSAGE". Clearly, the debug level should be set 
 * before the use of this class. 
 * 
 *  Suggested debugging level logic:
 *    0 = quiet. No debugging output. 
 *    1 = basic. Output of basic to intermediate program functions. 
 *    E.g. object creation and critical data processing.
 *    2 = verbose. Output of the majority of debugging statements.
 *    E.g. important method calls and low-level data processing. 
 *    3 = total. Output of ALL debugging statements.
 *    E.g. Tested utility method calls and bug-specific output. 
 * 
 * @author Nick Holt
 *
 */
public class Debug {
    private static int mDebugLevel;
    
    public static void print(int debugLevel, String message) {
        if (debugLevel <= mDebugLevel) {
            System.out.println("Debug (" + debugLevel + "): " + message );
        }
    }
    
    /**
     * @return the mDebugLevel
     */
    public static int getDebugLevel() {
        return mDebugLevel;
    }

    /**
     * @param mDebugLevel the mDebugLevel to set
     */
    public static void setDebugLevel(int debugLevel) {
        mDebugLevel = debugLevel;
    }

}
