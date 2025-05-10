# https://wiki.videolan.org/OutOfTreeCompile/

KBUILD = ../build/bin/native/releaseStatic/

LD = ld
CC = g++
PKG_CONFIG = pkg-config
INCLUDES = -I $(KBUILD)
CFLAGS = -g -O2 -Wall -Wextra $(INCLUDES)
LDFLAGS =
LIBS = $(KBUILD)libBlueBeatsVlc.a
VLC_PLUGIN_CFLAGS := $(shell $(PKG_CONFIG) --cflags vlc-plugin)
VLC_PLUGIN_LIBS := $(shell $(PKG_CONFIG) --libs vlc-plugin)

override CPPFLAGS += -DPIC -I. -Isrc
override CFLAGS += -fPIC
override LDFLAGS += -Wl,-no-undefined,-z,defs

override CPPFLAGS += -DMODULE_STRING=\"bluebeats\"
override CFLAGS += $(VLC_PLUGIN_CFLAGS)
override LIBS += $(VLC_PLUGIN_LIBS)

TARGETS = libbluebeats_plugin.so

build: libbluebeats_plugin.so

clean:
	rm -f -- libbluebeats_plugin.so ./*.o

SOURCES = module.c

libbluebeats_plugin.so: $(SOURCES:%.c=%.o)
	$(CC) $(LDFLAGS) -shared -o $@ $^ $(LIBS)

.PHONY: build clean
