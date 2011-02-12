package mindstorm;

import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    public static void main(String[] args) throws IOException {
        try {

            //Init log
            LogWriter log = new LogWriter("log/K.log");
            
            //Load maze
            Maze m = new Maze().load("maze/test.txt");
            System.out.println(m.getMapRepresentation());

            //Solve it
            Pathfinder f = new AStarPathfinder(log);
            List<Point> path = f.solve(m);

            //Print the found path
            if(path != null) for(Point p : path){
                System.out.println(m.getMapRepresentation(p, '+'));
                Thread.sleep(500);
            }

            //Close log
            log.close();

            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            
        }
    }

}
