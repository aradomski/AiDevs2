package di

import OPEN_AI_KEY
import api.AiDevs2Api
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
import service.TaskSolverService
import kotlin.time.Duration.Companion.seconds

private const val AI_DEVS_API = "AiDevsApi"
private const val FILE_DOWNLOADER_CLIENT = "FileDownloaderClient"
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
    single {
        AiDevs2Api(
            get(qualifier = StringQualifier(AI_DEVS_API)),
            get(qualifier = StringQualifier(AI_DEVS_API))
        )
    }
    single {
        FileDownloader(get(qualifier = StringQualifier(FILE_DOWNLOADER_CLIENT)))
    }
}
val ktorModule = module {
    single(qualifier = StringQualifier(AI_DEVS_API)) {
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
                requestTimeoutMillis = 1000 * 60 * 5
                connectTimeoutMillis = 1000 * 60 * 5
                socketTimeoutMillis = 1000 * 60 * 5
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Napier.v("HTTP Client", null, message)
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