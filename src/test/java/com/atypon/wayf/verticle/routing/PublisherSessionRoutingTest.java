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

package com.atypon.wayf.verticle.routing;

import io.restassured.http.ContentType;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(VertxUnitRunner.class)
public class PublisherSessionRoutingTest extends BaseHttpTest {
    private static final String[] SERVER_GENERATED_FIELDS = {
            "id",
            "createdDate",
            "modifiedDate",
            "lastActiveDate"
    };

    private static final String[] DEVICE_FIELDS = {
            "device.id",
    };

    @Test
    public void testCreateSessionForNewDevice() throws Exception {
        String requestJsonString = getFileAsString("json_files/publisher_session/create_request.json");

        String createResponse =
                given()
                        .contentType(ContentType.JSON)
                        .body(requestJsonString)
                        .post("/1/publisherSession")
                .then()
                    .statusCode(200)
                    .extract().response().asString();

        // Validate that the server generated fields
        assertNotNullPaths(createResponse, SERVER_GENERATED_FIELDS);

        // validate that we have a device ID
        assertNotNullPaths(createResponse, DEVICE_FIELDS);

        // Compare the JSON to the payload on record
        assertJsonEquals(requestJsonString, createResponse, ArrayUtils.addAll(SERVER_GENERATED_FIELDS, "device"));
    }

    @Test
    public void testCreateSessionForExistingDevice() throws Exception {
        String deviceRequest = getFileAsString("json_files/publisher_session/device.json");

        String deviceResponse =
                given()
                        .contentType(ContentType.JSON)
                        .body(deviceRequest)
                        .post("/1/device")
                .then()
                        .statusCode(200)
                        .extract().response().asString();

        String deviceId = readField(deviceResponse, "id");

        assertNotNull(deviceId);

        String publisherSessionRequest = getFileAsString("json_files/publisher_session/create_request.json");

        String createResponse =
                given()
                        .header("deviceId", deviceId)
                        .contentType(ContentType.JSON)
                        .body(publisherSessionRequest)
                        .post("/1/publisherSession")
                .then()
                        .statusCode(200)
                        .extract().response().asString();


        // Validate that the server generated fields
        assertNotNullPaths(createResponse, SERVER_GENERATED_FIELDS);

        // Validate that we have a device ID
        assertNotNullPaths(createResponse, DEVICE_FIELDS);

        // Validate that the device ID on the session was the one passed in via the header
        assertEquals(deviceId, readField(createResponse, "device.id"));

        // Compare the JSON to the payload on record
        assertJsonEquals(publisherSessionRequest, createResponse, ArrayUtils.addAll(SERVER_GENERATED_FIELDS, "device"));
    }

    @Test
    public void testReadByLocalId() throws Exception {
        String requestJsonString = getFileAsString("json_files/publisher_session/create_request.json");

        // Generate a random localId
        String randomLocalId = "local-id-" + UUID.randomUUID().toString();

        // Update the local ID to our randomly generated one
        String requestJsonWithRandomLocalId = setField(requestJsonString, "localId", randomLocalId);

        String createResponse =
                given()
                        .contentType(ContentType.JSON)
                        .body(requestJsonWithRandomLocalId)
                        .post("/1/publisherSession")
                .then()
                        .statusCode(200)
                        .extract().response().asString();

        String id = readField(createResponse, "id");

        // Assert that we were assigned an ID for our local id
        assertNotNull(id);
        assertEquals(randomLocalId, readField(createResponse, "localId"));

        String readByLocalIdResponse =
                given()
                        .urlEncodingEnabled(false)
                        .get("/1/publisherSession/localId="+randomLocalId)
                .then()
                        .statusCode(200)
                        .extract().response().asString();

        // Ensure the IDs are correct
        assertEquals(id, readField(readByLocalIdResponse,"id"));
        assertEquals(randomLocalId, readField(readByLocalIdResponse,"localId"));


        // Ensure server generated fields come back
        assertNotNullPaths(readByLocalIdResponse, SERVER_GENERATED_FIELDS);

        // Compare the JSON to the payload on record
        assertJsonEquals(requestJsonWithRandomLocalId, readByLocalIdResponse, SERVER_GENERATED_FIELDS);
    }

    @Test
    public void testReadById() throws Exception {
        String requestJsonString = getFileAsString("json_files/publisher_session/create_request.json");

        String createResponse =
                given()
                        .contentType(ContentType.JSON)
                        .body(requestJsonString)
                        .post("/1/publisherSession")
                .then()
                        .statusCode(200)
                        .extract().response().asString();


        String id = readField(createResponse, "id");

        assertNotNull(id);

        String readByIdResponse =
                given()
                        .urlEncodingEnabled(false)
                        .get("/1/publisherSession/" + id)
                 .then()
                        .statusCode(200)
                        .extract().response().asString();

        // Ensure the server gave us the ID we requested
        assertEquals(id, readField(readByIdResponse, "id"));

        // Ensure server generated fields come back
        assertNotNullPaths(readByIdResponse, SERVER_GENERATED_FIELDS);

        // Compare the JSON to the payload on record
        assertJsonEquals(requestJsonString, readByIdResponse, SERVER_GENERATED_FIELDS);
    }

}
