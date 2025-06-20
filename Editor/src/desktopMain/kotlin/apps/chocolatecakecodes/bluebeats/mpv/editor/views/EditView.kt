package apps.chocolatecakecodes.bluebeats.mpv.editor.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import apps.chocolatecakecodes.bluebeats.mpv.editor.utils.observeStateChange
import apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.GeneralSettingsForm
import apps.chocolatecakecodes.bluebeats.mpv.editor.widgets.ruleedits.*
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
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier.safeContentPadding().fillMaxSize().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isGeneralSettingsSelected = remember { mutableStateOf(true) }
        val selectedRule = remember { mutableStateOf<SelectedRule?>(null) }
        val formFinalizer = remember { mutableStateOf(KeyedFinalizer("", null)) }

        observeStateChange(formFinalizer) { old, new ->
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
            modifier = Modifier.safeContentPadding()
                .padding(vertical = 12.dp)
                .fillMaxWidth().fillMaxHeight()
                .verticalScroll(scrollState),
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
                editViewStateKey(isGeneralSettingsSelected.value, selectedRule.value?.rule),
                finalizer
            )
        }
    }
}

object GeneralSettingsItem

@Composable
private fun RuleTree(isGeneralSettingsSelected: MutableState<Boolean>, selectedRule: MutableState<SelectedRule?>) {
    Tree<Any> {
        Leaf(GeneralSettingsItem, customName = { Text("General Settings") })
        addRuleToTree(this, LoadedFile.rootGroup, false, null, "Root RuleGroup")
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
                    selectedRule.value = node.content as SelectedRule
                }
            }
        )
    }
}

@Composable
fun addRuleToTree(tree: TreeScope, rule: GenericRule, negated: Boolean, parent: RuleGroup?, nameOverwrite: String? = null) {
    val negatedState = remember { mutableStateOf(negated) }
    val entry = SelectedRule(rule, negatedState, parent)
    var name = nameOverwrite ?: "${rule.name} [${rule::class.simpleName}]"
    if(negatedState.value)
        name += " ⛔"

    if(rule is RuleGroup) {
        tree.Branch(entry, customName = { Text(name) }) {
            rule.getRules().forEach { (child, negated) ->
                addRuleToTree(this, child, negated, rule)
            }
        }
    } else {
        tree.Leaf(entry, customName = { Text(name) })
    }
}

@Composable
private fun GeneralSettings(): FormFinalizer {
    return GeneralSettingsForm()
}

@Composable
private fun RuleForm(entry: SelectedRule): FormFinalizer? {
    if(entry.parent != null) {
        observeStateChange(entry.negated) { old, new ->
            if(old == new) return@observeStateChange
            entry.parent.setRuleNegated(entry.rule, new)
        }
    }

    return when(entry.rule) {
        is RuleGroup -> RuleGroupForm(entry.rule, entry.negated, entry.rule !== LoadedFile.rootGroup)
        is ID3TagsRule -> ID3TagsRuleForm(entry.rule, entry.negated)
        is RegexRule -> RegexRuleForm(entry.rule, entry.negated)
        is UsertagsRule -> UsertagsRuleForm(entry.rule, entry.negated)
        is IncludeRule -> IncludeRuleForm(entry.rule, entry.negated)
        is TimeSpanRule -> TimeSpanRuleForm(entry.rule, entry.negated)
    }
}

private class SelectedRule(
    val rule: GenericRule,
    val negated: MutableState<Boolean>,
    val parent: RuleGroup?,
)
