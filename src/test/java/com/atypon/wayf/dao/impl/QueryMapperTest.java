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

import com.atypon.wayf.dao.QueryMapper;
import com.atypon.wayf.data.device.Device;
import com.atypon.wayf.data.publisher.Publisher;
import com.atypon.wayf.data.publisher.PublisherSession;
import com.atypon.wayf.data.publisher.PublisherSessionStatus;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class QueryMapperTest {

    @Test
    public void testQueryMapper() throws Exception {
        PublisherSession publisherSession = new PublisherSession();
        publisherSession.setId(UUID.randomUUID().toString());
        publisherSession.setStatus(PublisherSessionStatus.ACTIVE);
        publisherSession.setLocalId("123abc456");
        publisherSession.setLastActiveDate(new Date());
        publisherSession.setCreatedDate(new Date());
        publisherSession.setModifiedDate(new Date());

        Device device = new Device();
        device.setId(UUID.randomUUID().toString());
        publisherSession.setDevice(device);

        Publisher publisher = new Publisher();
        publisher.setId(UUID.randomUUID().toString());
        publisherSession.setPublisher(publisher);

        Map<String, Object> arguments = QueryMapper.buildQueryArguments("SELECT id AS 'id', " +
                "        local_id AS 'localId', " +
                "        status, n" +
                "        device_id AS 'device.id', " +
                "        publisher_id AS 'publisher.id' " +
                "        authenticated_by_id AS 'authenticatedBy.id', " +
                "        last_active_date AS 'lastActiveDate',  " +
                "        created_date AS 'createdDate',  " +
                "        modified_date AS 'modifiedDate' " +
                "    FROM wayf.publisher_session " +
                "        WHERE id = :id AND device_id = :device.id;", publisherSession);

        Assert.assertEquals(publisherSession.getId(), arguments.get("id"));
        Assert.assertEquals(publisherSession.getDevice().getId(), arguments.get("device.id"));


    }
}
