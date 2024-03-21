import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import di.initKoin
import init.init

fun main() = application {
    init()
    initKoin {
    }
    Window(onCloseRequest = ::exitApplication, title = "Ai_Devs2") {
        App()
    }
}