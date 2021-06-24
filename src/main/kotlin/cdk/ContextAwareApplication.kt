package cdk

import software.amazon.awscdk.core.App

class ContextAwareApplication(
    val config: Map<String, Map<String, DeploymentConfiguration>>,
    private val account: String? = null,
    private val region: String? = null,
    val _env: String? = null
) : App() {

    val env: String get() = _env ?: context("env")

    override fun getAccount(): String = account ?: context("account")

    override fun getRegion(): String =
        region
            ?: (config[getAccount()] as Map<String, DeploymentConfiguration>)[env]?.region?: throw RuntimeException("Region Not Found")
}