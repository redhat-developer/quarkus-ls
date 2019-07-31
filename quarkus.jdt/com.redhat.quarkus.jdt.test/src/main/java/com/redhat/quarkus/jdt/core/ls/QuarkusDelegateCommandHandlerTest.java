package com.redhat.quarkus.jdt.core.ls;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
* Quarkus integration test. In Eclipse, right-click > Run As > JUnit-Plugin. <br/>
* In Maven CLI, run "mvn integration-test".
*/
public class QuarkusDelegateCommandHandlerTest {

	private QuarkusDelegateCommandHandler commandHandler;

	@Before
	public void setUp() {
		commandHandler = new QuarkusDelegateCommandHandler();
	}

	@Test
	public void veryStupidTest() throws Exception {
		assertEquals("Hello World", commandHandler.executeCommand(QuarkusDelegateCommandHandler.COMMAND_ID, null, null));
	}
}