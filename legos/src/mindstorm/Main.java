package mindstorm;

import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import pathfinder.AStarPathfinder;
import pathfinder.BFSPathfinder;
import pathfinder.DFSPathfinder;
import pathfinder.GreedyPathfinder;
import pathfinder.UCSPathfinder;

/**
 *
 * @author Ecky
 */
public class Main {
    private static LogWriter log;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        try {
            //Check args, exit if fail
            if(args.length < 2) {
                printUsage();
                return;
            }

            //Init log
            log = new LogWriter("log/K.log");
            
            //Load maze
            Maze m = new Maze().load(args[0]);
            log.log("maze " + args[0] + " loaded successfully");
            System.out.println(m.getMapRepresentation());

            //Solve it
            Pathfinder f = parseAlgoName(args[1]);
            if(f == null) {
                System.out.println("Unknown Algorithm : " + args[1]);
                printUsage();
                return;
            }
            List<Point> path = f.solve(m);

            //Exit if path not found
            if(path == null) return;

            //Convert the path to Robot readable format
            List<Character> converted = convertPath(path, 0);
            log.log("Path converted to " + converted);

            //Send it to robot

            //Close log
            log.close();

            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Pathfinder parseAlgoName(String name) {
        if(name.equalsIgnoreCase("AStar"))  return new AStarPathfinder(log);
        if(name.equalsIgnoreCase("BFS"))    return new BFSPathfinder(log);
        if(name.equalsIgnoreCase("DFS"))    return new DFSPathfinder();
        if(name.equalsIgnoreCase("Greedy")) return new GreedyPathfinder(log);
        if(name.equalsIgnoreCase("UCS"))    return new UCSPathfinder();

        return null;
    }

    private static void printUsage() {
        System.out.println("usage : Main <mazefile> <AStar|BFS|DFS|Greedy|UCS>");
    }

    /**
     * Convert path into Robot readable format, saved in array of byte.
     * The format :
     *  F = Forward
     *  R = Rotate 90 deg right
     *  L = Rotate 90 deg left
     *  B = Backward
     * @param path
     * @param initDir   N=0, E=1, S=2, W=3
     * @return
     */
    public static List<Character> convertPath(List<Point> path, int initDir) {
        //Vars
        List<Character> ret  = new ArrayList<Character>();
        int curDir      = initDir;

        //Constants
        char FORWARD    = 'F';
        char TURN_RIGHT = 'R';
        char TURN_LEFT  = 'L';
        char BACKWARD   = 'B';

        for(int i = 0; i < path.size()-1; ++i) {
            //Make sure curdir is right
            curDir = curDir < 0 ? curDir + 4 : curDir;
            curDir = curDir % 4;

            //determine movement
            int dx = path.get(i+1).x - path.get(i).x;
            int dy = path.get(i+1).y - path.get(i).y;

            if(dy < 0) {
                switch(curDir) {
                    case 0 : ret.add(FORWARD); break;
                    case 1 : ret.add(TURN_LEFT); --curDir; ret.add(FORWARD); break;
                    case 2 : ret.add(BACKWARD); break;
                    case 3 : ret.add(TURN_RIGHT); ++curDir; ret.add(FORWARD); break;
                }
            }
            else if(dy > 0) {
                switch(curDir) {
                    case 0 : ret.add(BACKWARD); break;
                    case 1 : ret.add(TURN_RIGHT); ++curDir; ret.add(FORWARD); break;
                    case 2 : ret.add(FORWARD); break;
                    case 3 : ret.add(TURN_LEFT); --curDir; ret.add(FORWARD); break;
                }
            }
            else if(dx > 0) {
                switch(curDir) {
                    case 0 : ret.add(TURN_RIGHT); ++curDir; ret.add(FORWARD); break;
                    case 1 : ret.add(FORWARD); break;
                    case 2 : ret.add(TURN_LEFT); --curDir; ret.add(FORWARD); break;
                    case 3 : ret.add(BACKWARD); break;
                }
            }
            else if(dx < 0) {
                switch(curDir) {
                    case 0 : ret.add(TURN_LEFT); --curDir; ret.add(FORWARD); break;
                    case 1 : ret.add(BACKWARD); break;
                    case 2 : ret.add(TURN_RIGHT); ++curDir; ret.add(FORWARD); break;
                    case 3 : ret.add(FORWARD); break;
                }
            }
        }
        return ret;
    }
}
