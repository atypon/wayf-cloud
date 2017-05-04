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

package com.atypon.wayf.dao;

import com.atypon.wayf.request.RequestContextAccessor;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Map;

@Singleton
public class DbExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(DbExecutor.class);

    public static final String LIMIT = "limit";
    public static final String OFFSET = "offset";

    @Inject
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public DbExecutor() {
    }

    public <T> Maybe<T> executeSelectFirst(String query, Object arguments, Class<T> returnType) {
        return executeSelectFirst(query, QueryMapper.buildQueryArguments(query, arguments), returnType);
    }

    public <T> Maybe<T> executeSelectFirst(String query, Map<String, Object> arguments, Class<T> returnType) {
        return executeSelect(query, arguments, returnType).singleElement();
    }

    public <T> Observable<T> executeSelect(String query, Object arguments, Class<T> returnType) {
        return executeSelect(query, QueryMapper.buildQueryArguments(query, arguments), returnType);
    }

    public <T> Observable<T> executeSelect(String query, Map<String, Object> arguments, Class<T> returnType) {
        // Add in limit and offset arguments by default. The limit is increased by 1 so that we can see if there is
        // more data for the client to paginate
        arguments.put(LIMIT, RequestContextAccessor.get().getLimit() + 1);
        arguments.put(OFFSET, RequestContextAccessor.get().getOffset());

        LOG.debug("Running query [{}] with values [{}]", query, arguments);

        return Observable.fromIterable(namedParameterJdbcTemplate.query(query, arguments, new WayfRowMapper(returnType)));
    }

    public Single<Integer> executeUpdate(String query, Object arguments) {
        return executeUpdate(query, QueryMapper.buildQueryArguments(query, arguments));
    }

    public Single<Integer> executeUpdate(String query, Map<String, Object> arguments) {
        LOG.debug("Running update [{}] with values [{}]", query, arguments);

        return Single.just(query)
                .map((ignored) ->  namedParameterJdbcTemplate.update(query, arguments));

    }
}