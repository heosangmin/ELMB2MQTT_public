import java.nio.ByteBuffer;

public class Test {
    public static void main1(String[] args) {

        //int i = 0x9E25E695A66A61DD657050DB4E73652D;

        int a = 0x12;
        int b = 0x34;

        ByteBuffer buf = ByteBuffer.allocate(32);

        //buf.put((byte)a);
        //buf.put((byte)b);

        buf = buf.putInt(0x000000FF);
        //buf = buf.putInt(2);

        byte c = (byte)0xFF;
        int d = 0xFF;
        long e = Integer.toUnsignedLong(d);

        byte[] iBytes = {0x00,0x50,0x00,(byte)0xA0};
        ByteBuffer iBuf = ByteBuffer.wrap(iBytes);

        byte[] lBytes = {0x00,0x00,0x00,0x00,0x00,0x00,0x00,(byte)0xFF};
        ByteBuffer lBuf = ByteBuffer.wrap(lBytes);

        //System.out.println(String.format("0x%X", Integer.toBinaryString(d)));
        //System.out.println(Integer.toBinaryString(d));
        System.out.println(iBuf.getInt());
        System.out.println(lBuf.getLong());
        System.out.println((char)iBuf.get(1));
        System.out.println(Integer.toUnsignedString(0xFFFFFFFF));
        System.out.println(Long.toUnsignedString(0xFFFFFFFFFFFFFFFFL));
        System.out.println(Integer.parseInt("000000001101010000110001", 2));
        System.out.println(Integer.parseInt("00100000001", 2));
        System.out.println(Integer.parseInt("0001000001", 2));
    }
}