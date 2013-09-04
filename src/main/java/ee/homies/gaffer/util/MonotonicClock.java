package ee.homies.gaffer.util;

import java.util.concurrent.atomic.AtomicLong;

public class MonotonicClock implements Clock {
  private final AtomicLong lastTime = new AtomicLong();

  @Override
  public long currentTimeMillis() {
    long now = System.currentTimeMillis();
    long time = lastTime.get();
    if (now > time) {
      lastTime.compareAndSet(time, now);
      return lastTime.get();
    }
    return time;
  }
}
