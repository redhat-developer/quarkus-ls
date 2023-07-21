/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.parser.validator;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.lsp4j.Range;

import com.redhat.qute.parser.expression.MethodPart;
import com.redhat.qute.parser.expression.ObjectPart;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts.PartKind;
import com.redhat.qute.parser.expression.PropertyPart;
import com.redhat.qute.parser.template.ASTVisitor;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.CaseSection;
import com.redhat.qute.parser.template.sections.CustomSection;
import com.redhat.qute.parser.template.sections.EachSection;
import com.redhat.qute.parser.template.sections.ElseSection;
import com.redhat.qute.parser.template.sections.ForSection;
import com.redhat.qute.parser.template.sections.FragmentSection;
import com.redhat.qute.parser.template.sections.IfSection;
import com.redhat.qute.parser.template.sections.IncludeSection;
import com.redhat.qute.parser.template.sections.InsertSection;
import com.redhat.qute.parser.template.sections.IsSection;
import com.redhat.qute.parser.template.sections.LetSection;
import com.redhat.qute.parser.template.sections.SetSection;
import com.redhat.qute.parser.template.sections.SwitchSection;
import com.redhat.qute.parser.template.sections.WhenSection;
import com.redhat.qute.parser.template.sections.WithSection;
import com.redhat.qute.utils.QutePositionUtility;

/**
 * Qute syntax validator which reports the same error that the real Qute parser.
 * 
 * <p>
 * It improves the syntax validation of the real Qute parser by reporting:
 * 
 * <ul>
 * <li>several errors instead of the first error encountered in the
 * template.</li>
 * <li>a proper range (start/end position) instead of just one position.</li>
 * <li>a proper position instead of reporting strange position with the real
 * Qute parser.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * NOTE : this validator doesn't take care for the moment of the all error from
 * the real Qute parser but it will be improved step by step.
 * <p>
 * 
 * @author Angelo ZERR
 *
 */
public class QuteSyntaxValidator extends ASTVisitor {

	private final IQuteSyntaxValidatorReporter reporter;

	private Set<Section> sectionStartNotFoundToIgnore;

	public QuteSyntaxValidator(IQuteSyntaxValidatorReporter reporter) {
		this.reporter = reporter;
	}

	@Override
	public boolean visit(CaseSection node) {
		// {#case }
		return super.visit(node);
	}

	@Override
	public boolean visit(CustomSection node) {
		validateSectionSyntax(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(EachSection node) {
		validateSectionSyntax(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ElseSection node) {
		validateSectionSyntax(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ForSection node) {
		validateSectionSyntax(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(FragmentSection node) {
		validateSectionSyntax(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(IfSection node) {
		validateSectionSyntax(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(IncludeSection node) {
		validateSectionSyntax(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(InsertSection node) {
		validateSectionSyntax(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(IsSection node) {
		// {#is }
		return super.visit(node);
	}

	@Override
	public boolean visit(LetSection node) {
		validateSectionSyntax(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(SetSection node) {
		validateSectionSyntax(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(SwitchSection node) {
		validateSectionSyntax(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(WhenSection node) {
		validateSectionSyntax(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(WithSection node) {
		validateSectionSyntax(node);
		return super.visit(node);
	}

	private void validateSectionSyntax(Section section) {
		if (!section.hasTag()) {
			// {#}
			Range startSectionRange = QutePositionUtility.selectStartTagName(section);
			reporter.reportError(startSectionRange, section, QuteSyntaxErrorCode.NO_SECTION_NAME, "{#}");
		} else if (section.hasStartTag()) {
			boolean sectionBlock = section.isSectionBlock();
			if (!section.hasEndTag() && !section.isSelfClosed()) {
				Section orphanEndSection = section.getOrphanEndSection(
						sectionBlock ? section.getEnd() : section.getStart(), section.getTag(), true);
				if (orphanEndSection != null) {
					if (sectionStartNotFoundToIgnore == null) {
						sectionStartNotFoundToIgnore = new HashSet<>();
					}
					sectionStartNotFoundToIgnore.add(orphanEndSection);
					if (sectionBlock) {
						// The section is a block (ex : {/else} is a block of {#if}
						// {#if test}Hello{#else}Hi{/elsa}{/if}
						// --> Parser error: section block end tag [elsa] does not match the start tag
						// [else]
						Range endSectionRange = QutePositionUtility.selectEndTagName(orphanEndSection);
						reporter.reportError(endSectionRange, orphanEndSection,
								QuteSyntaxErrorCode.SECTION_BLOCK_END_DOES_NOT_MATCH_START,
								orphanEndSection.getTag(), section.getTag());
					} else {
						// {#if test}Hello {name}!{/for}
						// --> Parser error: section end tag [for] does not match the start tag [if]
						Range endSectionRange = QutePositionUtility.selectEndTagName(orphanEndSection);
						reporter.reportError(endSectionRange, orphanEndSection,
								QuteSyntaxErrorCode.SECTION_END_DOES_NOT_MATCH_START,
								orphanEndSection.getTag(), section.getTag());
					}
				} else {
					if (sectionBlock) {
						// {#else} is valid if it is declared in an {#if}
					} else {
						// #let and #include can be not closed
						if (!section.canSupportUnterminatedSection()) {
							// {#for}{#each}{/each}
							// --> Parser error: unterminated section [for] detected
							Range startSectionRange = QutePositionUtility.selectStartTagName(section);
							reporter.reportError(startSectionRange, section, QuteSyntaxErrorCode.UNTERMINATED_SECTION,
									section.getTag());
						}
					}
				}
			}
		} else {
			// It is an orphan end tag ({/if}
			if (!(sectionStartNotFoundToIgnore != null && sectionStartNotFoundToIgnore.contains(section))) {
				// {#if true}Bye...{/if} Hello {/if}
				// last {/if} must report as SECTION_START_NOT_FOUND error
				Range endSectionRange = QutePositionUtility.selectEndTagName(section);
				reporter.reportError(endSectionRange, section,
						QuteSyntaxErrorCode.SECTION_START_NOT_FOUND, "{/" + section.getTag() + "}");
			}
		}
	}

	@Override
	public boolean visit(ObjectPart node) {
		validateEndWithDotSyntax(node);
		return super.visit(node);
	}

	public boolean visit(PropertyPart node) {
		validateEndWithDotSyntax(node);
		return super.visit(node);
	}

	public boolean visit(MethodPart node) {
		// Validate end dot syntax for parameters
		for (Parameter parameter : node.getParameters()) {
			Expression expression = parameter.getJavaTypeExpression();
			if (expression != null) {
				expression.accept(this);
			}
		}
		validateEndWithDotSyntax(node);
		return super.visit(node);
	}

	private void validateEndWithDotSyntax(Part node) {
		if (!hasFollowingPart(node)) {
			Template template = node.getOwnerTemplate();
			// It's the last part, check if it is not ended with '.'
			int end = node.getPartKind() == PartKind.Method ? node.getEnd() + 1 : node.getEnd();
			String text = template.getText();
			if (end < text.length()) {
				char c = text.charAt(end);
				if (c == '.') {
					Range range = QutePositionUtility.createRange(end, end + 1, template);
					reporter.reportError(range, node,
							QuteSyntaxErrorCode.UNEXPECTED_TOKEN, c);
				}
			}
		}
	}

	private boolean hasFollowingPart(Part node) {
		Node next = node.getNextSibling();
		if (next == null) {
			// - {name.}
			// - {name.size().}
			return false;
		}
		if (next.getKind() == NodeKind.ExpressionPart) {
			Part nextPart = (Part) next;
			if (nextPart.getPartKind() == PartKind.Method) {
				// - {name.or(10)} <-- valid
				// - {name. ?: 10} <-- invaid: infix notation
				MethodPart methodPart = (MethodPart) nextPart;
				return !methodPart.isInfixNotation();
			}
		}
		return true;
	}
}
