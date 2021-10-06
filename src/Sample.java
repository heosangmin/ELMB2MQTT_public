import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.re.easymodbus.exceptions.ModbusException;

public class Sample {
    
    public Sample() {
    }

    public void test() throws UnknownHostException, SocketException, ModbusException, IOException {
        
        byte[] jsonData = Files.readAllBytes(Paths.get("json/EnapterElectrolyser.json"));
        ObjectMapper mapper = new ObjectMapper();
        Device device = mapper.readValue(jsonData, Device.class);

        device.setIpAddress("127.0.0.1");
        device.setPort(502);
        device.connect();

        device.writeUint64(0, 1633503636019L);
        long lResult = device.readUint64(0);
        System.out.println(lResult);

        device.writeBoolean(4, 1);
        boolean bResult = device.readBooleanHR(4);
        System.out.println(bResult);

        device.writeFloat32(1002, (float)50.5);
        float fResult = device.readFloat32HR(1002);
        System.out.println(fResult);

        device.writeUint32(4020, 0xC0A80201);
        int iResult = device.readUint32HR(4020);
        System.out.println(String.format("0x%X",iResult));

        device.writeUint16(4046, 514);
        iResult = device.readUint16HR(4046);
        System.out.println(iResult);

        // int deviceModel = 0x454C3231;
        // client.WriteMultipleRegisters(0, ModbusClient.ConvertDoubleToTwoRegisters(deviceModel));

        // int returned = ModbusClient.ConvertRegistersToDouble(client.ReadHoldingRegisters(0, 2));
        // System.out.println(String.format("0x%X",returned));
        
        

    }

    
    
}
