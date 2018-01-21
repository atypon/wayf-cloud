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

package com.atypon.wayf.integration;

import com.atypon.wayf.data.authentication.AuthorizationToken;
import com.atypon.wayf.verticle.routing.LoggingHttpRequestFactory;
import io.restassured.http.ContentType;
import io.restassured.http.Method;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.atypon.wayf.integration.HttpTestUtil.assertJsonEquals;
import static com.atypon.wayf.integration.HttpTestUtil.assertNotNullPaths;
import static com.atypon.wayf.integration.HttpTestUtil.readField;
import static com.atypon.wayf.request.ResponseWriter.DATE_FORMAT;
import static org.junit.Assert.assertTrue;

public class DeviceAccessTestUtil {
    private LoggingHttpRequestFactory requestFactory;

    public DeviceAccessTestUtil(LoggingHttpRequestFactory requestFactory) {
        this.requestFactory = requestFactory;
    }

    public void testDeviceHistoryError(int statusCode, String localId, AuthorizationToken publisherToken, String expectedHistoryJson) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", AuthorizationTokenTestUtil.generateApiTokenHeaderValue(publisherToken));

        String historyResponse =
                requestFactory
                        .request()
                        .contentType(ContentType.JSON)
                        .headers(headers)
                        .url("/1/device/" + localId + "/history")
                        .method(Method.GET)
                        .execute()
                        .statusCode(statusCode)
                        .extract().response().asString();

        String[] addDeviceHistoryGeneratedFields = {
                "$.stacktrace"
        };

        assertJsonEquals(expectedHistoryJson, historyResponse, addDeviceHistoryGeneratedFields);
    }


    public String testDeviceHistory(String localId, AuthorizationToken publisherToken, String expectedHistoryJson) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", AuthorizationTokenTestUtil.generateApiTokenHeaderValue(publisherToken));

        String historyResponse =
                requestFactory
                        .request()
                        .contentType(ContentType.JSON)
                        .headers(headers)
                        .url("/1/device/" + localId + "/history")
                        .method(Method.GET)
                        .execute()
                        .statusCode(200)
                        .extract().response().asString();

        compareDeviceHistory(expectedHistoryJson, historyResponse);

        return historyResponse;
    }

    public void compareDeviceHistory(String history1, String history2) {
        String[] addDeviceHistoryGeneratedFields = {
                "$[*].lastActiveDate",
                "$[*].idp.id",
                "$[*].idp.externalIds",
                "$[*].idp.createdDate"
        };

        assertJsonEquals(history1, history2, addDeviceHistoryGeneratedFields);
    }

    public void testLastActiveDateBetween(Date startDate, Date endDate, int idpCount, String deviceHistory) {
        for (int i = 0; i < idpCount; i++) {
            try {
                String lastActiveDateStr = readField(deviceHistory, "$[" + i + "].lastActiveDate");
                Date lastActiveDate = DATE_FORMAT.parse(lastActiveDateStr);

                assertTrue(startDate.compareTo(lastActiveDate) <= 0);
                assertTrue(endDate.compareTo(lastActiveDate) >= 0);
            } catch (Exception e) {
                throw new RuntimeException("Could not parse date in " + deviceHistory, e);
            }
        }
    }

    public void testLatestActivity(String globalId, String expectedResponse) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", "deviceId=" + globalId);

        String latestActivityResponse =
                requestFactory
                        .request()
                        .contentType(ContentType.JSON)
                        .headers(headers)
                        .url("/1/mydevice/activity?limit=1&type=ADD_IDP")
                        .method(Method.GET)
                        .execute()
                        .statusCode(200)
                        .extract().response().asString();

        String[] latestActivityGeneratedFields = {
                "$[*].id",
                "$[*].device.id",
                "$[*].identityProvider.id",
                "$[*].identityProvider.type",
                "$[*].identityProvider.name",
                "$[*].identityProvider.createdDate",
                "$[*].identityProvider.provider",
                "$[*].identityProvider.scope",
                "$[*].identityProvider.externalIds",
                "$[*].identityProvider.organizationId",
                "$[*].identityProvider.entityId",
                "$[*].identityProvider.federationId",
                "$[*].publisher.id",
                "$[*].publisher.status",
                "$[*].publisher.code",
                "$[*].publisher.name",
                "$[*].publisher.contact",
                "$[*].publisher.createdDate",
                "$[*].createdDate"
        };

        assertNotNullPaths(latestActivityResponse, latestActivityGeneratedFields);

        assertJsonEquals(expectedResponse, latestActivityResponse, latestActivityGeneratedFields);
    }

    public void testActivity(String globalId, String expectedResponse) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", "deviceId=" + globalId);

        String activityResponse =
                requestFactory
                        .request()
                        .contentType(ContentType.JSON)
                        .headers(headers)
                        .url("/1/mydevice/activity?limit=30")
                        .method(Method.GET)
                        .execute()
                        .statusCode(200)
                        .extract().response().asString();

        String[] activityGeneratedFields = {
                "$[*].id",
                "$[*].device.id",
                "$[*].identityProvider.id",
                "$[*].identityProvider.type",
                "$[*].identityProvider.name",
                "$[*].identityProvider.createdDate",
                "$[*].identityProvider.provider",
                "$[*].identityProvider.entityId",
                "$[*].identityProvider.scope",
                "$[*].identityProvider.externalIds",
                "$[*].identityProvider.organizationId",
                "$[*].identityProvider.federationId",
                "$[*].publisher.id",
                "$[*].publisher.status",
                "$[*].publisher.code",
                "$[*].publisher.name",
                "$[*].publisher.contact",
                "$[*].publisher.createdDate",
                "$[*].publisher.url",
                "$[*].createdDate"
        };

        assertNotNullPaths(activityResponse, activityGeneratedFields);

        assertJsonEquals(expectedResponse, activityResponse, activityGeneratedFields);
    }

    public String testDeviceHistory(String globalId, String expectedHistoryJson) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", "deviceId=" + globalId);

        String historyResponse =
                requestFactory
                        .request()
                        .contentType(ContentType.JSON)
                        .headers(headers)
                        .url("/1/mydevice/history")
                        .method(Method.GET)
                        .execute()
                        .statusCode(200)
                        .extract().response().asString();

        compareDeviceHistory(expectedHistoryJson, historyResponse);

        return historyResponse;
    }
}
