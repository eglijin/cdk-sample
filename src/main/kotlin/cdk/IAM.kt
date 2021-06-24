package cdk

import software.amazon.awscdk.core.Fn
import software.amazon.awscdk.services.cognito.IUserPool
import software.amazon.awscdk.services.iam.Effect
import software.amazon.awscdk.services.iam.PolicyStatement
import software.amazon.awscdk.services.iam.PolicyStatementProps
import software.amazon.awscdk.services.lambda.IFunction
import software.amazon.awscdk.services.s3.Bucket
import software.amazon.awscdk.services.sns.Topic
import software.amazon.awscdk.services.sqs.Queue

val kmsPolicyStatement: PolicyStatement = "*" allow "kms:*"

val secretsPolicyStatement: PolicyStatement = listOf(
    Fn.sub("arn:aws:secretsmanager:\${AWS::Region}:\${AWS::AccountId}:secret:gpc/issuing/*"),
    Fn.sub("arn:aws:secretsmanager:\${AWS::Region}:\${AWS::AccountId}:secret:gpc/paynetics/*")
) allow listOf("secretsmanager:GetSecretValue")


fun <T : IFunction> T.withPolicies(vararg policies: PolicyStatement): T = apply {
    role?.apply {
        policies.forEach { addToPrincipalPolicy(it) }
    }
}

fun <T : IFunction> T.crud(vararg buckets: Bucket): T = apply {
    role?.addToPrincipalPolicy(
        buckets.asList().flatMap {
            listOf(it.bucketArn, "${it.bucketArn}/*")
        } allow listOf(
            "s3:GetObject",
            "s3:ListBucket",
            "s3:GetBucketLocation",
            "s3:GetObjectVersion",
            "s3:PutObject",
            "s3:PutObjectAcl",
            "s3:GetLifecycleConfiguration",
            "s3:PutLifecycleConfiguration",
            "s3:DeleteObject"
        )
    )
}

fun <T : IFunction> T.integratesWith(vararg queues: Queue): T = apply {
    role?.addToPrincipalPolicy(
        queues.map { it.queueArn } allow listOf("sqs:*")
    )
}

fun <T : IFunction> T.integratesWith(vararg topics: Topic): T = apply {
    role?.addToPrincipalPolicy(
        topics.map { it.topicArn } allow listOf("sns:*")
    )
}



val sesPolicyStatement: PolicyStatement =
    Fn.sub("arn:aws:ses:*:\${AWS::AccountId}:identity/*") allow "ses:SendEmail"

fun <T : IFunction> T.sendsEmails(): T = apply {
    role?.addToPrincipalPolicy(sesPolicyStatement)
}

fun <T : IFunction> T.integratesWith(vararg pools: IUserPool): T = apply {
    role?.addToPrincipalPolicy(
        pools.map { it.userPoolArn } allow listOf(
            "cognito-identity:*",
            "cognito-idp:*",
            "cognito-sync:*",
        )
    )
}


infix fun String.allow(action: String) = PolicyStatement(
    PolicyStatementProps.builder()
        .effect(Effect.ALLOW)
        .actions(listOf(action))
        .resources(listOf(this))
        .build()
)

infix fun List<String>.allow(actions: List<String>) = PolicyStatement(
    PolicyStatementProps.builder()
        .effect(Effect.ALLOW)
        .actions(actions)
        .resources(this)
        .build()
)