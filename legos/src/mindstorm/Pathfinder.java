/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mindstorm;

import java.awt.Point;
import java.util.List;

/**
 * This class served as interface for all maze solver.
 * Any class that is going to solve the maze should implement this interface.
 * @author Ecky
 */
public interface Pathfinder {
    /**
     * solve the maze
     * @param maze
     * @return returns list of point, null if maze is unsolvable
     */
    public List<Point> solve(Maze maze);
}
