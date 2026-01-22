pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }

    environment {
        // Application settings
        APP_NAME = 'account-online'
        IMAGE_NAME = 'account-online'
        IMAGE_TAG = "${BUILD_NUMBER}"

        // Remote server settings
        REMOTE_HOST = '192.168.103.106'
        REMOTE_USER = 'dbaciftp'
        REMOTE_DIR = '/DATA/deployments/backend/account_online'
        CONTAINER_NAME = 'account-online-app'
        CONTAINER_PORT = '9000'
        HOST_PORT = '9000'
    }

    stages {
        stage('Checkout') {
            steps {
                echo '📥 Checking out code...'
                checkout scm
                sh 'git log -1 --oneline'
            }
        }

        stage('Build Info') {
            steps {
                echo '📋 Build Environment'
                sh '''
                    echo "=== Java Version ==="
                    java -version
                    echo ""
                    echo "=== Maven Version ==="
                    mvn -version
                    echo ""
                    echo "=== Docker Version ==="
                    docker --version
                    echo ""
                    echo "=== Build Info ==="
                    echo "Build: ${BUILD_NUMBER}"
                    echo "Image: ${IMAGE_NAME}:${IMAGE_TAG}"
                    echo "Deploy to: ${REMOTE_HOST}"
                '''
            }
        }

        stage('Clean & Compile') {
            steps {
                echo '🧹 Cleaning and compiling...'
                sh 'mvn clean compile'
            }
        }

        stage('Run Tests') {
            steps {
                echo '🧪 Running tests...'
                sh 'mvn test'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                echo '📦 Building JAR...'
                sh 'mvn package -DskipTests'
                sh 'ls -lh target/*.jar'
            }
        }

        stage('Build Docker Image') {
            steps {
                echo '🐳 Building Docker image...'
                script {
                    sh """
                        docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                        docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest
                        docker images | grep ${IMAGE_NAME}
                    """
                }
            }
        }

        stage('Save Docker Image') {
            steps {
                echo '💾 Saving Docker image to tar file...'
                sh """
                    docker save ${IMAGE_NAME}:${IMAGE_TAG} -o ${IMAGE_NAME}-${IMAGE_TAG}.tar
                    ls -lh ${IMAGE_NAME}-${IMAGE_TAG}.tar
                """
            }
        }

        stage('Stop Old Container') {
            steps {
                echo '🛑 Stopping old container on remote server...'
                sshagent(['deployment-server-ssh']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} "
                            docker stop ${CONTAINER_NAME} || true
                            docker rm ${CONTAINER_NAME} || true
                            echo 'Old container stopped and removed'
                        "
                    '''
                }
            }
        }

        stage('Backup Old Image') {
            steps {
                echo '💾 Backing up old image...'
                sshagent(['deployment-server-ssh']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} "
                            # Tag current latest as backup
                            if docker images ${IMAGE_NAME}:latest -q | grep -q .; then
                                docker tag ${IMAGE_NAME}:latest ${IMAGE_NAME}:backup-\\$(date +%Y%m%d_%H%M%S)
                                echo 'Old image backed up'
                            else
                                echo 'No previous image to backup'
                            fi
                        "
                    '''
                }
            }
        }

        stage('Transfer Docker Image') {
            steps {
                echo '📤 Transferring Docker image to remote server...'
                sshagent(['deployment-server-ssh']) {
                    sh '''
                        # Create deployment directory
                        ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} "
                            mkdir -p ${REMOTE_DIR}
                        "

                        # Copy image tar file
                        scp -o StrictHostKeyChecking=no ${IMAGE_NAME}-${IMAGE_TAG}.tar ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_DIR}/

                        echo 'Image transferred successfully'
                    '''
                }
            }
        }

        stage('Load Docker Image') {
            steps {
                echo '📥 Loading Docker image on remote server...'
                sshagent(['deployment-server-ssh']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} "
                            cd ${REMOTE_DIR}
                            docker load -i ${IMAGE_NAME}-${IMAGE_TAG}.tar
                            docker images | grep ${IMAGE_NAME}

                            # Clean up tar file
                            rm -f ${IMAGE_NAME}-${IMAGE_TAG}.tar
                        "
                    '''
                }
            }
        }

        stage('Start New Container') {
            steps {
                echo '🚀 Starting new container...'
                sshagent(['deployment-server-ssh']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} "
                            docker run -d \
                                --name ${CONTAINER_NAME} \
                                --restart unless-stopped \
                                -p ${HOST_PORT}:${CONTAINER_PORT} \
                                -e SPRING_PROFILES_ACTIVE=prod \
                                -v ${REMOTE_DIR}/logs:/app/logs \
                                ${IMAGE_NAME}:${IMAGE_TAG}

                            echo 'Container started successfully'
                            docker ps | grep ${CONTAINER_NAME}
                        "
                    '''
                }
            }
        }

        stage('Health Check') {
            steps {
                echo '🏥 Performing health check...'
                sshagent(['deployment-server-ssh']) {
                    sh '''
                        echo "Waiting for application to start..."

                        for i in {1..30}; do
                            echo "Attempt $i/30..."

                            STATUS=$(ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} "
                                curl -s -o /dev/null -w '%{http_code}' http://localhost:${HOST_PORT}/actuator/health 2>/dev/null || echo '000'
                            ")

                            if [ "$STATUS" = "200" ]; then
                                echo ""
                                echo "✅ Application is HEALTHY!"

                                ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} "
                                    echo ''
                                    echo '=== Container Status ==='
                                    docker ps | grep ${CONTAINER_NAME}

                                    echo ''
                                    echo '=== Container Logs (last 20 lines) ==='
                                    docker logs --tail 20 ${CONTAINER_NAME}
                                "

                                exit 0
                            fi

                            sleep 5
                        done

                        echo ""
                        echo "❌ Health check FAILED"
                        echo ""
                        echo "=== Container Logs ==="
                        ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} "
                            docker logs --tail 100 ${CONTAINER_NAME}
                        "
                        exit 1
                    '''
                }
            }
        }

        stage('Cleanup Old Images') {
            steps {
                echo '🧹 Cleaning up old Docker images...'
                sshagent(['deployment-server-ssh']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} "
                            # Keep only last 3 builds + latest + backup images
                            docker images ${IMAGE_NAME} --format '{{.ID}} {{.Tag}}' | \
                            grep -v latest | \
                            grep -v backup | \
                            tail -n +4 | \
                            awk '{print \\$1}' | \
                            xargs -r docker rmi -f || true

                            echo 'Cleanup completed'
                        "
                    '''
                }
            }
        }
    }

    post {
        success {
            echo ''
            echo '✅ ================================================='
            echo '✅ DEPLOYMENT SUCCESSFUL!'
            echo '✅ ================================================='
            echo "✅ Application: http://${REMOTE_HOST}:${HOST_PORT}"
            echo "✅ Health: http://${REMOTE_HOST}:${HOST_PORT}/actuator/health"
            echo "✅ Container: ${CONTAINER_NAME}"
            echo "✅ Image: ${IMAGE_NAME}:${IMAGE_TAG}"
            echo '✅ ================================================='
        }

        failure {
            echo ''
            echo '❌ DEPLOYMENT FAILED!'
            echo ''
            echo 'To rollback manually:'
            echo "ssh ${REMOTE_USER}@${REMOTE_HOST}"
            echo "docker stop ${CONTAINER_NAME} && docker rm ${CONTAINER_NAME}"
            echo "docker run -d --name ${CONTAINER_NAME} -p ${HOST_PORT}:${CONTAINER_PORT} ${IMAGE_NAME}:backup-*"
        }

        always {
            // Clean local workspace
            cleanWs(
                deleteDirs: true,
                patterns: [
                    [pattern: 'target/**', type: 'INCLUDE'],
                    [pattern: '*.tar', type: 'INCLUDE']
                ]
            )
        }
    }
}