package com.antkorwin.statemachine.config;

import com.antkorwin.statemachine.persist.CustomStateMachinePersist;
import com.antkorwin.statemachine.persist.InMemoryStateMachinePersist;
import com.antkorwin.statemachine.statemachine.Events;
import com.antkorwin.statemachine.statemachine.States;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.data.mongodb.MongoDbPersistingStateMachineInterceptor;
import org.springframework.statemachine.data.mongodb.MongoDbStateMachineRepository;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;

import java.util.UUID;

/**
 * Created by Korovin Anatolii on 09.05.2018.
 *
 * @author Korovin Anatolii
 * @version 1.0
 */
@Configuration
public class PersistConfig {

    @Bean
    @Profile({"in-memory", "default"})
    public StateMachinePersist<States, Events, UUID> inMemoryPersist() {
        return new InMemoryStateMachinePersist();
    }

    @Bean
    @Profile("custom")
    public StateMachinePersist<States, Events, UUID> customPersist() {
        return new CustomStateMachinePersist();
    }

    @Bean
    @Profile("mongo")
    public StateMachineRuntimePersister<States, Events, UUID> mongoRuntimePersist(
            MongoDbStateMachineRepository repository) {
        return new MongoDbPersistingStateMachineInterceptor<>(repository);
    }

    @Bean
    public StateMachinePersister<States, Events, UUID> persister(
            StateMachinePersist<States, Events, UUID> defaultPersist) {

        return new DefaultStateMachinePersister<>(defaultPersist);
    }
}
