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

import com.atypon.wayf.dao.DbExecutor;
import com.atypon.wayf.data.device.Device;
import com.atypon.wayf.data.publisher.PublisherSession;
import com.atypon.wayf.guice.WayfGuiceModule;
import com.atypon.wayf.reactivex.WayfReactivexConfig;
import com.atypon.wayf.request.RequestContext;
import com.atypon.wayf.request.RequestContextAccessor;
import com.google.inject.Guice;
import com.google.inject.Inject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;

public class PublisherSessionDaoDbImplTest {
    @Inject
    private DbExecutor dbExecutor;

    @Test
    public void testInsert() {
        Guice.createInjector(new WayfGuiceModule()).injectMembers(this);

        WayfReactivexConfig.initializePlugins();
        RequestContextAccessor.set(new RequestContext().setLimit(5).setOffset(0));
        PublisherSession publisherSession = new PublisherSession();
        publisherSession.setId(UUID.randomUUID().toString());
        publisherSession.setStatus(null);
        publisherSession.setLocalId("1abc123456789");
        publisherSession.setLastActiveDate(new Date());
        publisherSession.setCreatedDate(new Date());
        publisherSession.setModifiedDate(new Date());

        Device device = new Device();
        device.setId(UUID.randomUUID().toString());
        publisherSession.setDevice(device);

        String insert = "INSERT INTO wayf.publisher_session " +
                "  (id, local_id, device_id, status, last_active_date, created_date) " +
                "    VALUES (:id, :localId, :device.id, :status, :lastActiveDate, :createdDate);";

        dbExecutor.executeUpdate(insert, publisherSession).subscribe();

        String readSession = "SELECT id AS 'id'," +
                "        local_id AS 'localId', " +
                "        device_id AS 'device.id', " +
                "        last_active_date AS 'lastActiveDate', " +
                "        created_date AS 'createdDate', " +
                "        modified_date AS 'modifiedDate' " +
                "    FROM wayf.publisher_session " +
                "        WHERE id = :id OR status = :status;";

        PublisherSession read = dbExecutor.executeSelectFirst(readSession, publisherSession, PublisherSession.class).blockingGet();

        Assert.assertEquals(publisherSession.getId(), read.getId());
        Assert.assertEquals(publisherSession.getLocalId(), read.getLocalId());
        Assert.assertEquals(publisherSession.getDevice().getId(), read.getDevice().getId());
        Assert.assertNotNull(read.getCreatedDate());
    }
}
