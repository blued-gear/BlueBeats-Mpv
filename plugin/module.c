#include <vlc_common.h>
#include <vlc_plugin.h>
#include <vlc_interface.h>
#include <libBlueBeatsVlc_api.h>

static int Open(vlc_object_t *obj) {
    return k_open(obj);
}

static void Close(vlc_object_t *obj) {
    k_close(obj);
}

vlc_module_begin()
    set_shortname("BlueBeats")
    set_description("BlueBeats dynamic playlists features")
    set_category(CAT_INPUT)
    set_subcategory(SUBCAT_INPUT_DEMUX)
    set_capability("stream_filter", 10)
    set_callbacks(Open, Close)
vlc_module_end()
