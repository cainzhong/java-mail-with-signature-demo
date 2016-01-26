package com.java.mail.signature.operation;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class DataServiceTest {
	@Test
	public void testReplaceData() throws Exception {
		final String modifyDataMethodName = "modifyData";
		final byte[] expectedBinaryData = new byte[] { 42 };
		final String expectedDataId = "id";

		// Mock only the modifyData method
		DataService tested = PowerMock.createPartialMock(DataService.class, modifyDataMethodName);

		// Expect the private method call to "modifyData"
		PowerMock.expectPrivate(tested, modifyDataMethodName, expectedDataId, expectedBinaryData).andReturn(true);

		PowerMock.replay(tested);

		assertTrue(tested.replaceData(expectedDataId, expectedBinaryData));

		PowerMock.verify(tested);
	}
}
