package ua.danit.logging.configuration;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring configuration for DAO and data services module.
 *
 * @author Andrey Minov.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories("ua.danit.logging.dao")
@ComponentScan("ua.danit.logging.dao")
@PropertySource("/WEB-INF/application.properties")
public class DataConfiguration {
  @Value("${db.url}")
  private String url;
  @Value("${db.username}")
  private String username;
  @Value("${db.password}")
  private String password;
  @Value("${db.driver}")
  private String driverClassname;
  @Value("${db.dialect}")
  private String dialect;
  @Value("${db.connection.max_active:5}")
  private Integer maxActiveConnections;
  @Value("${db.connection.max_idle:1}")
  private Integer maxIdleConnection;
  @Value("${db.connection.test_on_borrow:false}")
  private Boolean testConnectionOnReturn;
  @Value("${db.connection.test_on_return:false}")
  private Boolean testConnectionOnBorrow;
  @Value("${db.model_packages}")
  private String modelPackages;
  @Value("${db.show_queries:false}")
  private String showQueries;

  @Value("classpath:create_script.sql")
  private Resource scriptResource;

  /**
   * Data source basic data source.
   *
   * @return spring bean containing data source of application.
   */
  @Bean(destroyMethod = "close")
  public BasicDataSource dataSource() {
    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setUrl(url);
    dataSource.setUsername(username);
    dataSource.setPassword(password);
    dataSource.setDriverClassName(driverClassname);
    dataSource.setMaxActive(maxActiveConnections);
    dataSource.setMaxIdle(maxIdleConnection);
    dataSource.setTestOnReturn(testConnectionOnReturn);
    dataSource.setTestOnReturn(testConnectionOnBorrow);
    return dataSource;
  }

  /**
   * Entity manager factory bean.
   *
   * @param dataSource the data source of the application
   * @return the local container entity manager factory bean for managing JPA transactions.
   */
  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
    HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
    jpaVendorAdapter.setDatabasePlatform(dialect);
    jpaVendorAdapter.setShowSql("true".equals(showQueries));

    LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
    bean.setJpaVendorAdapter(jpaVendorAdapter);
    bean.setDataSource(dataSource);
    bean.setPackagesToScan(modelPackages);
    return bean;
  }

  /**
   * Transaction manager jpa transaction manager.
   *
   * @param entityManagerFactory the entity manager factory for creating JPA entity managers.
   * @return the jpa transaction manager for managing transactions of application correctly.
   */
  @Bean
  public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }


  /**
   * Data source initializer for initializing HSQL database,
   *
   * @param dataSource the data source for JDBC connections.
   * @return the data source initializer
   */
  @Bean
  public DataSourceInitializer dataSourceInitializer(final DataSource dataSource) {
    ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
    populator.addScript(scriptResource);

    DataSourceInitializer initializer = new DataSourceInitializer();
    initializer.setDataSource(dataSource);
    initializer.setDatabasePopulator(populator);
    return initializer;
  }
}
