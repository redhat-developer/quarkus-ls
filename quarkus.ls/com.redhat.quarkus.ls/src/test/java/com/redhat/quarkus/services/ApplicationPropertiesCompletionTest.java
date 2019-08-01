/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.services;

import static com.redhat.quarkus.services.QuarkusAssert.c;
import static com.redhat.quarkus.services.QuarkusAssert.testCompletionFor;

import org.junit.Test;

import com.redhat.quarkus.ls.commons.BadLocationException;

/**
 * Test with completion in 'application.properties' file.
 * 
 * @author Angelo ZERR
 *
 */
public class ApplicationPropertiesCompletionTest {

	@Test
	public void completionOnKey() throws BadLocationException {
		String value = "|";
		testCompletionFor(value, c("quarkus.http.cors", "quarkus.http.cors"));
	}
}
