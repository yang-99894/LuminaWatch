package com.pointerwander.luminawatch.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointerwander.luminawatch.data.AppStore
import com.pointerwander.luminawatch.ui.DemoBadge
import com.pointerwander.luminawatch.ui.EmptyState
import com.pointerwander.luminawatch.ui.LocalWatchColors
import com.pointerwander.luminawatch.ui.PageColumn
import com.pointerwander.luminawatch.ui.PageHeader
import com.pointerwander.luminawatch.ui.SurfaceCard

@Composable
fun ChatHistoryScreen(store: AppStore, onBack: () -> Unit) {
    val colors = LocalWatchColors.current

    PageColumn {
        PageHeader("会话历史", "保存在本机", onBack = onBack)
        DemoBadge()

        SurfaceCard {
            Text(
                "当前对话共 ${store.messages.size} 条消息。跨设备同步尚未接入。",
                color = colors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp,
            )
        }

        if (store.messages.isEmpty()) {
            EmptyState(title = "还没有对话", body = "回到小云页开始第一句对话。")
        } else {
            store.messages.forEach { message ->
                SurfaceCard {
                    Row(Modifier.fillMaxWidth()) {
                        Text(
                            if (message.fromCloud) "小云" else "我",
                            color = if (message.fromCloud) colors.accent else colors.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            message.text,
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Text(message.timeLabel, color = colors.textSecondary, fontSize = 10.sp)
                }
            }
        }

        Text(
            "云端对话尚未接入，当前回复由本机生成。",
            color = colors.textSecondary,
            fontSize = 11.sp,
            modifier = Modifier.clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                .background(Color.Transparent)
                .padding(2.dp),
        )
    }
}
