package org.dreamcat.common.agent;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.io.IniUtil;
import org.dreamcat.common.util.ReflectUtil;

/**
 * @author Jerry Will
 * @version 2022-04-23
 */
@Slf4j
@RequiredArgsConstructor
public class PluginManager {

    private static final String MANIFEST_NAME = "Plugin-Class";

    private final Instrumentation inst;
    private final Environment env;
    private final ChainTransformer transformer;
    private final List<PostConfigurationProcessor> postConfigurationProcessors;

    public void loadPlugins() {
        File pluginDir = new File(env.getBaseDir(), "plugins");
        if (!pluginDir.exists()) {
            if (env.isDebug()) {
                log.debug("plugin dir {} is not found", pluginDir);
            }
            return;
        }
        File[] files;
        if (!pluginDir.isDirectory() || (files = pluginDir.listFiles()) == null) {
            log.warn("plugin dir {} is not a dir", pluginDir);
            return;
        }
        for (File file : files) {
            if (!file.isFile() ||
                    !file.getName().toLowerCase().endsWith(".jar")) continue;
            try {
                loadPluginJar(file);
            } catch (Exception e) {
                log.error("fail to load plugin {}, error: {}", file, e.getMessage());
            }
        }
    }

    private void loadPluginJar(File file) throws Exception {
        PluginClassLoader classLoader = new PluginClassLoader(file);
        Class<?> clazz;
        try (JarFile jarFile = new JarFile(file)) {
            clazz = getEntryClass(jarFile, classLoader);
        }
        if (clazz == null) return;

        PluginEntry entry = (PluginEntry) ReflectUtil.newInstance(clazz);
        String pluginName = entry.getName();
        if (!pluginName.matches("[0-9a-zA-Z][_0-9a-zA-Z]*")) {
            log.warn("{} has a invalid plugin name {}", clazz, pluginName);
            return;
        }
        Map<String, String> config = env.getConfigMap()
                .getOrDefault(pluginName, Collections.emptyMap());
        entry.init(config);

        transformer.addTransformers(entry.getTransformers());
        PostConfigurationProcessor processor = entry.getPostConfigurationProcessor();
        if (processor != null) postConfigurationProcessors.add(processor);

        log.info("loaded plugin {}", pluginName);
    }

    private Class<?> getEntryClass(JarFile jarFile, ClassLoader classLoader) throws Exception {
        Manifest manifest = jarFile.getManifest();
        String pluginClass = manifest.getMainAttributes().getValue(MANIFEST_NAME);
        if (pluginClass == null || pluginClass.trim().isEmpty()) {
            return null;
        }

        Class<?> clazz = Class.forName(pluginClass, false, classLoader);
        if (!PluginEntry.class.isAssignableFrom(clazz)) {
            if (env.isDebug()) {
                log.debug("{} doesn't extend to PluginEntry", clazz);
            }
            return null;
        }

        synchronized (inst) {
            // only use getName, close is ok
            inst.appendToBootstrapClassLoaderSearch(jarFile);
        }
        return clazz;
    }
}
