FROM public.ecr.aws/lambda/java:11

# Copy function code and runtime dependencies from Gradle layout
COPY var /var
#COPY dependency/* ${LAMBDA_TASK_ROOT}/lib/