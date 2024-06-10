package choehaualen.breath.presentation.main.productivity.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import choehaualen.breath.R
import choehaualen.breath.core.etc.transformFraction
import choehaualen.breath.core.ui.colors.ProvideBreathColors
import choehaualen.breath.core.ui.colors.SkyBlueColors
import choehaualen.breath.core.ui.components.topbar.TopBarDefaults
import choehaualen.breath.core.ui.components.topbar.basic_topbar.BasicTopBar
import choehaualen.breath.core.ui.theme.BreathTheme
import choehaualen.breath.presentation.main.productivity.ProductivityReminderInterval
import choehaualen.breath.presentation.main.productivity.ProductivityScreenState
import choehaualen.breath.presentation.main.productivity.ProductivityScreenUIAction
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import sv.lib.squircleshape.SquircleShape

@Composable
fun ProductivityScreen(
    screenState: ProductivityScreenState,
    onUIAction: (ProductivityScreenUIAction) -> Unit
) {

    var isScreenShown by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) { isScreenShown = true }

    val gradientAlpha by animateFloatAsState(
        targetValue = if (isScreenShown) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = ""
    )

    val background by rememberUpdatedState(
        Brush.verticalGradient(
            colors = listOf(
                BreathTheme.colors.backgroundGradientStart.copy(gradientAlpha),
                BreathTheme.colors.backgroundGradientEnd.copy(gradientAlpha)
            )
        )
    )

    val scrollBehavior = TopBarDefaults.exitUntilCollapsedScrollBehavior()

    val bgAlpha by remember(scrollBehavior.state.collapsedFraction) {
        derivedStateOf { scrollBehavior.state.collapsedFraction }
    }

    val topBarBgAlpha by remember(bgAlpha) { // this ensures smooth color transition
        derivedStateOf {
            transformFraction(
                value = bgAlpha,
                startX = .8f,
                endX = 1f,
                startY = 0f,
                endY = .5f
            )
        }
    }

    val topBarTitle by remember(scrollBehavior.state.collapsedFraction) {
        derivedStateOf {
            if (scrollBehavior.state.collapsedFraction in .75f..1f)
                "Productivity Reminders" else "Productivity\nReminders"
        }
    }

    val hazeState = remember { HazeState() }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = BreathTheme.colors.background.copy(bgAlpha),
        contentColor = BreathTheme.colors.text,
        topBar = {

            BasicTopBar(
                modifier = Modifier.hazeChild(
                    state = hazeState,
                    style = HazeStyle(
                        tint = BreathTheme.colors.background.copy(topBarBgAlpha),
                        blurRadius = 20.dp
                    )
                ),
                titleText = topBarTitle,
                scrollBehavior = scrollBehavior,
                navigationIcon = {

                    IconButton(
                        onClick = { onUIAction(ProductivityScreenUIAction.NavigateUp) }
                    ) {

                        Icon(
                            modifier = Modifier.size(32.dp),
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = "Navigate back to Home."
                        )

                    }

                },
                backgroundColor = BreathTheme.colors.background.copy(topBarBgAlpha)
            )

        },
        bottomBar = {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .hazeChild(hazeState)
            ) {

                Spacer(modifier = Modifier.navigationBarsPadding())

            }

        }
    ) { insetsPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .haze(hazeState),
            contentPadding = insetsPadding,
        ) {

            reminderItems()

        }

    }

}

private fun LazyListScope.reminderItems(
    // fixme: will fix this later: onUIAction: (ProductivityScreenUIAction) -> Unit
) {

    item(key = "water_intake_reminder") {

        var state by remember {
            mutableStateOf(
                ProductivityScreenReminderState(
                    interval = ProductivityReminderInterval.FortyFiveMinutes
                )
            )
        }

        val onUIAction = remember<(ProductivityScreenReminderUIAction) -> Unit> {
            { uiAction ->

                state = when (uiAction) {

                    is ProductivityScreenReminderUIAction.SetEnabled -> {
                        state.copy(enabled = uiAction.enabled)
                    }

                    is ProductivityScreenReminderUIAction.SetInterval -> {
                        state.copy(interval = uiAction.interval)
                    }

                }

            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ProductivityScreenReminder(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            state = state,
            shape = SquircleShape(
                topStart = 24.dp,
                topEnd = 24.dp,
                bottomEnd = 8.dp,
                bottomStart = 8.dp,
            ),
            icon = painterResource(id = R.drawable.water_glass),
            label = "Water intake",
            description = "Reminds you to drink water\n" +
                    "every interval to stay hydrated",
            onUIAction = onUIAction
        )

    }

    item(key = "read_book_reminder") {

        var state by remember { mutableStateOf(ProductivityScreenReminderState()) }
        val onUIAction = remember<(ProductivityScreenReminderUIAction) -> Unit> {
            { uiAction ->

                state = when (uiAction) {

                    is ProductivityScreenReminderUIAction.SetEnabled -> {
                        state.copy(enabled = uiAction.enabled)
                    }

                    else -> state

                }

            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ProductivityScreenReminder(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            state = state,
            shape = SquircleShape(8.dp),
            icon = painterResource(id = R.drawable.book),
            label = "Read a Book",
            description = "Reading daily enhances brain strength " +
                    "and cognitive abilities.\n" +
                    "Aim for at least 34 minutes\n" +
                    "of reading each day.",
//                    "It's recommended that you should " +
//                    "read a book for at least 34min a day",
            onUIAction = onUIAction
        )

    }

    item(key = "basic_workout_reminder") {

        var state by remember { mutableStateOf(ProductivityScreenReminderState()) }
        val onUIAction = remember<(ProductivityScreenReminderUIAction) -> Unit> {
            { uiAction ->

                state = when (uiAction) {

                    is ProductivityScreenReminderUIAction.SetEnabled -> {
                        state.copy(enabled = uiAction.enabled)
                    }

                    else -> state

                }

            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ProductivityScreenReminder(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            state = state,
            shape = SquircleShape(8.dp),
            icon = painterResource(id = R.drawable.activity),
            label = "Basic Workout",
            description = "For a basic workout, include " +
                    "running, lunges, and stretches, " +
                    "aim for a minimum of 45 minutes.",
            onUIAction = onUIAction
        )

    }

    item(key = "touch_grass_reminder") {

        var state by remember { mutableStateOf(ProductivityScreenReminderState()) }
        val onUIAction = remember<(ProductivityScreenReminderUIAction) -> Unit> {
            { uiAction ->

                state = when (uiAction) {

                    is ProductivityScreenReminderUIAction.SetEnabled -> {
                        state.copy(enabled = uiAction.enabled)
                    }

                    else -> state

                }

            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ProductivityScreenReminder(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            state = state,
            shape = SquircleShape(
                topStart = 8.dp,
                topEnd = 8.dp,
                bottomEnd = 24.dp,
                bottomStart = 24.dp,
            ),
            icon = painterResource(id = R.drawable.touch_grass),
            label = "Touch Grass",
            description = "Breathe recommends short breaks " +
                    "for nature interaction during long " +
                    "work periods to boost mood, oxygen levels, " +
                    "leading to better alertness and less fatigue.",
            onUIAction = onUIAction
        )

    }

    item(key = "bottom_spacer") {
        Spacer(modifier = Modifier.height(512.dp))
    }

}

@Preview
@Composable
private fun ProductivityScreenPreview() = BreathTheme {
    ProvideBreathColors(SkyBlueColors) {
        ProductivityScreen(
            screenState = ProductivityScreenState(),
            onUIAction = {}
        )
    }
}

// Okkayyy, now we need to push the UI and start working on the backend. yes,
// But firstly, lemme fix sth.