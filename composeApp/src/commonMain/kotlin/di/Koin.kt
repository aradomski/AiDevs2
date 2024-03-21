package di

import OPEN_AI_KEY
import api.AiDevs2Api
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.OpenAI
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import screens.answers.AnswerScreenModel
import screens.auth.AuthScreenModel
import screens.task.TaskScreenModel
import service.AiDevs2Service
import service.TaskSolverService
import kotlin.time.Duration.Companion.seconds

private const val AI_DEVS_API = "AiDevsApi"
fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(
            platformModule(),
            viewModelModule,
            servicesModule,
            apiModule,
            ktorModule,
            openAiClientModule,
        )
    }

val viewModelModule = module {
    factoryOf(::AuthScreenModel)
    factory {
        TaskScreenModel(it.get(), it.get(), get())
    }
    factory {
        AnswerScreenModel(it.get(), it.get(), it.get(), get())
    }
}
val servicesModule = module {
    singleOf(::AiDevs2Service)
    singleOf(::TaskSolverService)
}

val apiModule = module {
    singleOf(::AiDevs2Api)
}
val ktorModule = module {
    single {
        HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = 1000 * 60 * 2
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                        isLenient = true
                    }
                )
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Napier.v("HTTP Client", null, message)
                    }
                }
                level = LogLevel.ALL
            }
        }
    }
    single { "https://tasks.aidevs.pl" }
}
val openAiClientModule = module {
    single {
        OpenAI(
            token = OPEN_AI_KEY,
            timeout = Timeout(socket = 60.seconds),
            // additional configurations...
        )
    }
}

@Suppress("UNUSED")//for iOS
fun initKoin() = initKoin {}