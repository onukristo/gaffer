package ee.homies.gaffer.jms;

import javax.management.MXBean;

@MXBean
public interface ConnectionFactoryMXBean {
  public long getAllSessionGetsCount();

  public long getBufferedSessionGetsCount();

  public long getNonTransactionalSessionGetsCount();
}
