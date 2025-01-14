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

package com.github.pulsebeat02.deluxemediaplugin.command;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;

import java.lang.reflect.Method;

public final class CommandMapHelper {

  private static final Method GET_COMMAND_MAP_METHOD;

  static {
    try {
      final Class<? extends Server> craftServerClass = Bukkit.getServer().getClass();
      GET_COMMAND_MAP_METHOD = craftServerClass.getDeclaredMethod("getCommandMap");
      GET_COMMAND_MAP_METHOD.setAccessible(true);
    } catch (final ReflectiveOperationException exception) {
      throw new RuntimeException(exception);
    }
  }

  /**
   * Uses Reflection to expose and retrieve the CommandMap.
   *
   * @return CommandMap map
   */
  public static CommandMap getCommandMap() {
    try {
      return (CommandMap) GET_COMMAND_MAP_METHOD.invoke(Bukkit.getServer());
    } catch (final ReflectiveOperationException exception) {
      throw new RuntimeException(exception);
    }
  }
}
