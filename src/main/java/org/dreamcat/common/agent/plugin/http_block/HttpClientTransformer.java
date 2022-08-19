package org.dreamcat.common.agent.plugin.http_block;

import java.net.SocketTimeoutException;
import java.security.ProtectionDomain;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.agent.PluginEntry;
import org.dreamcat.common.agent.Transformer;
import org.dreamcat.common.util.ObjectUtil;
import org.dreamcat.common.x.asm.MakeClass;

/**
 * @author Jerry Will
 * @version 2022-04-25
 */
@Slf4j
@RequiredArgsConstructor
public class HttpClientTransformer implements Transformer {

    private final PluginEntry entry;

    @Override
    public String getAcceptClassName() {
        return "sun.net.www.http.HttpClient";
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        try {
            return transform0(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
        } catch (Exception e) {
            log.error(String.format("[Plugin: %s] error: %s", entry.getName(), e.getMessage()), e);
            return classfileBuffer;
        }
    }

    /**
     * @see sun.net.www.http.HttpClient#openServer()
     */
    private byte[] transform0(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws Exception {
        MakeClass mc = MakeClass.makeIfNew(classfileBuffer);
        mc.setMethodInsertBefore("getAllByName",
                new String[]{"java.lang.String", "java.net.InetAddress"},
                String.format("{ %s.block(url); }",
                        getClass().getName()));
        return mc.toBytecode();
    }

    public static void block(String url) throws SocketTimeoutException {
        if (url == null) return;
        if (ObjectUtil.isEmpty(HttpBlockPluginEntry.configMap)) {
            return;
        }
        Set<String> patterns = HttpBlockPluginEntry.configMap.keySet();
        for (String pattern : patterns) {
            if (url.matches(pattern)) {
                throw new SocketTimeoutException("connect timed out");
            }
        }
    }
}
