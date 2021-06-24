package cdk

import software.amazon.awscdk.core.Fn
import software.amazon.awscdk.core.Stack
import software.amazon.awscdk.services.iam.Effect
import software.amazon.awscdk.services.iam.PolicyDocument
import software.amazon.awscdk.services.iam.PolicyDocumentProps
import software.amazon.awscdk.services.iam.PolicyStatement
import software.amazon.awscdk.services.iam.PolicyStatementProps
import software.amazon.awscdk.services.iam.Role
import software.amazon.awscdk.services.iam.RoleProps
import software.amazon.awscdk.services.iam.ServicePrincipal
import software.amazon.awscdk.services.s3.Bucket
import software.amazon.awscdk.services.sns.Topic
import software.amazon.awscdk.services.sqs.Queue

fun Stack.issuingLambdaRole(
    env: String,
    topics: List<Topic>,
    queues: List<Queue>,
    buckets: List<Bucket>
): Role =
    Role(
        this,
        "IssuingLambdaExecutionRole",
        RoleProps.builder()
            .assumedBy(ServicePrincipal("lambda.amazonaws.com"))
            .description("Base issuing lambda Role")
            .inlinePolicies(
                mapOf(
                    "issuingKMSaccess" to policy("*" allow "kms:*"),
                    "issuingLogsAccess" to policy(
                        allowActions(
                            "logs:CreateLogGroup",
                            "logs:CreateLogStream",
                            "logs:PutLogEvents"
                        )
                    ),
                    "issuingVPCaccess" to policy(
                        allowActions(
                            "ec2:CreateNetworkInterface",
                            "ec2:DescribeNetworkInterfaces",
                            "ec2:DeleteNetworkInterface",
                            "ec2:AssignPrivateIpAddresses",
                            "ec2:UnassignPrivateIpAddresses",
                        )
                    ),
                    "issuingCognitoAccess" to policy(
                        allowActions(
                            "cognito-identity:*",
                            "cognito-idp:*",
                            "cognito-sync:*",
                        )
                    ),
                    "issuingS3Access" to policy(
                        buckets.flatMap {
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
                    ),
                    "issuingSQSAccess" to policy(
                        queues.map {
                            it.queueArn
                        } allow listOf(
                            "sqs:*",
                        )
                    ),
                    "issuingSNSAccess" to policy(
                        topics.map {
                            it.topicArn
                        } allow listOf(
                            "sns:*",
                        )
                    ),
                    "$stackName-ses-policy" to policy(
                        listOf(Fn.sub("arn:aws:ses:*:\${AWS::AccountId}:identity/*")) allow listOf(
                            "ses:SendEmail",
                        )
                    ),
                    "$stackName-secrets-manager" to policy(
                        listOf(
                            Fn.sub("arn:aws:secretsmanager:\${AWS::Region}:\${AWS::AccountId}:secret:gpc/issuing/$env-*"),
                            Fn.sub("arn:aws:secretsmanager:\${AWS::Region}:\${AWS::AccountId}:secret:gpc/paynetics/*")
                        ) allow listOf(
                            "secretsmanager:GetSecretValue",
                        )
                    ),
                )
            )
            .build()
    )

fun policy(vararg statements: PolicyStatement): PolicyDocument =
    PolicyDocument(PolicyDocumentProps.builder().statements(statements.asList()).build())

fun allowActions(vararg actions: String) =
    PolicyStatement(
        PolicyStatementProps.builder()
            .effect(Effect.ALLOW)
            .actions(actions.asList())
            .resources(listOf("*"))
            .build()
    )
