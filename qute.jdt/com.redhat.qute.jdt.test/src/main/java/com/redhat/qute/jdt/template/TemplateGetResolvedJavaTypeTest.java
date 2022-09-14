/*******************************************************************************
0* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.template;

import static com.redhat.qute.jdt.QuteProjectTest.getJDTUtils;
import static com.redhat.qute.jdt.QuteProjectTest.loadMavenProject;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import com.redhat.qute.commons.InvalidMethodReason;
import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.JavaTypeKind;
import com.redhat.qute.commons.QuteResolvedJavaTypeParams;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.jdt.QuteProjectTest.QuteMavenProjectName;
import com.redhat.qute.jdt.QuteSupportForTemplate;

/**
 * Tests for
 * {@link QuteSupportForTemplate#getResolvedJavaType(QuteResolvedJavaTypeParams, com.redhat.qute.jdt.utils.IJDTUtils, org.eclipse.core.runtime.IProgressMonitor)}
 *
 * @author Angelo ZERR
 *
 */
public class TemplateGetResolvedJavaTypeTest {

	@Test
	public void object() throws Exception {
		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams("java.lang.Object",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertNotNull(result);
		Assert.assertEquals("java.lang.Object", result.getSignature());
		Assert.assertEquals(JavaTypeKind.Class, result.getJavaTypeKind());

		// None valid methods
		Assert.assertNotNull(result.getMethods());
		Assert.assertTrue(result.getMethods().isEmpty());

		// Invalid method codePointAt(int index)
		JavaMethodInfo waitMethod = findMethod(result, "wait");
		Assert.assertNull(waitMethod);
		InvalidMethodReason reason = result.getInvalidMethodReason("wait");
		Assert.assertEquals(InvalidMethodReason.FromObject, reason);
	}

	@Test
	public void string() throws Exception {
		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams("java.lang.String",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertNotNull(result);
		Assert.assertEquals("java.lang.String", result.getSignature());
		Assert.assertEquals(JavaTypeKind.Class, result.getJavaTypeKind());
		Assert.assertNotNull(result.getMethods());

		// Valid method isEmpty()
		JavaMethodInfo isEmptyMethod = findMethod(result, "isEmpty");
		Assert.assertNotNull(isEmptyMethod);

		// Invalid method void getChars(int srcBegin, int srcEnd, char dst[], int
		// dstBegin) {
		JavaMethodInfo getCharsMethod = findMethod(result, "getChars");
		Assert.assertNull(getCharsMethod);
		InvalidMethodReason reason = result.getInvalidMethodReason("getChars");
		Assert.assertEquals(InvalidMethodReason.VoidReturn, reason);

		// Extended types
		// public final class String implements java.io.Serializable,
		// Comparable<String>, CharSequence {
		List<String> extendedTypes = result.getExtendedTypes();
		Assert.assertNotNull(extendedTypes);
		assertExtendedTypes("java.lang.String", "java.io.Serializable", extendedTypes);
		assertExtendedTypes("java.lang.String", "java.lang.CharSequence", extendedTypes);
	}

	@Test
	public void iterable() throws Exception {
		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		// java.lang.Iterable
		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams("java.lang.Iterable",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("java.lang.Iterable<T>", result.getSignature());
		Assert.assertEquals(JavaTypeKind.Interface, result.getJavaTypeKind());

		List<String> extendedTypes = result.getExtendedTypes();
		Assert.assertNotNull(extendedTypes);
		Assert.assertEquals(1, extendedTypes.size());
		assertExtendedTypes("java.lang.Iterable", "java.lang.Object", extendedTypes);

		// Iterable
		params = new QuteResolvedJavaTypeParams("Iterable", QuteMavenProjectName.qute_quickstart);
		result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("java.lang.Iterable<T>", result.getSignature());
		Assert.assertEquals(JavaTypeKind.Interface, result.getJavaTypeKind());

		extendedTypes = result.getExtendedTypes();
		Assert.assertNotNull(extendedTypes);
		Assert.assertEquals(1, extendedTypes.size());
		assertExtendedTypes("java.lang.Iterable", "java.lang.Object", extendedTypes);
	}

	@Test
	public void list() throws Exception {
		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		// java.util.List
		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams("java.util.List",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("java.util.List<E>", result.getSignature());
		Assert.assertEquals(JavaTypeKind.Interface, result.getJavaTypeKind());

		// Invalid method void clear();
		JavaMethodInfo clearMethod = findMethod(result, "clear");
		Assert.assertNull(clearMethod);
		InvalidMethodReason reason = result.getInvalidMethodReason("clear");
		Assert.assertEquals(InvalidMethodReason.VoidReturn, reason);

		List<String> extendedTypes = result.getExtendedTypes();
		Assert.assertNotNull(extendedTypes);
		Assert.assertEquals(2, extendedTypes.size());
		assertExtendedTypes("java.util.List", "java.lang.Object", extendedTypes);
		assertExtendedTypes("java.util.List", "java.util.Collection<E>", extendedTypes);

		// List
		params = new QuteResolvedJavaTypeParams("List", QuteMavenProjectName.qute_quickstart);
		result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertNull(result);
	}

	@Test
	public void collection() throws Exception {
		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		// java.util.Collection
		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams("java.util.Collection",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("java.util.Collection<E>", result.getSignature());
		Assert.assertEquals(JavaTypeKind.Interface, result.getJavaTypeKind());

		// Invalid method void clear();
		JavaMethodInfo clearMethod = findMethod(result, "clear");
		Assert.assertNull(clearMethod);
		InvalidMethodReason reason = result.getInvalidMethodReason("clear");
		Assert.assertEquals(InvalidMethodReason.VoidReturn, reason);

		List<String> extendedTypes = result.getExtendedTypes();
		Assert.assertNotNull(extendedTypes);
		Assert.assertEquals(2, extendedTypes.size());
		assertExtendedTypes("java.util.Collection", "java.lang.Object", extendedTypes);
		// The existing type is java.lang.Iterable<T>, but we resolve the generic type T
		// with E (java.lang.Iterable<E>)
		// because collection defines E and not T.
		assertExtendedTypes("java.util.Collection", "java.lang.Iterable<E>", extendedTypes);

		// Collection
		params = new QuteResolvedJavaTypeParams("Collection", QuteMavenProjectName.qute_quickstart);
		result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertNull(result);
	}

	@Test
	public void map() throws Exception {
		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		// java.util.Map
		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams("java.util.Map",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("java.util.Map<K,V>", result.getSignature());
		Assert.assertEquals(JavaTypeKind.Interface, result.getJavaTypeKind());

		// Methods
		Assert.assertNotNull(result.getMethods());

		JavaMethodInfo keySet = findMethod(result, "keySet");
		Assert.assertEquals("keySet() : java.util.Set<K>", keySet.getSignature());
		JavaMethodInfo values = findMethod(result, "values");
		Assert.assertEquals("values() : java.util.Collection<V>", values.getSignature());
		JavaMethodInfo entrySet = findMethod(result, "entrySet");
		Assert.assertEquals("entrySet() : java.util.Set<java.util.Map$Entry<K,V>>", entrySet.getSignature());

		// Map
		params = new QuteResolvedJavaTypeParams("Map", QuteMavenProjectName.qute_quickstart);
		result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertNull(result);
	}

	@Test
	public void mapEntry() throws Exception {
		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		// java.util.Map$Entry
		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams("java.util.Map$Entry",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("java.util.Map$Entry<K,V>", result.getSignature());
		Assert.assertEquals(JavaTypeKind.Interface, result.getJavaTypeKind());

		// Methods
		Assert.assertNotNull(result.getMethods());

		JavaMethodInfo getKey = findMethod(result, "getKey");
		Assert.assertNotNull(getKey);
		Assert.assertEquals("getKey() : K", getKey.getSignature());
		JavaMethodInfo getValue = findMethod(result, "getValue");
		Assert.assertNotNull(getValue);
		Assert.assertEquals("getValue() : V", getValue.getSignature());

		// Map$Entry
		params = new QuteResolvedJavaTypeParams("Map$Entry", QuteMavenProjectName.qute_quickstart);
		result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertNull(result);
	}
	
	@Test
	public void someInterface() throws Exception {
		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams("org.acme.qute.SomeInterface",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("org.acme.qute.SomeInterface", result.getSignature());

		Assert.assertNotNull(result.getMethods());
		Assert.assertEquals(1, result.getMethods().size());
		Assert.assertEquals("getName() : java.lang.String", result.getMethods().get(0).getSignature());
		Assert.assertEquals(JavaTypeKind.Interface, result.getJavaTypeKind());
	}

	@Test
	public void item() throws Exception {
		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams("org.acme.qute.Item",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertNotNull(result);
		Assert.assertEquals("org.acme.qute.Item", result.getSignature());
		Assert.assertEquals(JavaTypeKind.Class, result.getJavaTypeKind());

		// Fields
		Assert.assertNotNull(result.getFields());
		Assert.assertEquals(2, result.getFields().size());
		Assert.assertEquals("name", result.getFields().get(0).getName());
		Assert.assertEquals("java.lang.String", result.getFields().get(0).getType());
		Assert.assertEquals("price", result.getFields().get(1).getName());
		Assert.assertEquals("java.math.BigDecimal", result.getFields().get(1).getType());

		// Methods
		Assert.assertNotNull(result.getMethods());
		Assert.assertEquals(2, result.getMethods().size());
		Assert.assertEquals("getDerivedItems() : org.acme.qute.Item[]", result.getMethods().get(0).getSignature());
		Assert.assertEquals("varArgsMethod(index : int, elements : java.lang.String...) : java.lang.String",
				result.getMethods().get(1).getSignature());

		// Invalid methods(static method)
		JavaMethodInfo discountedPriceMethod = findMethod(result, "staticMethod");
		Assert.assertNull(discountedPriceMethod);
		InvalidMethodReason reason = result.getInvalidMethodReason("staticMethod");
		Assert.assertEquals(InvalidMethodReason.Static, reason);
	}

	@Test
	public void statusesEnum() throws Exception {
		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams("org.acme.qute.StatusesEnum",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertNotNull(result);
		Assert.assertEquals("org.acme.qute.StatusesEnum", result.getSignature());
		Assert.assertEquals(JavaTypeKind.Enum, result.getJavaTypeKind());

		// Enum
		Assert.assertNotNull(result.getFields());
		Assert.assertEquals(2, result.getFields().size());
		Assert.assertEquals("ON", result.getFields().get(0).getName());
		Assert.assertEquals("org.acme.qute.StatusesEnum", result.getFields().get(0).getType());
		Assert.assertEquals("OFF", result.getFields().get(1).getName());
		Assert.assertEquals("org.acme.qute.StatusesEnum", result.getFields().get(1).getType());
	}

	@Test
	public void record() throws Exception {
		Assume.assumeTrue(Integer.parseInt(System.getProperty("java.specification.version")) >= 17);
		loadMavenProject(QuteMavenProjectName.qute_java17);

		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams("org.acme.qute.RecordItem",
				QuteMavenProjectName.qute_java17);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertEquals("org.acme.qute.RecordItem", result.getSignature());
		Assert.assertEquals(JavaTypeKind.Unknown, result.getJavaTypeKind());

		Assert.assertNotNull(result.getFields());
		Assert.assertEquals(2, result.getFields().size());
		Assert.assertEquals("name", result.getFields().get(0).getName());
		Assert.assertEquals("java.lang.String", result.getFields().get(0).getType());
		Assert.assertEquals("price", result.getFields().get(1).getName());
		Assert.assertEquals("java.math.BigDecimal", result.getFields().get(1).getType());
	}

	@Test
	public void templateData() throws CoreException, Exception {
		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams("org.acme.qute.ItemWithTemplateData",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertNotNull(result);
		Assert.assertEquals("org.acme.qute.ItemWithTemplateData", result.getSignature());
		Assert.assertEquals(JavaTypeKind.Class, result.getJavaTypeKind());

		// @TemplateData

		// @TemplateData(target = BigDecimal.class)
		// @TemplateData(ignoreSuperclasses = true)
		// public class ItemWithTemplateData {
		Assert.assertNotNull(result.getTemplateDataAnnotations());
		Assert.assertEquals(2, result.getTemplateDataAnnotations().size());
		// @TemplateData(target = BigDecimal.class)
		Assert.assertFalse(result.getTemplateDataAnnotations().get(0).isIgnoreSuperclasses());
		// @TemplateData(ignoreSuperclasses = true)
		Assert.assertTrue(result.getTemplateDataAnnotations().get(1).isIgnoreSuperclasses());

		// Fields
		Assert.assertNotNull(result.getFields());
		Assert.assertEquals(3, result.getFields().size());
		Assert.assertEquals("name", result.getFields().get(0).getName());
		Assert.assertEquals("java.lang.String", result.getFields().get(0).getType());
		Assert.assertEquals("price", result.getFields().get(1).getName());
		Assert.assertEquals("java.math.BigDecimal", result.getFields().get(1).getType());
		Assert.assertEquals("count", result.getFields().get(2).getName());
		Assert.assertEquals("java.lang.String", result.getFields().get(2).getType());

		// Methods
		Assert.assertNotNull(result.getMethods());
		Assert.assertEquals(1, result.getMethods().size());
		Assert.assertEquals("getDerivedItems() : org.acme.qute.Item[]", result.getMethods().get(0).getSignature());

		// Invalid methods(static method)
		JavaMethodInfo discountedPriceMethod = findMethod(result, "staticMethod");
		Assert.assertNull(discountedPriceMethod);
		InvalidMethodReason reason = result.getInvalidMethodReason("staticMethod");
		Assert.assertEquals(InvalidMethodReason.Static, reason);
	}

	@Test
	public void templateDataStatic() throws CoreException, Exception {
		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams("org.acme.qute.Statuses",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertNotNull(result);
		Assert.assertEquals("org.acme.qute.Statuses", result.getSignature());
		Assert.assertEquals(JavaTypeKind.Class, result.getJavaTypeKind());

		// @TemplateData
		// @TemplateData(namespace = "FOO")
		// @TemplateData(namespace = "BAR")
		// public class Statuses {
		Assert.assertNotNull(result.getTemplateDataAnnotations());
		Assert.assertEquals(3, result.getTemplateDataAnnotations().size());
		// @TemplateData
		Assert.assertFalse(result.getTemplateDataAnnotations().get(0).isIgnoreSuperclasses());
		// @TemplateData(namespace = "FOO")
		Assert.assertFalse(result.getTemplateDataAnnotations().get(1).isIgnoreSuperclasses());
		// @TemplateData(namespace = "BAR")
		Assert.assertFalse(result.getTemplateDataAnnotations().get(2).isIgnoreSuperclasses());

		// Fields
		Assert.assertNotNull(result.getFields());
		Assert.assertEquals(2, result.getFields().size());
		Assert.assertEquals("ON", result.getFields().get(0).getName());
		Assert.assertEquals("java.lang.String", result.getFields().get(0).getType());
		Assert.assertEquals("OFF", result.getFields().get(1).getName());
		Assert.assertEquals("java.lang.String", result.getFields().get(1).getType());

		// Invalid methods(static method)
		Assert.assertNull(findMethod(result, "staticMethod"));
		InvalidMethodReason reason = result.getInvalidMethodReason("staticMethod");
		Assert.assertEquals(InvalidMethodReason.Static, reason);
	}

	@Test
	public void registerForReflection() throws CoreException, Exception {
		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams(
				"org.acme.qute.ItemWithRegisterForReflection", QuteMavenProjectName.qute_quickstart);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertNotNull(result);
		Assert.assertEquals("org.acme.qute.ItemWithRegisterForReflection", result.getSignature());
		Assert.assertEquals(JavaTypeKind.Class, result.getJavaTypeKind());

		// @RegisterForReflection

		// @RegisterForReflection(fields = false)
		// public class ItemWithRegisterForReflection {
		Assert.assertNotNull(result.getRegisterForReflectionAnnotation());
		Assert.assertFalse(result.getRegisterForReflectionAnnotation().isFields());
		Assert.assertTrue(result.getRegisterForReflectionAnnotation().isMethods());

		// Fields
		Assert.assertNotNull(result.getFields());
		Assert.assertEquals(2, result.getFields().size());
		Assert.assertEquals("name", result.getFields().get(0).getName());
		Assert.assertEquals("java.lang.String", result.getFields().get(0).getType());
		Assert.assertEquals("price", result.getFields().get(1).getName());
		Assert.assertEquals("java.math.BigDecimal", result.getFields().get(1).getType());

		// Methods
		Assert.assertNotNull(result.getMethods());
		Assert.assertEquals(1, result.getMethods().size());
		Assert.assertEquals("getDerivedItems() : org.acme.qute.Item[]", result.getMethods().get(0).getSignature());

		// Invalid methods(static method)
		JavaMethodInfo discountedPriceMethod = findMethod(result, "staticMethod");
		Assert.assertNull(discountedPriceMethod);
		InvalidMethodReason reason = result.getInvalidMethodReason("staticMethod");
		Assert.assertEquals(InvalidMethodReason.Static, reason);
	}

	@Test
	public void ignoreSyntheticMethod() throws CoreException, Exception {
		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams("java.lang.CharSequence",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertNotNull(result);

		// lambda$chars$0 should be ignored
		Assert.assertEquals("java.lang.CharSequence", result.getSignature());
		JavaMethodInfo syntheticMethod = findMethod(result, "lambda$chars$0");
		Assert.assertNull(syntheticMethod);
	}

	@Test
	public void generic() throws CoreException, Exception {
		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		// class AImpl extends A<String,Integer>
		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams("org.acme.qute.generic.AImpl",
				QuteMavenProjectName.qute_quickstart);
		ResolvedJavaTypeInfo result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertNotNull(result);

		List<String> extendedTypes = result.getExtendedTypes();
		Assert.assertNotNull(extendedTypes);
		Assert.assertEquals(1, extendedTypes.size());
		assertExtendedTypes("org.acme.qute.generic.AImpl",
				"org.acme.qute.generic.A<java.lang.String,java.lang.Integer>", extendedTypes);

		// class A<A1, A2> extends B<A2, String> implements Iterable<A1>
		params = new QuteResolvedJavaTypeParams("org.acme.qute.generic.A", QuteMavenProjectName.qute_quickstart);
		result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertNotNull(result);

		Assert.assertNotNull(result.getMethods());
		Assert.assertEquals(1, result.getMethods().size());
		Assert.assertEquals("iterator() : java.util.Iterator<A1>", result.getMethods().get(0).getSignature());

		extendedTypes = result.getExtendedTypes();
		Assert.assertNotNull(extendedTypes);
		Assert.assertEquals(2, extendedTypes.size());
		assertExtendedTypes("org.acme.qute.generic.A", "org.acme.qute.generic.B<A2,java.lang.String>", extendedTypes);
		assertExtendedTypes("org.acme.qute.generic.A", "java.lang.Iterable<A1>", extendedTypes);

		// class B<B1,B2>
		params = new QuteResolvedJavaTypeParams("org.acme.qute.generic.B", QuteMavenProjectName.qute_quickstart);
		result = QuteSupportForTemplate.getInstance().getResolvedJavaType(params, getJDTUtils(),
				new NullProgressMonitor());
		Assert.assertNotNull(result);

		Assert.assertNotNull(result.getFields());
		Assert.assertEquals(1, result.getFields().size());
		Assert.assertEquals("field : B1", result.getFields().get(0).getSignature());

		Assert.assertNotNull(result.getMethods());
		Assert.assertEquals(1, result.getMethods().size());
		Assert.assertEquals("get(param : B2) : B1", result.getMethods().get(0).getSignature());

		extendedTypes = result.getExtendedTypes();
		Assert.assertNotNull(extendedTypes);
		Assert.assertTrue(extendedTypes.isEmpty());
	}

	private static void assertExtendedTypes(String type, String extendedType, List<String> extendedTypes) {
		Assert.assertTrue("The Java type '" + type + "' should extends '" + extendedType + "'.",
				extendedTypes.contains(extendedType));
	}

	private static JavaMethodInfo findMethod(ResolvedJavaTypeInfo javaType, String methodName) {
		Optional<JavaMethodInfo> result = javaType.getMethods().stream() //
				.filter(m -> methodName.equals(m.getName())) //
				.findFirst();
		return result.isPresent() ? result.get() : null;
	}

}
