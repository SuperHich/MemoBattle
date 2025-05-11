package com.heroapps.memobattle

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heroapps.library.admob.AdMobBanner
import com.heroapps.library.admob.LaunchInterstitialAds
import com.heroapps.library.compose.AudioFeedback
import com.heroapps.library.compose.Difficulty
import com.heroapps.memobattle.AudioFeedbackExtension.loadSounds
import com.heroapps.memobattle.AudioFeedbackExtension.playError
import com.heroapps.memobattle.AudioFeedbackExtension.playFailed
import com.heroapps.memobattle.AudioFeedbackExtension.playMatched
import com.heroapps.memobattle.AudioFeedbackExtension.playSuccess
import com.heroapps.memobattle.components.RenamePlayerDialog
import com.heroapps.memobattle.ui.theme.Blue
import com.heroapps.memobattle.ui.theme.BlueDark
import com.heroapps.memobattle.ui.theme.Red
import com.heroapps.memobattle.ui.theme.RedDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.Serializable
import java.util.Random

data class CardItem(
    val id: Int,
    @DrawableRes val icon: Int,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false
) : Serializable

data class Player(
    val id: Int = Random().nextInt(),
    var name: String,
    var score: Int = 0,
    val color: Color,
    val backgroundColor: Color,
    val isCurrentTurn: Boolean = false
) : Serializable

@Composable
fun GameScreen(difficultyLevel: Difficulty, onBackToMenu: () -> Unit) {
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    LaunchInterstitialAds(context = context, Constants.FULL_PAGE_UNIT_ID)

    val audioFeedback = remember { AudioFeedback(context) }

    // Load sounds when the composable enters composition
    LaunchedEffect(Unit) {
        audioFeedback.loadSounds()
    }

    // Configuration de la difficulté
    val gridSize = when (difficultyLevel) {
        Difficulty.Easy -> Pair(4, 3)    // 12 cartes (6 paires)
        Difficulty.Medium -> Pair(4, 4)  // 16 cartes (8 paires)
        Difficulty.Hard -> Pair(6, 4)    // 24 cartes (12 paires)
    }

    // Préparation des icônes
    val iconList = listOf(
        R.drawable.ic_random_1,
        R.drawable.ic_random_2,
        R.drawable.ic_random_3,
        R.drawable.ic_random_4,
        R.drawable.ic_random_5,
        R.drawable.ic_random_6,
        R.drawable.ic_random_7,
        R.drawable.ic_random_8,
        R.drawable.ic_random_9,
        R.drawable.ic_random_10,
        R.drawable.ic_random_11,
        R.drawable.ic_random_12,
        R.drawable.ic_random_13,
        R.drawable.ic_random_14,
        R.drawable.ic_random_15,
        R.drawable.ic_random_16,
        R.drawable.ic_random_17,
        R.drawable.ic_random_18,
        R.drawable.ic_random_19,
        R.drawable.ic_random_20,
        R.drawable.ic_random_21
    ).shuffled().take(gridSize.first * gridSize.second / 2)

    // Generate cards
    val cards = remember {
        val allIcons = iconList + iconList  // Doubler pour créer des paires
        mutableStateListOf<CardItem>().apply {
            addAll(allIcons.shuffled().mapIndexed { index, icon ->
                CardItem(id = index, icon = icon)
            })
        }
    }

    // Game state
    var player1 by remember {
        mutableStateOf(
            Player(
                name = context.getString(R.string.default_name1),
                color = Blue,
                backgroundColor = BlueDark,
                isCurrentTurn = true
            )
        )
    }
    var player2 by remember {
        mutableStateOf(
            Player(
                name =  context.getString(R.string.default_name2),
                color = Red,
                backgroundColor = RedDark
            )
        )
    }
    var selectedCardIndices by remember { mutableStateOf<List<Int>>(emptyList()) }
    var gameOver by remember { mutableStateOf(false) }

    // Game logic
    fun flipCard(index: Int) {
        if (selectedCardIndices.size >= 2 || cards[index].isFlipped || cards[index].isMatched) {
            return
        }

        // Return card
        cards[index] = cards[index].copy(isFlipped = true)

        // Add to selection
        selectedCardIndices = selectedCardIndices + index

        // When 2nd card selected
        if (selectedCardIndices.size == 2) {
            val firstIndex = selectedCardIndices[0]
            val secondIndex = selectedCardIndices[1]

            // Check if cards match
            if (cards[firstIndex].icon == cards[secondIndex].icon) {
                audioFeedback.playMatched()
                // Match found
                scope.launch {
                    delay(500)  // Small break to show match
                    cards[firstIndex] = cards[firstIndex].copy(isMatched = true)
                    cards[secondIndex] = cards[secondIndex].copy(isMatched = true)

                    // Add point to current player
                    if (player1.isCurrentTurn) {
                        player1 = player1.copy(score = player1.score + 1)
                    } else {
                        player2 = player2.copy(score = player2.score + 1)
                    }

                    selectedCardIndices = emptyList()

                    // Check if game should end
                    if (cards.all { it.isMatched }) {
                        if(player1.score == player2.score) {
                            audioFeedback.playFailed()
                        } else {
                            audioFeedback.playSuccess()
                        }
                        gameOver = true
                    }
                }
            } else {
                // No match found
                scope.launch {
                    delay(250)
                    audioFeedback.playError()

                    delay(750)  // Montrer les cartes pendant un moment

                    // Bring back cards
                    cards[firstIndex] = cards[firstIndex].copy(isFlipped = false)
                    cards[secondIndex] = cards[secondIndex].copy(isFlipped = false)

                    // Switch to other player
                    player1 = player1.copy(isCurrentTurn = !player1.isCurrentTurn)
                    player2 = player2.copy(isCurrentTurn = !player2.isCurrentTurn)

                    selectedCardIndices = emptyList()
                }
            }
        }
    }

    val backgroundColor =
        if (player1.isCurrentTurn) player1.backgroundColor else player2.backgroundColor

    var toRenamePlayer: MutableState<Player?> = remember { mutableStateOf(null) }

    Column(
        modifier = Modifier
            .background(backgroundColor)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Game header with scores and current player
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Scores
            Row {
                PlayerScore(player = player1) {
                    toRenamePlayer.value = player1
                }
                Spacer(modifier = Modifier.width(16.dp))
                PlayerScore(player = player2) {
                    toRenamePlayer.value = player2
                }
            }

            // Back button
            IconButton(onClick = onBackToMenu) {
                Icon(Icons.Default.Close, contentDescription = "Retour au menu")
            }
        }

        // Player's tour
        Text(
            text = stringResource(R.string.tourOf, if (player1.isCurrentTurn) player1.name else player2.name),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Game grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(gridSize.first),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
        ) {
            items(cards.size) { index ->
                MemoryCard(
                    card = cards[index],
                    onClick = { flipCard(index) }
                )
            }
        }

        AdMobBanner(
            adUnitId = Constants.BANNER_UNIT_ID
        )

        // Dialog of the end
        if (gameOver) {
            val resultTitle = when {
                player1.score > player2.score -> stringResource(R.string.result_winner_is, player1.name)
                player2.score > player1.score ->  stringResource(R.string.result_winner_is, player2.name)
                else -> stringResource(R.string.result_draw)
            }

            AlertDialog(
                onDismissRequest = { },
                title = { Text(resultTitle) },
                text = {
                    Column {
                        Text(stringResource(R.string.results))
                        Text(stringResource(R.string.result_details, player1.name, player1.score))
                        Text(stringResource(R.string.result_details, player2.name, player2.score))
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        gameOver = false
                        onBackToMenu()
                    }) {
                        Text(stringResource(R.string.exit))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            // Reinit the game
                            gameOver = false
                            player1 = player1.copy(score = 0)
                            player2 = player2.copy(score = 0)
                            selectedCardIndices = emptyList()

                            // Shuffle and reinit cards
                            val allIcons = iconList + iconList
                            for (i in cards.indices) {
                                cards[i] = CardItem(
                                    id = i,
                                    icon = allIcons[i]
                                )
                            }
                            cards.shuffle()
                        }
                    ) {
                        Text(stringResource(R.string.restart))
                    }
                }
            )
        }

        if(toRenamePlayer.value != null) {
            RenamePlayerDialog(
                defaultName = toRenamePlayer.value!!.name,
                onDismiss = {
                    toRenamePlayer.value = null
                }
            ) { newName ->
                when {
                    toRenamePlayer.value!!.id == player1.id -> player1.name = newName
                    else -> player2.name = newName
                }
                toRenamePlayer.value = null
            }
        }
    }
}

@Composable
fun PlayerScore(player: Player, onRenameRequested: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(
                if (player.isCurrentTurn) player.color else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            ).clickable(onClick = {
                onRenameRequested()
            })
            .padding(8.dp)
    ) {
        Text(
            text = player.name,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "${player.score}",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}

@Composable
fun MemoryCard(card: CardItem, onClick: () -> Unit) {
    val rotation = animateFloatAsState(
        targetValue = if (card.isFlipped) 360f else 0f
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .aspectRatio(1f)
            .rotate(rotation.value)
            .clip(MaterialTheme.shapes.medium)
            .border(
                BorderStroke(
                    2.dp,
                    when {
                        card.isMatched -> MaterialTheme.colorScheme.tertiary
                        card.isFlipped -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outline
                    }
                ),
                shape = MaterialTheme.shapes.medium
            )
            .background(
                when {
                    card.isMatched -> MaterialTheme.colorScheme.tertiaryContainer
                    card.isFlipped -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .clickable(enabled = !card.isMatched && !card.isFlipped) { onClick() }
    ) {
        if (card.isFlipped || card.isMatched) {
            Image(
                painter = painterResource(card.icon),
                modifier = Modifier.size(48.dp),
                contentDescription = null
            )
        }
    }
}