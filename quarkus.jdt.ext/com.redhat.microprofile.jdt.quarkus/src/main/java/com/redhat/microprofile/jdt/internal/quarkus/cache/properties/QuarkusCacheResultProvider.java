/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.quarkus.cache.properties;

import static com.redhat.microprofile.jdt.internal.quarkus.QuarkusConstants.CACHE_RESULT_ANNOTATION;
import static com.redhat.microprofile.jdt.internal.quarkus.QuarkusConstants.CACHE_RESULT_ANNOTATION_CACHE_NAME;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.getAnnotationMemberValue;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4mp.commons.metadata.ItemHint;
import org.eclipse.lsp4mp.commons.metadata.ValueHint;
import org.eclipse.lsp4mp.jdt.core.AbstractAnnotationTypeReferencePropertiesProvider;
import org.eclipse.lsp4mp.jdt.core.IPropertiesCollector;
import org.eclipse.lsp4mp.jdt.core.SearchContext;

import com.redhat.microprofile.jdt.internal.quarkus.providers.QuarkusSearchContext;

/**
 * Quarkus cache support properties provider:
 * 
 * <ul>
 * <li>the provider replace '{*}' for all properties from 'quarkus-cache'
 * extension with '${quarkus.cache.name}' (ex :
 * quarkus.cache.caffeine.${quarkus.cache.name}.initial-capacity)</li>
 * <li>the provider generates hints for each @CacheResult/cacheName declaration
 * with the key hint '${quarkus.cache.name}'.</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusCacheResultProvider extends AbstractAnnotationTypeReferencePropertiesProvider {

	private static final String QUARKUS_CACHE_EXTENSION = "quarkus-cache";

	private static final String[] ANNOTATION_NAMES = { CACHE_RESULT_ANNOTATION };

	private static final String QUARKUS_CACHE_NAME = "${quarkus.cache.name}";

	@Override
	protected String[] getAnnotationNames() {
		return ANNOTATION_NAMES;
	}

	@Override
	public void beginSearch(SearchContext context, IProgressMonitor monitor) {
		QuarkusSearchContext quarkusContext = QuarkusSearchContext.getQuarkusContext(context);
		// replace '{*}' for all properties from 'quarkus-cache' extension with
		// '${quarkus.cache.name}'
		quarkusContext.registerPropertyMapKeyReplacer(QUARKUS_CACHE_EXTENSION,
				(baseKey, keyIndex) -> QUARKUS_CACHE_NAME);
	}

	@Override
	protected void processAnnotation(IJavaElement javaElement, IAnnotation cacheResultAnnotation, String annotationName,
			SearchContext context, IProgressMonitor monitor) throws JavaModelException {
		String cacheName = getAnnotationMemberValue(cacheResultAnnotation, CACHE_RESULT_ANNOTATION_CACHE_NAME);
		if (!StringUtils.isBlank(cacheName)) {
			// generates hints for @CacheResult/cacheName declaration with the key hint
			// '${quarkus.cache.name}'
			IPropertiesCollector collector = context.getCollector();
			ItemHint itemHint = collector.getItemHint(QUARKUS_CACHE_NAME);
			itemHint.setSource(Boolean.TRUE);

			ValueHint value = new ValueHint();
			value.setValue(cacheName);
			if (!itemHint.getValues().contains(value)) {
				itemHint.getValues().add(value);
			}
		}
	}

}
