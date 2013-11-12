package model;

/** A game board in a game of Tetrocity. A Board knows only of the Tetrimino
 * pieces currently in play. This includes the dead Tetriminoes and the live
 * Tetrimino. Board dimensions must be provided on instantiation. 
 * 
 *  When prompted to do so, a Board is capable of examining its state and reacting
 * appropriately. For example, if a Game calls update() on the Board, it will
 * scan for filled rows and request each Tetrimino occupying that row to delete
 * the appropriate Blocks. The Board will also determine which Tetriminoes are
 * eligible to be dropped one coordinate space. 
 * 
 * @author Nick Holt
 *
 */
public class Board {

}
