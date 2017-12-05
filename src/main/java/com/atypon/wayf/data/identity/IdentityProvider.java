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

import com.atypon.wayf.data.identity.external.IdPExternalId;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(
        use = Id.NAME,
        include = As.PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes({
        @Type(value = OpenAthensEntity.class, name = "OPEN_ATHENS"),
        @Type(value = SamlEntity.class, name = "SAML"),
        @Type(value = OauthEntity.class, name = "OAUTH")
})
public class IdentityProvider {
    private Long id;
    private String name;
    private Date createdDate;
    private Date modifiedDate;

    @JsonProperty("externalIds")
    private List<IdPExternalId> externalIds;

    public IdentityProvider() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("externalIds")
    public List<IdPExternalId> getExternalIds() {
        return externalIds;
    }

    @JsonProperty("externalIds")
    public void setExternalIds(List<IdPExternalId> externalIds) {
        this.externalIds = externalIds;
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

    public IdentityProviderType getType() {
        return null;
    }
}
