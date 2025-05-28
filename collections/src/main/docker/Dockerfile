FROM node:24-alpine

ENV COLLECTION="integration-tests"
ENV ENV="dev"

WORKDIR /app

RUN npm install -g @usebruno/cli

COPY ./src/main/resources /app/tests/
COPY ./src/main/scripts/* /app/scripts/

ENTRYPOINT ["/app/scripts/entrypoint.sh"]