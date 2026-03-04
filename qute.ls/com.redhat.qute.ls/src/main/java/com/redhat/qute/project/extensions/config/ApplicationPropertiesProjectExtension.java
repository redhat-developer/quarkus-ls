package com.redhat.qute.project.extensions.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.redhat.qute.project.datamodel.ExtendedDataModelProject;
import com.redhat.qute.project.extensions.ProjectExtension;

public class ApplicationPropertiesProjectExtension implements ProjectExtension {

	public static final String APPLICATION_PROPERTIES_PROJECT_EXTENSION_ID = "application-properties";

	private List<PropertiesFile> propertiesFiles;

	private ExtendedDataModelProject project;

	@Override
	public void init(ExtendedDataModelProject project) {
		this.project = project;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getId() {
		return APPLICATION_PROPERTIES_PROJECT_EXTENSION_ID;
	}

	public String getConfig(PropertyConfig property) {
		initFilesIfNeeded();
		if (propertiesFiles != null) {
			for (PropertiesFile propertiesFile : propertiesFiles) {
				String value = propertiesFile.getProperty(property.getName());
				if (value != null) {
					return value;
				}
			}
		}
		return property.getDefaultValue();
	}

	private void initFilesIfNeeded() {
		if (project == null) {
			return;
		}
		if (propertiesFiles == null) {
			propertiesFiles = new ArrayList<>();
		}
		// config/application.properties
		Path projectFolder = project.getProjectFolder();
		if (projectFolder != null) {
			propertiesFiles.add(new PropertiesFile(projectFolder.resolve("config/application-properties")));
		}
		Set<Path> sourcePaths = project.getSourcePaths();
		if (sourcePaths != null && !sourcePaths.isEmpty()) {
			for (Path sourcePath : sourcePaths) {
				propertiesFiles.add(new PropertiesFile(sourcePath.resolve("application-properties")));
			}
		}

	}
}
