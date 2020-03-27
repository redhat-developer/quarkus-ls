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
import static com.redhat.microprofile.jdt.internal.core.java.MicroProfileForJavaAssert.te;

import java.util.Collections;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextEdit;
import org.junit.Test;

import com.redhat.microprofile.commons.MicroProfileJavaCodeActionParams;
import com.redhat.microprofile.jdt.core.BasePropertiesManagerTest;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;

/**
 * Java diagnostics and code action for MicroProfile Health.
 * 
 * @author
 *
 */
public class GenerateOpenAPIAnnotationsTest extends BasePropertiesManagerTest {
	
	private static final Logger LOGGER = Logger.getLogger(GenerateOpenAPIAnnotationsTest.class.getSimpleName());

	@Test
	public void GenerateOpenAPIAnnotationsAction() throws Exception {
		IJavaProject javaProject = loadMavenProject(MavenProjectName.microprofile_openapi);
		IJDTUtils utils = JDT_UTILS;

		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/openapi/NoOperationAnnotation.java"));
		String uri = javaFile.getLocation().toFile().toURI().toString();
		MicroProfileJavaCodeActionParams codeActionParams = createCodeActionParams(uri);
		
		String newText = "\r\n\r\nimport org.eclipse.microprofile.openapi.annotations.Operation;" + 
						 "\r\n\r\n@RequestScoped\r\n@Path(\"/systems\")\r\npublic class NoOperationAnnotation {" + 
						 "\r\n\r\n\t@Operation(summary = \"\", description = \"\")\r\n\t@GET\r\n" + 
						 "\tpublic Response getMyInformation(String hostname) {\r\n\t\treturn " + 
						 "Response.ok(listContents()).build();\r\n\t}\r\n\r\n\t@Operation(summary = \"\", " + 
						 "description = \"\")\r\n\t";
		assertJavaCodeAction(codeActionParams, utils, 
				ca(uri, "Generate OpenAPI Annotations", new Diagnostic(), 
						te(6, 33, 17, 1, newText))
		);
	}

	private MicroProfileJavaCodeActionParams createCodeActionParams(String uri) {
		TextDocumentIdentifier textDocument = new TextDocumentIdentifier(uri);
		Position start = new Position(8, 6);
		Range range = new Range(start, start);
		CodeActionContext context = new CodeActionContext();
		context.setDiagnostics(Collections.emptyList());
		MicroProfileJavaCodeActionParams codeActionParams = new MicroProfileJavaCodeActionParams(textDocument, range,
				context);
		codeActionParams.setResourceOperationSupported(true);
		return codeActionParams;
	}
		
	public boolean teEquals(TextEdit te1, TextEdit te2) {
	    if (te1 == te2)
	      return true;
	 
	    LOGGER.info("      =================>>>>>> teEquals1: ");   
	    
	    if (te1.getClass() != te2.getClass())
	      return false; 
	    
	    LOGGER.info("      =================>>>>>> teEquals2: " + te1.getRange().equals(te2.getRange()));
	    LOGGER.info("         =================>>>>>> teEquals2.1: " + te1.getRange());
	    LOGGER.info("         =================>>>>>> teEquals2.2: " + te2.getRange());
	    
	    if (te1.getRange() == null) {
	      if (te2.getRange() != null)
	        return false;
	    } else if (!te1.getRange().equals(te2.getRange()))
	      return false;
	    
	    LOGGER.info("      =================>>>>>> teEquals3: ");    
	    
	    if (te1.getNewText() == null) {
	      if (te2.getNewText() != null)
	        return false;
	    } else if (!te1.getNewText().equals(te2.getNewText()))
	      return false;
	    return true;
  }
}
