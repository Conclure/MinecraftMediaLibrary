[![Build Status](https://img.shields.io/circleci/build/github/MinecraftMediaLibrary/MinecraftMediaLibrary?style=for-the-badge)](https://app.circleci.com/pipelines/github/MinecraftMediaLibrary/MinecraftMediaLibrary)
[![Documentation Status](https://img.shields.io/readthedocs/minecraftmedialibrary-wiki/latest?style=for-the-badge)](https://minecraftmedialibrary-wiki.readthedocs.io/en/latest/)
[![Discord](https://img.shields.io/discord/817501569108017223?style=for-the-badge)](https://discord.gg/qVhhbCWQQV)
[![Language](https://img.shields.io/badge/Made%20with-Java-1f425f.svg?style=for-the-badge)](https://www.java.com/en/)
[![Lines of Code](https://img.shields.io/tokei/lines/github/MinecraftMediaLibrary/MinecraftMediaLibrary?style=for-the-badge)](https://github.com/MinecraftMediaLibrary/MinecraftMediaLibrary)

[![Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/PulseBeat02/MinecraftMediaLibrary)

<img src="https://i.imgur.com/48CJD9j.png" alt="drawing" width="1000"/>

![Swag](http://ForTheBadge.com/images/badges/built-with-swag.svg)
---

## Introduction
**MinecraftMediaLibrary** is a library written along the Spigot API and net.minecraft.server classes to provide helpful
and useful classes for other plugins to take advantage of. One of the most important features perhaps is its ability to
play **videos** on a Minecraft Spigot server. It uses a [very optimized dithering method](https://github.com/MinecraftMediaLibrary/MinecraftMediaLibrary/blob/fdf5d6ad1e936680dd4aa0f372aad065b4f3a28a/MinecraftMediaLibrary-API/src/main/java/com/github/pulsebeat02/minecraftmedialibrary/frame/dither/FilterLiteDither.java#L200) along side with
**VLC Media Player Integration** if necessary to parse the video even quicker. As a result, frames can reach up to **35** times per
second at times with very good quality on maps if necessary. In comparison, a *smooth* animation is one which is 24
frames only. Here is a demo of what it would look like:

[![Epic Video Playback](https://yt-embed.herokuapp.com/embed?v=9oIns_Kp_sk&t=30s)](https://www.youtube.com/watch?v=9oIns_Kp_sk&t=30s)

The plugin takes advantages of maps to handle its video playback. Currently, it uses maps with ids to
display the video.

## Installation Guide
Due to financial concerns, I am unable to provide a Maven central repository to host my artifacts. However, the library
is usable if you use Jitpack.

---

Jitpack Repository:

**Maven**
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```

**Gradle (Groovy)**
```groovy
repositories {
  maven { 
    url 'https://jitpack.io' 
  }
}
```

**Gradle (Kotlin)**
```kotlin
repositories {
  maven { 
    url ("https://jitpack.io")
  }
}
```
---

MinecraftMediaLibrary Dependency:

**Maven**
```xml
<dependency>
  <groupId>com.github.pulsebeat02</groupId>
  <artifactId>minecraftmedialibrary</artifactId>
  <version>master-SNAPSHOT</version>
</dependency>
```

**Gradle (Groovy)**
```groovy
dependencies {
  implementation 'com.github.minecraftmedialibrary:minecraftmedialibrary:master-SNAPSHOT'
}
```

**Gradle (Kotlin)**
```kotlin
dependencies {
  implementation ("com.github.minecraftmedialibrary:minecraftmedialibrary:master-SNAPSHOT")
}
```

---

Most of the code is documented which can be found [here](https://minecraftmedialibrary.github.io/MinecraftMediaLibrary/). For detailed documentation with explanations, visit the [readthedocs page](https://minecraftmedialibrary-wiki.readthedocs.io/en/latest/) (which is still in progress).

However, if you want examples, you can take a look at [this module](https://github.com/MinecraftMediaLibrary/MinecraftMediaLibrary/tree/master/DeluxeMediaPlugin/src/main/java/com/github/pulsebeat02/deluxemediaplugin) where I wrote comments for every piece of code in the plugin which used the library. It is the plugin I used that displayed the video.

---

## List of Tasks
A list of the tasks I need to do can be found [here](https://github.com/MinecraftMediaLibrary/MinecraftMediaLibrary/issues/3).
I am currently working on a ton of support for other operating systems. It currently only works on Windows, but hopefully I can get it to work on Linux systems soon.

---

## Contributors/Acknowledgements
- [BananaPuncher714](https://github.com/BananaPuncher714) for helping lead the spark for the project, as well as code the Floyd-Steinberg implementation in Java.
- [Jetp250](https://github.com/jetp250) for helping with the Floyd-Steinberg implementation.
- [Fefo6644](https://github.com/Fefo6644) for helping me with asynchronous tasks and implementation.
- [Conclure](https://github.com/Conclure) for helping me migrate from maven to gradle.
- [VLC Media Player](https://www.videolan.org/) for allowing this library to play videos efficiently using C/C++ binaries.
- [VLCJ](https://github.com/caprica/vlcj) for providing the Java bindings and support to use VLC in the scope of Java.
- And other libraries used to allow this to be possible.

**Please Note**
I am not afflicted with Minecraft or Mojang in any way. This is a reminder due to how I have Minecraft in the library's name.

---
