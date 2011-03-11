package pathfinder;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;
import mindstorm.LogWriter;
import mindstorm.Maze;
import mindstorm.Pathfinder;

/**
 *
 * @author Ismail Sunni
 */
public class UCSPathfinder implements Pathfinder{
    private Maze            _maze;
    private LogWriter       _log;
    private UCSnode         _startPoint;
    private UCSnode         _endPoint;
    private List<Point>     _completePath;

    public UCSPathfinder(LogWriter log) {
        _log = log;
    }


    private List<UCSnode> getChild(UCSnode node){
        List<UCSnode> child  =  new ArrayList<UCSnode>();
        if (_maze.isWalkable(node.x-1, node.y)) child.add(new UCSnode(node.x-1, node.y, node.distance + 1, node));
        if (_maze.isWalkable(node.x+1, node.y)) child.add(new UCSnode(node.x+1, node.y, node.distance + 1, node));
        if (_maze.isWalkable(node.x, node.y-1)) child.add(new UCSnode(node.x, node.y-1, node.distance + 1, node));
        if (_maze.isWalkable(node.x, node.y+1)) child.add(new UCSnode(node.x, node.y+1, node.distance + 1, node));
        // log
        _log.log("create child of node ("+node.x+","+node.y+")");

        return child;
    }

    public boolean isuttonTouched(List<Point> buttons, Point button) {
        for (int i=0; i< buttons.size();++i)
        {
            if (button.x==buttons.get(i).x && button.y==buttons.get(i).y)   
            {
                _log.log("get button ("+button.x+","+button.y+")");
                return true;
            }

        }
        return false;
    }

    public List<Point> solve(Maze maze) {
        //Variables
        _log.log("Init UCS");
        _maze = maze;
        _startPoint = new UCSnode(_maze.getEnterPos().x, _maze.getEnterPos().y, 0) ;
        // log
        _log.log("Starting node : " + _startPoint);
        _endPoint = new UCSnode(_maze.getExitPos().x, _maze.getExitPos().y, 0) ;
        _completePath = new LinkedList<Point>();
        UCSnode currNode, startNode, tempNode;
        List<Point> buttons = _maze.getButtons();
        Stack stack = new Stack();
        List<UCSnode> nodeChild = null;
        int buttontouched = 0;
        Maze tempMaze = _maze.cloneMaze();
        PriorityQueue<UCSnode> PQ = new PriorityQueue<UCSnode>();
        //Queue queue = new LinkedList<UCSnode>();

        //Get All the button first
        currNode = _startPoint;
        startNode = _startPoint;
        while (buttontouched!=buttons.size())
        {
            PQ.removeAll(PQ);
            // log
            _log.log("emptied Priority Queue");
            nodeChild = getChild(currNode);
            // log
            for (int i=0; i< nodeChild.size();++i)
            {
                PQ.add(nodeChild.get(i));
                // log
                _log.log("add node to Priority Queue("+nodeChild.get(i).x+","+nodeChild.get(i).y+")");
            }

            //Find the button!
            while (!PQ.isEmpty() && tempMaze.getCell(currNode.x, currNode.y)!=Maze.BUTTON)
            {
                currNode = (UCSnode) PQ.remove();
                // log
                _log.log("compute node("+currNode.x+","+currNode.y+")");
                nodeChild = getChild(currNode);
                for (int i=0; i< nodeChild.size();++i)
                {
                    PQ.add(nodeChild.get(i));
                    // log
                    _log.log("add node to Priority Queue("+nodeChild.get(i).x+","+nodeChild.get(i).y+")");
                }
            }
            //Get The Button or PQ is empty
            //currNode == button
            if (tempMaze.getCell(currNode.x, currNode.y)==Maze.BUTTON)
            {
                tempNode = currNode;
                //Step the route
                while (currNode.x!=startNode.x || currNode.y!=startNode.y)
                {
                    stack.push(currNode);
                    // log
                    _log.log("put node to stack ("+currNode.x+","+currNode.y+")");
                    currNode = currNode.parent;
                }
                stack.push(currNode);
                // log
                _log.log("put node to stack ("+currNode.x+","+currNode.y+")");

                //Add route to _completePath
                while (!stack.isEmpty())
                {
                    Point temp = new Point(((UCSnode)stack.peek()).x, ((UCSnode)stack.peek()).y);
                    _completePath.add(temp);
                    // log
                    _log.log("put node to complete path"+temp.toString());
                    stack.pop();
                }
                ++buttontouched;
                startNode = currNode = tempNode;
                tempMaze.setCell(startNode.x, startNode.y, Maze.PATH);
             }
        }

        //Get the Finish
        PQ.removeAll(PQ);
        // log
        _log.log("emptied Priority Queue");
        nodeChild = getChild(currNode);
        for (int i=0; i< nodeChild.size();++i)
        {
            PQ.add(nodeChild.get(i));
            // log
            _log.log("add node to Priority Queue("+nodeChild.get(i).x+","+nodeChild.get(i).y+")");
        }
        //Find the funish!
        while (!PQ.isEmpty() && tempMaze.getCell(currNode.x, currNode.y)!=Maze.EXIT)
        {
            currNode = (UCSnode) PQ.remove();
            nodeChild = getChild(currNode);
            for (int i=0; i< nodeChild.size();++i)
            {
                PQ.add(nodeChild.get(i));
                // log
                _log.log("add node to Priority Queue("+nodeChild.get(i).x+","+nodeChild.get(i).y+")");
            }
        }
        //Get The Finish or PQ is empty
        //currNode == finish
        if (tempMaze.getCell(currNode.x, currNode.y)==Maze.EXIT)
        {
            tempNode = currNode;
            //Step the route
            while (currNode.x!=startNode.x || currNode.y!=startNode.y)
            {
                stack.push(currNode);
                // log
                _log.log("put node to stack ("+currNode.x+","+currNode.y+")");
                currNode = currNode.parent;
            }
            stack.push(currNode);
            // log
            _log.log("put node to stack ("+currNode.x+","+currNode.y+")");

            //Add route to _completePath
            while (!stack.isEmpty())
            {
                Point temp = new Point(((UCSnode)stack.peek()).x, ((UCSnode)stack.peek()).y);
                _completePath.add(temp);
                // log
                _log.log("put node to complete path:"+temp.toString());
                stack.pop();
            }
        }
        // log
        String sTemp = "";
        for(Point p: _completePath)
        {
            sTemp+=">>("+p.x+","+p.y+")";
        }
        // log
        _log.log("map solved in "+_completePath.size()+" step");
        _log.log("Complete Path : "+sTemp);
        _log.log("Finish");
        return _completePath.isEmpty() ? null : _completePath;
    }



    private class UCSnode extends Point implements Comparable<UCSnode>
    {
        int distance;
        UCSnode parent;

        public UCSnode(int X, int Y, int Distance)
        {
            x = X; y = Y; distance = Distance;
        }

        public UCSnode(int X, int Y, int Distance, UCSnode Parent)
        {
            x = X; y = Y; distance = Distance; parent = Parent;
        }

        public int compareTo(UCSnode u)
        {
            if(this.distance < u.distance) return -1;
            else if(this.distance > u.distance) return 1;
            else return 0;
        }
    }


}
