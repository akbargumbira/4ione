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
        states      = new Action[5];

        //Init States
        states[0] = new SetMotor();
        states[1] = new SetForward();
        states[2] = new SetRotate();
        states[3] = new Comm();
        states[4] = new RunMaze();

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

    private class SetMotor extends Action {

        public void init() {
            stop();
            showMotorInfo();
        }

        public void onPRGMPress() {
            // Motor A
            Motor.A.setPower((Motor.A.getPower() + 1) % 8);
            showMotorInfo();
        }

        public void onRUNPress() {
            // Motor C
            Motor.C.setPower((Motor.C.getPower() + 1) % 8);
            showMotorInfo();
        }

        private void showMotorInfo() {
            LCD.clear();
            LCD.showNumber(Motor.A.getPower() * 10 + Motor.C.getPower());
        }
    }

    private class SetForward extends Action {
        int startTime;
        byte state;

        public void init() {
            LCD.clear();
            TextLCD.print("Forward");
            stop();
            state = 0;
        }
        
        public void onPRGMPress() {
            state = 1;
            lastSensorVal = readSensor();
            forward();
        }
        
        public void onRUNPress() {
            state = 0;
            if(isMotorMoving()) stop();
            else forward();
        }

        public boolean update() {
            if(state == 0) {}
            else if(state == 1) {
                int newVal = readSensor();
                if(lastSensorVal/10 != newVal/10) {
                    startTime = (int) System.currentTimeMillis();
                    state = 2;
                    lastSensorVal = newVal;
                }
            }
            if(state == 2) {
                int newVal = readSensor();
                if(lastSensorVal/10 != newVal/10) {
                    forwardTime = (int) System.currentTimeMillis() - startTime;
                    forwardTime /= 2;
                    state = 0;
                    LCD.clear();
                    LCD.showNumber(forwardTime);
                }
            }

            return true;
        }
    }

    private class SetRotate extends Action {

        private Robot robot;
        private byte state;
        private float totalTimeA = 0;
        private float totalTimeC = 0;
        private byte tryCountA = 0;
        private byte tryCountC = 0;
        private float startTime;

        public void init() {
            state = 0;
            stopMotor();
            showInfo((int) getAverageC());
        }

        public void onPRGMPress() {
            if (state == 0) {
                Motor.A.forward();
                Motor.C.backward();
                ++tryCountA;
                startTime = System.currentTimeMillis();
                state = 1;
            } else if (state == 1) {
                stopMotor();
                totalTimeA += System.currentTimeMillis() - startTime;
                rotRTime = (int)(getAverageC() / 4.0);
                showInfo((int) rotRTime);

                Motor.C.forward();
                Motor.A.backward();
                startTime = System.currentTimeMillis();
                ++tryCountC;
                state = 2;
            } else if (state == 2) {
                stopMotor();
                totalTimeC += System.currentTimeMillis() - startTime;
                rotLTime = (int) (getAverageA() / 4.0);
                showInfo((int) rotLTime);
                state = 0;
            }
        }

        public void onRUNPress() {
            // Reset
            totalTimeA = totalTimeC = tryCountA = tryCountC = 0;
        }

        public float getAverageA() {
            return tryCountA == 0 ? 0 : totalTimeA / tryCountA;
        }

        public float getAverageC() {
            return tryCountC == 0 ? 0 : totalTimeC / tryCountC;
        }

        private void stopMotor() {
            Motor.A.stop();
            Motor.C.stop();
        }

        private void showInfo(int n) {
            LCD.clear();
            LCD.showNumber(n);
        }
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
            if(c == '0') return 0;
            if(c == '1') return 1;
            if(c == '2') return 2;
            if(c == '3') return 3;
            if(c == '4') return 4;
            if(c == '5') return 5;
            if(c == '6') return 6;
            if(c == '7') return 7;
            if(c == '8') return 8;
            if(c == '9') return 9;
            return 0;
        }
    }


    /////////////////////////////////
    // MAZE MOVEMENT STATES
    /////////////////////////////////

    private class RunMaze extends Action {
        byte iter;
        byte state;
        GoForward gf    = new GoForward();
        WallHandling wh = new WallHandling();
        GoToMiddle gm   = new GoToMiddle();

        public void init() {
            stop();
            LCD.clear();
            TextLCD.print("RM");

            iter = 0;
            state = 0;
        }

        public void onRUNPress() {
            state = (byte) (state > 0 ? 0 : 1);
            iter = 0;
            stop();
        }

        private void goToMiddle() {
            state = 4;
            gm.init();
        }

        private void hitWall() {
            state = 3;
            wh.init();
        }

        private void nextComm() {
            ++iter;
            state = 1;
        }

        private void goForward() {
            state = 2;
            gf.init();
        }
        
        public boolean update() {
            if(state == 0) { //Stopping..
            }
            else { //Running the maze
                try {

                    //State transitioning -- Messy :p
                    if(state == 2) {
                        gf.update();
                        if(gf.hasSucceeded()) goToMiddle();
                        else if(gf.hitWall()) hitWall();
                    }
                    else if(state == 3) {
                        if(wh.update()) goToMiddle();
                    }
                    else if(state == 4) {
                        gm.update();
                        if(gm.hasSucceeded()) nextComm();
                        else if(gm.hitWall()) hitWall();
                    }
                    else {
                        //Get curr command
                        char cur = steps[iter];

                        //parse and execute
                        if(cur == 'R') {
                            right();
                            Thread.sleep(rotRTime);
                            nextComm();
                        }
                        else if(cur == 'L') {
                            left();
                            Thread.sleep(rotLTime);
                            nextComm();
                        }
                        else if(cur == 'B') {
                            left();
                            Thread.sleep(rotLTime);
                            left();
                            Thread.sleep(rotLTime);
                            nextComm();
                        }
                        else if(cur == 'F') {
                            goForward();
                        }
                        else if(cur == 0) { //We're done..
                            LCD.clear();
                            TextLCD.print("DONE");
                            state = 0;
                        }
                    }
                } catch (InterruptedException ex) {}
            }

            return true;
        }
    }

    private class GoForward extends Action {
        protected boolean _hasSucceeded;

        public void init() {
            _hasSucceeded = false;
            lastSensorVal = readSensor()/10;
            forward();
        }
        
        public boolean update() {
            if(hitWall()) return false;
            if(lastSensorVal != readSensor()/10) _hasSucceeded = true;
            return true;
        }

        public boolean hasSucceeded() {
            return _hasSucceeded;
        }

        public boolean hitWall() {
            return readSensor()/10 == 2;
        }

    }

    private class GoToMiddle extends GoForward {
        int timer;
        
        public boolean update() {
            timer += System.currentTimeMillis();
            if(timer > forwardTime) _hasSucceeded = true;
            return true;
        }
    }

    private class WallHandling extends Action {
        byte state;
        byte found;
        int timer;
        int searchtime = 2000;

        //WARNING : Messy state transition management :/
        public void init() {
            //Init
            found = 0;
            
            //Search right
            timer = (int) System.currentTimeMillis();
            farRight(1);
            state = 0;
        }

        public boolean update() {
            try {
                if(state == 0) {
                    if(readSensor()/10 != 2) {
                        timer = (int) (System.currentTimeMillis() - timer);
                        found = 1; //found right
                        farRight(-1);
                        Thread.sleep(timer);

                        //Backward..
                        backward();
                        state = 3;
                    }
                    else if(System.currentTimeMillis() - timer > searchtime) {
                        farRight(-1);
                        Thread.sleep(searchtime);

                        //Search left
                        timer = (int) System.currentTimeMillis();
                        farLeft(1);
                        state = 2;
                    }
                }
                else if(state == 2) {
                    if(readSensor()/10 != 2) {
                        timer = (int) (System.currentTimeMillis() - timer);
                        found = 2; //found left
                        farLeft(-1);
                        Thread.sleep(timer);

                        //Backward..
                        backward();
                        state = 3;
                    }
                    else if(System.currentTimeMillis() - timer > searchtime) {
                        farLeft(-1);
                        Thread.sleep(searchtime);

                        //Search left
                        timer = (int) System.currentTimeMillis();
                        farRight(1);
                        state = 0;
                    }
                }
                else if(state == 3) {
                    int val = readSensor()/10;
                    if(val !=2 && val != lastSensorVal) {
                        //Correcting
                        if(found == 2) farLeft(1);
                        else farRight(1);
                        Thread.sleep(timer);
                        return true;
                    }
                }
            } catch(Exception e) {
                
            }

            return false;
        }
    }

    public static void main(String[] args) {
        Robot r = new Robot();
        while(r.update());
    }
}