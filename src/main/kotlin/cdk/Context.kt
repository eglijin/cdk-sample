package cdk

import software.amazon.awscdk.core.Construct

fun Construct.context(key: String): String = node.tryGetContext(key) as String

@Suppress("UNCHECKED_CAST")
fun Construct.contextMap(key: String): Map<String, String> = node.tryGetContext(key) as Map<String, String>
