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

package com.github.pulsebeat02.minecraftmedialibrary.dependency.vlc.pkg;

/**
 * @author Brandon Li
 *     <pre>
 *
 * Linux Packages
 *
 * ===================================================================
 * PROBLEM
 * ===================================================================
 * The primary problem is installing VLC into the Linux operating
 * system. VLC is a media player which uses native languages such as
 * C/C++/Lua for its plugins, dependencies, and main code. The goal is
 * to install the software without using sudo. The reason to this is
 * explained below.
 *
 * ===================================================================
 * TABLE
 * ===================================================================
 * Here is a table displaying which packages contain the binaries and
 * which do not:
 *
 * ﹍﹍﹍﹍﹍﹍﹍﹍﹍﹍﹍﹍﹍﹍﹍﹍﹍﹍﹍﹍﹍﹍﹍﹍﹍﹍﹍﹍﹍﹍﹍﹍﹍﹍
 * ︴ Package Extension  ︴ VLC Binaries?  ︴  Algorithm   ︴
 * ︴------------------- ︴--------------- ︴------------- ︴
 * ︴ (.deb)             ︴ No             ︴     (B)      ︴
 * ︴ (.rpm)             ︴ No             ︴     (B)      ︴
 * ︴ (.txz / .tar.xz)   ︴ Yes            ︴     (A)      ︴
 * ︴ (.tgz / .tar.gz)   ︴ Yes            ︴     (A)      ︴
 * ︴ (.zst)             ︴ Yes            ︴     (A)      ︴
 * ︴ (.eopkg)           ︴ Up to User     ︴     (C)      ︴
 * ﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉﹉
 *
 * ===================================================================
 * ALGORITHMS
 * ===================================================================
 *
 * (A) -> Extraction Algorithm
 *          - Due to the package already containing pre-compiled binaries of VLC,
 *            it is not necessary to perform any previous steps such as installing
 *            dependencies, other plugins, etc.
 *          - A simple extraction will do. Add it to a search path in NativeDiscovery
 *            for easy configuration.
 *
 * (B) -> Package Repository Hack Algorithm
 *          - Most famous packages today for Debian distributions use .deb file (also
 *            known as debian package). For CentOS and other Redhat distributions, .rpm
 *            packages are the common convention. Unfortunately, the packages do not
 *            contain the binaries of VLC. In fact, they do not contain the dependencies
 *            either, which makes the job very tough. The main issue here as well is that
 *            we are not guaranteed an environment such that the user is root. This is
 *            especially the case for server hosting company environments, where it is
 *            unlikely that:
 *              1) The consumer knows the password for the sudo command.
 *              2) The server hosting company will give out the password because it makes
 *                 it very vulnerable to hacks, ddos attacks, and many other security
 *                 issues.
 *          - Perhaps a common hack we could use to bypass this is by compiling VLC sources
 *            on the server computer to get the binaries. The benefit of this is that it
 *            isn't distribution dependent for this specific package. The issue, however, is
 *            that it requires either the user to have gcc or the library to use some form of
 *            C/C++/Lua compiler. At the moment, many native compilers written in Java are very
 *            outdated, which makes this option not viable.
 *          - Another way we could use is JuNest (https://github.com/fsquillace/junest) which
 *            basically uses a tiny distribution in the $HOME/.junest directory. This seems
 *            like a great solution, however, it would require many commands to run. The only
 *            known limitation is that the Linux kernel must be at least 2.6.32. Steps to take
 *            this process could include:
 *              - Build a CommandTaskChain.
 *                  (1) Execute the command to install JuNest:
 *                          > git clone git://github.com/fsquillace/junest ~/.local/share/junest
 *                          > export PATH=~/.local/share/junest/bin:$PATH
 *                  (2) Enter into the JuNest console:
 *                          > junest -f
 *                  (3) Install the Package:
 *                          > apt install ./vlc.deb
 *                  (4) Add NativeDiscovery onto the JuNest Directory:
 *                          NativeDiscovery.addSearchpath("$HOME/.junest/...");
 *                  (5) Load and done!
 *           - I also found some scripts that allow you to install some packages, but I fear
 *             that they are not reliable. For example, in the resources folder, there is a
 *             apt command for no root and a yum command for no root. Perhaps these processes
 *             may work, however, they may be unreliable as they rely on files with specific
 *             permissions (and if the script does not have permission for such permissions,
 *             it will not be able to work). The advantage of JuNest is that it is basically
 *             it's own Linux distribution with sudo (almost like a virtual machine!).
 *           - Based on these decisions, I feel that the JuNest approach is the best approach
 *             for solving this main issue. The kernel is the only limitation (which means the
 *             kernel must be at least dating from 2012 - 2013), so I feel like it would depend
 *             on the server hosting software. Honestly, I would hate those who don't update
 *             their kernel and I doubt that many server companies would be using outdated
 *             kernels. However, it would be interesting to see what other people's opinions
 *             are to solving this issue.
 *
 * (C) -> User Based Algorithm
 *          - In terms of availability, .eopkg packages will not be supported due to the
 *            tough nature of giving support to Solus systems. Solus is also very outdated
 *            as other Linux's have taken over. If the user would like to continue to run
 *            the library, they must perform a manual installation.
 *
 * ===================================================================
 * FINAL THOUGHTS
 * ===================================================================
 * If anyone has any thoughts, feel free to contact me. I am still
 * trying to elaborate on the best solution to this issue.
 *
 * </pre>
 */
public class ResearchNotes {}