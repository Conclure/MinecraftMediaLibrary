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

package com.github.pulsebeat02.minecraftmedialibrary.image.gif;

import com.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.extractor.FFmpegLocation;
import com.github.pulsebeat02.minecraftmedialibrary.frame.dither.DitherSetting;
import com.github.pulsebeat02.minecraftmedialibrary.frame.map.MapDataCallback;
import com.github.pulsebeat02.minecraftmedialibrary.frame.map.MapIntegratedPlayer;
import com.github.pulsebeat02.minecraftmedialibrary.image.MapImageHolder;
import com.github.pulsebeat02.minecraftmedialibrary.image.basic.MinecraftStaticImage;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import com.github.pulsebeat02.minecraftmedialibrary.utility.FileUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.utility.VideoUtilities;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class MinecraftDynamicImage implements MapImageHolder, ConfigurationSerializable {

  private final MediaLibrary library;
  private final File image;
  private final int map;
  private final int height;
  private final int width;
  private MapIntegratedPlayer player;

  /**
   * Instantiates a new MinecraftDynamicImage.
   *
   * @param library the library
   * @param map the map
   * @param image the image
   * @param width the width
   * @param height the height
   */
  public MinecraftDynamicImage(
      @NotNull final MediaLibrary library,
      final int map,
      @NotNull final File image,
      final int width,
      final int height) {
    Preconditions.checkArgument(image.exists(), "Image does not exist!");
    this.library = library;
    this.map = map;
    this.image = image;
    this.width = width;
    this.height = height;
    Logger.info(
        String.format("Initialized Image at Map ID %d (Source: %s)", map, image.getAbsolutePath()));
  }

  /**
   * Instantiates a new MapImage.
   *
   * @param library the library
   * @param map the map
   * @param url the url
   * @param width the width
   * @param height the height
   */
  public MinecraftDynamicImage(
      @NotNull final MediaLibrary library,
      final int map,
      @NotNull final String url,
      final int width,
      final int height) {
    this.library = library;
    this.map = map;
    image = FileUtilities.downloadImageFile(url, library.getImageFolder());
    this.width = width;
    this.height = height;
    Logger.info(
        String.format("Initialized Image at Map ID %d (Source: %s)", map, image.getAbsolutePath()));
  }

  /**
   * Returns a new builder class to use.
   *
   * @return the builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Deserializes a map image.
   *
   * @param library the library
   * @param deserialize the deserialize
   * @return the map image
   */
  @NotNull
  public static MinecraftStaticImage deserialize(
      @NotNull final MediaLibrary library,
      @NotNull final Map<String, Object> deserialize) {
    return new MinecraftStaticImage(
        library,
        NumberConversions.toInt(deserialize.get("map")),
        new File(String.valueOf(deserialize.get("image"))),
        NumberConversions.toInt(deserialize.get("width")),
        NumberConversions.toInt(deserialize.get("height")));
  }

  /**
   * Resets a specific map id.
   *
   * @param library the library
   * @param id the id
   */
  public static void resetMap(@NotNull final MediaLibrary library, final int id) {
    library.getHandler().unregisterMap(id);
  }

  /** Draws the specific image on the map id. */
  @Override
  public void drawImage() {
    onDrawImage();
    try {
      final Dimension dims = VideoUtilities.getDimensions(image);
      final int w = (int) dims.getWidth();
      player =
          MapIntegratedPlayer.builder()
              .setUrl(image.getAbsolutePath())
              .setWidth(w)
              .setHeight((int) dims.getHeight())
              .setCallback(
                  MapDataCallback.builder()
                      .setViewers(null)
                      .setMap(map)
                      .setItemframeWidth(width)
                      .setItemframeHeight(height)
                      .setVideoWidth(w)
                      .setDelay(0)
                      .setDitherHolder(DitherSetting.FLOYD_STEINBERG_DITHER.getHolder())
                      .build(library))
              .build(library);
      player.setRepeat(true);
      player.start(Bukkit.getOnlinePlayers());
      Logger.info(
          String.format(
              "Drew Dynamic Image at Map ID %d (Source: %s)", map, image.getAbsolutePath()));
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /** Converts a Gif into an MPEG file. */
  private void convertGifIntoMpeg() {
    final FFmpegLocation ffmpegLocator = new FFmpegLocation();
    final String name = image.getName();
    final Encoder encoder = new Encoder(ffmpegLocator);
    final AudioAttributes audio = new AudioAttributes();
    audio.setVolume(0);
    final EncodingAttributes attrs = new EncodingAttributes();
    attrs.setInputFormat(FilenameUtils.getExtension(name));
    attrs.setOutputFormat("mp4");
    attrs.setAudioAttributes(audio);
    final File output =
        new File(library.getImageFolder().toFile(), FilenameUtils.getBaseName(name) + ".mp4");
    try {
      encoder.encode(new MultimediaObject(image, ffmpegLocator), output, attrs);
    } catch (final EncoderException e) {
      e.printStackTrace();
    }
  }

  /** Called when an image is being draw on a map. */
  @Override
  public void onDrawImage() {}

  /**
   * Serializes a MapImage.
   *
   * @return map of serialized values
   */
  @Override
  @NotNull
  public Map<String, Object> serialize() {
    return ImmutableMap.of(
        "type", "dynamic",
        "map", map,
        "image", image.getAbsolutePath(),
        "width", width,
        "height", height);
  }

  /**
   * Gets library.
   *
   * @return the library
   */
  public MediaLibrary getLibrary() {
    return library;
  }

  /**
   * Gets map.
   *
   * @return the map
   */
  public int getMap() {
    return map;
  }

  /**
   * Gets image.
   *
   * @return the image
   */
  public File getImage() {
    return image;
  }

  /**
   * Gets height.
   *
   * @return the height
   */
  public int getHeight() {
    return height;
  }

  /**
   * Gets width.
   *
   * @return the width
   */
  public int getWidth() {
    return width;
  }

  /** The type Builder. */
  public static class Builder {

    private int map;
    private File image;
    private int height;
    private int width;

    private Builder() {}

    /**
     * Sets map.
     *
     * @param map the map
     * @return the map
     */
    public Builder setMap(final int map) {
      this.map = map;
      return this;
    }

    /**
     * Sets image.
     *
     * @param image the image
     * @return the image
     */
    public Builder setImage(@NotNull final File image) {
      this.image = image;
      return this;
    }

    /**
     * Sets height.
     *
     * @param height the height
     * @return the height
     */
    public Builder setHeight(final int height) {
      this.height = height;
      return this;
    }

    /**
     * Sets width.
     *
     * @param width the width
     * @return the width
     */
    public Builder setWidth(final int width) {
      this.width = width;
      return this;
    }

    /**
     * Create image map map image.
     *
     * @param library the library
     * @return the map image
     */
    public MinecraftDynamicImage build(final MediaLibrary library) {
      return new MinecraftDynamicImage(library, map, image, width, height);
    }
  }
}
