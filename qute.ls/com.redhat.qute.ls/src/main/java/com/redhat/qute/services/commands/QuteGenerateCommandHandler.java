/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.commands;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.google.gson.Gson;
import com.redhat.qute.settings.SharedSettings;

import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;

public class QuteGenerateCommandHandler implements IDelegateCommandHandler {

	public static final String COMMAND_ID = "qute.generate";

	@Override
	public CompletableFuture<Object> executeCommand(ExecuteCommandParams params, SharedSettings sharedSettings,
			CancelChecker cancelChecker) throws Exception {
		String templateContent = ArgumentsUtils.getArgAt(params, 0, String.class);
		String data = ArgumentsUtils.getArgAt(params, 1, String.class);

		Map d = new Gson().fromJson(data, Map.class);

		Engine engine = Engine.builder().addDefaults().build();
		Template template = engine.parse(templateContent);
		String result = template.data(d).render();
		return CompletableFuture.completedFuture(result);
	}

}
