package com.redhat.qute.project.usages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redhat.qute.parser.template.Parameter;

public abstract class UsagesRegistry<T extends ParameterUsages> {

	private final Map<String, T> usagesByTag;

	public UsagesRegistry() {
		this.usagesByTag = new HashMap<>();
	}

	/**
	 * Updates user tag usages for a given template.
	 *
	 * @param templateId  template URI
	 * @param usages      collected parameters per user tag
	 * @param oldTagNames previously known tag names for this template
	 */
	public void updateUsages(String templateId, Map<String, List<Parameter>> usages, Set<String> oldTagNames) {

		for (Map.Entry<String, List<Parameter>> entry : usages.entrySet()) {
			String tagName = entry.getKey();
			T tagUsages = getOrCreateUsages(tagName);
			tagUsages.updateUsages(templateId, entry.getValue());
		}

		// Handle removed usages
		for (String tagName : oldTagNames) {
			T tagUsages = usagesByTag.get(tagName);
			if (tagUsages != null) {
				tagUsages.updateUsages(templateId, List.of());
			}
		}
	}

	private T getOrCreateUsages(String tagName) {
		return usagesByTag.computeIfAbsent(tagName, this::createUsages);
	}

	public T getUsages(String tagName) {
		return usagesByTag.get(tagName);
	}

	protected void removeUsages(String name) {
		usagesByTag.remove(name);
	}

	protected abstract T createUsages(String tagName);

}
