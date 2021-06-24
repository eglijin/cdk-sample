package cdk

import software.amazon.awscdk.core.Stack
import software.amazon.awscdk.services.lambda.Function
import software.amazon.awscdk.services.lambda.eventsources.SnsEventSource
import software.amazon.awscdk.services.sns.ITopic
import software.amazon.awscdk.services.sns.Topic
import software.amazon.awscdk.services.sns.TopicProps

fun Stack.topic(id: String): Topic =
    Topic(
        this, id, TopicProps.builder()
            .topicName("$stackName-$id")
            .build()
    )

fun Function.snsEvent(topic: ITopic): Function = apply {
    addEventSource(SnsEventSource(topic))
}