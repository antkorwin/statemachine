package com.antkorwin.statemachine;

import com.antkorwin.statemachine.statemachine.Events;
import com.antkorwin.statemachine.statemachine.States;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by Korovin Anatolii on 06.05.2018.
 *
 * @author Korovin Anatolii
 * @version 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MultipleFactoriesTest {

    @Autowired
    @Qualifier("secondStateMachineFactory")
    private StateMachineFactory<States, Events> secondStateMachineFactory;

    @Test
    public void test() {
        // Arrange
        StateMachine<States, Events> stateMachine = secondStateMachineFactory.getStateMachine();

        // Act
        stateMachine.sendEvent(Events.START_FEATURE);
        stateMachine.sendEvent(Events.DEPLOY);

        // Asserts
        Assertions.assertThat(stateMachine.getState().getId()).isEqualTo(States.DONE);
    }
}
