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

import com.atypon.wayf.verticle.WayfVerticle;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.Predicate;
import com.jayway.jsonpath.matchers.JsonPathMatchers;
import io.restassured.RestAssured;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import org.hamcrest.core.IsNull;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public abstract class BaseHttpTest {
    private static Vertx vertx;
    private static Integer port;

    @BeforeClass
    public static void setUpClass(TestContext context) throws IOException {
        vertx = Vertx.vertx();

        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();

        DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put("http.port", port));

        vertx.deployVerticle(WayfVerticle.class.getName(), options, context.asyncAssertSuccess());

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @AfterClass
    public static void afterClass() {
        RestAssured.reset();
        vertx.close();
    }

    protected Predicate ALWAYS_TRUE_PREDICATE = (arg) -> true;


    protected String getFileAsString(String path) {
        try {
            return CharStreams.toString(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(path), Charsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected String readField(String json, String field) {
        Object value = JsonPath.read(json, field, ALWAYS_TRUE_PREDICATE);

        if (value == null) {
            return null;
        }

        if (Map.class.isAssignableFrom(value.getClass())) {
            return JsonPath.parse(value).jsonString();
        } else {
            return String.valueOf(value);
        }
    }

    protected String setField(String json, String field, String value) {
        return JsonPath.parse(json).set(field, value, ALWAYS_TRUE_PREDICATE).jsonString();
    }

    protected void assertNotNullPaths(String json, String... fields) {
        for (String field : fields) {
            assertThat(json, JsonPathMatchers.hasJsonPath(field, IsNull.notNullValue()));
        }
    }

    protected void removeFields(DocumentContext document, String... fields) {
        for (String field : fields) {
            try {
                document.delete(field, ALWAYS_TRUE_PREDICATE);
            } catch (PathNotFoundException e) {
                // ignore
            }
        }
    }

    protected void assertJsonEquals(String expected, String actual, String... blacklistedFields) {
        DocumentContext expectedDocument = JsonPath.parse(expected);
        DocumentContext actualDocument = JsonPath.parse(actual);

        if (blacklistedFields != null) {
            removeFields(expectedDocument, blacklistedFields);
            removeFields(actualDocument, blacklistedFields);
        }

        try {
            ObjectMapper mapper = new ObjectMapper();

            JsonNode expectedNode = mapper.readTree(expectedDocument.jsonString());
            JsonNode actualNode = mapper.readTree(actualDocument.jsonString());

            assertEquals(expectedNode, actualNode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
