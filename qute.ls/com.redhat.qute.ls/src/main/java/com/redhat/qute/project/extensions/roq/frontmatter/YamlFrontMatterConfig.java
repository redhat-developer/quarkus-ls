package com.redhat.qute.project.extensions.roq.frontmatter;

import com.redhat.qute.parser.yaml.YamlProperty;
import com.redhat.qute.parser.yaml.YamlScalar;
import com.redhat.qute.project.extensions.config.PropertyConfig;

public class YamlFrontMatterConfig {

	public static final PropertyConfig LAYOUT_PROPERTY = new PropertyConfig("layout", "", "default");
	public static final PropertyConfig TITLE_PROPERTY = new PropertyConfig("title", "", "");

	public static final PropertyConfig PAGINATE_PROPERTY = new PropertyConfig("paginate", "", "posts");

	private YamlFrontMatterConfig() {

	}

	public static boolean isPropertyConfig(YamlProperty property, PropertyConfig propertyConfig) {
		return isPropertyConfig(property, propertyConfig.getName());
	}

	public static boolean isPropertyConfig(YamlProperty property, String propertyName) {
		YamlScalar key = property.getKey();
		if (key == null) {
			return false;
		}
		String propertyKey = key.getValue();
		return propertyName.equals(propertyKey);
	}

}
