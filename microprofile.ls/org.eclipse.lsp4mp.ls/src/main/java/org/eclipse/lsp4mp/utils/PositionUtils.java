/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.utils;

import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4mp.ls.commons.BadLocationException;
import org.eclipse.lsp4mp.ls.commons.TextDocument;
import org.eclipse.lsp4mp.model.Node;

public class PositionUtils {

	public static Range createRange(int startOffset, int endOffset, TextDocument document) {
		try {
			return new Range(document.positionAt(startOffset), document.positionAt(endOffset));
		} catch (BadLocationException e) {
			return null;
		}
	}
	
	public static Range createRange(Node node) {
		return PositionUtils.createRange(node.getStart(), node.getEnd(), node.getDocument());
	}

}