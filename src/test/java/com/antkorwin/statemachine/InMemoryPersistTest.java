package com.antkorwin.statemachine;

import com.antkorwin.statemachine.statemachine.Events;
import com.antkorwin.statemachine.statemachine.States;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

/**
 * Created by Korovin Anatolii on 06.05.2018.
 *
 * @author Korovin Anatolii
 * @version 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("in-memory")
public class InMemoryPersistTest {

    @Autowired
    private StateMachinePersister<States, Events, UUID> persister;

    @Autowired
    private StateMachineFactory<States, Events> stateMachineFactory;

    @Test
    public void testInMemoryPersist() throws Exception {
        // Arrange
        StateMachine<States, Events> firstStateMachine = stateMachineFactory.getStateMachine();
        firstStateMachine.sendEvent(Events.START_FEATURE);
        firstStateMachine.sendEvent(Events.DEPLOY);

        StateMachine<States, Events> secondStateMachine = stateMachineFactory.getStateMachine();

        // Check Precondition
        Assertions.assertThat(firstStateMachine.getState().getId()).isEqualTo(States.IN_PROGRESS);
        Assertions.assertThat((boolean) firstStateMachine.getExtendedState().getVariables().get("deployed"))
                  .isEqualTo(true);
        Assertions.assertThat(secondStateMachine.getState().getId()).isEqualTo(States.BACKLOG);
        Assertions.assertThat(secondStateMachine.getExtendedState().getVariables().get("deployed")).isNull();

        // Act
        persister.persist(firstStateMachine, firstStateMachine.getUuid());
        persister.restore(secondStateMachine, firstStateMachine.getUuid());

        // Asserts
        Assertions.assertThat(secondStateMachine.getState().getId()).isEqualTo(States.IN_PROGRESS);
        Assertions.assertThat((boolean) secondStateMachine.getExtendedState().getVariables().get("deployed"))
                  .isEqualTo(true);
    }
}
