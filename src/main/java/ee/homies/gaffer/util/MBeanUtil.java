package ee.homies.gaffer.util;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

public class MBeanUtil {
  private static final FormatLogger log = new FormatLogger(MBeanUtil.class);

  public static void registerMBeanQuietly(Object mBean, String objectNameSt) {
    MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
    try {
      ObjectName objectName = new ObjectName(objectNameSt);
      boolean registered = mBeanServer.isRegistered(objectName);

      log.info("%s MBean for '%s'.", registered ? "Reregistering" : "Registering", objectNameSt);

      if (registered) {
        mBeanServer.unregisterMBean(objectName);
      }
      mBeanServer.registerMBean(mBean, new ObjectName(objectNameSt));
    } catch (Exception e) {
      log.error("Failed to register MBean '%s'." + objectNameSt, e);
    }
  }

  public static void unregisterMBeanQuietly(String objectNameSt) {
    MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
    try {
      ObjectName objectName = new ObjectName(objectNameSt);
      mBeanServer.unregisterMBean(objectName);
    } catch (Exception e) {
      log.error("Failed to unregister MBean '%s'.", objectNameSt);
    }
  }
}
