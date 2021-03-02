/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/11/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.dependency;

import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.relocation.JarRelocator;
import com.github.pulsebeat02.minecraftmedialibrary.relocation.Relocation;
import com.github.pulsebeat02.minecraftmedialibrary.utility.DependencyUtilities;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DependencyManagement {

  private final String path;
  private final Set<File> files;
  private final File dir;

  /** Instantiates a new DependencyManagement. */
  public DependencyManagement() {
    path = System.getProperty("user.dir");
    files = new HashSet<>();
    dir = new File(path + "/mml_libs");
  }

  /** Installs all libraries from links. */
  public void install() {
    if (!dir.exists()) {
      if (dir.mkdir()) {
        Logger.info(
            "Dependency Directory ("
                + dir.getAbsolutePath()
                + ") does not exist... Creating a folder");
      } else {
        Logger.info("Dependency Directory (" + dir.getAbsolutePath() + ") exists!");
      }
    }
    for (final RepositoryDependency dependency : RepositoryDependency.values()) {
      if (!checkExists(dir, dependency)) {
        File file = null;
        final String artifact = dependency.getArtifact();
        try {
          Logger.info("Checking Maven Central Repository for " + artifact);
          file = DependencyUtilities.downloadMavenDependency(dependency, path + "/mml_libs");
        } catch (final IOException e) {
          try {
            Logger.info(
                "Could not find in the Maven Central Repository... Checking Jitpack Central Repository for "
                    + artifact);
            file = DependencyUtilities.downloadJitpackDependency(dependency, path + "/mml_libs");
          } catch (final IOException exception) {
            Logger.error(
                "Could not find " + artifact + " in the Maven Central Repository or Jitpack");
            exception.printStackTrace();
          }
        }
        files.add(file);
      }
    }
  }

  /** Relocates Dependencies. */
  public void relocate() {
    for (final File f : dir.listFiles()) {
      if (f.getName().contains("asm")) {
        try {
          DependencyUtilities.loadDependency(f);
          files.remove(f);
        } catch (final IOException e) {
          e.printStackTrace();
        }
      }
    }
    final List<Relocation> relocations =
        Arrays.stream(JarRelocationConvention.values())
            .map(JarRelocationConvention::getRelocation)
            .collect(Collectors.toList());
    for (final File f : files) {
      final JarRelocator relocator = new JarRelocator(f, f, relocations);
      try {
        relocator.run();
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  /** Install and load. */
  public void load() {
    for (final File f : files) {
      try {
        DependencyUtilities.loadDependency(f);
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Check if dependency exists in the directory beforehand.
   *
   * @param dir the directory
   * @param dependency the dependency
   * @return the boolean
   */
  public boolean checkExists(
      @NotNull final File dir, @NotNull final RepositoryDependency dependency) {
    for (final File f : dir.listFiles()) {
      if (f.getName().contains(dependency.getArtifact())) {
        return true;
      }
    }
    return false;
  }
}
