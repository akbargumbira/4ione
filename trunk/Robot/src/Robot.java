/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import josx.platform.rcx.*;

/**
 *
 * @author user
 */
public class Robot {
    private byte currentState = 0;
    private boolean isDone;
    private Action states[];

    public float forwardTime;
    public float rotRTime;
    public float rotLTime;

    public Robot() {
        // Init Variable
        isDone = false;
        forwardTime = 1000;
        rotLTime = 1000;
        rotRTime = 1000;
        states = new Action[4];
        states[0] = new SetMotor();
        states[1] = new SetForward(this);
        states[2] = new SetRotate(this);
        states[3] = new RunMaze();

        // Add Listener
        Button.VIEW.addButtonListener(new ButtonListener() {

            public void buttonPressed(Button button) {
                // empty
            }

            public void buttonReleased(Button button) {
                ++currentState;
                currentState %= 4;
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
            LCD.showNumber(Motor.A.getPower()*10 + Motor.C.getPower());
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
                state =  1;
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

        public float getAverage() {
            return tryCount == 0 ? 0 : totalTime/tryCount;
        }

        private void stopMotor() {
            Motor.A.stop();
            Motor.C.stop();
        }

        private void showInfo() {
            LCD.clear();
            LCD.showNumber((int)getAverage());
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
            showInfo((int)getAverageC());
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
                rotRTime = (float) (getAverageC() / 4.0);
                showInfo((int)rotRTime);

                Motor.C.forward();
                Motor.A.backward();
                startTime = System.currentTimeMillis();
                ++tryCountC;
                state = 2;
            } else if (state == 2) {
                stopMotor();
                totalTimeC += System.currentTimeMillis() - startTime;
                rotLTime = (float) (getAverageA() / 4.0);
                showInfo((int)rotLTime);
                state = 0;
            }
        }

        public void onRUNPress() {
            // Reset
            totalTimeA = totalTimeC = tryCountA = tryCountC = 0;
        }

        public float getAverageA() {
            return tryCountA == 0 ? 0 : totalTimeA/tryCountA;
        }

        public float getAverageC() {
            return tryCountC == 0 ? 0 : totalTimeC/tryCountC;
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

        private char step[] = {'F', 'F', 'L', 'F', 'R', 'F', 'B', 'B'};

        public void init() {
            // Stop All Motor
            Motor.A.stop();
            Motor.C.stop();

            // Recieve step
        }

        public void onPRGMPress() {
            //
        }

        public void onRUNPress() {
            // Run
            byte i;
            byte length;

            length = (byte)step.length;
            for (i = 0; i < length; ++i) {
                if      (step[i] == 'F')    forward();
                else if (step[i] == 'L')    left();
                else if (step[i] == 'R')    right();
                else if (step[i] == 'B')    backward();
            }
            stop();
        }
    }

    public static void main(String[] args) {
        Robot r = new Robot();
        while (!r.isDone()) {
        }
    }
}