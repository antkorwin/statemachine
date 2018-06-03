package com.antkorwin.statemachine.statemachine.resolver;

import static org.junit.Assert.*;

import com.antkorwin.statemachine.statemachine.Events;
import com.antkorwin.statemachine.statemachine.States;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * Created by Korovin Anatolii on 02.06.2018.
 *
 * @author Korovin Anatolii
 * @version 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class StateMachineResolverIT {

    private StateMachine<States, Events> stateMachine;

    @Autowired
    private StateMachineFactory<States, Events> stateMachineFactory;



    @Before
    public void setUp() throws Exception {
        stateMachine = stateMachineFactory.getStateMachine();
    }

    @Autowired
    private StateMachineResolver<States, Events> stateMachineResolver;


    @Test
    public void testResolverWithoutGuard() {
        // Arrange
        // Act
        List<Events> availableEvents = stateMachineResolver.getAvailableEvents(stateMachine);
        // Asserts
        Assertions.assertThat(availableEvents)
                  .containsOnly(Events.START_FEATURE,
                                Events.ROCK_STAR_DOUBLE_TASK,
                                Events.DEPLOY);
    }

    @Test
    public void testResolverWithGuard() {
        // Arrange
        stateMachine.sendEvent(Events.START_FEATURE);
        // Act
        List<Events> availableEvents = stateMachineResolver.getAvailableEvents(stateMachine);
        // Asserts
        Assertions.assertThat(availableEvents)
                  .containsOnly(Events.DEPLOY);
    }
}