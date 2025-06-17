package apps.chocolatecakecodes.bluebeats.mpv.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
object Serializer {

    val json = Json {
        this.prettyPrint = true
        this.prettyPrintIndent = "  "
        this.allowComments = true
        this.allowTrailingComma = true
        this.coerceInputValues = true
        this.allowComments = true
        this.explicitNulls = true
    }

}
