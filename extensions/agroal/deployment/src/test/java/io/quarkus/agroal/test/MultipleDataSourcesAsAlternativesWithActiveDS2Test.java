package io.quarkus.agroal.test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InjectableInstance;
import io.quarkus.test.QuarkusUnitTest;

/**
 * Tests a use case where multiple datasources are defined at build time,
 * but only one is used at runtime.
 * <p>
 * This is mostly useful when each datasource has a distinct db-kind, but in theory that shouldn't matter,
 * so we use the h2 db-kind everywhere here to keep test dependencies simpler.
 * <p>
 * See {@link MultipleDataSourcesAsAlternativesWithActiveDS1Test} for the counterpart where PU2 is used at runtime.
 */
public class MultipleDataSourcesAsAlternativesWithActiveDS2Test {

    @RegisterExtension
    static QuarkusUnitTest runner = new QuarkusUnitTest()
            .withApplicationRoot((jar) -> jar
                    .addClass(MyProducer.class))
            .overrideConfigKey("quarkus.datasource.ds-1.db-kind", "h2")
            .overrideConfigKey("quarkus.datasource.ds-1.active", "false")
            .overrideConfigKey("quarkus.datasource.ds-2.db-kind", "h2")
            .overrideConfigKey("quarkus.datasource.ds-2.active", "false")
            // This is where we select datasource 2
            .overrideRuntimeConfigKey("quarkus.datasource.ds-2.active", "true")
            .overrideRuntimeConfigKey("quarkus.datasource.ds-2.jdbc.url", "jdbc:h2:mem:testds2");

    @Inject
    @DataSource("ds-2")
    AgroalDataSource explicitDatasourceBean;

    @Inject
    AgroalDataSource customIndirectDatasourceBean;

    @Test
    public void testExplicitDatasourceBeanUsable() {
        doTestDatasource(explicitDatasourceBean);
    }

    @Test
    public void testCustomIndirectDatasourceBeanUsable() {
        doTestDatasource(customIndirectDatasourceBean);
    }

    @Test
    public void testInactiveDatasourceBeanUnusable() {
        assertThatThrownBy(() -> Arc.container().select(AgroalDataSource.class, new DataSource.DataSourceLiteral("ds-1")).get()
                .getConnection())
                .hasMessageContaining("Datasource 'ds-1' was deactivated through configuration properties.");
    }

    private static void doTestDatasource(AgroalDataSource dataSource) {
        assertThatCode(() -> {
            try (var connection = dataSource.getConnection()) {
            }
        })
                .doesNotThrowAnyException();
    }

    private static class MyProducer {
        @Inject
        @DataSource("ds-1")
        InjectableInstance<AgroalDataSource> dataSource1Bean;

        @Inject
        @DataSource("ds-2")
        InjectableInstance<AgroalDataSource> dataSource2Bean;

        @Produces
        @ApplicationScoped
        public AgroalDataSource dataSource() {
            if (dataSource1Bean.getHandle().getBean().isActive()) {
                return dataSource1Bean.get();
            } else if (dataSource2Bean.getHandle().getBean().isActive()) {
                return dataSource2Bean.get();
            } else {
                throw new RuntimeException("No active datasource!");
            }
        }
    }
}
