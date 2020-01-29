package com.redhat.microprofile.ls;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfilePropertiesChangeEvent;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;
import com.redhat.microprofile.commons.metadata.ItemMetadata;
import com.redhat.microprofile.model.parser.ParseContext;
import com.redhat.microprofile.model.parser.PropertiesHandler;
import com.redhat.microprofile.model.parser.PropertiesParser;

class ApplicationPropertiesComparator {

	public static MicroProfilePropertiesChangeEvent compare(String initialText, String currentText,
			MicroProfileProjectInfo currentProjectInfo) {
		if (currentProjectInfo == null) {
			return null;
		}
		// Update values
		Map<String, PropertyAndValues> properties = currentProjectInfo.getProperties().stream()
				.filter(property -> property.getOnChange() != null && property.getOnChange().isEvictCache())
				.collect(Collectors.toMap(ItemMetadata::getName, property -> new PropertyAndValues()));
		if (properties.isEmpty()) {
			return null;
		}
		// Update first value for each properties
		PropertiesParser parser = new PropertiesParser();
		parser.parse(initialText, new PropertiesTracker(properties, false), null);
		// Update second value for each properties
		parser = new PropertiesParser();
		parser.parse(currentText, new PropertiesTracker(properties, true), null);

		if (properties.values().stream().filter(PropertyAndValues::isDirty).count() > 0) {
			MicroProfilePropertiesChangeEvent event = new MicroProfilePropertiesChangeEvent();
			event.setProjectURIs(new HashSet<>(Arrays.asList(currentProjectInfo.getProjectURI())));
			event.setType(MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);
			return event;
		}
		return null;

	}

	static class PropertyAndValues {

		private String value1;

		private String value2;

		public void setValue(String propertyValue, boolean updateFirstValue) {
			if (updateFirstValue) {
				this.value1 = propertyValue;
			} else {
				this.value2 = propertyValue;
			}
		}

		public boolean isDirty() {
			return !Objects.equals(value1, value2);
		}

	}

	static class PropertiesTracker implements PropertiesHandler {

		private final Map<String, PropertyAndValues> properties;

		private final boolean updateFirstValue;

		private Integer startNameOffset;

		private Integer startValueOffset;

		private PropertyAndValues current;

		public PropertiesTracker(Map<String, PropertyAndValues> properties, boolean updateFirstValue) {
			this.properties = properties;
			this.updateFirstValue = updateFirstValue;
		}

		@Override
		public void startDocument(ParseContext context) {

		}

		@Override
		public void endDocument(ParseContext context) {

		}

		@Override
		public void startProperty(ParseContext context) {
			this.current = null;
			this.startNameOffset = null;
		}

		@Override
		public void endProperty(ParseContext context) {
			this.current = null;
			this.startNameOffset = null;
		}

		@Override
		public void startPropertyName(ParseContext context) {
			this.startNameOffset = context.getLocationOffset();
		}

		@Override
		public void endPropertyName(ParseContext context) {
			String propertyName = context.getText(startNameOffset, context.getLocationOffset());
			current = properties.get(propertyName);
		}

		@Override
		public void startPropertyValue(ParseContext context) {
			if (current != null) {
				this.startValueOffset = context.getLocationOffset();
			}
		}

		@Override
		public void endPropertyValue(ParseContext context) {
			if (current != null) {
				String propertyValue = context.getText(startValueOffset, context.getLocationOffset());
				current.setValue(propertyValue, updateFirstValue);
			}

		}

		@Override
		public void startComment(ParseContext context) {

		}

		@Override
		public void endComment(ParseContext context) {

		}

		@Override
		public void blankLine(ParseContext context) {

		}

		@Override
		public void delimiterAssign(ParseContext context) {

		}

	}
}
