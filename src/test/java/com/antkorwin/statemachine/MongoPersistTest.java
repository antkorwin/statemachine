package com.antkorwin.statemachine;

import com.antkorwin.statemachine.statemachine.Events;
import com.antkorwin.statemachine.statemachine.States;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Korovin Anatolii on 06.05.2018.
 *
 * @author Korovin Anatolii
 * @version 1.0
 */
@ActiveProfiles("mongo")
public class MongoPersistTest extends BaseMongoIT {

    @Autowired
    private StateMachinePersister<States, Events, UUID> persister;
    @Autowired
    private StateMachineFactory<States, Events> stateMachineFactory;

    @Before
    public void setUp() throws Exception {
        mongoTemplate.getCollectionNames()
                     .forEach(mongoTemplate::dropCollection);
    }

    @Test
    public void firstTest() {
        // Arrange
        // Act
        Set<String> collectionNames = mongoTemplate.getCollectionNames();
        // Asserts
        Assertions.assertThat(collectionNames).isEmpty();
    }

    @Test
    public void testMongoPersist() throws Exception {
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
        persister.persist(secondStateMachine, secondStateMachine.getUuid());
        persister.restore(secondStateMachine, firstStateMachine.getUuid());

        // Asserts
        Assertions.assertThat(secondStateMachine.getState().getId())
                  .isEqualTo(States.IN_PROGRESS);

        Assertions.assertThat((boolean) secondStateMachine.getExtendedState().getVariables().get("deployed"))
                  .isEqualTo(true);

        // Mongo specific asserts:
        Assertions.assertThat(mongoTemplate.getCollectionNames())
                  .isNotEmpty();
        List<Document> documents = mongoTemplate.findAll(Document.class,
                                                         "MongoDbRepositoryStateMachine");

        Assertions.assertThat(documents).hasSize(2);
        Assertions.assertThat(documents)
                  .flatExtracting(Document::values)
                  .contains(firstStateMachine.getUuid().toString(),
                            secondStateMachine.getUuid().toString())
                  .contains(firstStateMachine.getState().getId().toString(),
                            secondStateMachine.getState().getId().toString());
    }
}
