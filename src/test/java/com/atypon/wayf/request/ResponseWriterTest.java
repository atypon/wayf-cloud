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

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class ResponseWriterTest {
    private class ResponseWriterMock extends ResponseWriter {
        public String _getLinkHeaderValue() {
            return super.getLinkHeaderValue();
        }
    }

    @After
    public void setup() {
        RequestContextAccessor.set(null);
    }

    @Test
    public void testHasMore() {
        RequestContextAccessor.set(new RequestContext().setRequestUrl("http://localhost:8080/list?param1=1&limit=30&offset=0").setOffset(0).setLimit(30).setHasAnotherDbPage(Boolean.TRUE));
        String link = new ResponseWriterMock()._getLinkHeaderValue();
        Assert.assertEquals("<http://localhost:8080/list?param1=1&limit=30&offset=30>; rel=\"next\"", link);
    }

    @Test
    public void testHasNoMore() {
        RequestContextAccessor.set(new RequestContext().setRequestUrl("/list").setOffset(0).setLimit(30).setHasAnotherDbPage(Boolean.FALSE));
        String link = new ResponseWriterMock()._getLinkHeaderValue();
        Assert.assertEquals("", link);
    }
}
