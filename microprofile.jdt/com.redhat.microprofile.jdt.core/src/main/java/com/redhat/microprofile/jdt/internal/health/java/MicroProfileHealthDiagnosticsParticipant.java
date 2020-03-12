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
package com.redhat.microprofile.jdt.internal.health.java;

import static com.redhat.microprofile.jdt.internal.health.MicroProfileHealthConstants.HEALTH_ANNOTATION;
import static com.redhat.microprofile.jdt.internal.health.MicroProfileHealthConstants.HEALTH_CHECK_INTERFACE_NAME;
import static com.redhat.microprofile.jdt.internal.health.MicroProfileHealthConstants.LIVENESS_ANNOTATION;
import static com.redhat.microprofile.jdt.internal.health.MicroProfileHealthConstants.READINESS_ANNOTATION;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;

import com.redhat.microprofile.commons.DocumentFormat;
import com.redhat.microprofile.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import com.redhat.microprofile.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import com.redhat.microprofile.jdt.core.utils.AnnotationUtils;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;
import com.redhat.microprofile.jdt.core.utils.JDTTypeUtils;
import com.redhat.microprofile.jdt.core.utils.PositionUtils;
import com.redhat.microprofile.jdt.internal.health.MicroProfileHealthConstants;

/**
 *
 * MicroProfile Health Diagnostics:
 * 
 * <ul>
 * <li>Diagnostic 1:display Health annotation diagnostic message if
 * Health/Liveness/Readiness annotation exists but HealthCheck interface is not
 * implemented</li>
 * <li>Diagnostic 2: display HealthCheck diagnostic message if HealthCheck
 * interface is implemented but Health/Liveness/Readiness annotation does not
 * exist</li>
 * 
 * </ul>
 * 
 * <p>
 * Those rules comes from
 * https://github.com/MicroShed/microprofile-language-server/blob/8f3401852d2b82310f49cd41ec043f5b541944a9/src/main/java/com/microprofile/lsp/internal/diagnostic/MicroProfileDiagnostic.java#L250
 * </p>
 * 
 * @author Angelo ZERR
 * 
 * @See https://github.com/eclipse/microprofile-health
 *
 */
public class MicroProfileHealthDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

	@Override
	public boolean isAdaptedForDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor)
			throws CoreException {
		// Collection of diagnostics for MicroProfile Health is done only if
		// microprofile-health is on the classpath
		IJavaProject javaProject = context.getJavaProject();
		return JDTTypeUtils.findType(javaProject, HEALTH_ANNOTATION) != null;
	}

	@Override
	public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor)
			throws CoreException {
		ITypeRoot typeRoot = context.getTypeRoot();
		IJavaElement[] elements = typeRoot.getChildren();
		List<Diagnostic> diagnostics = new ArrayList<>();
		collectDiagnostics(elements, diagnostics, context, monitor);
		return diagnostics;
	}

	private static void collectDiagnostics(IJavaElement[] elements, List<Diagnostic> diagnostics,
			JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {
		for (IJavaElement element : elements) {
			if (monitor.isCanceled()) {
				return;
			}
			if (element.getElementType() == IJavaElement.TYPE) {
				IType type = (IType) element;
				if (!type.isInterface()) {
					validateClassType(type, diagnostics, context, monitor);
				}
				continue;
			}
		}
	}

	private static void validateClassType(IType classType, List<Diagnostic> diagnostics, JavaDiagnosticsContext context,
			IProgressMonitor monitor) throws CoreException {
		String uri = context.getUri();
		IJDTUtils utils = context.getUtils();
		DocumentFormat documentFormat = context.getDocumentFormat();
		IType[] interfaces = findImplementedInterfaces(classType, monitor);
		boolean implementsHealthCheck = Stream.of(interfaces)
				.anyMatch(interfaceType -> HEALTH_CHECK_INTERFACE_NAME.equals(interfaceType.getElementName()));
		boolean hasOneOfHealthAnnotation = AnnotationUtils.hasAnnotation(classType, LIVENESS_ANNOTATION)
				|| AnnotationUtils.hasAnnotation(classType, READINESS_ANNOTATION)
				|| AnnotationUtils.hasAnnotation(classType, HEALTH_ANNOTATION);
		// Diagnostic 1:display Health annotation diagnostic message if
		// Health/Liveness/Readiness annotation exists but HealthCheck interface is not
		// implemented
		if (hasOneOfHealthAnnotation && !implementsHealthCheck) {
			Range healthCheckInterfaceRange = PositionUtils.toNameRange(classType, utils);
			Diagnostic d = context.createDiagnostic(uri, createDiagnostic1Message(classType, documentFormat),
					healthCheckInterfaceRange, MicroProfileHealthConstants.DIAGNOSTIC_SOURCE,
					MicroProfileHealthErrorCode.ImplementHealthCheck);
			diagnostics.add(d);
		}

		// Diagnostic 2: display HealthCheck diagnostic message if HealthCheck interface
		// is implemented but Health/Liveness/Readiness annotation does not exist
		if (implementsHealthCheck && !hasOneOfHealthAnnotation) {
			Range healthCheckInterfaceRange = PositionUtils.toNameRange(classType, utils);
			Diagnostic d = context.createDiagnostic(uri, createDiagnostic2Message(classType, documentFormat),
					healthCheckInterfaceRange, MicroProfileHealthConstants.DIAGNOSTIC_SOURCE,
					MicroProfileHealthErrorCode.HealthAnnotationMissing);
			diagnostics.add(d);
		}
	}

	private static String createDiagnostic1Message(IType classType, DocumentFormat documentFormat) {
		StringBuilder message = new StringBuilder("The class ");
		if (DocumentFormat.Markdown.equals(documentFormat)) {
			message.append("`");
		}
		message.append(classType.getFullyQualifiedName());
		if (DocumentFormat.Markdown.equals(documentFormat)) {
			message.append("`");
		}
		message.append(
				" using the @Liveness, @Readiness, or @Health annotation should implement the HealthCheck interface.");
		return message.toString();
	}

	private static String createDiagnostic2Message(IType classType, DocumentFormat documentFormat) {
		StringBuilder message = new StringBuilder("The class ");
		if (DocumentFormat.Markdown.equals(documentFormat)) {
			message.append("`");
		}
		message.append(classType.getFullyQualifiedName());
		if (DocumentFormat.Markdown.equals(documentFormat)) {
			message.append("`");
		}
		message.append(
				" implementing the HealthCheck interface should use the @Liveness, @Readiness, or @Health annotation.");
		return message.toString();
	}

	private static IType[] findImplementedInterfaces(IType type, IProgressMonitor progressMonitor)
			throws CoreException {
		ITypeHierarchy typeHierarchy = type.newSupertypeHierarchy(progressMonitor);
		return typeHierarchy.getRootInterfaces();
	}
}
