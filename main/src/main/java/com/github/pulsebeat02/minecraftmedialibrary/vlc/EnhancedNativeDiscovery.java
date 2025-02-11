/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package com.github.pulsebeat02.minecraftmedialibrary.vlc;

import com.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.annotation.LegacyApi;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.RuntimeUtilities;
import com.sun.jna.StringArray;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.binding.LibC;
import uk.co.caprica.vlcj.binding.internal.libvlc_instance_t;
import uk.co.caprica.vlcj.factory.discovery.strategy.NativeDiscoveryStrategy;
import uk.co.caprica.vlcj.support.version.LibVlcVersion;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

import static uk.co.caprica.vlcj.binding.LibVlc.libvlc_new;
import static uk.co.caprica.vlcj.binding.LibVlc.libvlc_release;

/**
 * A specialized NativeDiscoveryStrategy used to discover the VLC plugins folder (binaries) in the
 * VLC installation. Currently deprecated as NativeDiscovery is a better option to discover, and is
 * subject to removal soon as it is not being used anywhere at the moment. The class uses a
 * recursive search by using a stack and searching each of the folders, trying to find a result.
 * This isn't as reliable as NativeDiscovery's method, where it compares a matcher.
 */
@LegacyApi(since = "1.2.0")
@Deprecated
public class EnhancedNativeDiscovery implements NativeDiscoveryStrategy {

  private static final String VLC_PLUGIN_PATH;

  static {
    VLC_PLUGIN_PATH = "VLC_PLUGIN_PATH";
  }

  private final Path dir;
  private String path;

  /**
   * Instantiates an EnchancedNativeDiscovery.
   *
   * @param library instance
   */
  public EnhancedNativeDiscovery(@NotNull final MediaLibrary library) {
    this(library.getVlcFolder());
  }

  /**
   * Instantiates an EnchancedNativeDiscovery.
   *
   * @param dir directory
   */
  public EnhancedNativeDiscovery(@NotNull final Path dir) {
    this.dir = dir;
  }

  /** Returns whether the strategy is supported */
  @Override
  public boolean supported() {
    return true;
  }

  /**
   * Attempts to discover VLC installation downloaded from pre-compiled binaries.
   *
   * @return String discovered path, null if not found.
   */
  @Override
  public String discover() {
    final File fold = dir.toFile();
    if (!fold.exists()) {
      return null;
    }
    final File dependency = getVLCFile(fold);
    if (dependency == null) {
      return null;
    }
    final Queue<File> folders = new ArrayDeque<>();
    folders.add(dependency);
    while (!folders.isEmpty()) {
      final File f = folders.remove();
      if (f.isDirectory()) {
        if (f.getName().equals("plugins")) {
          path = f.getAbsolutePath();
          Logger.info(String.format("Found VLC plugins folder (%s)", path));
          loadLibrary();
          return path;
        }
        folders.addAll(Arrays.asList(f.listFiles()));
      }
    }
    Logger.error("Could NOT find VLC plugins folder. This is a fatal error!");
    return null;
  }

  /**
   * Tries to find VLC app/dependency within directory.
   *
   * @param dir directory
   * @return file
   */
  private File getVLCFile(@NotNull final File dir) {
    for (final File f : dir.listFiles()) {
      if (StringUtils.containsIgnoreCase(f.getName(), "VLC")) {
        return f;
      }
    }
    return null;
  }

  /**
   * Ran once path is found.
   *
   * @param s path
   * @return found
   */
  @Override
  public boolean onFound(final String s) {
    return true;
  }

  /**
   * Ran once plugin path is set.
   *
   * @param s path
   * @return found
   */
  @Override
  public boolean onSetPluginPath(final String s) {
    if (RuntimeUtilities.isWindows()) {
      return LibC.INSTANCE._putenv(String.format("%s=%s", VLC_PLUGIN_PATH, path)) == 0;
    }
    return LibC.INSTANCE.setenv(VLC_PLUGIN_PATH, path, 1) == 0;
  }

  protected boolean loadLibrary() {
    try {
      final libvlc_instance_t instance = libvlc_new(0, new StringArray(new String[0]));
      if (instance != null) {
        libvlc_release(instance);
        final LibVlcVersion version = new LibVlcVersion();
        if (version.isSupported()) {
          return true;
        }
      }
    } catch (final UnsatisfiedLinkError e) {
      System.err.println(e.getMessage());
    }
    return false;
  }

  /**
   * Gets the directory.
   *
   * @return dir directory
   */
  public Path getDir() {
    return dir;
  }

  /**
   * Gets the path.
   *
   * @return path path
   */
  public String getPath() {
    return path;
  }
}
