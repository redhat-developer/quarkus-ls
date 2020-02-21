/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.core.java;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;

import com.redhat.microprofile.jdt.core.java.IJavaDiagnosticsParticipant;
import com.redhat.microprofile.jdt.core.java.JavaDiagnosticsContext;

/**
 * Wrapper class around {@link IJavaDiagnosticsParticipant}.
 *
 */
public class JavaFeatureDefinition implements IJavaDiagnosticsParticipant {
	private static final Logger LOGGER = Logger.getLogger(JavaFeatureDefinition.class.getName());
	private final IJavaDiagnosticsParticipant collector;

	public JavaFeatureDefinition(IJavaDiagnosticsParticipant collector) {
		this.collector = collector;
	}

	@Override
	public void collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) {
		if (collector == null) {
			return;
		}
		try {
			if (collector.isAdaptedForDiagnostics(context, monitor)) {
				collector.collectDiagnostics(context, monitor);
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while collecting diagnostics", e);
		}
	}
}
