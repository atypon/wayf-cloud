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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;


public class WayfRowMapper implements RowMapper {
    private static final Logger LOG = LoggerFactory.getLogger(WayfRowMapper.class);

    private Class<?> returnType;
    private BeanMapper beanMapper;

    public WayfRowMapper(Class<?> returnType) {
        this.returnType = returnType;
        beanMapper = new BeanMapper();
    }

    @Override
    public Object mapRow(ResultSet resultSet, int i) throws SQLException {
        ResultSetMetaData metadata = resultSet.getMetaData();

        int numColumns = metadata.getColumnCount();

        Map<String, Object> resultSetValues = new HashMap<>();

        for (int columnIndex = 1; columnIndex <= numColumns; columnIndex++) {
            int columnType = metadata.getColumnType(columnIndex);
            String columnLabel = metadata.getColumnLabel(columnIndex);

            switch (columnType) {
                case Types.DOUBLE:
                case Types.DECIMAL:
                    resultSetValues.put(columnLabel, resultSet.getDouble(columnLabel));
                    break;
                case Types.INTEGER:
                case Types.TINYINT:
                case Types.BIGINT:
                    resultSetValues.put(columnLabel, resultSet.getInt(columnLabel));
                    break;
                case Types.TIMESTAMP:
                case Types.DATE:
                    resultSetValues.put(columnLabel, resultSet.getTimestamp(columnLabel));
                    break;
                case Types.CHAR:
                case Types.VARCHAR:
                    resultSetValues.put(columnLabel, resultSet.getString(columnLabel));
                    break;
                default:
                    throw new RuntimeException("Could not determine return type for column [" + columnLabel + "] with type [" + columnType + "]");
            }
        }

        LOG.debug("Found resultset values [{}]", resultSetValues);

        return beanMapper.map(resultSetValues, returnType);
    }
}
