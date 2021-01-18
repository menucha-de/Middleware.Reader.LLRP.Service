package havis.middleware.reader.llrp.service.exception;

import org.junit.Assert;
import org.junit.Test;

public class LLRPExceptionTest {
	@Test
	public void checkException() {
		String msg = "This is a test!";
		LLRPException exception = new LLRPException(msg);
		
		Assert.assertEquals(msg, exception.getMessage());
	}
}