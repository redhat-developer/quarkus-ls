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
package com.redhat.qute.parser.expression;

import java.util.List;
import java.util.stream.Collectors;

import com.redhat.qute.parser.CancelChecker;
import com.redhat.qute.parser.expression.Parts.PartKind;
import com.redhat.qute.parser.parameter.ParameterParser;
import com.redhat.qute.parser.template.ASTVisitor;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.ParametersContainer;

/**
 * Method part.
 * 
 * <p>
 * {item.getName()}
 * </p>
 * 
 * @author Angelo ZERR
 *
 */
public class MethodPart extends MemberPart implements ParametersContainer {

	private List<Parameter> parameters;
	private int openBracketOffset = NULL_VALUE;
	private int closeBracketOffset = NULL_VALUE;
	private int endName;

	public MethodPart(int start, int end) {
		super(start, end);
		this.endName = end;
	}

	/**
	 * Returns the offset of the method name end.
	 */
	@Override
	public int getEndName() {
		return endName;
	}

	@Override
	public PartKind getPartKind() {
		return PartKind.Method;
	}

	/**
	 * Set the open bracket offset.
	 *
	 * <p>
	 * {item.compute|(1,'abcd')}
	 * </p>
	 * 
	 * @param openBracketOffset the open bracket offset.
	 */
	void setOpenBracket(int openBracketOffset) {
		this.openBracketOffset = openBracketOffset;
		super.setEnd(openBracketOffset);
	}

	/**
	 * Returns true if the method part have '(' bracket and false otherwise (infix
	 * notation,like {item or}).
	 * 
	 * @return true if the method part have '(' bracket and false otherwise (infix
	 *         notation,like {item or}).
	 */
	public boolean hasOpenBracket() {
		return openBracketOffset != NULL_VALUE;
	}

	/**
	 * Set the close bracket offset.
	 *
	 * <p>
	 * {item.compute(1,'abcd'|)}
	 * </p>
	 * 
	 * @param closeBracketOffset the close bracket offset.
	 */
	void setCloseBracket(int closeBracketOffset) {
		this.closeBracketOffset = closeBracketOffset;
		super.setEnd(closeBracketOffset);
	}

	/**
	 * Returns true if the method part have ')' bracket and false otherwise.
	 * 
	 * @return true if the method part have ')' bracket and false otherwise.
	 */
	public boolean hasCloseBracket() {
		return closeBracketOffset != NULL_VALUE;
	}

	// ---------------------------- Parameters methods

	/**
	 * Returns parameters of the section.
	 * 
	 * @return parameters of the section.
	 */
	public List<Parameter> getParameters() {
		if (parameters == null) {
			this.parameters = parseParameters();
		}
		return this.parameters;
	}

	/**
	 * Returns the parameter at the given offset and null otherwise.
	 * 
	 * @param offset the offset.
	 * 
	 * @return the parameter at the given offset and null otherwise.
	 */
	public Parameter getParameterAtOffset(int offset) {
		if (!isInParameters(offset)) {
			return null;
		}
		List<Parameter> parameters = getParameters();
		return (Parameter) Node.findNodeAt(parameters.stream().map(param -> (Node) param).collect(Collectors.toList()),
				offset);
	}

	/**
	 * Returns true if the given offset is inside parameter expression of the
	 * section and false otherwise.
	 * 
	 * @param offset the offset.
	 * 
	 * @return true if the given offset is inside parameter expression of the
	 *         section and false otherwise.
	 */
	public boolean isInParameters(int offset) {
		// cases:
		// - {item.count(|}
		// - {item.count(aaa,|}
		// - {item.count(aaa,b|bb}
		return offset >= getStartParametersOffset() && offset <= getEndParametersOffset();
	}

	private synchronized List<Parameter> parseParameters() {
		if (parameters != null) {
			return parameters;
		}
		List<Parameter> parameters = ParameterParser.parse(this, true, false);
		parameters.stream().forEach(p -> p.setCanHaveExpression(true));
		return parameters;
	}

	/**
	 * Returns the start offset of the parameters method.
	 * 
	 * <p>
	 * {item.compute(|1,'abcd')}
	 * </p>
	 * 
	 * 
	 * @return the start offset of the parameters method.
	 */
	@Override
	public int getStartParametersOffset() {
		return openBracketOffset + 1;
	}

	/**
	 * Returns the end offset of the parameters method.
	 * 
	 * <p>
	 * {item.compute(1,'abcd'|)}
	 * </p>
	 * 
	 * 
	 * @return the end offset of the parameters method.
	 */
	@Override
	public int getEndParametersOffset() {
		if (isClosed()) {
			return closeBracketOffset;
		}
		return getEnd();
	}

	@Override
	public void setEnd(int end) {
		super.setEnd(end);
	}

	@Override
	public boolean isClosed() {
		return hasCloseBracket();
	}

	/**
	 * Returns true if the method is used in 'Infix Notation' context and false
	 * otherwise.
	 *
	 * <ul>
	 * <li>Infix notation : <code>
	 * {name or 'John'}
	 * </code></li>
	 * <li>
	 * <li>NO Infix notation : <code>
	 * {name.or('John')}
	 * </code></li> *</li>
	 * </ul>
	 * 
	 * @return true if the method is used in 'Infix Notation' context and false
	 *         otherwise.
	 * 
	 * @see https://quarkus.io/guides/qute-reference#virtual_methods
	 */
	public boolean isInfixNotation() {
		return false;
	}

	/**
	 * Returns true if the method part is an operator and false otherwise.
	 * 
	 * @return true if the method part is an operator and false otherwise.
	 */
	public boolean isOperator() {
		return false;
	}

	@Override
	public String getTemplateContent() {
		return getOwnerTemplate().getText();
	}

	@Override
	public CancelChecker getCancelChecker() {
		return getOwnerTemplate().getCancelChecker();
	}

	@Override
	protected void accept0(ASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}
}
