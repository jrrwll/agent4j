package org.dreamcat.common.agent;

import com.sun.tools.attach.VirtualMachine;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.agent.util.InstrumentationUtil;
import org.dreamcat.common.io.ClassPathUtil;
import org.dreamcat.common.io.IOUtil;
import org.dreamcat.common.text.DollarInterpolation;

/**
 * @author Jerry Will
 * @version 2022-04-23
 */
@Slf4j
public class Launcher {

    private static final String arg_attach = "--attach";
    private static final String version = "0.2.1";

    private Launcher(){}

    public static void main(String[] args) throws Exception {
        if (args.length <= 1 || !args[0].equals(arg_attach)) {
            String usage = IOUtil.readAsString(Launcher.class.getResourceAsStream("usage.txt"));
            System.out.println(DollarInterpolation.format(usage,
                    Collections.singletonMap("version", version)));
            System.exit(1);
        }

        String pid = args[1];
        String extraArgs = args.length > 2 ? args[2] : null;
        String agentPath = ClassPathUtil.getJarURL(Launcher.class).getPath();

        VirtualMachine vm = VirtualMachine.attach(pid);
        vm.loadAgent(agentPath, extraArgs);
        vm.detach();
        log.info("attached to {} successfully", pid);
    }

    // injected by JVM
    public static void agentmain(String args, Instrumentation inst) {
        config(inst, true);
    }

    // injected by JVM
    public static void premain(String args, Instrumentation inst) {
        config(inst, false);
    }

    private static void config(Instrumentation inst, boolean attach) {
        setInstrumentationUtil(inst);
        Environment env = new Environment(attach);

        ChainTransformer transformer = new ChainTransformer();
        PluginManager pluginManager = new PluginManager(inst, env, transformer);
        pluginManager.loadPlugins();

        inst.addTransformer(transformer);
        String nativeMethodPrefix = env.getNativeMethodPrefix();
        if (nativeMethodPrefix != null) {
            inst.setNativeMethodPrefix(transformer, nativeMethodPrefix);
        }

        Class<?>[] classes = inst.getAllLoadedClasses();
        Set<String> acceptClassNames = transformer.getAcceptClassNames();
        for (Class<?> clazz : classes) {
            if (acceptClassNames.contains(clazz.getName())) {
                try {
                    inst.retransformClasses(clazz);
                } catch (Exception e) {
                    log.error("fail to retransform " + clazz + ": " + e.getMessage(), e);
                }
            }
        }
    }

    private static void setInstrumentationUtil(Instrumentation inst) {
        try {
            Field field = InstrumentationUtil.class.getDeclaredField("inst");
            field.setAccessible(true);
            field.set(null, inst);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
