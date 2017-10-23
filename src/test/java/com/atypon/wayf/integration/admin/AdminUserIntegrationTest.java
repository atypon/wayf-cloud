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
import com.atypon.wayf.verticle.routing.BaseHttpTest;
import org.junit.Ignore;
import org.junit.Test;

import java.util.UUID;

public class AdminUserIntegrationTest extends BaseHttpTest {
    private static final String HTTP_LOGGING_FILE = "device_admin_integration_test";

    private static final String BASE_ADMIN_USER_FILE_PATH = "json_files/admin_user_integration/";

    private static final String CREATE_ADMIN_REQUEST_JSON = getFileAsString(BASE_ADMIN_USER_FILE_PATH + "user/create_admin_user_request.json");
    private static final String CREATE_ADMIN_RESPONSE_JSON = getFileAsString(BASE_ADMIN_USER_FILE_PATH + "user/create_admin_user_response.json");
    private static final String CREATE_ADMIN_NO_TOKEN_RESPONSE_JSON = getFileAsString(BASE_ADMIN_USER_FILE_PATH + "user/create_admin_user_no_token_response.json");

    private static final String CREATE_PUBLISHER_REQUEST_JSON = getFileAsString(BASE_ADMIN_USER_FILE_PATH + "publisher/create_publisher_a_request.json");
    private static final String CREATE_PUBLISHER_RESPONSE_JSON = getFileAsString(BASE_ADMIN_USER_FILE_PATH + "publisher/create_publisher_a_response.json");
    private static final String CREATE_PUBLISHER_NO_TOKEN_RESPONSE_JSON = getFileAsString(BASE_ADMIN_USER_FILE_PATH + "publisher/create_publisher_no_token_response.json");
    private static final String CREATE_PUBLISHER_BAD_TOKEN_RESPONSE_JSON = getFileAsString(BASE_ADMIN_USER_FILE_PATH + "publisher/create_publisher_bad_token_response.json");

    private static final String LOGIN_REQUEST_JSON = getFileAsString(BASE_ADMIN_USER_FILE_PATH + "user/admin_user_login.json");
    private static final String DELETE_SELF_RESPONSE_JSON = getFileAsString(BASE_ADMIN_USER_FILE_PATH + "user/delete_self_response.json");
    private static final String DELETE_USER_NO_CREDENTIALS_RESPONSE_JSON = getFileAsString(BASE_ADMIN_USER_FILE_PATH + "user/delete_user_no_credentials_response.json");
    private static final String RESET_LOGIN_REQUEST_JSON = getFileAsString(BASE_ADMIN_USER_FILE_PATH + "credentials/reset_login_request.json");
    private static final String LOGIN_AFTER_RESET_REQUEST_JSON = getFileAsString(BASE_ADMIN_USER_FILE_PATH + "credentials/login_after_reset_request.json");

    private static final String LIST_ADMIN_USER_RESPONSE_JSON = getFileAsString(BASE_ADMIN_USER_FILE_PATH + "user/list_admin_user_response.json");
    private static final String LIST_ADMIN_USER_NO_CREDENTIALS_RESPONSE_JSON = getFileAsString(BASE_ADMIN_USER_FILE_PATH + "user/list_admin_user_no_credentials_response.json");

    private static final String FILTER_PUBLISHER_ADMIN_VIEW_RESPONSE_JSON = getFileAsString(BASE_ADMIN_USER_FILE_PATH + "publisher/filter_publishers_admin_view_response.json");
    private static final String FILTER_PUBLISHER_NO_ADMIN_VIEW_RESPONSE_JSON = getFileAsString(BASE_ADMIN_USER_FILE_PATH + "publisher/filter_publishers_no_admin_view_response.json");

    private static final String CURRENT_USER_RESPONSE_JSON = getFileAsString(BASE_ADMIN_USER_FILE_PATH + "user/current_user_response.json");
    private static final String CURRENT_USER_INVALID_TOKEN_RESPONSE_JSON = getFileAsString(BASE_ADMIN_USER_FILE_PATH + "user/current_user_invalid_token_response.json");


    public AdminUserIntegrationTest() {
        super(HTTP_LOGGING_FILE);
    }

    @Test
    public void testCreateAdmin() {
        String credentialsEmail = UUID.randomUUID().toString() + "@atypon.com";
        userTestUtil.testCreateUser(credentialsEmail, CREATE_ADMIN_REQUEST_JSON, CREATE_ADMIN_RESPONSE_JSON);
    }

    @Test
    public void testCreateAdminNoToken() {
        String credentialsEmail = UUID.randomUUID().toString() + "@atypon.com";
        userTestUtil.testCreateUserNoToken(credentialsEmail, CREATE_ADMIN_REQUEST_JSON, CREATE_ADMIN_NO_TOKEN_RESPONSE_JSON);
    }

    @Test
    public void testCreateAndLogin() {
        String credentialsEmail = UUID.randomUUID().toString() + "@atypon.com";
        userTestUtil.testCreateUser(credentialsEmail, CREATE_ADMIN_REQUEST_JSON, CREATE_ADMIN_RESPONSE_JSON);

        String adminToken = userTestUtil.testLogin(credentialsEmail, LOGIN_REQUEST_JSON);
    }

    @Test
    public void testCreatePublisherNoToken() {
        publisherTestUtil.testCreatePublisherNoToken(CREATE_PUBLISHER_REQUEST_JSON, CREATE_PUBLISHER_NO_TOKEN_RESPONSE_JSON);
    }

    @Test
    public void testPublisherWithToken() {
        String credentialsEmail = UUID.randomUUID().toString() + "@atypon.com";
        userTestUtil.testCreateUser(credentialsEmail, CREATE_ADMIN_REQUEST_JSON, CREATE_ADMIN_RESPONSE_JSON);

        String adminToken = userTestUtil.testLogin(credentialsEmail, LOGIN_REQUEST_JSON);

        publisherTestUtil.testCreatePublisher(adminToken, CREATE_PUBLISHER_REQUEST_JSON, CREATE_PUBLISHER_RESPONSE_JSON);
    }

    @Test
    public void testOnlyOneActiveToken() {
        String credentialsEmail = UUID.randomUUID().toString() + "@atypon.com";
        userTestUtil.testCreateUser(credentialsEmail, CREATE_ADMIN_REQUEST_JSON, CREATE_ADMIN_RESPONSE_JSON);

        String firstToken = userTestUtil.testLogin(credentialsEmail, LOGIN_REQUEST_JSON);
        String secondToken = userTestUtil.testLogin(credentialsEmail, LOGIN_REQUEST_JSON);

        publisherTestUtil.testCreatePublisher(secondToken, CREATE_PUBLISHER_REQUEST_JSON, CREATE_PUBLISHER_RESPONSE_JSON);
        publisherTestUtil.testCreatePublisherBadToken(firstToken, CREATE_PUBLISHER_REQUEST_JSON, CREATE_PUBLISHER_BAD_TOKEN_RESPONSE_JSON);

        userTestUtil.testMe(secondToken, CURRENT_USER_RESPONSE_JSON);
        userTestUtil.testMeInvalidToken(firstToken, CURRENT_USER_INVALID_TOKEN_RESPONSE_JSON);

    }

    @Test
    public void testDelete() {
        String credentialsEmail = UUID.randomUUID().toString() + "@atypon.com";
        Long userId = userTestUtil.testCreateUser(credentialsEmail, CREATE_ADMIN_REQUEST_JSON, CREATE_ADMIN_RESPONSE_JSON);

        String adminToken = userTestUtil.testLogin(credentialsEmail, LOGIN_REQUEST_JSON);

        userTestUtil.deleteUser(userId);
        userTestUtil.readDeletedUser(userId, "{}");

        publisherTestUtil.testCreatePublisherBadToken(adminToken, CREATE_PUBLISHER_REQUEST_JSON, CREATE_PUBLISHER_BAD_TOKEN_RESPONSE_JSON);
    }

    @Test
    public void testDeleteUserNoCredentials() {
        String credentialsEmail = UUID.randomUUID().toString() + "@atypon.com";
        Long userId = userTestUtil.testCreateUser(credentialsEmail, CREATE_ADMIN_REQUEST_JSON, CREATE_ADMIN_RESPONSE_JSON);

        userTestUtil.deleteUserNoCredentials(userId, DELETE_USER_NO_CREDENTIALS_RESPONSE_JSON);
    }

    @Test
    public void testDeleteSelf() {
        String credentialsEmail = UUID.randomUUID().toString() + "@atypon.com";
        Long userId = userTestUtil.testCreateUser(credentialsEmail, CREATE_ADMIN_REQUEST_JSON, CREATE_ADMIN_RESPONSE_JSON);

        String adminToken = userTestUtil.testLogin(credentialsEmail, LOGIN_REQUEST_JSON);

        userTestUtil.deleteSelf(adminToken, userId, DELETE_SELF_RESPONSE_JSON);
    }

    @Test
    public void testResetUserPassword() {
        String credentialsEmail = UUID.randomUUID().toString() + "@atypon.com";
        Long userId = userTestUtil.testCreateUser(credentialsEmail, CREATE_ADMIN_REQUEST_JSON, CREATE_ADMIN_RESPONSE_JSON);

        String adminToken1 = userTestUtil.testLogin(credentialsEmail, LOGIN_REQUEST_JSON);

        userTestUtil.resetLogin(userId, RESET_LOGIN_REQUEST_JSON);


        String adminToken2 = userTestUtil.testLogin(credentialsEmail, LOGIN_AFTER_RESET_REQUEST_JSON);

        publisherTestUtil.testCreatePublisherBadToken(adminToken1, CREATE_PUBLISHER_REQUEST_JSON, CREATE_PUBLISHER_BAD_TOKEN_RESPONSE_JSON);
        publisherTestUtil.testCreatePublisher(adminToken2, CREATE_PUBLISHER_REQUEST_JSON, CREATE_PUBLISHER_RESPONSE_JSON);
    }

    @Test
    public void testListAdminUser() {
        String credentialsEmail = UUID.randomUUID().toString() + "@atypon.com";
        Long userId = userTestUtil.testCreateUser(credentialsEmail, CREATE_ADMIN_REQUEST_JSON, CREATE_ADMIN_RESPONSE_JSON);

    }

    @Test
    public void testListAdminUserCredentials() {
        String credentialsEmail = UUID.randomUUID().toString() + "@atypon.com";
        Long userId = userTestUtil.testCreateUser(credentialsEmail, CREATE_ADMIN_REQUEST_JSON, CREATE_ADMIN_RESPONSE_JSON);
        userTestUtil.testListAdminUsers(userId, LIST_ADMIN_USER_RESPONSE_JSON);
    }

    @Test
    public void testListAdminUserNoCredentials() {
        String credentialsEmail = UUID.randomUUID().toString() + "@atypon.com";
        Long userId = userTestUtil.testCreateUser(credentialsEmail, CREATE_ADMIN_REQUEST_JSON, CREATE_ADMIN_RESPONSE_JSON);
        userTestUtil.testListAdminUsersNoCredentials(userId, LIST_ADMIN_USER_NO_CREDENTIALS_RESPONSE_JSON);
    }

    @Test
    public void testGetMe() {
        String credentialsEmail = UUID.randomUUID().toString() + "@atypon.com";
        userTestUtil.testCreateUser(credentialsEmail, CREATE_ADMIN_REQUEST_JSON, CREATE_ADMIN_RESPONSE_JSON);

        String adminToken = userTestUtil.testLogin(credentialsEmail, LOGIN_REQUEST_JSON);
        userTestUtil.testMe(adminToken, CURRENT_USER_RESPONSE_JSON);
    }

    @Test
    public void testGetMeInvalidToken() {
        String credentialsEmail = UUID.randomUUID().toString() + "@atypon.com";
        userTestUtil.testCreateUser(credentialsEmail, CREATE_ADMIN_REQUEST_JSON, CREATE_ADMIN_RESPONSE_JSON);

        String adminToken1 = userTestUtil.testLogin(credentialsEmail, LOGIN_REQUEST_JSON);
        String adminToken2 = userTestUtil.testLogin(credentialsEmail, LOGIN_REQUEST_JSON);

        userTestUtil.testMeInvalidToken(adminToken1, CURRENT_USER_INVALID_TOKEN_RESPONSE_JSON);
    }

    @Test
    public void testFilterPublishers() {
        String credentialsEmail = UUID.randomUUID().toString() + "@atypon.com";
        Long userId = userTestUtil.testCreateUser(credentialsEmail, CREATE_ADMIN_REQUEST_JSON, CREATE_ADMIN_RESPONSE_JSON);

        String adminToken = userTestUtil.testLogin(credentialsEmail, LOGIN_REQUEST_JSON);

        Publisher publisher = publisherTestUtil.testCreatePublisher(adminToken, CREATE_PUBLISHER_REQUEST_JSON, CREATE_PUBLISHER_RESPONSE_JSON);

        publisherTestUtil.testFilterPublisherAdmin(adminToken, publisher.getId(), FILTER_PUBLISHER_ADMIN_VIEW_RESPONSE_JSON);
        publisherTestUtil.testFilterPublisher(publisher.getId(), FILTER_PUBLISHER_NO_ADMIN_VIEW_RESPONSE_JSON);
        publisherTestUtil.testPublisherAdminNoCredentials(publisher.getId(), LIST_ADMIN_USER_NO_CREDENTIALS_RESPONSE_JSON);
    }

}
