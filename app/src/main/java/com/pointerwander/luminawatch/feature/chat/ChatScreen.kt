package com.pointerwander.luminawatch.feature.chat

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointerwander.luminawatch.data.AppStore
import com.pointerwander.luminawatch.data.ChatMessage
import com.pointerwander.luminawatch.data.MessageStatus
import com.pointerwander.luminawatch.ui.ActionRow
import com.pointerwander.luminawatch.ui.CloudImage
import com.pointerwander.luminawatch.ui.CircleIconButton
import com.pointerwander.luminawatch.ui.LocalBottomBarInset
import com.pointerwander.luminawatch.ui.LocalWatchColors
import com.pointerwander.luminawatch.ui.LiquidGlassBottomSheet
import com.pointerwander.luminawatch.ui.LiquidGlassButton
import com.pointerwander.luminawatch.ui.LiquidGlassIconButton
import com.pointerwander.luminawatch.ui.SurfaceCard
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    store: AppStore,
    onOpenHistory: () -> Unit,
    onOpenMemory: () -> Unit,
    onOpenStyle: () -> Unit,
    onBack: (() -> Unit)? = null,
) {
    val colors = LocalWatchColors.current
    val context = LocalContext.current
    val attachmentBackdrop = rememberLayerBackdrop()
    var input by rememberSaveable { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showAttach by remember { mutableStateOf(false) }
    var attachReady by remember { mutableStateOf(false) }
    var pendingImage by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val contextEvent = store.careEvent(store.activeCareContextId)

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            pendingImage = copyImageToFiles(context, uri)
            showAttach = false
        }
    }
    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            pendingImage = saveBitmapToFiles(context, bitmap)
            showAttach = false
        }
    }

    fun send() {
        val image = pendingImage
        if (image == null && input.isBlank()) return
        if (image != null) store.sendImage(image)
        if (input.isNotBlank()) store.sendMessage(input)
        pendingImage = null
        input = ""
        scope.launch {
            delay(650)
            store.completeReply()
        }
    }

    LaunchedEffect(store.messages.size) {
        if (store.messages.isNotEmpty()) listState.animateScrollToItem(store.messages.lastIndex)
    }

    BackHandler(enabled = showAttach) { showAttach = false }

    LaunchedEffect(showAttach) {
        if (showAttach) {
            attachReady = false
            delay(220)
            attachReady = true
        }
    }

    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().layerBackdrop(attachmentBackdrop)) {
            Column(
                Modifier.fillMaxSize().padding(horizontal = 16.dp)
                    .padding(bottom = LocalBottomBarInset.current),
            ) {
                Row(
                    Modifier.fillMaxWidth().height(62.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onBack != null) {
                        CircleIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "返回", onBack)
                        Spacer(Modifier.width(8.dp))
                    }
                    CloudImage(Modifier.size(42.dp), avatarId = store.avatarId)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(store.cloudName, color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Text("陪你聊聊", color = colors.textSecondary, fontSize = 11.sp)
                    }
                    LiquidGlassIconButton(Icons.Rounded.MoreHoriz, "小云菜单",
                        { showMenu = true }, size = 42.dp)
                }

                if (contextEvent != null) {
                    SurfaceCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "这次关怀",
                                color = colors.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f),
                            )
                            LiquidGlassButton("关闭", { store.activeCareContextId = null },
                                height = 36.dp, filled = false)
                        }
                        Text(contextEvent.title, color = colors.textSecondary, fontSize = 12.sp)
                        Text(
                            if (contextEvent.contextAuthorized) "已允许小云使用这次关怀信息" else "健康数值不会发送给小云",
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                        )
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    items(store.messages.size) { index ->
                        MessageBubble(store.messages[index]) {
                            store.retryLastMessage()
                            scope.launch { delay(650); store.completeReply() }
                        }
                    }
                    if (store.pendingReply) {
                        item { Text("小云正在输入…", color = colors.textSecondary, fontSize = 12.sp) }
                    }
                }

                pendingImage?.let { path ->
                    AttachmentPreview(path) { pendingImage = null }
                }

                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
                        .background(colors.surfaceMuted).padding(horizontal = 6.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LiquidGlassIconButton(Icons.Rounded.Add, "添加照片",
                        { showAttach = true }, size = 38.dp)
                    Spacer(Modifier.width(4.dp))
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        placeholder = { Text("今天想聊点什么？", fontSize = 13.sp) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { send() }),
                    )
                    Spacer(Modifier.width(4.dp))
                    LiquidGlassIconButton(Icons.Rounded.Mic, "语音输入", {
                        Toast.makeText(context, "语音能力尚未接入", Toast.LENGTH_SHORT).show()
                    }, size = 38.dp)
                    Spacer(Modifier.width(4.dp))
                    LiquidGlassIconButton(Icons.AutoMirrored.Rounded.Send, "发送消息",
                        { send() }, filled = true, size = 42.dp)
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        if (showAttach) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = .22f))
                    .clickable { showAttach = false },
            )
            Box(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .padding(bottom = LocalBottomBarInset.current),
            ) {
                AttachmentSheet(
                    onDismiss = { showAttach = false },
                    onPickPhoto = {
                        pickPhoto.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    onTakePhoto = { takePhoto.launch(null) },
                    enabled = attachReady,
                    backdrop = attachmentBackdrop,
                )
            }
        }
    }

    if (showMenu) {
        LiquidGlassBottomSheet(onDismiss = { showMenu = false }) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CloudImage(Modifier.size(48.dp), avatarId = store.avatarId)
                Spacer(Modifier.width(12.dp))
                Text(store.cloudName, color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
                ActionRow("记忆", "查看小云记住的信息", Icons.Rounded.AutoAwesome, onClick = {
                    showMenu = false; onOpenMemory()
                })
                ActionRow("聊天记录", icon = Icons.Rounded.History, onClick = {
                    showMenu = false; onOpenHistory()
                })
                ActionRow("聊天风格", "调整名称、性格与简介", Icons.Rounded.Palette, onClick = {
                    showMenu = false; onOpenStyle()
                })
                Spacer(Modifier.height(22.dp))
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage, onRetry: () -> Unit) {
    val colors = LocalWatchColors.current
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.fromCloud) Arrangement.Start else Arrangement.End,
    ) {
        Column(
            Modifier.fillMaxWidth(if (message.fromCloud) .96f else .82f)
                .then(if (message.imageUri != null) Modifier.shadow(9.dp, RoundedCornerShape(20.dp)) else Modifier)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    when {
                        message.imageUri != null -> colors.surface.copy(alpha = .88f)
                        message.fromCloud -> Color.Transparent
                        else -> colors.surfaceMuted
                    },
                )
                .then(if (message.imageUri != null) Modifier.border(
                    1.5.dp, colors.textSecondary.copy(alpha = .32f), RoundedCornerShape(20.dp)) else Modifier)
                .padding(horizontal = 13.dp, vertical = 10.dp),
        ) {
            message.imageUri?.let { path ->
                val bitmap = rememberLocalImage(path)
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "聊天照片",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp)
                            .clip(RoundedCornerShape(14.dp)),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("仅保存在本机 · 未上传云岸", color = colors.textSecondary, fontSize = 10.sp)
                }
            }
            if (message.imageUri == null || message.text.isNotBlank()) {
                Text(message.text, color = colors.textPrimary, fontSize = 15.sp, lineHeight = 24.sp)
            }
            if (message.status == MessageStatus.FAILED) {
                LiquidGlassButton("发送失败 · 重试", onRetry,
                    height = 38.dp, filled = false, tint = colors.danger)
            }
        }
    }
}

@Composable
private fun AttachmentPreview(path: String, onRemove: () -> Unit) {
    val colors = LocalWatchColors.current
    val bitmap = rememberLocalImage(path)
    Row(
        Modifier.fillMaxWidth().padding(bottom = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceMuted.copy(alpha = .85f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "待发送照片",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)),
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("已选择照片", color = colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text("仅保存在本机 · 未上传云岸", color = colors.textSecondary, fontSize = 11.sp)
        }
        LiquidGlassIconButton(Icons.Rounded.Close, "移除照片", onRemove, size = 38.dp)
    }
}

@Composable
private fun rememberLocalImage(path: String?): ImageBitmap? {
    val context = LocalContext.current
    return remember(path) {
        path?.let {
            runCatching {
                val uri = Uri.parse(it)
                val stream = if (uri.scheme == "file") {
                    File(uri.path!!).inputStream()
                } else {
                    context.contentResolver.openInputStream(uri)
                }
                stream?.use { input -> BitmapFactory.decodeStream(input)?.asImageBitmap() }
            }.getOrNull()
        }
    }
}
