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
package com.redhat.qute.services.commands.validation;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.services.commands.ArgumentsUtils;
import com.redhat.qute.services.commands.IDelegateCommandHandler;
import com.redhat.qute.settings.QuteValidationSettings;
import com.redhat.qute.settings.SharedSettings;

/**
 * Returns the template validation status of a given Qute template file uri.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteTemplateValidationStatusCommandHandler implements IDelegateCommandHandler {

	public static final String COMMAND_ID = "qute.command.validation.template.status";

	@Override
	public CompletableFuture<Object> executeCommand(ExecuteCommandParams params, SharedSettings sharedSettings,
			CancelChecker cancelChecker) throws Exception {
		String fileUri = ArgumentsUtils.getArgAt(params, 0, String.class);

		TemplateValidationStatus status = new TemplateValidationStatus();
		QuteValidationSettings validationSettings = sharedSettings.getValidationSettings(fileUri);
		if (validationSettings == null) {
			status.setValidationEnabled(true);
			status.setExcluded(Collections.emptyList());
		} else {
			status.setValidationEnabled(validationSettings.isEnabled());
			status.setExcluded(validationSettings.getMatchingExcluded(fileUri));
		}

		return CompletableFuture.completedFuture(status);
	}

}
