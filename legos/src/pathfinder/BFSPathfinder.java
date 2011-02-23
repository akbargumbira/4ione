package pathfinder;

import java.awt.Point;
import mindstorm.LogWriter;
import mindstorm.Maze;
import mindstorm.Pathfinder;
import java.util.*;

/**
 *
 * @author Akbar Gumbira
 */
public class BFSPathfinder implements Pathfinder{

    private Maze            _maze;
    private LogWriter       _log;
    private BFSNode         _startPoint;
    private BFSNode         _endPoint;
    private List<Point>     _completePath;
    
    public BFSPathfinder(LogWriter log) {
        _log = log;
    }

    
    private List<BFSNode> getChild(BFSNode node){
        List<BFSNode> child  =  new ArrayList<BFSNode>();
        if (_maze.isWalkable(node.x-1, node.y)) child.add(new BFSNode(node.x-1, node.y, node));
        if (_maze.isWalkable(node.x+1, node.y)) child.add(new BFSNode(node.x+1, node.y, node));
        if (_maze.isWalkable(node.x, node.y-1)) child.add(new BFSNode(node.x, node.y-1, node));
        if (_maze.isWalkable(node.x, node.y+1)) child.add(new BFSNode(node.x, node.y+1, node));

        return child;
    }

    public boolean isuttonTouched(List<Point> buttons, Point button) {
        for (int i=0; i< buttons.size();++i)
        {
            if (button.x==buttons.get(i).x && button.y==buttons.get(i).y)   return true;
        }
        return false;
    }

    public List<Point> solve(Maze maze) {
        //Variables
        _maze = maze;
        _startPoint = new BFSNode(_maze.getEnterPos().x, _maze.getEnterPos().y) ;
        _endPoint = new BFSNode(_maze.getExitPos().x, _maze.getExitPos().y) ;
        _completePath = new LinkedList<Point>();
        BFSNode currNode, startNode, tempNode;
        List<Point> buttons = _maze.getButtons();
        Stack stack = new Stack();
        List<BFSNode> nodeChild = null;
        int buttontouched = 0;
        Maze tempMaze = _maze.cloneMaze();
        Queue queue = new LinkedList<BFSNode>();
        
        //Get All the button first
        currNode = _startPoint;
        startNode = _startPoint;
        while (buttontouched!=buttons.size())
        {
            queue.removeAll(queue);
            nodeChild = getChild(currNode);
            for (int i=0; i< nodeChild.size();++i)
            {
                queue.add(nodeChild.get(i));
            }
            
            //Find the button!
            while (!queue.isEmpty() && tempMaze.getCell(currNode.x, currNode.y)!=Maze.BUTTON)
            {
                currNode = (BFSNode) queue.remove();
                nodeChild = getChild(currNode);
                for (int i=0; i< nodeChild.size();++i)
                {
                    queue.add(nodeChild.get(i));
                }
            }
            //Get The Button or Queue is empty
            //currNode == button
            if (tempMaze.getCell(currNode.x, currNode.y)==Maze.BUTTON)
            {
                tempNode = currNode;
                //Step the route
                while (currNode.x!=startNode.x || currNode.y!=startNode.y)
                {
                    stack.push(currNode);
                    currNode = currNode.parent;
                }
                stack.push(currNode);

                //Add route to _completePath
                while (!stack.isEmpty())
                {
                    Point temp = new Point(((BFSNode)stack.peek()).x, ((BFSNode)stack.peek()).y);
                    _completePath.add(temp);
                    stack.pop();
                }
                ++buttontouched;
                startNode = currNode = tempNode;
                tempMaze.setCell(startNode.x, startNode.y, Maze.PATH);
             }
        }

        //Get the Finish
        queue.removeAll(queue);
        nodeChild = getChild(currNode);
        for (int i=0; i< nodeChild.size();++i)
        {
            queue.add(nodeChild.get(i));
        }
        //Find the funish!
        while (!queue.isEmpty() && tempMaze.getCell(currNode.x, currNode.y)!=Maze.EXIT)
        {
            currNode = (BFSNode) queue.remove();
            nodeChild = getChild(currNode);
            for (int i=0; i< nodeChild.size();++i)
            {
                queue.add(nodeChild.get(i));
            }
        }
        //Get The Finish or Queue is empty
        //currNode == finish
        if (tempMaze.getCell(currNode.x, currNode.y)==Maze.EXIT)
        {
            tempNode = currNode;
            //Step the route
            while (currNode.x!=startNode.x || currNode.y!=startNode.y)
            {
                stack.push(currNode);
                currNode = currNode.parent;
            }
            stack.push(currNode);
            
            //Add route to _completePath
            while (!stack.isEmpty())
            {
                Point temp = new Point(((BFSNode)stack.peek()).x, ((BFSNode)stack.peek()).y);
                _completePath.add(temp);
                stack.pop();
            }
        }
        return _completePath.isEmpty() ? null : _completePath;
    }


      private class BFSNode extends Point {
          public BFSNode parent;

          public BFSNode(int X, int Y) {
              x = X;
              y = Y;
              parent = null;
          }

          public BFSNode(int X, int Y, BFSNode Parent) {
              x = X;
              y = Y;
              parent = Parent;
          }
      }
}
