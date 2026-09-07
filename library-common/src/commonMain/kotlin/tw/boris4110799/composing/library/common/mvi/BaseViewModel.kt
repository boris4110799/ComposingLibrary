package tw.boris4110799.composing.library.common.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Base ViewModel of MVI.
 * @param initState The initial state of [S].
 */
abstract class BaseViewModel<S : BaseUiState, E : BaseUiEvent, I : BaseIntent>(initState: S) :
    ViewModel() {
    /**
     * Internal mutable flow of the UI state.
     */
    private val _uiState = MutableStateFlow(initState)

    /**
     * The UI state.
     */
    val uiState = _uiState.stateIn(viewModelScope, SharingStarted.Eagerly, initState)

    /**
     * Internal mutable flow of the UI event.
     */
    private val _uiEvent = MutableSharedFlow<E>()

    /**
     * The UI event.
     */
    val uiEvent = _uiEvent.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000L))

    /**
     * Internal mutable flow of the user intent.
     */
    private val _intent = MutableSharedFlow<I>()

    init {
        collectIntent()
    }

    /**
     * Collect user intent.
     */
    private fun collectIntent() {
        viewModelScope.launch(Dispatchers.Default) {
            _intent.shareIn(viewModelScope, SharingStarted.Eagerly)
                .collect { onIntentReceived(it) }
        }
    }

    /**
     * Handle user intent.
     */
    protected abstract suspend fun onIntentReceived(intent: I)

    /**
     * Get current UI state.
     */
    @OptIn(ExperimentalContracts::class)
    protected inline fun <R> withUiState(block: (uiState: S) -> R): R {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        return block(uiState.value)
    }

    /**
     * Update UI state.
     */
    protected fun updateUiState(newState: S.() -> S) {
        viewModelScope.launch {
            _uiState.emit(newState(uiState.value))
        }
    }

    /**
     * Send UI event.
     */
    protected fun sendEvent(event: E) {
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
    }

    /**
     * Send user intent.
     */
    fun sendIntent(intent: I) {
        viewModelScope.launch {
            _intent.emit(intent)
        }
    }
}

/**
 * Base UI state.
 */
interface BaseUiState

/**
 * Base UI event.
 */
interface BaseUiEvent

/**
 * Base user intent.
 */
interface BaseIntent

/**
 * Collect UI event.
 */
@Composable
fun <E : BaseUiEvent> CollectUiEvent(
    uiEvent: SharedFlow<E>,
    onEvent: (event: E) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            uiEvent.collect { onEvent(it) }
        }
    }
}
