package kipoderax.testConfigExternalProps;

import kipoderax.virtuallotto.test.jms.FakeJmsBroker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:testing.properties")
public class ExternalPropsEnvironment {

    @Autowired
    Environment env;

    @Bean
    public FakeJmsBroker fakeJmsBrokerEnv() {
        FakeJmsBroker fakeJmsBroker = new FakeJmsBroker();
        fakeJmsBroker.setUrl(env.getProperty("kipoderax.jms.server"));
        fakeJmsBroker.setPort(env.getRequiredProperty("kipoderax.jms.port", Integer.class));
        fakeJmsBroker.setUser(env.getProperty("kipoderax.jms.user"));
        fakeJmsBroker.setPassword(env.getProperty("kipoderax.jms.password"));

        return fakeJmsBroker;
    }
}