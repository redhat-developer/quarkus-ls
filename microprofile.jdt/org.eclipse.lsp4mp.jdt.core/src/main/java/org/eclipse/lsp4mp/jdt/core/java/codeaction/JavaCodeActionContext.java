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
package org.eclipse.lsp4mp.jdt.core.java.codeaction;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.internal.core.manipulation.dom.ASTResolving;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4mp.commons.MicroProfileJavaCodeActionParams;
import org.eclipse.lsp4mp.jdt.core.java.AbtractJavaContext;
import org.eclipse.lsp4mp.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.internal.core.java.ChangeUtil;

/**
 * Java codeAction context for a given compilation unit.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaCodeActionContext extends AbtractJavaContext implements IInvocationContext {

	private final int selectionOffset;
	private final int selectionLength;

	private final MicroProfileJavaCodeActionParams params;
	private NodeFinder fNodeFinder;
	private CompilationUnit fASTRoot;

	public JavaCodeActionContext(ITypeRoot typeRoot, int selectionOffset, int selectionLength, IJDTUtils utils,
			MicroProfileJavaCodeActionParams params) {
		super(params.getUri(), typeRoot, utils);
		this.selectionOffset = selectionOffset;
		this.selectionLength = selectionLength;
		this.params = params;
	}

	public MicroProfileJavaCodeActionParams getParams() {
		return params;
	}

	@Override
	public ICompilationUnit getCompilationUnit() {
		return (ICompilationUnit) getTypeRoot();
	}

	/**
	 * Returns the length.
	 *
	 * @return int
	 */
	@Override
	public int getSelectionLength() {
		return selectionLength;
	}

	/**
	 * Returns the offset.
	 *
	 * @return int
	 */
	@Override
	public int getSelectionOffset() {
		return selectionOffset;
	}

	@Override
	public CompilationUnit getASTRoot() {
		if (fASTRoot == null) {
			fASTRoot = ASTResolving.createQuickFixAST(getCompilationUnit(), null);
		}
		return fASTRoot;
	}

	/**
	 * @param root The ASTRoot to set.
	 */
	public void setASTRoot(CompilationUnit root) {
		fASTRoot = root;
	}

	@Override
	public ASTNode getCoveringNode() {
		if (fNodeFinder == null) {
			fNodeFinder = new NodeFinder(getASTRoot(), selectionOffset, selectionLength);
		}
		return fNodeFinder.getCoveringNode();
	}

	@Override
	public ASTNode getCoveredNode() {
		if (fNodeFinder == null) {
			fNodeFinder = new NodeFinder(getASTRoot(), selectionOffset, selectionLength);
		}
		return fNodeFinder.getCoveredNode();
	}

	public CodeAction convertToCodeAction(ChangeCorrectionProposal proposal, Diagnostic... diagnostics)
			throws CoreException {
		String name = proposal.getName();
		WorkspaceEdit edit = ChangeUtil.convertToWorkspaceEdit(proposal.getChange(), getUri(), getUtils(),
				params.isResourceOperationSupported());
		if (!ChangeUtil.hasChanges(edit)) {
			return null;
		}
		ExtendedCodeAction codeAction = new ExtendedCodeAction(name);
		codeAction.setRelevance(proposal.getRelevance());
		codeAction.setKind(proposal.getKind());
		codeAction.setEdit(edit);
		codeAction.setDiagnostics(Arrays.asList(diagnostics));
		return codeAction;
	}

}
