package service

import Task
import api.model.answer.AnswerRequest
import api.model.answer.AnswerResponse
import api.model.auth.AuthRequest
import api.model.auth.AuthResponse
import api.model.task.TaskResponses
import api.model.unknow.people.Person
import api.model.unknow.people.PersonWithMetadata
import api.model.unknow.search.UnknowNews
import api.model.unknow.search.UnknowNewsWithMetadata
import com.aallam.openai.api.audio.Transcription
import com.aallam.openai.api.audio.TranscriptionRequest
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.FunctionTool
import com.aallam.openai.api.chat.ToolChoice
import com.aallam.openai.api.chat.chatCompletionRequest
import com.aallam.openai.api.embedding.EmbeddingRequest
import com.aallam.openai.api.embedding.EmbeddingResponse
import com.aallam.openai.api.file.fileSource
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.api.moderation.ModerationModel
import com.aallam.openai.api.moderation.moderationRequest
import com.aallam.openai.client.OpenAI
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okio.Buffer

class TaskSolverService(
    private val openAI: OpenAI,
    private val aiDevs2Service: AiDevs2Service,
    private val fileDownloader: FileDownloader,
    private val qdrantSolverService: QdrantSolverService,
    private val json: Json
) {
    suspend fun solve(
        token: String,
        task: Task,
        response: TaskResponses,
        taskPayload: ExtraTaskPayload? = null,
    ): SolvingData {
        return when (task) {
            Task.HELLO_API -> solveHelloApi(token, response as TaskResponses.HelloApiResponse)
            Task.MODERATION -> solveModeration(
                token,
                response as TaskResponses.ModerationResponse
            )

            Task.BLOGGER -> solveBlogger(token, task, response as TaskResponses.BloggerResponse)
            Task.LIAR -> solveLiar(
                token,
                task,
                response as TaskResponses.LiarResponse,
                taskPayload as ExtraTaskPayload.Liar
            )

            Task.INPROMPT -> solveInprompt(
                token,
                task,
                response as TaskResponses.InpromptResponse,
            )

            Task.EMBEDDING -> solveEmbedding(
                token,
                task,
                response as TaskResponses.EmbeddingResponse
            )

            Task.WHISPER -> solveWhisper(token, task, response as TaskResponses.WhisperResponse)
            Task.FUNCTIONS -> solveFunctions(
                token,
                task,
                response as TaskResponses.FunctionsResponse
            )

            Task.RODO -> solveRodo(token, task, response as TaskResponses.RodoResponse)

            Task.SCRAPER -> solveScraper(token, task, response as TaskResponses.ScraperResponse)
            Task.WHOAMI -> solveWhoami(token, task, response as TaskResponses.EmptyWhoamiResponse)
            Task.SEARCH -> solveSearch(token, task, response as TaskResponses.EmptySearchResponse)
            Task.PEOPLE -> solvePeople(token, task, response as TaskResponses.PeopleResponse)
            Task.KNOWLEDGE -> solveKnowledge(
                token,
                task,
                response as TaskResponses.KnowledgeResponse
            )

            Task.TOOLS -> solveTools(token, task, response as TaskResponses.ToolsResponse)
        }
    }

    //
//    |
//    |if text is related to ToDo
//    |then return json like this
//    | {"tool":"ToDo","desc":" DO STUFF" }"
//    |
//    | if it is related to calendar return json like this:
//    |
//    |{"tool":"Calendar","desc":" CALENDAR ENTRY ","date":"2024-04-10"}"
//    |
    private suspend fun solveTools(
        token: String,
        task: Task,
        toolsResponse: TaskResponses.ToolsResponse
    ): SolvingData {
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = """Your role is to determine if the given text is note to calendar or ToDo list entry. If time or date is mentioned in text this is calendar entry
                        |if text is related to ToDo just return "TODO"
                        |if text is calendar entry just return "Calendar"
                    """.trimMargin()
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = toolsResponse.question
                )
            )
        )
        val chatCompletion = openAI.chatCompletion(chatCompletionRequest)

        val toolAnswer = when (chatCompletion.choices.get(0).message.content!!.lowercase()) {
            "todo" -> {
                val chatCompletionRequest = ChatCompletionRequest(
                    model = ModelId("gpt-3.5-turbo"),
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.System,
                            content = """Based on given TODO entry generate JSON like this:
                                |{"tool":"ToDo","desc":" DO STUFF" }"
                                |return only json
                                |
                    """.trimMargin()
                        ),
                        ChatMessage(
                            role = ChatRole.User,
                            content = toolsResponse.question
                        )
                    )
                )
                val chatCompletion = openAI.chatCompletion(chatCompletionRequest)

                json.decodeFromString<AnswerRequest.ToolToDoAnswer>(chatCompletion.choices.get(0).message.content!!)
            }

            "calendar" -> {
                val chatCompletionRequest = ChatCompletionRequest(
                    model = ModelId("gpt-3.5-turbo"),
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.System,
                            content = """Based on given Calendar  entry generate JSON like this:
                                |{"tool":"Calendar","desc":" CALENDAR ENTRY ","date":"yyyy-mm-dd"}"
                                |return only json. Today is 09.04.2024
                                |
                    """.trimMargin()
                        ),
                        ChatMessage(
                            role = ChatRole.User,
                            content = toolsResponse.question
                        )
                    )
                )
                val chatCompletion = openAI.chatCompletion(chatCompletionRequest)

                json.decodeFromString<AnswerRequest.ToolCalendarAnswer>(chatCompletion.choices.get(0).message.content!!)
            }

            else -> { throw  IllegalStateException("coś się zepsuło w powyżej")}
        }


        val answerRequest =
            AnswerRequest.Tool(toolAnswer)
        return SolvingData(
            answerRequest,
            aiDevs2Service.answer(token, answerRequest),
        )
    }

    private suspend fun solveKnowledge(
        token: String,
        task: Task,
        knowledgeResponse: TaskResponses.KnowledgeResponse
    ): SolvingData {

        val restcountriesUrl = "https://restcountries.com/v3.1/all/"
        val currencyUrl = " https://api.nbp.pl/api/exchangerates/tables/A/"


        val restCountries = fileDownloader.downloadText(restcountriesUrl)
        val currency = fileDownloader.downloadText(currencyUrl)

        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = """Your role is to determine if the questions is about currency or population based on provided question.
                        | Answer only by using "currency" or "population"
                    """.trimMargin()
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = knowledgeResponse.question
                )
            )
        )
        val chatCompletion = openAI.chatCompletion(chatCompletionRequest)

        val answer =
            if (chatCompletion.choices.get(0).message.content?.lowercase() == "population") {
                val chatCompletionRequest = ChatCompletionRequest(
                    model = ModelId("gpt-3.5-turbo"),
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.System,
                            content = """Your role is to answer question based on this data 
                            |
                            |$restCountries
                        """.trimMargin()
                        ),
                        ChatMessage(
                            role = ChatRole.User,
                            content = knowledgeResponse.question
                        )
                    )
                )
                val chatCompletion = openAI.chatCompletion(chatCompletionRequest)
                chatCompletion.choices.get(0).message.content
            } else {
                val chatCompletionRequest = ChatCompletionRequest(
                    model = ModelId("gpt-3.5-turbo"),
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.System,
                            content = """Your role is to answer question based on this data 
                            |
                            |$currency
                        """.trimMargin()
                        ),
                        ChatMessage(
                            role = ChatRole.User,
                            content = knowledgeResponse.question
                        )
                    )
                )
                val chatCompletion = openAI.chatCompletion(chatCompletionRequest)
                chatCompletion.choices.get(0).message.content
            }

        val answerRequest =
            AnswerRequest.Whoami(answer!!)
        return SolvingData(
            answerRequest,
            aiDevs2Service.answer(token, answerRequest),
        )
    }

    private suspend fun solvePeople(
        token: String,
        task: Task,
        peopleResponse: TaskResponses.PeopleResponse
    ): SolvingData {
        /// Qdrant must be running with opened port 6334. JVM only
        val collectionName = "people"
        val people: List<Person> =
            fileDownloader.downloadJson("https://tasks.aidevs.pl/data/people.json")


        val existedBefore = qdrantSolverService.createCollection(collectionName)
        if (!existedBefore) {
            val personWithMetadata = people.mapIndexed { index, it ->
                val embeddingRequest = EmbeddingRequest(
                    model = ModelId("text-embedding-ada-002"),
                    input = listOf(it.toString())
                )
                val embeddings: EmbeddingResponse = openAI.embeddings(embeddingRequest)

                PersonWithMetadata.fromPerson(it, index, embeddings.embeddings[0].embedding)
            }
            qdrantSolverService.upsert(collectionName, personWithMetadata, true)
        }

//        val peopleResponse = aiDevs2Service.getTask<TaskResponses.PeopleResponse>(auth.token)

        val embeddingRequest = EmbeddingRequest(
            model = ModelId("text-embedding-ada-002"),
            input = listOf(peopleResponse.question)
        )
        val embeddings: EmbeddingResponse = openAI.embeddings(embeddingRequest)


        val search = qdrantSolverService.search(collectionName, embeddings.embeddings[0].embedding)

        val person = people[search.toInt()]

        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = "You answer questions based on given person:" +
                            "$person"
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = peopleResponse.question
                )
            )
        )
        val chatCompletion = openAI.chatCompletion(chatCompletionRequest)


        val answerRequest =
            AnswerRequest.Whoami(chatCompletion.choices.get(0).message.content!!)
        return SolvingData(
            answerRequest,
            aiDevs2Service.answer(token, answerRequest),
        )
    }

    private suspend fun solveSearch(
        token: String,
        task: Task,
        emptySearchResponse: TaskResponses.EmptySearchResponse
    ): SolvingData {
        /// Qdrant must be running with opened port 6334. JVM only

        val collectionName = "search collection search"

        val auth = aiDevs2Service.auth(Task.SEARCH.taskName, AuthRequest(token))
        val unknowNews: List<UnknowNews> =
            fileDownloader.downloadJson("https://unknow.news/archiwum_aidevs.json")


        val existedBefore = qdrantSolverService.createCollection(collectionName)
        if (!existedBefore) {
            val unknowNewsWithMetadata = unknowNews.mapIndexed { index, it ->
                val embeddingRequest = EmbeddingRequest(
                    model = ModelId("text-embedding-ada-002"),
                    input = listOf(it.toString())
                )
                val embeddings: EmbeddingResponse = openAI.embeddings(embeddingRequest)

                UnknowNewsWithMetadata(
                    date = it.date, info = it.info, title = it.title, url = it.url, uuid = index,
                    source = collectionName, vector = embeddings.embeddings[0].embedding
                )
            }
            qdrantSolverService.upsert(collectionName, unknowNewsWithMetadata)
        }

        val searchResponse = aiDevs2Service.getTask<TaskResponses.SearchResponse>(auth.token)

        val embeddingRequest = EmbeddingRequest(
            model = ModelId("text-embedding-ada-002"),
            input = listOf(searchResponse.question)
        )
        val embeddings: EmbeddingResponse = openAI.embeddings(embeddingRequest)


        val search = qdrantSolverService.search(collectionName, embeddings.embeddings[0].embedding)


        val answerRequest =
            AnswerRequest.Whoami(unknowNews[search.toInt()].url)
        return SolvingData(
            answerRequest,
            aiDevs2Service.answer(auth.token, answerRequest),
        )
    }

    private suspend fun solveWhoami(
        token: String,
        task: Task,
        emptyWhoamiResponse: TaskResponses.EmptyWhoamiResponse
    ): SolvingData {

        val messages = mutableListOf(
            ChatMessage(
                role = ChatRole.System,
                content = """Your role is to guess character/person based on hints given by user.
                    | Respond only with guessed name.
                    | If you are not 100% sure if you guessed correctly respond only with "more info please" """.trimMargin()
            ),
        )
        var chatCompletion: ChatCompletion
        var authResponse: AuthResponse
        do {
            authResponse = aiDevs2Service.auth(task.taskName, AuthRequest(token))
            val whoamiResponse =
                aiDevs2Service.getTask<TaskResponses.WhoamiResponse>(authResponse.token)
            messages.add(
                ChatMessage(
                    role = ChatRole.User,
                    content = whoamiResponse.hint
                ),
            )
            val chatCompletionRequest = ChatCompletionRequest(
                model = ModelId("gpt-3.5-turbo"),
                messages = messages
            )
            chatCompletion = openAI.chatCompletion(chatCompletionRequest)
            messages.add(chatCompletion.choices[0].message)

        } while (chatCompletion.choices[0].message.content?.lowercase() == "more info please")
        val answerRequest =
            AnswerRequest.Whoami(chatCompletion.choices[0].message.content!!)
        return SolvingData(
            answerRequest,
            aiDevs2Service.answer(authResponse.token, answerRequest),
        )
    }

    private suspend fun solveScraper(
        token: String,
        task: Task,
        scraperResponse: TaskResponses.ScraperResponse
    ): SolvingData {
        var textToAnalyze = ""
        textToAnalyze = fileDownloader.downloadText(scraperResponse.input)


        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = """Twoje zadanie to odpowiadać na pytania na podstawie poniższego tekstu
                        |
                        |####
                        |
                        | $textToAnalyze
                    """.trimMargin()
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = scraperResponse.question
                )
            )
        )
        val chatCompletion = openAI.chatCompletion(chatCompletionRequest)


        val answerRequest =
            AnswerRequest.Scraper(chatCompletion.choices[0].message.content ?: "")
        return SolvingData(
            answerRequest,
            aiDevs2Service.answer(token, answerRequest),
        )
    }

    private suspend fun solveRodo(
        token: String,
        task: Task,
        rodoResponse: TaskResponses.RodoResponse
    ): SolvingData {
        val answerRequest =
            AnswerRequest.Rodo(
                """Your role is to anonymize data. You only return anonymized version of user input, nothing else.
                        | You anonymize key informations like name,surname,city, profession, country
                        | Use given placeholders for data to anonymize input:
                        | name - %imie%
                        | surname - %nazwisko%
                        | city - %miasto%
                        | profession - %zawod%
                        | country - %kraj%
                        | 
                        | 
                        | Do not analyze user input, only replace sensitive informations with given placeholders.
                        | Note that some proffesions might contain more than one word.""".trimMargin()
            )
        return SolvingData(
            answerRequest,
            aiDevs2Service.answer(token, answerRequest),
        )
    }

    private suspend fun solveFunctions(
        token: String,
        task: Task,
        functionsResponse: TaskResponses.FunctionsResponse
    ): SolvingData {
        val request = chatCompletionRequest {
            model = ModelId("gpt-3.5-turbo")
            messages = mutableListOf(
                ChatMessage(
                    role = ChatRole.User,
                    content = ""
                )
            )
            tools {
                function(
                    name = "addUser",
                    description = "Adds user",
                ) {
                    put("type", "object")
                    putJsonObject("properties") {
                        putJsonObject("name") {
                            put("type", "string")
                            put("description", "Name of the person")
                        }
                        putJsonObject("surname") {
                            put("type", "string")
                            put("description", "Surname of the person")
                        }
                        putJsonObject("year") {
                            put("type", "integer")
                            put("description", "Year of birth of the person")
                        }
                    }
                    putJsonArray("required") {
                        add("name")
                        add("surname")
                        add("year")
                    }
                }
            }
            toolChoice = ToolChoice.Auto // or ToolChoice.function("currentWeather")
        }
        val tool: FunctionTool? = request.tools?.getOrNull(0)?.function
        tool?.let {
            val answerRequest =
                AnswerRequest.Functions(it)
            return SolvingData(
                answerRequest,
                aiDevs2Service.answer(token, answerRequest),
                IntermediateData.FunctionsIntermediateData(it.toString())
            )
        }
        throw IllegalStateException("Somehow tool: FunctionTool is null")
    }

    private suspend fun solveWhisper(
        token: String,
        task: Task,
        whisperResponse: TaskResponses.WhisperResponse
    ): SolvingData {
        val chatCompletionRequestUrl = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = "You find urls in given sentences. Respond only with found url"
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = whisperResponse.msg
                )
            )
        )
        val chatCompletionUrl = openAI.chatCompletion(chatCompletionRequestUrl)
        val url = chatCompletionUrl.choices.getOrNull(0)?.message?.content
            ?: "no urls was found"


        val downloadedFile = fileDownloader.downloadFile(url)


        val transcriptionRequest =
            TranscriptionRequest(model = ModelId("whisper-1"), audio = fileSource {
                val buffer = Buffer()
                name = "file.mp3"
                source = buffer.write(downloadedFile)
            })
        val transcription = openAI.transcription(transcriptionRequest)


        val answerRequest =
            AnswerRequest.Whisper(transcription.text)
        return SolvingData(
            answerRequest,
            aiDevs2Service.answer(token, answerRequest),
            IntermediateData.WhisperIntermediateData(transcription)
        )
    }

    private suspend fun solveEmbedding(
        token: String,
        task: Task,
        embeddingResponse: TaskResponses.EmbeddingResponse
    ): SolvingData {
        val embeddingRequest = EmbeddingRequest(
            model = ModelId("text-embedding-ada-002"),
            input = listOf("Hawaiian pizza")
        )
        val embeddings = openAI.embeddings(embeddingRequest)

        val answerRequest =
            AnswerRequest.Embedding(embeddings.embeddings.map { it.embedding }.flatten())
        return SolvingData(
            answerRequest,
            aiDevs2Service.answer(token, answerRequest),
            IntermediateData.EmbeddingIntermediateData(embeddings)
        )
    }

    private suspend fun solveInprompt(
        token: String,
        task: Task,
        inpromptResponse: TaskResponses.InpromptResponse
    ): SolvingData {
        val chatCompletionRequestName = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = "You find names in given sentences. Respond only with found name"
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = inpromptResponse.question
                )
            )
        )
        val chatCompletionName = openAI.chatCompletion(chatCompletionRequestName)
        val name =
            chatCompletionName.choices.getOrNull(0)?.message?.content
                ?: "no names was found"

        val filteredFacts = inpromptResponse.input.filter { it.contains(name) }

        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = "You answer questions based on given facts\n ######## \n ${
                        filteredFacts.joinToString(
                            separator = "\n"
                        )
                    }"
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = inpromptResponse.question
                )
            )
        )
        val chatCompletion = openAI.chatCompletion(chatCompletionRequest)

        val answerRequest = chatCompletion.choices[0].message.content?.let {
            AnswerRequest.Inprompt(it)
        } ?: AnswerRequest.Inprompt("Something failed :(")


        return SolvingData(
            answerRequest,
            aiDevs2Service.answer(token, answerRequest),
            IntermediateData.InpromptIntermediateData(
                foundName = name
            )
        )
    }

    private suspend fun solveLiar(
        token: String,
        task: Task,
        liarResponse: TaskResponses.LiarResponse,
        taskPayload: ExtraTaskPayload.Liar
    ): SolvingData {
        val liarResponseForQuestion = aiDevs2Service.getTask<TaskResponses.LiarResponseForQuestion>(
            token,
            taskPayload.question
        )
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = "You are judge that verifies if the given answer corresponds to the given question"
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = "Return only YES or NO whenever question: \n ${taskPayload.question} \n matches given answer: \n ${liarResponseForQuestion.answer}"
                )
            )
        )
        val chatCompletion = openAI.chatCompletion(chatCompletionRequest)

        val answerRequest = AnswerRequest.Liar(chatCompletion.choices[0].message.content ?: "NO")
        return SolvingData(
            answerRequest,
            aiDevs2Service.answer(token, answerRequest),
            IntermediateData.LiarIntermediateData(taskPayload.question, liarResponseForQuestion)
        )
    }

    private suspend fun solveBlogger(
        token: String,
        task: Task,
        taskResponse: TaskResponses.BloggerResponse
    ): SolvingData {
        val answers = taskResponse.blog.map {
            val chatCompletionRequest = ChatCompletionRequest(
                model = ModelId("gpt-3.5-turbo"),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        content = "You are pizza maker blogger. Write blog posts based on its titles"
                    ),
                    ChatMessage(
                        role = ChatRole.User,
                        content = it
                    )
                )
            )
            openAI.chatCompletion(chatCompletionRequest)
        }.mapNotNull { it.choices[0].message.content }
        val answerRequest = AnswerRequest.Blogger(answers)
        return SolvingData(answerRequest, aiDevs2Service.answer(token, answerRequest))
    }

    private suspend fun solveModeration(
        token: String,
        taskResponse: TaskResponses.ModerationResponse
    ): SolvingData {
        val moderationRequest = moderationRequest {
            input = taskResponse.input
            model = ModerationModel.Latest
        }
        val answers = openAI.moderations(moderationRequest).results.map {
            if (it.flagged) {
                1
            } else {
                0
            }
        }


        val answerRequest = AnswerRequest.Moderation(answers)
        return SolvingData(answerRequest, aiDevs2Service.answer(token, answerRequest))
    }

    private suspend fun solveHelloApi(
        token: String,
        taskResponse: TaskResponses.HelloApiResponse
    ): SolvingData {
        val answerRequest = AnswerRequest.HelloApi(taskResponse.cookie)
        return SolvingData(answerRequest, aiDevs2Service.answer(token, answerRequest))
    }
}

sealed interface ExtraTaskPayload {
    data class Liar(val question: String) :
        ExtraTaskPayload
}

sealed interface IntermediateData {
    data class LiarIntermediateData(
        val question: String,
        val answer: TaskResponses.LiarResponseForQuestion
    ) : IntermediateData

    data class InpromptIntermediateData(
        val foundName: String,
    ) : IntermediateData

    data class EmbeddingIntermediateData(
        val embeddingResponse: EmbeddingResponse,
    ) : IntermediateData

    data class WhisperIntermediateData(
        val transcription: Transcription
    ) : IntermediateData

    data class FunctionsIntermediateData(
        val functionParametersDefinition: String
    ) : IntermediateData
}

data class SolvingData(
    val answerRequest: AnswerRequest,
    val answerResponse: AnswerResponse,
    val intermediateData: IntermediateData? = null
)