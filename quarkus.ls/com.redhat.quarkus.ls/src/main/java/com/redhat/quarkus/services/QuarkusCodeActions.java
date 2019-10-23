/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.services;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;

import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.ls.commons.BadLocationException;
import com.redhat.quarkus.ls.commons.CodeActionFactory;
import com.redhat.quarkus.model.PropertiesModel;
import com.redhat.quarkus.model.PropertyKey;
import com.redhat.quarkus.settings.QuarkusFormattingSettings;
import com.redhat.quarkus.utils.PositionUtils;
import com.redhat.quarkus.utils.QuarkusPropertiesUtils;

/**
 * The Quarkus code actions
 * 
 * @author Angelo ZERR
 *
 */
class QuarkusCodeActions {

	private static final float MAX_DISTANCE_DIFF_RATIO = 0.1f;

	private static final Logger LOGGER = Logger.getLogger(QuarkusCodeActions.class.getName());

	/**
	 * Returns code actions for the given diagnostics of the application.properties
	 * <code>document</code> by using the given Quarkus properties metadata
	 * <code>projectInfo</code>.
	 * 
	 * @param context            the code action context
	 * @param range              the range
	 * @param document           the properties model.
	 * @param projectInfo        the Quarkus properties
	 * @param formattingSettings the formatting settings.
	 * @return the result of the code actions.
	 */
	public List<CodeAction> doCodeActions(CodeActionContext context, Range range, PropertiesModel document,
			QuarkusProjectInfo projectInfo, QuarkusFormattingSettings formattingSettings) {
		List<CodeAction> codeActions = new ArrayList<>();
		if (context.getDiagnostics() != null) {
			// Loop for all diagnostics
			for (Diagnostic diagnostic : context.getDiagnostics()) {
				if (ValidationType.unknown.name().equals(diagnostic.getCode())) {
					// Manage code action for unknown
					doCodeActionsForUnknown(diagnostic, document, projectInfo, codeActions);
				}
			}
		}
		return codeActions;
	}

	/**
	 * Creation code action for 'unknown' property by searching similar name from
	 * the known Quarkus properties.
	 * 
	 * <p>
	 * LIMITATION: mapped property are not supported.
	 * </p>
	 * 
	 * @param diagnostic  the diagnostic
	 * @param the         properties model.
	 * @param projectInfo the Quarkus properties
	 * @param codeActions code actions list to fill.
	 */
	private void doCodeActionsForUnknown(Diagnostic diagnostic, PropertiesModel document,
			QuarkusProjectInfo projectInfo, List<CodeAction> codeActions) {
		try {
			// Get property name by using the diagnostic range
			PropertyKey propertyKey = (PropertyKey) document.findNodeAt(diagnostic.getRange().getStart());
			String propertyName = propertyKey.getPropertyName();
			// Loop for each metadata property
			for (ExtendedConfigDescriptionBuildItem metaProperty : projectInfo.getProperties()) {
				if (QuarkusPropertiesUtils.isMappedProperty(metaProperty.getPropertyName())) {
					// FIXME: support mapped property
				} else {
					// Check if the property name is similar to the metadata name
					if (isSimilar(metaProperty.getPropertyName(), propertyName)) {
						Range range = PositionUtils.createRange(propertyKey);
						CodeAction replaceAction = CodeActionFactory.replace(
								"Did you mean '" + metaProperty.getPropertyName() + "' ?", range,
								metaProperty.getPropertyName(), document.getDocument(), diagnostic);
						codeActions.add(replaceAction);
					}
				}
			}
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuarkusCodeActions, position error", e);
		}
	}

	private static boolean isSimilar(String reference, String current) {
		int threshold = Math.round(MAX_DISTANCE_DIFF_RATIO * reference.length());
		LevenshteinDistance levenshteinDistance = new LevenshteinDistance(threshold);
		return levenshteinDistance.apply(reference, current) != -1;
	}
}
