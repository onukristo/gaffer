package ee.homies.gaffer.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class UidImpl implements Uid {
  private static final int MAX_LENGTH = 64;
  private static final AtomicInteger sequencer = new AtomicInteger();
  private final String instanceId;
  private final long startTimeMillis;
  private final int sequence;
  private byte[] bytes;

  public UidImpl(String instanceId, long startTimeMillis) {
    this.instanceId = instanceId;
    this.startTimeMillis = startTimeMillis;
    sequence = sequencer.getAndIncrement();

    try {
      byte[] instanceIdBytes = instanceId.getBytes("UTF-8");
      bytes = new byte[Math.min(MAX_LENGTH, 4 + 8 + instanceIdBytes.length)];

      System.arraycopy(Encoder.intToBytes(sequence), 0, bytes, 0, 4);
      System.arraycopy(Encoder.longToBytes(sequence), 0, bytes, 4, 8);
      System.arraycopy(instanceIdBytes, 0, bytes, 12, Math.min(MAX_LENGTH - 12, instanceIdBytes.length));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public long getStartTimeMillis() {
    return startTimeMillis;
  }

  @Override
  public int getSequence() {
    return sequence;
  }

  @Override
  public String getInstanceId() {
    return instanceId;
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Performance is important.")
  public byte[] asBytes() {
    return bytes;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof Uid)) {
      return false;
    }
    return Arrays.equals(asBytes(), ((Uid) o).asBytes());
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(asBytes());
  }

  @Override
  public String toString() {
    return getInstanceId() + "-" + String.valueOf(sequence) + "-" + String.valueOf(getStartTimeMillis());
  }
}
