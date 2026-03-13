package com.redhat.qute.project.usages;

import com.redhat.qute.project.QuteProject;

public class IncludeUsagesRegistry extends UsagesRegistry<IncludeUsages> {

	private QuteProject project;

	public IncludeUsagesRegistry(QuteProject project) {
		this.project = project;
	}

	@Override
	protected IncludeUsages createUsages(String tagName) {
		return new IncludeUsages();
	}

	@Override
	public IncludeUsages getUsages(String templateId) {
		IncludeUsages usages = super.getUsages(templateId);
		if (usages != null) {
			return usages;
		}
		int index = templateId.indexOf('.');
		if (index != -1) {
			String id = templateId.substring(0, index);
			usages = super.getUsages(id);
			if (usages != null) {
				return usages;
			}
		} else {
			for (String variant : project.getTemplateVariants()) {
				String id = templateId + variant;
				usages = super.getUsages(id);
				if (usages != null) {
					return usages;
				}
			}
		}
		return null;
	}

}
