package com.redhat.qute.project.extensions;

import com.redhat.qute.commons.ProjectFeature;
import com.redhat.qute.project.datamodel.ExtendedDataModelProject;

public abstract class AbstractProjectExtension implements ProjectExtension {

	private final String extensionId;

	/** Reference to the parent data model project */
	private ExtendedDataModelProject dataModelProject;

	private ProjectFeature feature;

	private boolean enabled;

	public AbstractProjectExtension(ProjectFeature feature) {
		this.extensionId = feature.getName();
		this.feature = feature;
		this.enabled = false;
	}

	public AbstractProjectExtension(String extensionId) {
		this.extensionId = extensionId;
		this.enabled = true;
	}

	@Override
	public final void init(ExtendedDataModelProject dataModelProject) {
		this.dataModelProject = dataModelProject;
		if (feature != null) {
			this.enabled = dataModelProject.hasProjectFeature(feature);
		}
		doInit(dataModelProject);
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

	public ExtendedDataModelProject getDataModelProject() {
		return dataModelProject;
	}

	protected abstract void doInit(ExtendedDataModelProject dataModelProject);
}
