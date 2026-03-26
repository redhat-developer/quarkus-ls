/*******************************************************************************
 * Copyright (c) 2021 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package com.redhat.qute.parser.template;

import java.util.ArrayList;
import java.util.List;

import com.redhat.qute.parser.CancelChecker;

/**
 * Represents a parameter declaration node in a Qute template.
 *
 * <p>
 * A parameter declaration starts with {@code {@} and declares a Java type with
 * an optional alias and an optional default value assignment. The supported
 * forms are:
 * </p>
 *
 * <ul>
 * <li>Simple type: {@code {@String str}}</li>
 * <li>Qualified type with generic: {@code {@java.util.List<String> items}}</li>
 * <li>With single-quoted string default: {@code {@String str = 'foo'}}</li>
 * <li>With double-quoted string default: {@code {@String str = "foo"}}</li>
 * <li>With numeric default: {@code {@Integer i = 123}}</li>
 * </ul>
 *
 * <p>
 * The node exposes offsets for each part of the declaration (Java type, alias,
 * and default value) to support features such as completion, hover, and
 * validation.
 * </p>
 *
 * @see JavaTypeRangeOffset
 * 
 * @see ParametersContainer
 * @see JavaTypeInfoProvider
 */
public class ParameterDeclaration extends Node implements ParametersContainer, JavaTypeInfoProvider {

	/**
	 * Represents a character range (start/end offset) for one segment of a Java
	 * type name, with additional context about whether the segment is inside a
	 * generic type argument list.
	 *
	 * <p>
	 * For example, given {@code {@java.util.List<String> items}}:
	 * </p>
	 * <ul>
	 * <li>{@code java.util.List} → {@code inGeneric=false}</li>
	 * <li>{@code String} → {@code inGeneric=true}, {@code genericClosed=true}</li>
	 * </ul>
	 */
	public static class JavaTypeRangeOffset extends RangeOffset {

		/**
		 * Whether this segment is inside a generic type argument list (i.e. between
		 * {@code <} and {@code >}).
		 */
		private final boolean inGeneric;

		/**
		 * Whether the generic argument list is properly closed by {@code >}.
		 */
		private final boolean genericClosed;

		/**
		 * Creates a {@link JavaTypeRangeOffset} outside any generic context.
		 *
		 * @param start inclusive start offset in the template text
		 * @param end   exclusive end offset in the template text
		 */
		public JavaTypeRangeOffset(int start, int end) {
			this(start, end, false, false);
		}

		/**
		 * Creates a {@link JavaTypeRangeOffset} with full generic context.
		 *
		 * @param start         inclusive start offset in the template text
		 * @param end           exclusive end offset in the template text
		 * @param inGeneric     {@code true} if this segment is a generic type argument
		 * @param genericClosed {@code true} if the generic list is closed by {@code >}
		 */
		public JavaTypeRangeOffset(int start, int end, boolean inGeneric, boolean genericClosed) {
			super(start, end);
			this.inGeneric = inGeneric;
			this.genericClosed = genericClosed;
		}

		/**
		 * Returns whether this segment is inside a generic type argument list.
		 *
		 * @return {@code true} if this segment is between {@code <} and {@code >}
		 */
		public boolean isInGeneric() {
			return inGeneric;
		}

		/**
		 * Returns whether the generic argument list is properly closed.
		 *
		 * @return {@code true} if the closing {@code >} was found
		 */
		public boolean isGenericClosed() {
			return genericClosed;
		}
	}

	/**
	 * Constructs a parameter declaration node.
	 *
	 * @param start start offset of the node in the template text (inclusive)
	 * @param end   end offset of the node in the template text (exclusive)
	 */
	ParameterDeclaration(int start, int end) {
		super(start, end);
	}

	/** {@inheritDoc} */
	@Override
	public NodeKind getKind() {
		return NodeKind.ParameterDeclaration;
	}

	/**
	 * Returns the offset of the first meaningful character after the {@code {@}
	 * opening prefix (2 characters).
	 *
	 * @return start offset of the node content
	 */
	public int getStartContent() {
		return super.getStart() + 2;
	}

	/**
	 * Returns the offset just past the last meaningful character of the node,
	 * excluding the closing {@code }} and any trailing {@code \r} or {@code \n}
	 * characters.
	 *
	 * @return end offset of the node content (exclusive)
	 */
	public int getEndContent() {
		int index = super.getEnd() - 1;
		if (isClosed()) {
			// The node ends with '}'
			return index;
		}
		String text = super.getOwnerTemplate().getText();
		char c = text.charAt(index);
		while (c == '\r' || c == '\n') {
			index--;
			c = text.charAt(index);
		}
		return index + 1;
	}

	/**
	 * Returns the display name of this node, used for debugging and tooling.
	 *
	 * @return {@code "#parameter-declaration"}
	 */
	public String getNodeName() {
		return "#parameter-declaration";
	}

	/**
	 * Returns the full Java type name declared by this node, for example
	 * {@code java.util.List<String>} or {@code String}.
	 *
	 * @return the Java type name as a string, or an empty string if absent
	 */
	public String getJavaType() {
		Template template = getOwnerTemplate();
		int classNameStart = getClassNameStart();
		int classNameEnd = getClassNameEnd();
		return template.getText(classNameStart, classNameEnd);
	}

	/**
	 * Returns the start offset of the Java type name in the template text.
	 *
	 * @return start offset of the Java type name
	 */
	public int getClassNameStart() {
		return getStartContent();
	}

	/**
	 * Returns the end offset of the Java type name, i.e. the position of the first
	 * space after the type name, or {@link #getEndContent()} if none is found.
	 *
	 * @return end offset of the Java type name (exclusive)
	 */
	public int getClassNameEnd() {
		Template template = getOwnerTemplate();
		String text = template.getText();
		for (int i = getStartContent(); i < getEndContent(); i++) {
			char c = text.charAt(i);
			if (c == ' ') {
				return i;
			}
		}
		return getEndContent();
	}

	/**
	 * Returns whether the given offset falls within the Java type name range.
	 *
	 * @param offset the offset to test
	 * @return {@code true} if the offset is inside the Java type name
	 */
	public boolean isInJavaTypeName(int offset) {
		int classNameStart = getClassNameStart();
		int classNameEnd = getClassNameEnd();
		return offset >= classNameStart && offset <= classNameEnd;
	}

	/**
	 * Returns the alias declared for this parameter, i.e. the identifier that
	 * follows the Java type name. For {@code {@String str}}, returns {@code "str"}.
	 *
	 * @return the alias string, or {@code null} if no alias is declared
	 */
	public String getAlias() {
		int aliasStart = getAliasStart();
		if (aliasStart == -1) {
			return null;
		}
		int aliasEnd = getAliasEnd();
		Template template = getOwnerTemplate();
		return template.getText(aliasStart, aliasEnd);
	}

	/**
	 * Returns the start offset of the alias, i.e. the position of the first
	 * character after the space that separates the type from the alias.
	 *
	 * @return the alias start offset, or {@code -1} if no alias is present
	 */
	public int getAliasStart() {
		Template template = getOwnerTemplate();
		String text = template.getText();
		for (int i = getStartContent(); i < getEndContent(); i++) {
			char c = text.charAt(i);
			if (c == ' ' && (i + 1) < getEndContent()) {
				return i + 1;
			}
		}
		return -1;
	}

	/**
	 * Returns the end offset of the alias. When a default value is present (e.g.
	 * {@code {@String str = "foo"}}), the alias ends just before the {@code =}
	 * sign; otherwise it extends to {@link #getEndContent()}.
	 *
	 * @return the alias end offset (exclusive)
	 */
	public int getAliasEnd() {
		int aliasStart = getAliasStart();
		if (aliasStart == -1) {
			return getEndContent();
		}
		Template template = getOwnerTemplate();
		String text = template.getText();
		for (int i = aliasStart; i < getEndContent(); i++) {
			char c = text.charAt(i);
			if (c == ' ' || c == '=') {
				return i;
			}
		}
		return getEndContent();
	}

	/**
	 * Returns whether the given offset falls within the alias range.
	 *
	 * @param offset the offset to test
	 * @return {@code true} if the offset is inside the alias
	 */
	public boolean isInAlias(int offset) {
		int aliasStart = getAliasStart();
		if (aliasStart == -1) {
			return false;
		}
		int aliasEnd = getAliasEnd();
		return offset >= aliasStart && offset <= aliasEnd;
	}

	/**
	 * Returns whether this parameter declaration includes an alias.
	 *
	 * @return {@code true} if an alias is declared
	 */
	public boolean hasAlias() {
		return getAliasStart() != -1;
	}

	// -------------------------------------------------------------------------
	// Default value support
	// {@String str = 'foo'}, {@String str = "foo"}, {@Integer i = 123}
	// -------------------------------------------------------------------------

	/**
	 * Returns whether this parameter declaration includes a default value
	 * assignment, for example {@code {@String str = "foo"}} or {@code {@Integer i =
	 * 123}}.
	 *
	 * @return {@code true} if a default value is present
	 */
	public boolean hasDefaultValue() {
		return getDefaultValueStart() != -1;
	}

	/**
	 * Returns the start offset of the default value in the template text, pointing
	 * to the first character of the value itself (after the {@code =} sign and any
	 * surrounding spaces), or {@code -1} if no default value is present.
	 *
	 * <p>
	 * For {@code {@String str = "foo"}}, the offset points to {@code "}.<br>
	 * For {@code {@Integer i = 123}}, the offset points to {@code 1}.
	 * </p>
	 *
	 * @return the start offset of the default value, or {@code -1} if absent
	 */
	public int getDefaultValueStart() {
		int aliasStart = getAliasStart();
		if (aliasStart == -1) {
			return -1;
		}
		Template template = getOwnerTemplate();
		String text = template.getText();
		int end = getEndContent();
		boolean foundEquals = false;
		for (int i = aliasStart; i < end; i++) {
			char c = text.charAt(i);
			if (c == '=') {
				foundEquals = true;
			} else if (foundEquals && c != ' ') {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns the end offset of the default value in the template text (exclusive),
	 * or {@code -1} if no default value is present.
	 *
	 * <p>
	 * For quoted values ({@code 'foo'} or {@code "foo"}), the offset points to the
	 * character just after the closing quote.<br>
	 * For unquoted values ({@code 123}), the offset points to the character just
	 * after the last digit or non-space character.
	 * </p>
	 *
	 * @return the end offset of the default value, or {@code -1} if absent
	 */
	public int getDefaultValueEnd() {
		int start = getDefaultValueStart();
		if (start == -1) {
			return -1;
		}
		Template template = getOwnerTemplate();
		String text = template.getText();
		int end = getEndContent();
		char first = text.charAt(start);
		if (first == '\'' || first == '"') {
			// Quoted string: scan until matching closing quote
			for (int i = start + 1; i < end; i++) {
				if (text.charAt(i) == first) {
					return i + 1;
				}
			}
			// Unclosed quote: extend to end of content
			return end;
		}
		// Unquoted value (e.g. numeric): extend until space or end
		for (int i = start; i < end; i++) {
			if (text.charAt(i) == ' ') {
				return i;
			}
		}
		return end;
	}

	/**
	 * Returns the raw text of the default value as it appears in the template,
	 * including any surrounding quotes. Returns {@code null} if no default value is
	 * present.
	 *
	 * <p>
	 * Examples:
	 * </p>
	 * <ul>
	 * <li>{@code {@String str = "foo"}} → {@code "\"foo\""}</li>
	 * <li>{@code {@String str = 'foo'}} → {@code "'foo'"}</li>
	 * <li>{@code {@Integer i = 123}} → {@code "123"}</li>
	 * </ul>
	 *
	 * @return the raw default value string, or {@code null} if absent
	 */
	public String getDefaultValue() {
		int start = getDefaultValueStart();
		if (start == -1) {
			return null;
		}
		return getOwnerTemplate().getText(start, getDefaultValueEnd());
	}

	/**
	 * Returns whether the given offset falls within the default value range.
	 *
	 * @param offset the offset to test
	 * @return {@code true} if the offset is inside the default value
	 */
	public boolean isInDefaultValue(int offset) {
		int start = getDefaultValueStart();
		if (start == -1) {
			return false;
		}
		int end = getDefaultValueEnd();
		return offset >= start && offset <= end;
	}

	// -------------------------------------------------------------------------
	// Generic type ranges
	// -------------------------------------------------------------------------

	/**
	 * Returns all character ranges that make up the Java type name, including
	 * segments inside a generic argument list.
	 *
	 * <p>
	 * For {@code java.util.Map<String, Integer>} the result is:
	 * </p>
	 * <ol>
	 * <li>{@code java.util.Map} — {@code inGeneric=false}</li>
	 * <li>{@code String} — {@code inGeneric=true, genericClosed=false}</li>
	 * <li>{@code Integer} — {@code inGeneric=true, genericClosed=true}</li>
	 * </ol>
	 *
	 * @return ordered list of Java type name segment ranges
	 */
	public List<JavaTypeRangeOffset> getJavaTypeNameRanges() {
		List<JavaTypeRangeOffset> ranges = new ArrayList<>();
		Template template = getOwnerTemplate();
		String text = template.getText();
		int end = getEndContent();
		int startType = getStartContent();
		int endType = startType;
		boolean diamon = false;
		for (; endType < end; endType++) {
			char c = text.charAt(endType);
			if (isSpace(c) || c == '[') {
				break;
			} else if (c == '<') {
				diamon = true;
				break;
			}
		}

		ranges.add(new JavaTypeRangeOffset(startType, endType));

		if (diamon) {
			boolean genericClosed = false;
			endType++;
			startType = endType;
			for (; endType < end; endType++) {
				char c = text.charAt(endType);
				if (isSpace(c) || c == '>') {
					genericClosed = c == '>';
					break;
				} else if (c == ',') {
					ranges.add(new JavaTypeRangeOffset(startType, endType, true, true));
					startType = endType + 1;
				}
			}
			ranges.add(new JavaTypeRangeOffset(startType, endType, true, genericClosed));
		}
		return ranges;
	}

	/**
	 * Returns the {@link JavaTypeRangeOffset} segment that contains the given
	 * offset, or {@code null} if the offset does not fall inside any segment.
	 *
	 * @param offset the offset to locate
	 * @return the matching segment, or {@code null}
	 */
	public JavaTypeRangeOffset getJavaTypeNameRange(int offset) {
		List<JavaTypeRangeOffset> ranges = getJavaTypeNameRanges();
		for (JavaTypeRangeOffset range : ranges) {
			if (Node.isIncluded(range.getStart(), range.getEnd(), offset)) {
				return range;
			}
		}
		return null;
	}

	/**
	 * Returns whether the given character is a whitespace character ({@code ' '},
	 * {@code '\r'}, or {@code '\n'}).
	 *
	 * @param c the character to test
	 * @return {@code true} if {@code c} is a whitespace character
	 */
	private static boolean isSpace(char c) {
		return c == '\r' || c == '\n' || c == ' ';
	}

	/** {@inheritDoc} */
	@Override
	public Node getJavaTypeOwnerNode() {
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public int getStartParametersOffset() {
		return getStartContent();
	}

	/** {@inheritDoc} */
	@Override
	public int getEndParametersOffset() {
		return getEndContent();
	}

	/** {@inheritDoc} */
	@Override
	protected void accept0(ASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return the full text of the owning template
	 */
	@Override
	public String getTemplateContent() {
		return getOwnerTemplate().getText();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return the {@link CancelChecker} from the owning template
	 */
	@Override
	public CancelChecker getCancelChecker() {
		return getOwnerTemplate().getCancelChecker();
	}
}