package havis.middleware.reader.llrp.client;

import havis.middleware.ale.base.exception.ValidationException;
import havis.middleware.ale.reader.Property.Connector;
import havis.middleware.reader.llrp.client.LLRPProperties.PropertyName;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class LLRPConnectionTest {

	@Test
	public void checkLLRPConnectionConstructor() throws ValidationException {
		LLRPConnection llrpConnection = new LLRPConnection();
		LLRPProperties llrpProperties = new LLRPProperties();

		llrpProperties.setInventoryAttempts(10000);
		llrpProperties.setKeepalive(20000);

		llrpConnection.setConnectionProperties(llrpProperties);
		llrpConnection.setTimeout(10000);

		Assert.assertEquals(llrpProperties, llrpConnection.getConnectionProperties());
		Assert.assertEquals(10000, llrpConnection.getTimeout());
	}

	@Test
	public void checkLLRPConnection() throws ValidationException {
		Map<String, String> properties = new HashMap<>();
		properties.put(Connector.ConnectionType, "TCP");
		properties.put(Connector.Host, "10.10.10.10");
		properties.put(Connector.Port, "5084");
		properties.put(Connector.Timeout, "3000");
		properties.put(PropertyName.InventoryAttempts, "3");
		properties.put(PropertyName.Keepalive, "30000");

		LLRPConnection llrpConnection = LLRPConnection.validateConnectorProperties(properties);

		Assert.assertEquals(3000, llrpConnection.getTimeout());
		Assert.assertEquals(LLRPConnectionType.TCP, llrpConnection.getConnectionType());
		Assert.assertEquals("10.65.22.134", llrpConnection.getHost());
		Assert.assertEquals(5084, llrpConnection.getPort());
	}
	
	@Test
	public void checkLLRPConnectionEquality() throws ValidationException {
		Map<String, String> properties = new HashMap<>();
		properties.put(Connector.ConnectionType, "TCP");
		properties.put(Connector.Host, "10.10.10.10");
		properties.put(Connector.Port, "5084");
		properties.put(Connector.Timeout, "3000");
		properties.put(PropertyName.InventoryAttempts, "3");
		properties.put(PropertyName.Keepalive, "30000");

		LLRPConnection llrpConnection1 = LLRPConnection.validateConnectorProperties(properties);
		LLRPConnection llrpConnection2 = LLRPConnection.validateConnectorProperties(properties);

		Assert.assertEquals(true, llrpConnection1.equals(llrpConnection2));
	}
	
	@Test
	public void checkLLRPConnectionNotEquality() throws ValidationException {
		Map<String, String> properties = new HashMap<>();
		properties.put(Connector.ConnectionType, "TCP");
		properties.put(Connector.Host, "10.10.10.10");
		properties.put(Connector.Port, "5084");
		properties.put(Connector.Timeout, "3000");
		properties.put(PropertyName.InventoryAttempts, "3");
		properties.put(PropertyName.Keepalive, "30000");

		LLRPConnection llrpConnection1 = LLRPConnection.validateConnectorProperties(properties);
		LLRPConnection llrpConnection2 = null;

		Assert.assertEquals(false, llrpConnection1.equals(llrpConnection2));
		
		llrpConnection2 = LLRPConnection.validateConnectorProperties(properties);
		Assert.assertNotEquals(llrpConnection1.hashCode(), llrpConnection2.hashCode());
	}
	
	@Test
	public void checkLLRPConnectionToString() throws ValidationException {
		Map<String, String> properties = new HashMap<>();
		properties.put(Connector.ConnectionType, "TCP");
		properties.put(Connector.Host, "10.10.10.10");
		properties.put(Connector.Port, "5084");
		properties.put(Connector.Timeout, "3000");
		properties.put(PropertyName.InventoryAttempts, "3");
		properties.put(PropertyName.Keepalive, "30000");

		LLRPConnection llrpConnection1 = LLRPConnection.validateConnectorProperties(properties);
		LLRPConnection llrpConnection2 = LLRPConnection.validateConnectorProperties(properties);

		Assert.assertEquals(llrpConnection1.toString(), llrpConnection2.toString());
	}
	
	@Test
	public void checkUnknownConnectionType() throws ValidationException {
		Map<String, String> properties = new HashMap<>();
		properties.put(Connector.ConnectionType, "UDP");
		properties.put(Connector.Host, "10.10.10.10");
		properties.put(Connector.Port, "5084");
		properties.put(Connector.Timeout, "3000");
		properties.put(PropertyName.InventoryAttempts, "3");
		properties.put(PropertyName.Keepalive, "30000");

		boolean wasExceptionThown = false;
		
		try {
			LLRPConnection.validateConnectorProperties(properties);
		} catch(Exception e) {
			wasExceptionThown = true;
		}
		
		Assert.assertTrue(wasExceptionThown);
	}
	
	@Test
	public void checkConnectionTypeDefault() throws ValidationException {
		Map<String, String> properties = new HashMap<>();
		properties.put(Connector.ConnectionType, null);
		properties.put(Connector.Host, "10.10.10.10");
		properties.put(Connector.Port, "5084");
		properties.put(Connector.Timeout, "3000");
		properties.put(PropertyName.InventoryAttempts, "3");
		properties.put(PropertyName.Keepalive, "30000");

		LLRPConnection llrpConnection = LLRPConnection.validateConnectorProperties(properties);

		Assert.assertEquals(LLRPConnectionType.TCP, llrpConnection.getConnectionType());
	}
	
	@Test
	public void checkHostIsNull() throws ValidationException {
		Map<String, String> properties = new HashMap<>();
		properties.put(Connector.ConnectionType, "TCP");
		properties.put(Connector.Host, null);
		properties.put(Connector.Port, "5084");
		properties.put(Connector.Timeout, "3000");
		properties.put(PropertyName.InventoryAttempts, "3");
		properties.put(PropertyName.Keepalive, "30000");

		boolean wasExceptionThown = false;
		
		try {
			LLRPConnection.validateConnectorProperties(properties);
		} catch(Exception e) {
			wasExceptionThown = true;
		}
		
		Assert.assertTrue(wasExceptionThown);
	}
	
	@Test
	public void checkHostIsEmpty() throws ValidationException {
		Map<String, String> properties = new HashMap<>();
		properties.put(Connector.ConnectionType, "TCP");
		properties.put(Connector.Host, "   ");
		properties.put(Connector.Port, "5084");
		properties.put(Connector.Timeout, "3000");
		properties.put(PropertyName.InventoryAttempts, "3");
		properties.put(PropertyName.Keepalive, "30000");

		boolean wasExceptionThown = false;
		
		try {
			LLRPConnection.validateConnectorProperties(properties);
		} catch(Exception e) {
			wasExceptionThown = true;
		}
		
		Assert.assertTrue(wasExceptionThown);
	}
	
	@Test
	public void checkPortDefault() throws ValidationException {
		Map<String, String> properties = new HashMap<>();
		properties.put(Connector.ConnectionType, "TCP");
		properties.put(Connector.Host, "10.10.10.10");
		properties.put(Connector.Port, null);
		properties.put(Connector.Timeout, "3000");
		properties.put(PropertyName.InventoryAttempts, "3");
		properties.put(PropertyName.Keepalive, "30000");

		LLRPConnection llrpConnection = LLRPConnection.validateConnectorProperties(properties);
		Assert.assertEquals(5084, llrpConnection.getPort());
	}
	
	@Test
	public void checkPortIsAlpha() throws ValidationException {
		Map<String, String> properties = new HashMap<>();
		properties.put(Connector.ConnectionType, "TCP");
		properties.put(Connector.Host, "10.10.10.10");
		properties.put(Connector.Port, "");
		properties.put(Connector.Timeout, "3000");
		properties.put(PropertyName.InventoryAttempts, "3");
		properties.put(PropertyName.Keepalive, "30000");

		boolean wasExceptionThown = false;
		
		try {
			LLRPConnection.validateConnectorProperties(properties);
		} catch(Exception e) {
			wasExceptionThown = true;
		}
		
		Assert.assertTrue(wasExceptionThown);
	}
	
	@Test
	public void checkPortIsMoreThan() throws ValidationException {
		Map<String, String> properties = new HashMap<>();
		properties.put(Connector.ConnectionType, "TCP");
		properties.put(Connector.Host, "10.10.10.10");
		properties.put(Connector.Port, "65536");
		properties.put(Connector.Timeout, "3000");
		properties.put(PropertyName.InventoryAttempts, "3");
		properties.put(PropertyName.Keepalive, "30000");

		boolean wasExceptionThown = false;
		
		try {
			LLRPConnection.validateConnectorProperties(properties);
		} catch(Exception e) {
			wasExceptionThown = true;
		}
		
		Assert.assertTrue(wasExceptionThown);
	}
	
	@Test
	public void checkPortIsLessThan() throws ValidationException {
		Map<String, String> properties = new HashMap<>();
		properties.put(Connector.ConnectionType, "TCP");
		properties.put(Connector.Host, "10.10.10.10");
		properties.put(Connector.Port, "-1");
		properties.put(Connector.Timeout, "3000");
		properties.put(PropertyName.InventoryAttempts, "3");
		properties.put(PropertyName.Keepalive, "30000");

		boolean wasExceptionThown = false;
		
		try {
			LLRPConnection.validateConnectorProperties(properties);
		} catch(Exception e) {
			wasExceptionThown = true;
		}
		
		Assert.assertTrue(wasExceptionThown);
	}
	

	
	@Test
	public void checkDisagreeProperties() throws ValidationException {
		Map<String, String> properties = new HashMap<>();
		properties.put(havis.middleware.ale.reader.Prefix.Connector+"Test", "Test");

		boolean wasExceptionThown = false;
		
		try {
			LLRPConnection.validateConnectorProperties(properties);
		} catch(Exception e) {
			wasExceptionThown = true;
		}
		
		Assert.assertTrue(wasExceptionThown);
	}
	
	@Test
	public void checkInvalidTimeout() throws ValidationException {
		Map<String, String> properties = new HashMap<>();
		properties.put(Connector.ConnectionType, "TCP");
		properties.put(Connector.Host, "10.10.10.10");
		properties.put(Connector.Port, "8080");
		properties.put(Connector.Timeout, "a");
		properties.put(PropertyName.InventoryAttempts, "3");
		properties.put(PropertyName.Keepalive, "30000");

		boolean wasExceptionThown = false;
		
		try {
			LLRPConnection.validateConnectorProperties(properties);
		} catch(Exception e) {
			wasExceptionThown = true;
		}
		
		Assert.assertTrue(wasExceptionThown);
	}
	
	@Test
	public void checkInvalidInventoryAttempts() throws ValidationException {
		Map<String, String> properties = new HashMap<>();
		properties.put(Connector.ConnectionType, "TCP");
		properties.put(Connector.Host, "10.10.10.10");
		properties.put(Connector.Port, "8080");
		properties.put(Connector.Timeout, "10000");
		properties.put(PropertyName.InventoryAttempts, "d");
		properties.put(PropertyName.Keepalive, "30000");

		boolean wasExceptionThown = false;
		
		try {
			LLRPConnection.validateConnectorProperties(properties);
		} catch(Exception e) {
			wasExceptionThown = true;
		}
		
		Assert.assertTrue(wasExceptionThown);
	}
	
	@Test
	public void checkInvalidKeepAlive() throws ValidationException {
		Map<String, String> properties = new HashMap<>();
		properties.put(Connector.ConnectionType, "TCP");
		properties.put(Connector.Host, "10.10.10.10");
		properties.put(Connector.Port, "8080");
		properties.put(Connector.Timeout, "10000");
		properties.put(PropertyName.InventoryAttempts, "3");
		properties.put(PropertyName.Keepalive, "i");

		boolean wasExceptionThown = false;
		
		try {
			LLRPConnection.validateConnectorProperties(properties);
		} catch(Exception e) {
			wasExceptionThown = true;
		}
		
		Assert.assertTrue(wasExceptionThown);
	}
}
