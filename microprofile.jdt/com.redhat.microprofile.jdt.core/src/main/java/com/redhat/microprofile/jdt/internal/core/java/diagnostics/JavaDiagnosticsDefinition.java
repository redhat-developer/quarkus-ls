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
package com.redhat.microprofile.jdt.internal.core.java.diagnostics;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.lsp4j.Diagnostic;

import com.redhat.microprofile.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import com.redhat.microprofile.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import com.redhat.microprofile.jdt.internal.core.java.AbstractJavaFeatureDefinition;

/**
 * Wrapper class around java participants {@link IJavaDiagnosticsParticipant}.
 */
public class JavaDiagnosticsDefinition extends AbstractJavaFeatureDefinition<IJavaDiagnosticsParticipant>
		implements IJavaDiagnosticsParticipant {
	private static final Logger LOGGER = Logger.getLogger(JavaDiagnosticsDefinition.class.getName());

	public JavaDiagnosticsDefinition(IConfigurationElement element) {
		super(element);
	}

	// -------------- Diagnostics

	@Override
	public boolean isAdaptedForDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) {
		try {
			return getParticipant().isAdaptedForDiagnostics(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while calling isAdaptedForDiagnostics", e);
			return false;
		}
	}

	@Override
	public void beginDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) {
		try {
			getParticipant().beginDiagnostics(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while calling beginDiagnostics", e);
		}
	}

	@Override
	public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) {
		try {
			return getParticipant().collectDiagnostics(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while collecting diagnostics", e);
			return null;
		}
	}

	@Override
	public void endDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) {
		try {
			getParticipant().endDiagnostics(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while calling endDiagnostics", e);
		}
	}

}
