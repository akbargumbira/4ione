import java.io.IOException;
import josx.platform.rcx.*;
import josx.rcxcomm.RCXInputStream;

/**
 * USAGE :
 *  Press View to change states
 *  - Comm :
 *      - PRGM  : See the status of robot, will be displayed in LCD
 *      - RUN   : Init communication with tower
 *  - RunMaze :
 *      - PRGM  : Demo, robot will rotate 180 deg, go forward, turn left, then right, stop.
 *      - RUN   : Run the maze
 * @author Ecky, Rezan
 */
public class Robot {

    //States management
    private byte currentState = 0;
    private Action states[];

    //Timing
    public int forwardTime;
    public int rotRTime;
    public int rotLTime;

    //Step buffer
    private final int SIZE = 100;
    private char[] steps = new char[SIZE];

    public Robot() {
        //Init Variables
        forwardTime = 1000;
        rotLTime    = 1000;
        rotRTime    = 1000;
        states      = new Action[5];

        //Init States
        states    = new Action[2];
        states[0] = new Comm();
        states[1] = new RunMaze();
        
        // Add Listener
        Button.VIEW.addButtonListener(new ButtonListener() {

            public void buttonPressed(Button button) {}
            public void buttonReleased(Button button) {
                ++currentState;
                currentState %= states.length;
                states[currentState].init();
            }
        });

        Button.PRGM.addButtonListener(new ButtonListener() {

            public void buttonPressed(Button button) {}
            public void buttonReleased(Button button) {
                states[currentState].onPRGMPress();
            }
        });

        Button.RUN.addButtonListener(new ButtonListener() {

            public void buttonPressed(Button button) {}
            public void buttonReleased(Button button) {
                states[currentState].onRUNPress();
            }
        });

        //Init state
        states[currentState].init();
    }

    public boolean isMotorMoving() {
        return Motor.A.isMoving() || Motor.C.isMoving();
    }

    public void stop() {
        Motor.A.stop();
        Motor.C.stop();
    }

    public void forward() {
        Motor.A.forward();
        Motor.C.forward();
    }

    public void left() {
        Motor.A.backward();
        Motor.C.forward();
    }

    public void right() {
        Motor.A.forward();
        Motor.C.backward();
    }

    public boolean update() {
        return states[currentState].update();
    }








    
    private class Action {
        public void init() {}
        public void onPRGMPress() {}
        public void onRUNPress() {}
        public boolean update() { return true; }
    }

    private class Comm extends Action {

        RCXInputStream in;
        int byte1;
        int byte2;
        int read;

        public void init() {
            stop();
            LCD.clear();
            TextLCD.print("Comm");
        }
        
        public void onPRGMPress() {
            try {

                //Show Motors
                LCD.clear();
                LCD.showNumber(Motor.A.getPower() * 10 + Motor.C.getPower());
                Thread.sleep(500);

                //Show FF time
                LCD.clear();
                LCD.showNumber(forwardTime);
                Thread.sleep(500);

                //Show Rot L Time
                LCD.clear();
                LCD.showNumber(rotLTime);
                Thread.sleep(500);

                //Show Rot R Time
                LCD.clear();
                LCD.showNumber(rotRTime);
                Thread.sleep(500);
                
            } catch (InterruptedException ex) {}
        }

        public void onRUNPress() {
            LCD.clear();
            TextLCD.print("RunComm");
            read = 0;

            in = new RCXInputStream();
            try {
				LCD.clear();
				TextLCD.print("Block");
                byte1 = in.read();
                while (true) {
                    if (byte1 == ' ') break;
                    byte2 = in.read();

                    if      (byte1 == 's' && byte2 == 'm')  handleMaze();
                    else if (byte1 == 'f' && byte2 == 'f')  forwardTime = parseNum();
                    else if (byte1 == 'm' && byte2 == 'l')  Motor.A.setPower(parseNum());
                    else if (byte1 == 'm' && byte2 == 'r')  Motor.C.setPower(parseNum());
                    else if (byte1 == 'r' && byte2 == 'r')  rotRTime = parseNum();
                    else if (byte1 == 'r' && byte2 == 'l')  rotLTime = parseNum();

                    LCD.clear();
                    LCD.showNumber(++read);
                }
            } catch (IOException ex) {}

            LCD.clear();
            TextLCD.print("Fin");
            in.close();
        }

        private void handleMaze() throws IOException {
            //Reset steps
            int i;
            for(i = 0; i < SIZE; ++i) steps[i] = 0;

            //Read steps
            i = 0;
            while(true) {
                byte1 = in.read();
                if(byte1 != 'F' && byte1 != 'R' && byte1 != 'L' && byte1 != 'B') break;
                else steps[i++] = (char) byte1;
            }
        }

        private int parseNum() throws IOException {
            int retval  = 0;
            while(true) {
                byte1 = in.read();
                if(byte1 < '0' || byte1 > '9') break;

                retval = retval * 10 + charToInt((char) byte1);
            }

            return retval;
        }

        private int charToInt(char c) {
            return c - '0';
        }
    }

    private class RunMaze extends Action {
        byte iter;

        public void init() {
            stop();
            LCD.clear();
            TextLCD.print("RM");

            iter = 0;
        }
        
        public void onPRGMPress() {
            //Demo movement..
            try {

                //Go forward
                forward();
                Thread.sleep(4 * forwardTime);
				stop();
				Thread.sleep(1000);


				//Rot Right 180 deg
                right();
                Thread.sleep(rotRTime * 2);
				stop();
				Thread.sleep(1000);
				
                //Rot Left 180 deg
                left();
                Thread.sleep(rotLTime * 2);

                //Stop
                stop();
            
            } catch (InterruptedException ex) {}
        }

        public void onRUNPress() {
            iter = 0;
            stop();

            while(true) {
                try {
                    char cur = steps[iter++];
                    if(cur == 'F') {
                        forward();
                        Thread.sleep(forwardTime);
                    }
                    else if(cur == 'B') {
                        left();
                        Thread.sleep(rotLTime * 2);
                    }
                    else if(cur == 'L') {
                        left();
                        Thread.sleep(rotLTime);
                    }
                    else if(cur == 'R') {
                        right();
                        Thread.sleep(rotRTime);
                    }
                    else if(cur == 0) {
                        stop();
                        break;
                    }
                } catch(InterruptedException ex) {
                    
                }
            }
        }



    }


    public static void main(String[] args) {
        Robot r = new Robot();
        while(true);
    }
}