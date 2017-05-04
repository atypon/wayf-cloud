/*
 * Copyright 2017 Atypon Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atypon.wayf.dao.impl;

import com.atypon.wayf.data.publisher.Publisher;
import com.atypon.wayf.data.publisher.PublisherFilter;
import com.atypon.wayf.data.publisher.PublisherStatus;
import com.atypon.wayf.guice.WayfGuiceModule;
import com.atypon.wayf.reactivex.WayfReactivexConfig;
import com.atypon.wayf.request.RequestContext;
import com.atypon.wayf.request.RequestContextAccessor;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Inject;
import io.reactivex.Observable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class PublisherDaoDbImplTest {

    @Inject
    private PublisherDaoDbImpl dao;

    @Before
    public void setUp() {
        Guice.createInjector(new WayfGuiceModule()).injectMembers(this);

        WayfReactivexConfig.initializePlugins();
        RequestContextAccessor.set(new RequestContext().setLimit(5).setOffset(0));
    }

    @Test
    public void testCreate() {
        Publisher publisher = new Publisher();
        publisher.setName("Test Publisher");
        publisher.setStatus(PublisherStatus.ACTIVE);

        Publisher createdPublisher = dao.create(publisher).blockingGet();

        Assert.assertEquals(PublisherStatus.ACTIVE, createdPublisher.getStatus());
        Assert.assertNotNull(createdPublisher.getId());
        Assert.assertEquals("Test Publisher", createdPublisher.getName());
        Assert.assertNotNull(createdPublisher.getCreatedDate());
        Assert.assertNotNull(createdPublisher.getModifiedDate());
    }

    @Test
    public void testRead() {
        Publisher publisher = new Publisher();
        publisher.setName("Test Publisher");
        publisher.setStatus(PublisherStatus.ACTIVE);

        Publisher createdPublisher = dao.create(publisher).blockingGet();
        Publisher readPublisher = dao.read(createdPublisher.getId()).blockingGet();

        Assert.assertEquals(PublisherStatus.ACTIVE, readPublisher.getStatus());
        Assert.assertNotNull(readPublisher.getId());
        Assert.assertEquals("Test Publisher", readPublisher.getName());
        Assert.assertNotNull(readPublisher.getCreatedDate());
        Assert.assertNotNull(readPublisher.getModifiedDate());
    }

    @Test
    public void testFilter() {
        Map<String, Publisher> publishersById = new HashMap<>();

        for (int i = 0; i < 5; i++) {
            Publisher publisher = new Publisher();
            publisher.setName("Test Publisher " + i);
            publisher.setStatus(PublisherStatus.ACTIVE);

            Publisher createdPublisher = dao.create(publisher).blockingGet();
            publishersById.put(createdPublisher.getId(), createdPublisher);
        }

        Assert.assertEquals(5, publishersById.keySet().size());

        PublisherFilter filter = new PublisherFilter();
        filter.setIds(Lists.newArrayList(publishersById.keySet()));

        Observable<Publisher> publishers = dao.filter(filter);

        Iterable<Publisher> readPublishers = publishers.blockingIterable();

        for (Publisher readPublisher : readPublishers) {
            Publisher expected = publishersById.get(readPublisher.getId());
            Assert.assertEquals(expected.getId(), readPublisher.getId());
            Assert.assertEquals(expected.getName(), readPublisher.getName());
            Assert.assertEquals(expected.getStatus(), readPublisher.getStatus());
            Assert.assertNotNull(readPublisher.getCreatedDate());
            Assert.assertNotNull(readPublisher.getModifiedDate());
        }

    }
}
