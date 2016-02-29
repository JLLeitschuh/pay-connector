package uk.gov.pay.connector.it.dao;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.jpa.JpaPersistModule;
import uk.gov.pay.connector.app.DataAccessModule;

public class GuicedTestEnvironment {

    private final Injector injector;

    private GuicedTestEnvironment(JpaPersistModule persistModule) {
        injector = Guice.createInjector(new DataAccessModule(),persistModule);
    }

    public static GuicedTestEnvironment from(JpaPersistModule persistModule) {
        return new GuicedTestEnvironment(persistModule);
    }

    public GuicedTestEnvironment start() {
        injector.getInstance(PersistService.class).start();
        return this;
    }

    public GuicedTestEnvironment stop() {
        injector.getInstance(PersistService.class).stop();
        return this;
    }

    public <T> T getInstance(Class<T> daoClass) {
        return injector.getInstance(daoClass);
    }
}
