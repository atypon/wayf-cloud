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


import com.atypon.wayf.data.publisher.PublisherSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class BeanMapperTest {

    @Test
    public void testResultSetProcessor() throws Exception {
        BeanMapper processor = new BeanMapper();

        Map<String, Object> row = new HashMap<>();
        row.put("id", "testId");
        row.put("device.id", "testDeviceId");

        PublisherSession publisherSession = processor.map(row, PublisherSession.class);

        Assert.assertEquals("testId", publisherSession.getId());
        Assert.assertEquals("testDeviceId", publisherSession.getDevice().getId());
    }


}
