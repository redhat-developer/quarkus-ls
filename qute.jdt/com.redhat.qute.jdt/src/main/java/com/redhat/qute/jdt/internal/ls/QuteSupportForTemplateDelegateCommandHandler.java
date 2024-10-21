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
package com.redhat.qute.jdt.internal.ls;

import static com.redhat.qute.jdt.internal.ls.ArgumentUtils.getBoolean;
import static com.redhat.qute.jdt.internal.ls.ArgumentUtils.getFirst;
import static com.redhat.qute.jdt.internal.ls.ArgumentUtils.getString;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.WorkspaceEdit;

import com.redhat.qute.commons.DocumentFormat;
import com.redhat.qute.commons.FileUtils;
import com.redhat.qute.commons.GenerateMissingJavaMemberParams;
import com.redhat.qute.commons.GenerateMissingJavaMemberParams.MemberType;
import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.QuteJavaTypesParams;
import com.redhat.qute.commons.QuteJavadocParams;
import com.redhat.qute.commons.QuteProjectParams;
import com.redhat.qute.commons.QuteResolvedJavaTypeParams;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.TemplateRootPath;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.QuteDataModelProjectParams;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverKind;
import com.redhat.qute.commons.usertags.QuteUserTagParams;
import com.redhat.qute.commons.usertags.UserTagInfo;
import com.redhat.qute.jdt.QuteSupportForTemplate;

/**
 * JDT LS commands used by Qute template.
 *
 * @author Angelo ZERR
 *
 */
public class QuteSupportForTemplateDelegateCommandHandler extends AbstractQuteDelegateCommandHandler {

	private static final Logger LOGGER = Logger.getLogger(QuteSupportForTemplateDelegateCommandHandler.class.getName());

	private static final String PROJECT_URI_ATTR = "projectUri";

	private static final String PATTERN_ATTR = "pattern";

	private static final String SOURCE_TYPE_ATTR = "sourceType";

	private static final String SOURCE_METHOD_ATTR = "sourceMethod";
	private static final String SOURCE_PARAMETER_ATTR = "sourceParameter";

	private static final String DATA_METHOD_INVOCATION_ATTR = "dataMethodInvocation";
	private static final String SOURCE_FIELD_ATTR = "sourceField";

	private static final String TEMPLATE_FILE_URI_ATTR = "templateFileUri";

	private static final String CLASS_NAME_ATTR = "className";

	private static final String QUTE_TEMPLATE_PROJECTS_COMMAND_ID = "qute/template/projects";
	private static final String QUTE_TEMPLATE_PROJECT_COMMAND_ID = "qute/template/project";

	private static final String QUTE_TEMPLATE_PROJECT_DATA_MODEL_COMMAND_ID = "qute/template/projectDataModel";

	private static final String QUTE_TEMPLATE_USER_TAGS_COMMAND_ID = "qute/template/userTags";

	private static final String QUTE_TEMPLATE_JAVA_TYPES_COMMAND_ID = "qute/template/javaTypes";

	private static final String QUTE_TEMPLATE_JAVA_DEFINITION_COMMAND_ID = "qute/template/javaDefinition";

	private static final String QUTE_TEMPLATE_RESOLVED_JAVA_TYPE_COMMAND_ID = "qute/template/resolvedJavaType";

	private static final String QUTE_JAVADOC_RESOLVE_COMMAND_ID = "qute/template/javadoc";

	private static final String QUTE_TEMPLATE_GENERATE_MISSING_JAVA_MEMBER = "qute/template/generateMissingJavaMember";

	private static final String QUTE_TEMPLATE_IS_IN_TEMPLATE = "qute/template/isInTemplate";

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor monitor) throws Exception {
		switch (commandId) {
		case QUTE_TEMPLATE_PROJECTS_COMMAND_ID:
			return getProjects(commandId, monitor);
		case QUTE_TEMPLATE_PROJECT_COMMAND_ID:
			return getProjectInfo(arguments, commandId, monitor);
		case QUTE_TEMPLATE_PROJECT_DATA_MODEL_COMMAND_ID:
			return getProjectDataModel(arguments, commandId, monitor);
		case QUTE_TEMPLATE_USER_TAGS_COMMAND_ID:
			return getUserTags(arguments, commandId, monitor);
		case QUTE_TEMPLATE_JAVA_TYPES_COMMAND_ID:
			return getJavaTypes(arguments, commandId, monitor);
		case QUTE_TEMPLATE_RESOLVED_JAVA_TYPE_COMMAND_ID:
			return getResolvedJavaType(arguments, commandId, monitor);
		case QUTE_TEMPLATE_JAVA_DEFINITION_COMMAND_ID:
			return getJavaDefinition(arguments, commandId, monitor);
		case QUTE_JAVADOC_RESOLVE_COMMAND_ID:
			return getJavadoc(arguments, commandId, monitor);
		case QUTE_TEMPLATE_GENERATE_MISSING_JAVA_MEMBER:
			return generateMissingJavaMember(arguments, commandId, monitor);
		case QUTE_TEMPLATE_IS_IN_TEMPLATE:
			return isInTemplate(arguments, commandId, monitor);
		}
		return null;
	}

	private static List<ProjectInfo> getProjects(String commandId, IProgressMonitor monitor) {
		return QuteSupportForTemplate.getInstance().getProjects(JDTUtilsLSImpl.getInstance(), monitor);
	}

	private static ProjectInfo getProjectInfo(List<Object> arguments, String commandId, IProgressMonitor monitor) {
		QuteProjectParams params = createQuteProjectParams(arguments, commandId);
		return QuteSupportForTemplate.getInstance().getProjectInfo(params, JDTUtilsLSImpl.getInstance(), monitor);
	}

	private static QuteProjectParams createQuteProjectParams(List<Object> arguments, String commandId) {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(
					String.format("Command '%s' must be called with one QuteProjectParams argument!", commandId));
		}
		// Get project name from the java file URI
		String templateFileUri = getString(obj, TEMPLATE_FILE_URI_ATTR);
		if (templateFileUri == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be called with required QuteProjectParams.templateFileUri!", commandId));
		}
		return new QuteProjectParams(templateFileUri);
	}

	private static DataModelProject<DataModelTemplate<DataModelParameter>> getProjectDataModel(List<Object> arguments,
			String commandId, IProgressMonitor monitor) throws Exception {
		QuteDataModelProjectParams params = createQuteProjectDataModelParams(arguments, commandId);
		// Execute the getProjectDataModel in a Job to benefit with progress
		// monitor
		final AtomicReference<DataModelProject<DataModelTemplate<DataModelParameter>>> dataModelRef = new AtomicReference<DataModelProject<DataModelTemplate<DataModelParameter>>>(
				null);
		Job job = Job.create("Qute data model collector", progress -> {
			DataModelProject<DataModelTemplate<DataModelParameter>> project = QuteSupportForTemplate.getInstance()
					.getDataModelProject(params, JDTUtilsLSImpl.getInstance(), progress);
			dataModelRef.set(project);
		});
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException e) {
			LOGGER.log(Level.WARNING, "Error while joining Qute data model collector job", e);
		}

		Exception jobException = (Exception) job.getResult().getException();
		if (jobException != null) {
			if (jobException.getCause() != null) {
				throw (Exception) jobException.getCause();
			}
			throw jobException;
		}

		return dataModelRef.get();
	}

	private static QuteDataModelProjectParams createQuteProjectDataModelParams(List<Object> arguments,
			String commandId) {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be called with one QuteProjectDataModelParams argument!", commandId));
		}
		// Get project name from the java file URI
		String projectUri = getString(obj, PROJECT_URI_ATTR);
		if (projectUri == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required QuteProjectDataModelParams.projectUri!", commandId));
		}
		return new QuteDataModelProjectParams(projectUri);
	}

	/**
	 * Collect user tags from a given project Uri.
	 *
	 * @param arguments
	 * @param commandId
	 * @param monitor
	 *
	 * @return user tags from a given project Uri.
	 *
	 * @throws CoreException
	 */
	private static List<UserTagInfo> getUserTags(List<Object> arguments, String commandId, IProgressMonitor monitor)
			throws CoreException {
		QuteUserTagParams params = createQuteUserTagParams(arguments, commandId);
		return QuteSupportForTemplate.getInstance().getUserTags(params, JDTUtilsLSImpl.getInstance(), monitor);
	}

	private static QuteUserTagParams createQuteUserTagParams(List<Object> arguments, String commandId) {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(
					String.format("Command '%s' must be called with one QuteUserTagParams argument!", commandId));
		}
		// Get project name from the java file URI
		String projectUri = getString(obj, PROJECT_URI_ATTR);
		if (projectUri == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be called with required QuteUserTagParams.projectUri!", commandId));
		}
		return new QuteUserTagParams(projectUri);
	}

	/**
	 * Returns the file information (package name, etc) for the given Java file.
	 *
	 * @param arguments
	 * @param commandId
	 * @param monitor
	 * @return the file information (package name, etc) for the given Java file.
	 * @throws CoreException
	 * @throws JavaModelException
	 */
	private static ResolvedJavaTypeInfo getResolvedJavaType(List<Object> arguments, String commandId,
			IProgressMonitor monitor) throws JavaModelException, CoreException {
		// Create java file information parameter
		QuteResolvedJavaTypeParams params = createQuteResolvedJavaTypeParams(arguments, commandId);
		// Return file information from the parameter
		return QuteSupportForTemplate.getInstance().getResolvedJavaType(params, JDTUtilsLSImpl.getInstance(), monitor);
	}

	private static QuteResolvedJavaTypeParams createQuteResolvedJavaTypeParams(List<Object> arguments,
			String commandId) {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be called with one QuteResolvedJavaTypeParams argument!", commandId));
		}
		// Get project name from the java file URI
		String projectUri = getString(obj, PROJECT_URI_ATTR);
		if (projectUri == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required QuteResolvedJavaTypeParams.projectUri!", commandId));
		}
		String className = getString(obj, CLASS_NAME_ATTR);
		if (className == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required QuteResolvedJavaTypeParams.className!", commandId));
		}
		ValueResolverKind kind = null;
		try {
			kind = ValueResolverKind.forValue(ArgumentUtils.getInt(obj, "kind"));
		} catch (IllegalArgumentException e) {
			// ignored
		}
		return new QuteResolvedJavaTypeParams(className, kind, projectUri);
	}

	private static List<JavaTypeInfo> getJavaTypes(List<Object> arguments, String commandId, IProgressMonitor monitor)
			throws JavaModelException, CoreException {
		// Create java file information parameter
		QuteJavaTypesParams params = createQuteJavaTypesParams(arguments, commandId);
		// Return file information from the parameter
		return QuteSupportForTemplate.getInstance().getJavaTypes(params, JDTUtilsLSImpl.getInstance(), monitor);
	}

	private static QuteJavaTypesParams createQuteJavaTypesParams(List<Object> arguments, String commandId) {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(
					String.format("Command '%s' must be called with one QuteJavaTypesParams argument!", commandId));
		}
		// Get project name from the java file URI
		String projectUri = getString(obj, PROJECT_URI_ATTR);
		if (projectUri == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be called with required QuteJavaTypesParams.projectUri!", commandId));
		}
		String pattern = getString(obj, PATTERN_ATTR);
		if (pattern == null) {
			throw new UnsupportedOperationException(
					String.format("Command '%s' must be called with required QuteJavaTypesParams.pattern!", commandId));
		}
		return new QuteJavaTypesParams(pattern, projectUri);
	}

	/**
	 * Returns the Java definition for the given class / method.
	 *
	 * @param arguments
	 * @param commandId
	 * @param monitor
	 * @return the Java definition for the given class / method.
	 * @throws CoreException
	 * @throws JavaModelException
	 */
	private static Location getJavaDefinition(List<Object> arguments, String commandId, IProgressMonitor monitor)
			throws JavaModelException, CoreException {
		// Create java definition parameter
		QuteJavaDefinitionParams params = createQuteJavaDefinitionParams(arguments, commandId);
		// Return file information from the parameter
		return QuteSupportForTemplate.getInstance().getJavaDefinition(params, JDTUtilsLSImpl.getInstance(), monitor);
	}

	private static QuteJavaDefinitionParams createQuteJavaDefinitionParams(List<Object> arguments, String commandId) {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be called with one QuteJavaDefinitionParams argument!", commandId));
		}
		// Get project name from the java file URI
		String templateFileUri = getString(obj, PROJECT_URI_ATTR);
		if (templateFileUri == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required QuteJavaDefinitionParams.projectUri !", commandId));
		}
		String sourceType = getString(obj, SOURCE_TYPE_ATTR);
		if (sourceType == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required QuteJavaDefinitionParams.sourceType!", commandId));
		}
		QuteJavaDefinitionParams params = new QuteJavaDefinitionParams(sourceType, templateFileUri);
		String sourceField = getString(obj, SOURCE_FIELD_ATTR);
		params.setSourceField(sourceField);
		String sourceMethod = getString(obj, SOURCE_METHOD_ATTR);
		params.setSourceMethod(sourceMethod);
		String methodParameter = getString(obj, SOURCE_PARAMETER_ATTR);
		params.setSourceParameter(methodParameter);
		boolean dataMethodInvocation = getBoolean(obj, DATA_METHOD_INVOCATION_ATTR);
		params.setDataMethodInvocation(dataMethodInvocation);
		return params;
	}

	private static WorkspaceEdit generateMissingJavaMember(List<Object> arguments, String commandId,
			IProgressMonitor monitor) throws JavaModelException, CoreException {

		// Create java definition parameter
		GenerateMissingJavaMemberParams params = createGenerateMissingJavaMemberParams(arguments, commandId);
		// Return file information from the parameter
		return QuteSupportForTemplate.getInstance().generateMissingJavaMember(params, JDTUtilsLSImpl.getInstance(),
				monitor);
	}

	private static GenerateMissingJavaMemberParams createGenerateMissingJavaMemberParams(List<Object> arguments,
			String commandId) {

		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with one GenerateMissingJavaMemberParams argument!", commandId));
		}

		MemberType memberType = null;
		try {
			memberType = MemberType.forValue(ArgumentUtils.getInt(obj, "memberType"));
		} catch (IllegalArgumentException e) {
			// ignored
		}
		if (memberType == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required GenerateMissingJavaMemberParams.memberType !",
					commandId));
		}

		String missingProperty = getString(obj, "missingProperty");
		if (missingProperty == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required GenerateMissingJavaMemberParams.missingProperty !",
					commandId));
		}

		String javaType = getString(obj, "javaType");
		if (javaType == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required GenerateMissingJavaMemberParams.javaType !", commandId));
		}

		String projectUri = getString(obj, "projectUri");
		if (projectUri == null) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required GenerateMissingJavaMemberParams.projectUri !",
					commandId));
		}

		String templateClass = getString(obj, "templateClass");
		if (templateClass == null && memberType == GenerateMissingJavaMemberParams.MemberType.AppendTemplateExtension) {
			throw new UnsupportedOperationException(String.format(
					"Command '%s' must be called with required GenerateMissingJavaMemberParams.templateClass when memberType == memberType.AppendTemplateExtension !",
					commandId));
		}

		return new GenerateMissingJavaMemberParams(memberType, missingProperty, javaType, projectUri, templateClass);

	}

	private static String getJavadoc(List<Object> arguments, String commandId, IProgressMonitor monitor) {
		QuteJavadocParams quteJavadocParams = createQuteJavadocParams(arguments, commandId);
		return QuteSupportForTemplate.getInstance().getJavadoc(quteJavadocParams, JDTUtilsLSImpl.getInstance(),
				monitor);
	}

	private static QuteJavadocParams createQuteJavadocParams(List<Object> arguments, String commandId) {
		Map<String, Object> obj = getFirst(arguments);
		if (obj == null) {
			throw new UnsupportedOperationException(
					String.format("Command '%s' must be called with one QuteJavadocParams argument!", commandId));
		}

		String sourceType = getString(obj, "sourceType");
		if (sourceType == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be called with required QuteJavadocParams.sourceType !", commandId));
		}

		String projectUri = getString(obj, "projectUri");
		if (projectUri == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be called with required QuteJavadocParams.projectUri !", commandId));
		}

		String memberName = getString(obj, "memberName");
		if (memberName == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be called with required QuteJavadocParams.memberName !", commandId));
		}

		String signature = getString(obj, "signature");
		if (signature == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be called with required QuteJavadocParams.signature !", commandId));
		}

		DocumentFormat documentFormat = null;
		try {
			documentFormat = DocumentFormat.forValue(ArgumentUtils.getInt(obj, "documentFormat"));
		} catch (IllegalArgumentException e) {
			// ignored
		}
		if (documentFormat == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' must be called with required QuteJavadocParams.documentFormat !", commandId));
		}

		return new QuteJavadocParams(sourceType, projectUri, memberName, signature, documentFormat);
	}

	private static boolean isInTemplate(List<Object> arguments, String commandId, IProgressMonitor monitor) {
		QuteProjectParams params = createQuteProjectParams(arguments, commandId);
		ProjectInfo projectInfo = QuteSupportForTemplate.getInstance().getProjectInfo(params,
				JDTUtilsLSImpl.getInstance(), monitor);
		if (projectInfo == null) {
			return false;
		}
		Path templatePath = FileUtils.createPath(params.getTemplateFileUri());
		for (TemplateRootPath rootPath : projectInfo.getTemplateRootPaths()) {
			if (rootPath.isIncluded(templatePath)) {
				return true;
			}
		}
		return false;
	}

}
