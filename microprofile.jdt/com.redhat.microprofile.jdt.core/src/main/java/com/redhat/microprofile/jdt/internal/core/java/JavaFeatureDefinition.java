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
package com.redhat.microprofile.jdt.internal.core.java;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.lsp4j.Hover;

import com.redhat.microprofile.jdt.core.java.IJavaDiagnosticsParticipant;
import com.redhat.microprofile.jdt.core.java.IJavaHoverParticipant;
import com.redhat.microprofile.jdt.core.java.JavaDiagnosticsContext;
import com.redhat.microprofile.jdt.core.java.JavaHoverContext;

/**
 * Wrapper class around java participants :
 * 
 * <ul>
 * <li>{@link IJavaHoverParticipant}.</li>
 * <li>{@link IJavaDiagnosticsParticipant}.</li>
 * </ul>
 *
 */
public class JavaFeatureDefinition implements IJavaHoverParticipant, IJavaDiagnosticsParticipant {
	private static final Logger LOGGER = Logger.getLogger(JavaFeatureDefinition.class.getName());

	private final IJavaHoverParticipant hoverParticipant;
	private final IJavaDiagnosticsParticipant diagnosticsParticipant;

	public JavaFeatureDefinition(IJavaHoverParticipant hoverParticipant,
			IJavaDiagnosticsParticipant diagnosticsParticipant) {
		this.hoverParticipant = hoverParticipant;
		this.diagnosticsParticipant = diagnosticsParticipant;
	}

	// -------------- Hover

	@Override
	public boolean isAdaptedForHover(JavaHoverContext context, IProgressMonitor monitor) {
		if (hoverParticipant == null) {
			return false;
		}
		try {
			return hoverParticipant.isAdaptedForHover(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while calling isAdaptedForHover", e);
			return false;
		}
	}

	@Override
	public void beginHover(JavaHoverContext context, IProgressMonitor monitor) {
		try {
			hoverParticipant.beginHover(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while calling beginHover", e);
		}
	}

	@Override
	public Hover collectHover(JavaHoverContext context, IProgressMonitor monitor) {
		try {
			return hoverParticipant.collectHover(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while collecting hover", e);
			return null;
		}
	}

	@Override
	public void endHover(JavaHoverContext context, IProgressMonitor monitor) {
		try {
			hoverParticipant.endHover(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while calling endHover", e);
		}
	}

	// -------------- Diagnostics

	@Override
	public boolean isAdaptedForDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) {
		if (diagnosticsParticipant == null) {
			return false;
		}
		try {
			return diagnosticsParticipant.isAdaptedForDiagnostics(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while calling isAdaptedForDiagnostics", e);
			return false;
		}
	}

	@Override
	public void beginDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) {
		try {
			diagnosticsParticipant.beginDiagnostics(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while calling beginDiagnostics", e);
		}
	}

	@Override
	public void collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) {
		try {
			diagnosticsParticipant.collectDiagnostics(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while collecting diagnostics", e);
		}
	}

	@Override
	public void endDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) {
		try {
			diagnosticsParticipant.endDiagnostics(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while calling endDiagnostics", e);
		}
	}

}
