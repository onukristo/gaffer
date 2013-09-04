package ee.homies.gaffer.test.complextest1.app;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

@Component("idGenerator")
public class IdGenerator {
	private final AtomicInteger seq = new AtomicInteger();

	public int next() {
		return seq.addAndGet(1);
	}
}
