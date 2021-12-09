package jp.co.nttdatabizsys.modbus;

import java.io.IOException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.paho.client.mqttv3.MqttException;

import de.re.easymodbus.exceptions.ModbusException;

public class MainTest {

    public static void main(String[] args) {

        try {
            Sample sample = new Sample();
            
            TimerTask f1 = new TimerTask() {
                public void run() {
                    try {
                        sample.sendKeepAlive();
                    } catch (ModbusException | IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            TimerTask f2 = new TimerTask() {
                public void run() {
                    try {
                        sample.readAndPublishAll();
                    } catch (ModbusException | IOException | MqttException e) {
                        e.printStackTrace();
                    }
                }
            };

            Timer timer1 = new Timer("timer_Toshiba_keepalive");
            Timer timer2 = new Timer("timer_read_and_publish");

            Calendar cal = Calendar.getInstance();
            
            // 次回起動時刻設定
            // cal.clear(Calendar.SECOND);
            // cal.clear(Calendar.MILLISECOND);
            // cal.add(Calendar.MINUTE, 1);

            timer1.scheduleAtFixedRate(f1, cal.getTime(), 1000);
            timer2.scheduleAtFixedRate(f2, cal.getTime(), 60000);

            sample.subscribe();

        } catch (IOException | MqttException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}