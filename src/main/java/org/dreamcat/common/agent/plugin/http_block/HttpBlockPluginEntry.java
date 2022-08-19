package org.dreamcat.common.agent.plugin.http_block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.dreamcat.common.agent.PluginEntry;
import org.dreamcat.common.agent.Transformer;
import org.dreamcat.common.agent.plugin.dns_block.InetAddressTransformer;

/**
 * @author Jerry Will
 * @version 2022-04-25
 */
public class HttpBlockPluginEntry implements PluginEntry {

    private static final String plugin_name = "http_block";
    static Map<String, String> configMap = Collections.emptyMap();

    private final List<Transformer> transformers = new ArrayList<>();

    @Override
    public String getName() {
        return plugin_name;
    }

    @Override
    public void init(Map<String, String> map) {
        configMap = Collections.unmodifiableMap(map);
        transformers.add(new HttpClientTransformer(this));
    }

    @Override
    public List<Transformer> getTransformers() {
        return transformers;
    }

}
