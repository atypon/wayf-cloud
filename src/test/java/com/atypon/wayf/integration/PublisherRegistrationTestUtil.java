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

import com.atypon.wayf.verticle.routing.LoggingHttpRequestFactory;
import io.restassured.http.ContentType;
import io.restassured.http.Method;

import java.util.HashMap;
import java.util.Map;

import static com.atypon.wayf.integration.HttpTestUtil.*;
import static org.junit.Assert.assertEquals;

public class PublisherRegistrationTestUtil {

    private LoggingHttpRequestFactory requestFactory;

    public PublisherRegistrationTestUtil(LoggingHttpRequestFactory requestFactory) {
        this.requestFactory = requestFactory;
    }

    public Long testPublisherRegistration(String requestJson, String expectedResponseJson) {
        String createResponse =
                requestFactory
                        .request()
                        .contentType(ContentType.JSON)
                        .method(Method.POST)
                        .url("/1/publisherRegistration")
                        .body(requestJson)
                        .execute()
                        .statusCode(200)
                        .extract().response().asString();

        String[] createResponseGeneratedFields = {
                "$.id",
                "$.contact.id",
                "$.contact.createdDate",
                "$.applicationDate",
                "$.createdDate"
        };

        Long id = Long.valueOf(readField(createResponse, "$.id"));

        assertNotNullPaths(createResponse, createResponseGeneratedFields);
        assertJsonEquals(expectedResponseJson, createResponse, createResponseGeneratedFields);

        return id;
    }

    public String readRegistration(Long id, String expectedResponseJson) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", AuthorizationTokenTestUtil.generateDefaultApiTokenHeaderValue());

        String readResponse =
                requestFactory
                        .request()
                        .contentType(ContentType.JSON)
                        .method(Method.GET)
                        .headers(headers)
                        .url("/1/publisherRegistration/" + id + "?fields=CONTACT")
                        .execute()
                        .statusCode(200)
                        .extract().response().asString();

        String[] readResponseGeneratedFields = {
                "$.id",
                "$.contact.id",
                "$.contact.createdDate",
                "$.applicationDate",
                "$.createdDate"
        };


        assertNotNullPaths(readResponse, readResponseGeneratedFields);
        assertJsonEquals(expectedResponseJson, readResponse, readResponseGeneratedFields);

        return readResponse;
    }

    public void updateRegistrationStatus(boolean isApproved, Long id, String body, String expectedResponseJson) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", AuthorizationTokenTestUtil.generateDefaultApiTokenHeaderValue());

        String updateResponse =
                requestFactory
                        .request()
                        .contentType(ContentType.JSON)
                        .method(Method.PATCH)
                        .headers(headers)
                        .url("/1/publisherRegistration/" + id)
                        .body(body)
                        .execute()
                        .statusCode(200)
                        .extract().response().asString();

        String[] approvalResponseGeneratedFields = {
                "$.id",
                "$.contact.id",
                "$.applicationDate",
                "$.approvalDate",
                "$.modifiedDate",
                "$.url",
                "$.createdDate"
        };

        String[] rejectedResponseGeneratedFields = {
                "$.id",
                "$.contact.id",
                "$.applicationDate",
                "$.modifiedDate",
                "$.url",
                "$.createdDate"
        };

        assertNotNullPaths(updateResponse, isApproved? approvalResponseGeneratedFields : rejectedResponseGeneratedFields);
        assertJsonEquals(expectedResponseJson, updateResponse, isApproved? approvalResponseGeneratedFields : rejectedResponseGeneratedFields);
    }

    public void findPendingRegistrations(Long id, String expectedResponseJson) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", AuthorizationTokenTestUtil.generateDefaultApiTokenHeaderValue());

        String findResponse =
                requestFactory
                        .request()
                        .contentType(ContentType.JSON)
                        .method(Method.GET)
                        .headers(headers)
                        .url("/1/publisherRegistrations?statuses=PENDING&fields=CONTACT&limit=1")
                        .execute()
                        .statusCode(200)
                        .extract().response().asString();

        String[] findResponseGeneratedFields = {
                "$[*].id",
                "$[*].contact.id",
                "$[*].contact.createdDate",
                "$[*].applicationDate",
                "$[*].createdDate"
        };

        assertNotNullPaths(findResponse, findResponseGeneratedFields);
        assertJsonEquals(expectedResponseJson, findResponse, findResponseGeneratedFields);

        Long responseId = Long.valueOf(readField(findResponse, "$[0].id"));

        assertEquals(id, responseId);
    }
}
