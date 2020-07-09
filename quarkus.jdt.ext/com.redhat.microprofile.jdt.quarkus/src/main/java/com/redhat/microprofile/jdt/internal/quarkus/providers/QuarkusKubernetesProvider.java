/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.quarkus.providers;

import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.findType;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getDefaultValue;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getEnclosedType;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getPropertyType;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getResolvedResultTypeName;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getSourceMethod;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.isSimpleFieldType;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.SearchPattern;

import com.redhat.microprofile.commons.DocumentFormat;
import com.redhat.microprofile.jdt.core.AbstractTypeDeclarationPropertiesProvider;
import com.redhat.microprofile.jdt.core.BuildingScopeContext;
import com.redhat.microprofile.jdt.core.IPropertiesCollector;
import com.redhat.microprofile.jdt.core.SearchContext;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;

import io.quarkus.runtime.util.StringUtil;

/**
 * Properties provider to collect Quarkus properties from the io dekorate config
 * class for Kubernetes, OpenShift, Docker and S2i and generates the same
 * properties than https://quarkus.io/guides/kubernetes#configuration-options
 * 
 * <p>
 * As Quarkus Kubernetes doesn't use a standard mechanism with
 * Quarkus @ConfigRoot annotation to collect properties. Indeed the
 * io.quarkus.kubernetes.deployment.KubernetesProcessor which manages Quarkus
 * kubernetes.*, openshift.* properties uses the io dekorate project, so we need
 * to introspect the io dekorate config class like
 * io.dekorate.kubernetes.config.KubernetesConfig to generate kubernetes,
 * openshift, docker and s2i properties.
 * </p>
 * 
 * <p>
 * io.dekorate.kubernetes.config.KubernetesConfig i sgenerated from the
 * annotation io.dekorate.kubernetes.annotation.KubernetesApplication. it's
 * better to use this annotation type to get default value and javadoc.
 * </p>
 * 
 * @author Angelo ZERR
 * @see <a href=
 *      "https://quarkus.io/guides/kubernetes#configuration-options">https://quarkus.io/guides/kubernetes#configuration-options</a>
 */
public class QuarkusKubernetesProvider extends AbstractTypeDeclarationPropertiesProvider {

	private static final String S2I_PREFIX = "s2i";
	private static final String DOCKER_PREFIX = "docker";
	private static final String OPENSHIFT_PREFIX = "openshift";
	private static final String KUBERNETES_PREFIX = "kubernetes";
	private static final String KUBERNETES_APPLICATION_ANNOTATION = "io.dekorate.kubernetes.annotation.KubernetesApplication";
	private static final String OPENSHIFT_APPLICATION_ANNOTATION = "io.dekorate.openshift.annotation.OpenshiftApplication";
	private static final String S2I_BUILD_ANNOTATION = "io.dekorate.s2i.annotation.S2iBuild";
	private static final String DOCKER_BUILD_ANNOTATION = "io.dekorate.docker.annotation.DockerBuild";

	@Override
	protected String[] getTypeNames() {
		return new String[] { KUBERNETES_APPLICATION_ANNOTATION, DOCKER_BUILD_ANNOTATION,
				OPENSHIFT_APPLICATION_ANNOTATION, S2I_BUILD_ANNOTATION };
	}

	@Override
	public void beginBuildingScope(BuildingScopeContext context, IProgressMonitor monitor) {
		// The kubernetes support is only available if quarkus-kubernetes artifact is
		// declared in the pom.xml
		// When quarkus-kubernetes is declared, this JAR declares the deployment JAR
		// quarkus-kubernetes-deployment
		// This quarkus-kubernetes-deployment artifact has some dependencies to
		// io.dekorate

		// In other words, to add
		// io.dekorate.kubernetes.annotation.KubernetesApplication class in the search
		// classpath,
		// the dependencies of quarkus-kubernetes-deployment artifact must be downloaded
		QuarkusContext quarkusContext = QuarkusContext.getQuarkusContext(context);
		quarkusContext.collectDependenciesFor("quarkus-kubernetes-deployment");
	}

	@Override
	protected SearchPattern createSearchPattern(String annotationName) {
		return createAnnotationTypeDeclarationSearchPattern(annotationName);
	}

	@Override
	protected void processClass(IType configType, String className, SearchContext context, IProgressMonitor monitor)
			throws JavaModelException {
		String configPrefix = getConfigPrefix(className);
		if (configPrefix != null) {
			IPropertiesCollector collector = context.getCollector();
			IJDTUtils utils = context.getUtils();
			DocumentFormat documentFormat = context.getDocumentFormat();
			collectProperties(configPrefix, configType, collector, utils, documentFormat, monitor);
			// We need to hard code some properties because KubernetesProcessor does that
			switch (configPrefix) {
			case KUBERNETES_PREFIX:
				// kubernetes.deployment.target
				// see
				// https://github.com/quarkusio/quarkus/blob/44e5e2e3a642d1fa7af9ddea44b6ff8d37e862b8/extensions/kubernetes/deployment/src/main/java/io/quarkus/kubernetes/deployment/KubernetesProcessor.java#L94
				super.addItemMetadata(collector, "kubernetes.deployment.target", "java.lang.String", //
						"To enable the generation of OpenShift resources, you need to include OpenShift in the target platforms: `kubernetes.deployment.target=openshift`."
								+ System.lineSeparator()
								+ "If you need to generate resources for both platforms (vanilla Kubernetes and OpenShift), then you need to include both (coma separated)."
								+ System.lineSeparator() + "`kubernetes.deployment.target=kubernetes, openshift`.",
						null, null, null, KUBERNETES_PREFIX, null, true);
				// kubernetes.registry
				// see
				// https://github.com/quarkusio/quarkus/blob/44e5e2e3a642d1fa7af9ddea44b6ff8d37e862b8/extensions/kubernetes/deployment/src/main/java/io/quarkus/kubernetes/deployment/KubernetesProcessor.java#L103
				super.addItemMetadata(collector, "kubernetes.registry", "java.lang.String", //
						"Specify the docker registry.", null, null, null, null, null, true);
				break;
			case OPENSHIFT_PREFIX:
				// openshift.registry
				super.addItemMetadata(collector, "openshift.registry", "java.lang.String", //
						"Specify the docker registry.", null, null, null, null, null, true);
				break;
			}
		}
	}

	private void collectProperties(String prefix, IType configType, IPropertiesCollector collector, IJDTUtils utils,
			DocumentFormat documentFormat, IProgressMonitor monitor) throws JavaModelException {
		String sourceType = configType.getFullyQualifiedName();
		IMethod[] methods = configType.getMethods();
		for (IMethod method : methods) {
			String resultTypeName = getResolvedResultTypeName(method);
			IType resultTypeClass = findType(method.getJavaProject(), resultTypeName);
			String methodName = method.getElementName();
			String propertyName = prefix + "." + StringUtil.hyphenate(methodName);
			boolean isArray = Signature.getArrayCount(method.getReturnType()) > 0;
			if (isArray) {
				propertyName += "[*]";
			}
			if (isSimpleFieldType(resultTypeClass, resultTypeName)) {
				String type = getPropertyType(resultTypeClass, resultTypeName);
				String description = utils.getJavadoc(method, documentFormat);
				String sourceMethod = getSourceMethod(method);
				String defaultValue = getDefaultValue(method);
				String extensionName = null;

				// Enumerations
				IType enclosedType = getEnclosedType(resultTypeClass, resultTypeName, method.getJavaProject());
				super.updateHint(collector, enclosedType);

				super.addItemMetadata(collector, propertyName, type, description, sourceType, null, sourceMethod,
						defaultValue, extensionName, method.isBinary());
			} else {
				collectProperties(propertyName, resultTypeClass, collector, utils, documentFormat, monitor);
			}
		}
	}

	private static String getConfigPrefix(String configClassName) {
		switch (configClassName) {
		case KUBERNETES_APPLICATION_ANNOTATION:
			return KUBERNETES_PREFIX;
		case OPENSHIFT_APPLICATION_ANNOTATION:
			return OPENSHIFT_PREFIX;
		case DOCKER_BUILD_ANNOTATION:
			return DOCKER_PREFIX;
		case S2I_BUILD_ANNOTATION:
			return S2I_PREFIX;
		default:
			return null;
		}
	}

}
