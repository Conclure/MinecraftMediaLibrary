/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 3/22/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.os;

import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.dependency.task.CommandTask;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.RuntimeUtilities;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class MacSilentInstallation extends SilentOSDependentSolution {

  public MacSilentInstallation(@NotNull final MinecraftMediaLibrary library) {
    super(library);
  }

  public MacSilentInstallation(@NotNull final String dir) {
    super(dir);
  }

  @Override
  public boolean checkVLCExistance(@NotNull final String dir) {
    final File folder = findVLCFolder(new File(dir));
    if (folder == null || !folder.exists()) {
      return false;
    }
    loadNativeDependency(folder);
    return getNativeDiscovery().discover();
  }

  @Override
  public void downloadVLCLibrary() throws IOException {
    final String dir = getDir();
    if (checkVLCExistance(dir)) {
      Logger.info("Found VLC Library in Mac! No need to install into path.");
    } else {
      final File dmg = new File(dir, "VLC.dmg");
      final File diskPath = new File("/Volumes/VLC media player");
      FileUtils.copyURLToFile(new URL(RuntimeUtilities.getURL()), dmg);
      try {
        if (mountDiskImage(dmg) != 0) {
          throw new IOException("Could not Mount Disk File!");
        }
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
      final File app = new File(diskPath, "/Applications/VLC.app");
      FileUtils.copyDirectory(new File(diskPath, "VLC.app"), app);
      try {
        changePermissions(app.getAbsolutePath());
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
      Logger.info("Moved File!");
      try {
        if (unmountDiskImage(diskPath.getAbsolutePath()) != 0) {
          throw new IOException("Could not Unmount Disk File!");
        }
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
      Logger.info("Unmounting Disk Successfully");
      deleteArchive(dmg);
      Logger.info("Deleted DMG File");
    }
    loadNativeDependency(null);
  }

  /**
   * Mounts disk image from file.
   *
   * @param dmg disk image
   * @return result code
   * @throws IOException if dmg cannot be found
   * @throws InterruptedException waiting for process
   */
  private int mountDiskImage(@NotNull final File dmg) throws IOException, InterruptedException {
    final String[] command = {"/usr/bin/hdiutil", "attach", dmg.getAbsolutePath()};
    final CommandTask t = new CommandTask(command, true);
    Logger.info("============= DMG INFORMATION =============");
    Logger.info(t.getResult());
    Logger.info("===========================================");
    return t.getProcess().waitFor();
  }

  /**
   * Unmounts disk image from file.
   *
   * @param path disk path
   * @return result code
   * @throws IOException if path cannot be found
   * @throws InterruptedException waiting for process
   */
  private int unmountDiskImage(@NotNull final String path)
      throws IOException, InterruptedException {
    final String[] command = {"diskutil", "unmount", path};
    final CommandTask t = new CommandTask(command, true);
    Logger.info("=========== UNMOUNT INFORMATION ===========");
    Logger.info(t.getResult());
    Logger.info("===========================================");
    return t.getProcess().waitFor();
  }

  /**
   * Changes permission of app file.
   *
   * @param path path
   * @throws IOException if path couldn't be found
   * @throws InterruptedException waiting for process
   */
  private void changePermissions(@NotNull final String path)
      throws IOException, InterruptedException {
    final String[] command = {"chmod", "-R", "755", path};
    new CommandTask(command, true).getProcess().waitFor();
  }

  @Override
  public void loadNativeDependency(@Nullable final File folder) {
    getNativeDiscovery().discover();
  }
}
