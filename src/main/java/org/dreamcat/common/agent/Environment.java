package org.dreamcat.common.agent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.io.ClassPathUtil;
import org.dreamcat.common.io.IniUtil;
import org.dreamcat.common.util.SystemUtil;

/**
 * @author Jerry Will
 * @version 2022-04-23
 */
@Slf4j
@Getter
public class Environment {

    private final boolean attach;
    private boolean debug;
    private final File baseDir;
    private final File agentFile;
    private String nativeMethodPrefix;
    // settings.ini
    private Map<String, Map<String, String>> configMap = Collections.emptyMap();

    public Environment(boolean attach) {
        this.attach = attach;
        URL jarURL = ClassPathUtil.getJarURL(Launcher.class);
        this.agentFile = new File(jarURL.getPath());
        this.baseDir = agentFile.getParentFile();

        debug = SystemUtil.getEnv("COMMON_AGENT_DEBUG", false);
        if (!debug) {
            debug = SystemUtil.getProperty("common.agent.debug", false);
        }

        // parse config
        File configFile = new File(baseDir,  "settings.ini");
        if (!configFile.exists() || !configFile.isFile()) {
            configFile = new File(baseDir, "settings.conf");
        }
        if (!configFile.exists() || !configFile.isFile()) {
            return;
        }

        try {
            configMap = IniUtil.parse(configFile, true);
        } catch (IOException e) {
            if (debug) {
                log.warn("fail to parse plugin config: " + e.getMessage());
            }
            return;
        }


        nativeMethodPrefix = configMap.getOrDefault("", Collections.emptyMap())
                .get("native-method-prefix");
    }
}
