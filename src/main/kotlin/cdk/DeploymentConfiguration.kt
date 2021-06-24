package cdk

data class DeploymentConfiguration(
    val domain: String,
    val corporateHost: String,
    val consumerHost: String,
    val region: String,
    val corporateIdpArn: String,
    val consumerIdpArn: String,
    val kmsKeyId: String,
    val vpcId: String,
    val securityGroupIds: List<String>,
    val vpcSubnetIds: List<String>,
    val allowHeaders: String,
    val allowMethods: String,
    val allowOrigin: String,
    val alarmEmail: String,
    val certificateArn: String,
    val issuingEmailSender: String,
    val issuingEmailSenderArn: String,
    val backofficeEmailSender: String,
    val backofficeEmailSenderArn: String,
    val provisioning: Int? = null,
    val corporateLegacyPoolArn: String? = null,
    val corporateLegacyPoolClientId: String? = null,
    val consumerLegacyPoolArn: String? = null,
    val consumerLegacyPoolClientId: String? = null
)
