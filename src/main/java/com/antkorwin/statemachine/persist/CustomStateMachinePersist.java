package com.antkorwin.statemachine.persist;

import com.antkorwin.statemachine.statemachine.Events;
import com.antkorwin.statemachine.statemachine.States;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Korovin Anatolii on 06.05.2018.
 *
 * @author Korovin Anatolii
 * @version 1.0
 */
@Slf4j
public class CustomStateMachinePersist implements StateMachinePersist<States, Events, UUID> {

    private HashMap<UUID, StateMachineContext<States,Events>> storage = new HashMap<>();

    @Override
    public void write(StateMachineContext<States, Events> context, UUID contextObj) throws Exception {
        log.warn("!!!push: "+contextObj);
        storage.put(contextObj, context);
    }

    @Override
    public StateMachineContext<States, Events> read(UUID contextObj) throws Exception {
        log.warn("!!!pop: "+contextObj);
        return storage.get(contextObj);
    }
}
