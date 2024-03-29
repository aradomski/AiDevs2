package service

import Task
import api.model.answer.AnswerRequest
import api.model.answer.AnswerResponse
import api.model.task.TaskResponses
import com.aallam.openai.api.audio.Transcription
import com.aallam.openai.api.audio.TranscriptionRequest
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
import kotlinx.serialization.json.add
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okio.Buffer

class TaskSolverService(
    private val openAI: OpenAI,
    private val aiDevs2Service: AiDevs2Service,
    private val fileDownloader: FileDownloader
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
        }
    }

    private suspend fun solveRodo(
        token: String,
        task: Task,
        rodoResponse: TaskResponses.RodoResponse
    ): SolvingData {
//        val request = chatCompletionRequest {
//            model = ModelId("gpt-3.5-turbo")
//            messages = mutableListOf(
//                ChatMessage(
//                    role = ChatRole.System,
//                    content = """Your role is to anonymize data. You only return anonymized version of user input, nothing else.
//                        | You anonymize key informations like name,surname,city, profession, country
//                        | Use given placeholders for data to anonymize input:
//                        | name - %imie%
//                        | surname - %nazwisko%
//                        | city - %miasto%
//                        | profession - %zawod%
//                        | country - %kraj%
//                        |
//                        |
//                        | Do not analyze user input, only replace sensitive informations with given placeholders.
//                        | Note that some proffesions might contain more than one word
//                    """.trimMargin()
//                ),
//                ChatMessage(
//                    role = ChatRole.User,
//                    content = rodoResponse.msg
//                )
//            )
//        }
//        val chatCompletion = openAI.chatCompletion(request)
//        chatCompletion.choices[0].message.content?.let {
            val answerRequest =
                AnswerRequest.Rodo("""Your role is to anonymize data. You only return anonymized version of user input, nothing else.
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
                        | Note that some proffesions might contain more than one word """.trimMargin())
            return SolvingData(
                answerRequest,
                aiDevs2Service.answer(token, answerRequest),
            )
//        }
//        throw IllegalStateException("  chatCompletion.choices[0].message.content? is null")
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