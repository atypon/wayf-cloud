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

package com.atypon.wayf.dao;

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
    public void test() throws Exception {
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

        Map<String, Object> arguments = QueryMapper.buildQueryArguments(
                "INSERT INTO wayf.publisher_session \\\n" +
                "  (id, localId, status, device_id, lastActiveDate, createdDate) \\\n" +
                "    VALUES :id, :localId, :status, :device.id, :lastActiveDate, :createdDate;\n", publisherSession);

        Assert.assertEquals(publisherSession.getId(), arguments.get("id"));
        Assert.assertEquals(publisherSession.getLocalId(), arguments.get("localId"));
        Assert.assertEquals(publisherSession.getStatus().toString(), arguments.get("status"));
        Assert.assertEquals(publisherSession.getLastActiveDate(), arguments.get("lastActiveDate"));
        Assert.assertEquals(publisherSession.getDevice().getId(), arguments.get("device.id"));
        Assert.assertEquals(publisherSession.getCreatedDate(), arguments.get("createdDate"));


    }
}
