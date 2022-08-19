package org.dreamcat.common.agent.plugin.dns_block;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
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
 * @version 2022-04-23
 */
@Slf4j
@RequiredArgsConstructor
public class InetAddressTransformer implements Transformer {

    private final PluginEntry entry;

    @Override
    public String getAcceptClassName() {
        return "java.net.InetAddress";
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
     * @see InetAddress#getAllByName(String, InetAddress)
     * @see InetAddress#isReachable(NetworkInterface, int, int)
     */
    private byte[] transform0(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws Exception {
        MakeClass mc = MakeClass.makeIfNew(classfileBuffer);
        mc.setMethodInsertBefore("getAllByName",
                new String[]{"java.lang.String", "java.net.InetAddress"},
                String.format("{ $1 = %s.filterHost($1); if($1==null) return null; }",
                        getClass().getName()));
        return mc.toBytecode();
    }

    public static String filterHost(String host) throws UnknownHostException {
        if (host == null) {
            return null;
        }
        if (ObjectUtil.isEmpty(DnsBlockPluginEntry.configMap)) {
            return host;
        }
        Set<String> hostPatterns = DnsBlockPluginEntry.configMap.keySet();
        for (String hostPattern : hostPatterns) {
            if (host.matches(hostPattern)) {
                throw new UnknownHostException("["+host+"]");
            }
        }
        return host;
    }
}
