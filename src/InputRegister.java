import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import de.re.easymodbus.exceptions.ModbusException;

public class InputRegister extends Register  implements IRegister{

    public ByteBuffer read() throws UnknownHostException, SocketException, ModbusException, IOException {
        int[] regs = modbusClient.ReadInputRegisters(address, dataType.registers);
        ByteBuffer buf = ByteBuffer.allocate(regs.length * 2);
        for (int i = regs.length - 1; i > -1; i--) {
            buf.put((byte) ((regs[i] >> 8) & 0xFF));
            buf.put((byte) (regs[i] & 0xFF));
        }
        buf.position(0);
        return buf;
    }

    public void write(int value) {
        System.out.println("[ModB][IR] Error: Input Register is read only.");
    }

    public void write(float value) {
        System.out.println("[ModB][IR] Error: Input Register is read only.");
    }

}
