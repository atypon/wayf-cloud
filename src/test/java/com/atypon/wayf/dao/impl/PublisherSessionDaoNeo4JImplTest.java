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

import com.atypon.wayf.dao.DeviceDao;
import com.atypon.wayf.dao.PublisherDao;
import com.atypon.wayf.data.device.Device;
import com.atypon.wayf.data.device.DeviceStatus;
import com.atypon.wayf.data.publisher.*;
import com.atypon.wayf.guice.WayfGuiceModule;
import com.atypon.wayf.reactivex.WayfReactivexConfig;
import com.atypon.wayf.request.RequestContext;
import com.atypon.wayf.request.RequestContextAccessor;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Inject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;
import java.util.List;

@Ignore
public class PublisherSessionDaoNeo4JImplTest {

    @Inject
    private DeviceDao deviceDao;

    @Inject
    private PublisherDao publisherDao;

    @Inject
    private PublisherSessionDaoNeo4JImpl dao;

    @Before
    public void setUp() {
        Guice.createInjector(new WayfGuiceModule()).injectMembers(this);

        WayfReactivexConfig.initializePlugins();
        RequestContextAccessor.set(new RequestContext().setLimit(5).setOffset(0));
    }

    @Test
    public void testCreate() {
        Device device = new Device();
        device.setStatus(DeviceStatus.ACTIVE);

        Publisher publisher = new Publisher();
        publisher.setName("Test Publisher");
        publisher.setStatus(PublisherStatus.ACTIVE);

        Device createdDevice = deviceDao.create(device).blockingGet();
        Assert.assertNotNull(createdDevice.getId());

        Publisher createdPublisher = publisherDao.create(publisher).blockingGet();
        Assert.assertNotNull(createdPublisher.getId());

        PublisherSession publisherSession = new PublisherSession();
        publisherSession.setId("123");
        publisherSession.setDevice(createdDevice);
        publisherSession.setPublisher(createdPublisher);
        publisherSession.setLocalId("123abc");
        publisherSession.setLastActiveDate(new Date());
        publisherSession.setStatus(PublisherSessionStatus.ACTIVE);

        PublisherSession createdPublisherSession = dao.create(publisherSession).blockingGet();
        Assert.assertEquals(PublisherSessionStatus.ACTIVE, createdPublisherSession.getStatus());
        Assert.assertNotNull(createdPublisherSession.getId());
        Assert.assertNotNull(createdPublisherSession.getCreatedDate());
        Assert.assertNotNull(createdPublisherSession.getModifiedDate());
    }

    @Test
    public void testFilter() {
        PublisherSessionQuery filter = new PublisherSessionQuery()
                .setDeviceId("3ece9fea-dbda-4775-84fb-9109dc37ba59")
                .setFields(Sets.newHashSet(PublisherSessionQuery.IDENTITY_PROVIDER_FIELD, PublisherSessionQuery.PUBLISHER_FIELD));

        List<PublisherSession> sessions =  dao.filter(filter).toList().blockingGet();
        Assert.assertEquals(4, sessions.size());
    }
}
