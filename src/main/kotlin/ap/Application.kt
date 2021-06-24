@file:JvmName("Application")

package ap

import api
import cdk.*
import cors


fun main() {
    application(context) {
        stack("cors") {
            parameter("AllowOrigin", default = allowOrigin)
            parameter("AllowMethods", default = allowMethods)
            parameter("AllowHeaders", default = allowHeaders)
            api("cors-test", "./api/apigw.yml")
//                .cors(allowHeaders, allowOrigin, allowMethods)
            dockerFunction("", "ap.Greeter", 512)
                .withAlias("live")
        }
    }
}


val context: Map<String, Map<String, DeploymentConfiguration>> = mapOf(
    "855718874679" to mapOf(
        "test" to DeploymentConfiguration(
            domain = "api.iaecsp.org",
            corporateHost = "corporate.test.iaecsp.org",
            consumerHost = "consumer.test.iaecsp.org",
            region = "us-west-2",
            corporateIdpArn = "arn:aws:cognito-idp:us-west-2:855718874679:userpool/us-west-2_kJGpP5r83",
            corporateLegacyPoolArn = "arn:aws:cognito-idp:us-west-2:855718874679:userpool/us-west-2_kJGpP5r83",
            corporateLegacyPoolClientId = "69dqsc23d661ej7tkadjc9lv6v",
            consumerIdpArn = "arn:aws:cognito-idp:us-west-2:855718874679:userpool/us-west-2_3ZgW1AC7J",
            consumerLegacyPoolArn = "arn:aws:cognito-idp:us-west-2:855718874679:userpool/us-west-2_3ZgW1AC7J",
            consumerLegacyPoolClientId = "2u07fjltb8mu4iigekodu3rgli",
            kmsKeyId = "2ad1f6be-2f4a-4538-ad07-4cbf45401e31",
            vpcId = "vpc-38e8a25f",
            securityGroupIds = listOf("sg-0d7ef638f933bffed"),
            vpcSubnetIds = listOf(
                "subnet-024aaea51ea65d13a",
                "subnet-0a6131fe68601ce09"
            ),
            allowHeaders = "'*'",
            allowMethods = "'*'",
            allowOrigin = "'*'",
            alarmEmail = "monitoring@agilepartners.eu",
            certificateArn = "arn:aws:acm:us-west-2:855718874679:certificate/ca7053ae-6830-4fb9-96c8-dfb4128a48ef",
            issuingEmailSender = "IAECSP <no-reply@iaecsp.org>",
            issuingEmailSenderArn = "arn:aws:ses:us-west-2:855718874679:identity/no-reply@iaecsp.org",
            backofficeEmailSender = "Backoffice <no-reply@payall.com>",
            backofficeEmailSenderArn = "arn:aws:ses:us-west-2:855718874679:identity/no-reply@payall.com",
        ),
        "sandbox" to DeploymentConfiguration(
            domain = "api.iaecsp.org",
            corporateHost = "corporate.test.iaecsp.org",
            consumerHost = "consumer.test.iaecsp.org",
            region = "us-west-2",
            corporateIdpArn = "arn:aws:cognito-idp:us-west-2:855718874679:userpool/us-west-2_kJGpP5r83",
            corporateLegacyPoolArn = "arn:aws:cognito-idp:us-west-2:855718874679:userpool/us-west-2_kJGpP5r83",
            corporateLegacyPoolClientId = "69dqsc23d661ej7tkadjc9lv6v",
            consumerIdpArn = "arn:aws:cognito-idp:us-west-2:855718874679:userpool/us-west-2_3ZgW1AC7J",
            consumerLegacyPoolArn = "arn:aws:cognito-idp:us-west-2:855718874679:userpool/us-west-2_3ZgW1AC7J",
            consumerLegacyPoolClientId = "2u07fjltb8mu4iigekodu3rgli",
            kmsKeyId = "2ad1f6be-2f4a-4538-ad07-4cbf45401e31",
            vpcId = "vpc-38e8a25f",
            securityGroupIds = listOf("sg-0d7ef638f933bffed"),
            vpcSubnetIds = listOf(
                "subnet-024aaea51ea65d13a",
                "subnet-0a6131fe68601ce09"
            ),
            allowHeaders = "'*'",
            allowMethods = "'*'",
            allowOrigin = "'*'",
            alarmEmail = "monitoring@agilepartners.eu",
            certificateArn = "arn:aws:acm:us-west-2:855718874679:certificate/ca7053ae-6830-4fb9-96c8-dfb4128a48ef",
            issuingEmailSender = "IAECSP <no-reply@iaecsp.org>",
            issuingEmailSenderArn = "arn:aws:ses:us-west-2:855718874679:identity/no-reply@iaecsp.org",
            backofficeEmailSender = "Backoffice <no-reply@payall.com>",
            backofficeEmailSenderArn = "arn:aws:ses:us-west-2:855718874679:identity/no-reply@payall.com",
        )
    )
)