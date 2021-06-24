package cdk

import software.amazon.awscdk.core.Duration
import software.amazon.awscdk.services.cognito.AuthFlow
import software.amazon.awscdk.services.cognito.CfnUserPool
import software.amazon.awscdk.services.cognito.UserPool
import software.amazon.awscdk.services.cognito.UserPoolClient
import software.amazon.awscdk.services.cognito.UserPoolClientIdentityProvider

fun ContextAwareStack.userPool(id: String = "$stackName-idp", block: UserPool.Builder.() -> Unit): UserPool =
    UserPool.Builder.create(this, id)
        .userPoolName(if ("idp" in stackName) stackName else "$stackName-idp")
        .apply(block)
        .build()

fun UserPool.withEmailSender(sender: String, senderArn: String): UserPool = apply {
    (node.defaultChild as CfnUserPool).apply {
        setEmailConfiguration(
            CfnUserPool.EmailConfigurationProperty.builder()
                .emailSendingAccount("DEVELOPER")
                .from(sender)
                .sourceArn(senderArn)
                .build()
        )
    }
}

inline fun UserPool.withClient(block: UserPoolClient.Builder.() -> Unit = {}): UserPoolClient =
    UserPoolClient.Builder
        .create(this, "${stack.stackName}-idp-client")
        .userPoolClientName("${stack.stackName}-idp-client")
        .userPool(this)
        .generateSecret(false)
        .authFlows(AuthFlow.builder().userPassword(true).userSrp(true).build())
        .refreshTokenValidity(Duration.days(30))
        .preventUserExistenceErrors(true)
        .supportedIdentityProviders(listOf(UserPoolClientIdentityProvider.COGNITO))
        .apply(block)
        .build()
