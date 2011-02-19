package mindstorm;

import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Maze class - handles reading map from file and provides interface to do stuff
 * with the maze
 * @author Ecky
 */
public class Maze {
    //Constants
    public static char PATH    = 'P';
    public static char ENTER   = 'E';
    public static char EXIT    = 'X';
    public static char BUTTON  = 'B';
    public static char WALL    = 'W';

    //Vars
    private char        _map[][];
    private int         _sizeX;
    private int         _sizeY;
    private Point       _enterPos;
    private Point       _exitPos;
    private List<Point> _buttons;

    public Maze() {
        //Init vars
        _sizeX      = 0;
        _sizeY      = 0;
        _enterPos   = new Point(0, 0);
        _exitPos    = new Point(0, 0);
        _buttons    = new ArrayList<Point>();
    }
    
    public Maze cloneMaze() {
        Maze cloneMaze = new Maze();

        cloneMaze._map = new char[_sizeX][_sizeY];
        for (int j=0; j<_sizeY; ++j)
        {
            for (int i=0; i<_sizeX; ++i)
            {
                cloneMaze._map[i][j] =_map[i][j];

            }
        }
        cloneMaze._sizeX = _sizeX;
        cloneMaze._sizeY = _sizeY;
       
        cloneMaze._enterPos = cloneMaze.getEnterPos();
        cloneMaze._exitPos = cloneMaze.getExitPos();
        cloneMaze._buttons = cloneMaze.getButtons();
        
        return cloneMaze;
    }

    /**
     * Initialize map from file.
     * WARNING : input file must be valid
     * @param filename
     * @return this instance
     */
    public Maze load(String filename) throws FileNotFoundException {
        //Reset vars
        _sizeX = 0;
        _sizeY = 0;
        _enterPos.setLocation(0, 0);
        _exitPos.setLocation(0, 0);
        _buttons.clear();

        //Open file
        File    f = new File(filename);
        Scanner s = new Scanner(f);

        //Get size
        _sizeX = Integer.parseInt(s.next());
        _sizeY = Integer.parseInt(s.next());

        //Create map
        _map = new char[_sizeX][_sizeY];

        //Read file, save point of interests
        for(int y = 0; y < _sizeY; ++y) {
            for(int x = 0; x < _sizeX; ++x) {
                char c = Character.toUpperCase(s.next().charAt(0));
                _map[x][y] = c;
                if(c == EXIT)           _exitPos = new Point(x, y);
                else if(c == ENTER)     _enterPos = new Point(x, y);
                else if(c == BUTTON)    _buttons.add(new Point(x, y));
            }
        }

        return this;
    }

    /**
     * Check whether a given point is inside the map boundary
     * @param posX
     * @param posY
     * @return true if point insida map boundary
     */
    public boolean isValidPoint(int posX, int posY) {
        return 
                posX >= 0 && posX < _sizeX &&
                posY >= 0 && posY < _sizeY;
    }

    /**
     * Checks whether robot can walk on the given point
     * @param posX
     * @param posY
     * @return
     */
    public boolean isWalkable(int posX, int posY) {
        return isValidPoint(posX, posY) && getCell(posX, posY) != WALL;
    }

    /**
     * Get cell at given position.
     * @param posX
     * @param posY
     * @return the cell's code in given position
     */
    public char getCell(int posX, int posY) {
        return _map[posX][posY];
    }

    public void setCell(int posX, int posY, char C, Maze m){
        m._map[posX][posY] = Character.toUpperCase(C);
    }


    /**
     * Get string that represents the current maze. print it so you an see it.
     * @return
     */
    public String getMapRepresentation() {
        String retval = "";

        for(int y = 0; y < _sizeY; ++y) {
            for(int x = 0; x < _sizeX; ++x) {
                retval += getCell(x, y) + " ";
            }
            retval += "\n";
        }

        return retval;
    }
    
    public String getMapRepresentation(Point taintPos, char taint) {
        String retval = "";

        char temp = _map[taintPos.x][taintPos.y];
        _map[taintPos.x][taintPos.y] = taint;

        retval = getMapRepresentation();

        _map[taintPos.x][taintPos.y] = temp;

        return retval;
    }

    //Getters
    public Point        getEnterPos()   { return _enterPos; }
    public Point        getExitPos()    { return _exitPos; }
    public List<Point>  getButtons()    { return _buttons; }
}
