package com.redhat.qute.project;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.ValueResolver;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.ls.api.QuteDataModelProjectProvider;
import com.redhat.qute.project.datamodel.ExtendedDataModelProject;

public abstract class MockQuteProject extends QuteProject {

	private final List<ResolvedJavaTypeInfo> resolvedClassesCache;

	private List<DataModelTemplate<DataModelParameter>> templates;

	private final List<ValueResolver> resolvers;

	public MockQuteProject(ProjectInfo projectInfo, QuteDataModelProjectProvider dataModelProvider) {
		super(projectInfo, dataModelProvider);
		this.resolvedClassesCache = createResolvedClasses();
		this.templates = createTemplates();
		this.resolvers = createValueResolvers();
	}

	public ResolvedJavaTypeInfo getResolvedJavaClassSync(String className) {
		for (ResolvedJavaTypeInfo resolvedJavaType : resolvedClassesCache) {
			if (className.equals(resolvedJavaType.getClassName())) {
				return resolvedJavaType;
			}
		}
		return null;
	}

	public List<JavaTypeInfo> getJavaClasses() {
		return resolvedClassesCache //
				.stream() //
				.collect(Collectors.toList());
	}

	protected abstract List<ResolvedJavaTypeInfo> createResolvedClasses();

	protected abstract List<DataModelTemplate<DataModelParameter>> createTemplates();

	protected abstract List<ValueResolver> createValueResolvers();

	protected static JavaMemberInfo registerField(String fieldName, String fieldType,
			ResolvedJavaTypeInfo resolvedClass) {
		JavaFieldInfo member = new JavaFieldInfo();
		member.setName(fieldName);
		member.setType(fieldType);
		resolvedClass.getFields().add(member);
		return member;
	}

	protected static JavaMemberInfo registerMethod(String methodSignature, ResolvedJavaTypeInfo resolvedClass) {
		JavaMethodInfo member = new JavaMethodInfo();
		member.setSignature(methodSignature);
		resolvedClass.getMethods().add(member);
		return member;
	}

	protected static ResolvedJavaTypeInfo createResolvedJavaTypeInfo(String className,
			List<ResolvedJavaTypeInfo> cache, ResolvedJavaTypeInfo... extended) {
		return createResolvedJavaClassInfo(className, null, null, cache, extended);
	}

	protected static ResolvedJavaTypeInfo createResolvedJavaClassInfo(String className, String iterableType,
			String iterableOf, List<ResolvedJavaTypeInfo> cache, ResolvedJavaTypeInfo... extended) {
		ResolvedJavaTypeInfo resolvedClass = new ResolvedJavaTypeInfo();
		resolvedClass.setUri(className + ".java");
		resolvedClass.setClassName(className);
		resolvedClass.setIterableType(iterableType);
		resolvedClass.setIterableOf(iterableOf);
		resolvedClass.setFields(new ArrayList<>());
		resolvedClass.setMethods(new ArrayList<>());
		if (extended != null) {
			List<String> extendedTypes = Stream.of(extended).map(type -> type.getClassName())
					.collect(Collectors.toList());
			resolvedClass.setExtendedTypes(extendedTypes);
		}
		cache.add(resolvedClass);
		return resolvedClass;
	}

	@Override
	protected synchronized CompletableFuture<ExtendedDataModelProject> loadDataModelProject() {
		DataModelProject<DataModelTemplate<DataModelParameter>> project = new DataModelProject<DataModelTemplate<DataModelParameter>>();
		project.setTemplates(templates);
		project.setValueResolvers(resolvers);
		return CompletableFuture.completedFuture(new ExtendedDataModelProject(project));
	}

	protected static ValueResolver createValueResolver(String signature, String sourceType) {
		ValueResolver resolver = new ValueResolver();
		resolver.setSignature(signature);
		resolver.setSourceType(sourceType);
		return resolver;
	}
}
