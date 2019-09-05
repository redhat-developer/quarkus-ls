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

import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.model.Node;
import com.redhat.quarkus.model.Node.NodeType;
import com.redhat.quarkus.model.PropertiesModel;
import com.redhat.quarkus.model.Property;
import com.redhat.quarkus.settings.QuarkusValidationSettings;
import com.redhat.quarkus.utils.PositionUtils;
import com.redhat.quarkus.utils.QuarkusPropertiesUtils;

/**
 * Quarkus validator to validate properties declared in application.properties.
 * 
 * @author Angelo ZERR
 *
 */
class QuarkusValidator {

	private static final String QUARKUS_DIAGNOSTIC_SOURCE = "quarkus";

	private final QuarkusProjectInfo projectInfo;
	private final List<Diagnostic> diagnostics;

	private final QuarkusValidationSettings validationSettings;

	public QuarkusValidator(QuarkusProjectInfo projectInfo, List<Diagnostic> diagnostics,
			QuarkusValidationSettings validationSettings) {
		this.projectInfo = projectInfo;
		this.diagnostics = diagnostics;
		this.validationSettings = validationSettings;
	}

	public void validate(PropertiesModel document, CancelChecker cancelChecker) {
		List<Node> nodes = document.getChildren();
		for (Node node : nodes) {
			if (cancelChecker != null) {
				cancelChecker.checkCanceled();
			}
			if (node.getNodeType() == NodeType.PROPERTY) {
				validateProperty((Property) node);
			}
		}
	}

	private void validateProperty(Property property) {
		String propertyName = property.getPropertyName();
		if (propertyName != null && !propertyName.isEmpty()) {
			// Validate Syntax property
			validateSyntaxProperty(propertyName, property);
			// Validate Duplicate property
			validateDuplicateProperty(propertyName, property);

			ExtendedConfigDescriptionBuildItem metadata = QuarkusPropertiesUtils.getProperty(propertyName, projectInfo);
			if (metadata == null) {
				// Validate Unknown property
				validateUnknownProperty(propertyName, property);
			} else {
				// Validate property Value
				validatePropertyValue(propertyName, metadata, property);
			}
		}
	}

	private void validateSyntaxProperty(String propertyName, Property property) {
		DiagnosticSeverity severity = validationSettings.getSyntax().getDiagnosticSeverity(propertyName);
		if (severity == null) {
			// The syntax validation must be ignored for this property name
			return;
		}
		// TODO : validate if there are an assign '='
	}

	private void validateDuplicateProperty(String propertyName, Property property) {
		DiagnosticSeverity severity = validationSettings.getDuplicate().getDiagnosticSeverity(propertyName);
		if (severity == null) {
			// The duplicate validation must be ignored for this property name
			return;
		}
		// TODO : store in the set the property to know if it already exsists
	}

	private void validateUnknownProperty(String propertyName, Property property) {
		DiagnosticSeverity severity = validationSettings.getUnknown().getDiagnosticSeverity(propertyName);
		if (severity == null) {
			// The unknown validation must be ignored for this property name
			return;
		}
		addDiagnostic("Unknown property '" + propertyName + "'", property.getKey(), severity,
				ValidationType.unknown.name());
	}

	private void validatePropertyValue(String propertyName, ExtendedConfigDescriptionBuildItem metadata,
			Property property) {
		DiagnosticSeverity severity = validationSettings.getValue().getDiagnosticSeverity(propertyName);
		if (severity == null) {
			// The value validation must be ignored for this property name
			return;
		}

		// TODO validate boolean, int, enums types
		String type = metadata.getType();

		Boolean required = metadata.isRequired();
		if (required != null && required) {
			// TODO validate required property.
		}
	}

	private void addDiagnostic(String message, Node node, DiagnosticSeverity severity, String code) {
		Range range = PositionUtils.createRange(node);
		diagnostics.add(new Diagnostic(range, message, severity, QUARKUS_DIAGNOSTIC_SOURCE, code));
	}

	public QuarkusValidationSettings getValidationSettings() {
		return validationSettings;
	}

}
