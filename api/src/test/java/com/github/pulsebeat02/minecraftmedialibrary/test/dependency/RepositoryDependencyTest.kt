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
package com.github.pulsebeat02.minecraftmedialibrary.test.dependency

import com.github.kiulian.downloader.cipher.Cipher
import com.github.kiulian.downloader.cipher.CipherFactory
import com.github.kiulian.downloader.cipher.CipherFunction
import com.github.pulsebeat02.minecraftmedialibrary.dependency.DependencyManagement
import com.github.pulsebeat02.minecraftmedialibrary.dependency.FFmpegDependencyInstallation
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger
import java.nio.file.Paths

fun main() {
    Logger.setVerbose(true)
    val management = DependencyManagement(Paths.get(""))
    management.install()
    management.relocate()
    management.load()
    val ffmpeg =
        FFmpegDependencyInstallation(Paths.get(""))
    ffmpeg.install()
    object : CipherFactory {
        override fun createCipher(s: String): Cipher? {
            return null
        }

        override fun addInitialFunctionPattern(i: Int, s: String) {}
        override fun addFunctionEquivalent(s: String, cipherFunction: CipherFunction) {}
    }
}
