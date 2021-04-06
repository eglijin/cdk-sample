import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
class Metrics {

    val http = HttpClient(CIO) {
        defaultRequest {
            url.host = "ke21y4qag1.execute-api.us-west-2.amazonaws.com"
            url.protocol = URLProtocol.HTTPS
            url.encodedPath = "test"
        }
        engine {
            requestTimeout = 30000
        }
    }

    @Test
    fun mertics() = runBlocking {
        println(http.get<String>())
    }
}