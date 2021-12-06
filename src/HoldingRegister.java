import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;

public class HoldingRegister extends Register implements IRegister{

    public ByteBuffer read() throws UnknownHostException, SocketException, ModbusException, IOException {
        int[] regs = modbusClient.ReadHoldingRegisters(address, dataType.registers);
        ByteBuffer buf = ByteBuffer.allocate(regs.length * 2);
        for (int i = regs.length - 1; i > -1; i--) {
            buf.put((byte) ((regs[i] >> 8) & 0xFF));
            buf.put((byte) (regs[i] & 0xFF));
        }
        buf.position(0);
        return buf;
    }

    public void write(int value) throws UnknownHostException, SocketException, ModbusException, IOException {
        if (dataType.registers  == 1) {
            modbusClient.WriteSingleRegister(address, value);
        } else if (dataType.registers  == 2) {
            int regs[] = ModbusClient.ConvertDoubleToTwoRegisters(value);
            modbusClient.WriteMultipleRegisters(address, regs);
        }
    }

    public void write(float value) throws UnknownHostException, SocketException, ModbusException, IOException {
        int[] regs = ModbusClient.ConvertFloatToTwoRegisters(value);
        modbusClient.WriteMultipleRegisters(address, regs);
    }
    
}
