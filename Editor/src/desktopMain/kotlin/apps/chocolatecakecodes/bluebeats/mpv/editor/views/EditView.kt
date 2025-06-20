package apps.chocolatecakecodes.bluebeats.mpv.editor.views

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.*
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

private class SelectedRule(
    val rule: GenericRule,
    val negated: MutableState<Boolean>,
    val parent: RuleGroup?,
)

private fun editViewStateKey(isGeneralSettingsSelected: Boolean, rule: GenericRule?): String {
    return "$isGeneralSettingsSelected ; ${System.identityHashCode(rule)}"
}

@Composable
internal fun EditView() {
    val scrollState = rememberScrollState()
    val treeScrollState = rememberScrollState()

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

        Box(
            modifier = Modifier.safeContentPadding().fillMaxHeight().fillMaxWidth(0.4f),
        ) {
            Box(modifier = Modifier.fillMaxHeight(1f)
                .padding(end = 12.dp, bottom = 12.dp)
                .horizontalScroll(treeScrollState)
                .widthIn(8.dp, 2048.dp)
            ) {
                RuleTree(isGeneralSettingsSelected, selectedRule)
            }
            HorizontalScrollbar(
                adapter = rememberScrollbarAdapter(treeScrollState),
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart).padding(end = 12.dp, bottom = 4.dp)
            )
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

private object GeneralSettingsItem

@Composable
private fun RuleTree(isGeneralSettingsSelected: MutableState<Boolean>, selectedRule: MutableState<SelectedRule?>) {
    val versionKey = remember { mutableStateOf(1) }
    key(versionKey.value) {
        Tree<Any> {
            Leaf(GeneralSettingsItem, customName = { Text("General Settings") })
            addRuleToTree(
                this,
                LoadedFile.rootGroup,
                false,
                null,
                selectedRule,
                versionKey,
                "Root RuleGroup"
            )
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
}

@Composable
private fun addRuleToTree(
    tree: TreeScope,
    rule: GenericRule,
    negated: Boolean,
    parent: RuleGroup?,
    selectedRule: MutableState<SelectedRule?>,
    treeVersion: MutableState<Int>,
    nameOverwrite: String? = null,
) {
    val negatedState = remember { mutableStateOf(negated) }
    val entry = SelectedRule(rule, negatedState, parent)
    var name = nameOverwrite ?: "${rule.name} [${rule::class.simpleName}]"
    if(negatedState.value)
        name += " ⛔"

    if(rule is RuleGroup) {
        tree.Branch(entry, customName = { RuleTreeItem(name, rule, parent, selectedRule, treeVersion) }) {
            rule.getRules().forEach { (child, negated) ->
                addRuleToTree(this, child, negated, rule, selectedRule, treeVersion)
            }
        }
    } else {
        tree.Leaf(entry, customName = { RuleTreeItem(name, rule, parent, selectedRule, treeVersion) })
    }
}

@Composable
private fun RuleTreeItem(
    name: String,
    rule: GenericRule,
    parent: RuleGroup?,
    selectedRule: MutableState<SelectedRule?>,
    treeVersion: MutableState<Int>,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(name)

        if(parent != null) {
            IconButton(
                onClick = {
                    if(rule == selectedRule.value?.rule) {
                        selectedRule.value = SelectedRule(LoadedFile.rootGroup, mutableStateOf(false), null)
                    }
                    parent.removeRule(rule)
                    treeVersion.value = treeVersion.value + 1
                }
            ) {
                Icon(Icons.Default.Delete, contentDescription = "delete rule")
            }
        }
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
