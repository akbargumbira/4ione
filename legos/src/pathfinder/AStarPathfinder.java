/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pathfinder;

import java.awt.Point;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import mindstorm.Maze;
import mindstorm.Pathfinder;

/**
 * Maze solver algorithm using AStar
 * @author Ecky
 */
public class AStarPathfinder implements Pathfinder {
    private AStarNode _startPoint;
    private AStarNode _endPoint;
    private List<AStarNode> _tour;
    private List<AStarNode> _closed;
    private List<AStarNode> _open;
    private List<Point> _completePath;
    private AStarNode _curPoint;
    private Maze _maze;

    public List<Point> solve(Maze maze) {

        ///Init vars
        _maze           = maze;
        _startPoint     = new AStarNode(maze.getEnterPos());
        _curPoint       = _startPoint;
        _endPoint       = new AStarNode(maze.getExitPos());
        _tour           = new LinkedList<AStarNode>();
        _completePath   = new LinkedList<Point>();
        for(Point p : maze.getButtons()) _tour.add(new AStarNode(p));

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

            //Exit if there is no candidate
            if(_open.isEmpty()) break;

            //Select candidate and process it
            processNode(selectCandidate());

            //Exit if tour has completed
            if(_tour.isEmpty()) break;
        }

        //Return found path if tour is finished, else return null
        return _tour.isEmpty() ? _completePath : null;
    }

    private void initTour() {
        _tour.remove(_endPoint);
        for (AStarNode node : _tour) node.calulateF(_startPoint, _endPoint);
        Collections.sort(_tour);
        _tour.add(_endPoint);
    }

    private void reset() {
        //Reset vars
        _closed = new LinkedList<AStarNode>();
        _open   = new LinkedList<AStarNode>();
    }

    private void processNode(AStarNode candidate) {
        char c = _maze.getCell(candidate.x, candidate.y);

        //Check whether the selected node is Button
        if(c == Maze.BUTTON) {
            //Search the button in tour
            AStarNode btn = null;
            for(AStarNode p : _tour) {
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
                _curPoint   = new AStarNode(_curPoint);
                _startPoint = _curPoint;
                initTour();
            }
        }

        //If it is an Exit
        else if(c == Maze.EXIT && _tour.size() == 1) {
            _completePath.addAll(candidate.createPathFromOrigin());
            _tour.remove(candidate);
        }
    }

    private AStarNode selectCandidate() {
        Collections.sort(_open);
        AStarNode selected = _open.remove(0);
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
                AStarNode node = new AStarNode(_curPoint.x + x, _curPoint.y + y);

                //If a button found, set the Fn to minimum so the node will be
                //more likely to be chosen next
                if(_maze.getCell(node.x, node.y) != Maze.BUTTON)
                    node.calulateF(_startPoint, _tour.get(0));
                else
                    node.calulateF(node, node);

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
            return f = getManhattanDistance(startNode) + getManhattanDistance(endNode);
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
