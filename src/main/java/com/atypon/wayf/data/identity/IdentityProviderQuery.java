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

import java.util.Collection;

public class IdentityProviderQuery {
    private Long id;
    private Long[] ids;
    private String entityId;
    private IdentityProviderType type;
    private String organizationId;
    private OauthProvider provider;

    public IdentityProviderQuery() {
    }

    public Long getId() {
        return id;
    }

    public IdentityProviderQuery setId(Long id) {
        this.id = id;
        return this;
    }

    public Long[] getIds() {
        return ids;
    }

    public IdentityProviderQuery ids(Collection<Long> ids) {
        this.ids = ids.toArray(new Long[0]);
        return this;
    }

    public void setIds(Long... ids) {
        this.ids = ids;
    }

    public String getEntityId() {
        return entityId;
    }

    public IdentityProviderQuery setEntityId(String entityId) {
        this.entityId = entityId;
        return this;
    }

    /**
     * Database use only
     * @return
     */
    public boolean isNullIds() {
        return ids == null;
    }

    public IdentityProviderType getType() {
        return type;
    }

    public IdentityProviderQuery setType(IdentityProviderType type) {
        this.type = type;
        return this;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public IdentityProviderQuery setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    public OauthProvider getProvider() {
        return provider;
    }

    public IdentityProviderQuery setProvider(OauthProvider provider) {
        this.provider = provider;
        return this;
    }
}
