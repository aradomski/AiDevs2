package api.model.task

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class TaskResponses {

    @Serializable
    data class HelloApiResponse(
        @SerialName("code")
        val code: Int,
        @SerialName("cookie")
        val cookie: String,
        @SerialName("msg")
        val msg: String
    ) : TaskResponses()

    @Serializable
    data class ModerationResponse(
        @SerialName("code")
        val code: Int,
        @SerialName("input")
        val input: List<String>,
        @SerialName("msg")
        val msg: String
    ) : TaskResponses()

    @Serializable
    data class BloggerResponse(
        @SerialName("code")
        val code: Int,
        @SerialName("blog")
        val blog: List<String>,
        @SerialName("msg")
        val msg: String
    ) : TaskResponses()

    @Serializable
    data class LiarResponse(
        @SerialName("code")
        val code: Int,
        @SerialName("msg")
        val msg: String,
        @SerialName("hint1")
        val hint1: String,
        @SerialName("hint2")
        val hint2: String,
        @SerialName("hint3")
        val hint3: String,
    ) : TaskResponses()

    @Serializable
    data class LiarResponseForQuestion(
        @SerialName("code")
        val code: Int,
        @SerialName("msg")
        val msg: String,
        @SerialName("answer")
        val answer: String,
    ) : TaskResponses()

    @Serializable
    data class InpromptResponse(
        @SerialName("code")
        val code: Int,
        @SerialName("msg")
        val msg: String,
        @SerialName("input")
        val input: List<String>,
        @SerialName("question")
        val question: String,
    ) : TaskResponses()

    @Serializable
    data class EmbeddingResponse(
        @SerialName("code")
        val code: Int,
        @SerialName("msg")
        val msg: String,
        @SerialName("hint1")
        val hint1: String,
        @SerialName("hint2")
        val hint2: String,
        @SerialName("hint3")
        val hint3: String,
    ) : TaskResponses()

    @Serializable
    data class WhisperResponse(
        @SerialName("code")
        val code: Int,
        @SerialName("msg")
        val msg: String,
        @SerialName("hint")
        val hint: String,
    ) : TaskResponses()

    @Serializable
    data class FunctionsResponse(
        @SerialName("code")
        val code: Int,
        @SerialName("msg")
        val msg: String,
        @SerialName("hint1")
        val hint1: String,
    ) : TaskResponses()

    @Serializable
    data class RodoResponse(
        @SerialName("code")
        val code: Int,
        @SerialName("msg")
        val msg: String,
        @SerialName("hint1")
        val hint1: String,
        @SerialName("hint2")
        val hint2: String,
        @SerialName("hint3")
        val hint3: String,
    ) : TaskResponses()

    @Serializable
    data class ScraperResponse(
        @SerialName("code")
        val code: Int,
        @SerialName("msg")
        val msg: String,
        @SerialName("input")
        val input: String,
        @SerialName("question")
        val question: String,
    ) : TaskResponses()

    @Serializable
    data class WhoamiResponse(
        @SerialName("code")
        val code: Int,
        @SerialName("msg")
        val msg: String,
        @SerialName("hint")
        val hint: String,
    ) : TaskResponses()

    data object EmptyWhoamiResponse : TaskResponses()

    data object EmptySearchResponse : TaskResponses()


    @Serializable
    data class SearchResponse(
        @SerialName("code")
        val code: Int,
        @SerialName("msg")
        val msg: String,
        @SerialName("question")
        val question: String,
    ) : TaskResponses()

    @Serializable
    data class PeopleResponse(
        @SerialName("code")
        val code: Int,
        @SerialName("msg")
        val msg: String,
        @SerialName("data")
        val data: String,
        @SerialName("question")
        val question: String,
        @SerialName("hint1")
        val hint1: String,
        @SerialName("hint2")
        val hint2: String,
    ) : TaskResponses()

    @Serializable
    data class KnowledgeResponse(
        @SerialName("code")
        val code: Int,
        @SerialName("msg")
        val msg: String,
        @SerialName("question")
        val question: String,
        @SerialName("database #1")
        val hint1: String,
        @SerialName("database #2")
        val hint2: String,
    ) : TaskResponses()

//{
//    "code": 0,
//    "msg": "Decide whether the task should be added to the ToDo list or to the calendar (if time is provided) and return the corresponding JSON",
//    "hint": "always use YYYY-MM-DD format for dates",
//    "example for ToDo": "Przypomnij mi, \u017ce mam kupi\u0107 mleko = {\"tool\":\"ToDo\",\"desc\":\"Kup mleko\" }",
//    "example for Calendar": "Jutro mam spotkanie z Marianem = {\"tool\":\"Calendar\",\"desc\":\"Spotkanie z Marianem\",\"date\":\"2024-04-10\"}",
//    "question": "Pojutrze mam wizyt\u0119 u dentysty"
//}
    @Serializable
    data class ToolsResponse(
        @SerialName("code")
        val code: Int,
        @SerialName("msg")
        val msg: String,
        @SerialName("hint")
        val hint: String,
        @SerialName("example for ToDo")
        val exampleToDo: String,
        @SerialName("example for Calendar")
        val exampleCalendar: String,
        @SerialName("question")
        val question: String,
    ) : TaskResponses()
}