package apps.chocolatecakecodes.bluebeats.mpv.serialization.misc

enum class ID3TagType(
    val description: String,
) {
    INVALID("[unknown ID3 tag]"),
    TITLE("Title"),
    ARTIST("Artist"),
    GENRE("Genre"),
}
