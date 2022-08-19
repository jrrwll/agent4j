package org.dreamcat.common.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jerry Will
 * @version 2022-04-23
 */
@Slf4j
@RequiredArgsConstructor
public class ChainTransformer implements ClassFileTransformer {

    private final List<Transformer> transformers = new ArrayList<>();
    private final Map<String, List<Transformer>> acceptTransformers = new HashMap<>();
    @Getter
    private final Set<String> acceptClassNames = new HashSet<>();

    public void addTransformers(List<Transformer> transformers) {
        for (Transformer transformer : transformers) {
            if (transformer.isGlobal()) {
                this.transformers.add(transformer);
            } else {
                String acceptClassName =  transformer.getAcceptClassName();
                acceptTransformers.computeIfAbsent(acceptClassName, k -> new ArrayList<>())
                        .add(transformer);
                acceptClassNames.add(acceptClassName);
            }
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className == null) return classfileBuffer;
        for (Transformer transformer : transformers) {
            classfileBuffer = transformer.before(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
        }
        List<Transformer> list = acceptTransformers.get(className);
        if (list != null) {
            for (Transformer transformer : list) {
                classfileBuffer = transformer.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
            }
        }

        for (Transformer transformer : transformers) {
            classfileBuffer = transformer.after(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
        }
        return classfileBuffer;
    }

}
