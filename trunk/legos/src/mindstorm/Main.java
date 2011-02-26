package mindstorm;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import josx.rcxcomm.RCXPort;
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
            //Init log
            log = new LogWriter("log/K.log");

            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String s;
            while(true) {
                //Read user input
                System.out.print(">");
                s = in.readLine();

                //Exit if s is empty
                if(s.isEmpty()) break;

                //Parse user input
                List<Character> toSend = parseUserInput(s);
                if(toSend == null) continue;

                //Send to robot
                sendToRobot(toSend);
            }

            //Close log
            log.close();

            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Print program usage
     */
    private static void printUsage() {
        System.out.println("[USAGE] ----------------------------------- ");
        System.out.println("Solve Maze : ");
        System.out.println("sm <maze file> <astar|bfs|dfs|greedy|ucs>");
        System.out.println("Use any combination of these to set robot :");
        System.out.println("ml <number>  -- Set robot's left motor power");
        System.out.println("mr <number>  -- Set robot's left motor power");
        System.out.println("rl <number>  -- Set rotate 90deg left time");
        System.out.println("rr <number>  -- Set rotate 90deg right time");
        System.out.println("ff <number>  -- Set forward one cell time");
        System.out.println("[USAGE] ----------------------------------- ");
    }

    /**
     * Returns list of character that is ready to be sent to robot.
     * Those characters contains no space
     * @param userInput
     * @return
     */
    private static List<Character> parseUserInput(String userInput) {
        //Vars
        List<Character> comm = null;

        //Solve maze and send it to robot
        if(userInput.matches("^sm\\s+\\S+\\s+(astar|bfs|dfs|greedy|ucs)$")) {
            String ss[] = userInput.split("\\s+");
            comm        = solveMaze(ss[1], ss[2]);
            if(comm != null) {
                comm.add(0, 'm');
                comm.add(0, 's');
            }
        }
        
        //Robot settings :
        // The regex only matches, with or without spaces, in or not in line :
        // ml <number>
        // mr <number>
        // rl <number>
        // rr <number>
        // ff <number>
        // Ex :
        //  ml 7 mr 6 rl 4000 rr 4000 ff 1200
        //  ml 7 rl 400
        else if(userInput.matches("^((ml|mr|rl|rr|ff)\\s*\\d+\\s*)+$")) {
            String ss   = userInput.replaceAll("\\s+", "");
            char ch[]   = ss.toCharArray();
            comm        = new ArrayList<Character>();
            for(char c : ch) comm.add(c);
        }

        //Invalid input, show the usage
        else printUsage();

        //Return
        return comm;
    }

    /**
     * Solve the maze and return array of char (command) for robot to execute
     * @param mazeName
     * @param algoName
     * @return
     */
    private static List<Character> solveMaze(String mazeName, String algoName) {
        try {

            //Load maze
            Maze m = new Maze().load(mazeName);
            log.log("maze " + mazeName + " loaded successfully");
            System.out.println(m.getMapRepresentation());

            //Solve it
            Pathfinder f = parseAlgoName(algoName);
            if (f == null) {
                System.out.println("Unknown Algorithm : " + algoName);
                printUsage();
                return null;
            }
            List<Point> path = f.solve(m);
            
            //Exit if path not found
            if (path == null) return null;

            //Return the list of character (convertedpath)
            List<Character> converted = convertPath(path, 0);
            log.log("Path converted to " + converted);
            return converted;
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static Pathfinder parseAlgoName(String name) {
        if(name.equalsIgnoreCase("AStar"))  return new AStarPathfinder(log);
        if(name.equalsIgnoreCase("BFS"))    return new BFSPathfinder(log);
        //if(name.equalsIgnoreCase("DFS"))    return new DFSPathfinder();
        if(name.equalsIgnoreCase("Greedy")) return new GreedyPathfinder(log);
        if(name.equalsIgnoreCase("UCS"))    return new UCSPathfinder();

        return null;
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
                    case 2 : ret.add(BACKWARD); ret.add(FORWARD); curDir += 2; break;
                    case 3 : ret.add(TURN_RIGHT); ++curDir; ret.add(FORWARD); break;
                }
            }
            else if(dy > 0) {
                switch(curDir) {
                    case 0 : ret.add(BACKWARD); ret.add(FORWARD); curDir += 2; break;
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
                    case 3 : ret.add(BACKWARD); ret.add(FORWARD); curDir += 2; break;
                }
            }
            else if(dx < 0) {
                switch(curDir) {
                    case 0 : ret.add(TURN_LEFT); --curDir; ret.add(FORWARD); break;
                    case 1 : ret.add(BACKWARD); ret.add(FORWARD); curDir += 2; break;
                    case 2 : ret.add(TURN_RIGHT); ++curDir; ret.add(FORWARD); break;
                    case 3 : ret.add(FORWARD); break;
                }
            }
        }
        return ret;
    }

    /**
     * Send the list of character to robot
     * @param toSend
     */
    private static void sendToRobot(List<Character> toSend) {
        try {
            RCXPort rp = new RCXPort("usb");
            OutputStream out = rp.getOutputStream();

            for (int i=0; i<toSend.size(); ++i)
                out.write(toSend.get(i));

            out.write(' ');
            out.close();
            rp.close();
        } catch (IOException ex) {
            //
        }
        
    }
}
