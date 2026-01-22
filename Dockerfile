# Use your existing Spring Boot image
FROM springboot-java17-app:1.0.0

USER root
WORKDIR /build

# Install JDK (needed for Maven compilation)
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk wget curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Find and set correct JAVA_HOME
RUN JAVA_PATH=$(dirname $(dirname $(readlink -f $(which java)))) && \
    echo "JAVA_HOME found at: $JAVA_PATH" && \
    echo "export JAVA_HOME=$JAVA_PATH" >> /etc/environment && \
    echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> /etc/environment

# Set JAVA_HOME for this build
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Verify Java and Javac
RUN java -version && javac -version

# Download and install Maven
RUN MAVEN_VERSION=3.9.9 && \
    mkdir -p /usr/share/maven /usr/share/maven/ref && \
    wget -q https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz -O /tmp/maven.tar.gz || \
    curl -fsSL https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz -o /tmp/maven.tar.gz && \
    tar xzf /tmp/maven.tar.gz -C /usr/share/maven --strip-components=1 && \
    ln -s /usr/share/maven/bin/mvn /usr/bin/mvn && \
    rm -f /tmp/maven.tar.gz

# Verify Maven with correct Java
RUN mvn -version

# Copy project files
COPY pom.xml .
COPY src ./src

# Build with correct JAVA_HOME
RUN mvn clean package -DskipTests

# Verify JAR was created
RUN ls -lh target/*.jar

# Prepare app directory
RUN mkdir -p /app && \
    cp target/account_online.jar /app/app.jar && \
    ls -lh /app/app.jar

# Clean build directory
WORKDIR /app
RUN rm -rf /build

# Create user
RUN groupadd -r spring 2>/dev/null || true && \
    useradd -r -g spring spring 2>/dev/null || true && \
    chown -R spring:spring /app

USER spring

EXPOSE 9000

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:9000/actuator/health 2>/dev/null || \
        curl -f http://localhost:9000/actuator/health 2>/dev/null || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]