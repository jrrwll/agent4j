package org.dreamcat.common.agent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import lombok.RequiredArgsConstructor;

/**
 * @author Jerry Will
 * @version 2022-04-23
 */
@RequiredArgsConstructor
public class PluginClassLoader extends ClassLoader {

    private static final int BUFFER_SIZE = 4096; // 4k
    private final File file;

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes;
        try (JarFile jarFile = new JarFile(file)) {
            bytes = loadClassFromJar(jarFile, name);

        } catch (IOException e) {
            throw new ClassNotFoundException(name);
        }

        return defineClass(name, bytes, 0, bytes.length);
    }

    public byte[] loadClassFromJar(JarFile jarFile, String name)
            throws IOException, ClassNotFoundException {
        String classFile = name.replace('.', '/') + ".class";
        ZipEntry entry = jarFile.getEntry(classFile);
        if (entry == null) {
            throw new ClassNotFoundException(name);
        }
        try (InputStream in = jarFile.getInputStream(entry)) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buf = new byte[BUFFER_SIZE];
                int readSize;
                while ((readSize = in.read(buf)) != -1) {
                    out.write(buf, 0, readSize);
                }
                return out.toByteArray();
            }
        }
    }
}
