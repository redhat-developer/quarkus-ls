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
package com.redhat.microprofile.jdt.core.openapi;

import static com.redhat.microprofile.jdt.internal.core.java.MicroProfileForJavaAssert.assertJavaCodeAction;
import static com.redhat.microprofile.jdt.internal.core.java.MicroProfileForJavaAssert.ca;
import static com.redhat.microprofile.jdt.internal.core.java.MicroProfileForJavaAssert.createCodeActionParams;
import static com.redhat.microprofile.jdt.internal.core.java.MicroProfileForJavaAssert.te;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.Test;

import com.redhat.microprofile.commons.MicroProfileJavaCodeActionParams;
import com.redhat.microprofile.jdt.core.BasePropertiesManagerTest;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;

/**
 * Code action for generating MicroProfile OpenAPI annotations.
 * 
 * @author Benson Ning
 *
 */
public class GenerateOpenAPIAnnotationsTest extends BasePropertiesManagerTest {
	
	@Test
	public void GenerateOpenAPIAnnotationsAction() throws Exception {
		IJavaProject javaProject = loadMavenProject(MavenProjectName.microprofile_openapi);
		IJDTUtils utils = JDT_UTILS;

		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/openapi/NoOperationAnnotation.java"));
		String uri = javaFile.getLocation().toFile().toURI().toString();
		Diagnostic d = new Diagnostic();
		Position start = new Position(8, 23);
		d.setRange(new Range(start, start));
		MicroProfileJavaCodeActionParams codeActionParams = createCodeActionParams(uri, d);
		
		String newText = "\n\nimport org.eclipse.microprofile.openapi.annotations.Operation;" + 
						 "\n\n@RequestScoped\n@Path(\"/systems\")\npublic class NoOperationAnnotation {" + 
						 "\n\n\t@Operation(summary = \"\", description = \"\")\n\t@GET\n" + 
						 "\tpublic Response getMyInformation(String hostname) {\n\t\treturn " + 
						 "Response.ok(listContents()).build();\n\t}\n\n\t@Operation(summary = \"\", " + 
						 "description = \"\")\n\t";
		assertJavaCodeAction(codeActionParams, utils, 
				ca(uri, "Generate OpenAPI Annotations", d, 
						te(6, 33, 17, 1, newText))
		);
	}

}
