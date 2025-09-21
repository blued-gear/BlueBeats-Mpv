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
and put the *.so* file in `~/.config/mpv/scripts/` (the directory might be different if you use a player which is based on mpv).\
When you open a bbdp file, the plugin will fill a playlist with items and start playback.
Whenever the end of the list is reached, it will be regenerated.

For some players it is necessary to register a MIME audio type. This will also allow you to associate a default player for bbdp files.
To do this, follow these steps:
1. download the `chocolatecakecodes-bbdp.xml` file from the `extra` directory in this repo
2. run `xdg-mime install --mode user ./chocolatecakecodes-bbdp.xml`
3. run `update-mime-database ~/.local/share/mime/`

### Editor

[Download](https://projects.chocolatecakecodes.goip.de/bluebeats/bluebeats-mpv/-/packages/23) and run the Jar.
