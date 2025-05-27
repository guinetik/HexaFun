package com.guinetik.hexafun;

import java.util.Set;

/**
 * Default implementation of HexaApp.
 */
public  class HexaAppImpl extends HexaApp {

    /**
     * Create a new empty HexaAppImpl.
     */
    public HexaAppImpl() {
        // Uses the parent's maps
    }

    @Override
    public Set<String> registeredUseCases() {
        return useCases.keySet();
    }
}
