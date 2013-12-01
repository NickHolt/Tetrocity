package testing;

/** A master controller for all testing classes. The main method runs
 * every testing class' printAll() method.
 * 
 * @author Nick Holt
 *
 */
public class Main {

    /** Runs all testing class' runAll() method. Comment out
     * class runs as necessary. 
     * 
     * @param args null. 
     */
    public static void main(String[] args) {
        MatricesTest matricesTest = new MatricesTest(); matricesTest.printFails();
        ShapeTest shapeTest = new ShapeTest(); shapeTest.printFails();
        TetriminoTest tetriminoTest = new TetriminoTest(); tetriminoTest.printFails();
        System.out.println("Testing completed.");
    }

}
