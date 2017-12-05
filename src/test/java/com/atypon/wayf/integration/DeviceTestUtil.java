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
import io.restassured.http.Cookie;
import io.restassured.http.Method;
import io.restassured.response.ExtractableResponse;

import java.util.HashMap;
import java.util.Map;

import static com.atypon.wayf.integration.HttpTestUtil.assertJsonEquals;
import static org.junit.Assert.assertNotNull;

public class DeviceTestUtil {
    private LoggingHttpRequestFactory requestFactory;

    public DeviceTestUtil(LoggingHttpRequestFactory requestFactory) {
        this.requestFactory = requestFactory;
    }

    public String relateDeviceToPublisherError(int statusCode, String localId, String publisherCode, String globalId, String expectedResponseJson) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", AuthorizationTokenTestUtil.generateJwtTokenHeaderValue(publisherCode));
        headers.put("User-Agent", "Test-Agent");
        headers.put("Origin", "test-origin.com");

        Cookie cookie = null;
        if (globalId != null) {
            cookie = new Cookie.Builder("deviceId", globalId).setDomain("test-origin.com").build();
        }

        ExtractableResponse relateResponse = requestFactory
                .request()
                .headers(headers)
                .url("/1/device/" + localId)
                .method(Method.PATCH)
                .cookie(cookie)
                .execute()
                .statusCode(statusCode)
                .extract();

        String deviceIdHeader = relateResponse.cookie("deviceId");

        String deviceBody = relateResponse.response().body().asString();

        String[] relateResponseGeneratedFields = {
                "$.stacktrace"
        };

        assertJsonEquals(expectedResponseJson, deviceBody, relateResponseGeneratedFields);

        return deviceIdHeader;
    }

    public void registerLocalId(String localId, AuthorizationToken publisherToken) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", AuthorizationTokenTestUtil.generateApiTokenHeaderValue(publisherToken));
        headers.put("User-Agent", "Test-Agent");

        ExtractableResponse relateResponse = requestFactory
                .request()
                .headers(headers)
                .url("/1/device/" + localId)
                .method(Method.POST)
                .execute()
                .statusCode(200)
                .extract();
    }

    public String relateDeviceToPublisher(String localId, String publisherCode, String globalId, String expectedResponseJson) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", AuthorizationTokenTestUtil.generateJwtTokenHeaderValue(publisherCode));
        headers.put("User-Agent", "Test-Agent");
        headers.put("Origin", "test-origin.com");

        Cookie cookie = null;
        if (globalId != null) {
            cookie = new Cookie.Builder("deviceId", globalId).setDomain("test-origin.com").build();
        }

        ExtractableResponse relateResponse = requestFactory
                .request()
                .headers(headers)
                .url("/1/device/" + localId)
                .method(Method.PATCH)
                .cookie(cookie)
                .execute()
                .statusCode(200)
                .extract();

        String deviceIdHeader = relateResponse.cookie("deviceId");
        assertNotNull(deviceIdHeader);

        String deviceBody = relateResponse.response().body().asString();

        String[] relateResponseGeneratedFields = {
                "$.id",
                "$.createdDate"
        };

        assertJsonEquals(expectedResponseJson, deviceBody, relateResponseGeneratedFields);

        return deviceIdHeader;
    }

    public void deviceQueryBadPublisherToken(String localId, AuthorizationToken publisherToken, String expectedResponseJson) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", AuthorizationTokenTestUtil.generateApiTokenHeaderValue(publisherToken));
        headers.put("User-Agent", "Test-Agent");

        ExtractableResponse relateBadTokenResponse = requestFactory
                .request()
                .headers(headers)
                .url("/1/device/" + localId)
                .method(Method.PATCH)
                .execute()
                .statusCode(401)
                .extract();

        String[] relateResponseGeneratedFields = {
                "$.stacktrace"
        };

        assertJsonEquals(expectedResponseJson, relateBadTokenResponse.body().asString(), relateResponseGeneratedFields);
    }

    public void readMyDevice(String globalId, String expectedResponseJson) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", "deviceId=" + globalId);

        ExtractableResponse relateResponse = requestFactory
                .request()
                .headers(headers)
                .url("/1/mydevice/")
                .method(Method.GET)
                .execute()
                .statusCode(200)
                .extract();


        String deviceBody = relateResponse.response().body().asString();

        String[] relateResponseGeneratedFields = {
                "$.id",
                "$.globalId",
                "$.createdDate"
        };

        assertJsonEquals(expectedResponseJson, deviceBody, relateResponseGeneratedFields);
    }
    public void readDevices(String[] globalIds, String expectedResponseJson) {
        Map<String, String> headers = new HashMap<>();

        StringBuilder builder = new StringBuilder();
        for (String globalId : globalIds) {
            builder.append(globalId);
            builder.append(",");
        }

        builder.setLength(builder.length() - 1);


        ExtractableResponse relateResponse = requestFactory
                .request()
                .headers(headers)
                .url("/1/devices?globalIds=" + builder.toString())
                .method(Method.GET)
                .execute()
                .statusCode(200)
                .extract();

        String devicesBody = relateResponse.response().body().asString();

        String[] relateResponseGeneratedFields = {
                "$[*].id",
                "$[*].globalId",
                "$[*].createdDate"
        };

        assertJsonEquals(expectedResponseJson, devicesBody, relateResponseGeneratedFields);
    }
    public void readDevice(String globalId, String[] fields, String expectedResponseJson) {
        Map<String, String> headers = new HashMap<>();

        String fieldsParam = "";
        if(fields != null && fields.length > 0) {
            StringBuilder builder = new StringBuilder();
            for (String field : fields) {
                builder.append(field);
                builder.append(",");
            }

            builder.setLength(builder.length() - 1);

            fieldsParam = "?fields=" + builder.toString();
        }

        ExtractableResponse relateResponse = requestFactory
                .request()
                .headers(headers)
                .url("/1/device/" + globalId + fieldsParam)
                .method(Method.GET)
                .execute()
                .statusCode(200)
                .extract();


        String deviceBody = relateResponse.response().body().asString();

        String[] relateResponseGeneratedFields = {
                "$.id",
                "$.globalId",
                "$.history[*].lastActiveDate",
                "$.history[*].idp.id",
                "$.history[*].idp.createdDate",
                "$.activity[*].id",
                "$.activity[*].device.id",
                "$.activity[*].device.globalId",
                "$.activity[*].device.createdDate",
                "$.activity[*].publisher.id",
                "$.activity[*].publisher.code",
                "$.activity[*].publisher.contact.id",
                "$.activity[*].publisher.createdDate",
                "$.activity[*].identityProvider.id",
                "$.activity[*].identityProvider.createdDate",
                "$.activity[*].createdDate",
                "$.createdDate"
        };

        assertJsonEquals(expectedResponseJson, deviceBody, relateResponseGeneratedFields);
    }

    public void createDevice(String expectedResponseJson, String origin) {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Test-Agent");

        if(origin != null){
            headers.put("Origin", origin);
        }


        ExtractableResponse relateResponse = requestFactory
                .request()
                .headers(headers)
                .url("/1/device/")
                .method(Method.POST)
                .execute()
                .statusCode(200)
                .extract();

        String deviceIdHeader = relateResponse.cookie("deviceId");
        assertNotNull(deviceIdHeader);

        if(origin != null){
            String accessControlHeader = relateResponse.header("Access-Control-Allow-Origin");
            assertNotNull(accessControlHeader);
        }

        String deviceBody = relateResponse.response().body().asString();

        String[] relateResponseGeneratedFields = {
                "$.id",
                "$.createdDate"
        };
        assertJsonEquals(expectedResponseJson, deviceBody, relateResponseGeneratedFields);

    }

    public void CreateAndDeleteDevice() {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Test-Agent");

        ExtractableResponse relateResponse = requestFactory
                .request()
                .headers(headers)
                .url("/1/device/")
                .method(Method.POST)
                .execute()
                .statusCode(200)
                .extract();

        String deviceIdHeader = relateResponse.cookie("deviceId");
        assertNotNull(deviceIdHeader);

        headers.clear();
        headers.put("Cookie", "deviceId=" + deviceIdHeader);
        requestFactory
                .request()
                .headers(headers)
                .url("/1/device/")
                .method(Method.DELETE)
                .execute()
                .statusCode(200)
                .extract().response();
    }
}
