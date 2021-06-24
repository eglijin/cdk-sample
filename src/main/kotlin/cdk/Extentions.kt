package cdk

import software.amazon.awscdk.core.CfnParameter
import software.amazon.awscdk.core.Construct
import software.amazon.awscdk.core.Duration
import software.amazon.awscdk.core.Stack
import software.amazon.awscdk.cxapi.CloudAssembly
import software.amazon.awscdk.services.events.Rule
import software.amazon.awscdk.services.events.RuleProps
import software.amazon.awscdk.services.events.Schedule
import software.amazon.awscdk.services.events.targets.LambdaFunction
import software.amazon.awscdk.services.lambda.Function
import software.amazon.awscdk.services.s3.assets.Asset
import software.amazon.awscdk.services.s3.assets.AssetProps

fun Function.schedule(duration: Duration): Rule =
    Rule(
        this, "${node.id}Timer", RuleProps.builder()
            .schedule(Schedule.rate(duration))
            .targets(listOf(LambdaFunction(this)))
            .build()
    )

fun Stack.parameter(
    id: String,
    type: String = "String",
    description: String? = null,
    default: Any? = null
): CfnParameter =
    CfnParameter.Builder
        .create(this, id)
        .type(type)
        .description(description).defaultValue(default)
        .build()

inline fun application(
    config: Map<String, Map<String, DeploymentConfiguration>>,
    block: ContextAwareApplication.() -> Unit
): CloudAssembly =
    ContextAwareApplication(config)
        .apply(block)
        .synth()

fun Construct.asset(path: String): Asset = Asset(
    this, path.substringAfterLast('/').substringBefore('.'),
    AssetProps.builder().path(path).build()
)