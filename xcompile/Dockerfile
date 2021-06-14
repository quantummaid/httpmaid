FROM openjdk:11-jdk-slim

RUN apt-get update --yes
RUN apt-get upgrade --yes
RUN apt-get autoremove --yes
RUN apt-get install --yes --no-install-recommends unzip curl gnupg python3-pip jq bc git build-essential libz-dev zlib1g-dev
RUN curl -L "https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-21.0.0.2/graalvm-ce-java11-linux-amd64-21.0.0.2.tar.gz" -o "graalvm.tar.gz" && \
    tar -xzf graalvm.tar.gz
ENV PATH="/graalvm-ce-java11-21.0.0.2/bin:${PATH}"
ENV JAVA_HOME="/graalvm-ce-java11-21.0.0.2/"
RUN gu install native-image
RUN echo "alias python='python3'" >> ~/.bashrc
RUN . ~/.bashrc
RUN ln /usr/bin/python3 /usr/bin/python
RUN pip3 --no-cache-dir install setuptools
RUN pip3 --no-cache-dir install aws-sam-cli
RUN rm -rf /var/lib/apt/lists/*

ARG MAVEN_MAJOR_VERSION=3
ARG MAVEN_VERSION=3.6.3

RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && \
    unzip awscliv2.zip && \
    ./aws/install && \
    rm awscliv2.zip && \
    curl https://downloads.apache.org/maven/maven-${MAVEN_MAJOR_VERSION}/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.zip > apache-maven-${MAVEN_VERSION}-bin.zip && \
    unzip apache-maven-${MAVEN_VERSION}-bin.zip && \
    rm apache-maven-${MAVEN_VERSION}-bin.zip && \
    ln -s /apache-maven-${MAVEN_VERSION}/bin/mvn /usr/bin/mvn

COPY ./ /usr/local/app
WORKDIR /usr/local/app
