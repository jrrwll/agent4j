package org.dreamcat.common.agent.plugin.dns_block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.dreamcat.common.agent.PluginEntry;
import org.dreamcat.common.agent.Transformer;

/**
 * @author Jerry Will
 * @version 2022-04-23
 */
public class DnsBlockPluginEntry implements PluginEntry {

    private static final String plugin_name = "dns_block";
    static Map<String, String> configMap = Collections.emptyMap();

    private final List<Transformer> transformers = new ArrayList<>();

    @Override
    public String getName() {
        return plugin_name;
    }

    @Override
    public void init(Map<String, String> map) {
        configMap = Collections.unmodifiableMap(map);
        transformers.add(new InetAddressTransformer(this));
    }

    @Override
    public List<Transformer> getTransformers() {
        return transformers;
    }
}
