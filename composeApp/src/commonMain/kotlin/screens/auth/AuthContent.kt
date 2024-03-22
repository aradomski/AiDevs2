package screens.auth

import Task
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import screens.task.TaskScreen

@Composable
fun AuthContent(
    state: AuthContract.State,
    effectFlow: Flow<AuthContract.Effect>,
    onEventSent: (event: AuthContract.Event) -> Unit,
) {
    val navigator = LocalNavigator.currentOrThrow
    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(value = state.token ?: "", onValueChange = {
            onEventSent(AuthContract.Event.TokenUpdated(it))
        }, label = {
            Text("Input your token")
        })
        Task.entries.forEach { task ->
            Row(
                modifier = Modifier.selectable(state.task == task, onClick = {
                    onEventSent(AuthContract.Event.TaskUpdated(task))
                }).padding(end = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(state.task == task, onClick = {
                    onEventSent(AuthContract.Event.TaskUpdated(task))
                })
                Text("$task")
            }
        }

        OutlinedButton(onClick = {
            onEventSent(AuthContract.Event.SubmitToken)
        }, enabled = state.token?.isNotBlank() == true && state.task != null) {
            Text("Submit")
        }
        AnimatedVisibility(state.response != null) {
            Text("${state.response}")
        }
        val composableScope = rememberCoroutineScope()
        composableScope.launch {
            effectFlow.collect {
                when (it) {
                    is AuthContract.Effect.Authed -> {
                        navigator.push(TaskScreen(it.task, it.token))
                    }
                }
            }
        }
    }
}