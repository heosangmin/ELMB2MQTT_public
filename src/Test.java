import java.math.BigInteger;
import java.nio.ByteBuffer;

import de.re.easymodbus.modbusclient.ModbusClient;

public class Test {
    public static void main(String[] args) {

        //int i = 0x9E25E695A66A61DD657050DB4E73652D;

        int a = 0x12;
        int b = 0x34;

        ByteBuffer buf = ByteBuffer.allocate(128);

        buf.put((byte)a);
        buf.put((byte)b);

        System.out.println(buf.array()[0]);
        System.out.println(buf.array()[1]);
        System.out.println(buf.array()[2]);
        

    }
}