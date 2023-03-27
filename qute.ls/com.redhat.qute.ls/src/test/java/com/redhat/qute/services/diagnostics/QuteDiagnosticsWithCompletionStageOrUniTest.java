/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.diagnostics;

import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.junit.jupiter.api.Test;

/**
 * Test diagnostics with CompletableFuture and Uni as data model.
 *
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsWithCompletionStageOrUniTest {

	@Test
	public void objectPartWithCompletionStage() throws Exception {
		String template = "{@java.util.concurrent.CompletionStage<org.acme.Item> item}\r\n" + //
				" \r\n" + //
				"{item.name}";
		testDiagnosticsFor(template);
	}

	@Test
	public void objectPartWithCompletableFuture() throws Exception {
		String template = "{@java.util.concurrent.CompletableFuture<org.acme.Item> item}\r\n" + //
				" \r\n" + //
				"{item.name}";
		testDiagnosticsFor(template);
	}

	@Test
	public void objectPartWithUni() throws Exception {
		String template = "{@io.smallrye.mutiny.Uni<org.acme.Item> item}\r\n" + //
				" \r\n" + //
				"{item.name}";
		testDiagnosticsFor(template);
	}

	@Test
	public void objectPartWithAsyncResultUni() throws Exception {
		String template = "{@io.smallrye.mutiny.vertx.AsyncResultUni<org.acme.Item> item}\r\n" + //
				" \r\n" + //
				"{item.name}";
		testDiagnosticsFor(template);
	}

	@Test
	public void forWithCompletableFuture() throws Exception {
		String template = "{@java.util.concurrent.CompletableFuture<java.util.List<org.acme.Item>> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.name}    \r\n" + //
				"{/for}";
		testDiagnosticsFor(template);
	}

	@Test
	public void forWithCompletionStage() throws Exception {
		String template = "{@java.util.concurrent.CompletionStage<java.util.List<org.acme.Item>> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.name}    \r\n" + //
				"{/for}";
		testDiagnosticsFor(template);
	}

	@Test
	public void forWithUni() throws Exception {
		String template = "{@io.smallrye.mutiny.Uni<java.util.List<org.acme.Item>> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.name}    \r\n" + //
				"{/for}";
		testDiagnosticsFor(template);
	}

	@Test
	public void forWithAsyncResultUni() throws Exception {
		String template = "{@io.smallrye.mutiny.vertx.AsyncResultUni<java.util.List<org.acme.Item>> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.name}    \r\n" + //
				"{/for}";
		testDiagnosticsFor(template);
	}

	@Test
	public void completionStagePOJO() throws Exception {
		String template = "{@org.acme.CompletionStagePOJO obj}\r\n"
				+ "\r\n"
				+ "{#for stringElt in obj.myStrings}\r\n"
				+ "    {stringElt.isEmpty()}\r\n"
				+ "{/for}\r\n"
				+ "\r\n"
				+ "{obj.myStrings.get(0)}";
		testDiagnosticsFor(template);
	}
	
}