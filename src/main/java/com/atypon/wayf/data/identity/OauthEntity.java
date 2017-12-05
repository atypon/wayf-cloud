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

package com.atypon.wayf.data.identity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Date;

@JsonPropertyOrder({
        "provider",
        "type",
        "name",
        "externalIds"
})

public class OauthEntity extends IdentityProvider {
    private OauthProvider provider;

    private Date createdDate;
    private Date modifiedDate;

    public OauthEntity() {
    }

    @Override
    public IdentityProviderType getType() {
        return IdentityProviderType.OAUTH;
    }

    public void setType(IdentityProviderType type) {
    }

    public OauthProvider getProvider() {
        return provider;
    }

    public void setProvider(OauthProvider provider) {
        this.provider = provider;
    }

    @Override
    public Date getCreatedDate() {
        return createdDate;
    }

    @Override
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public Date getModifiedDate() {
        return modifiedDate;
    }

    @Override
    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}
