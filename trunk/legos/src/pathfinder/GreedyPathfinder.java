package pathfinder;

import java.awt.Point;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import mindstorm.LogWriter;
import mindstorm.Maze;
import mindstorm.Pathfinder;

/**
 *
 * @author Rezan Achmad
 */
public class GreedyPathfinder implements Pathfinder {
    private Node        _startPoint;
    private Node        _endPoint;
    private List<Node>  _tour;
    private List<Node>  _closed;
    private List<Node>  _open;
    private List<Point> _completePath;
    private Node        _curPoint;
    private Maze        _maze;
    private LogWriter   _log;

    public GreedyPathfinder(LogWriter log) {
        _log = log;
    }

    public List<Point> solve(Maze maze) {

        //Logging
        _log.log("Init Best-first search");
        long startTime = System.currentTimeMillis();

        ///Init vars
        _maze           = maze;
        _startPoint     = new Node(maze.getEnterPos());
        _curPoint       = _startPoint;
        _endPoint       = new Node(maze.getExitPos());
        _tour           = new LinkedList<Node>();
        _completePath   = new LinkedList<Point>();
        for(Point p : maze.getButtons()) _tour.add(new Node(p));

        //Calculating Fn
        _endPoint.calculateF(_endPoint);
        _startPoint.calculateF(_endPoint);

        //Init tour
        initTour();

        //Reset
        reset();

        while(true) {
            //Get candidates
            getNewCandidates();

            //Exit if there is no candidate
            if(_open.isEmpty()) break;

            //Select candidate and process it
            processNode(selectCandidate());

            //Exit if tour has completed
            if(_tour.isEmpty()) break;
        }

        //Result to log
        _log.log(
                "Best-first " +
                (_tour.isEmpty() ? "succeeded" : "failed") +
                ". Total time " +
                Long.toString(System.currentTimeMillis() - startTime) + "ms"
        );

        //Return found path if tour is finished, else return null
        return _tour.isEmpty() ? _completePath : null;
    }

    private void initTour() {
        _tour.remove(_endPoint);
        for (Node node : _tour) node.calculateF(_startPoint);
        Collections.sort(_tour);
        _tour.add(_endPoint);
    }

    private void reset() {
        //Reset vars
        _closed = new LinkedList<Node>();
        _open   = new LinkedList<Node>();
    }

    private void processNode(Node candidate) {
        char c = _maze.getCell(candidate.x, candidate.y);

        //Check whether the selected node is Button
        if(c == Maze.BUTTON) {
            //Search the button in tour
            Node btn = null;
            for(Node p : _tour) {
                if(p.x == candidate.x && p.y == candidate.y) {
                    btn = p;
                    break;
                }
            }
            //Remove it from tour, save the path to reach the tour, reset
            if(btn != null) {
                _tour.remove(btn);
                _completePath.addAll(candidate.createPathFromOrigin());
                reset();
                _curPoint   = new Node(_curPoint);
                initTour();
            }
        }

        //If it is an Exit
        else if(c == Maze.EXIT && _tour.size() == 1) {
            _completePath.addAll(candidate.createPathFromOrigin());
            _tour.remove(candidate);
        }
    }

    private Node selectCandidate() {
        Collections.sort(_open);
        Node selected = _open.remove(0);
        _closed.add(selected);
        return _curPoint = selected;
    }

    private void getNewCandidates() {
        for(int x = -1; x <= 1; ++x) {
            for(int y = -1; y <= 1; ++y) {
                //Continue if invalid point
                if(Math.abs(x + y) != 1) continue;
                if(!isValidCandidate(_curPoint.x + x, _curPoint.y + y)) continue;

                //Create the node
                Node node = new Node(_curPoint.x + x, _curPoint.y + y);

                //If a button found, set the Fn to minimum so the node will be
                //more likely to be chosen next
                if(_maze.getCell(node.x, node.y) != Maze.BUTTON)
                    node.calculateF(_tour.get(0));
                else
                    node.calculateF(node);

                //Set the parent and add it as a candidate
                node.parent = _curPoint;
                _open.add(node);
            }
        }
    }

    private boolean isValidCandidate(int x, int y) {
        if(!_maze.isWalkable(x, y)) return false;
        for(Point p : _closed) if(p.x == x && p.y == y) return false;
        for(Point p : _open) if(p.x == x && p.y == y) return false;

        return true;
    }

    private class Node extends Point implements Comparable<Node> {

        private int f = 0;
        public Node parent = null;

        public Node(Point enterPos) {
            x = enterPos.x;
            y = enterPos.y;
        }

        public Node(int x, int y) {
            super(x, y);
        }

        private int calculateF(Node from) {
            return f = Math.abs(from.x - this.x) + Math.abs(from.y - this.y);
        }

        public int getF() {
            return f;
        }

        public int compareTo(Node o) {
            if(this.getF() < o.getF()) return -1;
            else if(this.getF() > o.getF()) return 1;
            else return 0;
        }

        public List<Point> createPathFromOrigin() {
            List<Point> path = new LinkedList<Point>();
            Node pp = this;
            while(pp != null) {
                path.add(0,new Point(pp));
                pp = pp.parent;
            }
            return path;
        }
    }
}
