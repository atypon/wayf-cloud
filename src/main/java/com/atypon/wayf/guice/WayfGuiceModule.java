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

import com.atypon.wayf.cache.Cache;
import com.atypon.wayf.cache.CacheManager;
import com.atypon.wayf.cache.LoadingCache;
import com.atypon.wayf.cache.impl.CacheManagerImpl;
import com.atypon.wayf.cache.impl.LoadingCacheGuavaImpl;
import com.atypon.wayf.cache.impl.LoadingCacheRedisImpl;
import com.atypon.wayf.dao.*;
import com.atypon.wayf.dao.impl.*;
import com.atypon.wayf.data.*;
import com.atypon.wayf.data.authentication.*;
import com.atypon.wayf.data.identity.IdentityProviderType;
import com.atypon.wayf.data.identity.OauthEntity;
import com.atypon.wayf.data.identity.OpenAthensEntity;
import com.atypon.wayf.data.identity.SamlEntity;
import com.atypon.wayf.database.AuthenticatableBeanFactory;
import com.atypon.wayf.database.AuthenticationCredentialsBeanFactory;
import com.atypon.wayf.database.BeanFactory;
import com.atypon.wayf.database.DbExecutor;
import com.atypon.wayf.facade.*;
import com.atypon.wayf.facade.impl.*;
import com.atypon.wayf.mail.DefaultMailMessageSender;
import com.atypon.wayf.mail.MailMessageSender;
import com.google.common.cache.CacheBuilder;
import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class WayfGuiceModule extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(WayfGuiceModule.class);

    private static final String WAYF_CONFIG_FILE = "wayf.properties";

    @Override
    protected void configure() {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            Properties properties = new Properties();

            String configDirectory = System.getProperty("wayf.conf.dir");
            if (configDirectory != null) {
                String configFile = configDirectory + "/" + WAYF_CONFIG_FILE;
                LOG.info("Loading wayf config file from location [{}]", configFile);

                FileReader reader = new FileReader(configFile);
                properties.load(reader);
            } else {
                LOG.info("Loading wayf config file from classpath");
                properties.load(classLoader.getResourceAsStream(WAYF_CONFIG_FILE));
            }

            properties.load(classLoader.getResourceAsStream("dao/device-access-dao-db.properties"));
            properties.load(classLoader.getResourceAsStream("dao/publisher-dao-db.properties"));
            properties.load(classLoader.getResourceAsStream("dao/device-dao-db.properties"));
            properties.load(classLoader.getResourceAsStream("dao/open-athens-entity-dao-db.properties"));
            properties.load(classLoader.getResourceAsStream("dao/oauth-entity-dao-db.properties"));
            properties.load(classLoader.getResourceAsStream("dao/device-identity-provider-blacklist-dao-db.properties"));
            properties.load(classLoader.getResourceAsStream("dao/authorization-token-dao-db.properties"));
            properties.load(classLoader.getResourceAsStream("dao/error-logger-dao-db.properties"));
            properties.load(classLoader.getResourceAsStream("dao/publisher-registration-dao-db.properties"));
            properties.load(classLoader.getResourceAsStream("dao/user-dao-db.properties"));
            properties.load(classLoader.getResourceAsStream("dao/password-credentials-dao-db.properties"));
            properties.load(classLoader.getResourceAsStream("dao/external-id-dao-db.properties"));

            Names.bindProperties(binder(), properties);

            bind(DeviceIdentityProviderBlacklistFacade.class).to(DeviceIdentityProviderBlacklistFacadeImpl.class);
            bind(IdentityProviderUsageFacade.class).to(IdentityProviderUsageFacadeImpl.class);

            bind(CryptFacade.class).to(CryptFacadeBcryptImpl.class);

            bind(CacheManager.class).to(CacheManagerImpl.class);

            bind(PasswordCredentialsFacade.class).to(PasswordCredentialsFacadeImpl.class);
            bind(PasswordCredentialsDao.class).to(PasswordCredentialsDaoDbImpl.class);

            bind(AuthorizationTokenFacade.class).to(AuthorizationTokenFacadeImpl.class);
            bind(AuthorizationTokenFactory.class).to(AuthorizationTokenFactoryImpl.class);
            bind(new TypeLiteral<AuthenticationCredentialsDao<PasswordCredentials>>(){}).to(PasswordCredentialsDaoDbImpl.class);
            bind(new TypeLiteral<AuthenticationCredentialsDao<AuthorizationToken>>(){}).to(AuthorizationTokenDaoDbImpl.class);

            bind(AuthenticationFacade.class).to(AuthenticationFacadeImpl.class);
            bind(DeviceAccessFacade.class).to(DeviceAccessFacadeImpl.class);
            bind(DeviceAccessDao.class).to(DeviceAccessDaoDbImpl.class);

            bind(DeviceFacade.class).to(DeviceFacadeImpl.class);
            bind(DeviceDao.class).to(DeviceDaoDbImpl.class);

            bind(UserFacade.class).to(UserFacadeImpl.class);
            bind(UserDao.class).to(UserDaoDbImpl.class);

            bind(PublisherFacade.class).to(PublisherFacadeImpl.class);
            bind(PublisherDao.class).to(PublisherDaoDbImpl.class);
            bind(PublisherRegistrationDao.class).to(PublisherRegistrationDaoDbImpl.class);
            bind(PublisherRegistrationFacade.class).to(PublisherRegistrationFacadeImpl.class);

            bind(IdentityProviderFacade.class).to(IdentityProviderFacadeImpl.class);
            bind(IdpExternalIdFacade.class).to(IdpExternalIdFacadeImpl.class);
            bind(IdPExternalIdDao.class).to(IdPExternalIdDaoImpl.class);

            bind(ErrorLoggerFacade.class).to(ErrorLoggerFacadeImpl.class);
            bind(ErrorLoggerDao.class).to(ErrorLoggerDaoDbImpl.class);

            bind(new TypeLiteral<InflationPolicyParser<String>>(){}).to(InflationPolicyParserQueryParamImpl.class);

            bind(DeviceIdentityProviderBlacklistDao.class).to(DeviceIdentityProviderBlacklistDaoDbImpl.class);

            bind(ClientJsFacade.class).to(ClientJsFacadeImpl.class);

            bind(MailMessageSender.class).to(DefaultMailMessageSender.class);

        } catch (Exception e) {
            LOG.error("Error initializing Guice", e);
            throw new RuntimeException(e);
        }
    }

    @Provides @Named("samlEntity")
    @Singleton
    public IdentityProviderDao provideSamlEntityDao(DbExecutor dbExecutor) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        Properties properties = new Properties();
        properties.load(classLoader.getResourceAsStream("dao/saml-entity-dao-db.properties"));

        return new IdentityProviderDaoDbImpl(properties.getProperty("saml-entity.dao.db.create"),
                properties.getProperty("saml-entity.dao.db.read"),
                properties.getProperty("saml-entity.dao.db.filter"),
                dbExecutor,
                SamlEntity.class);
    }

    @Provides @Named("openAthensEntity")
    @Singleton
    public IdentityProviderDao provideOpenAthensEntityDao(DbExecutor dbExecutor) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        Properties properties = new Properties();

        properties.load(classLoader.getResourceAsStream("dao/open-athens-entity-dao-db.properties"));

        return new IdentityProviderDaoDbImpl(properties.getProperty("open-athens-entity.dao.db.create"),
                properties.getProperty("open-athens-entity.dao.db.read"),
                properties.getProperty("open-athens-entity.dao.db.filter"),
                dbExecutor,
                OpenAthensEntity.class);
    }

    @Provides @Named("oauthEntity")
    @Singleton
    public IdentityProviderDao provideOauthEntityDao(DbExecutor dbExecutor) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        Properties properties = new Properties();

        properties.load(classLoader.getResourceAsStream("dao/oauth-entity-dao-db.properties"));

        return new IdentityProviderDaoDbImpl(properties.getProperty("oauth-entity.dao.db.create"),
                properties.getProperty("oauth-entity.dao.db.read"),
                properties.getProperty("oauth-entity.dao.db.filter"),
                dbExecutor,
                OauthEntity.class);
    }

    @Provides @Named("identityProviderDaoMap")
    @Singleton
    public Map<IdentityProviderType, IdentityProviderDao> provideIdentityProviderDaoMap(
            @Named("samlEntity") IdentityProviderDao samlDao,
            @Named("openAthensEntity") IdentityProviderDao openAthensDao,
            @Named("oauthEntity") IdentityProviderDao oauthDao) {
        Map<IdentityProviderType, IdentityProviderDao> daoMap = new HashMap<>();
        daoMap.put(IdentityProviderType.SAML, samlDao);
        daoMap.put(IdentityProviderType.OPEN_ATHENS, openAthensDao);
        daoMap.put(IdentityProviderType.OAUTH, oauthDao);
        return daoMap;
    }

    @Provides @Named("beanFactoryMap")
    @Singleton
    public Map<Class<?>, BeanFactory<?>> provideBeanFactoryMap(AuthenticatableBeanFactory authenticatableBeanFactory,
                                                               AuthenticationCredentialsBeanFactory credentialsBeanFactory) {
        Map<Class<?>, BeanFactory<?>> beanFactoryMap = new HashMap<>();

        beanFactoryMap.put(Authenticatable.class, authenticatableBeanFactory);
        beanFactoryMap.put(AuthenticationCredentials.class, credentialsBeanFactory);

        return beanFactoryMap;
    }

    @Provides
    @Singleton
    public JedisPool getJedisPool(@Named("redis.host") String redisHost, @Named("redis.port") int redisPort) {
        return new JedisPool(new JedisPoolConfig(), redisHost, redisPort);
    }

    @Provides
    @Singleton
    public NamedParameterJdbcTemplate getJdbcTemplate(
            @Named("jdbc.driver") String driver,
            @Named("jdbc.username") String username,
            @Named("jdbc.password") String password,
            @Named("jdbc.url") String url,
            @Named("jdbc.maxActive") Integer maxActive,
            @Named("jdbc.maxIdle") Integer maxIdle,
            @Named("jdbc.initialSize") Integer initialSize,
            @Named("jdbc.validationQuery") String validationQuery) {
        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setDriverClassName(driver);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setUrl(url);
        dataSource.setMaxActive(maxActive);
        dataSource.setMaxIdle(maxIdle);
        dataSource.setInitialSize(initialSize);
        dataSource.setValidationQuery(validationQuery);

        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Provides
    @Named("jwtSecret")
    @Singleton
    public String getJwtSecret() {
        return "shh_its_a_secret";
    }

    @Provides
    @Named("authenticationCacheGroup")
    @Singleton
    public String getAuthenticationCacheGroupName() {
        return "AUTHENTICATION_CACHE_GROUP";
    }

    @Provides
    @Named("passwordSaltCacheGroup")
    @Singleton
    public String getPasswordSaltCacheGroupName() {
        return "PASSWORD_SALT_CACHE_GROUP";
    }

    @Provides
    @Named("authenticatableRedisDao")
    @Singleton
    public RedisDao<AuthenticationCredentials, AuthenticatedEntity> getAuthenticatableRedisDao(JedisPool jedisPool) {
        return new RedisDaoImpl<AuthenticationCredentials, AuthenticatedEntity>()
                .setPrefix("AUTHENTICABLE")
                .setPool(jedisPool)
                .setTtlSeconds(172800)
                .setDeserializer((json) -> AuthenticatableRedisSerializer.deserialize((String) json))
                .setSerializer((authenticatable) -> AuthenticatableRedisSerializer.serialize((AuthenticatedEntity) authenticatable));
    }

    @Provides
    @Named("authenticatableCache")
    @Singleton
    public LoadingCache<AuthenticationCredentials, AuthenticatedEntity> getLoadingCache(
            @Named("authenticatableRedisDao") RedisDao<AuthenticationCredentials, AuthenticatedEntity> authenticatableRedisDao,
            AuthenticationFacade authenticationFacade,
            CacheManager cacheManager,
            @Named("authenticationCacheGroup") String authenticationCacheGroupName
    ) {
        LoadingCacheRedisImpl<AuthenticationCredentials, AuthenticatedEntity> l2Cache = new LoadingCacheRedisImpl<>();
        l2Cache.setRedisDao(authenticatableRedisDao);
        l2Cache.setCacheLoader((key) -> authenticationFacade.determineDao(key).authenticate(key));
        l2Cache.setName("AUTHENTICATION_REDIS_CACHE");

        LoadingCacheGuavaImpl<AuthenticationCredentials, AuthenticatedEntity> l1Cache = new LoadingCacheGuavaImpl<>();
        l1Cache.setGuavaCache(CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.DAYS).build());
        l1Cache.setCacheLoader((key) -> l2Cache.get(key));
        l2Cache.setName("AUTHENTICATION_GUAVA_CACHE");

        cacheManager.registerCacheGroup(authenticationCacheGroupName, l1Cache, l2Cache);

        return l1Cache;
    }

    @Provides
    @Named("publisherSaltRedisDao")
    @Singleton
    public RedisDao<Long, String> getPublisherSaltRedisDao(JedisPool jedisPool) {
        return new RedisDaoImpl<String, AuthenticatedEntity>()
                .setPrefix("PUBLISHER_SALT")
                .setPool(jedisPool)
                .setTtlSeconds(172800)
                .setDeserializer((salt) -> salt)
                .setSerializer((salt) -> salt);
    }

    @Provides
    @Named("publisherSaltRedisCache")
    @Singleton
    public LoadingCache<Long, String> getLoadingCache(
            @Named("publisherSaltRedisDao") RedisDao<Long, String> publisherSaltRedisDao,
            PublisherDao publisherDao) {
        LoadingCacheRedisImpl<Long, String> l2Cache = new LoadingCacheRedisImpl<>();
        l2Cache.setRedisDao(publisherSaltRedisDao);
        l2Cache.setCacheLoader((key) -> publisherDao.read(key).map((publisher) -> {LOG.debug("found publisher for l3 [{}]", publisher.getSalt());return publisher.getSalt();}));

        return l2Cache;
    }

    @Provides
    @Named("publisherSaltCache")
    @Singleton
    public Cache<Long, String> getPublisherSaltLoadingCache(
            @Named("publisherSaltRedisCache") LoadingCache<Long, String> publisherSaltRedisCache) {
        LoadingCacheGuavaImpl<Long, String> l1Cache = new LoadingCacheGuavaImpl<>();
        l1Cache.setGuavaCache(CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.DAYS).build());
        l1Cache.setCacheLoader((key) -> publisherSaltRedisCache.get(key));

        return l1Cache;
    }

    @Provides
    @Named("passwordSaltRedisDao")
    public RedisDao<String, String> getAdminSaltRedisDao(JedisPool jedisPool) {
        return new RedisDaoImpl<String, AuthenticatedEntity>()
                .setPrefix("ADMIN_SALT")
                .setPool(jedisPool)
                .setTtlSeconds(172800)
                .setDeserializer((salt) -> salt)
                .setSerializer((salt) -> salt);
    }

    @Provides
    @Named("passwordSaltCache")
    @Singleton
    public Cache<String, String> getAdminSaltLoadingCache(
            CacheManager cacheManager,
            PasswordCredentialsFacade passwordCredentialsFacade,
            @Named("passwordSaltRedisDao") RedisDao<String, String> passwordSaltRedisDao,
            @Named("passwordSaltCacheGroup") String passwordSaltCacheGroup) {

        LoadingCacheRedisImpl<String, String> l2Cache = new LoadingCacheRedisImpl<>();
        l2Cache.setRedisDao(passwordSaltRedisDao);
        l2Cache.setCacheLoader((email) -> passwordCredentialsFacade.getSaltForEmail(email).toMaybe());

        LoadingCacheGuavaImpl<String, String> l1Cache = new LoadingCacheGuavaImpl<>();
        l1Cache.setGuavaCache(CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.DAYS).build());
        l1Cache.setCacheLoader((key) -> l2Cache.get(key));

        cacheManager.registerCacheGroup(passwordSaltCacheGroup, l1Cache, l2Cache);

        return l1Cache;
    }
}