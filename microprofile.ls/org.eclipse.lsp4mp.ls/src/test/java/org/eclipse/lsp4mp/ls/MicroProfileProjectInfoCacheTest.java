/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.ls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfoParams;
import org.eclipse.lsp4mp.commons.MicroProfilePropertiesChangeEvent;
import org.eclipse.lsp4mp.commons.MicroProfilePropertiesScope;
import org.eclipse.lsp4mp.ls.MicroProfileProjectInfoCache;
import org.eclipse.lsp4mp.ls.api.MicroProfileProjectInfoProvider;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for cache of {@link MicroProfileProjectInfo}
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileProjectInfoCacheTest {

	private static final String PROJECT1 = "project1";
	private static final String PROJECT1_APPLICATION_PROPERTIES = PROJECT1 + "/application.properties";

	static class MicroProfileProjectInfoProviderTracker implements MicroProfileProjectInfoProvider {

		private final AtomicInteger instanceCount = new AtomicInteger();

		@Override
		public CompletableFuture<MicroProfileProjectInfo> getProjectInfo(MicroProfileProjectInfoParams params) {
			return CompletableFuture.supplyAsync(() -> {
				instanceCount.incrementAndGet();
				MicroProfileProjectInfo info = new MicroProfileProjectInfo();
				info.setProjectURI(params.getUri().substring(0, params.getUri().indexOf('/')));
				synchronized (info) {
					try {
						info.wait(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				return info;
			});
		}

		public int getInstanceCount() {
			return instanceCount.get();
		}
	}

	static class MicroProfileProjectInfoParamsThrowException extends MicroProfileProjectInfoParams {
		private boolean throwError;

		public MicroProfileProjectInfoParamsThrowException(String uri) {
			super(uri);
		}

		public boolean isThrowError() {
			return throwError;
		}

		public void setThrowError(boolean throwError) {
			this.throwError = throwError;
		}
	}

	public static class MicroProfileProjectInfoProviderThrowException implements MicroProfileProjectInfoProvider {

		@Override
		public CompletableFuture<MicroProfileProjectInfo> getProjectInfo(MicroProfileProjectInfoParams params) {
			boolean throwError = ((MicroProfileProjectInfoParamsThrowException) params).isThrowError();
			if (throwError) {
				CompletableFuture<MicroProfileProjectInfo> completableFuture = new CompletableFuture<>();
				completableFuture.completeExceptionally(new UnsupportedOperationException());
				return completableFuture;
			}
			MicroProfileProjectInfo projectInfo = new MicroProfileProjectInfo();
			projectInfo.setProjectURI("project1");
			projectInfo.setProperties(new ArrayList<>());
			return CompletableFuture.completedFuture(projectInfo);
		}
	}

	@Test
	public void getProjectInfoFromCache() throws InterruptedException, ExecutionException {
		MicroProfileProjectInfoProviderTracker tracker = new MicroProfileProjectInfoProviderTracker();
		MicroProfileProjectInfoCache cache = new MicroProfileProjectInfoCache(tracker);

		// Execute 2 getProjectInfo in same time
		MicroProfileProjectInfoParams params = new MicroProfileProjectInfoParams(PROJECT1_APPLICATION_PROPERTIES);
		CompletableFuture<MicroProfileProjectInfo> request1 = cache.getProjectInfoFromCache(params);
		CompletableFuture<MicroProfileProjectInfo> request2 = cache.getProjectInfoFromCache(params);

		Assert.assertTrue("Same futures for getProjectInfo in same time with 2 completion requests",
				request1 == request2);
		Assert.assertTrue("Same instances of getProjectInfo in same time with 2 completion requests",
				request1.get() == request2.get());
		Assert.assertEquals("Number of call of getProjectInfo in same time with 2 completion requests", 1,
				tracker.getInstanceCount());

		// Execute getProjectInfo which should be get from cache
		CompletableFuture<MicroProfileProjectInfo> request3 = cache.getProjectInfoFromCache(params);

		Assert.assertTrue("Same futures for getProjectInfo in same time with 2 completion requests",
				request1 == request3);
		Assert.assertTrue("Same instances of getProjectInfo in same time with 2 completion requests",
				request1.get() == request3.get());
		Assert.assertEquals("Number of call of getProjectInfo in same time with 2 completion requests", 1,
				tracker.getInstanceCount());

		// Properties changed -> a new getProjectInfo instance should be get
		MicroProfilePropertiesChangeEvent event = new MicroProfilePropertiesChangeEvent();
		event.setProjectURIs(new HashSet<String>(Arrays.asList(PROJECT1)));
		event.setType(MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);
		cache.propertiesChanged(event);

		CompletableFuture<MicroProfileProjectInfo> request4 = cache.getProjectInfoFromCache(params);

		Assert.assertFalse("Different futures for getProjectInfo after propertiesChanged", request1 == request4);
		Assert.assertFalse("Different instance of getProjectInfo after propertiesChanged",
				request1.get() == request4.get());
		Assert.assertEquals("Number of call of getProjectInfo after propertiesChanged", 2, tracker.getInstanceCount());

	}

	@Test
	public void getProjectInfoCacheProviderException() throws InterruptedException, ExecutionException {
		MicroProfileProjectInfoProvider provider = new MicroProfileProjectInfoProviderThrowException();
		MicroProfileProjectInfoCache cache = new MicroProfileProjectInfoCache(provider);

		MicroProfileProjectInfoParamsThrowException params = new MicroProfileProjectInfoParamsThrowException(
				"application.properties");

		// With error
		params.setThrowError(true);
		CompletableFuture<MicroProfileProjectInfo> request = cache.getProjectInfo(params);
		// The call of projectInfo throws an error but it is catch and return an empty
		// project
		MicroProfileProjectInfo infoWithError = request.get();
		Assert.assertNotNull("Project info after an error should be not null", infoWithError);
		Assert.assertTrue("Project info after an error should have no project URI",
				infoWithError.getProjectURI().isEmpty());

		// With no error (after an error)
		params.setThrowError(false);
		request = cache.getProjectInfo(params);
		// The call of projectInfo throws an error but it is catch and return an empty
		// project
		MicroProfileProjectInfo infoWithNoError = request.get();
		Assert.assertNotNull("Project info after an error should be not null", infoWithNoError);
		Assert.assertTrue("Project info after an error should have a project URI",
				!infoWithNoError.getProjectURI().isEmpty());

	}
}
