package havis.middleware.reader.llrp.service.exception;

import havis.llrpservice.data.message.ErrorMessage;
import havis.llrpservice.data.message.MessageHeader;
import havis.llrpservice.data.message.ProtocolVersion;
import havis.llrpservice.data.message.parameter.LLRPStatus;
import havis.llrpservice.data.message.parameter.LLRPStatusCode;
import havis.llrpservice.data.message.parameter.TLVParameterHeader;
import havis.middleware.reader.llrp.util.IDGenerator;

import org.junit.Assert;
import org.junit.Test;

public class LLRPErrorExceptionTest {
	@Test
	public void checkException() {
		String msg = "This is a test!";
		LLRPStatus llrpStatus = new LLRPStatus(new TLVParameterHeader(), LLRPStatusCode.R_DEVICE_ERROR, msg);
		MessageHeader messageHeader = new MessageHeader((byte)0, ProtocolVersion.LLRP_V1_1, IDGenerator.getUniqueMessageID());
		ErrorMessage errorMessage = new ErrorMessage(messageHeader, llrpStatus);

		LLRPErrorException exception = new LLRPErrorException(errorMessage);
		
		Assert.assertEquals(msg, exception.getLLRPError().getStatus().getErrorDescription());
	}
}