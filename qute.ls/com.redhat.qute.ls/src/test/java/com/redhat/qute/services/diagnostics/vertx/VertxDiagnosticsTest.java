/*******************************************************************************
* Copyright (c) 2024 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.diagnostics.vertx;

import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.junit.jupiter.api.Test;

/**
 * Test diagnostics with Vertx extension.
 *
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#vertx_integration
 *
 */
public class VertxDiagnosticsTest {

	@Test
	public void noError() throws Exception {
		String template = "{@io.vertx.core.json.JsonObject tool}\r\n" + //
				"{tool.name}\r\n" + //
				"{tool.fieldNames}\r\n" + //
				"{tool.fields}\r\n" + //
				"{tool.size}\r\n" + //
				"{tool.empty}\r\n" + //
				"{tool.isEmpty}\r\n" + //
				"{tool.get('name')}\r\n" + //
				"{tool.containsKey('name')}";
		testDiagnosticsFor(template);
	}

}
