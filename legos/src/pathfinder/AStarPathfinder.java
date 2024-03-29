package pathfinder;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import mindstorm.LogWriter;
import mindstorm.Maze;
import mindstorm.Pathfinder;

/**
 * Maze solver algorithm using AStar
 * @author Ecky
 */
public class AStarPathfinder implements Pathfinder {
    private AStarNode       _startPoint;
    private AStarNode       _endPoint;
    private List<AStarNode> _tour;
    private List<AStarNode> _closed;
    private List<AStarNode> _open;
    private List<Point>     _completePath;
    private AStarNode       _curPoint;
    private Maze            _maze;
    private LogWriter       _log;

    public AStarPathfinder(LogWriter log) {
        _log = log;
    }

    public List<Point> solve(Maze maze) {
        //Logging
        _log.log("Init A* search");
        long startTime = System.currentTimeMillis();

        ///Init vars
        _maze           = maze;
        _startPoint     = new AStarNode(maze.getEnterPos());
        _curPoint       = _startPoint;
        _endPoint       = new AStarNode(maze.getExitPos());
        _tour           = new LinkedList<AStarNode>();
        _completePath   = new LinkedList<Point>();
        for(Point p : maze.getButtons()) _tour.add(new AStarNode(p));

        //Log
        _log.log("Starting node : " + _startPoint);

        //Calculating Fn
        _endPoint.calulateF(_startPoint, _endPoint);
        _startPoint.calulateF(_startPoint, _endPoint);

        //Init tour
        initTour();

        //Reset
        reset();

        while(true) {

            //Get candidates
            getNewCandidates();

            //Log
            _log.log("Open list : " + _open);

            //Exit if there is no candidate
            if(_open.isEmpty()) break;

            //Select candidate and process it
            processNode(selectCandidate());

            //Exit if tour has completed
            if(_tour.isEmpty()) break;
        }

        //Result to log
        _log.log(
                "A* " +
                (_tour.isEmpty() ? "succeeded" : "failed") +
                ". Total time " +
                Long.toString(System.currentTimeMillis() - startTime) + "ms (Including logging time)"
        );
        String path = "";
        for(Point p : _completePath) path += ">>(" + p.x + "," + p.y + ")";
        _log.log("Complete path : " + path);

        //Return found path if tour is finished, else return null
        return _tour.isEmpty() ? _completePath : null;
    }

    private void initTour() {
        _tour.remove(_endPoint);
        _tour = getShortestTour();
        _tour.add(_endPoint);

        //Log
        String tour = "";
        for(AStarNode node : _tour) tour += ">>" + node.toString();
        _log.log("Planned tour : " + tour);
    }

    //Using greedy to get the shortest tour
    private List<AStarNode> getShortestTour() {
        for(AStarNode node : _tour) node.calulateF(_startPoint, node);
        Collections.sort(_tour);
        return _tour;
    }

    private void reset() {
        //Reset vars
        _closed = new LinkedList<AStarNode>();
        _open   = new LinkedList<AStarNode>();
        _closed.add(_curPoint);
    }

    private void processNode(AStarNode candidate) {
        char c = _maze.getCell(candidate.x, candidate.y);

        //Check whether the selected node is Button
        if(c == Maze.BUTTON) {
            //Search the button in tour
            AStarNode btn = null;
            for(AStarNode p : _tour) {
                if(p.isEqual(candidate)) {
                    btn = p;
                    break;
                }
            }
            //Remove it from tour, save the path to reach the tour, reset
            if(btn != null) {
                _tour.remove(btn);
                _completePath.addAll(candidate.createPathFromOrigin());
                reset();
                _curPoint   = new AStarNode(_curPoint);

                _log.log("Stepped on BUTTON " + candidate.toString());

                initTour();
            }
        }

        //If it is an Exit
        else if(c == Maze.EXIT && _tour.size() == 1) {
            _completePath.addAll(candidate.createPathFromOrigin());
            _tour.remove(candidate);

            _log.log("Stepped on EXIT " + candidate.toString());
            
        }
    }

    private AStarNode selectCandidate() {
        Collections.sort(_open);
        AStarNode selected = _open.remove(0);
        _closed.add(selected);
        _log.log("Moving to : " + selected.toString());
        return _curPoint = selected;
    }

    private void getNewCandidates() {
        List<String> candidates = new ArrayList<String>();

        for(int x = -1; x <= 1; ++x) {
            for(int y = -1; y <= 1; ++y) {
                //Continue if invalid point
                if(Math.abs(x + y) != 1) continue;
                if(!isValidCandidate(_curPoint.x + x, _curPoint.y + y)) continue;

                //Create the node
                AStarNode node = new AStarNode(_curPoint.x + x, _curPoint.y + y);

                //If a button found, set the Fn to minimum so the node will be
                //more likely to be chosen next
                if(_maze.getCell(node.x, node.y) != Maze.BUTTON) {
                    node.calulateF(_startPoint, _tour.get(0));
                    candidates.add(node.toString() + " f = " + node.getF());
                }
                else {
                    node.calulateF(node, node);
                    candidates.add(node.toString() + " f = " + node.getF() + " <BUTTON!>");
                }

                //Set the parent and add it as a candidate
                node.parent = _curPoint;
                _open.add(node);
            }
        }

        _log.log("New node candidates : " + candidates);
    }

    private boolean isValidCandidate(int x, int y) {
        if(!_maze.isWalkable(x, y)) return false;
        for(Point p : _closed) if(p.x == x && p.y == y) return false;
        for(Point p : _open) if(p.x == x && p.y == y) return false;

        return true;
    }



    
    private class AStarNode extends Point implements Comparable<AStarNode> {

        private int f = 0;
        public AStarNode parent = null;

        public AStarNode(Point enterPos) {
            x = enterPos.x;
            y = enterPos.y;
        }

        public AStarNode(int x, int y) {
            super(x, y);
        }
        
        private int getManhattanDistance(AStarNode from) {
            return Math.abs(from.x - this.x) + Math.abs(from.y - this.y);
        }

        public int calulateF(AStarNode startNode, AStarNode endNode) {
            f = getManhattanDistance(startNode) + getManhattanDistance(endNode);
            return f;
        }

        public boolean isEqual(Point p) {
            return x == p.x && y == p.y;
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }

        public int getF() {
            return f;
        }

        public int compareTo(AStarNode o) {
            if(this.getF() < o.getF()) return -1;
            else if(this.getF() > o.getF()) return 1;
            else return 0;
        }

        public List<Point> createPathFromOrigin() {
            List<Point> path = new LinkedList<Point>();
            AStarNode pp = this;
            while(pp != null) {
                path.add(0,new Point(pp));
                pp = pp.parent;
            }
            return path;
        }
    }
}
