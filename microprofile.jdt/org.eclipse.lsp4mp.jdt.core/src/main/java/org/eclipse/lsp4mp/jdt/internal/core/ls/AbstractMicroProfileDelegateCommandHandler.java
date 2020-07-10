/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.jdt.internal.core.ls;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.lsp4mp.jdt.core.IMicroProfilePropertiesChangedListener;
import org.eclipse.lsp4mp.jdt.internal.core.MicroProfilePropertiesListenerManager;

/**
 * Abstract class for MicroProfile JDT LS command handler
 * 
 * @author Angelo ZERR
 *
 */
public abstract class AbstractMicroProfileDelegateCommandHandler implements IDelegateCommandHandler {

	private static final Logger LOGGER = Logger.getLogger(AbstractMicroProfileDelegateCommandHandler.class.getName());

	/**
	 * MicroProfile client commands
	 */
	private static final String MICROPROFILE_PROPERTIES_CHANGED_COMMAND = "microprofile/propertiesChanged";

	private static final IMicroProfilePropertiesChangedListener LISTENER = (event) -> {
		try {
			// Execute client command with a timeout of 5 seconds to avoid blocking jobs.
			JavaLanguageServerPlugin.getInstance().getClientConnection().executeClientCommand(
					Duration.of(5, ChronoUnit.SECONDS), MICROPROFILE_PROPERTIES_CHANGED_COMMAND, event);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while sending 'microprofile/propertiesChanged' event to the client", e);
		}
	};

	private static boolean initialized;

	public AbstractMicroProfileDelegateCommandHandler() {
		initialize();
	}

	/**
	 * Add MicroProfile properties changed listener if needed.
	 */
	private static synchronized void initialize() {
		if (initialized) {
			return;
		}
		// Add a classpath changed listener to execute client command
		// "microprofile/propertiesChanged"
		MicroProfilePropertiesListenerManager.getInstance().addMicroProfilePropertiesChangedListener(LISTENER);
		initialized = true;
	}
}
