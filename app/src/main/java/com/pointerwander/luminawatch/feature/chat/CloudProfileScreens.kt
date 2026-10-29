package com.pointerwander.luminawatch.feature.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointerwander.luminawatch.data.AppStore
import com.pointerwander.luminawatch.ui.EmptyState
import com.pointerwander.luminawatch.ui.LocalWatchColors
import com.pointerwander.luminawatch.ui.LiquidGlassChoiceChip
import com.pointerwander.luminawatch.ui.PageColumn
import com.pointerwander.luminawatch.ui.PageHeader
import com.pointerwander.luminawatch.ui.PrimaryButton
import com.pointerwander.luminawatch.ui.SectionTitle
import com.pointerwander.luminawatch.ui.SurfaceCard
import com.pointerwander.luminawatch.ui.ToggleRow

@Composable
fun CloudMemoryScreen(store: AppStore, onBack: () -> Unit) {
    val colors = LocalWatchColors.current
    PageColumn {
        PageHeader("小云记忆", "由你决定小云记住什么", onBack = onBack)
        SurfaceCard {
            ToggleRow("启用记忆", "开启后才允许保存你主动确认的信息", store.memoryEnabled) {
                store.updateMemoryEnabled(it)
            }
        }
        SectionTitle("记忆列表")
        EmptyState("还没有记忆", "目前没有保存的记忆。接入云岸后，新增记忆会先征得你的同意。")
        Text("你可以随时关闭记忆并管理保存的内容。", color = colors.textSecondary, fontSize = 12.sp)
    }
}

@Composable
fun CloudStyleScreen(store: AppStore, onBack: () -> Unit) {
    val colors = LocalWatchColors.current
    var name by remember { mutableStateOf(store.cloudName) }
    var personality by remember { mutableStateOf(store.cloudPersonality) }
    var description by remember { mutableStateOf(store.cloudDescription) }
    PageColumn {
        PageHeader("小云设定", "让陪伴更像你喜欢的样子", onBack = onBack)
        SurfaceCard {
            Text("名称", color = colors.textSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(name, { name = it.take(20) }, Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(16.dp))
            Text("性格", color = colors.textSecondary, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("开朗", "温柔", "幽默", "成熟").forEach { item ->
                    LiquidGlassChoiceChip(item, personality == item, { personality = item })
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("简介", color = colors.textSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(description, { description = it.take(120) }, Modifier.fillMaxWidth(),
                minLines = 3)
        }
        PrimaryButton("保存", {
            store.updateCloudProfile(name, personality, description)
            onBack()
        }, Modifier.fillMaxWidth())
        Text("这些设定仅影响当前 App 的显示，云岸同步将在接入后开放。",
            color = colors.textSecondary, fontSize = 12.sp)
    }
}
