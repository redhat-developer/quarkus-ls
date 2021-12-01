package com.redhat.qute.project;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.QuteJavaTypesParams;
import com.redhat.qute.commons.QuteResolvedJavaTypeParams;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.QuteDataModelProjectParams;

public class MockQuteProjectRegistry extends QuteProjectRegistry {

	public static final Range JAVA_CLASS_RANGE = new Range(new Position(0, 0), new Position(0, 0));

	public static final Range JAVA_FIELD_RANGE = new Range(new Position(1, 1), new Position(1, 1));

	public static final Range JAVA_METHOD_RANGE = new Range(new Position(2, 2), new Position(2, 2));

	public MockQuteProjectRegistry() {
		super(null, null, null, null);
	}

	@Override
	protected QuteProject createProject(ProjectInfo projectInfo) {
		if (QuteQuickStartProject.PROJECT_URI.equals(projectInfo.getUri())) {
			return new QuteQuickStartProject(projectInfo, this);
		}
		return super.createProject(projectInfo);
	}

	@Override
	public CompletableFuture<List<JavaTypeInfo>> getJavaClasses(QuteJavaTypesParams params) {
		MockQuteProject project = (MockQuteProject) getProject(params.getProjectUri());
		if (project == null) {
			return CompletableFuture.completedFuture(Collections.emptyList());
		}
		return CompletableFuture.completedFuture(project.getJavaClasses());
	}

	@Override
	protected CompletableFuture<ResolvedJavaTypeInfo> getResolvedJavaClass(QuteResolvedJavaTypeParams params) {
		MockQuteProject project = (MockQuteProject) getProject(params.getProjectUri());
		if (project == null) {
			return CompletableFuture.completedFuture(null);
		}
		return CompletableFuture.completedFuture(project.getResolvedJavaClassSync(params.getClassName()));
	}

	@Override
	public CompletableFuture<Location> getJavaDefinition(QuteJavaDefinitionParams params) {
		MockQuteProject project = (MockQuteProject) getProject(params.getProjectUri());
		if (project == null) {
			return CompletableFuture.completedFuture(null);
		}
		String className = params.getSourceType();
		ResolvedJavaTypeInfo classInfo = project.getResolvedJavaClassSync(className);
		if (classInfo != null) {
			Range definitionRange = null;
			String fieldName = params.getSourceField();
			if (fieldName != null) {
				// Definition for field
				JavaFieldInfo fieldInfo = classInfo.findField(fieldName);
				if (fieldInfo != null) {
					definitionRange = JAVA_FIELD_RANGE;
				}
			} else {
				// Definition for method
				String methodName = params.getSourceMethod();
				if (methodName != null) {
					JavaMethodInfo methodInfo = classInfo.findMethod(methodName);
					if (methodInfo != null) {
						definitionRange = JAVA_METHOD_RANGE;
					}
				} else {
					// Definition for class
					definitionRange = JAVA_CLASS_RANGE;
				}
			}

			if (definitionRange != null) {
				String javeFileUri = className.replaceAll("[.]", "/") + ".java";
				Location location = new Location(javeFileUri, definitionRange);
				return CompletableFuture.completedFuture(location);
			}
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<DataModelProject<DataModelTemplate<DataModelParameter>>> getDataModelProject(
			QuteDataModelProjectParams params) {
		return CompletableFuture.completedFuture(null);
	}

}
