package cdk

import software.amazon.awscdk.core.Stack
import software.amazon.awscdk.services.s3.Bucket
import software.amazon.awscdk.services.s3.BucketProps

inline fun Stack.bucket(id: String, block: BucketProps.Builder.() -> Unit = {}): Bucket =
    Bucket(
        this,
        id,
        BucketProps.builder()
            .bucketName("$stackName-$id")
            .apply(block)
            .build()
    )
