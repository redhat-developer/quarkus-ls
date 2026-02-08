package com.redhat.qute.parser.yaml;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Range;

import com.redhat.qute.ls.commons.BadLocationException;

public class YamlPositionUtility {

	private static final Logger LOGGER = Logger.getLogger(YamlPositionUtility.class.getName());

	public static Range createRange(YamlNode node) {
		YamlDocument document = node.getOwnerDocument();
		return createRange(node.getStart(), node.getEnd(), document);
	}

	public static Range createRange(int startOffset, int endOffset, YamlDocument document) {
		try {
			return new Range(document.positionAt(startOffset), document.positionAt(endOffset));
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "While creating Range the Offset was a BadLocation", e);
			return null;
		}
	}
}
