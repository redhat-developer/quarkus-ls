/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.ls;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfileProjectInfoParams;
import com.redhat.microprofile.commons.MicroProfilePropertiesChangeEvent;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;
import com.redhat.microprofile.ls.api.MicroProfileProjectInfoProvider;

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

	@Test
	public void getProjectInfoCache() throws InterruptedException, ExecutionException {
		MicroProfileProjectInfoProviderTracker tracker = new MicroProfileProjectInfoProviderTracker();
		MicroProfileProjectInfoCache cache = new MicroProfileProjectInfoCache(tracker);

		// Execute 2 getProjectInfo in same time
		MicroProfileProjectInfoParams params = new MicroProfileProjectInfoParams(PROJECT1_APPLICATION_PROPERTIES);
		CompletableFuture<MicroProfileProjectInfo> request1 = cache.getProjectInfo(params);
		CompletableFuture<MicroProfileProjectInfo> request2 = cache.getProjectInfo(params);

		Assert.assertTrue("Same futures for getProjectInfo in same time with 2 completion requests",
				request1 == request2);
		Assert.assertTrue("Same instances of getProjectInfo in same time with 2 completion requests",
				request1.get() == request2.get());
		Assert.assertEquals("Number of call of getProjectInfo in same time with 2 completion requests", 1,
				tracker.getInstanceCount());

		// Execute getProjectInfo which should be get from cache
		CompletableFuture<MicroProfileProjectInfo> request3 = cache.getProjectInfo(params);

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

		CompletableFuture<MicroProfileProjectInfo> request4 = cache.getProjectInfo(params);

		Assert.assertFalse("Different futures for getProjectInfo after propertiesChanged", request1 == request4);
		Assert.assertFalse("Different instance of getProjectInfo after propertiesChanged",
				request1.get() == request4.get());
		Assert.assertEquals("Number of call of getProjectInfo after propertiesChanged", 2, tracker.getInstanceCount());

	}
}
