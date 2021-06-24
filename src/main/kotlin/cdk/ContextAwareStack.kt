package cdk

import software.amazon.awscdk.core.Environment
import software.amazon.awscdk.core.Stack
import software.amazon.awscdk.core.StackProps
import software.amazon.awscdk.services.certificatemanager.Certificate
import software.amazon.awscdk.services.certificatemanager.ICertificate
import software.amazon.awscdk.services.cognito.IUserPool
import software.amazon.awscdk.services.cognito.UserPool
import software.amazon.awscdk.services.ec2.ISecurityGroup
import software.amazon.awscdk.services.ec2.IVpc
import software.amazon.awscdk.services.ec2.SecurityGroup
import software.amazon.awscdk.services.ec2.Subnet
import software.amazon.awscdk.services.ec2.SubnetSelection
import software.amazon.awscdk.services.ec2.Vpc
import software.amazon.awscdk.services.ec2.VpcLookupOptions

class ContextAwareStack(
    app: ContextAwareApplication,
    id: String,
    properties: StackProps.Builder.() -> Unit = {}
) : Stack(
    app, id,
    StackProps.builder()
        .env(
            Environment.builder()
                .account(app.account)
                .region(app.region)
                .build()
        )
        .apply(properties)
        .build()
) {
    private val deploymentConfiguration: DeploymentConfiguration =
        (app.config[account] ?: throw RuntimeException("account not found"))[app.env]
            ?: throw RuntimeException("Environment not Found")
    val env: String = app.env
    val kmsKeyId: String = deploymentConfiguration.kmsKeyId
    val allowMethods: String = deploymentConfiguration.allowMethods
    val allowHeaders: String = deploymentConfiguration.allowHeaders
    val allowOrigin: String = deploymentConfiguration.allowOrigin
    val alarmEmail: String = deploymentConfiguration.alarmEmail
    val provisioning: Int? = deploymentConfiguration.provisioning

    val corporateHost: String = deploymentConfiguration.corporateHost
    val consumerHost: String = deploymentConfiguration.consumerHost

    val issuingEmailSender: String = deploymentConfiguration.issuingEmailSender
    val issuingEmailSenderArn: String = deploymentConfiguration.issuingEmailSenderArn

    val backofficeEmailSender: String = deploymentConfiguration.backofficeEmailSender
    val backofficeEmailSenderArn: String = deploymentConfiguration.backofficeEmailSenderArn
    val domain: String = deploymentConfiguration.domain
    val corporateLegacyPoolArn: String? = deploymentConfiguration.corporateLegacyPoolArn
    val corporateLegacyPoolClientId: String? = deploymentConfiguration.corporateLegacyPoolClientId
    val consumerLegacyPoolArn: String? = deploymentConfiguration.consumerLegacyPoolArn
    val consumerLegacyPoolClientId: String? = deploymentConfiguration.consumerLegacyPoolClientId

    val certificate: ICertificate = deploymentConfiguration.certificateArn.let {
        Certificate.fromCertificateArn(this, it.substringAfterLast("/"), it)
    }

    val vpc: IVpc =
        deploymentConfiguration.vpcId.let {
            Vpc.fromLookup(this, it, VpcLookupOptions.builder().vpcId(it).build())
        }

    val securityGroups: List<ISecurityGroup> = deploymentConfiguration.securityGroupIds.map {
        SecurityGroup.fromSecurityGroupId(this, it, it)
    }

    val subnetSelection: SubnetSelection =
        SubnetSelection
            .builder()
            .subnets(deploymentConfiguration.vpcSubnetIds.map {
                Subnet.fromSubnetId(this, it, it)
            })
            .build()

    val baseEnvironmentVariables: Map<String, String> = mapOf(
        "region" to region,
        "kmsKeyId" to kmsKeyId,
        "environment" to env,
        "stackName" to stackName,
    )

    val corporateUserPool: IUserPool by lazy {
        deploymentConfiguration.corporateIdpArn.let {
            UserPool.fromUserPoolArn(this, it.substringAfterLast("/"), it)
        }
    }

    val consumerUserPool: IUserPool by lazy {
        deploymentConfiguration.consumerIdpArn.let {
            UserPool.fromUserPoolArn(this, it.substringAfterLast("/"), it)
        }
    }
}