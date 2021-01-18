package havis.middleware.reader.llrp.service.exception;

import org.junit.Assert;
import org.junit.Test;

public class LLRPTimeoutExceptionTest {

	@Test
	public void checkException() {
		String msg = "This is a test!";
		LLRPTimeoutException exception = new LLRPTimeoutException(msg);
		
		Assert.assertEquals(msg, exception.getMessage());
	}
}