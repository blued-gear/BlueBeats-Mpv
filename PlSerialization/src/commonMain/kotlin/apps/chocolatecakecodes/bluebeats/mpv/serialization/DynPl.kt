package apps.chocolatecakecodes.bluebeats.mpv.serialization

import apps.chocolatecakecodes.bluebeats.mpv.serialization.rules.RuleGroupSerializable
import kotlinx.serialization.Serializable

@Serializable
data class DynPl(
    val mediaRoot: String,
    val iterationSize: Int,
    val rootRule: RuleGroupSerializable,
)
