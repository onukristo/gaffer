package ee.homies.gaffer.util;

public interface Uid {
  long getStartTimeMillis();

  int getSequence();

  String getInstanceId();

  byte[] asBytes();
}
