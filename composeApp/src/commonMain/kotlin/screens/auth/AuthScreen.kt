package screens.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel

class AuthScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<AuthScreenModel>()
        LaunchedEffect(Unit) {
            viewModel.setEvent(AuthContract.Event.Init)
        }
        AuthContent(
            state = viewModel.uiState.collectAsState().value,
            effectFlow = viewModel.effect,
            onEventSent = { event -> viewModel.setEvent(event) }
        )
    }
}