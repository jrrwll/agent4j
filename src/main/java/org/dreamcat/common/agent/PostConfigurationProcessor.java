package org.dreamcat.common.agent;

import java.lang.instrument.Instrumentation;

/**
 * @author Jerry Will
 * @version 2022-08-19
 */
public interface PostConfigurationProcessor {

    String getProcessorName();

    void process(Instrumentation inst);
}
