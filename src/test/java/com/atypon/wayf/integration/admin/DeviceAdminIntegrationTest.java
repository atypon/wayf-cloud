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

import com.atypon.wayf.data.publisher.Publisher;
import com.atypon.wayf.integration.HttpTestUtil;
import com.atypon.wayf.verticle.routing.BaseHttpTest;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class DeviceAdminIntegrationTest extends BaseHttpTest {
    private static final String HTTP_LOGGING_FILE = "device_admin_integration_test";

    private static final String BASE_PUBLISHER_FILE_PATH = "json_files/publisher_device_integration/";
    private static final String BASE_DEVICE_ADMIN_FILE_PATH = "json_files/device_admin_integration/";

    private static final String CREATE_PUBLISHER_A_REQUEST_JSON = getFileAsString(BASE_PUBLISHER_FILE_PATH + "publisher/create_publisher_a_request.json");
    private static final String CREATE_PUBLISHER_A_RESPONSE_JSON = getFileAsString(BASE_PUBLISHER_FILE_PATH + "publisher/create_publisher_a_response.json");

    private static final String CREATE_PUBLISHER_B_REQUEST_JSON = getFileAsString(BASE_PUBLISHER_FILE_PATH + "publisher/create_publisher_b_request.json");
    private static final String CREATE_PUBLISHER_B_RESPONSE_JSON = getFileAsString(BASE_PUBLISHER_FILE_PATH + "publisher/create_publisher_b_response.json");

    private static final String CREATE_SAML_IDP_REQUEST_JSON = getFileAsString(BASE_DEVICE_ADMIN_FILE_PATH + "identity_provider/create_saml_entity_request.json");
    private static final String CREATE_SAML_IDP_RESPONSE_JSON = getFileAsString(BASE_DEVICE_ADMIN_FILE_PATH + "identity_provider/create_saml_entity_response.json");

    private static final String CREATE_OPEN_ATHENS_IDP_REQUEST_JSON = getFileAsString(BASE_DEVICE_ADMIN_FILE_PATH + "identity_provider/create_open_athens_entity_request.json");
    private static final String CREATE_OPEN_ATHENS_IDP_RESPONSE_JSON = getFileAsString(BASE_DEVICE_ADMIN_FILE_PATH + "identity_provider/create_open_athens_entity_response.json");

    private static final String CREATE_OAUTH_IDP_REQUEST_JSON = getFileAsString(BASE_DEVICE_ADMIN_FILE_PATH + "identity_provider/create_oauth_entity_request.json");
    private static final String CREATE_OAUTH_IDP_RESPONSE_JSON = getFileAsString(BASE_DEVICE_ADMIN_FILE_PATH + "identity_provider/create_oauth_entity_response.json");

    private static final String RELATE_NEW_DEVICE_PUBLISHER_A_RESPONSE_JSON = getFileAsString(BASE_PUBLISHER_FILE_PATH + "/device/relate_new_device_publisher_a_response.json");


    private static final String SAML_IDP_RESPONSE_JSON = getFileAsString(BASE_DEVICE_ADMIN_FILE_PATH + "/identity_provider/saml_response.json");
    private static final String IDPS_RESPONSE_JSON = getFileAsString(BASE_DEVICE_ADMIN_FILE_PATH + "/identity_provider/idps_response.json");

    private static final String PUBLISHER_A_READ_RESPONSE_JSON = getFileAsString(BASE_DEVICE_ADMIN_FILE_PATH + "/publisher/publisher_a_response.json");
    private static final String PUBLISHERS_READ_RESPONSE_JSON = getFileAsString(BASE_DEVICE_ADMIN_FILE_PATH + "/publisher/publishers_response.json");

    private static final String LATEST_ACTIVITY_RESPONSE_JSON = getFileAsString(BASE_DEVICE_ADMIN_FILE_PATH + "/access/latest_activity_response.json");
    private static final String ACTIVITY_RESPONSE_JSON = getFileAsString(BASE_DEVICE_ADMIN_FILE_PATH + "/access/activity_response.json");

    private static final String DEVICE_HISTORY_RESPONSE_JSON = getFileAsString(BASE_DEVICE_ADMIN_FILE_PATH + "/history/device_history_response.json");
    private static final String HISTORY_AFTER_DELETE_RESPONSE_JSON = getFileAsString(BASE_DEVICE_ADMIN_FILE_PATH + "/history/history_after_delete_response.json");

    private static final String MYDEVICE_RESPONSE_JSON = getFileAsString(BASE_DEVICE_ADMIN_FILE_PATH + "/device/mydevice_response.json");
    private static final String CREATE_SAML_IDP_WITH_EXTERNAL_ID_REQUEST =  getFileAsString(BASE_DEVICE_ADMIN_FILE_PATH + "/identity_provider/external_id/saml_with_external_id_request.json");
    private static final String CREATE_SAML_IDP_WITH_EXTERNAL_ID_RESPONSE =  getFileAsString(BASE_DEVICE_ADMIN_FILE_PATH + "/identity_provider/external_id/saml_with_external_id_response.json");
    private static final String FILTER_EXTERNAL_ID_FOR_IDP_RESPONSE=  getFileAsString(BASE_DEVICE_ADMIN_FILE_PATH + "/identity_provider/external_id/filter_external_id_for_idp_response.json");
    private static final String FILTER_EXTERNAL_ID_FOR_IDP_WITH_PROVIDER_NAME_RESPONSE=  getFileAsString(BASE_DEVICE_ADMIN_FILE_PATH + "/identity_provider/external_id/filter_external_id_for_idp_wiht_provider_name_response.json");


    private Publisher publisherA;
    private Publisher publisherB;
    private String globalId;
    private Long samlIdpId;
    private List<Long> idpIds;

    public DeviceAdminIntegrationTest() {
        super(HTTP_LOGGING_FILE);

        seedData();
    }

    private void seedData() {
        // Create 2 Publishers
        String publisherAJson = CREATE_PUBLISHER_A_REQUEST_JSON;
        publisherAJson = HttpTestUtil.setField(publisherAJson, "code", "code-" + UUID.randomUUID().toString());

        String publisherBJson = CREATE_PUBLISHER_B_REQUEST_JSON;
        publisherBJson = HttpTestUtil.setField(publisherBJson, "code", "code-" + UUID.randomUUID().toString());

        publisherA = publisherTestUtil.testCreatePublisher(publisherAJson, CREATE_PUBLISHER_A_RESPONSE_JSON);
        publisherB = publisherTestUtil.testCreatePublisher(publisherBJson, CREATE_PUBLISHER_B_RESPONSE_JSON);

        String publisherALocalId = "local-id-publisher-a-" + UUID.randomUUID().toString();

        deviceTestUtil.registerLocalId(publisherALocalId, publisherA.getToken());

        // Create device
        globalId = deviceTestUtil.relateDeviceToPublisher(publisherALocalId, publisherA.getCode(), null, RELATE_NEW_DEVICE_PUBLISHER_A_RESPONSE_JSON);

        // Add the IDPs to the device multiple times and validate the IDP's ID is the same each time
        Long samlId = identityProviderTestUtil.testAddIdpToDeviceAndIdpResolution(5, publisherALocalId, publisherA.getToken(), CREATE_SAML_IDP_WITH_EXTERNAL_ID_REQUEST, CREATE_SAML_IDP_WITH_EXTERNAL_ID_RESPONSE);
        identityProviderTestUtil.addIdpExternalId(samlId, CREATE_SAML_IDP_WITH_EXTERNAL_ID_REQUEST, CREATE_SAML_IDP_WITH_EXTERNAL_ID_RESPONSE);
        identityProviderTestUtil.readIdpExternalIdByIdpId(samlId, FILTER_EXTERNAL_ID_FOR_IDP_RESPONSE);
        String provider = "redlink";
        identityProviderTestUtil.readIdpExternalIdByIdpIdAndExternalProvider(samlId, provider, FILTER_EXTERNAL_ID_FOR_IDP_WITH_PROVIDER_NAME_RESPONSE);

        Long openAthensId = identityProviderTestUtil.testAddIdpToDeviceAndIdpResolution(4, publisherALocalId, publisherA.getToken(), CREATE_OPEN_ATHENS_IDP_REQUEST_JSON, CREATE_OPEN_ATHENS_IDP_RESPONSE_JSON);
        Long oauthId = identityProviderTestUtil.testAddIdpToDeviceAndIdpResolution(3, publisherALocalId, publisherA.getToken(), CREATE_OAUTH_IDP_REQUEST_JSON, CREATE_OAUTH_IDP_RESPONSE_JSON);

        samlIdpId = samlId;
        idpIds = Lists.newArrayList(samlId, openAthensId, oauthId);

        // Relate the device to publisher B
        String publisherBLocalId = "local-id-publisher-b-" + UUID.randomUUID().toString();

        deviceTestUtil.registerLocalId(publisherBLocalId, publisherB.getToken());
        deviceTestUtil.relateDeviceToPublisher(publisherBLocalId, publisherB.getCode(), globalId, RELATE_NEW_DEVICE_PUBLISHER_A_RESPONSE_JSON);

        // Remove the SAML entity from the device from publisher A
        identityProviderTestUtil.removeIdpForDevice(publisherALocalId, publisherA.getToken(), samlId);

        // Add back the SAML identity to the device
        identityProviderTestUtil.testAddIdpToDeviceAndIdpResolution(5, publisherBLocalId, publisherB.getToken(), CREATE_SAML_IDP_REQUEST_JSON, CREATE_SAML_IDP_WITH_EXTERNAL_ID_RESPONSE);

    }
    @Test
    public void readMyDevice() {
        deviceTestUtil.readMyDevice(globalId, MYDEVICE_RESPONSE_JSON);
    }

    @Test
    public void readIdpById() {
        identityProviderTestUtil.readIdpById(samlIdpId, SAML_IDP_RESPONSE_JSON);
    }

    @Test
    public void readIdpsByIds() {
        identityProviderTestUtil.readIdpsByIds(idpIds, IDPS_RESPONSE_JSON);
    }

    @Test
    public void readPublisherById() {
        publisherTestUtil.testReadPublisher(publisherA.getId(), PUBLISHER_A_READ_RESPONSE_JSON);
    }

    @Test
    public void readPublishersByIds() {
        publisherTestUtil.testReadPublishers(Lists.newArrayList(publisherA.getId(), publisherB.getId()), PUBLISHERS_READ_RESPONSE_JSON);
    }

    @Test
    public void testActivity() {
        // Test the lastest item from history
        deviceAccessTestUtil.testLatestActivity(globalId, LATEST_ACTIVITY_RESPONSE_JSON);

        // Test the activity from seedData comes back
        deviceAccessTestUtil.testActivity(globalId, ACTIVITY_RESPONSE_JSON);

        // Verify the device suggestions
        deviceAccessTestUtil.testDeviceHistory(globalId, DEVICE_HISTORY_RESPONSE_JSON);

        // Delete the IDP from suggestions
        identityProviderTestUtil.removeIdpForDevice(globalId, samlIdpId);

        // Check to make sure the IDP is no longer presented
        deviceAccessTestUtil.testDeviceHistory(globalId, HISTORY_AFTER_DELETE_RESPONSE_JSON);
    }
}
