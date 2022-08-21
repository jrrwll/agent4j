package org.dreamcat.common.agent;

import com.sun.tools.attach.VirtualMachine;
import java.io.File;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import jdk.jfr.events.ExceptionThrownEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dreamcat.common.agent.util.InstrumentationUtil;
import org.dreamcat.common.io.ClassPathUtil;
import org.dreamcat.common.io.FileUtil;
import org.dreamcat.common.io.IOUtil;
import org.dreamcat.common.text.InterpolationUtil;
import org.dreamcat.common.util.ReflectUtil;

/**
 * @author Jerry Will
 * @version 2022-04-23
 */
@Slf4j
public class Launcher {

    private static final String arg_attach = "--attach";
    private static final String version = "0.2.1";

    private Launcher() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length <= 1 || !args[0].equals(arg_attach)) {
            String usage = IOUtil.readAsString(Launcher.class.getResourceAsStream("usage.txt"));
            System.out.println(InterpolationUtil.format(usage,
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

    @SneakyThrows
    private static void config(Instrumentation inst, boolean attach) {
        ReflectUtil.setFieldValue(null, InstrumentationUtil.class, "inst", inst);
        Environment env = new Environment(attach);

        ChainTransformer transformer = new ChainTransformer();
        List<PostConfigurationProcessor> processors = new ArrayList<>();
        PluginManager pluginManager = new PluginManager(inst, env, transformer, processors);
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

        for (PostConfigurationProcessor processor : processors) {
            try {
                processor.process(inst);
            } catch (Throwable e) {
                String processorName = processor.getProcessorName();
                log.error("error occurred when process post configuration by " +
                        processorName + ", message: " + e.getMessage(), e);
            }
        }
    }

    @SneakyThrows
    private static void listen(Instrumentation inst) {
        File classesDir = new File("./build/classes/java/main")
                .getCanonicalFile();
        System.out.println("listened on " + classesDir);

        Path dir = classesDir.toPath();

        FileUtil.listenOnModify(dir, 300, path -> {
            System.out.println("modify " + path);
            reload(inst, dir, path);
            return true;
        });
    }

    @SneakyThrows
    private static void reload(
            Instrumentation inst, Path base, Path path) {
        String fileName = path.toFile().getName();
        String className = fileName
                .replace(File.pathSeparatorChar, '.');
        className = className.substring(0, className.length() - 6); // remove .class
        System.out.println("reloading class " + className);

        Class<?> c = Class.forName(className);
        Path absolutePath = new File(base.toFile(), fileName).toPath();
        byte[] cf = Files.readAllBytes(absolutePath);

        ClassDefinition cd = new ClassDefinition(c, cf);
        inst.redefineClasses(cd);
        System.out.println("reloaded " + c);
    }
}
