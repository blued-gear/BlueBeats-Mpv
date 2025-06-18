package apps.chocolatecakecodes.bluebeats.mpv.editor.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.*
import apps.chocolatecakecodes.bluebeats.mpv.editor.LoadedFile
import apps.chocolatecakecodes.bluebeats.mpv.editor.utils.observerStateChange
import apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.GeneralSettingsForm
import apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.ruleedits.ID3TagsRuleForm
import apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.ruleedits.RegexRuleForm
import apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.ruleedits.RuleGroupForm
import apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.ruleedits.UsertagsRuleForm
import cafe.adriel.bonsai.core.Bonsai
import cafe.adriel.bonsai.core.node.Branch
import cafe.adriel.bonsai.core.node.Leaf
import cafe.adriel.bonsai.core.tree.Tree
import cafe.adriel.bonsai.core.tree.TreeScope

private typealias FormFinalizer = (() -> Unit)

private class KeyedFinalizer(
    val key: String,
    val finalizer: FormFinalizer?,
) {
    override fun equals(other: Any?): Boolean {
        if(other !is KeyedFinalizer) return false
        return this.key == other.key
    }
    override fun hashCode(): Int {
        return key.hashCode()
    }
}

private fun editViewStateKey(isGeneralSettingsSelected: Boolean, rule: GenericRule?): String {
    return "$isGeneralSettingsSelected ; ${System.identityHashCode(rule)}"
}

@Composable
internal fun EditView() {
    Row(
        modifier = Modifier.safeContentPadding().fillMaxSize().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isGeneralSettingsSelected = remember { mutableStateOf(true) }
        val selectedRule = remember { mutableStateOf<GenericRule?>(null) }
        val formFinalizer = remember { mutableStateOf(KeyedFinalizer("", null)) }

        observerStateChange(formFinalizer) { old, new ->
            old.finalizer?.invoke()
        }

        Column(
            modifier = Modifier.safeContentPadding().fillMaxHeight().fillMaxWidth(0.4f),
            verticalArrangement = Arrangement.Top
        ) {
            RuleTree(isGeneralSettingsSelected, selectedRule)
        }
        VerticalDivider()
        Column(
            modifier = Modifier.safeContentPadding().wrapContentSize().fillMaxHeight().padding(vertical = 12.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            var finalizer: FormFinalizer? = null
            if(isGeneralSettingsSelected.value) {
                finalizer = GeneralSettings()
            } else if(selectedRule.value != null) {
                finalizer = RuleForm(selectedRule.value!!)
            }

            formFinalizer.value = KeyedFinalizer(
                editViewStateKey(isGeneralSettingsSelected.value, selectedRule.value),
                finalizer
            )
        }
    }
}

object GeneralSettingsItem

@Composable
private fun RuleTree(isGeneralSettingsSelected: MutableState<Boolean>, selectedRule: MutableState<GenericRule?>) {
    Tree<Any> {
        Leaf(GeneralSettingsItem, customName = { Text("General Settings") })
        addRuleToTree(this, LoadedFile.rootGroup, "Root RuleGroup")
    }.let {
        remember { it.expandAll() }
        Bonsai(
            it,
            onClick = { node ->
                if(node.content == GeneralSettingsItem) {
                    isGeneralSettingsSelected.value = true
                    selectedRule.value = null
                } else {
                    isGeneralSettingsSelected.value = false
                    selectedRule.value = node.content as GenericRule
                }
            }
        )
    }
}

@Composable
fun addRuleToTree(tree: TreeScope, rule: GenericRule, nameOverwrite: String? = null) {
    val name = nameOverwrite ?: "${rule.name} [${rule::class.simpleName}]"
    if(rule is RuleGroup) {
        tree.Branch(rule, customName = { Text(name) }) {
            rule.getRules().forEach { (child, _) ->
                addRuleToTree(this, child)
            }
        }
    } else {
        tree.Leaf(rule, customName = { Text(name) })
    }
}

@Composable
private fun GeneralSettings(): FormFinalizer {
    return GeneralSettingsForm()
}

@Composable
private fun RuleForm(rule: GenericRule): FormFinalizer? {
    //TODO handle 'negate' attribute
    return when(rule) {
        is RuleGroup -> RuleGroupForm(rule, rule !== LoadedFile.rootGroup)
        is ID3TagsRule -> ID3TagsRuleForm(rule)
        is RegexRule -> RegexRuleForm(rule)
        is UsertagsRule -> UsertagsRuleForm(rule)
        else -> null
    }
}
