package ee.homies.gaffer;

import javax.transaction.xa.Xid;

import ee.homies.gaffer.util.Uid;

public class GafferXid implements Xid {
  private static final int FORMAT_ID = 0x47666672;

  private Uid globalTransactionId;
  private Uid branchQualifier;

  @Override
  public int getFormatId() {
    return FORMAT_ID;
  }

  @Override
  public byte[] getGlobalTransactionId() {
    return globalTransactionId.asBytes();
  }

  @Override
  public byte[] getBranchQualifier() {
    return branchQualifier.asBytes();
  }

  public static void main(String... args) throws Exception {
    byte[] bytes = "Gffr".getBytes("UTF-8");
    StringBuilder sb = new StringBuilder("FORMAT_ID = 0x");
    for (int i = 0; i < 4; i++) {
      sb.append(String.valueOf(bytes[i] / 16)).append(String.valueOf(bytes[i] % 16));
    }
    System.out.println(sb);
  }
}
