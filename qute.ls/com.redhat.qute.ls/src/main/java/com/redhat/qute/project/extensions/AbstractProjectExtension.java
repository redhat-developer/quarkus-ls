package com.redhat.qute.project.extensions;

import com.redhat.qute.commons.ProjectFeature;
import com.redhat.qute.project.datamodel.ExtendedDataModelProject;

public abstract class AbstractProjectExtension implements ProjectExtension {

	private final String extensionId;

	/** Reference to the parent data model project */
	private ExtendedDataModelProject dataModelProject;

	private ProjectFeature feature;

	private boolean enabled;

	private boolean onLoad;

	public AbstractProjectExtension(ProjectFeature feature) {
		this(feature.getName(), false);
		this.feature = feature;
	}

	public AbstractProjectExtension(String extensionId) {
		this(extensionId, true);
	}

	AbstractProjectExtension(String extensionId, boolean enabled) {
		this.extensionId = extensionId;
		this.enabled = enabled;
		this.onLoad = true;
	}

	@Override
	public final void initialize(ExtendedDataModelProject dataModelProject, ProjectExtensionContext context) {
		this.dataModelProject = dataModelProject;
		if (feature != null) {
			this.enabled = dataModelProject.hasProjectFeature(feature);
		}
		try {
			initialize(dataModelProject, onLoad, isEnabled(), context);
		} finally {
			onLoad = false;
		}
	}

	/**
	 * Checks if this extension is enabled.
	 * 
	 * <p>
	 * Returns true if the project has the project feature enabled or no feature,
	 * false otherwise. When disabled, all extension methods are skipped for
	 * performance.
	 * </p>
	 * 
	 * @return true if project feature is enabled for this project
	 */
	@Override
	public final boolean isEnabled() {
		return enabled;
	}

	@Override
	public final String getId() {
		return extensionId;
	}

	public final ExtendedDataModelProject getDataModelProject() {
		return dataModelProject;
	}

	protected abstract void initialize(ExtendedDataModelProject dataModelProject, boolean onLoad, boolean enabled,
			ProjectExtensionContext context);
}
