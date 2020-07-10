/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copied from /org.eclipse.jdt.ui/src/org/eclipse/jdt/ui/text/java/correction/CUCorrectionProposal.java
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.lsp4mp.jdt.core.java.corrections.proposal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.manipulation.CUCorrectionProposalCore;
import org.eclipse.jdt.core.manipulation.ICUCorrectionProposal;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.text.edits.TextEdit;



/**
 * A proposal for quick fixes and quick assists that work on a single compilation unit. Either a
 * {@link TextChange text change} is directly passed in the constructor or method
 * {@link #addEdits(IDocument, TextEdit)} is overridden to provide the text edits that are applied
 * to the document when the proposal is evaluated.
 * <p>
 * The proposal takes care of the preview of the changes as proposal information.
 * </p>
 */
public class CUCorrectionProposal extends ChangeCorrectionProposal implements ICUCorrectionProposal {

	private CUCorrectionProposalCore fProposalCore;

	/**
	 * Constructs a correction proposal working on a compilation unit with a given
	 * text change.
	 *
	 * @param name
	 *            the name that is displayed in the proposal selection dialog
	 * @param kind
	 *            the kind of the correction type that the proposal performs
	 * @param cu
	 *            the compilation unit to which the change can be applied
	 * @param change
	 *            the change that is executed when the proposal is applied or
	 *            <code>null</code> if implementors override
	 *            {@link #addEdits(IDocument, TextEdit)} to provide the text edits
	 *            or {@link #createTextChange()} to provide a text change
	 * @param relevance
	 *            the relevance of this proposal
	 */
	public CUCorrectionProposal(String name, String kind, ICompilationUnit cu, TextChange change, int relevance) {
		super(name, kind, change, relevance);
		if (cu == null) {
			throw new IllegalArgumentException("Compilation unit must not be null"); //$NON-NLS-1$
		}
		fProposalCore = new CUCorrectionProposalCore(name, cu, change, relevance);
	}

	/**
	 * Called when the {@link CompilationUnitChange} is initialized. Subclasses can override to add
	 * text edits to the root edit of the change. Implementors must not access the proposal, e.g.
	 * not call {@link #getChange()}.
	 * <p>
	 * The default implementation does not add any edits
	 * </p>
	 *
	 * @param document content of the underlying compilation unit. To be accessed read only.
	 * @param editRoot The root edit to add all edits to
	 * @throws CoreException can be thrown if adding the edits is failing.
	 */
	protected void addEdits(IDocument document, TextEdit editRoot) throws CoreException {
		// empty default implementation
	}

	@Override
	public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
		return fProposalCore.getAdditionalProposalInfo(monitor);
	}

	@Override
	public void apply() throws CoreException {
		performChange();
	}

	/**
	 * Creates the text change for this proposal. This method is only called once
	 * and only when no text change has been passed in
	 * {@link #CUCorrectionProposal(String, ICompilationUnit, TextChange, int)}.
	 *
	 * @return the created text change
	 * @throws CoreException
	 *             if the creation of the text change failed
	 */
	protected TextChange createTextChange() throws CoreException {
		TextChange change = fProposalCore.getNewChange();
		// initialize text change
		IDocument document= change.getCurrentDocument(new NullProgressMonitor());
		addEdits(document, change.getEdit());
		return change;
	}

	@Override
	protected final Change createChange() throws CoreException {
		return createTextChange(); // make sure that only text changes are allowed here
	}

	/**
	 * Returns the text change that is invoked when the change is applied.
	 *
	 * @return the text change that is invoked when the change is applied
	 * @throws CoreException if accessing the change failed
	 */
	@Override
	public final TextChange getTextChange() throws CoreException {
		return (TextChange) getChange();
	}

	/**
	 * The compilation unit on which the change works.
	 *
	 * @return the compilation unit on which the change works
	 */
	public final ICompilationUnit getCompilationUnit() {
		return fProposalCore.getCompilationUnit();
	}

	/**
	 * Creates a preview of the content of the compilation unit after applying the change.
	 *
	 * @return the preview of the changed compilation unit
	 * @throws CoreException if the creation of the change failed
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public String getPreviewContent() throws CoreException {
		return getTextChange().getPreviewContent(new NullProgressMonitor());
	}

	@Override
	public String toString() {
		try {
			return getPreviewContent();
		} catch (CoreException e) {
			// didn't work out
		}
		return super.toString();
	}

}
