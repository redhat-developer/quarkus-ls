package com.redhat.quarkus.jdt.core.ls;

import static org.junit.Assert.assertEquals;
import com.redhat.quarkus.jdt.core.ls.QuarkusDelegateCommandHandler;

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
		assertEquals("quarkus.java.projectInfo", QuarkusDelegateCommandHandler.PROJECT_INFO_COMMAND_ID);
	}
}