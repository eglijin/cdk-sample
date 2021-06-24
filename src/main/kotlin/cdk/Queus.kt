package cdk

import software.amazon.awscdk.core.Duration
import software.amazon.awscdk.core.Stack
import software.amazon.awscdk.services.lambda.Function
import software.amazon.awscdk.services.lambda.eventsources.SqsEventSource
import software.amazon.awscdk.services.lambda.eventsources.SqsEventSourceProps
import software.amazon.awscdk.services.sqs.IQueue
import software.amazon.awscdk.services.sqs.Queue
import software.amazon.awscdk.services.sqs.QueueProps

fun Stack.queue(id: String): Queue =
    Queue(
        this,
        id,
        QueueProps.builder()
            .queueName("$stackName-$id")
            .visibilityTimeout(Duration.seconds(300))
            .build()
    )

fun Function.sqsEvent(sqs: IQueue, batch: Int = 10): Function = apply {
    addEventSource(
        SqsEventSource(
            sqs, SqsEventSourceProps.builder()
                .batchSize(batch)
                .build()
        )
    )
}