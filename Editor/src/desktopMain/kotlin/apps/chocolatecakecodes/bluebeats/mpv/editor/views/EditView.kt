package apps.chocolatecakecodes.bluebeats.mpv.editor.views

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.GenericRule
import apps.chocolatecakecodes.bluebeats.blueplaylists.playlist.dynamicplaylist.rules.RuleGroup
import apps.chocolatecakecodes.bluebeats.mpv.editor.LoadedFile
import apps.chocolatecakecodes.bluebeats.mpv.editor.utils.observerStateChange
import apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.GeneralSettingsForm
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
        modifier = Modifier.safeContentPadding().fillMaxSize(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isGeneralSettingsSelected = remember { mutableStateOf(true) }
        val selectedRule = remember { mutableStateOf<GenericRule?>(null) }
        val formFinalizer = remember { mutableStateOf(KeyedFinalizer("", null)) }

        observerStateChange(formFinalizer) { old, new ->
            old.finalizer?.invoke()
        }

        Column(
            modifier = Modifier.safeContentPadding().fillMaxHeight().fillMaxWidth(0.3f),
            verticalArrangement = Arrangement.Top
        ) {
            RuleTree(isGeneralSettingsSelected, selectedRule)
        }
        Spacer(Modifier.width(8.dp))
        Column(
            modifier = Modifier.safeContentPadding().wrapContentSize().fillMaxHeight(),
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
    if(rule is RuleGroup) {
        tree.Branch(rule, customName = { Text(nameOverwrite ?: rule::class.simpleName!!) }) {
            rule.getRules().forEach { (child, _) ->
                addRuleToTree(this, child)
            }
        }
    } else {
        tree.Leaf(rule, customName = { Text(nameOverwrite ?: rule::class.simpleName!!) })
    }
}

@Composable
private fun GeneralSettings(): FormFinalizer {
    return GeneralSettingsForm()
}

@Composable
private fun RuleForm(rule: GenericRule): FormFinalizer? {
    Text("FORM PANE")
    return null
}
