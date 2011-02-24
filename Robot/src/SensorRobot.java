
import josx.platform.rcx.*;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author user
 */
public class SensorRobot {

    byte[] steps = {'F', 'F', 'F', 'F'};
    int curStep = 0;
    
    public SensorRobot() {
        Sensor.S2.setTypeAndMode(3, 0x80);
        Sensor.S2.activate();
        Sensor.S2.addSensorListener(new SensorListener() {
            public void stateChanged (Sensor src, int oldValue, int newValue) {
                if(oldValue/10 != newValue/10) {
                    curStep++;
                    LCD.clear();
                    LCD.showNumber(curStep);
                }
            }
        });

        Motor.A.forward();
        Motor.C.forward();
    }

    public static void Main(String args[]) {
        new SensorRobot();
        while(true);
    }

}
