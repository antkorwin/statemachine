package com.antkorwin.statemachine.config;

import com.antkorwin.statemachine.statemachine.Events;
import com.antkorwin.statemachine.statemachine.States;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

import java.util.Optional;

/**
 * Created by Korovin Anatolii on 05.05.2018.
 * <p>
 * Second state machine, you can:
 * - use this other states and events
 * - make other business logic configuration
 *
 * @author Korovin Anatolii
 * @version 1.0
 */
@Slf4j
@Configuration
@EnableStateMachineFactory(name = "secondStateMachineFactory")
public class SecondStateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {

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
                log.error("SECOND: Not accepted event: {}", event);
            }

            @Override
            public void transition(Transition<States, Events> transition) {
                log.info("TRANS from: {} to: {}",
                         ofNullableState(transition.getSource()),
                         ofNullableState(transition.getTarget()));
            }

            private Object ofNullableState(State s) {
                return Optional.ofNullable(s)
                               .map(State::getId)
                               .orElse(null);
            }
        };
    }

    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception {
        states.withStates()
              .initial(States.BACKLOG)
              .end(States.DONE);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions) throws Exception {
        transitions.withExternal()
                   .source(States.BACKLOG)
                   .target(States.DONE)
                   .event(Events.START_FEATURE);
    }
}
