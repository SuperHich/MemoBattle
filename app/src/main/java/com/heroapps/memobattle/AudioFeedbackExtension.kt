package com.heroapps.memobattle

import com.heroapps.library.compose.AudioFeedback

/**
 * A utility class for playing different types of audio feedback in Android Compose applications
 */
object AudioFeedbackExtension {

    val SOUND_SUCCESS: Int = R.raw.sound_success
    val SOUND_FAILED: Int = R.raw.sound_failed
    val SOUND_MATCHED: Int = R.raw.sound_matched
    val SOUND_ERROR: Int = R.raw.sound_error

    fun AudioFeedback.loadSounds() {
        loadSounds(
            listOf(
                SOUND_SUCCESS,
                SOUND_FAILED,
                SOUND_MATCHED,
                SOUND_ERROR
            )
        )
    }

    // Common tones for different actions
    fun AudioFeedback.playSuccess() {
        playSound(SOUND_SUCCESS)
    }

    fun AudioFeedback.playFailed() {
        playSound(SOUND_FAILED)
    }

    fun AudioFeedback.playMatched() {
        playSound(SOUND_MATCHED)
    }

    fun AudioFeedback.playError() {
        playSound(SOUND_ERROR)
    }
}