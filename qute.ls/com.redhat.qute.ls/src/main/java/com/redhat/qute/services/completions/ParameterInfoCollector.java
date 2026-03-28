package com.redhat.qute.services.completions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.parser.template.ASTVisitor;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.ParameterDeclaration;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.ForSection;
import com.redhat.qute.parser.template.sections.LetSection;
import com.redhat.qute.parser.template.sections.SetSection;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.datamodel.ExtendedDataModelParameter;
import com.redhat.qute.project.datamodel.ExtendedDataModelTemplate;
import com.redhat.qute.project.datamodel.resolvers.ValueResolver;
import com.redhat.qute.utils.StringUtils;

/**
 * AST visitor that collects all parameters available at a given offset in a
 * Qute template, for use in user tag parameter completion.
 *
 * <p>
 * Two indexes are maintained:
 * <ul>
 * <li>{@code nameToType} — maps a parameter name to its Java type. Used to
 * check if a parameter name matches a user tag parameter and generate a
 * completion without a value.</li>
 * <li>{@code typeToNames} — maps a Java type to the set of parameter names that
 * have that type. Used to generate a list of candidate names for a given user
 * tag parameter type.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Scope is respected: only parameters that are visible at the given completion
 * offset are collected. A parameter is visible if it is declared before the
 * offset and in an enclosing or sibling scope.
 * </p>
 */
public class ParameterInfoCollector extends ASTVisitor {

	// Maps parameter name -> Java type
	// ex: "foo" -> "org.acme.Foo"
	// Used to check if a name matches a user tag parameter type
	private Map<String, String> nameToType;

	// Maps Java type -> set of parameter names with that type
	// ex: "org.acme.Foo" -> {"foo", "myFoo"}
	// Used to suggest candidate names for a given user tag parameter type
	private Map<String, Set<String>> typeToNames;

	// The offset at which completion is requested.
	// Only parameters visible (declared before this offset
	// in an enclosing or sibling scope) are collected.
	private final int completionOffset;

	public ParameterInfoCollector(Template template, int completionOffset, QuteProject project) {
		this.completionOffset = completionOffset;
		collectGlobalVariables(project);
		collectDataModel(template);
		template.accept(this);
	}

	private void collectGlobalVariables(QuteProject project) {
		List<ValueResolver> resolvers = project != null ? project.getGlobalVariables().getNow(null) : null;
		if (resolvers != null) {
			for (ValueResolver resolver : resolvers) {
				String name = resolver.getNamed();
				if (name == null) {
					name = resolver.getName();
				}
				String type = getJavaType(resolver);
				register(name, type);
			}
		}
	}

	private void collectDataModel(Template template) {
		ExtendedDataModelTemplate dataModel = template.getDataModelTemplate().getNow(null);
		if (dataModel != null) {
			for (ExtendedDataModelParameter parameter : dataModel.getParameters()) {
				register(parameter.getKey(), parameter.getJavaType());
			}
		}
	}

	private static String getJavaType(ValueResolver resolver) {
		if (resolver instanceof JavaFieldInfo) {
			return ((JavaFieldInfo) resolver).getType();
		} else if (resolver instanceof JavaMethodInfo) {
			return ((JavaMethodInfo) resolver).getReturnType();
		}
		return null;
	}

	/**
	 * Collects {@code @} parameter declarations.
	 *
	 * <p>
	 * ex: {@code {@org.acme.Foo foo}}
	 * </p>
	 */
	@Override
	public boolean visit(ParameterDeclaration node) {
		// Only collect parameters declarations that enclose the completion offset
		if (!isVisibleAtOffset(node)) {
			return false;
		}
		// ParameterDeclaration is always at template root scope,
		// visible everywhere in the template
		if (node.hasAlias()) {
			String alias = node.getAlias();
			String type = node.getJavaType();
			if (!StringUtils.isEmpty(type)) {
				register(alias, type);
			}
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(LetSection node) {
		// Only collect parameters from sections that enclose the completion offset
		if (!isVisibleAtOffset(node)) {
			return false;
		}
		// {#let name=value} / {#set name=value}
		// Each parameter name=value contributes name -> type of value
		collectLetSetParameters(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(SetSection node) {
		// Only collect parameters from sections that enclose the completion offset
		if (!isVisibleAtOffset(node)) {
			return false;
		}
		// {#let name=value} / {#set name=value}
		// Each parameter name=value contributes name -> type of value
		collectLetSetParameters(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ForSection node) {
		// Only collect parameters from sections that enclose the completion offset
		if (!isVisibleAtOffset(node)) {
			return false;
		}
		// {#for item in items}
		// contributes "item" -> type of items element
		collectForParameters(node);
		return super.visit(node);
	}

	/**
	 * Collects parameters from sections (#let, #for, #set, #with, user tags).
	 *
	 * <p>
	 * Only collects parameters from sections that are visible at the completion
	 * offset, i.e. the section starts before the offset and the offset is inside
	 * the section's scope.
	 * </p>
	 */
	/*
	 * @Override public boolean visit(Section node) { // Only collect parameters
	 * from sections that enclose the completion offset if
	 * (!isVisibleAtOffset(node)) { return false; } switch (node.getSectionKind()) {
	 * case LET: case SET: // {#let name=value} / {#set name=value} // Each
	 * parameter name=value contributes name -> type of value
	 * collectLetSetParameters(node); break; case FOR: // {#for item in items} //
	 * contributes "item" -> type of items element collectForParameters(node);
	 * break; default: // User tags and other sections: collect named parameters
	 * collectNamedParameters(node); break; } return super.visit(node); }
	 */

	/**
	 * Returns true if the given section is visible at the completion offset.
	 *
	 * <p>
	 * A section is visible if:
	 * <ul>
	 * <li>it starts before the completion offset</li>
	 * <li>and the completion offset is inside the section (between start and
	 * end)</li>
	 * </ul>
	 * </p>
	 */
	private boolean isVisibleAtOffset(Node node) {
		return node.getEnd() <= completionOffset;
	}

	/**
	 * Collects parameters from a #let or #set section.
	 *
	 * <p>
	 * ex: {@code {#let name=value myPrice=order.price}} contributes "name" and
	 * "myPrice" with their respective types.
	 * </p>
	 *
	 * <p>
	 * Note: the Java type is derived from the expression value — this requires type
	 * resolution which may not be available at collection time. For now, we
	 * register the parameter name with a null type and let the caller resolve the
	 * type if needed.
	 * </p>
	 */
	private void collectLetSetParameters(Section node) {
		List<Parameter> parameters = node.getParameters();
		if (parameters == null) {
			return;
		}
		for (Parameter parameter : parameters) {
			String name = parameter.getName();
			if (!StringUtils.isEmpty(name)) {
				// Type resolution of the value expression is deferred to the caller.
				// Register with null type for now — caller can enrich later.
				register(name, null);
			}
		}
	}

	/**
	 * Collects the iteration variable from a #for section.
	 *
	 * <p>
	 * ex: {@code {#for item in items}} contributes "item" with the element type of
	 * "items".
	 * </p>
	 *
	 * <p>
	 * Note: element type resolution requires type inference and is deferred to the
	 * caller.
	 * </p>
	 */
	private void collectForParameters(Section node) {
		List<Parameter> parameters = node.getParameters();
		if (parameters == null || parameters.isEmpty()) {
			return;
		}
		// #for has the structure: item in items
		// parameter[0] = "item" (the iteration variable)
		Parameter iterationVar = parameters.get(0);
		String name = iterationVar.getName();
		if (!StringUtils.isEmpty(name)) {
			// Element type resolution is deferred to the caller.
			register(name, null);
		}
	}

	/**
	 * Collects named parameters from user tags and other sections.
	 *
	 * <p>
	 * ex: {@code {#myTag name=value foo=bar}} contributes "name" and "foo".
	 * </p>
	 */
	private void collectNamedParameters(Section node) {
		List<Parameter> parameters = node.getParameters();
		if (parameters == null) {
			return;
		}
		for (Parameter parameter : parameters) {
			String name = parameter.getName();
			if (!StringUtils.isEmpty(name)) {
				register(name, null);
			}
		}
	}

	/**
	 * Registers a parameter name and its Java type in both indexes.
	 *
	 * @param name the parameter name (ex: "foo").
	 * @param type the Java type (ex: "org.acme.Foo"), or null if not yet resolved.
	 */
	private void register(String name, String type) {
		if (nameToType == null) {
			nameToType = new HashMap<>();
		}
		nameToType.put(name, type);

		if (type != null) {
			if (typeToNames == null) {
				typeToNames = new HashMap<>();
			}
			Set<String> names = typeToNames.get(type);
			if (names == null) {
				names = new HashSet<>();
				typeToNames.put(type, names);
			}
			names.add(name);
		}
	}

	/**
	 * Returns the Java type for the given parameter name, or null if not found or
	 * not yet resolved.
	 *
	 * <p>
	 * Used to check if a parameter name matches a user tag parameter type and
	 * generate a completion without a value.
	 * </p>
	 *
	 * @param name the parameter name.
	 * @return the Java type or null.
	 */
	public String getType(String name) {
		return nameToType != null ? nameToType.get(name) : null;
	}

	/**
	 * Returns the set of parameter names that have the given Java type, or an empty
	 * list if none found.
	 *
	 * <p>
	 * Used to generate a list of candidate names for a given user tag parameter
	 * type.
	 * </p>
	 *
	 * @param type the Java type.
	 * @return the set of parameter names with that type.
	 */
	public Set<String> getNames(String type) {
		if (typeToNames == null) {
			return new HashSet<>();
		}
		Set<String> names = typeToNames.get(type);
		return names != null ? names : new HashSet<>();
	}

	/**
	 * Returns true if a parameter with the given name is visible at the completion
	 * offset and false otherwise.
	 *
	 * @param name the parameter name.
	 * @return true if the parameter is visible.
	 */
	public boolean hasParameter(String name) {
		return nameToType != null && nameToType.containsKey(name);
	}

	/**
	 * Returns all collected parameter names visible at the completion offset.
	 *
	 * @return the set of parameter names.
	 */
	public Set<String> getAllNames() {
		return nameToType != null ? nameToType.keySet() : new HashSet<>();
	}
}