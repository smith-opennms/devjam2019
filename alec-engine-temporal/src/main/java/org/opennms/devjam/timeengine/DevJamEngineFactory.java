package org.opennms.devjam.timeengine;

import org.opennms.alec.engine.api.Engine;
import org.opennms.alec.engine.api.EngineFactory;

public class DevJamEngineFactory implements EngineFactory {

    public String getName() {
        return "DevJam!";
    }

    public Engine createEngine() {
        return new DevJamEngine();
    }

}
