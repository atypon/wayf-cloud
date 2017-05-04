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

package com.atypon.wayf.guice;

import com.atypon.wayf.dao.*;
import com.atypon.wayf.dao.impl.*;
import com.atypon.wayf.dao.redis.RedisDao;
import com.atypon.wayf.dao.redis.impl.RedisDaoDefaultImpl;
import com.atypon.wayf.data.cache.CascadingCache;
import com.atypon.wayf.facade.*;
import com.atypon.wayf.facade.impl.*;
import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.apache.commons.dbcp.BasicDataSource;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Properties;

public class WayfGuiceModule extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(WayfGuiceModule.class);

    @Override
    protected void configure() {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            Properties properties = new Properties();
            properties.load(classLoader.getResourceAsStream("dao/publisher-session-dao-db.properties"));
            properties.load(classLoader.getResourceAsStream("dao/publisher-dao-db.properties"));
            properties.load(classLoader.getResourceAsStream("dao/device-dao-db.properties"));
            properties.load(classLoader.getResourceAsStream("dao/identity-provider-dao-db.properties"));

            Names.bindProperties(binder(), properties);

            bind(PublisherSessionFacade.class).to(PublisherSessionFacadeImpl.class);
            bind(PublisherSessionDao.class).to(PublisherSessionDaoDbImpl.class);

            bind(DeviceFacade.class).to(DeviceFacadeImpl.class);
            bind(DeviceDao.class).to(DeviceDaoDbImpl.class);

            bind(PublisherFacade.class).to(PublisherFacadeImpl.class);
            bind(PublisherDao.class).to(PublisherDaoDbImpl.class);

            bind(IdentityProviderFacade.class).to(IdentityProviderFacadeImpl.class);
            bind(IdentityProviderDao.class).to(IdentityProviderDaoDbImpl.class);

            bind(RedisDao.class)
                    .annotatedWith(Names.named("publisherIdRedisDao"))
                    .toProvider(() -> new RedisDaoDefaultImpl("PUBLISHER_ID"));

            bind(RedisDao.class)
                    .annotatedWith(Names.named("identityProviderRedisDao"))
                    .toProvider(() -> new RedisDaoDefaultImpl("IDENTITY_PROVIDER"));

            bind(Driver.class).toProvider(() -> GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "test")));

            bind(new TypeLiteral<CascadingCache<String, String>>(){})
                    .annotatedWith(Names.named("publisherIdCache"))
                    .toProvider(new Provider<CascadingCache<String, String>>() {
                        @Inject
                        @Named("publisherIdRedisDao")
                        private RedisDao l1;

                        @Inject
                        private PublisherSessionDaoDbImpl l2;

                        @Override
                        public CascadingCache<String, String> get() {
                            Guice.createInjector(new WayfGuiceModule()).injectMembers(this);
                            return new CascadingCache(l1, l2);
                        }
                    });

            bind(new TypeLiteral<CascadingCache<String, String>>(){})
                    .annotatedWith(Names.named("identityProviderCache"))
                    .toProvider(new Provider<CascadingCache<String, String>>() {
                        @Inject
                        @Named("identityProviderRedisDao")
                        private RedisDao l1;

                        @Inject
                        private IdentityProviderDaoDbImpl l2;

                        @Override
                        public CascadingCache<String, String> get() {
                            Guice.createInjector(new WayfGuiceModule()).injectMembers(this);
                            return new CascadingCache(l1, l2);
                        }
                    });

            BasicDataSource dataSource = new BasicDataSource();

            dataSource.setDriverClassName("com.mysql.jdbc.Driver");
            dataSource.setUsername("root");
            dataSource.setPassword("test");
            dataSource.setUrl("jdbc:mysql://localhost:3306/wayf");
            dataSource.setMaxActive(10);
            dataSource.setMaxIdle(5);
            dataSource.setInitialSize(5);
            dataSource.setValidationQuery("SELECT 1");

            bind(NamedParameterJdbcTemplate.class).toProvider(() -> new NamedParameterJdbcTemplate(dataSource));
        } catch (Exception e) {
            LOG.error("Error initializing Guice", e);
            throw new RuntimeException(e);
        }
    }
}