package cdk

import software.amazon.awscdk.core.App
import software.amazon.awscdk.core.Environment

class ContextAwareApplication(
    val config: Map<String, Map<String, DeploymentConfiguration>>,
    private val _account: String? = null,
    val _env: String? = null
) : App() {

    val env: String get() = _env ?: context("env")

    override fun getAccount(): String = _account ?: context("account")

    val configuration: DeploymentConfiguration =
        config[account]?.let { it[env] } ?: throw RuntimeException("Configuration not found for $account/$env")

    val environment: Environment = Environment.builder().account(account).region(region).build()

    override fun getRegion(): String = configuration.region
}