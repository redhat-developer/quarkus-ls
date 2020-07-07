/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.model;

import org.eclipse.lsp4j.Position;
import java.util.List;
import org.eclipse.lsp4mp.ls.commons.BadLocationException;
import org.eclipse.lsp4mp.ls.commons.TextDocument;
import org.eclipse.lsp4mp.model.parser.ErrorEvent;
import org.eclipse.lsp4mp.model.parser.ErrorHandler;
import org.eclipse.lsp4mp.model.parser.ParseContext;
import org.eclipse.lsp4mp.model.parser.ParseException;
import org.eclipse.lsp4mp.model.parser.PropertiesHandler;
import org.eclipse.lsp4mp.model.parser.PropertiesParser;

/**
 * The properties model (application.properties) which stores each start/end
 * offset of each property keys/values.
 * 
 * @author Angelo ZERR
 *
 */
public class PropertiesModel extends Node {

	/**
	 * This handler catch each properties events (start/end property, etc) to build
	 * a DOM properties model which maintains offset locations.
	 *
	 */
	private static class PropertiesModelHandler implements PropertiesHandler {

		private final PropertiesModel model;
		private Property property;
		private Comments comment;

		public PropertiesModelHandler(PropertiesModel model) {
			this.model = model;
		}

		@Override
		public void startDocument(ParseContext context) {
			model.setStart(0);
		}

		@Override
		public void endDocument(ParseContext context) {
			model.setEnd(context.getLocationOffset());
		}

		@Override
		public void startProperty(ParseContext context) {
			this.property = new Property();
			property.setStart(context.getLocationOffset());
			model.addNode(property);
		}

		@Override
		public void startPropertyName(ParseContext context) {
			PropertyKey key = new PropertyKey();
			key.setStart(context.getLocationOffset());
			property.setKey(key);
		}

		@Override
		public void endPropertyName(ParseContext context) {
			Node key = property.getKey();
			key.setEnd(context.getLocationOffset());
		}

		@Override
		public void startPropertyValue(ParseContext context) {
			PropertyValue value = new PropertyValue();
			value.setStart(context.getLocationOffset());
			property.setValue(value);
		}

		@Override
		public void endPropertyValue(ParseContext context) {
			Node value = property.getValue();
			value.setEnd(context.getLocationOffset());
		}

		@Override
		public void endProperty(ParseContext context) {
			property.setEnd(context.getLocationOffset());
			this.property = null;
		}

		@Override
		public void startComment(ParseContext context) {
			this.comment = new Comments();
			comment.setStart(context.getLocationOffset());
			model.addNode(comment);
		}

		@Override
		public void endComment(ParseContext context) {
			comment.setEnd(context.getLocationOffset());
			this.comment = null;
		}

		@Override
		public void delimiterAssign(ParseContext context) {
			Node assign = new Assign();
			assign.setStart(context.getLocationOffset());

			// assumption: delimiters are only one character long
			assign.setEnd(context.getLocationOffset() + 1);

			property.setDelimiterAssign(assign);
		}

		@Override
		public void blankLine(ParseContext context) {

		}

		@Override
		public void startPropertyValueLiteral(ParseContext context) {
			Node valLiteral = new PropertyValueLiteral();
			valLiteral.setStart(context.getLocationOffset());
			property.getValue().addNode(valLiteral);
		}

		@Override
		public void endPropertyValueLiteral(ParseContext context) {
			List<Node> propFragments = property.getValue().getChildren();
			propFragments.get(propFragments.size() - 1).setEnd(context.getLocationOffset());
		}

		@Override
		public void startPropertyValueExpression(ParseContext context) {
			Node expression = new PropertyValueExpression();
			expression.setStart(context.getLocationOffset());
			property.getValue().addNode(expression);
		}

		@Override
		public void endPropertyValueExpression(ParseContext context) {
			List<Node> propFragments = property.getValue().getChildren();
			propFragments.get(propFragments.size() - 1).setEnd(context.getLocationOffset());
		}
	}

	private final TextDocument document;

	PropertiesModel(TextDocument document) {
		this.document = document;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.DOCUMENT;
	}

	/**
	 * Returns the properties model from the given text.
	 * 
	 * @param text
	 * @param uri
	 * @return the properties model from the given text.
	 */
	public static PropertiesModel parse(String text, String uri) {
		return parse(new TextDocument(text, uri));
	}

	/**
	 * Returns the properties model from the text of the given document.
	 * 
	 * @param document the text document
	 * @return the properties model from the text of the given document.
	 */
	public static PropertiesModel parse(TextDocument document) {
		PropertiesModel model = new PropertiesModel(document);
		PropertiesParser parser = new PropertiesParser();
		parser.parse(document.getText(), new PropertiesModelHandler(model), new ErrorHandler() {

			@Override
			public void error(ParseContext context, ErrorEvent errorEvent) throws ParseException {

			}
		});
		return model;
	}

	/**
	 * Returns the text from the <code>start</code> offset (inclusive) to the <code>end</code>
	 * offset (exclusive).
	 * 
	 * @param start         the start offset
	 * @param end           the end offset
	 * @param skipMultiLine determines whether or not new lines characters and backslashes
	 * should be preserved for multi line text values
	 * @return the text from the <code>start</code> offset (inclusive) to the <code>end</code>
	 * offset (exclusive).
	 */
	public String getText(int start, int end, boolean skipMultiLine) {
		String text = document.getText();
		if (!skipMultiLine) {
			return text.substring(start, end);
		}

		StringBuilder sb = new StringBuilder();
		int i = start;
		boolean trimLeading = false;
		while (i < end) {
			char curr = text.charAt(i);
			if (curr == '\\') {
				if (i < end - 1 && text.charAt(i + 1) == '\n') {
					i += 2;
					trimLeading = true;
					continue;
				} else if (i < end - 2 && text.charAt(i + 1) == '\r' && text.charAt(i + 2) == '\n') {
					i += 3;
					trimLeading = true;
					continue;
				}
			}

			if (!trimLeading || !Character.isWhitespace(curr)) {
				trimLeading = false;
				sb.append(curr);
			} 
			
			i++;
		}
		return sb.toString();
	}

	public int offsetAt(Position position) throws BadLocationException {
		return document.offsetAt(position);
	}

	public Position positionAt(int position) throws BadLocationException {
		return document.positionAt(position);
	}

	@Override
	public PropertiesModel getOwnerModel() {
		return this;
	}

	@Override
	public TextDocument getDocument() {
		return document;
	}

	@Override
	public String getText() {
		return document.getText();
	}

	public String getDocumentURI() {
		return getDocument().getUri();
	}

}
