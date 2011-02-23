/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import josx.platform.rcx.*;
import josx.rcxcomm.RCXInputStream;

/**
 *
 * @author user
 */
public class Robot {

    private byte currentState = 0;
    private boolean isDone;
    private Action states[];
    public int forwardTime;
    public int rotRTime;
    public int rotLTime;
    public String s;

    // Step
    int SIZE = 200;
    char[] steps;


    public Robot() {
        // Init Variable
        isDone = false;
        forwardTime = 1000;
        rotLTime = 1000;
        rotRTime = 1000;
        states = new Action[5];
        states[0] = new SetMotor();
        states[1] = new SetForward(this);
        states[2] = new SetRotate(this);
        states[3] = new Comm();
        states[4] = new RunMaze();

        // Add Listener
        Button.VIEW.addButtonListener(new ButtonListener() {

            public void buttonPressed(Button button) {
                // empty
            }

            public void buttonReleased(Button button) {
                ++currentState;
                currentState %= states.length;
                states[currentState].init();
            }
        });

        Button.PRGM.addButtonListener(new ButtonListener() {

            public void buttonPressed(Button button) {
                //
            }

            public void buttonReleased(Button button) {
                states[currentState].onPRGMPress();
            }
        });

        Button.RUN.addButtonListener(new ButtonListener() {

            public void buttonPressed(Button button) {
                //
            }

            public void buttonReleased(Button button) {
                states[currentState].onRUNPress();
            }
        });

        //Init state
        states[currentState].init();
    }

    public void stop() {
        Motor.A.stop();
        Motor.C.stop();
    }

    public void forward() {
        Motor.A.forward();
        Motor.C.forward();
        try {
            Thread.sleep((long) forwardTime);
        } catch (InterruptedException ex) {
            //
        }
    }

    public void backward() {
        Motor.A.backward();
        Motor.C.backward();
        try {
            Thread.sleep((long) forwardTime);
        } catch (InterruptedException ex) {
            //
        }
    }

    public void left() {
        Motor.A.backward();
        Motor.C.forward();
        try {
            Thread.sleep((long) rotLTime);
        } catch (InterruptedException ex) {
            //
        }
    }

    public void right() {
        Motor.A.forward();
        Motor.C.backward();
        try {
            Thread.sleep((long) rotRTime);
        } catch (InterruptedException ex) {
            //
        }
    }

    private boolean isDone() {
        return isDone;
    }

    private class Action {

        public void init() {
        }

        public void onPRGMPress() {
        }

        public void onRUNPress() {
        }
    }

    private class SetMotor extends Action {

        public void init() {
            Motor.A.stop();
            Motor.C.stop();
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

        private byte state;
        private float totalTime = 0;
        private byte tryCount = 0;
        private float startTime;
        private Robot robot;

        public SetForward(Robot r) {
            robot = r;
        }

        public void init() {
            state = 0;
            stopMotor();
            showInfo();
        }

        public void onPRGMPress() {
            if (state == 0) {
                startTime = System.currentTimeMillis();
                Motor.A.forward();
                Motor.C.forward();
                state = 1;
                ++tryCount;
            } else if (state == 1) {
                stopMotor();
                totalTime += System.currentTimeMillis() - startTime;
                state = 0;
                robot.forwardTime = getAverage();
            }

            showInfo();
        }

        public void onRUNPress() {
            // Reset
            totalTime = 0;
            tryCount = 0;
            robot.forwardTime = 1000;
            showInfo();
        }

        public int getAverage() {
            return tryCount == 0 ? 0 : (int)(totalTime / tryCount);
        }

        private void stopMotor() {
            Motor.A.stop();
            Motor.C.stop();
        }

        private void showInfo() {
            LCD.clear();
            LCD.showNumber((int) getAverage());
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

        public SetRotate(Robot robot) {
            this.robot = robot;
        }

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

    private class RunMaze extends Action {

        public void init() {
            // Stop All Motor
            Motor.A.stop();
            Motor.C.stop();

            LCD.clear();
            TextLCD.print("RM");
        }

        public void onPRGMPress() {
            //
        }

        public void onRUNPress() {
            // Run
            byte i = 0;
            byte length;

            length = (byte) steps.length;
            while(steps[i]!=0) {
                if (steps[i] == 'F') {
                    forward();
                } else if (steps[i] == 'L') {
                    left();
                } else if (steps[i] == 'R') {
                    right();
                } else if (steps[i] == 'B') {
                    backward();
                }
                
                ++i;
            }
            stop();
        }
    }


    private class Comm extends Action {

        RCXInputStream in;
        int byte1;
        int byte2;

        public void init() {
            LCD.clear();
            TextLCD.print("Comm");
        }

        public void onPRGMPress() {
        }

        public void onRUNPress() {
            LCD.clear();
            TextLCD.print("RunComm");

            in = new RCXInputStream();
            try {
                while (true) {
                    byte1 = in.read();
                    showInfo(byte1);
                    if (byte1 == ' ')
                        break;

//                    byte2 = in.read();
//                    showInfo(byte2);
//
//                    if      (byte1 == 's' && byte2 == 'm')  handleMaze();
//
////                    else if (byte1 == 'f' && byte2 == 'f')  parseForward();
////                    else if (byte1 == 'm' && byte2 == 'l')  parseMotorLeft();
////                    else if (byte1 == 'm' && byte2 == 'r')  parseMotorRight();
////                    else if (byte1 == 'r' && byte2 == 'r')  parseRotateRight();
////                    else if (byte1 == 'r' && byte2 == 'l')  parseRotateLeft();

                }
            } catch (IOException ex) {
                //
            }
            in.close();
        }

        private void handleMaze() {
            int i = 0;
            try {
                while (true) {
                    byte1 = in.read();
                    showInfo(byte1);
                    if (byte1 == ' ')
                        break;

                    steps[i++] = (char)byte1;
                }
            } catch (IOException ex) {
            }
            steps[i] = 0;
        }

        private void showInfo(int n) {
            LCD.clear();
            LCD.showNumber(n);
        }
    }
    public static void main(String[] args) {
        Robot r = new Robot();
        while (!r.isDone()) {
        }
    }
}
