package com.github.pulsebeat02.minecraftmedialibrary.video.dither;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class FloydImageDither implements AbstractDitherHolder {

    private static final int[] PALETTE;
    private static final byte[] COLOR_MAP;
    private static final int[] FULL_COLOR_MAP;
    /**
     * What a piece of optimization;
     * Performs incredibly fast Minecraft color conversion and dithering.
     *
     * @author jetp250, BananaPuncher714
     */
    private static final int largest = 0;

    static {
        PALETTE = StaticDitherInitialization.PALETTE;
        COLOR_MAP = StaticDitherInitialization.COLOR_MAP;
        FULL_COLOR_MAP = StaticDitherInitialization.FULL_COLOR_MAP;
    }

    public static int getLargest() {
        return largest;
    }

    public static int[] getPALETTE() {
        return PALETTE;
    }

    public static byte[] getColorMap() {
        return COLOR_MAP;
    }

    public static int[] getFullColorMap() {
        return FULL_COLOR_MAP;
    }

    public int getLargestColorVal() {
        return largest;
    }

    public int getColorFromMinecraftPalette(final byte val) {
        return PALETTE[(val + 256) % 256];
    }

    public byte getBestColorIncludingTransparent(final int rgb) {
        return (rgb >>> 24 & 0xFF) == 0 ? 0 : getBestColor(rgb);
    }

    public byte getBestColor(final int rgb) {
        return COLOR_MAP[(rgb >> 16 & 0xFF) >> 1 << 14 | (rgb >> 8 & 0xFF) >> 1 << 7 | (rgb & 0xFF) >> 1];
    }

    public byte getBestColor(final int red, final int green, final int blue) {
        return COLOR_MAP[red >> 1 << 14 | green >> 1 << 7 | blue >> 1];
    }

    public int getBestFullColor(final int red, final int green, final int blue) {
        return FULL_COLOR_MAP[red >> 1 << 14 | green >> 1 << 7 | blue >> 1];
    }

    public byte[] simplify(final @NotNull int[] buffer) {
        final byte[] map = new byte[buffer.length];
        for (int index = 0; index < buffer.length; index++) {
            final int rgb = buffer[index];
            final int red = rgb >> 16 & 0xFF;
            final int green = rgb >> 8 & 0xFF;
            final int blue = rgb & 0xFF;
            final byte ptr = getBestColor(red, green, blue);
            map[index] = ptr;
        }
        return map;
    }

    @Override
    public void dither(final int[] buffer, final int width) {
        final int height = buffer.length / width;
        final int widthMinus = width - 1;
        final int heightMinus = height - 1;
        final int[][] dither_buffer = new int[2][width + width << 1];
        for (int y = 0; y < height; y++) {
            final boolean hasNextY = y < heightMinus;
            final int yIndex = y * width;
            if ((y & 0x1) == 0) {
                int bufferIndex = 0;
                final int[] buf1 = dither_buffer[0];
                final int[] buf2 = dither_buffer[1];
                for (int x = 0; x < width; x++) {
                    final boolean hasPrevX = x > 0;
                    final boolean hasNextX = x < widthMinus;
                    final int index = yIndex + x;
                    final int rgb = buffer[index];
                    int red = rgb >> 16 & 0xFF;
                    int green = rgb >> 8 & 0xFF;
                    int blue = rgb & 0xFF;
                    red = (red += buf1[bufferIndex++]) > 255 ? 255 : red < 0 ? 0 : red;
                    green = (green += buf1[bufferIndex++]) > 255 ? 255 : green < 0 ? 0 : green;
                    blue = (blue += buf1[bufferIndex++]) > 255 ? 255 : blue < 0 ? 0 : blue;
                    final int closest = getBestFullColor(red, green, blue);
                    final int delta_r = red - (closest >> 16 & 0xFF);
                    final int delta_g = green - (closest >> 8 & 0xFF);
                    final int delta_b = blue - (closest & 0xFF);
                    if (hasNextX) {
                        buf1[bufferIndex] = (int) (0.4375 * delta_r);
                        buf1[bufferIndex + 1] = (int) (0.4375 * delta_g);
                        buf1[bufferIndex + 2] = (int) (0.4375 * delta_b);
                    }
                    if (hasNextY) {
                        if (hasPrevX) {
                            buf2[bufferIndex - 6] = (int) (0.1875 * delta_r);
                            buf2[bufferIndex - 5] = (int) (0.1875 * delta_g);
                            buf2[bufferIndex - 4] = (int) (0.1875 * delta_b);
                        }
                        buf2[bufferIndex - 3] = (int) (0.3125 * delta_r);
                        buf2[bufferIndex - 2] = (int) (0.3125 * delta_g);
                        buf2[bufferIndex - 1] = (int) (0.3125 * delta_b);
                        if (hasNextX) {
                            buf2[bufferIndex] = (int) (0.0625 * delta_r);
                            buf2[bufferIndex + 1] = (int) (0.0625 * delta_g);
                            buf2[bufferIndex + 2] = (int) (0.0625 * delta_b);
                        }
                    }
                    buffer[index] = closest;
                }
            } else {
                int bufferIndex = width + (width << 1) - 1;
                final int[] buf1 = dither_buffer[1];
                final int[] buf2 = dither_buffer[0];
                for (int x = width - 1; x >= 0; x--) {
                    final boolean hasPrevX = x < widthMinus;
                    final boolean hasNextX = x > 0;
                    final int index = yIndex + x;
                    final int rgb = buffer[index];
                    int red = rgb >> 16 & 0xFF;
                    int green = rgb >> 8 & 0xFF;
                    int blue = rgb & 0xFF;
                    blue = (blue += buf1[bufferIndex--]) > 255 ? 255 : blue < 0 ? 0 : blue;
                    green = (green += buf1[bufferIndex--]) > 255 ? 255 : green < 0 ? 0 : green;
                    red = (red += buf1[bufferIndex--]) > 255 ? 255 : red < 0 ? 0 : red;
                    final int closest = getBestFullColor(red, green, blue);
                    final int delta_r = red - (closest >> 16 & 0xFF);
                    final int delta_g = green - (closest >> 8 & 0xFF);
                    final int delta_b = blue - (closest & 0xFF);
                    if (hasNextX) {
                        buf1[bufferIndex] = (int) (0.4375 * delta_b);
                        buf1[bufferIndex - 1] = (int) (0.4375 * delta_g);
                        buf1[bufferIndex - 2] = (int) (0.4375 * delta_r);
                    }
                    if (hasNextY) {
                        if (hasPrevX) {
                            buf2[bufferIndex + 6] = (int) (0.1875 * delta_b);
                            buf2[bufferIndex + 5] = (int) (0.1875 * delta_g);
                            buf2[bufferIndex + 4] = (int) (0.1875 * delta_r);
                        }
                        buf2[bufferIndex + 3] = (int) (0.3125 * delta_b);
                        buf2[bufferIndex + 2] = (int) (0.3125 * delta_g);
                        buf2[bufferIndex + 1] = (int) (0.3125 * delta_r);
                        if (hasNextX) {
                            buf2[bufferIndex] = (int) (0.0625 * delta_b);
                            buf2[bufferIndex - 1] = (int) (0.0625 * delta_g);
                            buf2[bufferIndex - 2] = (int) (0.0625 * delta_r);
                        }
                    }
                    buffer[index] = closest;
                }
            }
        }
    }

    @Override
    public ByteBuffer ditherIntoMinecraft(final int[] buffer, final int width) {
        final int height = buffer.length / width;
        final int widthMinus = width - 1;
        final int heightMinus = height - 1;
        final int[][] dither_buffer = new int[2][width + width << 1];
        final ByteBuffer data = ByteBuffer.allocate(buffer.length);
        for (int y = 0; y < height; y++) {
            final boolean hasNextY = y < heightMinus;
            final int yIndex = y * width;
            if ((y & 0x1) == 0) {
                int bufferIndex = 0;
                final int[] buf1 = dither_buffer[0];
                final int[] buf2 = dither_buffer[1];
                for (int x = 0; x < width; x++) {
                    final boolean hasPrevX = x > 0;
                    final boolean hasNextX = x < widthMinus;
                    final int index = yIndex + x;
                    final int rgb = buffer[index];
                    int red = rgb >> 16 & 0xFF;
                    int green = rgb >> 8 & 0xFF;
                    int blue = rgb & 0xFF;
                    red = (red += buf1[bufferIndex++]) > 255 ? 255 : red < 0 ? 0 : red;
                    green = (green += buf1[bufferIndex++]) > 255 ? 255 : green < 0 ? 0 : green;
                    blue = (blue += buf1[bufferIndex++]) > 255 ? 255 : blue < 0 ? 0 : blue;
                    final int closest = getBestFullColor(red, green, blue);
                    final int delta_r = red - (closest >> 16 & 0xFF);
                    final int delta_g = green - (closest >> 8 & 0xFF);
                    final int delta_b = blue - (closest & 0xFF);
                    if (hasNextX) {
                        buf1[bufferIndex] = (int) (0.4375 * delta_r);
                        buf1[bufferIndex + 1] = (int) (0.4375 * delta_g);
                        buf1[bufferIndex + 2] = (int) (0.4375 * delta_b);
                    }
                    if (hasNextY) {
                        if (hasPrevX) {
                            buf2[bufferIndex - 6] = (int) (0.1875 * delta_r);
                            buf2[bufferIndex - 5] = (int) (0.1875 * delta_g);
                            buf2[bufferIndex - 4] = (int) (0.1875 * delta_b);
                        }
                        buf2[bufferIndex - 3] = (int) (0.3125 * delta_r);
                        buf2[bufferIndex - 2] = (int) (0.3125 * delta_g);
                        buf2[bufferIndex - 1] = (int) (0.3125 * delta_b);
                        if (hasNextX) {
                            buf2[bufferIndex] = (int) (0.0625 * delta_r);
                            buf2[bufferIndex + 1] = (int) (0.0625 * delta_g);
                            buf2[bufferIndex + 2] = (int) (0.0625 * delta_b);
                        }
                    }
                    data.put(index, getBestColor(closest));
                }
            } else {
                int bufferIndex = width + (width << 1) - 1;
                final int[] buf1 = dither_buffer[1];
                final int[] buf2 = dither_buffer[0];
                for (int x = width - 1; x >= 0; x--) {
                    final boolean hasPrevX = x < widthMinus;
                    final boolean hasNextX = x > 0;
                    final int index = yIndex + x;
                    final int rgb = buffer[index];
                    int red = rgb >> 16 & 0xFF;
                    int green = rgb >> 8 & 0xFF;
                    int blue = rgb & 0xFF;
                    blue = (blue += buf1[bufferIndex--]) > 255 ? 255 : blue < 0 ? 0 : blue;
                    green = (green += buf1[bufferIndex--]) > 255 ? 255 : green < 0 ? 0 : green;
                    red = (red += buf1[bufferIndex--]) > 255 ? 255 : red < 0 ? 0 : red;
                    final int closest = getBestFullColor(red, green, blue);
                    final int delta_r = red - (closest >> 16 & 0xFF);
                    final int delta_g = green - (closest >> 8 & 0xFF);
                    final int delta_b = blue - (closest & 0xFF);
                    if (hasNextX) {
                        buf1[bufferIndex] = (int) (0.4375 * delta_b);
                        buf1[bufferIndex - 1] = (int) (0.4375 * delta_g);
                        buf1[bufferIndex - 2] = (int) (0.4375 * delta_r);
                    }
                    if (hasNextY) {
                        if (hasPrevX) {
                            buf2[bufferIndex + 6] = (int) (0.1875 * delta_b);
                            buf2[bufferIndex + 5] = (int) (0.1875 * delta_g);
                            buf2[bufferIndex + 4] = (int) (0.1875 * delta_r);
                        }
                        buf2[bufferIndex + 3] = (int) (0.3125 * delta_b);
                        buf2[bufferIndex + 2] = (int) (0.3125 * delta_g);
                        buf2[bufferIndex + 1] = (int) (0.3125 * delta_r);
                        if (hasNextX) {
                            buf2[bufferIndex] = (int) (0.0625 * delta_b);
                            buf2[bufferIndex - 1] = (int) (0.0625 * delta_g);
                            buf2[bufferIndex - 2] = (int) (0.0625 * delta_r);
                        }
                    }
                    data.put(index, getBestColor(closest));
                }
            }
        }
        return data;
    }

    @Override
    public DitherSetting getSetting() {
        return DitherSetting.FLOYD_STEINBERG_DITHER;
    }

    public BufferedImage toBufferedImage(final @NotNull Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        final BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        final Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        return bimage;
    }

    public int[] getRGBArray(final @NotNull BufferedImage image) {
        return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
    }

}