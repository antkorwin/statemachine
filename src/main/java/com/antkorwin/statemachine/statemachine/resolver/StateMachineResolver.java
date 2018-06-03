package com.antkorwin.statemachine.statemachine.resolver;

import org.springframework.statemachine.StateMachine;

import java.util.List;

/**
 * Created by Korovin Anatolii on 02.06.2018.
 *
 * @author Korovin Anatolii
 * @version 1.0
 */
public interface StateMachineResolver<S, E> {

    /**
     * Evaluate available events from current states of state-machine
     *
     * @param stateMachine state machine
     *
     * @return Events collection
     */
    List<E> getAvailableEvents(StateMachine<S, E> stateMachine);
}
