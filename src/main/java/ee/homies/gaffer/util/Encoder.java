package ee.homies.gaffer.util;

public class Encoder {
  public static byte[] longToBytes(long aLong) {
    byte[] array = new byte[8];

    array[7] = (byte) (aLong & 0xff);
    array[6] = (byte) ((aLong >> 8) & 0xff);
    array[5] = (byte) ((aLong >> 16) & 0xff);
    array[4] = (byte) ((aLong >> 24) & 0xff);
    array[3] = (byte) ((aLong >> 32) & 0xff);
    array[2] = (byte) ((aLong >> 40) & 0xff);
    array[1] = (byte) ((aLong >> 48) & 0xff);
    array[0] = (byte) ((aLong >> 56) & 0xff);

    return array;
  }

  public static byte[] intToBytes(int anInt) {
    byte[] array = new byte[4];

    array[3] = (byte) (anInt & 0xff);
    array[2] = (byte) ((anInt >> 8) & 0xff);
    array[1] = (byte) ((anInt >> 16) & 0xff);
    array[0] = (byte) ((anInt >> 24) & 0xff);

    return array;
  }
}
