#!/bin/sh

podman run --name bluebeatsvlc --rm \
  -v "${XDG_RUNTIME_DIR}/${WAYLAND_DISPLAY}:/tmp/wayland-0:rw,z" \
  -v "${XDG_RUNTIME_DIR}/pipewire-0:/tmp/pipewire-0:rw,z" \
  -v "./media:/home/vlc/media:rw,Z" \
  -v "./plugins:/usr/lib64/vlc/plugins/custom:ro,Z" \
  --device /dev/dri \
  --security-opt label=type:container_runtime_t \
  --userns keep-id:uid=1001,gid=1001 \
  localhost/bluebeatsvlc \
  vlc -vv --color \
  2>&1 | tee log.txt
