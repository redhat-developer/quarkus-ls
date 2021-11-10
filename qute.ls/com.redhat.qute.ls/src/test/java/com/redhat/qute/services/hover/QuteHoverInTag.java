package com.redhat.qute.services.hover;

import static com.redhat.qute.QuteAssert.assertHover;

import org.junit.jupiter.api.Test;

public class QuteHoverInTag {

	@Test
	public void noHover() throws Exception {
		String template = "{#|}";
		assertHover(template);
	}
}
