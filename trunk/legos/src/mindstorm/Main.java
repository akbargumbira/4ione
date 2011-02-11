/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mindstorm;

import java.awt.Point;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import pathfinder.AStarPathfinder;

/**
 *
 * @author Ecky
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {

            
            //Load maze
            Maze m = new Maze().load("maze/test.txt");
            System.out.println(m.getMapRepresentation());

            //Solve it
            Pathfinder f = new AStarPathfinder();
            List<Point> path = f.solve(m);

            //Print the found path
            if(path != null) for(Point p : path){
                System.out.println(m.getMapRepresentation(p, '+'));
                Thread.sleep(500);
            }

            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            
        }
    }

}
