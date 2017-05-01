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

import java.util.Set;

public class PublisherSessionQuery {
    public static final String PUBLISHER_FIELD = "publisher";
    public static final String IDENTITY_PROVIDER_FIELD = "identityProvider";

    private String id;
    private boolean matchOnId;

    private String localId;
    private boolean matchOnLocalId;

    private String deviceId;

    private Set<String> fields;

    public PublisherSessionQuery() {
    }

    public String getId() {
        return id;
    }

    public PublisherSessionQuery setId(String id) {
        this.id = id;
        return this;
    }

    public boolean isMatchOnId() {
        return matchOnId;
    }

    public PublisherSessionQuery setMatchOnId(boolean matchOnId) {
        this.matchOnId = matchOnId;
        return this;
    }

    public String getLocalId() {
        return localId;
    }

    public PublisherSessionQuery setLocalId(String localId) {
        this.localId = localId;
        return this;
    }

    public boolean isMatchOnLocalId() {
        return matchOnLocalId;
    }

    public PublisherSessionQuery setMatchOnLocalId(boolean matchOnLocalId) {
        this.matchOnLocalId = matchOnLocalId;
        return this;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public PublisherSessionQuery setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public Set<String> getFields() {
        return fields;
    }

    public PublisherSessionQuery setFields(Set<String> fields) {
        this.fields = fields;
        return this;
    }


    // Read only
    public boolean getInflatePublisher() {
        return fields != null && fields.contains(PUBLISHER_FIELD);
    }

    // Read only
    public boolean getInflateIdentityProvider() {
        return fields != null && fields.contains(IDENTITY_PROVIDER_FIELD);
    }
}
