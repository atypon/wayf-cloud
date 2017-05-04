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

import com.atypon.wayf.data.device.Device;
import com.atypon.wayf.data.device.DeviceStatus;
import com.atypon.wayf.guice.WayfGuiceModule;
import com.atypon.wayf.reactivex.WayfReactivexConfig;
import com.atypon.wayf.request.RequestContext;
import com.atypon.wayf.request.RequestContextAccessor;
import com.google.inject.Guice;
import com.google.inject.Inject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;



public class DeviceDaoDbImplTest {

    @Inject
    private DeviceDaoDbImpl dao;

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

        Device createdDevice = dao.create(device).blockingGet();

        Assert.assertEquals(DeviceStatus.ACTIVE, createdDevice.getStatus());
        Assert.assertNotNull(createdDevice.getId());
        Assert.assertNotNull(createdDevice.getCreatedDate());
    }

    @Test
    public void testRead() {
        Device device = new Device();
        device.setStatus(DeviceStatus.ACTIVE);

        Device createdDevice = dao.create(device).blockingGet();
        Device readDevice = dao.read(createdDevice.getId()).blockingGet();

        Assert.assertEquals(createdDevice.getId(), readDevice.getId());
        Assert.assertEquals(DeviceStatus.ACTIVE, readDevice.getStatus());
        Assert.assertNotNull(readDevice.getId());
        Assert.assertNotNull(readDevice.getCreatedDate());
    }
}
