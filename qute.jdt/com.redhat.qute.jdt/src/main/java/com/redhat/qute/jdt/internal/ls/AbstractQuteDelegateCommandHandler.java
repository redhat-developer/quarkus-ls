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
package com.redhat.qute.jdt.internal.ls;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;

import com.redhat.qute.jdt.IJavaDataModelChangedListener;
import com.redhat.qute.jdt.internal.JavaDataModelListenerManager;

/**
 * Abstract class for Qute JDT LS command handler
 *
 * @author Angelo ZERR
 *
 */
public abstract class AbstractQuteDelegateCommandHandler implements IDelegateCommandHandler {

	private static final Logger LOGGER = Logger.getLogger(AbstractQuteDelegateCommandHandler.class.getName());

	/**
	 * Qute client commands
	 */
	private static final String DATA_MODEL_CHANGED_COMMAND = "qute/dataModelChanged";

	private static final IJavaDataModelChangedListener LISTENER = (event) -> {
		try {
			// Execute client command with a timeout of 5 seconds to avoid blocking jobs.
			JavaLanguageServerPlugin.getInstance().getClientConnection()
					.executeClientCommand(Duration.of(5, ChronoUnit.SECONDS), DATA_MODEL_CHANGED_COMMAND, event);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,
					"Error while sending '" + DATA_MODEL_CHANGED_COMMAND + "' event to the client", e);
		}
	};

	private static boolean initialized;

	public AbstractQuteDelegateCommandHandler() {
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
		// "qute/javaDataModelChanged"
		JavaDataModelListenerManager.getInstance().addJavaDataModelChangedListener(LISTENER);
		initialized = true;
	}
}
