package havis.middleware.reader.llrp.service.event;

import havis.llrpservice.data.message.MessageHeader;
import havis.llrpservice.data.message.ROAccessReport;

import org.junit.Assert;
import org.junit.Test;

public class LLRPEventArgsTest {

	@Test
	public void checkLLRPEventArgs() {
		ROAccessReport message = new ROAccessReport(new MessageHeader());

		LLRPEventArgs<ROAccessReport> args = new LLRPEventArgs<ROAccessReport>(message);

		Assert.assertEquals(message, args.getMessage());
	}
}