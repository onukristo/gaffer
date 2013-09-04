package ee.homies.gaffer.test.complextest1.app;

import org.springframework.stereotype.Component;

@Component("config")
public class Config {
  private boolean createPassword = true;
  private boolean failPasswordCreation;
  private boolean useHibernate = false;

  public boolean isCreatePassword() {
    return createPassword;
  }

  public void setCreatePassword(boolean createPassword) {
    this.createPassword = createPassword;
  }

  public boolean isFailPasswordCreation() {
    return failPasswordCreation;
  }

  public void setFailPasswordCreation(boolean failPasswordCreation) {
    this.failPasswordCreation = failPasswordCreation;
  }

  public boolean isUseHibernate() {
    return useHibernate;
  }

  public void setUseHibernate(boolean useHibernate) {
    this.useHibernate = useHibernate;
  }

}
