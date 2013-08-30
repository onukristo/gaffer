package ee.homies.gaffer.util;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormatLogger {
  private final Logger log;

  public FormatLogger(Class<?> clazz) {
    log = LoggerFactory.getLogger(clazz);
  }

  public FormatLogger(String category) {
    log = LoggerFactory.getLogger(category);
  }

  public void debug(String format, Object... args) {
    if (log.isDebugEnabled()) {
      log.debug(getMessage(format, args));
    }
  }

  public void debug(String format, Throwable t, Object... args) {
    if (log.isDebugEnabled()) {
      log.debug(getMessage(format, args), t);
    }
  }

  public void warn(String format, Object... args) {
    if (log.isWarnEnabled()) {
      log.warn(getMessage(format, args));
    }
  }

  public void warn(String format, Throwable t, Object... args) {
    if (log.isWarnEnabled()) {
      log.warn(getMessage(format, args), t);
    }
  }

  public void info(String format, Object... args) {
    if (log.isInfoEnabled()) {
      log.info(getMessage(format, args));
    }
  }

  public void info(String format, Throwable t, Object... args) {
    if (log.isInfoEnabled()) {
      log.info(getMessage(format, args), t);
    }
  }

  public void error(String format, Object... args) {
    if (log.isErrorEnabled()) {
      log.error(getMessage(format, args));
    }
  }

  public void error(String format, Throwable t, Object... args) {
    if (log.isErrorEnabled()) {
      log.error(getMessage(format, args), t);
    }
  }

  public void trace(String format, Object... args) {
    if (log.isTraceEnabled()) {
      log.trace(getMessage(format, args));
    }
  }

  public void trace(String format, Throwable t, Object... args) {
    if (log.isTraceEnabled()) {
      log.trace(getMessage(format, args), t);
    }
  }

  public boolean isDebugEnabled() {
    return log.isDebugEnabled();
  }

  public boolean isWarnEnabled() {
    return log.isWarnEnabled();
  }

  public boolean isInfoEnabled() {
    return log.isInfoEnabled();
  }

  public boolean isErrorEnabled() {
    return log.isErrorEnabled();
  }

  public boolean isTraceEnabled() {
    return log.isTraceEnabled();
  }

  private static String getMessage(String format, Object... args) {
    if (format == null) {
      return Arrays.toString(args);
    }
    if (args == null || args.length == 0) {
      return format;
    }
    return String.format(format, args);
  }
}
