package com.redhat.qute.services.completions;

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute completion with @CkeckedTemplate.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCompletionWithCheckedTemplateTest {

	@Test
	public void noCheckedTemplateMatching() throws Exception {
		String template = "Item: {|";
		testCompletionFor(template);
	}

	@Test
	public void checkedTemplateMatching() throws Exception {
		String template = "Item: {|";
		testCompletionFor(template, //
				"src/main/resources/templates/ItemResource/items.qute.html", //
				c("items", "items", r(0, 7, 0, 7)));
	}
}