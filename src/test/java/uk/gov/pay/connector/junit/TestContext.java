package uk.gov.pay.connector.junit;

import io.dropwizard.db.DataSourceFactory;
import org.skife.jdbi.v2.DBI;
import uk.gov.pay.connector.util.DatabaseTestHelper;

public class TestContext {

    private final String databaseUrl;
    private final String databaseUser;
    private final String databasePassword;
    private DBI jdbi;
    // This should be out of the text context really (since it is a specific class for this project)
    // but is fine for now
    private DatabaseTestHelper databaseTestHelper;
    private int port;

    public TestContext(int port, DataSourceFactory dataSourceFactory) {
        databaseUrl = dataSourceFactory.getUrl();
        databaseUser = dataSourceFactory.getUser();
        databasePassword = dataSourceFactory.getPassword();
        jdbi = new DBI(databaseUrl, databaseUser, databasePassword);
        this.databaseTestHelper = new DatabaseTestHelper(jdbi);
        this.port = port;
    }

    public DBI getJdbi() {
        return jdbi;
    }

    public DatabaseTestHelper getDatabaseTestHelper() {
        return databaseTestHelper;
    }

    public int getPort() {
        return port;
    }

    String getDatabaseUrl() {
        return databaseUrl;
    }

    String getDatabaseUser() {
        return databaseUser;
    }

    String getDatabasePassword() {
        return databasePassword;
    }
}
