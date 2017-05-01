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

package com.atypon.wayf.dao.neo4j;

import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.exceptions.NoSuchRecordException;
import org.neo4j.driver.v1.summary.ResultSummary;
import org.neo4j.driver.v1.types.*;
import org.neo4j.driver.v1.util.Function;
import org.neo4j.driver.v1.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MockSession implements Session {
    private String lastQuery;
    private Map<String, Object> lastArguments;
    private Integer numRowsToReturn = 5;

    public String getLastQuery() {
        return lastQuery;
    }

    public void setLastQuery(String lastQuery) {
        this.lastQuery = lastQuery;
    }

    public Map<String, Object> getLastArguments() {
        return lastArguments;
    }

    public void setLastArguments(Map<String, Object> lastArguments) {
        this.lastArguments = lastArguments;
    }

    public Integer getNumRowsToReturn() {
        return numRowsToReturn;
    }

    public void setNumRowsToReturn(Integer numRowsToReturn) {
        this.numRowsToReturn = numRowsToReturn;
    }

    @Override
    public Transaction beginTransaction() {
        return null;
    }

    @Override
    public Transaction beginTransaction(String s) {
        return null;
    }

    @Override
    public <T> T readTransaction(TransactionWork<T> transactionWork) {
        return null;
    }

    @Override
    public <T> T writeTransaction(TransactionWork<T> transactionWork) {
        return null;
    }

    @Override
    public String lastBookmark() {
        return null;
    }

    @Override
    public void reset() {

    }

    @Override
    public void close() {

    }

    @Override
    public StatementResult run(String s, Value value) {
        return null;
    }

    @Override
    public StatementResult run(String s, Map<String, Object> map) {
        lastQuery = s;
        lastArguments = map;

        return new StatementResult() {
            @Override
            public List<String> keys() {
                return null;
            }

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Record next() {
                return null;
            }

            @Override
            public Record single() throws NoSuchRecordException {
                return null;
            }

            @Override
            public Record peek() {
                return null;
            }

            @Override
            public List<Record> list() {
                List<Record> rows = new ArrayList<>(numRowsToReturn);
                for (int i = 0; i < numRowsToReturn; i++) {
                    rows.add(new Record() {
                        @Override
                        public List<String> keys() {
                            return null;
                        }

                        @Override
                        public List<Value> values() {
                            return null;
                        }

                        @Override
                        public boolean containsKey(String s) {
                            return false;
                        }

                        @Override
                        public int index(String s) {
                            return 0;
                        }

                        @Override
                        public Value get(String s) {
                            return null;
                        }

                        @Override
                        public Value get(int i) {
                            return null;
                        }

                        @Override
                        public int size() {
                            return 0;
                        }

                        @Override
                        public Map<String, Object> asMap() {
                            return new HashMap<>();
                        }

                        @Override
                        public <T> Map<String, T> asMap(Function<Value, T> function) {
                            return null;
                        }

                        @Override
                        public List<Pair<String, Value>> fields() {
                            return null;
                        }

                        @Override
                        public Value get(String s, Value value) {
                            return null;
                        }

                        @Override
                        public Object get(String s, Object o) {
                            return null;
                        }

                        @Override
                        public Number get(String s, Number number) {
                            return null;
                        }

                        @Override
                        public Entity get(String s, Entity entity) {
                            return null;
                        }

                        @Override
                        public Node get(String s, Node node) {
                            return null;
                        }

                        @Override
                        public Path get(String s, Path path) {
                            return null;
                        }

                        @Override
                        public Relationship get(String s, Relationship relationship) {
                            return null;
                        }

                        @Override
                        public List<Object> get(String s, List<Object> list) {
                            return null;
                        }

                        @Override
                        public <T> List<T> get(String s, List<T> list, Function<Value, T> function) {
                            return null;
                        }

                        @Override
                        public Map<String, Object> get(String s, Map<String, Object> map) {
                            return null;
                        }

                        @Override
                        public <T> Map<String, T> get(String s, Map<String, T> map, Function<Value, T> function) {
                            return null;
                        }

                        @Override
                        public int get(String s, int i) {
                            return 0;
                        }

                        @Override
                        public long get(String s, long l) {
                            return 0;
                        }

                        @Override
                        public boolean get(String s, boolean b) {
                            return false;
                        }

                        @Override
                        public String get(String s, String s1) {
                            return null;
                        }

                        @Override
                        public float get(String s, float v) {
                            return 0;
                        }

                        @Override
                        public double get(String s, double v) {
                            return 0;
                        }
                    });
                }
                return rows;
            }

            @Override
            public <T> List<T> list(Function<Record, T> function) {
                return null;
            }

            @Override
            public ResultSummary consume() {
                return null;
            }

            @Override
            public ResultSummary summary() {
                return null;
            }
        };
    }

    @Override
    public StatementResult run(String s, Record record) {
        return null;
    }

    @Override
    public StatementResult run(String s) {
        return null;
    }

    @Override
    public StatementResult run(Statement statement) {
        return null;
    }

    @Override
    public TypeSystem typeSystem() {
        return null;
    }

    @Override
    public boolean isOpen() {
        return false;
    }
}