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
import com.atypon.wayf.data.authentication.AuthorizationTokenType;
import com.atypon.wayf.data.publisher.Publisher;
import com.atypon.wayf.verticle.routing.LoggingHttpRequestFactory;
import io.restassured.http.ContentType;
import io.restassured.http.Method;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atypon.wayf.integration.HttpTestUtil.*;

public class PublisherTestUtil {

    private LoggingHttpRequestFactory requestFactory;

    public PublisherTestUtil(LoggingHttpRequestFactory requestFactory) {
        this.requestFactory = requestFactory;
    }

    public void testReadPublisher(Long publisherId, String expectedResponseJson) {
        String readResponse =
                requestFactory
                        .request()
                        .contentType(ContentType.JSON)
                        .method(Method.GET)
                        .url("/1/publisher/" + publisherId)
                        .execute()
                        .statusCode(200)
                        .extract().response().asString();

        String[] readResponseGeneratedFields = {
                "$.id",
                "$.code",
                "$.contact.id",
                "$.createdDate"
        };

        assertNotNullPaths(readResponse, readResponseGeneratedFields);
        assertJsonEquals(expectedResponseJson, readResponse, readResponseGeneratedFields);
    }

    public void testReadPublishers(List<Long> publisherIds, String expectedResponseJson) {
        StringBuilder idsBuilder = new StringBuilder();

        for (Long publisherId : publisherIds) {
            idsBuilder.append(publisherId);
            idsBuilder.append(",");
        }

        idsBuilder.setLength(idsBuilder.length() - 1);

        String readResponse =
                requestFactory
                        .request()
                        .contentType(ContentType.JSON)
                        .method(Method.GET)
                        .url("/1/publishers?ids=" + idsBuilder.toString())
                        .execute()
                        .statusCode(200)
                        .extract().response().asString();

        String[] readResponseGeneratedFields = {
                "$[*].id",
                "$[*].code",
                "$[*].contact.id",
                "$[*].createdDate"
        };

        assertNotNullPaths(readResponse, readResponseGeneratedFields);
        assertJsonEquals(expectedResponseJson, readResponse, readResponseGeneratedFields);
    }

    public Publisher testCreatePublisher(String adminToken, String requestBody, String response) {
        AuthorizationToken adminAuthToken = new AuthorizationToken();
        adminAuthToken.setValue(adminToken);
        adminAuthToken.setType(AuthorizationTokenType.API_TOKEN);

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", AuthorizationTokenTestUtil.generateApiTokenHeaderValue(adminAuthToken));

        String createResponse =
                requestFactory
                        .request()
                        .contentType(ContentType.JSON)
                        .body(requestBody)
                        .headers(headers)
                        .method(Method.POST)
                        .url("/1/publisher")
                        .execute()
                        .statusCode(200)
                        .extract().response().asString();

        String[] createResponseGeneratedFields = {
                "$.id",
                "$.code",
                "$.token.value",
                "$.contact.id",
                "$.contact.createdDate",
                "$.widgetLocation",
                "$.createdDate"
        };

        assertNotNullPaths(createResponse, createResponseGeneratedFields);

        Long id = Long.valueOf(readField(createResponse, "$.id"));
        String authorizationTokenType = readField(createResponse, "$.token.type");
        String authorizationTokenValue = readField(createResponse, "$.token.value");
        String code = readField(createResponse, "$.code");

        assertJsonEquals(response, createResponse, createResponseGeneratedFields);

        Publisher publisher = new Publisher();
        publisher.setId(id);
        publisher.setCode(code);

        AuthorizationToken token = new AuthorizationToken();
        token.setType(AuthorizationTokenType.valueOf(authorizationTokenType));
        token.setValue(authorizationTokenValue);
        publisher.setToken(token);

        return publisher;
    }

    public Publisher testCreatePublisher(String requestBody, String response) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", AuthorizationTokenTestUtil.generateDefaultApiTokenHeaderValue());

        String createResponse =
                requestFactory
                        .request()
                        .contentType(ContentType.JSON)
                        .headers(headers)
                        .body(requestBody)
                        .method(Method.POST)
                        .url("/1/publisher")
                        .execute()
                        .statusCode(200)
                        .extract().response().asString();

        String[] createResponseGeneratedFields = {
                "$.id",
                "$.code",
                "$.token.value",
                "$.contact.id",
                "$.contact.createdDate",
                "$.widgetLocation",
                "$.createdDate"
        };

        assertNotNullPaths(createResponse, createResponseGeneratedFields);

        Long id = Long.valueOf(readField(createResponse, "$.id"));
        String authorizationTokenType = readField(createResponse, "$.token.type");
        String authorizationTokenValue = readField(createResponse, "$.token.value");
        String code = readField(createResponse, "$.code");

        assertJsonEquals(response, createResponse, createResponseGeneratedFields);

        Publisher publisher = new Publisher();
        publisher.setId(id);
        publisher.setCode(code);

        AuthorizationToken token = new AuthorizationToken();
        token.setType(AuthorizationTokenType.valueOf(authorizationTokenType));
        token.setValue(authorizationTokenValue);
        publisher.setToken(token);

        return publisher;
    }

    public void testCreatePublisherNoToken(String requestBody, String response) {
        String errorResponse =
                requestFactory
                        .request()
                        .contentType(ContentType.JSON)
                        .body(requestBody)
                        .method(Method.POST)
                        .url("/1/publisher")
                        .execute()
                        .statusCode(401)
                        .extract().response().asString();

        String[] errorResponseGeneratedFields = {
                "$.stacktrace"
        };
        assertJsonEquals(response, errorResponse, errorResponseGeneratedFields);
    }

    public void testCreatePublisherBadToken(String adminToken, String requestBody, String response) {
        AuthorizationToken adminAuthToken = new AuthorizationToken();
        adminAuthToken.setValue(adminToken);
        adminAuthToken.setType(AuthorizationTokenType.API_TOKEN);

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", AuthorizationTokenTestUtil.generateApiTokenHeaderValue(adminAuthToken));

        String errorResponse =
                requestFactory
                        .request()
                        .contentType(ContentType.JSON)
                        .body(requestBody)
                        .headers(headers)
                        .method(Method.POST)
                        .url("/1/publisher")
                        .execute()
                        .statusCode(401)
                        .extract().response().asString();

        String[] errorResponseGeneratedFields = {
                "$.stacktrace"
        };

        assertJsonEquals(response, errorResponse, errorResponseGeneratedFields);
    }

    public void testFilterPublisherAdmin(String adminToken, long publisherId, String response){
        AuthorizationToken adminAuthToken = new AuthorizationToken();
        adminAuthToken.setValue(adminToken);
        adminAuthToken.setType(AuthorizationTokenType.API_TOKEN);

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", AuthorizationTokenTestUtil.generateApiTokenHeaderValue(adminAuthToken));

        String readResponse =
                requestFactory
                        .request()
                        .headers(headers)
                        .contentType(ContentType.JSON)
                        .method(Method.GET)
                        .url("/1/publishers?view=ADMIN&ids=" + publisherId)
                        .execute()
                        .statusCode(200)
                        .extract().response().asString();


        String[] readResponseGeneratedFields = {
                "$[*].id",
                "$[*].code",
                "$[*].contact.id",
                "$[*].widgetLocation",
                "$[*].token.value",
                "$[*].createdDate"
        };

        assertNotNullPaths(readResponse, readResponseGeneratedFields);
        assertJsonEquals(response, readResponse, readResponseGeneratedFields);
    }

    public void testFilterPublisher(long publisherId, String response){

        String readResponse =
                requestFactory
                        .request()
                        .contentType(ContentType.JSON)
                        .method(Method.GET)
                        .url("/1/publishers?ids=" + publisherId)
                        .execute()
                        .statusCode(200)
                        .extract().response().asString();


        String[] readResponseGeneratedFields = {
                "$[*].id",
                "$[*].code",
                "$[*].contact.id",
                "$[*].createdDate"
        };

        assertNotNullPaths(readResponse, readResponseGeneratedFields);
        assertJsonEquals(response, readResponse, readResponseGeneratedFields);
    }

    public void testPublisherAdminNoCredentials(Long publisherId, String expectedResponse) {
        String response =
                requestFactory
                        .request()
                        .contentType(ContentType.JSON)
                        .method(Method.GET)
                        .url("/1/publishers?view=ADMIN&ids=" + publisherId)
                        .execute()
                        .statusCode(401)
                        .extract().response().asString();

        String[] responseGeneratedFields = {
                "$.stacktrace"
        };

        assertNotNullPaths(response, responseGeneratedFields);
        assertJsonEquals(expectedResponse, response, responseGeneratedFields);
    }
}
