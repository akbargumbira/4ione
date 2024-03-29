package pathfinder;

import java.awt.Point;
import java.util.*;
import mindstorm.Maze;
import mindstorm.Pathfinder;
import mindstorm.LogWriter;

/**
 *
 * @author Sandy Socrates
 */
public class DFSPathfinder implements Pathfinder {
    private Maze            _maze;
    private LogWriter       _log;
    private DFSNode         _startPoint;
    private Point         _endPoint;
    private List<Point>     _completePath;

    public DFSPathfinder(LogWriter log) {
        _log = log;
    }


    public boolean isButtonTouched(List<Point> buttons, Point button) {
        for (int i=0; i< buttons.size();++i)
        {
            if (button.x==buttons.get(i).x && button.y==buttons.get(i).y)   return true;
        }
        return false;
    }

    public List<Point> solve(Maze maze) {
        //Variables
        _maze = maze;
        _startPoint = new DFSNode(_maze.getEnterPos().x, _maze.getEnterPos().y) ;
        _endPoint = new DFSNode(_maze.getExitPos().x, _maze.getExitPos().y) ;
        _completePath = new LinkedList<Point>();
        DFSNode currNode;
        List<Point> buttons = _maze.getButtons();
        int button_length = _maze.getButtons().size();
        //Get start
        currNode = _startPoint;

        List<Point> tempPath = new  LinkedList<Point>();
        Point tempPoint = new Point(currNode.x,currNode.y);
        boolean buttonfound = true;
        //Starting from the startpoint, recurse
        while (buttons.size()!=0 && buttonfound){
            buttonfound = FindButton(tempPoint,tempPath,buttons);
            _completePath.addAll(tempPath);
            tempPoint = tempPath.get(tempPath.size()-1);
            tempPath.clear();
            _log.log("buton size = " + String.valueOf(buttons.size()));
        }
        //No Button found
        if (!buttonfound && button_length!=0)
        {
            _log.log("Button unreacheable");
            return null;
        }
        //No Path to EXIT
        if(!FindFinish(tempPoint, tempPath))
        {
            _log.log("EXIT unreachable");
            return null;
        }

        _completePath.addAll(tempPath);
        _log.log("Path :");
        for (int i = 0; i < _completePath.size(); ++i)
        {
            _log.log(_completePath.get(i).x + "," + _completePath.get(i).y);
        }
        
        return _completePath;
    }

    private boolean FindButton(Point currentNode, List<Point> Path, List<Point> Buttons)
    {
        Point point;
        Path.add(currentNode);
        this._log.log("push "+ currentNode.x + "," +currentNode.y);
        if (isButtonTouched(Buttons,currentNode))
        {
            Buttons.remove(currentNode);
            _log.log("Found button at " + currentNode.x + "," + currentNode.y);
            return true;
        }
        else
        {
            if (_maze.isWalkable(currentNode.x + 1, currentNode.y) && !Path.contains(new Point(currentNode.x + 1, currentNode.y)))
                if(FindButton(point = new Point(currentNode.x + 1, currentNode.y),Path,Buttons)) return true;
            if (_maze.isWalkable(currentNode.x, currentNode.y + 1) && !Path.contains(new Point(currentNode.x, currentNode.y + 1)))
                if (FindButton(point = new Point(currentNode.x, currentNode.y + 1),Path,Buttons)) return true;
            if (_maze.isWalkable(currentNode.x - 1, currentNode.y) && !Path.contains(new Point(currentNode.x - 1, currentNode.y)))
                if (FindButton(point = new Point(currentNode.x - 1, currentNode.y),Path,Buttons)) return true;
            if (_maze.isWalkable(currentNode.x, currentNode.y - 1) && !Path.contains(new Point(currentNode.x, currentNode.y - 1)))
                if(FindButton(point = new Point(currentNode.x, currentNode.y - 1),Path,Buttons)) return true;
        }
        _log.log("pop " + currentNode.x + "," + currentNode.y);
        Path.remove(currentNode);
        return false;
    }

    private boolean FindFinish(Point currentNode, List<Point> Path)
    {
        Point point;
        Path.add(currentNode);
        this._log.log("push "+ currentNode.x + "," +currentNode.y);

        if (_maze.getCell(currentNode.x, currentNode.y)==Maze.EXIT)
        {
            _log.log("Found EXIT at " + currentNode.x + "," + currentNode.y);
            return true;
        }
        else
        {
            if (_maze.isWalkable(currentNode.x + 1, currentNode.y) && !Path.contains(new Point(currentNode.x + 1, currentNode.y)))
                if(FindFinish(point = new Point(currentNode.x + 1, currentNode.y),Path)) return true;
            if (_maze.isWalkable(currentNode.x, currentNode.y + 1) && !Path.contains(new Point(currentNode.x, currentNode.y + 1)))
                if (FindFinish(point = new Point(currentNode.x, currentNode.y + 1),Path)) return true;
            if (_maze.isWalkable(currentNode.x - 1, currentNode.y) && !Path.contains(new Point(currentNode.x - 1, currentNode.y)))
                if (FindFinish(point = new Point(currentNode.x - 1, currentNode.y),Path)) return true;
            if (_maze.isWalkable(currentNode.x, currentNode.y - 1) && !Path.contains(new Point(currentNode.x, currentNode.y - 1)))
                if (FindFinish(point = new Point(currentNode.x, currentNode.y - 1),Path)) return true;
        }
        _log.log("pop " + currentNode.x + "," + currentNode.y);
        Path.remove(currentNode);
        return false;
    }

    private class DFSNode extends Point {
          public DFSNode parent;

          public DFSNode(int X, int Y) {
              x = X;
              y = Y;
              parent = null;
          }

          public DFSNode(int X, int Y, DFSNode Parent) {
              x = X;
              y = Y;
              parent = Parent;
          }
      }
}
