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

package com.github.pulsebeat02.minecraftmedialibrary.nms.impl.v1_8_R3;

import com.github.pulsebeat02.minecraftmedialibrary.nms.PacketHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_8_R3.MapIcon;
import net.minecraft.server.v1_8_R3.MinecraftKey;
import net.minecraft.server.v1_8_R3.PacketDataSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutCustomPayload;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutMap;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NMSMapPacketIntercepter implements PacketHandler {

  public static final int PACKET_THRESHOLD_MS = 0;

  private static final Field[] MAP_FIELDS = new Field[10];
  private static Field METADATA_ID;

  static {
    try {
      MAP_FIELDS[0] = PacketPlayOutMap.class.getDeclaredField("a");
      MAP_FIELDS[1] = PacketPlayOutMap.class.getDeclaredField("b");
      MAP_FIELDS[2] = PacketPlayOutMap.class.getDeclaredField("c");
      MAP_FIELDS[3] = PacketPlayOutMap.class.getDeclaredField("d");
      MAP_FIELDS[4] = PacketPlayOutMap.class.getDeclaredField("e");
      MAP_FIELDS[5] = PacketPlayOutMap.class.getDeclaredField("f");
      MAP_FIELDS[6] = PacketPlayOutMap.class.getDeclaredField("g");
      MAP_FIELDS[7] = PacketPlayOutMap.class.getDeclaredField("h");
      for (final Field field : MAP_FIELDS) {
        field.setAccessible(true);
      }
      METADATA_ID = PacketPlayOutEntityMetadata.class.getDeclaredField("a");
      METADATA_ID.setAccessible(true);
      final Field METADATA_ITEMS = PacketPlayOutEntityMetadata.class.getDeclaredField("b");
      METADATA_ITEMS.setAccessible(true);
    } catch (final Exception exception) {
      exception.printStackTrace();
    }
  }

  private final Map<UUID, PlayerConnection> playerConnections = new ConcurrentHashMap<>();
  private final Map<UUID, Long> lastUpdated = new ConcurrentHashMap<>();
  private final Set<Integer> maps = new TreeSet<>();
  private final String debugMarker = new MinecraftKey("debug/game_test_add_marker").a();

  @Override
  public void displayDebugMarker(
          final UUID[] viewers,
          final int x,
          final int y,
          final int z,
          final int color,
          final int time) {
    final ByteBuf buf = Unpooled.buffer();
    buf.writeLong(((long) x & 67108863L) << 38 | (long) y & 4095L | ((long) z & 67108863L) << 12);
    buf.writeInt(color);
    buf.writeInt(time);
    final PacketPlayOutCustomPayload packet =
            new PacketPlayOutCustomPayload(debugMarker, new PacketDataSerializer(buf));
    for (final UUID uuid : viewers) {
      playerConnections.get(uuid).sendPacket(packet);
    }
  }

  @Override
  public void displayMaps(
      final UUID[] viewers,
      final int map,
      final int width,
      final int height,
      final ByteBuffer rgb,
      final int videoWidth) {
    final int vidHeight = rgb.capacity() / videoWidth;
    final int pixW = width << 7;
    final int pixH = height << 7;
    final int xOff = (pixW - videoWidth) >> 1;
    final int yOff = (pixH - vidHeight) >> 1;
    displayMaps(viewers, map, width, height, rgb, videoWidth, xOff, yOff);
  }

  @Override
  public void displayMaps(
      final UUID[] viewers,
      final int map,
      final int width,
      final int height,
      final ByteBuffer rgb,
      final int videoWidth,
      final int xOff,
      final int yOff) {
    final int vidHeight = rgb.capacity() / videoWidth;
    final int negXOff = xOff + videoWidth;
    final int negYOff = yOff + vidHeight;
    final int xLoopMin = Math.max(0, xOff / 128);
    final int yLoopMin = Math.max(0, yOff / 128);
    final int xLoopMax = Math.min(width, (int) Math.ceil(negXOff / 128.0));
    final int yLoopMax = Math.min(height, (int) Math.ceil(negYOff / 128.0));
    final PacketPlayOutMap[] packetArray =
        new PacketPlayOutMap[(xLoopMax - xLoopMin) * (yLoopMax - yLoopMin)];
    int arrIndex = 0;
    for (int y = yLoopMin; y < yLoopMax; y++) {
      final int relY = y << 7;
      final int topY = Math.max(0, yOff - relY);
      final int yDiff = Math.min(128 - topY, negYOff - (relY + topY));
      for (int x = xLoopMin; x < xLoopMax; x++) {
        final int relX = x << 7;
        final int topX = Math.max(0, xOff - relX);
        final int xDiff = Math.min(128 - topX, negXOff - (relX + topX));
        final int xPixMax = xDiff + topX;
        final int yPixMax = yDiff + topY;
        final byte[] mapData = new byte[xDiff * yDiff];
        for (int iy = topY; iy < yPixMax; iy++) {
          final int yPos = relY + iy;
          final int indexY = (yPos - yOff) * videoWidth;
          for (int ix = topX; ix < xPixMax; ix++) {
            final int val = (iy - topY) * xDiff + ix - topX;
            mapData[val] = rgb.get(indexY + relX + ix - xOff);
          }
        }
        final int mapId = map + width * y + x;
        final PacketPlayOutMap packet = new PacketPlayOutMap();
        try {
          MAP_FIELDS[0].set(packet, mapId);
          MAP_FIELDS[1].set(packet, (byte) 0);
          MAP_FIELDS[2].set(packet, false);
          MAP_FIELDS[3].set(packet, false);
          MAP_FIELDS[4].set(packet, new MapIcon[0]);
          MAP_FIELDS[5].set(packet, topX);
          MAP_FIELDS[6].set(packet, topY);
          MAP_FIELDS[7].set(packet, xDiff);
          MAP_FIELDS[8].set(packet, yDiff);
          MAP_FIELDS[9].set(packet, mapData);
        } catch (final Exception exception) {
          exception.printStackTrace();
        }
        packetArray[arrIndex++] = packet;
      }
    }
    if (viewers == null) {
      for (final UUID uuid : playerConnections.keySet()) {
        final long val = lastUpdated.getOrDefault(uuid, 0L);
        if (System.currentTimeMillis() - val > PACKET_THRESHOLD_MS) {
          lastUpdated.put(uuid, System.currentTimeMillis());
          final PlayerConnection connection = playerConnections.get(uuid);
          for (final PacketPlayOutMap packet : packetArray) {
            connection.sendPacket(packet);
          }
        }
      }
    } else {
      for (final UUID uuid : viewers) {
        final long val = lastUpdated.getOrDefault(uuid, 0L);
        if (System.currentTimeMillis() - val > PACKET_THRESHOLD_MS) {
          lastUpdated.put(uuid, System.currentTimeMillis());
          final PlayerConnection connection = playerConnections.get(uuid);
          if (connection != null) {
            for (final PacketPlayOutMap packet : packetArray) {
              connection.sendPacket(packet);
            }
          }
        }
      }
    }
  }

  @Override
  public void displayEntities(
      final UUID[] viewers, final Entity[] entities, final int[] data, final int width) {
    final int height = data.length / width;
    final int maxHeight = Math.min(height, entities.length);
    final PacketPlayOutEntityMetadata[] packets = new PacketPlayOutEntityMetadata[maxHeight];
    for (int i = 0; i < maxHeight; i++) {
      final int id = ((CraftEntity) entities[i]).getHandle().getId();
      final PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata();
      try {
        METADATA_ID.set(packet, id);
      } catch (final IllegalArgumentException | IllegalAccessException e) {
        e.printStackTrace();
      }
      packets[i] = packet;
    }
    if (viewers == null) {
      for (final UUID uuid : playerConnections.keySet()) {
        final PlayerConnection connection = playerConnections.get(uuid);
        for (final PacketPlayOutEntityMetadata packet : packets) {
          connection.sendPacket(packet);
        }
      }
    } else {
      for (final UUID uuid : viewers) {
        final PlayerConnection connection = playerConnections.get(uuid);
        if (connection != null) {
          for (final PacketPlayOutEntityMetadata packet : packets) {
            connection.sendPacket(packet);
          }
        }
      }
    }
  }

  @Override
  public Object onPacketInterceptOut(final Player viewer, final Object packet) {
    if (packet instanceof PacketPlayOutMinimap) {
      return ((PacketPlayOutMinimap) packet).packet;
    }
    return packet;
  }

  @Override
  public Object onPacketInterceptIn(final Player viewer, final Object packet) {
    return packet;
  }

  @Override
  public void registerPlayer(final Player player) {
    playerConnections.put(
        player.getUniqueId(), ((CraftPlayer) player).getHandle().playerConnection);
  }

  @Override
  public void unregisterPlayer(final Player player) {
    playerConnections.remove(player.getUniqueId());
  }

  @Override
  public boolean isMapRegistered(final int id) {
    return maps.contains(id);
  }

  @Override
  public void registerMap(final int id) {
    maps.add(id);
  }

  @Override
  public void unregisterMap(final int id) {
    maps.remove(id);
  }

  private static class PacketPlayOutMinimap extends PacketPlayOutMap {
    protected final PacketPlayOutMap packet;

    protected PacketPlayOutMinimap(final PacketPlayOutMap packet) {
      this.packet = packet;
    }
  }
}
