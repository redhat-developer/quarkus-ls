/*******************************************************************************
 * Copyright (c) 2016-2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.lsp4mp.jdt.internal.core.java;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.util.SimpleDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.text.edits.CopySourceEdit;
import org.eclipse.text.edits.CopyTargetEdit;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.ISourceModifier;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MoveSourceEdit;
import org.eclipse.text.edits.MoveTargetEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditVisitor;

/**
 * Converts an {@link org.eclipse.text.edits.TextEdit} to
 * {@link org.eclipse.lsp4j.TextEdit}
 *
 * @author Gorkem Ercan
 *
 */
public class TextEditConverter extends TextEditVisitor {

	private static final Logger LOGGER = Logger.getLogger(TextEditConverter.class.getName());

	private final TextEdit source;
	protected ICompilationUnit compilationUnit;
	protected List<org.eclipse.lsp4j.TextEdit> converted;

	private final String uri;

	private final IJDTUtils utils;

	public TextEditConverter(ICompilationUnit unit, TextEdit edit, String uri, IJDTUtils utils) {
		this.source = edit;
		this.converted = new ArrayList<>();
		if (unit == null) {
			throw new IllegalArgumentException("Compilation unit can not be null");
		}
		this.compilationUnit = unit;
		this.uri = uri;
		this.utils = utils;
	}

	public List<org.eclipse.lsp4j.TextEdit> convert() {
		if (this.source != null) {
			this.source.accept(this);
		}
		return converted;
	}

	public TextDocumentEdit convertToTextDocumentEdit(int version) {
		VersionedTextDocumentIdentifier identifier = new VersionedTextDocumentIdentifier(version);
		identifier.setUri(uri);
		return new TextDocumentEdit(identifier, this.convert());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.text.edits.TextEditVisitor#visit(org.eclipse.text.edits.
	 * InsertEdit)
	 */
	@Override
	public boolean visit(InsertEdit edit) {
		try {
			org.eclipse.lsp4j.TextEdit te = new org.eclipse.lsp4j.TextEdit();
			te.setNewText(edit.getText());
			te.setRange(utils.toRange(compilationUnit, edit.getOffset(), edit.getLength()));
			converted.add(te);
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error converting TextEdits", e);
		}
		return super.visit(edit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.text.edits.TextEditVisitor#visit(org.eclipse.text.edits.
	 * CopySourceEdit)
	 */
	@Override
	public boolean visit(CopySourceEdit edit) {
		try {
			if (edit.getTargetEdit() != null) {
				org.eclipse.lsp4j.TextEdit te = new org.eclipse.lsp4j.TextEdit();
				te.setRange(utils.toRange(compilationUnit, edit.getOffset(), edit.getLength()));
				Document doc = new Document(compilationUnit.getSource());
				edit.apply(doc, TextEdit.UPDATE_REGIONS);
				String content = doc.get(edit.getOffset(), edit.getLength());
				if (edit.getSourceModifier() != null) {
					content = applySourceModifier(content, edit.getSourceModifier());
				}
				te.setNewText(content);
				converted.add(te);
			}
			return false;
		} catch (JavaModelException | MalformedTreeException | BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Error converting TextEdits", e);
		}
		return super.visit(edit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.text.edits.TextEditVisitor#visit(org.eclipse.text.edits.
	 * DeleteEdit)
	 */
	@Override
	public boolean visit(DeleteEdit edit) {
		try {
			org.eclipse.lsp4j.TextEdit te = new org.eclipse.lsp4j.TextEdit();
			te.setNewText("");
			te.setRange(utils.toRange(compilationUnit, edit.getOffset(), edit.getLength()));
			converted.add(te);
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error converting TextEdits", e);
		}
		return super.visit(edit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.text.edits.TextEditVisitor#visit(org.eclipse.text.edits.
	 * MultiTextEdit)
	 */
	@Override
	public boolean visit(MultiTextEdit edit) {
		try {
			org.eclipse.lsp4j.TextEdit te = new org.eclipse.lsp4j.TextEdit();
			te.setRange(utils.toRange(compilationUnit, edit.getOffset(), edit.getLength()));
			Document doc = new Document(compilationUnit.getSource());
			edit.apply(doc, TextEdit.UPDATE_REGIONS);
			String content = doc.get(edit.getOffset(), edit.getLength());
			te.setNewText(content);
			converted.add(te);
			return false;
		} catch (JavaModelException | MalformedTreeException | BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Error converting TextEdits", e);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.text.edits.TextEditVisitor#visit(org.eclipse.text.edits.
	 * ReplaceEdit)
	 */
	@Override
	public boolean visit(ReplaceEdit edit) {
		try {
			org.eclipse.lsp4j.TextEdit te = new org.eclipse.lsp4j.TextEdit();
			te.setNewText(edit.getText());
			te.setRange(utils.toRange(compilationUnit, edit.getOffset(), edit.getLength()));
			converted.add(te);
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error converting TextEdits", e);
		}
		return super.visit(edit);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.text.edits.TextEditVisitor#visit(org.eclipse.text.edits.
	 * CopyTargetEdit)
	 */
	@Override
	public boolean visit(CopyTargetEdit edit) {
		try {
			if (edit.getSourceEdit() != null) {
				org.eclipse.lsp4j.TextEdit te = new org.eclipse.lsp4j.TextEdit();
				te.setRange(utils.toRange(compilationUnit, edit.getOffset(), edit.getLength()));

				Document doc = new Document(compilationUnit.getSource());
				edit.apply(doc, TextEdit.UPDATE_REGIONS);
				String content = doc.get(edit.getSourceEdit().getOffset(), edit.getSourceEdit().getLength());

				if (edit.getSourceEdit().getSourceModifier() != null) {
					content = applySourceModifier(content, edit.getSourceEdit().getSourceModifier());
				}

				te.setNewText(content);
				converted.add(te);
			}
			return false; // do not visit children
		} catch (MalformedTreeException | BadLocationException | CoreException e) {
			LOGGER.log(Level.SEVERE, "Error converting TextEdits", e);
		}
		return super.visit(edit);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.text.edits.TextEditVisitor#visit(org.eclipse.text.edits.
	 * MoveSourceEdit)
	 */
	@Override
	public boolean visit(MoveSourceEdit edit) {
		try {
			// If MoveSourcedEdit & MoveTargetEdit are the same level, should delete the
			// original contenxt.
			// See issue#https://github.com/redhat-developer/vscode-java/issues/253
			if (edit.getParent() != null && edit.getTargetEdit() != null
					&& edit.getParent().equals(edit.getTargetEdit().getParent())) {
				org.eclipse.lsp4j.TextEdit te = new org.eclipse.lsp4j.TextEdit();
				te.setNewText("");
				te.setRange(utils.toRange(compilationUnit, edit.getOffset(), edit.getLength()));
				converted.add(te);
				return false;
			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error converting TextEdits", e);
		}
		return super.visit(edit);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.text.edits.TextEditVisitor#visit(org.eclipse.text.edits.
	 * MoveTargetEdit)
	 */
	@Override
	public boolean visit(MoveTargetEdit edit) {
		try {
			if (edit.getSourceEdit() != null) {
				org.eclipse.lsp4j.TextEdit te = new org.eclipse.lsp4j.TextEdit();
				te.setRange(utils.toRange(compilationUnit, edit.getOffset(), edit.getLength()));

				Document doc = new Document(compilationUnit.getSource());
				edit.apply(doc, TextEdit.UPDATE_REGIONS);
				String content = doc.get(edit.getSourceEdit().getOffset(), edit.getSourceEdit().getLength());
				if (edit.getSourceEdit().getSourceModifier() != null) {
					content = applySourceModifier(content, edit.getSourceEdit().getSourceModifier());
				}
				te.setNewText(content);
				converted.add(te);
				return false; // do not visit children
			}
		} catch (MalformedTreeException | BadLocationException | CoreException e) {
			LOGGER.log(Level.SEVERE, "Error converting TextEdits", e);
		}
		return super.visit(edit);
	}

	private String applySourceModifier(String content, ISourceModifier modifier) {
		if (StringUtils.isBlank(content) || modifier == null) {
			return content;
		}

		SimpleDocument subDocument = new SimpleDocument(content);
		TextEdit newEdit = new MultiTextEdit(0, subDocument.getLength());
		ReplaceEdit[] replaces = modifier.getModifications(content);
		for (ReplaceEdit replace : replaces) {
			newEdit.addChild(replace);
		}
		try {
			newEdit.apply(subDocument, TextEdit.NONE);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Error applying edit to document", e);
		}
		return subDocument.get();
	}
}
