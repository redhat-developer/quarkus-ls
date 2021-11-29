package com.redhat.qute.resolvers;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.ValueResolver;
import com.redhat.qute.project.datamodel.ValueResolversRegistry;

public class ValueResolversRegistryTest {

	@Test 
	public void match() {
		ValueResolversRegistry registry = new ValueResolversRegistry();
		ResolvedJavaTypeInfo javaType = new ResolvedJavaTypeInfo();
		javaType.setSignature("org.acme.Item");
		javaType.setExtendedTypes(Arrays.asList("java.lang.Object"));
		
		List<ValueResolver> resolvers = registry.getResolversFor(javaType);
		System.err.println(resolvers.get(0).getSignature());
	}
}
