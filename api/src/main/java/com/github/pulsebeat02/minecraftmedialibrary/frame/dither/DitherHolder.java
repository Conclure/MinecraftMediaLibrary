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

package com.github.pulsebeat02.minecraftmedialibrary.frame.dither;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * An interface that is useful for handling custom dithering algorithms. Used in
 * MinecraftMediaLibrary for many algorithms.
 */
public interface DitherHolder {

  /**
   * Dithers the buffer using the given width.
   *
   * @param buffer data for the image
   * @param width units for the image
   */
  void dither(final int[] buffer, final int width);

  /**
   * Dithers the buffer into the ByteBuffer.
   *
   * @param buffer data for the image
   * @param width units for the image
   * @return ByteBuffer buffer for dithered image
   */
  ByteBuffer ditherIntoMinecraft(final int[] buffer, final int width);

  /**
   * Gets the current dither setting.
   *
   * @return DitherSetting for the setting.
   */
  @NotNull
  DitherSetting getSetting();
}
