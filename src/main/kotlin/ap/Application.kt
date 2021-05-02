@file:JvmName("Application")

package ap

import software.amazon.awscdk.core.*
import software.amazon.awscdk.core.CfnResource
import software.amazon.awscdk.cxapi.CloudAssembly
import software.amazon.awscdk.services.apigateway.*
import software.amazon.awscdk.services.apigateway.IResource
import software.amazon.awscdk.services.apigateway.Resource
import software.amazon.awscdk.services.cognito.UserPool
import software.amazon.awscdk.services.lambda.*
import software.amazon.awscdk.services.lambda.Function
import software.amazon.awscdk.services.s3.Bucket
import software.amazon.awscdk.services.s3.assets.Asset
import software.amazon.awscdk.services.s3.assets.AssetProps


fun main() {
    application {
        stack("performance-metrics") {

            val apigwDefinition = Asset(this, "PerformanceAPIGW", AssetProps.builder().path("./api/apigw.yml").build())

            val api = SpecRestApi("PerformanceApi") {
                apiDefinition(
                    ApiDefinition.fromInline(
                        Fn.transform(
                            "AWS::Include",
                            mapOf<String, String>(
                                "Location" to apigwDefinition.s3ObjectUrl
                            )
                        )
                    )
                )
                cloudWatchRole(true)
                deploy(true)
                deployOptions(
                    StageOptions.builder()
                        .stageName("performance")
                        .build()
                )
                restApiName(Fn.sub("\${AWS::StackName} Performance API"))
            }

            val function = dockerFunction("PerformanceFunction") {
                code(
                    DockerImageCode.fromImageAsset(
                        "./build",
                        AssetImageCodeProps.builder()
                            .exclude(
                                listOf(
                                    "distributions",
                                    "kotlin",
                                    "libs",
                                    "reports",
                                    "resources",
                                    "scripts",
                                    "tmp",
                                    "test-results",
                                )
                            )
                            .file("Dockerfile")
                            .ignoreMode(IgnoreMode.DOCKER)
                            .cmd(listOf("ap.Greeter::handleRequest"))
                            .build()
                    )
                )
//                events(
//                    listOf(
//                        ApiEventSource(
//                            "get",
//                            "/",
//                            MethodOptions.builder().build()
//                        )
//                    )
//                )
            }

            val alias = Alias("live", function)

//            LambdaIntegration { alias }
//
//            RestApi("PerformanceApi") {
//                get { LambdaIntegration { alias } }
//                resource("list") {
//                    get { LambdaIntegration { alias } }
//                }
//            }
        }
    }
}

fun IResource.resource(path: String, block: IResource.() -> Unit): IResource =
    Resource.Builder.create(stack, path).parent(this).pathPart(path).build().apply(block)

fun IResource.get(target: Integration): IResource =
    addMethod("GET", target).let { this }

fun IResource.get(block: () -> Integration): IResource =
    addMethod("GET", block()).let { this }

fun LambdaIntegration(alias: () -> Alias): LambdaIntegration = LambdaIntegration(alias())

inline fun LambdaIntegration(alias: Alias, block: LambdaIntegration.Builder.() -> Unit = {}): LambdaIntegration =
    LambdaIntegration.Builder
        .create(alias)
        .passthroughBehavior(PassthroughBehavior.WHEN_NO_MATCH)
        .apply(block)
        .build()

fun Construct.Version(id: String, block: Version.Builder.() -> Unit = {}): Version =
    Version.Builder.create(this, id).apply(block).build()

fun Construct.Alias(id: String, function: Function): Alias =
    Alias.Builder.create(this, id).aliasName(id).version(Version("version") { lambda(function) }).build()

fun Construct.Alias(id: String, block: Alias.Builder.() -> Unit = {}): Alias =
    Alias.Builder.create(this, id).aliasName(id).apply(block).build()

fun Construct.SpecRestApi(id: String, block: SpecRestApi.Builder.() -> Unit = {}): SpecRestApi =
    SpecRestApi.Builder.create(this, id).apply(block).build()

fun Construct.context(key: String): String = node.tryGetContext(key) as String

@Suppress("UNCHECKED_CAST")
fun Construct.contextMap(key: String): Map<String, String> = node.tryGetContext(key) as Map<String, String>

fun Stack.parameter(id: String, type: String = "String", description: String? = null): CfnParameter =
    CfnParameter.Builder.create(this, id).type(type).description(description).build()

fun Construct.userPool(id: String, block: UserPool.Builder.() -> Unit): UserPool =
    UserPool.Builder.create(this, id).userPoolName(id).apply(block).build()

inline fun Stack.RestApi(
    id: String,
    deploy: Boolean = true,
    restApiName: String = Fn.sub("\${AWS::StackName} $id"),
    block: IResource.() -> Unit = {}
): RestApi =
    RestApi.Builder.create(this, id)
        .deploy(deploy)
        .restApiName(restApiName)
        .build()
        .apply { block(root) }

inline fun application(block: App.() -> Unit): CloudAssembly =
    App().apply(block).synth()

inline fun stack(id: String, properties: StackProps? = null, block: Stack.() -> Unit = {}): CloudAssembly =
    application { stack(id, properties, block) }

inline fun Construct.stack(id: String, properties: StackProps? = null, block: Stack.() -> Unit = {}): Stack =
    Stack(this, id, properties).apply(block)

inline fun Construct.stack(id: CfnParameter, properties: StackProps? = null, block: Stack.() -> Unit = {}): Stack =
    stack(id.valueAsString, properties, block)

inline fun Construct.s3(id: String, block: Bucket.Builder.() -> Unit = {}): Bucket =
    Bucket.Builder.create(this, id).apply(block).build()

inline fun Construct.dockerFunction(
    id: String,
    functionName: String = id,
    timeout: Duration = Duration.minutes(15),
    block: DockerImageFunction.Builder.() -> Unit = {}
): Function =
    DockerImageFunction.Builder.create(this, id)
        .functionName(functionName)
        .timeout(timeout)
        .apply(block)
        .build()
        .also { (it.node.defaultChild as CfnResource).overrideLogicalId(id) }

inline fun Construct.function(id: String, block: Function.Builder.() -> Unit = {}): Function =
    Function.Builder.create(this, id).apply(block).build()