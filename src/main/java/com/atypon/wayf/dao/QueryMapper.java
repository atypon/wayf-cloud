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


import com.google.common.collect.Sets;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryMapper {
    private static final Logger LOG = LoggerFactory.getLogger(QueryMapper.class);

    private static final Set<String> FIELD_BLACKLIST = Sets.newHashSet(DbExecutor.LIMIT, DbExecutor.OFFSET);

    private static final String DELIMITER = ".";
    private static final String REGEX_DELIMITER = "\\.";

    private static final String FIELD_REGEX = ":([a-zA-Z0-9\\.]+)?";
    private static final Pattern PATTERN = Pattern.compile(FIELD_REGEX, Pattern.DOTALL | Pattern.MULTILINE);

    private static final Map<String, List<String>> parsedQueryCache = new HashMap<>(); // Don't let this grow too big

    private static BeanUtilsBean beanUtilsBean = new BeanUtilsBean(
    );

    public static Map<String, Object> buildQueryArguments(String query, Object bean) {
        List<String> parsedQuery = parseQuery(query);

        Map<String, Object> values = new HashMap<>();

        for (String queryField : parsedQuery) {
            values.put(queryField, getValue(queryField, bean));
        }

        return values;
    }

    private static List<String> parseQuery(String query) {
        List<String> parsedQuery = parsedQueryCache.get(query);

        if (parsedQuery == null) {
            Matcher result = PATTERN.matcher(query);

            parsedQuery = new LinkedList<>();

            while (result.find()) {
                for (int i = 1; i <= result.groupCount(); i++) {
                    String field = result.group(i);

                    if (!FIELD_BLACKLIST.contains(field)) {
                        parsedQuery.add(field);
                    }
                }
            }

            parsedQueryCache.put(query, parsedQuery);
        }

        return parsedQuery;
    }

    private static Object getValue(String field, Object bean) {
        LOG.debug("Getting value for field [{}] of bean [{}]", field, bean);

        try {
            Object fieldValue = field.contains(DELIMITER) ?
                    handleNestedValue(bean, field.split(REGEX_DELIMITER), 0) :
                    beanUtilsBean.getPropertyUtils().getProperty(bean, field);

            LOG.debug("Found value [{}] for field [{}] of bean [{}]", fieldValue, field, bean);

            return getDbValue(fieldValue);
        } catch (Exception e) {
            LOG.error("Could not parse argument [{}] from bean [{}]", field, bean);
            throw new RuntimeException(e);
        }
    }

    private static Object handleNestedValue(Object bean, String[] path, int index) throws Exception {
        LOG.debug("Handling nested value for bean[{}] path[{}] index[{}]", bean, path, index);

        String fieldName = path[index];

        if (index == path.length - 1) {
            LOG.debug("Returning nested field [{}] of bean [{}]", fieldName, bean);

            return beanUtilsBean.getPropertyUtils().getProperty(bean, fieldName);
        } else {
            Object childBean = beanUtilsBean.getPropertyUtils().getProperty(bean, fieldName);

            if (childBean == null) {
                return null;
            }

            LOG.debug("Recursing for childBean bean[{}] field[{}]", childBean, fieldName);

            return handleNestedValue(childBean, path, ++index);
        }
    }

    private static Object getDbValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value.getClass().isEnum()) {
            return value.toString();
        }

        if (value.getClass().equals(UUID.class)) {
            return value.toString();
        }

        return value;
    }
}
