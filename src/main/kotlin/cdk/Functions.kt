package cdk

import apigwServicePrincipal
import post
import software.amazon.awscdk.core.CfnResource
import software.amazon.awscdk.core.Construct
import software.amazon.awscdk.core.Duration
import software.amazon.awscdk.core.IgnoreMode
import software.amazon.awscdk.services.apigateway.LambdaIntegration
import software.amazon.awscdk.services.apigateway.PassthroughBehavior
import software.amazon.awscdk.services.apigateway.RestApiBase
import software.amazon.awscdk.services.lambda.*
import software.amazon.awscdk.services.lambda.Function

fun <T : Construct> T.overrideLogicalId(id: String): T =
    also { (it.node.defaultChild as CfnResource).overrideLogicalId(id) }

fun IFunction.apiEvent(api: RestApiBase, path: String): IFunction = apply {
    addPermission(
        "${api.node.id}-$path",
        Permission.builder()
            .scope(this.stack)
            .principal(apigwServicePrincipal)
            .action("lambda:InvokeFunction")
            .sourceArn(api post path)
            .build()
    )
}

fun String.appendFileSeparator() = if (isBlank()) this else "$this/"

inline fun ContextAwareStack.dockerFunction(
    prefix: String,
    handler: String,
    memory: Int,
    environment: Map<String, String> = mapOf(),
    id: String = handler
        .substringAfterLast(".")
        .let { if (!it.endsWith("Function")) "${it}Function" else it },
    timeout: Duration = Duration.minutes(5),
    block: DockerImageFunction.Builder.() -> Unit = {}
): Function =
    DockerImageFunction.Builder.create(this, id)
        .timeout(timeout)
        .memorySize(memory)
        .code(
            DockerImageCode.fromImageAsset(
                "./${prefix.appendFileSeparator()}build/lambda",
                AssetImageCodeProps.builder()
                    .ignoreMode(IgnoreMode.DOCKER)
                    .cmd(listOf("$handler::handleRequest"))
                    .build()
            )
        )
        .tracing(Tracing.ACTIVE)
        .environment(environment + baseEnvironmentVariables)
        .securityGroups(securityGroups)
        .vpc(vpc)
        .vpcSubnets(subnetSelection)
        .apply(block)
        .build()
        .overrideLogicalId(id)
        .withPolicies(kmsPolicyStatement, secretsPolicyStatement)

fun Function.withAlias(id: String, provisioning: Int? = null): Alias =
    Alias(id, this, provisioning)

fun Function.version(id: String, block: Version.Builder.() -> Unit = {}): Version =
    Version.Builder.create(this, id).apply(block).build()

fun Function.Alias(id: String, function: Function, provisioning: Int? = null): Alias =
    Alias.Builder
        .create(this, id)
        .aliasName(id)
        .version(
            version("version") {
                lambda(function)
                provisioning?.let { provisionedConcurrentExecutions(provisioning) }
            }
        )
        .build()

fun Construct.Alias(id: String, block: Alias.Builder.() -> Unit = {}): Alias =
    Alias.Builder.create(this, id).aliasName(id).apply(block).build()


fun LambdaIntegration(alias: () -> Alias): LambdaIntegration = LambdaIntegration(alias())

inline fun LambdaIntegration(alias: Alias, block: LambdaIntegration.Builder.() -> Unit = {}): LambdaIntegration =
    LambdaIntegration.Builder
        .create(alias)
        .passthroughBehavior(PassthroughBehavior.WHEN_NO_MATCH)
        .apply(block)
        .build()