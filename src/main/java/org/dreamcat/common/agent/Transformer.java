package org.dreamcat.common.agent;

import java.security.ProtectionDomain;
import java.util.Set;

/**
 * @author Jerry Will
 * @version 2022-04-23
 */
public interface Transformer {

    default boolean isGlobal() {
        return false;
    }

    // which class will be transformed
    String getAcceptClassName();

    byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer);

    default byte[] before(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        return classfileBuffer;
    }

    default byte[] after(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        return classfileBuffer;
    }
}
