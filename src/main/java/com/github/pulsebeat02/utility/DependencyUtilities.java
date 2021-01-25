package com.github.pulsebeat02.utility;

import com.github.pulsebeat02.dependency.MavenDependency;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class DependencyUtilities {

    public static File downloadMavenDependency(@NotNull final MavenDependency dependency, @NotNull final String parent) {
        return downloadFile(dependency, getMavenCentralUrl(dependency), parent);
    }

    public static File downloadJitpackDependency(@NotNull final MavenDependency dependency, @NotNull final String parent) {
        return downloadFile(dependency, getJitpackUrl(dependency), parent);
    }

    public static String getMavenCentralUrl(@NotNull final MavenDependency dependency) {
        return getDependencyUrl(dependency, "https://repo1.maven.org/maven2/");
    }

    public static String getJitpackUrl(@NotNull final MavenDependency dependency) {
        return getDependencyUrl(dependency, "https://jitpack.io/");
    }

    public static String getDependencyUrl(@NotNull final MavenDependency dependency, @NotNull final String base) {
        return base +
                dependency.getGroup().replaceAll("\\.", "/") + "/" +
                dependency.getArtifact() + "/" +
                dependency.getVersion() + "/";
    }

    public static File downloadFile(@NotNull final MavenDependency dependency, @NotNull final String link, @NotNull final String parent) {
        String file = dependency.getArtifact() + "-" + dependency.getVersion() + ".jar";
        String url = link + file;
        Path path = Paths.get(parent + "/" + file);
        try (final ReadableByteChannel byteChannel = Channels.newChannel(new URL(url).openStream())) {
            if (Files.notExists(path)) {
                Files.createFile(path);
            }
            FileChannel.open(path).transferFrom(byteChannel, 0L, Long.MAX_VALUE);
        } catch (final IOException | NonWritableChannelException e) {
            return null;
        }
        return new File(path.toString());
    }

    public static void loadDependency(@NotNull final File file) throws IOException {
        String jarPath = file.getAbsolutePath();
        System.out.println(jarPath);
        JarFile jarFile = new JarFile(jarPath);
        Enumeration<JarEntry> e = jarFile.entries();
        URL[] urls = {new URL("jar:file:" + jarPath + "!/")};
        URLClassLoader cl = URLClassLoader.newInstance(urls);
        while (e.hasMoreElements()) {
            JarEntry je = e.nextElement();
            if (je.isDirectory() || !je.getName().endsWith(".class")) {
                continue;
            }
            String className = je.getName().substring(0, je.getName().length() - 6);
            className = className.replace('/', '.');
            try {
                cl.loadClass(className);
            } catch (ClassNotFoundException classNotFoundException) {
                classNotFoundException.printStackTrace();
            }
        }
    }

}