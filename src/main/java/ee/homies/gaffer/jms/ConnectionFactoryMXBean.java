package ee.homies.gaffer.jms;

import javax.management.MXBean;

@MXBean
public interface ConnectionFactoryMXBean {
  long getAllSessionGetsCount();

  long getBufferedSessionGetsCount();

  long getNonTransactionalSessionGetsCount();
}
