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

package com.atypon.wayf.integration.admin;

import com.atypon.wayf.integration.publisher.PublisherIntegrationTest;
import com.atypon.wayf.verticle.routing.BaseHttpTest;
import org.junit.Test;
import java.util.UUID;

public class DeviceReadIntegrationTest extends BaseHttpTest {
    private static final String HTTP_LOGGING_FILE = "device_integration_test";

    private static final String BASE_DEVICE_FILE_PATH = "json_files/device/";

    private static final String SHALLOW_READ_RESPONSE = getFileAsString(BASE_DEVICE_FILE_PATH + "/shallow_read_response.json");
    private static final String SHALLOW_FILTER_RESPONSE = getFileAsString(BASE_DEVICE_FILE_PATH + "/shallow_filter_response.json");
    private static final String FULL_READ_RESPONSE = getFileAsString(BASE_DEVICE_FILE_PATH + "/full_read_response.json");
    private static final String CREATE_RESPONSE = getFileAsString(BASE_DEVICE_FILE_PATH + "/create_new_global_id.json");
    private static final String ERROR_404_BAD_GLOBAL_ID_RESPONSE_JSON = getFileAsString(BASE_DEVICE_FILE_PATH + "/404_bad_global_id.json");

    private String globalId;

    public DeviceReadIntegrationTest() {
        super(HTTP_LOGGING_FILE);

        seedData();
    }

    private void seedData() {
        PublisherIntegrationTest dataSeed = new PublisherIntegrationTest();
        try {
            dataSeed.createPublishers();
            dataSeed.multiplePublisherFullFlow();
            globalId = dataSeed.globalId;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void shallowDeviceTest() {
        deviceTestUtil.readDevice(globalId, null, SHALLOW_READ_RESPONSE);
    }

    @Test
    public void deviceQueryTest() {
        String[] globalIds = {globalId};
        deviceTestUtil.readDevices(globalIds, SHALLOW_FILTER_RESPONSE);

    }

    @Test
    public void fullDeviceTest() {
        String[] fields = {
                "activity%7BidentityProvider%2Cdevice%2Cpublisher%7D%2Chistory"
        };

        deviceTestUtil.readDevice(globalId,fields, FULL_READ_RESPONSE);
    }


    @Test
    public void createGlobalId() {
        deviceTestUtil.createDevice(CREATE_RESPONSE, null);
        deviceTestUtil.createDevice(CREATE_RESPONSE, "test-origin.com");
    }

    @Test
    public void deleteDevice(){
        deviceTestUtil.CreateAndDeleteDevice();
    }
}
