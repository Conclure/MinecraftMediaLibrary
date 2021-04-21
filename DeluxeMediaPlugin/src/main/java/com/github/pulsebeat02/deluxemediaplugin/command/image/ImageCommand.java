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

package com.github.pulsebeat02.deluxemediaplugin.command.image;

import com.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import com.github.pulsebeat02.deluxemediaplugin.command.BaseCommand;
import com.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities;
import com.github.pulsebeat02.minecraftmedialibrary.MinecraftMediaLibrary;
import com.github.pulsebeat02.minecraftmedialibrary.image.basic.MinecraftStaticImage;
import com.github.pulsebeat02.minecraftmedialibrary.utility.FileUtilities;
import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;

public class ImageCommand extends BaseCommand implements Listener {

  private final Set<MinecraftStaticImage> images;
  private final Set<UUID> listen;
  private final LiteralCommandNode<CommandSender> literalNode;
  private int width;
  private int height;

  public ImageCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final TabExecutor executor) {
    super(plugin, "dither", executor, "deluxemediaplugin.command.image", "");
    Bukkit.getPluginManager().registerEvents(this, plugin);
    listen = new HashSet<>();
    images = new HashSet<>();
    width = 1;
    height = 1;
    final LiteralArgumentBuilder<CommandSender> builder = literal(getName());
    builder
        .requires(super::testPermission)
        .then(
            literal("reset")
                .then(
                    literal("map")
                        .then(argument("id", StringArgumentType.word()).executes(this::resetMap)))
                .then(literal("all").executes(this::resetAllMaps)))
        .then(
            literal("set")
                .then(
                    literal("map")
                        .then(
                            argument("id", LongArgumentType.longArg())
                                .then(
                                    argument("mrl", StringArgumentType.greedyString())
                                        .executes(this::setImage))))
                .then(
                    literal("dimensions")
                        .then(
                            argument("dim", StringArgumentType.greedyString())
                                .executes(this::setDimensions))))
        .then(literal("rickroll").executes(this::setRickRoll));
    literalNode = builder.build();
  }

  private boolean isUrl(@NotNull final String mrl) {
    return ((mrl.startsWith("http://")) || mrl.startsWith("https://")) && mrl.endsWith(".png");
  }

  @Override
  public LiteralCommandNode<CommandSender> getCommandNode() {
    return literalNode;
  }

  @Override
  public Component usage() {
    return ChatUtilities.getCommandUsage(
        ImmutableMap.of(
            "/image", "Lists the proper usage of the command",
            "/image reset all", "Purges all loaded images onto maps",
            "/image reset map [id]", "Purges a specific map on an id",
            "/image rickroll", "Posts a Rick Roll image on map id 69"));
  }

  private int setRickRoll(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    final MinecraftMediaLibrary library = getPlugin().getLibrary();
    final File f =
        FileUtilities.downloadImageFile(
            "https://images.news18.com/ibnlive/uploads/2020/12/1607660925_untitled-design-2020-12-11t095722.206.png",
            library.getParentFolder());
    new MinecraftStaticImage(library, 69, f, width, height).drawImage();
    audience.sendMessage(
        ChatUtilities.formatMessage(Component.text("Gottem", NamedTextColor.GOLD)));
    return 1;
  }

  private int setDimensions(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    final Optional<int[]> opt =
        ChatUtilities.checkDimensionBoundaries(audience, context.getArgument("dims", String.class));
    if (!opt.isPresent()) {
      return 1;
    }
    final int[] dims = opt.get();
    width = dims[0];
    height = dims[1];
    audience.sendMessage(
        ChatUtilities.formatMessage(
            Component.text(
                String.format(
                    "Changed item frame dimensions to %d:%d (width:height)", width, height),
                NamedTextColor.GOLD)));
    return 1;
  }

  private int setImage(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    final OptionalLong opt =
        ChatUtilities.checkMapBoundaries(audience, context.getArgument("id", String.class));
    if (!opt.isPresent()) {
      return 1;
    }
    final int id = (int) opt.getAsLong();
    final String mrl = context.getArgument("mrl", String.class);
    final ComponentLike successful =
        ChatUtilities.formatMessage(
            Component.text(
                String.format("Successfully drew image on map %d", id), NamedTextColor.GOLD));
    final DeluxeMediaPlugin plugin = getPlugin();
    final MinecraftMediaLibrary library = plugin.getLibrary();
    if (isUrl(mrl)) {
      final File img = FileUtilities.downloadImageFile(mrl, plugin.getLibrary().getParentFolder());
      new MinecraftStaticImage(library, id, img, width, height).drawImage();
      audience.sendMessage(successful);
    } else {
      final File img = new File(plugin.getDataFolder(), mrl);
      if (img.exists()) {
        new MinecraftStaticImage(library, id, img, width, height).drawImage();
        audience.sendMessage(successful);
      } else {
        audience.sendMessage(
            ChatUtilities.formatMessage(
                Component.text(
                    String.format("File %s cannot be found!", img.getName()), NamedTextColor.RED)));
      }
    }
    return 1;
  }

  private int resetMap(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    final OptionalLong opt =
        ChatUtilities.checkMapBoundaries(audience, context.getArgument("id", String.class));
    if (!opt.isPresent()) {
      return 1;
    }
    final int id = (int) opt.getAsLong();
    final Iterator<MinecraftStaticImage> itr = images.iterator();
    while (itr.hasNext()) {
      if (itr.next().getMap() == id) {
        itr.remove();
        break;
      }
    }
    MinecraftStaticImage.resetMap(getPlugin().getLibrary(), id);
    audience.sendMessage(
        ChatUtilities.formatMessage(
            Component.text(
                String.format("Successfully purged the map with ID %d", id), NamedTextColor.GOLD)));
    return 1;
  }

  private int resetAllMaps(@NotNull final CommandContext<CommandSender> context) {
    final CommandSender sender = context.getSource();
    final Audience audience = getPlugin().getAudiences().sender(context.getSource());
    listen.add(((Player) sender).getUniqueId());
    audience.sendMessage(
        ChatUtilities.formatMessage(
            Component.text(
                "Are you sure you want to purge and delete all maps? Type YES if you would like to continue.",
                NamedTextColor.RED)));
    return 1;
  }

  @EventHandler
  public void onPlayerChat(final AsyncPlayerChatEvent event) {
    final Player p = event.getPlayer();
    if (listen.contains(p.getUniqueId())) {
      final Audience audience = getPlugin().getAudiences().player(p);
      if (event.getMessage().equalsIgnoreCase("YES")) {
        for (final MinecraftStaticImage image : images) {
          MinecraftStaticImage.resetMap(getPlugin().getLibrary(), image.getMap());
        }
        images.clear();
        audience.sendMessage(
            ChatUtilities.formatMessage(
                Component.text("Successfully purged all image maps", NamedTextColor.GOLD)));
      } else {
        audience.sendMessage(
            ChatUtilities.formatMessage(
                Component.text("Cancelled purge of all image maps", NamedTextColor.RED)));
      }
    }
  }
}