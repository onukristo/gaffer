package ee.homies.gaffer;

public class ServiceRegistryHolder {
  private static volatile ServiceRegistry serviceRegistry;
  private static volatile Configuration configuration;

  public static ServiceRegistry getServiceRegistry() {
    if (serviceRegistry == null) {
      synchronized (ServiceRegistryHolder.class) {
        if (serviceRegistry == null) {
          createServiceRegistry();
        }
      }
    }
    return serviceRegistry;
  }

  public static void destroyServiceRegistry() {
    if (serviceRegistry != null) {
      serviceRegistry.destroy();
      serviceRegistry = null;
    }
  }

  private static void createServiceRegistry() {
    if (configuration == null) {
      configuration = new Configuration();
    }
    serviceRegistry = new ServiceRegistry(configuration);
  }
}
