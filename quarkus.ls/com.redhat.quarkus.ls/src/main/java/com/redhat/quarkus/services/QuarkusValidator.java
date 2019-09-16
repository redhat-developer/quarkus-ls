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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.quarkus.commons.EnumItem;
import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.model.Node;
import com.redhat.quarkus.model.Node.NodeType;
import com.redhat.quarkus.model.PropertiesModel;
import com.redhat.quarkus.model.Property;
import com.redhat.quarkus.settings.QuarkusValidationSettings;
import com.redhat.quarkus.utils.PositionUtils;
import com.redhat.quarkus.utils.QuarkusPropertiesUtils;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

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
	private final Map<String, List<Property>> existingProperties;

	public QuarkusValidator(QuarkusProjectInfo projectInfo, List<Diagnostic> diagnostics,
			QuarkusValidationSettings validationSettings) {
		this.projectInfo = projectInfo;
		this.diagnostics = diagnostics;
		this.validationSettings = validationSettings;
		this.existingProperties = new HashMap<String, List<Property>>();
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

		addDiagnosticsForDuplicates();
	}

	private void validateProperty(Property property) {
		String propertyNameWithProfile = property.getPropertyNameWithProfile();
		if (propertyNameWithProfile != null && !propertyNameWithProfile.isEmpty()) {
			// Validate Syntax property
			validateSyntaxProperty(propertyNameWithProfile, property);
			// Validate Duplicate property
			validateDuplicateProperty(propertyNameWithProfile, property);
		}

		String propertyName = property.getPropertyName();
		if (propertyName != null && !propertyName.isEmpty()) {
			ExtendedConfigDescriptionBuildItem metadata = QuarkusPropertiesUtils.getProperty(propertyName, projectInfo);
			if (metadata == null) {
				// Validate Unknown property
				validateUnknownProperty(propertyNameWithProfile, property);
			} else {
				// Validate property Value
				validatePropertyValue(propertyNameWithProfile, metadata, property);
			}
		}
	}

	private void validateSyntaxProperty(String propertyName, Property property) {
		DiagnosticSeverity severity = validationSettings.getSyntax().getDiagnosticSeverity(propertyName);
		if (severity == null) {
			// The syntax validation must be ignored for this property name
			return;
		}
		if (property.getDelimiterAssign() == null) {
			addDiagnostic("Missing equals sign after '" + propertyName + "'", property.getKey(), severity,
					ValidationType.syntax.name());
		}
	}

	private void validateDuplicateProperty(String propertyName, Property property) {
		DiagnosticSeverity severity = validationSettings.getDuplicate().getDiagnosticSeverity(propertyName);
		if (severity == null) {
			// The duplicate validation must be ignored for this property name
			return;
		}

		if (!existingProperties.containsKey(propertyName)) {
			existingProperties.put(propertyName, new ArrayList<Property>());
		}

		existingProperties.get(propertyName).add(property);
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
		
		if (property.getValue() == null) {
			return;
		}

		DiagnosticSeverity severity = validationSettings.getValue().getDiagnosticSeverity(propertyName);
		if (severity == null) {
			// The value validation must be ignored for this property name
			return;
		}

		String value = property.getPropertyValue();
		if (value == null || value.isEmpty()) {
			return;
		}
		
		String errorMessage = null;
		if (metadata.getEnums() != null && metadata.getEnumItem(value) == null) {
			errorMessage = "Invalid enum value: '" + value + "' is invalid for type " + metadata.getType();
		} else if (isValueTypeMismatch(metadata, value)) {
			errorMessage = "Type mismatch: " + metadata.getType() + " expected";
		}

		if (errorMessage != null) {
			addDiagnostic(errorMessage, property.getValue(), severity, ValidationType.value.name());
		}
	}

	/**
	 * Returns true only if <code>value</code> is a valid value for the property
	 * defined by <code>metadata</code>
	 * @param metadata metadata defining a property
	 * @param value    value to check
	 * @return true only if <code>value</code> is a valid value for the property
	 * defined by <code>metadata</code>
	 */
	private static boolean isValueTypeMismatch(ExtendedConfigDescriptionBuildItem metadata, String value) {
		return !isBuildtimePlaceholder(value) &&
		((metadata.isIntegerType() && !isIntegerString(value)) ||
		(metadata.isFloatType() && !isFloatString(value)) ||
		(metadata.isBooleanType() && !isBooleanString(value)) ||
		(metadata.isDoubleType() && !isDoubleString(value)) ||
		(metadata.isLongType() && !isLongString(value)) ||
		(metadata.isShortType() && !isShortString(value)));
	}

	private static boolean isBooleanString(String str) {
		return "true".equals(str) || "false".equals(str);
	}

	private static boolean isIntegerString(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static boolean isFloatString(String str) {
		try {
			Float.parseFloat(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static boolean isLongString(String str) {
		try {
			Long.parseLong(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static boolean isDoubleString(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static boolean isShortString(String str) {
		try {
			Short.parseShort(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static boolean isBuildtimePlaceholder(String str) {
		return str.startsWith("${") && str.endsWith("}");
	}

	private void addDiagnosticsForDuplicates() {
		existingProperties.forEach((propertyName, propertyList) -> {
			if (propertyList.size() <= 1) {
				return;
			}

			DiagnosticSeverity severity = validationSettings.getDuplicate().getDiagnosticSeverity(propertyName);

			for (Property property : propertyList) {
				addDiagnostic("Duplicate property '" + propertyName + "'", property.getKey(), severity,
						ValidationType.duplicate.name());
			}
		});
	}

	private void addDiagnostic(String message, Node node, DiagnosticSeverity severity, String code) {
		Range range = PositionUtils.createRange(node);
		diagnostics.add(new Diagnostic(range, message, severity, QUARKUS_DIAGNOSTIC_SOURCE, code));
	}

	public QuarkusValidationSettings getValidationSettings() {
		return validationSettings;
	}

}
