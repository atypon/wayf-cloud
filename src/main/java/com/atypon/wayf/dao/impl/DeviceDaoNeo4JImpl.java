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
import com.atypon.wayf.dao.neo4j.Neo4JExecutor;
import com.atypon.wayf.data.Institution;
import com.atypon.wayf.data.device.Device;
import com.atypon.wayf.request.RequestContextAccessor;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
public class DeviceDaoNeo4JImpl implements DeviceDao {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceDaoNeo4JImpl.class);

    private String createCypher;
    private String readCypher;
    private String updateCypher;
    private String deleteCypher;

    @Inject
    public DeviceDaoNeo4JImpl(
            @Named("device.dao.neo4j.create") String createCypher,
            @Named("device.dao.neo4j.read") String readCypher,
            @Named("device.dao.neo4j.update")  String updateCypher,
            @Named("device.dao.neo4j.delete") String deleteCypher) {
        this.createCypher = createCypher;
        this.readCypher = readCypher;
        this.updateCypher = updateCypher;
        this.deleteCypher = deleteCypher;
    }

    @Override
    public Device create(Device device) {
        LOG.debug("Creating device [{}] in Neo4J", device);

        device.setId(UUID.randomUUID().toString());
        device.setCreatedDate(new Date());
        device.setModifiedDate(new Date());

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("id", device.getId());

        if (device.getStatus() != null) {
            arguments.put("status", device.getStatus().toString());
        }
        if (device.getCreatedDate() != null) {
            arguments.put("createdDate", device.getCreatedDate().getTime());
        }
        if (device.getModifiedDate() != null) {
            arguments.put("modifiedDate", device.getModifiedDate().getTime());
        }
        return Neo4JExecutor.executeQuery(createCypher, arguments, Device.class).get(0);
    }

    @Override
    public Device read(String id) {
        return null;
    }

    @Override
    public Device update(Device device) {
        return null;
    }

    @Override
    public void delete(String id) {

    }
}