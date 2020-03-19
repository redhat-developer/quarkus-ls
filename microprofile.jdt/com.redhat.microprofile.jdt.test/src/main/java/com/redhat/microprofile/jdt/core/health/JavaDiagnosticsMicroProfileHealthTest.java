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

import static com.redhat.microprofile.jdt.internal.core.java.MicroProfileForJavaAssert.assertJavaCodeAction;
import static com.redhat.microprofile.jdt.internal.core.java.MicroProfileForJavaAssert.assertJavaDiagnostics;
import static com.redhat.microprofile.jdt.internal.core.java.MicroProfileForJavaAssert.ca;
import static com.redhat.microprofile.jdt.internal.core.java.MicroProfileForJavaAssert.createCodeActionParams;
import static com.redhat.microprofile.jdt.internal.core.java.MicroProfileForJavaAssert.d;
import static com.redhat.microprofile.jdt.internal.core.java.MicroProfileForJavaAssert.te;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.Test;

import com.redhat.microprofile.commons.DocumentFormat;
import com.redhat.microprofile.commons.MicroProfileJavaCodeActionParams;
import com.redhat.microprofile.commons.MicroProfileJavaDiagnosticsParams;
import com.redhat.microprofile.jdt.core.BasePropertiesManagerTest;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;
import com.redhat.microprofile.jdt.internal.health.MicroProfileHealthConstants;
import com.redhat.microprofile.jdt.internal.health.java.MicroProfileHealthErrorCode;

/**
 * Java diagnostics and code action for MicroProfile Health.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaDiagnosticsMicroProfileHealthTest extends BasePropertiesManagerTest {

	@Test
	public void ImplementHealthCheck() throws Exception {
		IJavaProject javaProject = loadMavenProject(MavenProjectName.microprofile_health_quickstart);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaDiagnosticsParams diagnosticsParams = new MicroProfileJavaDiagnosticsParams();
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/health/DontImplementHealthCheck.java"));
		diagnosticsParams.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
		diagnosticsParams.setDocumentFormat(DocumentFormat.Markdown);

		Diagnostic d = d(9, 13, 37,
				"The class `org.acme.health.DontImplementHealthCheck` using the @Liveness, @Readiness, or @Health annotation should implement the HealthCheck interface.",
				DiagnosticSeverity.Warning, MicroProfileHealthConstants.DIAGNOSTIC_SOURCE,
				MicroProfileHealthErrorCode.ImplementHealthCheck);
		assertJavaDiagnostics(diagnosticsParams, utils, //
				d);

		String uri = javaFile.getLocation().toFile().toURI().toString();
		MicroProfileJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d);
		assertJavaCodeAction(codeActionParams, utils, //
				ca(uri, "Let 'DontImplementHealthCheck' implement 'org.eclipse.microprofile.health.HealthCheck'", d, //
						te(2, 50, 9, 37, "\r\n\r\n" + //
								"import org.eclipse.microprofile.health.HealthCheck;\r\n" + //
								"import org.eclipse.microprofile.health.HealthCheckResponse;\r\n" + //
								"import org.eclipse.microprofile.health.Liveness;\r\n\r\n@Liveness\r\n" + //
								"@ApplicationScoped\r\n" + //
								"public class DontImplementHealthCheck implements HealthCheck")));
	}

	@Test
	public void HealthAnnotationMissing() throws Exception {
		IJavaProject javaProject = loadMavenProject(MavenProjectName.microprofile_health_quickstart);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaDiagnosticsParams diagnosticsParams = new MicroProfileJavaDiagnosticsParams();
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/health/ImplementHealthCheck.java"));
		diagnosticsParams.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
		diagnosticsParams.setDocumentFormat(DocumentFormat.Markdown);

		Diagnostic d = d(5, 13, 33,
				"The class `org.acme.health.ImplementHealthCheck` implementing the HealthCheck interface should use the @Liveness, @Readiness, or @Health annotation.",
				DiagnosticSeverity.Warning, MicroProfileHealthConstants.DIAGNOSTIC_SOURCE,
				MicroProfileHealthErrorCode.HealthAnnotationMissing);
		assertJavaDiagnostics(diagnosticsParams, utils, //
				d);

		String uri = javaFile.getLocation().toFile().toURI().toString();
		MicroProfileJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d);
		assertJavaCodeAction(codeActionParams, utils, //
				ca(uri, "Insert @Health", d, //
						te(2, 0, 5, 0, "import org.eclipse.microprofile.health.Health;\r\n" + //
								"import org.eclipse.microprofile.health.HealthCheck;\r\n" + //
								"import org.eclipse.microprofile.health.HealthCheckResponse;\r\n\r\n" + //
								"@Health\r\n")),
				ca(uri, "Insert @Liveness", d, //
						te(3, 59, 5, 0, "\r\n" + //
								"import org.eclipse.microprofile.health.Liveness;\r\n\r\n" + //
								"@Liveness\r\n")), //
				ca(uri, "Insert @Readiness", d, //
						te(3, 59, 5, 0, "\r\n" + //
								"import org.eclipse.microprofile.health.Readiness;\r\n\r\n" + //
								"@Readiness\r\n")) //
		);
	}

}
