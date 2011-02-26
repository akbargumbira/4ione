
import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mindstorm.LogWriter;
import mindstorm.Maze;
import mindstorm.Pathfinder;
import pathfinder.AStarPathfinder;
import pathfinder.BFSPathfinder;
import pathfinder.DFSPathfinder;
import pathfinder.GreedyPathfinder;
import pathfinder.UCSPathfinder;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author user
 */
public class Debug {

    public static void main(String[] args) throws IOException {
        try {
            //  // Input Algorithm
//            System.out.println("Choose Algorithm");
//            System.out.println("1. BFS");
//            System.out.println("2. DFS");
//            System.out.println("3. UCS");
//            System.out.println("4. Best FS");
//            System.out.println("5. A*");
//            System.out.println("0. Exit");
//
//            System.out.print("Input : ");

            //Init log
            LogWriter  log = new LogWriter("log/K.log");

            //Load maze
            Maze m = new Maze().load("maze/test2.txt");
            System.out.println(m.getMapRepresentation());

            //Solve it
            Pathfinder f = new BFSPathfinder(log);
//            Pathfinder f = new DFSPathfinder(log);
//            Pathfinder f = new UCSPathfinder();
//            Pathfinder f = new GreedyPathfinder(log);
//            Pathfinder f = new AStarPathfinder(log);
            List<Point> path = f.solve(m);

            //Print the found path
            if(path != null) for(Point p : path){
                System.out.println(m.getMapRepresentation(p, '+'));
                Thread.sleep(500);
            }


        } catch (FileNotFoundException ex) {
            Logger.getLogger(Debug.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {

        }
    }
}
