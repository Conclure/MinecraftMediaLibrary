package com.github.pulsebeat02.minecraftmedialibrary;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A simple class to handle all the paths the library uses to store configuration files, audio
 * files, video files, and other necessities used throughout the library.
 */
public class LibraryPathHandle {

  private final Path parentFolder;
  private final Path httpParentFolder;
  private final Path dependenciesFolder;
  private final Path vlcFolder;
  private final Path imageFolder;
  private final Path audioFolder;

  /**
   * Instantiates a new LibraryPathHandle.
   *
   * @param plugin the plugin
   * @param http the http directory
   * @param libraryPath the library path
   * @param vlcPath the vlc path
   * @param imagePath the image path
   * @param audioPath the audio path
   */
  public LibraryPathHandle(
      @NotNull final Plugin plugin,
      @Nullable final String http,
      @Nullable final String libraryPath,
      @Nullable final String vlcPath,
      @Nullable final String imagePath,
      @Nullable final String audioPath) {
    final String path = String.format("%s/mml", plugin.getDataFolder().getAbsolutePath());
    parentFolder = Paths.get(path);
    httpParentFolder = Paths.get(http == null ? String.format("%s/http/", path) : http);
    imageFolder = Paths.get(imagePath == null ? String.format("%s/image/", path) : imagePath);
    audioFolder = Paths.get(audioPath == null ? String.format("%s/audio/", path) : audioPath);
    dependenciesFolder =
        Paths.get(libraryPath == null ? String.format("%s/libraries/", path) : libraryPath);
    vlcFolder = Paths.get(vlcPath == null ? String.format("%s/vlc/", path) : vlcPath);
    try {
      Files.createDirectories(parentFolder);
      Files.createDirectories(httpParentFolder);
      Files.createDirectories(imageFolder);
      Files.createDirectories(audioFolder);
      Files.createDirectories(dependenciesFolder);
      Files.createDirectories(vlcFolder);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Gets the parent folder of the library.
   *
   * @return the path of the library
   */
  public Path getParentFolder() {
    return parentFolder;
  }

  /**
   * Gets the http parent folder.
   *
   * @return the parent
   */
  public Path getHttpParentFolder() {
    return httpParentFolder;
  }

  /**
   * Gets dependencies folder.
   *
   * @return the dependencies folder
   */
  public Path getDependenciesFolder() {
    return dependenciesFolder;
  }

  /**
   * Gets the vlc folder.
   *
   * @return the vlc folder
   */
  public Path getVlcFolder() {
    return vlcFolder;
  }

  /**
   * Gets the image folder of the library.
   *
   * @return the path of the image folder
   */
  public Path getImageFolder() {
    return imageFolder;
  }

  /**
   * Gets the audio folder of the library.
   *
   * @return the path of the audio folder
   */
  public Path getAudioFolder() {
    return audioFolder;
  }
}
