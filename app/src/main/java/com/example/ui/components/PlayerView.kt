package com.example.ui.components

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.Channel
import com.example.ui.theme.XtvAccent
import com.example.ui.theme.XtvBackground
import com.example.ui.theme.XtvBorder
import com.example.ui.theme.XtvError
import com.example.ui.theme.XtvLiveGreen
import com.example.ui.theme.XtvSurface
import com.example.ui.theme.XtvSurfaceElevated
import com.example.ui.theme.XtvTextPrimary
import com.example.ui.theme.XtvTextSecondary
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun FullscreenPlayer(
    channel: Channel,
    channelList: List<Channel>,
    isFavorite: Boolean,
    sleepTimerMinutes: Int?,
    sleepTimerSecondsRemaining: Int?,
    onClose: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSelectChannel: (Channel) -> Unit,
    onFavoriteToggle: () -> Unit,
    onMarkFailed: () -> Unit,
    onSetSleepTimer: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var isControlsVisible by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var playbackError by remember { mutableStateOf<String?>(null) }
    var isMuted by remember { mutableStateOf(false) }
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var showQuickDrawer by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }

    // Intercept back button to close player
    BackHandler {
        if (showQuickDrawer) {
            showQuickDrawer = false
        } else {
            onClose()
        }
    }

    // Initialize ExoPlayer with robust HTTP DataSource
    val exoPlayer = remember {
        val httpDataSourceFactory = androidx.media3.datasource.DefaultHttpDataSource.Factory()
            .setUserAgent("XTV-Android/1.0")
            .setConnectTimeoutMs(8000)
            .setReadTimeoutMs(10000)
            .setAllowCrossProtocolRedirects(true)
            .setKeepPostFor302Redirects(true)

        val dataSourceFactory = androidx.media3.datasource.DefaultDataSource.Factory(context, httpDataSourceFactory)
        val mediaSourceFactory = androidx.media3.exoplayer.source.DefaultMediaSourceFactory(dataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                playWhenReady = true
            }
    }

    // Auto-hide controls after 3.8 seconds of user inactivity
    LaunchedEffect(isControlsVisible, isPlaying) {
        if (isControlsVisible && isPlaying && playbackError == null) {
            delay(3800)
            isControlsVisible = false
        }
    }

    // Prepare media item whenever channel changes
    LaunchedEffect(channel.streamUrl) {
        playbackError = null
        isBuffering = true
        try {
            val mediaItem = MediaItem.fromUri(channel.streamUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.repeatMode = if (channel.streamUrl.startsWith("android.resource")) {
                Player.REPEAT_MODE_ONE
            } else {
                Player.REPEAT_MODE_OFF
            }
            exoPlayer.prepare()
            exoPlayer.play()
        } catch (e: Exception) {
            isBuffering = false
            playbackError = "تعذر تحميل رابط البث: ${e.message}"
        }
    }

    // ExoPlayer event listener
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> isBuffering = true
                    Player.STATE_READY -> {
                        isBuffering = false
                        playbackError = null
                    }
                    Player.STATE_ENDED -> isBuffering = false
                    Player.STATE_IDLE -> Unit
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                val cause = error.cause
                val isNoNetwork = cause is java.net.UnknownHostException ||
                        cause?.cause is java.net.UnknownHostException ||
                        error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ||
                        error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ||
                        error.message?.contains("UnknownHostException", ignoreCase = true) == true

                playbackError = if (isNoNetwork) {
                    "لا يتوفر اتصال بالإنترنت أو تعذر الوصول لسيرفر القناة حالياً."
                } else if (channel.streamUrl.startsWith("http://") && !channel.streamUrl.startsWith("https://")) {
                    "قد يكون البث محظوراً لاشتراط تشفير HTTPS، أو أن خادم القناة غير مستجيب."
                } else {
                    "تعذر تشغيل هذا البث المباشر (${error.errorCodeName}). قد يكون البث متوقفاً من المصدر."
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.stop()
            exoPlayer.release()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isControlsVisible = !isControlsVisible
            }
            .testTag("fullscreen_player")
    ) {
        // ExoPlayer Surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    this.resizeMode = resizeMode
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                playerView.resizeMode = resizeMode
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading spinner when buffering
        if (isBuffering && playbackError == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = XtvAccent,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(54.dp)
                )
            }
        }

        // Error Overlay
        if (playbackError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(XtvBackground.copy(alpha = 0.92f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = XtvError,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "عذراً، انقطع البث المباشر",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = XtvTextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = playbackError ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = XtvTextSecondary
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                playbackError = null
                                isBuffering = true
                                exoPlayer.prepare()
                                exoPlayer.play()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = XtvAccent)
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إعادة المحاولة")
                        }

                        Button(
                            onClick = onNext,
                            colors = ButtonDefaults.buttonColors(containerColor = XtvSurfaceElevated)
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("القناة التالية")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            playbackError = null
                            isBuffering = true
                            val localUri = "android.resource://${context.packageName}/raw/xtv_sample"
                            exoPlayer.setMediaItem(MediaItem.fromUri(localUri))
                            exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
                            exoPlayer.prepare()
                            exoPlayer.play()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = XtvAccent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تشغيل بث المعاينة التجريبي (أوفلاين)", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = onMarkFailed) {
                        Text("تعليم كقناة لا تعمل", color = XtvTextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }

        // Custom Overlay Controls
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back / Close Button + Channel Name
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(XtvSurface.copy(alpha = 0.8f))
                                .testTag("player_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق المشغل",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = channel.name,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            )
                            Text(
                                text = "${channel.category} • ${channel.resolution ?: "HD"}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = XtvTextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // Top Action Buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Sleep Timer Button
                        IconButton(
                            onClick = { showTimerDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (sleepTimerMinutes != null) XtvAccent.copy(alpha = 0.25f) else XtvSurface.copy(alpha = 0.7f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "مؤقت النوم",
                                tint = if (sleepTimerMinutes != null) XtvAccent else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Share Button
                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "قناة ${channel.name} على XTV")
                                    putExtra(Intent.EXTRA_TEXT, "أشاهد الآن قناة ${channel.name} على تطبيق XTV الفاخر:\n${channel.streamUrl}")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "مشاركة القناة"))
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(XtvSurface.copy(alpha = 0.7f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "مشاركة",
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Copy Link
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("XTV Stream", channel.streamUrl))
                                Toast.makeText(context, "تم نسخ رابط البث", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(XtvSurface.copy(alpha = 0.7f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "نسخ الرابط",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Center Navigation Controls
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Channel
                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(XtvSurface.copy(alpha = 0.75f))
                            .border(1.dp, XtvBorder, CircleShape)
                            .testTag("player_prev_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "القناة السابقة",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    // Play / Pause
                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                exoPlayer.pause()
                            } else {
                                exoPlayer.play()
                            }
                        },
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(XtvAccent)
                            .testTag("player_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    // Next Channel
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(XtvSurface.copy(alpha = 0.75f))
                            .border(1.dp, XtvBorder, CircleShape)
                            .testTag("player_next_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "القناة التالية",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                // Bottom Controls Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Live Status Pill & Sleep timer indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(XtvLiveGreen.copy(alpha = 0.2f))
                                .border(1.dp, XtvLiveGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(XtvLiveGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "بث مباشر",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = XtvLiveGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        if (sleepTimerSecondsRemaining != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            val mins = sleepTimerSecondsRemaining / 60
                            val secs = sleepTimerSecondsRemaining % 60
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(XtvSurface.copy(alpha = 0.85f))
                                    .border(1.dp, XtvAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "نوم: %02d:%02d".format(mins, secs),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = XtvAccent,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    // Bottom Right Tools: Quick Drawer, Aspect Ratio, Screen Rotate, Mute
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Quick Channels Drawer Button
                        IconButton(
                            onClick = { showQuickDrawer = !showQuickDrawer },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (showQuickDrawer) XtvAccent else XtvSurface.copy(alpha = 0.7f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatListBulleted,
                                contentDescription = "قائمة القنوات السريعة",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Aspect Ratio Toggle (Fit vs Zoom)
                        IconButton(
                            onClick = {
                                resizeMode = if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                } else {
                                    AspectRatioFrameLayout.RESIZE_MODE_FIT
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(XtvSurface.copy(alpha = 0.7f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.AspectRatio,
                                contentDescription = "تناسب الشاشة",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Screen Rotation
                        IconButton(
                            onClick = {
                                activity?.let { act ->
                                    val current = act.requestedOrientation
                                    act.requestedOrientation = if (current == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                    } else {
                                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(XtvSurface.copy(alpha = 0.7f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ScreenRotation,
                                contentDescription = "تدوير الشاشة",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Mute / Unmute
                        IconButton(
                            onClick = {
                                isMuted = !isMuted
                                exoPlayer.volume = if (isMuted) 0f else 1f
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(XtvSurface.copy(alpha = 0.7f))
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                contentDescription = if (isMuted) "إلغاء الكتم" else "كتم الصوت",
                                tint = if (isMuted) XtvAccent else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Channel Drawer (Side / Overlay List)
        if (showQuickDrawer) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .align(Alignment.CenterEnd)
                    .background(XtvSurface.copy(alpha = 0.96f))
                    .border(1.dp, XtvBorder)
                    .statusBarsPadding()
                    .padding(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "القنوات (${channelList.size})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = XtvTextPrimary
                            )
                        )
                        IconButton(onClick = { showQuickDrawer = false }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = XtvTextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(channelList.take(60), key = { it.id + it.streamUrl }) { ch ->
                            val isCurrent = ch.id == channel.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isCurrent) XtvAccent.copy(alpha = 0.2f) else XtvSurfaceElevated)
                                    .border(
                                        1.dp,
                                        if (isCurrent) XtvAccent else XtvBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        onSelectChannel(ch)
                                        showQuickDrawer = false
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(XtvSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ChannelMonogram(name = ch.name, modifier = Modifier.size(24.dp))
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ch.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isCurrent) XtvAccent else XtvTextPrimary
                                        ),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = ch.category,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = XtvTextSecondary,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sleep Timer Dialog
        if (showTimerDialog) {
            AlertDialog(
                onDismissRequest = { showTimerDialog = false },
                containerColor = XtvSurface,
                titleContentColor = XtvTextPrimary,
                textContentColor = XtvTextSecondary,
                title = {
                    Text(
                        text = "مؤقت النوم",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "اختر مدة إيقاف تشغيل البث تلقائياً:",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        listOf(15, 30, 60).forEach { mins ->
                            Button(
                                onClick = {
                                    onSetSleepTimer(mins)
                                    showTimerDialog = false
                                    Toast.makeText(context, "تم ضبط مؤقت النوم على $mins دقيقة", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (sleepTimerMinutes == mins) XtvAccent else XtvSurfaceElevated
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("$mins دقيقة", color = Color.White)
                            }
                        }

                        if (sleepTimerMinutes != null) {
                            OutlinedButton(
                                onClick = {
                                    onSetSleepTimer(null)
                                    showTimerDialog = false
                                    Toast.makeText(context, "تم إلغاء مؤقت النوم", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("إلغاء المؤقت", color = XtvError)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showTimerDialog = false }) {
                        Text("إغلاق", color = XtvAccent)
                    }
                }
            )
        }
    }
}
