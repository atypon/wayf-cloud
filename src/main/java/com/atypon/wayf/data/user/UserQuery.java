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

package com.atypon.wayf.data.user;

import java.util.Collection;

public class UserQuery {
    public static final String ADMIN_VIEW = "ADMIN";

    Long[] ids;
    String view;

    public Long[] getIds() {
        return ids;
    }

    public void setIds(Long[] ids) {
        this.ids = ids;
    }

    public UserQuery ids(Collection<Long> ids) {
        this.ids = ids.toArray(new Long[0]);
        return this;
    }

    /**
     * Database use only
     * @return
     */
    public boolean isNullIds() {
        return ids == null;
    }


    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }
}
