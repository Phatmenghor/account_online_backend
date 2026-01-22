pipeline {
    agent any

    environment {
        // Application settings
        APP_NAME = 'account-online'
        IMAGE_NAME = 'account-online'
        IMAGE_TAG = "${BUILD_NUMBER}"
        JAR_NAME = 'account_online.jar'

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
                echo '📥 Checking out code from GitLab...'
                checkout scm
                sh '''
                    echo "=== Git Information ==="
                    git log -1 --oneline
                    git branch -a
                    echo ""
                    echo "=== Repository Files ==="
                    ls -la
                '''
            }
        }

        stage('Build Info') {
            steps {
                echo '📋 Build Environment Information'
                sh '''
                    echo "========================================="
                    echo "BUILD INFORMATION"
                    echo "========================================="
                    echo "Build Number: ${BUILD_NUMBER}"
                    echo "Job Name: ${JOB_NAME}"
                    echo "Workspace: ${WORKSPACE}"
                    echo "Git Branch: ${GIT_BRANCH}"
                    echo "Git Commit: ${GIT_COMMIT}"
                    echo ""
                    echo "=== Docker Version ==="
                    docker --version
                    docker info | grep "Server Version" || true
                    echo ""
                    echo "=== Disk Space ==="
                    df -h | grep -E "Filesystem|/var/lib/docker|/$" || df -h
                    echo ""
                    echo "=== Deployment Configuration ==="
                    echo "Server: ${REMOTE_HOST}"
                    echo "Port: ${HOST_PORT}"
                    echo "Container: ${CONTAINER_NAME}"
                    echo "Image: ${IMAGE_NAME}:${IMAGE_TAG}"
                    echo "JAR: ${JAR_NAME}"
                    echo "========================================="
                '''
            }
        }

        stage('Validate Files') {
            steps {
                echo '🔍 Validating required files...'
                sh '''
                    echo "Checking required files..."

                    if [ ! -f "Dockerfile" ]; then
                        echo "❌ ERROR: Dockerfile not found!"
                        exit 1
                    fi
                    echo "✓ Dockerfile found"
                    cat Dockerfile | head -10

                    if [ ! -f "pom.xml" ]; then
                        echo "❌ ERROR: pom.xml not found!"
                        exit 1
                    fi
                    echo "✓ pom.xml found"

                    # Check finalName in pom.xml
                    if grep -q "<finalName>account_online</finalName>" pom.xml; then
                        echo "✓ finalName correctly set to account_online"
                    else
                        echo "⚠ WARNING: finalName might not be set correctly"
                    fi

                    if [ ! -d "src" ]; then
                        echo "❌ ERROR: src directory not found!"
                        exit 1
                    fi
                    echo "✓ src directory found"

                    echo ""
                    echo "✅ All required files present"
                '''
            }
        }

        stage('Build Docker Image') {
            steps {
                echo '🐳 Building Docker image (Maven runs inside Docker)...'
                script {
                    sh """
                        echo "Building Docker image: ${IMAGE_NAME}:${IMAGE_TAG}"
                        echo "This will:"
                        echo "  1. Download Maven dependencies"
                        echo "  2. Compile Java code"
                        echo "  3. Run tests"
                        echo "  4. Package ${JAR_NAME}"
                        echo "  5. Create runtime image"
                        echo ""

                        # Build the image (Maven runs inside Docker)
                        docker build \
                            --build-arg BUILD_NUMBER=${BUILD_NUMBER} \
                            --build-arg GIT_COMMIT=${GIT_COMMIT} \
                            -t ${IMAGE_NAME}:${IMAGE_TAG} \
                            . 2>&1 | tee docker-build.log

                        # Tag as latest
                        docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest

                        echo ""
                        echo "=== Built Images ==="
                        docker images | grep ${IMAGE_NAME} || docker images | head -5

                        echo ""
                        echo "=== Image Details ==="
                        docker inspect ${IMAGE_NAME}:${IMAGE_TAG} --format='Size: {{.Size}} bytes ({{ div .Size 1048576 }} MB)'
                        docker inspect ${IMAGE_NAME}:${IMAGE_TAG} --format='Created: {{.Created}}'
                    """
                }
            }
        }

        stage('Test Docker Image') {
            steps {
                echo '🧪 Testing Docker image...'
                sh """
                    echo "Running validation tests on image..."

                    echo ""
                    echo "=== Test 1: Java Version ==="
                    docker run --rm ${IMAGE_NAME}:${IMAGE_TAG} java -version

                    echo ""
                    echo "=== Test 2: Check JAR File ==="
                    docker run --rm ${IMAGE_NAME}:${IMAGE_TAG} ls -lh /app/app.jar

                    echo ""
                    echo "=== Test 3: Check User ==="
                    docker run --rm ${IMAGE_NAME}:${IMAGE_TAG} whoami

                    echo ""
                    echo "=== Test 4: Check Port ==="
                    docker inspect ${IMAGE_NAME}:${IMAGE_TAG} --format='Exposed Ports: {{.Config.ExposedPorts}}'

                    echo ""
                    echo "✅ All Docker image tests passed"
                """
            }
        }

        stage('Save Docker Image') {
            steps {
                echo '💾 Saving Docker image to tar file...'
                sh """
                    echo "Exporting image to tar file..."
                    docker save ${IMAGE_NAME}:${IMAGE_TAG} -o ${IMAGE_NAME}-${IMAGE_TAG}.tar

                    echo ""
                    echo "=== Tar File Info ==="
                    ls -lh ${IMAGE_NAME}-${IMAGE_TAG}.tar

                    # Calculate size in MB
                    SIZE=\$(du -m ${IMAGE_NAME}-${IMAGE_TAG}.tar | cut -f1)
                    echo "Tar file size: \${SIZE} MB"

                    if [ \$SIZE -gt 500 ]; then
                        echo "⚠ WARNING: Image is quite large (\${SIZE} MB)"
                    else
                        echo "✓ Image size is reasonable"
                    fi
                """
            }
        }

        stage('Stop Old Container') {
            steps {
                echo '🛑 Stopping old container on remote server...'
                sshagent(['deployment-server-ssh']) {
                    sh '''
                        echo "Connecting to ${REMOTE_HOST}..."

                        ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} "
                            echo '=== Checking for existing container ==='

                            if docker ps -a | grep -q ${CONTAINER_NAME}; then
                                echo 'Found existing container: ${CONTAINER_NAME}'

                                # Get container info before stopping
                                echo 'Container uptime:'
                                docker ps --filter name=${CONTAINER_NAME} --format 'Status: {{.Status}}'

                                echo 'Stopping container...'
                                docker stop ${CONTAINER_NAME} || true

                                echo 'Removing container...'
                                docker rm ${CONTAINER_NAME} || true

                                echo '✓ Old container stopped and removed'
                            else
                                echo '○ No existing container found (first deployment)'
                            fi
                        "
                    '''
                }
            }
        }

        stage('Backup Old Image') {
            steps {
                echo '💾 Backing up old Docker image...'
                sshagent(['deployment-server-ssh']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} "
                            echo '=== Backing up previous image ==='

                            # Check if latest image exists
                            if docker images ${IMAGE_NAME}:latest -q | grep -q .; then
                                BACKUP_TAG=backup-\\$(date +%Y%m%d_%H%M%S)
                                echo \\\"Creating backup: ${IMAGE_NAME}:\\${BACKUP_TAG}\\\"
                                docker tag ${IMAGE_NAME}:latest ${IMAGE_NAME}:\\${BACKUP_TAG}

                                echo \\\"✓ Backup created: \\${BACKUP_TAG}\\\"

                                # Keep only last 3 backups
                                echo 'Cleaning old backups (keeping last 3)...'
                                docker images ${IMAGE_NAME} --format '{{.Tag}}' | \
                                grep '^backup-' | \
                                sort -r | \
                                tail -n +4 | \
                                xargs -I {} docker rmi ${IMAGE_NAME}:{} 2>/dev/null || true
                            else
                                echo '○ No previous image to backup (first deployment)'
                            fi

                            echo ''
                            echo '=== Available Images ==='
                            docker images ${IMAGE_NAME}
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
                        echo "Creating deployment directory..."
                        ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} "
                            mkdir -p ${REMOTE_DIR}
                            mkdir -p ${REMOTE_DIR}/logs
                            mkdir -p ${REMOTE_DIR}/backups
                        "

                        echo ""
                        echo "Transferring image tar file..."
                        echo "Source: ${IMAGE_NAME}-${IMAGE_TAG}.tar"
                        echo "Destination: ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_DIR}/"

                        # Transfer with progress
                        scp -o StrictHostKeyChecking=no \
                            ${IMAGE_NAME}-${IMAGE_TAG}.tar \
                            ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_DIR}/

                        echo ""
                        echo "✓ Image transferred successfully"

                        # Verify transfer
                        ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} "
                            ls -lh ${REMOTE_DIR}/${IMAGE_NAME}-${IMAGE_TAG}.tar
                        "
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

                            echo 'Loading Docker image from tar file...'
                            docker load -i ${IMAGE_NAME}-${IMAGE_TAG}.tar

                            echo ''
                            echo '=== Loaded Images ==='
                            docker images | grep ${IMAGE_NAME} | head -5

                            echo ''
                            echo 'Cleaning up tar file...'
                            rm -f ${IMAGE_NAME}-${IMAGE_TAG}.tar

                            echo '✓ Image loaded successfully'
                        "
                    '''
                }
            }
        }

        stage('Start New Container') {
            steps {
                echo '🚀 Starting new container on remote server...'
                sshagent(['deployment-server-ssh']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} "
                            echo '=== Starting Container ==='
                            echo 'Container Name: ${CONTAINER_NAME}'
                            echo 'Image: ${IMAGE_NAME}:${IMAGE_TAG}'
                            echo 'Port Mapping: ${HOST_PORT}:${CONTAINER_PORT}'
                            echo ''

                            docker run -d \
                                --name ${CONTAINER_NAME} \
                                --restart unless-stopped \
                                -p ${HOST_PORT}:${CONTAINER_PORT} \
                                -e SPRING_PROFILES_ACTIVE=prod \
                                -e SERVER_PORT=${CONTAINER_PORT} \
                                -v ${REMOTE_DIR}/logs:/app/logs \
                                ${IMAGE_NAME}:${IMAGE_TAG}

                            echo ''
                            echo '✓ Container started successfully'
                            echo ''
                            echo '=== Container Status ==='
                            docker ps | grep ${CONTAINER_NAME}

                            echo ''
                            echo '=== Container Details ==='
                            docker inspect ${CONTAINER_NAME} --format='ID: {{.Id}}'
                            docker inspect ${CONTAINER_NAME} --format='IP: {{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}'
                            docker inspect ${CONTAINER_NAME} --format='Status: {{.State.Status}}'
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
                        echo "Health check URL: http://localhost:${HOST_PORT}/actuator/health"
                        echo ""

                        for i in {1..30}; do
                            echo "Health check attempt $i/30..."

                            STATUS=$(ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} "
                                curl -s -o /dev/null -w '%{http_code}' http://localhost:${HOST_PORT}/actuator/health 2>/dev/null || echo '000'
                            ")

                            if [ "$STATUS" = "200" ]; then
                                echo ""
                                echo "✅ ========================================"
                                echo "✅ APPLICATION IS HEALTHY!"
                                echo "✅ ========================================"

                                ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} "
                                    echo ''
                                    echo '=== Health Status ==='
                                    curl -s http://localhost:${HOST_PORT}/actuator/health 2>/dev/null || echo 'Health endpoint responded'

                                    echo ''
                                    echo '=== Container Status ==='
                                    docker ps | grep ${CONTAINER_NAME}

                                    echo ''
                                    echo '=== Container Logs (last 20 lines) ==='
                                    docker logs --tail 20 ${CONTAINER_NAME}

                                    echo ''
                                    echo '=== Port Status ==='
                                    netstat -tuln | grep ${HOST_PORT} || ss -tuln | grep ${HOST_PORT} || echo 'Port is listening'
                                "

                                exit 0
                            fi

                            sleep 5
                        done

                        echo ""
                        echo "❌ ========================================"
                        echo "❌ HEALTH CHECK FAILED"
                        echo "❌ ========================================"
                        echo ""
                        echo "=== Container Logs (last 100 lines) ==="
                        ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} "
                            docker logs --tail 100 ${CONTAINER_NAME}
                        "

                        echo ""
                        echo "=== Container Status ==="
                        ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} "
                            docker ps -a | grep ${CONTAINER_NAME}
                            docker inspect ${CONTAINER_NAME} --format='Status: {{.State.Status}}'
                            docker inspect ${CONTAINER_NAME} --format='Exit Code: {{.State.ExitCode}}'
                        "

                        exit 1
                    '''
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                echo '✅ Final deployment verification...'
                sshagent(['deployment-server-ssh']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_HOST} "
                            echo '================================================'
                            echo '  DEPLOYMENT VERIFICATION'
                            echo '================================================'

                            echo ''
                            echo '=== Application Info ==='
                            echo 'Name: ${APP_NAME}'
                            echo 'Build: ${BUILD_NUMBER}'
                            echo 'Container: ${CONTAINER_NAME}'
                            echo 'Image: ${IMAGE_NAME}:${IMAGE_TAG}'

                            echo ''
                            echo '=== Network Info ==='
                            echo 'Host: ${REMOTE_HOST}'
                            echo 'Port: ${HOST_PORT}'
                            echo 'URL: http://${REMOTE_HOST}:${HOST_PORT}'

                            echo ''
                            echo '=== Container Status ==='
                            docker ps | grep ${CONTAINER_NAME}

                            echo ''
                            echo '=== Resource Usage ==='
                            docker stats --no-stream ${CONTAINER_NAME}

                            echo ''
                            echo '=== Disk Usage ==='
                            du -sh ${REMOTE_DIR}

                            echo ''
                            echo '=== Available Backups ==='
                            docker images ${IMAGE_NAME} | grep backup | head -3 || echo 'No backups yet'

                            echo '================================================'
                        "
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
                            echo 'Cleaning up old images...'

                            # Keep only last 3 build images + latest + backup images
                            docker images ${IMAGE_NAME} --format '{{.ID}} {{.Tag}}' | \
                            grep -v latest | \
                            grep -v backup | \
                            tail -n +4 | \
                            awk '{print \\$1}' | \
                            xargs -r docker rmi -f 2>/dev/null || true

                            echo ''
                            echo '=== Remaining Images ==='
                            docker images ${IMAGE_NAME}

                            echo ''
                            echo '✓ Cleanup completed'
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
            echo '✅         DEPLOYMENT SUCCESSFUL!                  '
            echo '✅ ================================================='
            echo "✅ Application URL: http://${REMOTE_HOST}:${HOST_PORT}"
            echo "✅ Health Check: http://${REMOTE_HOST}:${HOST_PORT}/actuator/health"
            echo "✅ Container Name: ${CONTAINER_NAME}"
            echo "✅ Image: ${IMAGE_NAME}:${IMAGE_TAG}"
            echo "✅ Build Number: ${BUILD_NUMBER}"
            echo "✅ Deployment Directory: ${REMOTE_DIR}"
            echo '✅ ================================================='
            echo ''
            echo '📋 Quick Commands:'
            echo "   View logs: ssh ${REMOTE_USER}@${REMOTE_HOST} 'docker logs -f ${CONTAINER_NAME}'"
            echo "   Restart: ssh ${REMOTE_USER}@${REMOTE_HOST} 'docker restart ${CONTAINER_NAME}'"
            echo "   Stop: ssh ${REMOTE_USER}@${REMOTE_HOST} 'docker stop ${CONTAINER_NAME}'"
            echo ''
        }

        failure {
            echo ''
            echo '❌ ================================================='
            echo '❌         DEPLOYMENT FAILED!                      '
            echo '❌ ================================================='
            echo '❌ Check the console output above for errors'
            echo ''
            echo '🔄 Rollback Instructions:'
            echo "   1. SSH to server: ssh ${REMOTE_USER}@${REMOTE_HOST}"
            echo "   2. Stop failed container: docker stop ${CONTAINER_NAME} && docker rm ${CONTAINER_NAME}"
            echo "   3. List backups: docker images ${IMAGE_NAME} | grep backup"
            echo "   4. Restore backup: docker tag ${IMAGE_NAME}:backup-YYYYMMDD_HHMMSS ${IMAGE_NAME}:latest"
            echo "   5. Start backup: docker run -d --name ${CONTAINER_NAME} -p ${HOST_PORT}:${CONTAINER_PORT} ${IMAGE_NAME}:latest"
            echo ''
            echo '📋 Debug Commands:'
            echo "   View logs: ssh ${REMOTE_USER}@${REMOTE_HOST} 'docker logs ${CONTAINER_NAME}'"
            echo "   Check container: ssh ${REMOTE_USER}@${REMOTE_HOST} 'docker ps -a | grep ${CONTAINER_NAME}'"
            echo "   Check images: ssh ${REMOTE_USER}@${REMOTE_HOST} 'docker images ${IMAGE_NAME}'"
            echo '❌ ================================================='
            echo ''
        }

        always {
            echo '📊 Pipeline finished at: ' + new Date().toString()
            echo '⏱️  Total duration: ' + currentBuild.durationString

            // Clean local workspace
            cleanWs(
                deleteDirs: true,
                patterns: [
                    [pattern: 'target/**', type: 'INCLUDE'],
                    [pattern: '*.tar', type: 'INCLUDE'],
                    [pattern: '*.log', type: 'INCLUDE']
                ]
            )

            echo '✓ Workspace cleaned'
        }
    }
}