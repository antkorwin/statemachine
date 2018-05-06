package com.antkorwin.statemachine.config;

import com.antkorwin.statemachine.persist.CustomStateMachinePersist;
import com.antkorwin.statemachine.persist.InMemoryStateMachinePersist;
import com.antkorwin.statemachine.statemachine.Events;
import com.antkorwin.statemachine.statemachine.States;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.data.mongodb.MongoDbPersistingStateMachineInterceptor;
import org.springframework.statemachine.data.mongodb.MongoDbStateMachineRepository;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.transition.Transition;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by Korovin Anatolii on 05.05.2018.
 *
 * @author Korovin Anatolii
 * @version 1.0
 */
@Slf4j
@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<States, Events> config) throws Exception {
        config.withConfiguration()
              .listener(listener())
              .autoStartup(true);
    }

    private StateMachineListener<States, Events> listener() {

        return new StateMachineListenerAdapter<States, Events>() {
            @Override
            public void eventNotAccepted(Message<Events> event) {
                log.error("Not accepted event: {}", event);
            }

            @Override
            public void transition(Transition<States, Events> transition) {
                if (transition != null &&
                    transition.getSource() != null && transition.getSource().getId() != null &&
                    transition.getTarget() != null && transition.getTarget().getId() != null) {

                    log.warn("Move from {} to {}", transition.getSource().getId(), transition.getTarget().getId());
                }
            }
        };


    }


    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception {
        states.withStates()
              .initial(States.BACKLOG, developersWakeUpAction())
              .state(States.IN_PROGRESS, weNeedCoffeeAction())
              .state(States.TESTING, qaWakeUpAction())
              .state(States.DONE, goToSleepAction())
              .end(States.DONE);
    }

    private Action<States, Events> developersWakeUpAction() {
        return stateContext -> log.warn("Просыпайтесь лентяи!");
    }

    private Action<States, Events> weNeedCoffeeAction() {
        return stateContext -> log.warn("Без кофе никак!");
    }

    private Action<States, Events> qaWakeUpAction() {
        return stateContext -> log.warn("Будим команду тестирования, солнце высоко!");
    }

    private Action<States, Events> goToSleepAction() {
        return stateContext -> log.warn("Всем спать! клиент доволен.");
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions) throws Exception {
        transitions.withExternal()
                   .source(States.BACKLOG)
                   .target(States.IN_PROGRESS)
                   .event(Events.START_FEATURE)
                   .and()
                   // DEVELOPERS:
                   .withExternal()
                   .source(States.IN_PROGRESS)
                   .target(States.TESTING)
                   .event(Events.FINISH_FEATURE)
                   .guard(alreadyDeployedGuard())
                   .and()
                   // QA-TEAM:
                   .withExternal()
                   .source(States.TESTING)
                   .target(States.DONE)
                   .event(Events.QA_CHECKED_UC)
                   .and()
                   .withExternal()
                   .source(States.TESTING)
                   .target(States.IN_PROGRESS)
                   .event(Events.QA_REJECTED_UC)
                   .and()
                   // ROCK-STAR:
                   .withExternal()
                   .source(States.BACKLOG)
                   .target(States.TESTING)
                   .event(Events.ROCK_STAR_DOUBLE_TASK)
                   .and()
                   // DEVOPS:
                   .withInternal()
                   .source(States.IN_PROGRESS)
                   .event(Events.DEPLOY)
                   .action(deployPreProd())
                   .and()
                   .withInternal()
                   .source(States.BACKLOG)
                   .event(Events.DEPLOY)
                   .action(deployPreProd());
    }

    private Guard<States, Events> alreadyDeployedGuard() {
        return context -> Optional.ofNullable(context.getExtendedState().getVariables().get("deployed"))
                                  .map(v -> (boolean) v)
                                  .orElse(false);
    }

    private Action<States, Events> deployPreProd() {
        return stateContext -> {
            log.warn("DEPLOY: Выкатываемся на препродакшен.");
            stateContext.getExtendedState().getVariables().put("deployed", true);
        };
    }

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
