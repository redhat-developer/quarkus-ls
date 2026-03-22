package com.redhat.qute.project.usages;

import java.util.List;

import com.redhat.qute.parser.NodeBase;
import com.redhat.qute.parser.template.Parameter;
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

	public IncludeUsages getFragmentUsages(String templateId, String fragmentId) {
		return getUsages(templateId, fragmentId);
	}

	public Parameter findFragmentParameter(String templateId, String fragmentId, String parameterName) {
		List<? extends NodeBase<?>> parameters = findFragmentParameters(templateId, fragmentId, parameterName);
		if (parameters != null) {
			for (NodeBase<?> parameter : parameters) {
				if (parameter instanceof Parameter) {
					return (Parameter) parameter;
				}
			}
		}
		return null;
	}

	public List<? extends NodeBase<?>> findFragmentParameters(String templateId, String fragmentId,
			String parameterName) {
		IncludeUsages usages = getFragmentUsages(templateId, fragmentId);
		return usages != null ? usages.findParameters(parameterName) : null;
	}

	@Override
	public IncludeUsages getUsages(String templateId) {
		return getUsages(templateId, null);
	}

	private IncludeUsages getUsages(String templateId, String fragmentId) {
		IncludeUsages usages = super.getUsages(getId(templateId, fragmentId));
		if (usages != null) {
			return usages;
		}
		int index = templateId.indexOf('.');
		if (index != -1) {
			String id = templateId.substring(0, index);
			usages = super.getUsages(getId(id, fragmentId));
			if (usages != null) {
				return usages;
			}
		} else {
			for (String variant : project.getTemplateVariants()) {
				String id = templateId + variant;
				usages = super.getUsages(getId(id, fragmentId));
				if (usages != null) {
					return usages;
				}
			}
		}
		return null;
	}

	private static String getId(String templateId, String fragmentId) {
		if (fragmentId == null) {
			return templateId;
		}
		return templateId + "$" + fragmentId;
	}

}
