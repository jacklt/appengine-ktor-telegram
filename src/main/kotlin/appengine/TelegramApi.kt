package com.github.jacklt.gae.ktor.tg.appengine

import com.github.jacklt.gae.ktor.tg.config.AppConfig
import com.github.jacklt.gae.ktor.tg.utils.parseNotNull
import com.github.jacklt.gae.ktor.tg.utils.toJsonContentForTelegram
import com.google.api.client.extensions.appengine.http.UrlFetchTransport
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpRequestFactory
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

object TelegramApi {
    private val config = AppConfig.getDefault().telegram
    private val basePath = "https://api.telegram.org/bot${config.apiKey}"
    private val httpTransport = UrlFetchTransport.getDefaultInstance()
    private fun requestFactory(): HttpRequestFactory = httpTransport.createRequestFactory()

    fun sendMessage(
        chatId: String,
        text: String,
        parseMode: ParseMode = ParseMode.NONE,
        disableWebPagePreview: Boolean = false,
        button: List<List<InlineKeyboardButton>>? = null
    ) = requestFactory()
        .buildPostRequest(
            GenericUrl("$basePath/sendMessage"),
            MessageRequest(
                chat_id = chatId,
                text = text,
                parse_mode = parseMode.str,
                disable_web_page_preview = disableWebPagePreview,
                reply_markup = button?.let { InlineKeyboardMarkup(it) }
            ).toJsonContentForTelegram()
        )
        .execute()
        .parseNotNull(TelegramResponse.serializer(MessageResponse.serializer()))

    fun editMessageText(
        chatId: String,
        messageId: Int,
        text: String,
        parseMode: ParseMode = ParseMode.NONE,
        disableWebPagePreview: Boolean = false,
        button: List<List<InlineKeyboardButton>>? = null
    ) = requestFactory()
        .buildPostRequest(
            GenericUrl("$basePath/editMessageText"),
            MessageRequest(
                text = text,
                chat_id = chatId,
                message_id = messageId,
                parse_mode = parseMode.str,
                disable_web_page_preview = disableWebPagePreview,
                reply_markup = button?.let { InlineKeyboardMarkup(it) }
            ).toJsonContentForTelegram()
        )
        .execute()
        //.parseForTelegram<TelegramResponse<MessageResponse>>()
        .parseNotNull(TelegramResponse.serializer(MessageResponse.serializer()))


    fun forwardMessage(
        chatId: String,
        fromChatId: String,
        messageId: Int,
        disableNotification: Boolean? = null
    ) = requestFactory()
        .buildPostRequest(
            GenericUrl("$basePath/forwardMessage"),
            ForwardRequest(
                chat_id = chatId,
                from_chat_id = fromChatId,
                message_id = messageId,
                disable_notification = disableNotification
            ).toJsonContentForTelegram()
        )
        .execute()
        .parseNotNull(TelegramResponse.serializer(MessageResponse.serializer()))


    fun deleteMessage(
        chatId: String,
        messageId: Int
    ) = requestFactory()
        .buildPostRequest(
            GenericUrl("$basePath/deleteMessage"),
            MessageDeleteRequest(chatId, messageId).toJsonContentForTelegram()
        )
        .execute()
        .parseNotNull(TelegramResponse.serializer(Boolean.serializer()))


    enum class ParseMode(val str: kotlin.String?) {
        NONE(null),
        MARKDOWN("Markdown"),
        HTML("HTML")
    }

    @Serializable
    data class TelegramResponse<T>(val ok: Boolean, val result: T)

    @Serializable
    data class MessageRequest(
        val chat_id: String,
        val text: String,
        @Optional val parse_mode: String? = null,
        @Optional val disable_web_page_preview: Boolean? = null,
        @Optional val message_id: Int? = null,
        @Optional val reply_markup: InlineKeyboardMarkup? = null
    )

    @Serializable
    data class ForwardRequest(
        val chat_id: String,
        val from_chat_id: String,
        val message_id: Int,
        @Optional val disable_notification: Boolean? = null
    )

    @Serializable
    data class MessageDeleteRequest(
        val chat_id: String,
        val message_id: Int
    )

    @Serializable
    data class MessageResponse(
        val message_id: Int,
        val chat: ChatResponse,
        val text: String
    )

    @Serializable
    data class ChatResponse(
        val id: Int
    )

    @Serializable
    data class InlineKeyboardMarkup(
        val inline_keyboard: List<List<InlineKeyboardButton>>
    )

    @Serializable
    data class InlineKeyboardButton(
        val text: String,
        val url: String
    )

    @Serializable
    data class UpdateMessageRequest(
        val update_id: Int,
        val message: MessageResponse
    )

    @Serializable
    data class UpdateMessageResponse(
        val chat_id: Int,
        val text: String
//        @Optional val parse_mode: String? = null,
//        @Optional val disable_web_page_preview: Boolean? = null,
//        @Optional val message_id: Int? = null,
//        @Optional val reply_markup: InlineKeyboardMarkup? = null
    ) {
        val method = "sendMessage"
    }
}