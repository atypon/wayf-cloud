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

package com.atypon.wayf.data.publisher;

import com.atypon.wayf.data.authentication.Authenticatable;
import com.atypon.wayf.data.authentication.AuthenticatableType;
import com.atypon.wayf.data.authentication.AuthorizationToken;
import com.atypon.wayf.data.publisher.registration.PublisherRegistration;
import com.atypon.wayf.data.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

public class Publisher implements Authenticatable {
    private Long id;
    private PublisherStatus status;

    private PublisherRegistration registration;

    private AuthorizationToken token;
    private String salt;
    private String widgetLocation;

    private String url;

    private String code;
    private String name;

    private User contact;

    private Date createdDate;
    private Date modifiedDate;

    public Publisher() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PublisherStatus getStatus() {
        return status;
    }

    public void setStatus(PublisherStatus status) {
        this.status = status;
    }

    public PublisherRegistration getRegistration() {
        return registration;
    }

    public void setRegistration(PublisherRegistration registration) {
        this.registration = registration;
    }

    @JsonIgnore
    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    @Override
    @JsonIgnore
    public AuthenticatableType getAuthenticatableType() {
        return AuthenticatableType.PUBLISHER;
    }

    public AuthorizationToken getToken() {
        return token;
    }

    public void setToken(AuthorizationToken token) {
        this.token = token;
    }

    public String getWidgetLocation() {
        return widgetLocation;
    }

    public void setWidgetLocation(String widgetLocation) {
        this.widgetLocation = widgetLocation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public User getContact() {
        return contact;
    }

    public void setContact(User contact) {
        this.contact = contact;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
