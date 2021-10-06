import java.io.IOException;

import de.re.easymodbus.exceptions.ModbusException;

public class Main {
    public static void main(String[] args) {
        Sample sample = new Sample();
        try {
            sample.test();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ModbusException e) {
            e.printStackTrace();
        }
    }
}