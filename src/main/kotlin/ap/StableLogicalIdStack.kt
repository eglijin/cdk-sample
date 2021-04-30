package ap

import software.amazon.awscdk.core.CfnElement
import software.amazon.awscdk.core.Construct
import software.amazon.awscdk.core.Stack
import software.amazon.awscdk.core.StackProps

class StableLogicalIdStack(construct: Construct, id: String, props: StackProps?) : Stack(construct, id, props) {
    override fun allocateLogicalId(cfnElement: CfnElement): String {
        println(cfnElement.node.id)
        println(cfnElement.node.addr)
        println(cfnElement.node.path)
        println("===")
        return cfnElement.node.id.let { if ("Function" in it) it else super.allocateLogicalId(cfnElement) }
    }
}