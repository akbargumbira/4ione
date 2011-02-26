import java.io.IOException;
import josx.platform.rcx.*;
import josx.rcxcomm.RCXInputStream;

/**
 *
 * @author user
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
    private final int SIZE = 200;
    private char[] steps = new char[SIZE];

    //Movement logic
    private int lastSensorVal;


    public Robot() {
        //Init Variables
        forwardTime = 1000;
        rotLTime    = 1000;
        rotRTime    = 1000;
        states      = new Action[1];

        //Init States
        states[0] = new Comm();

        //Init sensors
        Sensor.S2.setTypeAndMode(3, 0x80);
        Sensor.S2.activate();

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

    public int readSensor() {
        return Sensor.S2.readValue();
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

    public void backward() {
        Motor.A.backward();
        Motor.C.backward();
    }

    public void left() {
        Motor.A.backward();
        Motor.C.forward();
    }

    public void right() {
        Motor.A.forward();
        Motor.C.backward();
    }

    public void farRight(int dir) {
        if(dir > 0) Motor.A.forward();
        else Motor.A.backward();
        Motor.C.stop();
    }

    public void farLeft(int dir) {
        Motor.A.stop();
        if(dir > 0) Motor.C.forward();
        else Motor.C.backward();
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
					LCD.clear();
					TextLCD.print("while");
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
                if(byte1 != 'F' || byte1 != 'R' || byte1 != 'L' || byte1 != 'B') break;
                else steps[i++] = (char) byte1;
            }
        }

        private int parseNum() throws IOException {
            int idx     = 1;
            int retval  = 0;
            while(true) {
                byte1 = in.read();
                if(byte1 < '0' || byte1 > '9') break;

                retval = retval * idx + charToInt((char) byte1);
                idx *= 10;
            }

            return retval;
        }

        private int charToInt(char c) {
            return c - '0';
        }
    }

    public static void main(String[] args) {
        Robot r = new Robot();
        while(r.update());
    }
}