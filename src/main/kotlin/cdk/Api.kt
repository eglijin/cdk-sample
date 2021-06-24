import cdk.ContextAwareStack
import cdk.asset
import software.amazon.awscdk.core.Fn
import software.amazon.awscdk.services.apigateway.ApiDefinition
import software.amazon.awscdk.services.apigateway.DomainName
import software.amazon.awscdk.services.apigateway.DomainNameProps
import software.amazon.awscdk.services.apigateway.EndpointType
import software.amazon.awscdk.services.apigateway.IResource
import software.amazon.awscdk.services.apigateway.Integration
import software.amazon.awscdk.services.apigateway.IntegrationOptions
import software.amazon.awscdk.services.apigateway.IntegrationResponse
import software.amazon.awscdk.services.apigateway.MethodLoggingLevel
import software.amazon.awscdk.services.apigateway.MethodOptions
import software.amazon.awscdk.services.apigateway.MethodResponse
import software.amazon.awscdk.services.apigateway.MockIntegration
import software.amazon.awscdk.services.apigateway.PassthroughBehavior
import software.amazon.awscdk.services.apigateway.Resource
import software.amazon.awscdk.services.apigateway.RestApi
import software.amazon.awscdk.services.apigateway.RestApiBase
import software.amazon.awscdk.services.apigateway.SecurityPolicy
import software.amazon.awscdk.services.apigateway.SpecRestApi
import software.amazon.awscdk.services.apigateway.StageOptions
import software.amazon.awscdk.services.iam.ServicePrincipal
import software.amazon.awscdk.services.iam.ServicePrincipalOpts

val apigwServicePrincipal = ServicePrincipal(
    "apigateway.amazonaws.com", ServicePrincipalOpts.builder()
        .build()
)

fun ContextAwareStack.domain(id: String, domain: String, api: RestApiBase): DomainName = DomainName(
    this, id, DomainNameProps.builder()
        .certificate(certificate)
        .domainName(domain)
        .mapping(api)
        .endpointType(EndpointType.REGIONAL)
        .securityPolicy(SecurityPolicy.TLS_1_2)
        .build()
)

inline val RestApiBase.stagePrefix
    get() =
        when (deploymentStage.stageName) {
            "prod" -> ""
            else -> "${deploymentStage.stageName}-"
        }

inline val ContextAwareStack.domainPrefix get() = if ("api" !in domain) "-api" else ""

fun ContextAwareStack.api(id: String, location: String, stageVariables: Map<String, String>? = null) = SpecRestApi(id) {
    apiDefinition(
        ApiDefinition.fromInline(
            Fn.transform(
                "AWS::Include",
                mapOf<String, String>(
                    "Location" to asset(location).s3ObjectUrl
                )
            )
        )
    )
    cloudWatchRole(true)
    deploy(true)
    deployOptions(
        StageOptions.builder()
            .variables(stageVariables)
            .stageName(env)
            .loggingLevel(MethodLoggingLevel.INFO)
            .tracingEnabled(true)
            .build()
    )
    restApiName("$stackName $id")
}.apply {
    domain(
        "${id}-domain",
        "$stagePrefix${id.substringBefore(".")}$domainPrefix.$domain",
        this
    )
}

infix fun RestApiBase.post(path: String) =
    "arn:aws:execute-api:${stack.region}:${stack.account}:${restApiId}/*/POST$path"


fun IResource.resource(path: String, block: IResource.() -> Unit): IResource =
    Resource.Builder.create(stack, path).parent(this).pathPart(path).build().apply(block)

fun IResource.get(target: Integration): IResource =
    addMethod("GET", target).let { this }

fun IResource.get(block: () -> Integration): IResource =
    addMethod("GET", block()).let { this }

fun ContextAwareStack.SpecRestApi(id: String, block: SpecRestApi.Builder.() -> Unit = {}): SpecRestApi =
    SpecRestApi.Builder.create(this, id).apply(block).build()

inline fun ContextAwareStack.RestApi(
    id: String,
    deploy: Boolean = true,
    restApiName: String = "$stackName $id",
    block: IResource.() -> Unit = {}
): RestApi =
    RestApi.Builder.create(this, id)
        .deploy(deploy)
        .restApiName(restApiName)
        .build()
        .apply { block(root) }

//inline fun <reified T : IFunction> T.apiEvent(path: String, method: String = "post"): T = apply {
//    addEventSource(
//        ApiEventSource(
//            method, path,
////            MethodOptions.builder().build()
//        )
//    )
//}

fun SpecRestApi.cors(allowedHeaders: String, allowedOrigins: String, allowedMethods: String) = apply {
    root.addResource("{proxy+}").apply {
        addMethod(
            "OPTIONS",
            MockIntegration(
                IntegrationOptions.builder()
                    .integrationResponses(
                        listOf(
                            IntegrationResponse.builder()
                                .statusCode("200")
                                .responseParameters(
                                    mapOf(
                                        "method.response.header.Access-Control-Allow-Headers" to allowedHeaders,
                                        "method.response.header.Access-Control-Allow-Origin" to allowedOrigins,
//                                                    "method.response.header.Access-Control-Allow-Credentials" to "'false'",
                                        "method.response.header.Access-Control-Allow-Methods" to allowedMethods,
                                        "method.response.header.X-Frame-Options" to """'"sameorigin" always;'""",
                                        "method.response.header.X-XSS-Protection" to """'"1; mode=block" always;'""",
                                        "method.response.header.Strict-Transport-Security" to """'"max-age=31536000; includeSubDomains" always;'""",
                                        "method.response.header.X-Content-Type-Options" to "'nosniff;'"
                                    )
                                )
                                .build()
                        )
                    )
                    .passthroughBehavior(PassthroughBehavior.NEVER)
                    .requestTemplates(
                        mapOf(
                            "application/json" to """{"statusCode": 200}"""
                        )
                    )
                    .build()
            ), MethodOptions.builder().methodResponses(
                listOf(
                    MethodResponse.builder()
                        .statusCode("200")
                        .responseParameters(
                            mapOf(
                                "method.response.header.Access-Control-Allow-Headers" to true,
                                "method.response.header.Access-Control-Allow-Origin" to true,
//                                "method.response.header.Access-Control-Allow-Credentials" to true,
                                "method.response.header.Access-Control-Allow-Methods" to true,
                                "method.response.header.X-Frame-Options" to true,
                                "method.response.header.X-XSS-Protection" to true,
                                "method.response.header.Strict-Transport-Security" to true,
                                "method.response.header.X-Content-Type-Options" to true,
                            )
                        )
                        .build()
                )
            ).build()
        )
    }
}