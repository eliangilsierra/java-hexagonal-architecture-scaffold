#!/bin/bash
# Runs automatically once LocalStack is ready (mounted at
# /etc/localstack/init/ready.d in docker-compose, and reused as-is by the
# Testcontainers-based integration test). Creates the SNS topic, the SQS
# queue, and subscribes the queue to the topic with raw message delivery so
# the JSON payload arrives on the queue without an SNS envelope wrapped
# around it.
set -e

TOPIC_ARN=$(awslocal sns create-topic --name orders-created-topic --query 'TopicArn' --output text)
QUEUE_URL=$(awslocal sqs create-queue --queue-name orders-created-queue --query 'QueueUrl' --output text)
QUEUE_ARN=$(awslocal sqs get-queue-attributes --queue-url "$QUEUE_URL" --attribute-names QueueArn --query 'Attributes.QueueArn' --output text)

awslocal sns subscribe \
    --topic-arn "$TOPIC_ARN" \
    --protocol sqs \
    --notification-endpoint "$QUEUE_ARN" \
    --attributes RawMessageDelivery=true

echo "orders-created-topic ($TOPIC_ARN) -> orders-created-queue ($QUEUE_URL) subscribed with raw message delivery."
