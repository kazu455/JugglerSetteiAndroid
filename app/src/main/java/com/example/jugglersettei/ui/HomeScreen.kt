package com.example.jugglersettei.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.jugglersettei.data.JugglerMachine
import com.example.jugglersettei.data.JugglerMachineRepository
import com.example.jugglersettei.logic.EstimationInput
import com.example.jugglersettei.logic.EstimationResult
import com.example.jugglersettei.logic.SettingEstimator
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val machines = JugglerMachineRepository.machines
    var selectedMachine by remember { mutableStateOf(machines.first()) }

    var totalSpinsText by remember { mutableStateOf("") }
    var bigCountText by remember { mutableStateOf("") }
    var regCountText by remember { mutableStateOf("") }
    var grapeCountText by remember { mutableStateOf("") }

    var results by remember { mutableStateOf<List<EstimationResult>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("ジャグラー設定判別") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            MachineSelector(
                machines = machines,
                selected = selectedMachine,
                onSelected = {
                    selectedMachine = it
                    results = null
                    if (!it.hasGrapeData) grapeCountText = ""
                }
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = totalSpinsText,
                onValueChange = { totalSpinsText = it.filter(Char::isDigit) },
                label = { Text("総回転数") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = bigCountText,
                    onValueChange = { bigCountText = it.filter(Char::isDigit) },
                    label = { Text("BIG回数") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = regCountText,
                    onValueChange = { regCountText = it.filter(Char::isDigit) },
                    label = { Text("REG回数") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = grapeCountText,
                onValueChange = { grapeCountText = it.filter(Char::isDigit) },
                label = {
                    Text(
                        if (selectedMachine.hasGrapeData) "ぶどう回数（任意・わかる場合のみ）"
                        else "ぶどう回数（この機種は非公表のため判別には使用されません）"
                    )
                },
                enabled = selectedMachine.hasGrapeData,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    val spins = totalSpinsText.toIntOrNull()
                    val big = bigCountText.toIntOrNull()
                    val reg = regCountText.toIntOrNull()
                    val grape = grapeCountText.toIntOrNull()

                    if (spins == null || spins <= 0 || big == null || reg == null) {
                        errorMessage = "総回転数・BIG回数・REG回数を正しく入力してください"
                        results = null
                        return@Button
                    }
                    errorMessage = null
                    results = SettingEstimator.estimate(
                        selectedMachine,
                        EstimationInput(spins, big, reg, grape)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("設定を判別する")
            }

            Spacer(Modifier.height(24.dp))

            results?.let { res ->
                ResultSection(res)
                Spacer(Modifier.height(24.dp))
            }

            ReferenceTable(selectedMachine)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MachineSelector(
    machines: List<JugglerMachine>,
    selected: JugglerMachine,
    onSelected: (JugglerMachine) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("機種を選択") },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            machines.forEach { machine ->
                DropdownMenuItem(
                    text = { Text(machine.displayName) },
                    onClick = {
                        onSelected(machine)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ResultSection(results: List<EstimationResult>) {
    val best = results.maxByOrNull { it.posteriorProbability }
    Text("設定判別結果（期待度）", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        results.forEach { r ->
            val percent = (r.posteriorProbability * 100).roundToInt()
            val isBest = r.setting == best?.setting
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "設定${r.setting}",
                    modifier = Modifier.width(64.dp),
                    fontWeight = if (isBest) FontWeight.Bold else FontWeight.Normal
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = (r.posteriorProbability.toFloat()).coerceIn(0f, 1f))
                            .background(
                                if (isBest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text("$percent%", modifier = Modifier.width(48.dp))
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Text(
        "※ あくまで統計的な期待度の目安です。少数データでは信頼性が下がります。設定6を保証するものではありません。",
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun ReferenceTable(machine: JugglerMachine) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        TextButton(onClick = { expanded = !expanded }) {
            Text(if (expanded) "設定判別要素表を閉じる ▲" else "${machine.displayName} の設定判別要素表を見る ▼")
        }
        if (expanded) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Row {
                        Text("設定", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Text("BIG", Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                        Text("REG", Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                        Text("ぶどう", Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                    }
                    Divider(Modifier.padding(vertical = 4.dp))
                    machine.settings.forEach { s ->
                        Row {
                            Text("設定${s.setting}", Modifier.weight(1f))
                            Text("1/${"%.1f".format(s.bigDenom)}", Modifier.weight(1.5f))
                            Text("1/${"%.1f".format(s.regDenom)}", Modifier.weight(1.5f))
                            Text(
                                s.grapeDenom?.let { "1/${"%.2f".format(it)}" } ?: "非公表",
                                Modifier.weight(1.5f)
                            )
                        }
                    }
                    if (!machine.hasGrapeData) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "※ この機種はメーカーがぶどう確率を公表していないため、判別にはBIG/REG確率のみを使用します。",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
