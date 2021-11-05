/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.quarkus.configmapping;

import static com.redhat.microprofile.jdt.internal.quarkus.QuarkusConstants.QUARKUS_PREFIX;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.assertJavaDiagnostics;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.d;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4mp.commons.DocumentFormat;
import org.eclipse.lsp4mp.commons.MicroProfileJavaDiagnosticsParams;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.junit.Test;

import com.redhat.microprofile.jdt.quarkus.QuarkusMavenProjectName;

/**
 * @ConfigMapping validation.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusConfigMappingASTVisitorTest extends BasePropertiesManagerTest {

	@Test
	public void expectedInterface() throws Exception {

		IJavaProject javaProject = loadMavenProject(QuarkusMavenProjectName.config_mapping);
		IJDTUtils utils = JDT_UTILS;
		MicroProfileJavaDiagnosticsParams diagnosticsParams = new MicroProfileJavaDiagnosticsParams();

		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/validation/ServerClass.java"));
		diagnosticsParams.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));
		diagnosticsParams.setDocumentFormat(DocumentFormat.Markdown);
		assertJavaDiagnostics(diagnosticsParams, utils, //
				d(4, 0, 24,
						"The @ConfigMapping annotation can only be placed in interfaces, class `ServerClass` is a class",
						DiagnosticSeverity.Error, QUARKUS_PREFIX, null));
	}
}
