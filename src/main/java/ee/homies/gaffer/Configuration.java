package ee.homies.gaffer;

public class Configuration {
  private String instanceId = "Gaffer";

  /**
   * Sometimes calling API like Spring will not show any exceptions during
   * rollbacks and commits, so logging those out in gaffer can be neccessary.
   */
  private boolean logExceptions = false;
  private long beforeCommitValidationRequiredTimeMs = -1;

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public boolean isLogExceptions() {
    return logExceptions;
  }

  public void setLogExceptions(boolean logExceptions) {
    this.logExceptions = logExceptions;
  }

  public long getBeforeCommitValidationRequiredTimeMs(){
    return beforeCommitValidationRequiredTimeMs;
  }

  public void setbeforeCommitValidationRequiredTimeMs(long beforeCommitValidationRequiredTimeMs){
    this.beforeCommitValidationRequiredTimeMs = beforeCommitValidationRequiredTimeMs;
  }

}
