# BlueBeats Mpv

A C plugin for [mpv](https://mpv.io/).\
It enables using of `bbdp` files which define *Dynamic Playlists*.
These are playlists which collect their items by a set of rules. So the list is assembled in any combination you can imagine.
Use directories, ID3-Tags, specific chapters and even Regex to define which file should be included.

This project also provides an Editor, a GUI to create and edit bbdp files.

For more info about the rules see [here](https://projects.chocolatecakecodes.goip.de/bluebeats/bluebeats-app/-/wikis/Dynamic-Playlists)
and [here](https://projects.chocolatecakecodes.goip.de/bluebeats/blueplaylists#rules).

A bbdp file contains a `mediaRoot` path. This is the directory which contains all media which should be scanned
so that rules can find them (also IncludeRule paths are relative to mediaRoot).
If you want to use a bbdp file on an other computer than where it was created, adjust the value
(you can use a texteditor for that).

## Installation
### Plugin

[Download](https://projects.chocolatecakecodes.goip.de/bluebeats/bluebeats-mpv/-/packages/22) the latest version
and put the *.so* file in `~/.config/mpv/scripts/` (the directory might be different if you use a player which is based on mpv).
Also you need to install a package which provides the legacy-binding `libcrypt.so.1` (needed by Kotlin Native).
On Fedora this is `libxcrypt-compat`.\
When you open a bbdp file, the plugin will fill a playlist with items and start playback.
Whenever the end of the list is reached, it will be regenerated.

### Editor

[Download](https://projects.chocolatecakecodes.goip.de/bluebeats/bluebeats-mpv/-/packages/23) and run the Jar.
