package com.redhat.qute.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.ValueResolver;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.ls.api.QuteDataModelProjectProvider;

public class QuteQuickstartProject extends MockQuteProject {

	public final static String PROJECT_URI = "qute-quickstart";

	public QuteQuickstartProject(ProjectInfo projectInfo, QuteDataModelProjectProvider dataModelProvider) {
		super(projectInfo, dataModelProvider);
	}

	@Override
	protected List<ResolvedJavaTypeInfo> createResolvedClasses() {
		List<ResolvedJavaTypeInfo> cache = new ArrayList<>();

		createResolvedJavaClassInfo("org.acme", cache).setUri(null);

		ResolvedJavaTypeInfo string = createResolvedJavaClassInfo("java.lang.String", cache);
		registerField("UTF16", "byte", string);
		createResolvedJavaClassInfo("java.lang.Boolean", cache);
		createResolvedJavaClassInfo("java.lang.Integer", cache);
		createResolvedJavaClassInfo("java.lang.Double", cache);
		createResolvedJavaClassInfo("java.lang.Long", cache);
		createResolvedJavaClassInfo("java.lang.Float", cache);
		createResolvedJavaClassInfo("java.math.BigInteger", cache);

		ResolvedJavaTypeInfo review = createResolvedJavaClassInfo("org.acme.Review", cache);
		registerField("name", "java.lang.String", review);
		registerField("average", "java.lang.Integer", review);

		ResolvedJavaTypeInfo item = createResolvedJavaClassInfo("org.acme.Item", cache);
		registerField("name", "java.lang.String", item);
		registerField("price", "java.math.BigInteger", item);
		registerField("review", "org.acme.Review", item);
		registerMethod("isAvailable() : java.lang.Boolean", item);
		registerMethod("getReview2() : org.acme.Review", item);
		registerMethod("getReviews() : java.util.List<org.acme.Review>", item);
		createResolvedJavaClassInfo("java.util.List<org.acme.Review>", "java.util.List", "org.acme.Review", cache);
		registerField("derivedItems", "java.util.List<org.acme.Item>", item);
		registerField("derivedItemArray", "org.acme.Item[]", item);

		createResolvedJavaClassInfo("java.util.List<org.acme.Item>", "java.util.List", "org.acme.Item", cache);
		createResolvedJavaClassInfo("org.acme.Item[]", null, "org.acme.Item", cache);

		ResolvedJavaTypeInfo list = createResolvedJavaClassInfo("java.util.List", cache);
		list.setExtendedTypes(Arrays.asList("java.lang.Iterable"));
		registerMethod("size() : int", list);

		ResolvedJavaTypeInfo itemResource = createResolvedJavaClassInfo("org.acme.ItemResource", cache);
		registerMethod("discountedPrice(item : org.acme.Item) : java.math.BigDecimal", itemResource);

		return cache;
	}

	@Override
	protected List<DataModelTemplate<DataModelParameter>> createTemplates() {
		List<DataModelTemplate<DataModelParameter>> templates = new ArrayList<>();
		DataModelTemplate<DataModelParameter> template = new DataModelTemplate<DataModelParameter>();
		template.setTemplateUri("src/main/resources/templates/ItemResource/items");
		template.setSourceType("org.acme.qute.ItemResource$Templates");
		template.setSourceMethod("items");
		templates.add(template);

		DataModelParameter parameter = new DataModelParameter();
		parameter.setKey("items");
		parameter.setSourceType("java.util.List<org.acme.Item>");
		template.addParameter(parameter);

		return templates;
	}

	@Override
	protected List<ValueResolver> createValueResolvers() {
		List<ValueResolver> resolvers = new ArrayList<>();
		resolvers.add(createValueResolver("discountedPrice(item : org.acme.Item) : java.math.BigDecimal",
				"org.acme.ItemResource"));
		return resolvers;
	}
}
