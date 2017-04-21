package uk.gov.pay.connector.rules;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.EnvironmentCommand;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import org.eclipse.jetty.util.component.ContainerLifeCycle;

/**
 * Lightweight variation of server command for testing purposes.
 * Handles managed objects lifecycle.
 */
public class TestCommand<C extends Configuration> extends EnvironmentCommand<C> {

    private final Class<C> configurationClass;
    private ContainerLifeCycle container;

    public TestCommand(final Application<C> application) {
        super(application, "guicey-test", "Specific command to run guice context without jetty server");
        configurationClass = application.getConfigurationClass();
    }

    @Override
    protected void run(final Environment environment, final Namespace namespace,
                       final C configuration) throws Exception {
        // simulating managed objects lifecycle support
        container = new ContainerLifeCycle();
        environment.lifecycle().attach(container);
        container.start();
    }

    public void stop() {
        try {
            container.stop();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to stop managed objects container", e);
        }
        container.destroy();
    }

    @Override
    protected Class<C> getConfigurationClass() {
        return configurationClass;
    }
}