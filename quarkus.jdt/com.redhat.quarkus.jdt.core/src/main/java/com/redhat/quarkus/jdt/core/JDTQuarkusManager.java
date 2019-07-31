/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.jdt.core;

import static io.quarkus.runtime.util.StringUtil.camelHumpsIterator;
import static io.quarkus.runtime.util.StringUtil.hyphenate;
import static io.quarkus.runtime.util.StringUtil.join;
import static io.quarkus.runtime.util.StringUtil.lowerCase;
import static io.quarkus.runtime.util.StringUtil.lowerCaseFirst;
import static io.quarkus.runtime.util.StringUtil.withoutSuffix;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.commons.QuarkusProjectInfo;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;

/**
 * 
 * @author Angelo ZERR
 * @see https://quarkus.io/guides/extension-authors-guide#configuration
 * @see https://quarkus.io/guides/application-configuration-guide
 */
public class JDTQuarkusManager {

	private static final String CONFIG_ROOT_ANNOTATION = "io.quarkus.runtime.annotations.ConfigRoot";
	private static final String CONFIG_GROUP_ANNOTATION = "io.quarkus.runtime.annotations.ConfigGroup";
	private static final String CONFIG_ITEM_ANNOTATION = "io.quarkus.runtime.annotations.ConfigItem";
	private static final String CONFIG_PROPERTY_ANNOTATION = "org.eclipse.microprofile.config.inject.ConfigProperty";

	private static final JDTQuarkusManager INSTANCE = new JDTQuarkusManager();

	public static JDTQuarkusManager getInstance() {
		return INSTANCE;
	}

	public QuarkusProjectInfo getQuarkusProjectInfo(String projectName) {
		IJavaProject javaProject = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(projectName);
		return getQuarkusProjectInfo(javaProject);
	}

	public QuarkusProjectInfo getQuarkusProjectInfo(IJavaProject project) {
		QuarkusProjectInfo info = new QuarkusProjectInfo();
		List<ExtendedConfigDescriptionBuildItem> quarkusProperties = new ArrayList<>();

		SearchPattern pattern = createQuarkusConfigSearchPattern();
		SearchEngine engine = new SearchEngine();
		try {
			engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
					createSearchScope(project), new SearchRequestor() {

						@Override
						public void acceptSearchMatch(SearchMatch match) throws CoreException {
							Object element = match.getElement();
							if (element instanceof IAnnotatable && element instanceof IJavaElement) {
								IJavaElement javaElement = (IJavaElement) element;
								processQuarkusAnnotation(javaElement, quarkusProperties);
							}
						}

					}, new NullProgressMonitor());
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		info.setQuarkusProject(quarkusProperties.size() > 0);
		info.setProperties(quarkusProperties);
		return info;
	}

	private void processQuarkusAnnotation(IJavaElement javaElement,
			List<ExtendedConfigDescriptionBuildItem> quarkusProperties) throws JavaModelException {
		IAnnotation[] annotations = ((IAnnotatable) javaElement).getAnnotations();
		for (IAnnotation annotation : annotations) {
			if (CONFIG_PROPERTY_ANNOTATION.contentEquals(annotation.getElementName())) {
				processConfigProperty(javaElement, annotation, quarkusProperties);
			} else if (CONFIG_ROOT_ANNOTATION.contentEquals(annotation.getElementName())) {
				processConfigRoot(javaElement, annotation, quarkusProperties);
			}
		}
	}

	private static void processConfigRoot(IJavaElement javaElement, IAnnotation annotation,
			List<ExtendedConfigDescriptionBuildItem> quarkusProperties) throws JavaModelException {
		ConfigPhase configPhase = getConfigPhase(annotation);
		String configRootAnnotationName = getConfigRootName(annotation);
		String extension = getExtensionName(getSimpleName(javaElement.getElementName()), configRootAnnotationName,
				configPhase);
		if (extension == null) {
			return;
		}
		String baseKey = "quarkus." + extension;
		processConfigGroup(javaElement, baseKey, quarkusProperties);
	}

	private static void processConfigGroup(IJavaElement javaElement, String baseKey,
			List<ExtendedConfigDescriptionBuildItem> quarkusProperties) throws JavaModelException {
		if (javaElement.getElementType() == IJavaElement.TYPE) {
			IJavaElement parent = javaElement.getParent();
			if (parent.getElementType() == IJavaElement.CLASS_FILE) {
				// IClassFile classFile = (IClassFile) parent;
				IJavaElement[] elements = ((IParent) javaElement).getChildren();
				for (IJavaElement child : elements) {
					if (child.getElementType() == IJavaElement.FIELD) {
						IField field = (IField) child;
						final IAnnotation configItemAnnotation = getAnnotation((IAnnotatable) field,
								CONFIG_ITEM_ANNOTATION);
						String name = configItemAnnotation == null ? hyphenate(field.getElementName())
								: (String) getAnnotationMemberValue(configItemAnnotation, "name");
						if (name == null) {
							name = ConfigItem.HYPHENATED_ELEMENT_NAME;
						}
						String subKey;
						boolean consume;
						if (name.equals(ConfigItem.PARENT)) {
							subKey = baseKey;
							consume = false;
						} else if (name.equals(ConfigItem.ELEMENT_NAME)) {
							subKey = baseKey + "." + field.getElementName();
							consume = true;
						} else if (name.equals(ConfigItem.HYPHENATED_ELEMENT_NAME)) {
							subKey = baseKey + "." + hyphenate(field.getElementName());
							consume = true;
						} else {
							subKey = baseKey + "." + name;
							consume = true;
						}
						final String defaultValue = configItemAnnotation == null ? ConfigItem.NO_DEFAULT
								: (String) getAnnotationMemberValue(configItemAnnotation, "defaultValue");

						IType fieldClass = getFieldType(field);
						final IAnnotation configGroupAnnotation = getAnnotation((IAnnotatable) fieldClass,
								CONFIG_GROUP_ANNOTATION);
						if (configGroupAnnotation != null) {
							processConfigGroup(fieldClass, subKey, quarkusProperties);
						} else {

							String propertyName = subKey;
							String type = fieldClass != null ? fieldClass.getFullyQualifiedName() : null;
							String docs = null;
							String source = field.getDeclaringType().getFullyQualifiedName() + "#"
									+ field.getElementName();

							ExtendedConfigDescriptionBuildItem property = new ExtendedConfigDescriptionBuildItem();
							property.setPropertyName(propertyName);
							property.setType(type);
							property.setDefaultValue(defaultValue);
							property.setDocs(docs);

							// Extra properties
							property.setSource(source);
							quarkusProperties.add(property);
						}
					}
				}
			}
		}
	}

	private static IType getFieldType(IField field) {
		try {
			String signature = field.getTypeSignature();
			IType primaryType = field.getTypeRoot().findPrimaryType();
			String name = JavaModelUtil.getResolvedTypeName(signature, primaryType);
			if (name == null) {
				return null;
			}
			return field.getJavaProject().findType(name);
		} catch (JavaModelException e) {
			return null;
		}
	}

	private static String getSimpleName(String elementName) {
		int index = elementName.lastIndexOf('.');
		return index != -1 ? elementName.substring(index + 1, elementName.length()) : elementName;
	}

	private static String getConfigRootName(IAnnotation annotation) {
		String value = (String) getAnnotationMemberValue(annotation, "name");
		if (value != null) {
			return value;
		}
		return ConfigItem.HYPHENATED_ELEMENT_NAME;
	}

	private static ConfigPhase getConfigPhase(IAnnotation annotation) {
		String value = (String) getAnnotationMemberValue(annotation, "phase");
		if (value != null) {
			if (value.endsWith(ConfigPhase.RUN_TIME.name())) {
				return ConfigPhase.RUN_TIME;
			}
			if (value.endsWith(ConfigPhase.BUILD_AND_RUN_TIME_FIXED.name())) {
				return ConfigPhase.BUILD_AND_RUN_TIME_FIXED;
			}
		}
		return ConfigPhase.BUILD_TIME;
	}

	private static String getExtensionName(String configRootClassSimpleName, String configRootAnnotationName,
			ConfigPhase configPhase) {
		// See
		// https://github.com/quarkusio/quarkus/blob/master/core/deployment/src/main/java/io/quarkus/deployment/configuration/ConfigDefinition.java#L173
		// registerConfigRoot
		final String containingName;
		if (configPhase == ConfigPhase.RUN_TIME) {
			containingName = join(withoutSuffix(lowerCaseFirst(camelHumpsIterator(configRootClassSimpleName)), "Config",
					"Configuration", "RunTimeConfig", "RunTimeConfiguration"));
		} else {
			containingName = join(withoutSuffix(lowerCaseFirst(camelHumpsIterator(configRootClassSimpleName)), "Config",
					"Configuration", "BuildTimeConfig", "BuildTimeConfiguration"));
		}
		final String name = configRootAnnotationName;
		final String rootName;
		if (name.equals(ConfigItem.PARENT)) {
			// throw reportError(configRoot, "Root cannot inherit parent name because it has
			// no parent");
			return null;
		} else if (name.equals(ConfigItem.ELEMENT_NAME)) {
			rootName = containingName;
		} else if (name.equals(ConfigItem.HYPHENATED_ELEMENT_NAME)) {
			rootName = join("-",
					withoutSuffix(lowerCase(camelHumpsIterator(configRootClassSimpleName)), "config", "configuration"));
		} else {
			rootName = name;
		}
		return rootName;
	}

	private static void processConfigProperty(IJavaElement javaElement, IAnnotation annotation,
			List<ExtendedConfigDescriptionBuildItem> quarkusProperties) {
		String name = javaElement.getElementName();
		// getAnnotation(java, annotationName)
		System.err.println(name);
	}

	private static IAnnotation getAnnotation(IAnnotatable annotatable, String annotationName)
			throws JavaModelException {
		if (annotatable == null) {
			return null;
		}
		IAnnotation[] annotations = annotatable.getAnnotations();
		for (IAnnotation annotation : annotations) {
			if (annotationName.equals(annotation.getElementName())) {
				return annotation;
			}
		}
		return null;
	}

	private static Object getAnnotationMemberValue(IAnnotation annotation, String memberName) {
		try {
			for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
				if (memberName.equals(pair.getMemberName())) {
					return pair.getValue();
				}
			}
		} catch (JavaModelException e) {
			// e.printStackTrace();
		}
		return null;
	}

	private static IJavaSearchScope createSearchScope(IJavaProject project) throws JavaModelException {
		return SearchEngine.createJavaSearchScope(new IJavaElement[] { project },
				/*IJavaSearchScope.SOURCES |*/ IJavaSearchScope.APPLICATION_LIBRARIES);
	}

	private static SearchPattern createQuarkusConfigSearchPattern() {
		// Pattern to search @ConfigRoot annotation
		SearchPattern configRootPattern = SearchPattern.createPattern(CONFIG_ROOT_ANNOTATION,
				IJavaSearchConstants.ANNOTATION_TYPE, IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE,
				SearchPattern.R_EXACT_MATCH);
		// Pattern to search @ConfigItem annotation
		SearchPattern configItemPattern = SearchPattern.createPattern(CONFIG_ITEM_ANNOTATION,
				IJavaSearchConstants.ANNOTATION_TYPE, IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE,
				SearchPattern.R_EXACT_MATCH);
		// Pattern to search @ConfigProperty annotation
		SearchPattern configPropertyPattern = SearchPattern.createPattern(CONFIG_PROPERTY_ANNOTATION,
				IJavaSearchConstants.ANNOTATION_TYPE, IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE,
				SearchPattern.R_EXACT_MATCH);
		return SearchPattern.createOrPattern(configRootPattern,
				SearchPattern.createOrPattern(configItemPattern, configPropertyPattern));
	}
}
