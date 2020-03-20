/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core;

import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getOptionalTypeParameter;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EnumTypeAdapter;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.microprofile.commons.ClasspathKind;
import com.redhat.microprofile.commons.DocumentFormat;
import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;
import com.redhat.microprofile.commons.metadata.ItemHint;
import com.redhat.microprofile.commons.metadata.ItemHint.ValueHint;
import com.redhat.microprofile.commons.metadata.ItemMetadata;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;

/**
 * Generator to generate JSON properties and definitions used for Junit test of
 * MicroProfile LS.
 * 
 * @author Angelo ZERR
 *
 */
public class GenerateAllPropertiesAndDefinition extends BasePropertiesManagerTest {

	private static final Logger LOGGER = Logger.getLogger(GenerateAllPropertiesAndDefinition.class.getName());

	static class PropertyDefinition {

		private String sourceType;

		private String sourceField;

		private String sourceMethod;

		private Location location;

		public String getSourceType() {
			return sourceType;
		}

		public void setSourceType(String sourceType) {
			this.sourceType = sourceType;
		}

		public String getSourceField() {
			return sourceField;
		}

		public void setSourceField(String sourceField) {
			this.sourceField = sourceField;
		}

		public String getSourceMethod() {
			return sourceMethod;
		}

		public void setSourceMethod(String sourceMethod) {
			this.sourceMethod = sourceMethod;
		}

		public Location getLocation() {
			return location;
		}

		public void setLocation(Location location) {
			this.location = location;
		}
	}

	/**
	 * This method generates the
	 * 
	 * <ul>
	 * <li>all-quarkus-properties.json used for Junit test in MicroProfile LS ->
	 * https://github.com/redhat-developer/quarkus-ls/blob/master/microprofile.ls/com.redhat.microprofile.ls/src/test/resources/com/redhat/microprofile/services/all-quarkus-properties.json</li>
	 * </ul>
	 * 
	 * <p>
	 * To generate this file:
	 * </p>
	 * 
	 * <ul>
	 * <li>Uncomment @Ignore</li>
	 * <li>Start the method with JUnit PDE Run</li>
	 * </ul>
	 * 
	 * @throws JavaModelException
	 * @throws CoreException
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void generateAllQuarkusExtensionProperties() throws JavaModelException, CoreException, Exception {
		generateJsonFiles(MavenProjectName.all_quarkus_extensions, JDT_UTILS, false);
	}

	/**
	 * This method generates the
	 * 
	 * <ul>
	 * <li>all-quarkus-properties.json used for Junit test in MicroProfile LS ->
	 * https://github.com/redhat-developer/quarkus-ls/blob/master/microprofile.ls/com.redhat.microprofile.ls/src/test/resources/com/redhat/microprofile/services/all-quarkus-properties.json</li>
	 * <li>all-quarkus-definitions.json used for Junit test in MicroProfile LS ->
	 * https://github.com/redhat-developer/quarkus-ls/blob/master/microprofile.ls/com.redhat.microprofile.ls/src/test/resources/com/redhat/microprofile/services/all-quarkus-definitions.json</li>
	 * </ul>
	 * 
	 * <p>
	 * To generate those files:
	 * </p>
	 * 
	 * <ul>
	 * <li>Uncomment @Ignore</li>
	 * <li>Start the method with JUnit PDE Run</li>
	 * </ul>
	 * 
	 * @throws JavaModelException
	 * @throws CoreException
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void generateAllQuarkusExtensionPropertiesAndDefinitions()
			throws JavaModelException, CoreException, Exception {
		generateJsonFiles(MavenProjectName.all_quarkus_extensions, JDT_UTILS, true);
	}

	private void generateJsonFiles(MavenProjectName mavenProject, IJDTUtils utils, boolean generateDefinition)
			throws JavaModelException, CoreException, Exception {

		Gson gson = new GsonBuilder().registerTypeAdapterFactory(new EnumTypeAdapter.Factory()).setPrettyPrinting()
				.create();

		long start = System.currentTimeMillis();
		LOGGER.info("Start generating all-quarkus-properties.json");
		IJavaProject javaProject = loadMavenProject(mavenProject);
		MicroProfileProjectInfo info = PropertiesManager.getInstance().getMicroProfileProjectInfo(javaProject,
				MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES, ClasspathKind.SRC, utils, DocumentFormat.Markdown,
				new NullProgressMonitor());

		String baseDir = "../../microprofile.ls/com.redhat.microprofile.ls/src/test/resources/com/redhat/microprofile/services/";
		// Generate all-quarkus-properties.json
		String propertiesAsJson = gson.toJson(info);
		Files.write(Paths.get(baseDir + "all-quarkus-properties.json"), propertiesAsJson.getBytes());

		LOGGER.info("End generating all-quarkus-properties.json in " + (System.currentTimeMillis() - start) + "ms");

		if (generateDefinition) {
			final IJavaProject fakeJavaProject = PropertiesManager.getInstance().configureSearchClasspath(javaProject,
					true, MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES, new NullProgressMonitor());

			start = System.currentTimeMillis();
			LOGGER.info("Start generating all-quarkus-definitions.json");
			// Generate all-quarkus-definitions.json

			// Enable classFileContentsSupport to generate jdt Location
			enableClassFileContentsSupport();

			long definitionsCount = info.getProperties().size()
					+ info.getHints().stream().map(hint -> hint.getValues().size()).count();
			List<PropertyDefinition> definitions = new ArrayList<>();

			AtomicLong current = new AtomicLong(0);
			for (ItemMetadata item : info.getProperties()) {
				String sourceType = item.getSourceType();
				String sourceField = item.getSourceField();
				String sourceMethod = item.getSourceMethod();
				definitions.add(createDefinition(fakeJavaProject, sourceType, sourceField, sourceMethod, utils, current,
						definitionsCount));
			}

			for (ItemHint hint : info.getHints()) {
				for (ValueHint value : hint.getValues()) {
					String sourceType = getSourceType(
							value.getSourceType() != null ? value.getSourceType() : hint.getSourceType());
					String sourceField = value.getValue();
					String sourceMethod = null;
					definitions.add(createDefinition(fakeJavaProject, sourceType, sourceField, sourceMethod, utils,
							current, definitionsCount));
				}
			}

			String definitionsAsJson = gson.toJson(definitions);
			Files.write(Paths.get(baseDir + "all-quarkus-definitions.json"), definitionsAsJson.getBytes());

			LOGGER.info(
					"End generating all-quarkus-definitions.json in " + (System.currentTimeMillis() - start) + "ms");
		}
	}

	private String getSourceType(String sourceType) {
		if (sourceType == null) {
			return sourceType;
		}
		String type = getOptionalTypeParameter(sourceType);
		return type != null ? type : sourceType;
	}

	private static PropertyDefinition createDefinition(final IJavaProject fakeJavaProject, String sourceType,
			String sourceField, String sourceMethod, IJDTUtils utils, AtomicLong current, long definitionsCount) {
		LOGGER.info("Compute definition for " + sourceType + "#" + sourceField + "#" + sourceMethod + " ("
				+ current.incrementAndGet() + "/" + definitionsCount + ")");
		PropertyDefinition definition = new PropertyDefinition();
		Location location = null;

		try {
			location = PropertiesManager.getInstance().findPropertyLocation(fakeJavaProject, sourceType, sourceField,
					sourceMethod, utils, new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
		definition.setSourceType(sourceType);
		definition.setSourceField(sourceField);
		definition.setSourceMethod(sourceMethod);
		definition.setLocation(location);
		return definition;
	}

	private static void enableClassFileContentsSupport() {
		Map<String, Object> extendedClientCapabilities = new HashMap<>();
		extendedClientCapabilities.put("classFileContentsSupport", "true");
		JavaLanguageServerPlugin.getPreferencesManager().updateClientPrefences(new ClientCapabilities(),
				extendedClientCapabilities);
	}
}
