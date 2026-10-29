package com.pointerwander.luminawatch.ui

import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberCanvasBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.RoundedRectangle
import com.kyant.shapes.UnevenRoundedRectangle

/** 弹层玻璃容器；安全的兄弟图层可通过 [backdrop] 注入实时背景。 */
@Composable
fun LiquidGlassPanel(
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    shape: Shape = RoundedRectangle(28.dp),
    surfaceColor: Color? = null,
    translucent: Boolean = false,
    contentPadding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalWatchColors.current
    val baseColor = colors.background
    val fallback = rememberCanvasBackdrop { drawRect(if (translucent) Color.Transparent else baseColor) }
    Column(
        modifier.drawBackdrop(
            backdrop = backdrop ?: fallback,
            shape = { shape },
            effects = {
                vibrancy()
                blur(10.dp.toPx())
                lens(8.dp.toPx(), 14.dp.toPx(), chromaticAberration = true)
            },
            shadow = { Shadow(radius = 22.dp, color = Color.Black.copy(alpha = if (translucent) .10f else .13f)) },
            onDrawSurface = {
                if (translucent) {
                    drawRect(Brush.verticalGradient(listOf(
                        Color.White.copy(alpha = .96f),
                        (surfaceColor ?: colors.surface).copy(alpha = .89f),
                    )))
                } else {
                    drawRect((surfaceColor ?: colors.surface).copy(alpha = if (backdrop == null) .94f else .92f))
                }
            },
        ).border(
            1.dp,
            Brush.linearGradient(
                listOf(Color.White, colors.outline.copy(alpha = if (translucent) .35f else .65f), Color.White.copy(alpha = .75f)),
            ),
            shape,
        ).padding(contentPadding),
        content = content,
    )
}

/** 所有底部操作弹层统一入口，避免各页重新实现圆角、模糊和底色。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidGlassBottomSheet(
    onDismiss: () -> Unit,
    fullHeight: Boolean = false,
    surfaceColor: Color? = null,
    translucent: Boolean = false,
    contentPadding: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (fullHeight) (surfaceColor ?: LocalWatchColors.current.groupedSurface) else Color.Transparent,
        dragHandle = null,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = fullHeight),
        modifier = if (fullHeight) Modifier.fillMaxHeight() else Modifier,
    ) {
        val view = LocalView.current
        val blurPixels = with(LocalDensity.current) { 36.dp.toPx().toInt() }
        DisposableEffect(view, translucent) {
            val window = (view.parent as? DialogWindowProvider)?.window
            if (translucent && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                val params = window.attributes
                params.blurBehindRadius = blurPixels
                window.attributes = params
            }
            onDispose {
                if (translucent && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && window != null) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                }
            }
        }
        val navigation = LocalAppNavigationState.current
        val sheetColor = surfaceColor ?: LocalWatchColors.current.groupedSurface
        val navBackdrop = rememberCanvasBackdrop { drawRect(sheetColor) }
        Box(Modifier.fillMaxWidth()
            .then(if (fullHeight) Modifier.fillMaxHeight() else Modifier)
            .background(if (translucent) Color.Transparent else sheetColor,
                RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))) {
            LiquidGlassPanel(
                Modifier.fillMaxWidth()
                    .then(if (fullHeight) Modifier.fillMaxHeight() else Modifier)
                    .then(if (navigation != null) Modifier.padding(bottom = 88.dp) else Modifier),
                shape = UnevenRoundedRectangle(
                    topStart = 28.dp, topEnd = 28.dp, bottomEnd = 0.dp, bottomStart = 0.dp,
                ),
                surfaceColor = surfaceColor,
                translucent = translucent,
                contentPadding = contentPadding,
                content = content,
            )
            if (navigation != null) {
                Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .then(if (fullHeight) Modifier else Modifier.padding(top = 12.dp))
                    .then(if (translucent) Modifier.background(sheetColor.copy(alpha = .90f)) else Modifier)
                    .imePadding()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(bottom = 10.dp), contentAlignment = Alignment.Center) {
                    WatchBottomBar(navigation.selected, false, navBackdrop, navigation.onSelect)
                }
            }
        }
    }
}

/** 标准确认弹窗：语义色仅用于确认动作，取消保持浅玻璃。 */
@Composable
fun LiquidGlassConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmTint: Color? = null,
) {
    val colors = LocalWatchColors.current
    Dialog(onDismissRequest = onDismiss) {
        LiquidGlassPanel(Modifier.fillMaxWidth()) {
            Text(title, color = colors.textPrimary, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Text(message, color = colors.textSecondary, fontSize = 14.sp, lineHeight = 21.sp,
                modifier = Modifier.padding(top = 10.dp, bottom = 20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LiquidGlassButton("取消", onDismiss, Modifier.weight(1f), filled = false)
                LiquidGlassButton(confirmLabel, onConfirm, Modifier.weight(1f), tint = confirmTint)
            }
        }
    }
}
