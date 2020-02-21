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
package com.redhat.microprofile.jdt.core.health;

import static com.redhat.microprofile.jdt.internal.core.java.MicroProfileForJavaAssert.assertJavaDiagnostics;
import static com.redhat.microprofile.jdt.internal.core.java.MicroProfileForJavaAssert.d;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.Test;

import com.redhat.microprofile.commons.DocumentFormat;
import com.redhat.microprofile.commons.MicroProfileJavaDiagnosticsParams;
import com.redhat.microprofile.jdt.core.BasePropertiesManagerTest;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;
import com.redhat.microprofile.jdt.internal.core.ls.JDTUtilsLSImpl;
import com.redhat.microprofile.jdt.internal.health.MicroProfileHealthConstants;
import com.redhat.microprofile.jdt.internal.health.java.MicroProfileHealthErrorCode;

/**
 * Java diagnostics for MicroProfile Health.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaDiagnosticsMicroProfileHealthTest extends BasePropertiesManagerTest {

	@Test
	public void restClientDiagnostics() throws Exception {
		IJavaProject javaProject = loadMavenProject(MavenProjectName.microprofile_health_quickstart);
		IJDTUtils utils = JDTUtilsLSImpl.getInstance();

		MicroProfileJavaDiagnosticsParams params = new MicroProfileJavaDiagnosticsParams();
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/health/DontImplementHealthCheck.java"));
		params.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
		params.setDocumentFormat(DocumentFormat.Markdown);

		assertJavaDiagnostics(params, utils, //
				d(10, 13, 37,
						"The class `org.acme.health.DontImplementHealthCheck` using the @Liveness, @Readiness, or @Health annotation should implement the HealthCheck interface.",
						DiagnosticSeverity.Warning, MicroProfileHealthConstants.DIAGNOSTIC_SOURCE,
						MicroProfileHealthErrorCode.ImplementHealthCheck));

		params = new MicroProfileJavaDiagnosticsParams();
		javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/health/ImplementHealthCheck.java"));
		params.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
		params.setDocumentFormat(DocumentFormat.Markdown);

		assertJavaDiagnostics(params, utils, //
				d(5, 13, 33,
						"The class `org.acme.health.ImplementHealthCheck` implementing the HealthCheck interface should use the @Liveness, @Readiness, or @Health annotation.",
						DiagnosticSeverity.Warning, MicroProfileHealthConstants.DIAGNOSTIC_SOURCE,
						MicroProfileHealthErrorCode.HealthAnnotationMissing));

	}
}
