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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Hover;

import com.redhat.microprofile.jdt.core.java.IJavaCodeLensParticipant;
import com.redhat.microprofile.jdt.core.java.IJavaDiagnosticsParticipant;
import com.redhat.microprofile.jdt.core.java.IJavaHoverParticipant;
import com.redhat.microprofile.jdt.core.java.JavaCodeLensContext;
import com.redhat.microprofile.jdt.core.java.JavaDiagnosticsContext;
import com.redhat.microprofile.jdt.core.java.JavaHoverContext;

/**
 * Wrapper class around java participants :
 * 
 * <ul>
 * <li>{@link IJavaHoverParticipant}.</li>
 * <li>{@link IJavaCodeLensParticipant}.</li>
 * <li>{@link IJavaDiagnosticsParticipant}.</li>
 * </ul>
 *
 */
public class JavaFeatureDefinition
		implements IJavaHoverParticipant, IJavaCodeLensParticipant, IJavaDiagnosticsParticipant {
	private static final Logger LOGGER = Logger.getLogger(JavaFeatureDefinition.class.getName());

	private final IJavaCodeLensParticipant codeLensParticipant;
	private final IJavaDiagnosticsParticipant diagnosticsParticipant;
	private final IJavaHoverParticipant hoverParticipant;

	public JavaFeatureDefinition(IJavaCodeLensParticipant codeLensParticipant) {
		this(codeLensParticipant, null, null);
	}

	public JavaFeatureDefinition(IJavaDiagnosticsParticipant diagnosticsParticipant) {
		this(null, diagnosticsParticipant, null);
	}

	public JavaFeatureDefinition(IJavaHoverParticipant hoverParticipant) {
		this(null, null, hoverParticipant);
	}

	private JavaFeatureDefinition(IJavaCodeLensParticipant codeLensParticipant,
			IJavaDiagnosticsParticipant diagnosticsParticipant, IJavaHoverParticipant hoverParticipant) {
		this.codeLensParticipant = codeLensParticipant;
		this.diagnosticsParticipant = diagnosticsParticipant;
		this.hoverParticipant = hoverParticipant;
	}

	// -------------- CodeLens

	@Override
	public boolean isAdaptedForCodeLens(JavaCodeLensContext context, IProgressMonitor monitor) {
		if (codeLensParticipant == null) {
			return false;
		}
		try {
			return codeLensParticipant.isAdaptedForCodeLens(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while calling isAdaptedForCodeLens", e);
			return false;
		}
	}

	@Override
	public void beginCodeLens(JavaCodeLensContext context, IProgressMonitor monitor) {
		try {
			codeLensParticipant.beginCodeLens(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while calling beginCodeLens", e);
		}
	}

	@Override
	public List<CodeLens> collectCodeLens(JavaCodeLensContext context, IProgressMonitor monitor) {
		try {
			return codeLensParticipant.collectCodeLens(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while collecting codeLens", e);
			return null;
		}
	}

	@Override
	public void endCodeLens(JavaCodeLensContext context, IProgressMonitor monitor) {
		try {
			codeLensParticipant.endCodeLens(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while calling endCodeLens", e);
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
	public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) {
		try {
			return diagnosticsParticipant.collectDiagnostics(context, monitor);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while collecting diagnostics", e);
			return null;
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

}
