package me.ash.reader.ui.page.home.reading

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.infrastructure.android.TextToSpeechManager
import me.ash.reader.infrastructure.preference.LocalPullToSwitchArticle
import me.ash.reader.infrastructure.preference.LocalReadingAutoHideToolbar
import me.ash.reader.infrastructure.preference.LocalReadingBoldCharacters
import me.ash.reader.infrastructure.preference.LocalReadingColorTheme
import me.ash.reader.infrastructure.preference.LocalReadingHorizontalScroll
import me.ash.reader.infrastructure.preference.ReadingColorThemePreference
import me.ash.reader.infrastructure.preference.LocalReadingPullDownAction
import me.ash.reader.infrastructure.preference.LocalReadingPullUpAction
import me.ash.reader.infrastructure.preference.ReadingPullDownActionPreference
import me.ash.reader.infrastructure.preference.ReadingPullUpActionPreference
import me.ash.reader.infrastructure.preference.LocalReadingTextLineHeight
import me.ash.reader.infrastructure.preference.not
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.showToast
import me.ash.reader.ui.page.adaptive.ArticleListReaderViewModel
import me.ash.reader.ui.page.adaptive.NavigationAction
import me.ash.reader.ui.page.adaptive.ReaderState
import me.ash.reader.ui.page.home.reading.tts.TtsButton

private const val UPWARD = 1
private const val DOWNWARD = -1

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun ReadingPage(
    //    navController: NavHostController,
    viewModel: ArticleListReaderViewModel,
    navigationAction: NavigationAction,
    onLoadArticle: (String, Int) -> Unit,
    onNavAction: (NavigationAction) -> Unit,
    onNavigateToStylePage: () -> Unit,
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val isHorizontalScrollEnabled = LocalReadingHorizontalScroll.current.value
    val pullDownAction = LocalReadingPullDownAction.current
    val pullUpAction = LocalReadingPullUpAction.current
    val readingUiState = viewModel.readingUiState.collectAsStateValue()
    val readerState = viewModel.readerStateStateFlow.collectAsStateValue()
    val summaryState = viewModel.summaryState.collectAsStateValue()
    val boldCharacters = LocalReadingBoldCharacters.current
    val coroutineScope = rememberCoroutineScope()

    var isReaderScrollingDown by remember { mutableStateOf(false) }
    var showFullScreenImageViewer by remember { mutableStateOf(false) }

    var currentImageData by remember { mutableStateOf(ImageData()) }

    val isShowToolBar =
        if (LocalReadingAutoHideToolbar.current.value) {
            readerState.articleId != null && !isReaderScrollingDown
        } else {
            true
        }

    var showTopDivider by remember { mutableStateOf(false) }

    //    LaunchedEffect(readerState.listIndex) {
    //        readerState.listIndex?.let {
    //            navController.previousBackStackEntry?.savedStateHandle?.set("articleIndex", it)
    //        }
    //    }

    var bringToTop by remember { mutableStateOf(false) }
    val readingColorTheme = LocalReadingColorTheme.current
    val currentTheme = MaterialTheme.colorScheme
    val readingColorScheme = remember(readingColorTheme, currentTheme) {
        readingColorTheme.getColorScheme(currentTheme)
    }

    MaterialTheme(colorScheme = readingColorScheme) {

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        content = { paddings ->
            Box(modifier = Modifier.fillMaxSize()) {
                if (readerState.articleId != null) {
                    TopBar(
                        isShow = isShowToolBar,
                        isScrolled = showTopDivider,
                        title = readerState.title,
                        link = readerState.link,
                        onClick = { bringToTop = true },
                        navigationAction = navigationAction,
                        onNavButtonClick = onNavAction,
                        onNavigateToStylePage = onNavigateToStylePage,
                    )
                }

                val isNextArticleAvailable = readerState.nextArticle != null
                val isPreviousArticleAvailable = readerState.previousArticle != null

                if (readerState.articleId != null) {
                    // Content
                    AnimatedContent(
                        targetState = readerState,
                        transitionSpec = {
                            val isHorizontal = isHorizontalScrollEnabled
                            val direction =
                                when {
                                    initialState.nextArticle?.articleId == targetState.articleId ->
                                        UPWARD
                                    initialState.previousArticle?.articleId ==
                                        targetState.articleId -> DOWNWARD
                                    initialState.articleId == targetState.articleId -> {
                                        when (targetState.content) {
                                            is ReaderState.Description -> DOWNWARD
                                            else -> UPWARD
                                        }
                                    }

                                    else -> UPWARD
                                }
                            val exit = 100
                            val enter = exit * 2
                            if (isHorizontal) {
                                if (initialState.articleId == targetState.articleId) {
                                    if (initialState.content == targetState.content) {
                                        EnterTransition.None togetherWith ExitTransition.None
                                    } else {
                                        fadeIn(tween(150)) togetherWith fadeOut(tween(150))
                                    }
                                } else {
                                    val slideDirection = if (direction == UPWARD) 1 else -1
                                    slideInHorizontally(
                                        initialOffsetX = { it * slideDirection },
                                        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                                    ) togetherWith
                                        slideOutHorizontally(
                                            targetOffsetX = { -it * slideDirection },
                                            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                                        )
                                }
                            } else {
                                (slideInVertically(
                                    initialOffsetY = { (it * 0.2f * direction).toInt() },
                                    animationSpec =
                                        spring(
                                            dampingRatio = .9f,
                                            stiffness = Spring.StiffnessLow,
                                            visibilityThreshold = IntOffset.VisibilityThreshold,
                                        ),
                                ) +
                                    fadeIn(
                                        tween(
                                            delayMillis = exit,
                                            durationMillis = enter,
                                            easing = LinearOutSlowInEasing,
                                        )
                                    )) togetherWith
                                    (slideOutVertically(
                                        targetOffsetY = { (it * -0.2f * direction).toInt() },
                                        animationSpec =
                                            spring(
                                                dampingRatio = Spring.DampingRatioNoBouncy,
                                                stiffness = Spring.StiffnessLow,
                                                visibilityThreshold = IntOffset.VisibilityThreshold,
                                            ),
                                    ) +
                                        fadeOut(
                                            tween(durationMillis = exit, easing = FastOutLinearInEasing)
                                        ))
                            }
                        },
                        label = "",
                    ) {
                        remember { it }
                            .run {
                                val isPullToLoadEnabled =
                                    pullDownAction !is ReadingPullDownActionPreference.None ||
                                        pullUpAction !is ReadingPullUpActionPreference.None

                                val canLoadPrevious =
                                    when (pullDownAction) {
                                        is ReadingPullDownActionPreference.None -> false
                                        is ReadingPullDownActionPreference.PreviousArticle -> isPreviousArticleAvailable
                                        else -> true
                                    }

                                val canLoadNext =
                                    when (pullUpAction) {
                                        is ReadingPullUpActionPreference.None -> false
                                        is ReadingPullUpActionPreference.NextArticle -> isNextArticleAvailable
                                        else -> true
                                    }

                                val previousIcon =
                                    when (pullDownAction) {
                                        is ReadingPullDownActionPreference.AiSummary -> Icons.Rounded.AutoAwesome
                                        is ReadingPullDownActionPreference.FullContent -> Icons.AutoMirrored.Rounded.Article
                                        is ReadingPullDownActionPreference.PreviousArticle -> Icons.Rounded.KeyboardArrowUp
                                        else -> Icons.Rounded.KeyboardArrowUp
                                    }

                                val nextIcon =
                                    when (pullUpAction) {
                                        is ReadingPullUpActionPreference.AiSummary -> Icons.Rounded.AutoAwesome
                                        is ReadingPullUpActionPreference.FullContent -> Icons.AutoMirrored.Rounded.Article
                                        is ReadingPullUpActionPreference.NextArticle -> Icons.Rounded.KeyboardArrowDown
                                        else -> Icons.Rounded.KeyboardArrowDown
                                    }

                                val state =
                                    rememberPullToLoadState(
                                        key = content,
                                        onLoadPrevious =
                                            when (pullDownAction) {
                                                is ReadingPullDownActionPreference.None -> null
                                                is ReadingPullDownActionPreference.AiSummary -> {
                                                    { viewModel.summarizeArticle() }
                                                }
                                                is ReadingPullDownActionPreference.FullContent -> {
                                                    {
                                                        val isAlreadyFull = readerState.content is ReaderState.FullContent
                                                        viewModel.renderFullContent(forceFetch = isAlreadyFull)
                                                    }
                                                }
                                                is ReadingPullDownActionPreference.PreviousArticle -> {
                                                    if (isPreviousArticleAvailable) {
                                                        {
                                                            readerState.previousArticle?.let { (id, index) ->
                                                                onLoadArticle(id, index)
                                                            }
                                                        }
                                                    } else null
                                                }
                                            },
                                        onLoadNext =
                                            when (pullUpAction) {
                                                is ReadingPullUpActionPreference.None -> null
                                                is ReadingPullUpActionPreference.AiSummary -> {
                                                    { viewModel.summarizeArticle() }
                                                }
                                                is ReadingPullUpActionPreference.FullContent -> {
                                                    {
                                                        val isAlreadyFull = readerState.content is ReaderState.FullContent
                                                        viewModel.renderFullContent(forceFetch = isAlreadyFull)
                                                    }
                                                }
                                                is ReadingPullUpActionPreference.NextArticle -> {
                                                    if (isNextArticleAvailable) {
                                                        {
                                                            readerState.nextArticle?.let { (id, index) ->
                                                                onLoadArticle(id, index)
                                                            }
                                                        }
                                                    } else null
                                                }
                                            },
                                    )

                                val listState =
                                    rememberSaveable(
                                        inputs = arrayOf(content),
                                        saver = LazyListState.Saver,
                                    ) {
                                        LazyListState()
                                    }

                                val scrollState = rememberScrollState()

                                val scope = rememberCoroutineScope()

                                val density = LocalDensity.current
                                val configuration = LocalConfiguration.current
                                val screenWidthPx = remember(configuration, density) {
                                    with(density) { configuration.screenWidthDp.dp.toPx() }
                                }
                                val swipeThresholdPx = remember(density) { with(density) { 64.dp.toPx() } }
                                var dragOffset by remember { mutableFloatStateOf(0f) }
                                var hasHapticTriggered by remember { mutableStateOf(false) }
                                var animJob by remember { mutableStateOf<Job?>(null) }

                                val draggableState = rememberDraggableState { delta ->
                                    val canGoForward = isNextArticleAvailable
                                    val canGoBackward = isPreviousArticleAvailable
                                    val resistance = when {
                                        delta < 0 && dragOffset <= 0 && !canGoForward -> 0.18f
                                        delta > 0 && dragOffset >= 0 && !canGoBackward -> 0.18f
                                        else -> 0.85f
                                    }
                                    dragOffset += delta * resistance

                                    if (!hasHapticTriggered) {
                                        if ((dragOffset <= -swipeThresholdPx && canGoForward) ||
                                            (dragOffset >= swipeThresholdPx && canGoBackward)
                                        ) {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                            hasHapticTriggered = true
                                        }
                                    } else {
                                        if (abs(dragOffset) < swipeThresholdPx * 0.7f) {
                                            hasHapticTriggered = false
                                        }
                                    }
                                }

                                LaunchedEffect(bringToTop) {
                                    if (bringToTop) {
                                        scope
                                            .launch {
                                                if (scrollState.value != 0) {
                                                    scrollState.animateScrollTo(0)
                                                } else if (listState.firstVisibleItemIndex != 0) {
                                                    listState.animateScrollToItem(0)
                                                }
                                            }
                                            .invokeOnCompletion { bringToTop = false }
                                    }
                                }

                                showTopDivider =
                                    snapshotFlow {
                                            scrollState.value >= 120 ||
                                                listState.firstVisibleItemIndex != 0
                                        }
                                        .collectAsStateValue(initial = false)

                                CompositionLocalProvider(
                                    LocalTextStyle provides
                                        LocalTextStyle.current.run {
                                            merge(
                                                lineHeight =
                                                    if (lineHeight.isSpecified)
                                                        (lineHeight.value *
                                                                LocalReadingTextLineHeight.current)
                                                            .sp
                                                    else TextUnit.Unspecified
                                            )
                                        }
                                ) {
                                    val progress = (dragOffset / screenWidthPx).coerceIn(-1f, 1f)
                                    val absProgress = abs(progress)
                                    val activationProgress = (abs(dragOffset) / swipeThresholdPx).coerceIn(0f, 1f)
                                    val cardScale = 1f - (absProgress * 0.08f)
                                    val cardCornerRadius = (activationProgress * 28f).dp
                                    val cardElevation = if (absProgress > 0.005f) (activationProgress * 10f).dp else 0.dp

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        // Predictive preview underneath
                                        if (isHorizontalScrollEnabled && abs(dragOffset) > 2f) {
                                            if (dragOffset > 0f) {
                                                val isReady = dragOffset >= swipeThresholdPx && isPreviousArticleAvailable
                                                val previewAlpha = (activationProgress * 1.5f).coerceIn(0f, 1f)
                                                val pillScale = 0.85f + (activationProgress * 0.15f)
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(start = 20.dp, end = 40.dp)
                                                        .graphicsLayer {
                                                            alpha = previewAlpha
                                                            scaleX = pillScale
                                                            scaleY = pillScale
                                                        },
                                                    contentAlignment = Alignment.CenterStart,
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(20.dp),
                                                        color = if (isReady) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                                        tonalElevation = 3.dp,
                                                        shadowElevation = 2.dp,
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                                                contentDescription = null,
                                                                tint = if (isReady) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            )
                                                            Column(modifier = Modifier.widthIn(max = 200.dp)) {
                                                                Text(
                                                                    text = if (isPreviousArticleAvailable) "Previous Article" else "No Previous Article",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    color = (if (isReady) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f),
                                                                )
                                                                if (isPreviousArticleAvailable && !readerState.previousArticle?.title.isNullOrBlank()) {
                                                                    Text(
                                                                        text = readerState.previousArticle?.title ?: "",
                                                                        style = MaterialTheme.typography.titleSmall,
                                                                        color = if (isReady) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                        maxLines = 1,
                                                                        overflow = TextOverflow.Ellipsis,
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                val isReady = dragOffset <= -swipeThresholdPx && isNextArticleAvailable
                                                val previewAlpha = (activationProgress * 1.5f).coerceIn(0f, 1f)
                                                val pillScale = 0.85f + (activationProgress * 0.15f)
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(start = 40.dp, end = 20.dp)
                                                        .graphicsLayer {
                                                            alpha = previewAlpha
                                                            scaleX = pillScale
                                                            scaleY = pillScale
                                                        },
                                                    contentAlignment = Alignment.CenterEnd,
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(20.dp),
                                                        color = if (isReady) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                                        tonalElevation = 3.dp,
                                                        shadowElevation = 2.dp,
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                        ) {
                                                            Column(
                                                                modifier = Modifier.widthIn(max = 200.dp),
                                                                horizontalAlignment = Alignment.End,
                                                            ) {
                                                                Text(
                                                                    text = if (isNextArticleAvailable) "Next Article" else "No Next Article",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    color = (if (isReady) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f),
                                                                )
                                                                if (isNextArticleAvailable && !readerState.nextArticle?.title.isNullOrBlank()) {
                                                                    Text(
                                                                        text = readerState.nextArticle?.title ?: "",
                                                                        style = MaterialTheme.typography.titleSmall,
                                                                        color = if (isReady) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                        maxLines = 1,
                                                                        overflow = TextOverflow.Ellipsis,
                                                                    )
                                                                }
                                                            }
                                                            Icon(
                                                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                                                contentDescription = null,
                                                                tint = if (isReady) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        Content(
                                            modifier =
                                                Modifier
                                                    .then(
                                                        if (isHorizontalScrollEnabled) {
                                                            Modifier
                                                                .graphicsLayer {
                                                                    translationX = dragOffset
                                                                    scaleX = cardScale
                                                                    scaleY = cardScale
                                                                    shadowElevation = cardElevation.toPx()
                                                                    shape = RoundedCornerShape(cardCornerRadius)
                                                                    clip = absProgress > 0.005f
                                                                }
                                                                .background(
                                                                    color = MaterialTheme.colorScheme.surface,
                                                                    shape = RoundedCornerShape(cardCornerRadius),
                                                                )
                                                                .draggable(
                                                                    state = draggableState,
                                                                    orientation = Orientation.Horizontal,
                                                                    enabled = isHorizontalScrollEnabled,
                                                                    onDragStarted = {
                                                                        animJob?.cancel()
                                                                        animJob = null
                                                                        hasHapticTriggered = false
                                                                    },
                                                                    onDragStopped = { velocity ->
                                                                        val shouldLoadNext =
                                                                            (dragOffset <= -swipeThresholdPx || (dragOffset < 0 && velocity < -800f)) &&
                                                                                isNextArticleAvailable
                                                                        val shouldLoadPrevious =
                                                                            (dragOffset >= swipeThresholdPx || (dragOffset > 0 && velocity > 800f)) &&
                                                                                isPreviousArticleAvailable

                                                                        if (shouldLoadNext) {
                                                                            if (!hasHapticTriggered) {
                                                                                hapticFeedback.performHapticFeedback(
                                                                                    HapticFeedbackType.GestureThresholdActivate
                                                                                )
                                                                            }
                                                                            readerState.nextArticle?.let { (id, index) ->
                                                                                onLoadArticle(id, index)
                                                                            }
                                                                        } else if (shouldLoadPrevious) {
                                                                            if (!hasHapticTriggered) {
                                                                                hapticFeedback.performHapticFeedback(
                                                                                    HapticFeedbackType.GestureThresholdActivate
                                                                                )
                                                                            }
                                                                            readerState.previousArticle?.let { (id, index) ->
                                                                                onLoadArticle(id, index)
                                                                            }
                                                                        } else {
                                                                            animJob =
                                                                                scope.launch {
                                                                                    animate(
                                                                                        initialValue = dragOffset,
                                                                                        targetValue = 0f,
                                                                                        initialVelocity = velocity,
                                                                                        animationSpec =
                                                                                            spring(
                                                                                                dampingRatio =
                                                                                                    Spring.DampingRatioMediumBouncy,
                                                                                                stiffness =
                                                                                                    Spring.StiffnessMediumLow,
                                                                                            ),
                                                                                    ) { value, _ ->
                                                                                        dragOffset = value
                                                                                    }
                                                                                    dragOffset = 0f
                                                                                    hasHapticTriggered = false
                                                                                }
                                                                        }
                                                                    },
                                                                )
                                                        } else {
                                                            Modifier
                                                        }
                                                    )
                                                    .pullToLoad(
                                                        state = state,
                                                        onScroll = { f ->
                                                            if (abs(f) > 2f)
                                                                isReaderScrollingDown = f < 0f
                                                        },
                                                        enabled = isPullToLoadEnabled,
                                                    ),
                                            contentPadding = paddings,
                                            content = content.text ?: "",
                                            feedName = feedName,
                                            title = title.toString(),
                                            author = author,
                                            link = link,
                                            publishedDate = publishedDate,
                                            isLoading = content is ReaderState.Loading,
                                            scrollState = scrollState,
                                            listState = listState,
                                            summaryState = summaryState,
                                            onSummarize = { viewModel.summarizeArticle() },
                                            onDismissSummary = { viewModel.dismissSummary() },
                                            onImageClick = { imgUrl, altText ->
                                                currentImageData = ImageData(imgUrl, altText)
                                                showFullScreenImageViewer = true
                                            },
                                        )
                                        if (isPullToLoadEnabled) {
                                            PullToLoadIndicator(
                                                state = state,
                                                canLoadPrevious = canLoadPrevious,
                                                canLoadNext = canLoadNext,
                                                previousIcon = previousIcon,
                                                nextIcon = nextIcon,
                                            )
                                        }
                                    }
                                }
                            }
                    }
                }
                // Bottom Bar
                if (readerState.articleId != null) {
                    BottomBar(
                        isShow = isShowToolBar,
                        isUnread = readingUiState.isUnread,
                        isStarred = readingUiState.isStarred,
                        isNextArticleAvailable = isNextArticleAvailable,
                        isFullContent =
                            readerState.content is ReaderState.FullContent ||
                                readerState.content is ReaderState.Error,
                        isBoldCharacters = boldCharacters.value,
                        onUnread = { viewModel.updateReadStatus(it) },
                        onStarred = { viewModel.updateStarredStatus(it) },
                        onNextArticle = {
                            readerState.nextArticle?.let {
                                val (id, index) = it
                                onLoadArticle(id, index)
                            }
                        },
                        onFullContent = {
                            if (it) viewModel.renderFullContent()
                            else viewModel.renderDescriptionContent()
                        },
                        onBoldCharacters = { (!boldCharacters).put(context, coroutineScope) },
                        onReadAloud = {
                            viewModel.textToSpeechManager.readHtml(
                                readerState.content.text ?: return@BottomBar
                            )
                        },
                        onSummarize = { viewModel.summarizeArticle() },
                        ttsButton = {
                            TtsButton(
                                onClick = {
                                    when (it) {
                                        TextToSpeechManager.State.Error -> {
                                            context.showToast("TextToSpeech initialization failed")
                                        }

                                        TextToSpeechManager.State.Idle -> {
                                            viewModel.textToSpeechManager.readHtml(
                                                readerState.content.text ?: ""
                                            )
                                        }

                                        is TextToSpeechManager.State.Reading -> {
                                            viewModel.textToSpeechManager.stop()
                                        }

                                        TextToSpeechManager.State.Preparing -> {
                                            /* no-op */
                                        }
                                    }
                                },
                                state =
                                    viewModel.textToSpeechManager.stateFlow.collectAsStateValue(),
                            )
                        },
                    )
                }
            }
        },
    )
    }
    if (showFullScreenImageViewer) {

        ReaderImageViewer(
            imageData = currentImageData,
            onDownloadImage = {
                viewModel.downloadImage(
                    it,
                    onSuccess = { context.showToast(context.getString(R.string.image_saved)) },
                    onFailure = {
                        // FIXME: crash the app for error report
                        th ->
                        throw th
                    },
                )
            },
            onDismissRequest = { showFullScreenImageViewer = false },
        )
    }
}
