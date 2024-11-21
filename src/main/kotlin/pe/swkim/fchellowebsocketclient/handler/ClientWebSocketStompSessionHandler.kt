package pe.swkim.fchellowebsocketclient.handler

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.annotation.Order
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.stereotype.Component
import java.lang.reflect.Type

private val logger = KotlinLogging.logger {}

@Component
@Order(value = 1)
class ClientWebSocketStompSessionHandler(
    val objectMapper: ObjectMapper,
) : StompSessionHandlerAdapter() {
    companion object {
        private const val SUBSCRIPTION_TOPIC = "/topic/chatting"
        private const val PUBLISH_DEST = "/app/chatting-message"
    }

    override fun handleFrame(
        headers: StompHeaders,
        payload: Any?,
    ) {
        logger.trace { ">>> handleFrame: $headers" }

        // 구독한 채널의 메시지 받기
        val responseRawMessage = String(payload as ByteArray)
        val responseMessage = objectMapper.readValue(responseRawMessage, ResponseMessage::class.java)
        logger.trace { ">>> content: ${responseMessage.content}" }
    }

    override fun getPayloadType(headers: StompHeaders): Type = Any::class.java

    override fun afterConnected(
        session: StompSession,
        connectedHeaders: StompHeaders,
    ) {
        logger.trace { ">>> afterConnected: $session" }

        // 구독
        session.subscribe(SUBSCRIPTION_TOPIC, this)
        val params: MutableMap<String, Any> = HashMap()
        params["message"] = "반갑습니다. 저는 2번째 클라이언트 입니다."

        // 메시지 보냄
        session.send(PUBLISH_DEST, params)
        logger.trace { ">>> params: $params" }
    }

    override fun handleException(
        session: StompSession,
        command: StompCommand?,
        headers: StompHeaders,
        payload: ByteArray,
        exception: Throwable,
    ) {
        logger.error { ">>> handleException: $exception" }
    }

    override fun handleTransportError(
        session: StompSession,
        exception: Throwable,
    ) {
        logger.error { ">>> handleTransportError: ${exception.message}" }
    }
}
