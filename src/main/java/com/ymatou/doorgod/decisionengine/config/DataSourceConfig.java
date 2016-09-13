/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import com.alibaba.druid.pool.DruidDataSource;
import com.ymatou.common.mybatis.annotation.MyBatisDao;
import com.ymatou.common.mybatis.interceptor.PaginationInterceptor;
import com.ymatou.common.mybatis.typehandler.SerializableTypeHandler;


@Configuration
@MapperScan(basePackages = "com.ymatou.doorgod.decisionengine.dao.mapper", annotationClass = MyBatisDao.class)
@EnableJpaRepositories(basePackages = "com.ymatou.doorgod.decisionengine.repository")
@EnableJpaAuditing
@EnableTransactionManagement(proxyTargetClass = true)
@EnableConfigurationProperties({ConnectionConfig.class})
public class DataSourceConfig
        implements TransactionManagementConfigurer {
    @Autowired
    private ConnectionConfig connectionConfig;

    @Bean
    public DataSource dataSource() {

        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(connectionConfig.getDriver());
        dataSource.setUrl(connectionConfig.getUrl());
        dataSource.setUsername(connectionConfig.getUsername());
        dataSource.setPassword(connectionConfig.getPassword());
        dataSource.setInitialSize(connectionConfig.getInitialSize());
        dataSource.setMinIdle(connectionConfig.getMinIdle());
        dataSource.setMaxActive(connectionConfig.getMaxActive());
        dataSource.setDefaultAutoCommit(false);

        return dataSource;
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {

        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();

        // 获取properties中的对应配置信息
        String mapperPackage = "/mapper/*Mapper.xml";
        String dialect = "mysql";

        Properties properties = new Properties();
        properties.setProperty("dialect", dialect);
        properties.setProperty("transactionManager", "MANAGED");


        sessionFactory.setDataSource(dataSource());
        sessionFactory.setConfigurationProperties(properties);
        // 设置MapperLocations路径
        if (!StringUtils.isEmpty(mapperPackage)) {
            ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
            sessionFactory.setMapperLocations(resourcePatternResolver.getResources(mapperPackage));
        }
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        Properties pageProp = new Properties();
        pageProp.setProperty("dialect", dialect);
        paginationInterceptor.setProperties(pageProp);

        // 设置插件
        sessionFactory.setPlugins(new Interceptor[] {paginationInterceptor});

        sessionFactory.setTypeHandlers(new TypeHandler[] {new SerializableTypeHandler()});

        return sessionFactory.getObject();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactory.setDataSource(dataSource);

        HibernatePersistenceProvider persistenceProvider = new HibernatePersistenceProvider();
        JpaDialect jpaDialect = new HibernateJpaDialect();

        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
        properties.setProperty("hibernate.show_sql", "true");
        entityManagerFactory.setPackagesToScan("com.ymatou.doorgod.decisionengine.model");
        entityManagerFactory.setJpaProperties(properties);
        entityManagerFactory.setPersistenceProvider(persistenceProvider);
        entityManagerFactory.setJpaDialect(jpaDialect);
        entityManagerFactory.setLoadTimeWeaver(new InstrumentationLoadTimeWeaver());
        return entityManagerFactory;
    }



    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return transactionManager();
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager() {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource());
        return transactionManager;
    }

    @Bean(name = "transactionTemplate")
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        return transactionTemplate;
    }

}
