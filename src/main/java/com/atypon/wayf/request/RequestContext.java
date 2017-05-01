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

package com.atypon.wayf.request;

import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestContext {
    private static final Logger LOG = LoggerFactory.getLogger(RequestContext.class);

    private static final Integer DEFAULT_LIMIT = 30;
    private static final Integer DEFAULT_OFFSET = 0;

    private Integer limit = DEFAULT_LIMIT;
    private Integer offset = DEFAULT_OFFSET;

    private String deviceId;
    private String requestUrl;
    private String requestUri;
    private boolean forceSync;

    private Boolean hasAnotherDbPage = Boolean.FALSE;

    public RequestContext() {
    }

    public static RequestContext fromRoutingContext(RoutingContext routingContext) {
        RequestContext requestContext = new RequestContext();

        requestContext.setRequestUri(routingContext.request().uri());
        requestContext.setRequestUrl(routingContext.request().absoluteURI());

        String forceSyncQueryValue = RequestReader.getQueryValue(routingContext, "forceSync");
        requestContext.setForceSync(Boolean.parseBoolean(forceSyncQueryValue));

        String limit = RequestReader.getQueryValue(routingContext, "limit");
        if (limit != null && !limit.isEmpty()) {
            LOG.debug("Found limit query param [{}]", limit);

            requestContext.setLimit(Integer.parseInt(limit));
        }

        String offset = RequestReader.getQueryValue(routingContext, "offset");
        if (offset != null && !offset.isEmpty()) {
            LOG.debug("Found offset query param [{}]", offset);

            requestContext.setOffset(Integer.parseInt(offset));
        }

        String deviceId = RequestReader.getHeaderValue(routingContext, "deviceId");
        if (deviceId != null && !deviceId.isEmpty()) {
            LOG.debug("Found deviceId header [{}]", deviceId);

            requestContext.setDeviceId(deviceId);
        }

        return requestContext;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public RequestContext setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
        return this;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public RequestContext setRequestUri(String requestUri) {
        this.requestUri = requestUri;
        return this;
    }

    public boolean isForceSync() {
        return forceSync;
    }

    public RequestContext setForceSync(boolean forceSync) {
        this.forceSync = forceSync;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public RequestContext setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public Integer getOffset() {
        return offset;
    }

    public RequestContext setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public Boolean getHasAnotherDbPage() {
        return hasAnotherDbPage;
    }

    public RequestContext setHasAnotherDbPage(Boolean hasAnotherDbPage) {
        this.hasAnotherDbPage = hasAnotherDbPage;
        return this;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public RequestContext setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }
}
