package init

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

actual fun platformInit() {
    Napier.base(DebugAntilog("Napier"))
}