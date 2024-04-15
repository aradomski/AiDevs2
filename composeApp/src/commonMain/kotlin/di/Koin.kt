package di

import OPEN_AI_KEY
import api.aidevs.AiDevs2Api
import api.renderform.RenderfromApi
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpRequestRetry
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
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import screens.answers.AnswerScreenModel
import screens.auth.AuthScreenModel
import screens.task.TaskScreenModel
import service.AiDevs2Service
import service.FileDownloader
import service.RenderFormService
import service.TaskSolverService
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private const val AI_DEVS_API = "AiDevsApi"
private const val FILE_DOWNLOADER_CLIENT = "FileDownloaderClient"
private const val RENDER_FORM_CLIENT = "RenderFormClient"
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
    singleOf(::RenderFormService)
}

val apiModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
    }
    single {
        AiDevs2Api(
            get(qualifier = StringQualifier(AI_DEVS_API)),
            get(qualifier = StringQualifier(AI_DEVS_API))
        )
    }
    single {
        FileDownloader(
            get(qualifier = StringQualifier(FILE_DOWNLOADER_CLIENT)), get(), get(
                qualifier = StringQualifier(
                    AI_DEVS_API
                )
            )
        )
    }
    single {
        RenderfromApi(
            get(qualifier = StringQualifier(RENDER_FORM_CLIENT)),
            get(qualifier = StringQualifier(RENDER_FORM_CLIENT))
        )
    }
}
val ktorModule = module {
    single(qualifier = StringQualifier(AI_DEVS_API)) {
        HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = 1000 * 60 * 2
            }
            install(ContentNegotiation) { json(get()) }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Napier.d(message = message)
                    }
                }
                level = LogLevel.ALL
            }
        }
    }
    single(qualifier = StringQualifier(AI_DEVS_API)) { "https://tasks.aidevs.pl" }

    single(qualifier = StringQualifier(FILE_DOWNLOADER_CLIENT)) {
        HttpClient {
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 10)
                exponentialDelay(maxDelayMs = 5.seconds.inWholeMilliseconds)
                retryOnException()
                retryIf { httpRequest, httpResponse ->
                    httpResponse.status.value !in 200..299
                }

            }
            install(HttpTimeout) {
                requestTimeoutMillis = 5.minutes.inWholeMilliseconds
                connectTimeoutMillis = 5.minutes.inWholeMilliseconds
                socketTimeoutMillis = 5.minutes.inWholeMilliseconds
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Napier.d(message = message)
                    }
                }
                level = LogLevel.ALL
            }
            install(DefaultRequest) {
                headers.append(
                    "User-Agent",
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:124.0) Gecko/20100101 Firefox/124.0"
                )
            }
        }
    }

    single(qualifier = StringQualifier(RENDER_FORM_CLIENT)) {
        HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = 1000 * 60 * 2
            }
            install(ContentNegotiation) { json(get()) }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Napier.d(tag = "Renderform", message = message)
                    }
                }
                level = LogLevel.ALL
            }
        }
    }
    single(qualifier = StringQualifier(RENDER_FORM_CLIENT)) { "https://api.renderform.io" }


}
val openAiClientModule = module {
    single {
        OpenAI(
            logging = LoggingConfig(
                logLevel = com.aallam.openai.api.logging.LogLevel.All,
                logger = com.aallam.openai.api.logging.Logger.Simple,
            ),
            token = OPEN_AI_KEY,
            timeout = Timeout(socket = 60.seconds),
            // additional configurations...
        )
    }
}

@Suppress("UNUSED")//for iOS
fun initKoin() = initKoin {}