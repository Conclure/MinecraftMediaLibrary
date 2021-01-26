package com.github.pulsebeat02.utility;

import com.github.pulsebeat02.logger.Logger;
import com.github.pulsebeat02.dependency.MavenDependency;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class DependencyUtilities {

    public static File downloadMavenDependency(@NotNull final MavenDependency dependency, @NotNull final String parent) throws IOException {
        return downloadFile(dependency, getMavenCentralUrl(dependency), parent);
    }

    public static File downloadJitpackDependency(@NotNull final MavenDependency dependency, @NotNull final String parent) throws IOException {
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

    public static File downloadFile(@NotNull final MavenDependency dependency, @NotNull final String link, @NotNull final String parent) throws IOException {
        String file = dependency.getArtifact() + "-" + dependency.getVersion() + ".jar";
        String url = link + file;
        Path path = Paths.get(parent + "/" + file);
        Logger.info("Downloading Dependency at " + url + " into folder " + path);
        BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(String.valueOf(path));
        byte[] dataBuffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
            fileOutputStream.write(dataBuffer, 0, bytesRead);
        }
        return new File(path.toString());
    }

    public static void loadDependency(@NotNull final File file) throws IOException {
        String jarPath = file.getAbsolutePath();
        JarFile jarFile = new JarFile(jarPath);
        Logger.info("Loading JAR Dependency at: " + jarPath);
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
                Logger.info("Loaded " + className);
                cl.loadClass(className);
            } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                Logger.error("Could NOT Load " + className);
            }
        }
        Logger.info("Finished Loading Dependency " + file.getName());
    }

}
