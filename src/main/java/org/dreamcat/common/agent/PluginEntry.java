package org.dreamcat.common.agent;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Jerry Will
 * @version 2022-04-23
 */
public interface PluginEntry {

    default void init(Map<String, String> configMap) {
        // nop
    }

    String getName();

    default String getVersion() {
        return "0.1.0";
    }

    default String getDescription() {
        return "Plugin " + getName();
    }

    default List<Transformer> getTransformers() {
        return Collections.emptyList();
    }

    default PostConfigurationProcessor getPostConfigurationProcessor() {
        return null;
    }

}
