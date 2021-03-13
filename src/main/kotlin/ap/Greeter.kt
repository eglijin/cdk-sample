package ap

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import kotlinx.coroutines.runBlocking

@Suppress("unused")
class Greeter : RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    override fun handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent =
        runBlocking {
            println("~${blocker/1000} cold start")
            APIGatewayProxyResponseEvent().withBody(
                /*language=JSON*/
                """{"message": "Hello"}"""
            )
        }
}