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

package com.atypon.wayf.facade.impl;

import com.atypon.wayf.data.*;
import com.atypon.wayf.data.authentication.*;
import com.atypon.wayf.data.publisher.Publisher;
import com.atypon.wayf.data.user.User;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class AuthenticatableRedisSerializer {
    private static final String EMAIL_PASSWORD_CREDENTIALS = "emailPassword";
    private static final String AUTHORIZATION_TOKEN_CREDENTIALS = "authorizationToken";

    private enum AuthenticatableFields {
        AUTHENTICATABLE_TYPE("authenticatable_type"),
        AUTHENTICATABLE_ID("authenticatable_id"),
        CREDENTIALS_TYPE("credentials_type"),
        CREDENTIALS("credentials");

        private String fieldName;

        AuthenticatableFields(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldName() {
            return fieldName;
        }
    }

    private enum AuthorizationTokenFields {
        TOKEN_TYPE("tokenType"),
        TOKEN_VALUE("tokenValue"),
        VALID_UNTIL("validUntil");

        private String fieldName;

        AuthorizationTokenFields(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldName() {
            return fieldName;
        }
    }

    private enum EmailPasswordCredentialsFields {
        EMAIL("email"),
        PASSWORD("password");

        private String fieldName;

        EmailPasswordCredentialsFields(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldName() {
            return fieldName;
        }
    }

    public static String serialize(AuthenticatedEntity authenticatedEntity) {
        JSONObject authenticatableJsonObject = new JSONObject();

        authenticatableJsonObject.put(AuthenticatableFields.AUTHENTICATABLE_TYPE.getFieldName(), authenticatedEntity.getAuthenticatable().getAuthenticatableType());
        authenticatableJsonObject.put(AuthenticatableFields.AUTHENTICATABLE_ID.getFieldName(), authenticatedEntity.getAuthenticatable().getId().toString());

        if (authenticatedEntity.getCredentials() == null) {
            throw new ServiceException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "An AuthorizationToken is expected");
        }

        JSONObject credentials = null;

        if (PasswordCredentials.class.isAssignableFrom(authenticatedEntity.getCredentials().getClass())) {
            credentials = serializeEmail((PasswordCredentials) authenticatedEntity.getCredentials());
            authenticatableJsonObject.put(AuthenticatableFields.CREDENTIALS_TYPE.getFieldName(), EMAIL_PASSWORD_CREDENTIALS);
        } else if (AuthorizationToken.class.isAssignableFrom(authenticatedEntity.getCredentials().getClass())){
            credentials = serializeToken((AuthorizationToken) authenticatedEntity.getCredentials());
            authenticatableJsonObject.put(AuthenticatableFields.CREDENTIALS_TYPE.getFieldName(), AUTHORIZATION_TOKEN_CREDENTIALS);

        } else {
            throw new ServiceException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Could not serialize authenticatable");
        }

        authenticatableJsonObject.put(AuthenticatableFields.CREDENTIALS.getFieldName(), credentials);

        return authenticatableJsonObject.toString();
    }

    private static JSONObject serializeEmail(PasswordCredentials credentials) {
        JSONObject emailJsonObject = new JSONObject();
        emailJsonObject.put(EmailPasswordCredentialsFields.EMAIL.getFieldName(), credentials.getEmailAddress());
        emailJsonObject.put(EmailPasswordCredentialsFields.PASSWORD.getFieldName(), credentials.getPassword());

        return emailJsonObject;

    }

    private static JSONObject serializeToken(AuthorizationToken token) {
        JSONObject authorizationTokenJsonObject = new JSONObject();
        authorizationTokenJsonObject.put(AuthorizationTokenFields.TOKEN_TYPE.getFieldName(), token.getType().toString());
        authorizationTokenJsonObject.put(AuthorizationTokenFields.TOKEN_VALUE.getFieldName(), token.getValue());

        if (token.getValidUntil() != null) {
            authorizationTokenJsonObject.put(AuthorizationTokenFields.VALID_UNTIL.getFieldName(), token.getValidUntil().getTime());
        }

        return authorizationTokenJsonObject;
    }



    public static AuthenticatedEntity deserialize(String json) {
        JSONObject authenticatableJsonObject = new JSONObject(json);

        String id = authenticatableJsonObject.getString(AuthenticatableFields.AUTHENTICATABLE_ID.getFieldName());

        AuthenticatableType type = authenticatableJsonObject.getEnum(AuthenticatableType.class, AuthenticatableFields.AUTHENTICATABLE_TYPE.getFieldName());

        Authenticatable authenticatable = null;
        if (AuthenticatableType.PUBLISHER == type) {
            Publisher publisher = new Publisher();
            publisher.setId(Long.valueOf(id));

            authenticatable = publisher;
        } else if (AuthenticatableType.USER == type) {
            User admin = new User();
            admin.setId(Long.valueOf(id));

            authenticatable = admin;
        } else {
            throw new ServiceException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Could not deserialize authenticatable");
        }

        AuthenticatedEntity authenticatedEntity = new AuthenticatedEntity();
        authenticatedEntity.setAuthenticatable(authenticatable);

        String credentialsType = authenticatableJsonObject.getString(AuthenticatableFields.CREDENTIALS_TYPE.getFieldName());
        JSONObject credentialsJsonObject = authenticatableJsonObject.getJSONObject(AuthenticatableFields.CREDENTIALS.getFieldName());

        AuthenticationCredentials credentials = null;

        if (EMAIL_PASSWORD_CREDENTIALS.equals(credentialsType)) {
            credentials = deserializeEmailPassword(credentialsJsonObject);
        } else if (AUTHORIZATION_TOKEN_CREDENTIALS.equals(credentialsType)) {
            credentials = deserializeAuthorizationToken(credentialsJsonObject);
            authenticatedEntity .setAuthenticatedUntil(((AuthorizationToken)credentials).getValidUntil());
        } else {
            throw new ServiceException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Could not deserialize authenticatble");
        }

        authenticatedEntity.setCredentials(credentials);

        return authenticatedEntity;
    }

    private static AuthenticationCredentials deserializeEmailPassword(JSONObject credentials) {
        PasswordCredentials emailPasswordCredentials = new PasswordCredentials();
        emailPasswordCredentials.setEmailAddress(EmailPasswordCredentialsFields.EMAIL.getFieldName());
        emailPasswordCredentials.setPassword(credentials.getString(EmailPasswordCredentialsFields.PASSWORD.getFieldName()));

        return new CachedPasswordCredentials(emailPasswordCredentials);
    }

    private static AuthenticationCredentials deserializeAuthorizationToken(JSONObject credentials) {
        AuthorizationToken authorizationToken = new AuthorizationToken();
        authorizationToken.setType(AuthorizationTokenType.valueOf(credentials.getString(AuthorizationTokenFields.TOKEN_TYPE.getFieldName())));
        authorizationToken.setValue(credentials.getString(AuthorizationTokenFields.TOKEN_VALUE.getFieldName()));

        try {
            authorizationToken.setValidUntil(new Date(credentials.getLong(AuthorizationTokenFields.VALID_UNTIL.getFieldName())));
        } catch (JSONException e) {
            // Thrown if validUntil does not exist which is valid for publisher tokens
        }

        return new CachedAuthorizationToken(authorizationToken);
    }

}
