package org.dreamcat.common.agent.util;

import java.lang.instrument.Instrumentation;

/**
 * @author Jerry Will
 * @version 2021-11-03
 */
public class InstrumentationUtil {

    /**
     * injected by {@link org.dreamcat.common.agent.Launcher}
     */
    private static Instrumentation inst;

    private InstrumentationUtil() {
    }

    public static long getObjectSize(Object o) {
        return inst.getObjectSize(o);
    }

}
