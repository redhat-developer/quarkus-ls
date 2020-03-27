/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copied from /org.eclipse.jdt.ui/src/org/eclipse/jdt/internal/ui/text/correction/proposals/NewAnnotationMemberProposal.java
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.redhat.microprofile.jdt.core.java.corrections.proposal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.lsp4j.CodeActionKind;

/**
 * A proposal for generating OpenAPI annotations that works on an AST rewrite.  
 *  
 * @author Benson Ning
 *
 */
public class OpenAPIAnnotationProposal extends ASTRewriteCorrectionProposal {

	private final CompilationUnit fInvocationNode;
	private final TypeDeclaration fTypeNode;
	private final String fAnnotation;
	
	public OpenAPIAnnotationProposal(String label, ICompilationUnit targetCU, CompilationUnit invocationNode, 
			TypeDeclaration type, String annotation, int relevance) {
		super(label, CodeActionKind.Source, targetCU, null, relevance);
		this.fInvocationNode = invocationNode;
		this.fTypeNode = type;
		this.fAnnotation = annotation;
	}
	
	@Override
	protected ASTRewrite getRewrite() throws CoreException {
		MethodDeclaration methods[] = this.fTypeNode.getMethods();
		List<MethodDeclaration> responseReturnMethods = new ArrayList<>();
		for (MethodDeclaration method : methods) {
			boolean operationFlag = false;
			if (method.getReturnType2().resolveBinding().getQualifiedName().equals("javax.ws.rs.core.Response")) {
				List<?> modifiers = method.modifiers();
				for (Iterator<?> iter = modifiers.iterator(); iter.hasNext();) {
					Object next = iter.next();
					if (next instanceof IExtendedModifier) {
						IExtendedModifier modifier = (IExtendedModifier) next;
						if (modifier.isAnnotation()) {
							Annotation annotation = (Annotation) modifier;
							if (annotation.resolveTypeBinding().getQualifiedName()
									.equals("org.eclipse.microprofile.openapi.annotations.Operation")) {
								operationFlag = true;
								break;
							}
						}
					}
				}
	
				if (!operationFlag) {
					responseReturnMethods.add(method);
				}	
			}
		}
		
		if (!responseReturnMethods.isEmpty()) {
			AST ast = fTypeNode.getAST();
			ASTRewrite rewrite = ASTRewrite.create(ast);
			
			ImportRewrite imports = createImportRewrite(this.fInvocationNode);
			ImportRewriteContext importRewriteContext= new ContextSensitiveImportRewriteContext(this.fInvocationNode, imports);
			
			NormalAnnotation marker = ast.newNormalAnnotation(); //newMarkerAnnotation();
			marker.setTypeName(ast.newName(imports.addImport(fAnnotation, importRewriteContext))); //$NON-NLS-1$
			List<MemberValuePair> values = marker.values();
			// "summary" parameter
			MemberValuePair memberValuePair = ast.newMemberValuePair();
			memberValuePair.setName(ast.newSimpleName("summary"));
			StringLiteral stringValue = ast.newStringLiteral();
			stringValue.setLiteralValue("");
			memberValuePair.setValue(stringValue);
			values.add(memberValuePair);
			// "description" parameter
			memberValuePair = ast.newMemberValuePair();
			memberValuePair.setName(ast.newSimpleName("description"));
			stringValue = ast.newStringLiteral();
			stringValue.setLiteralValue("");
			memberValuePair.setValue(stringValue);
			values.add(memberValuePair);			
						
			for (MethodDeclaration method : responseReturnMethods) {
				rewrite.getListRewrite(method, MethodDeclaration.MODIFIERS2_PROPERTY).insertFirst(marker, null);
			}			
			return rewrite;
		}
		
		return null;
	}
}
