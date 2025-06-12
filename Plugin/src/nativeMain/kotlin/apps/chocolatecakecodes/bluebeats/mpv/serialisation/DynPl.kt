package apps.chocolatecakecodes.bluebeats.mpv.serialisation

import apps.chocolatecakecodes.bluebeats.mpv.serialisation.rules.RuleGroupSerializable
import kotlinx.serialization.Serializable

@Serializable
internal data class DynPl(
    val mediaRoot: String,
    val iterationSize: Int,
    val rootRule: RuleGroupSerializable,
)
