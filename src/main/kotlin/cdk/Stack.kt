package cdk

import software.amazon.awscdk.core.StackProps

inline fun ContextAwareApplication.stack(
    id: String,
    noinline properties: StackProps.Builder.() -> Unit = {},
    block: ContextAwareStack.() -> Unit = {}
): ContextAwareStack {
    return ContextAwareStack(this, "$id-$env", properties)
        .apply(block)
}