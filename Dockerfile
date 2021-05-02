FROM public.ecr.aws/lambda/java:11

# Copy function code and runtime dependencies from Gradle layout
COPY classes/kotlin/main ${LAMBDA_TASK_ROOT}
COPY dependency/* ${LAMBDA_TASK_ROOT}/lib/