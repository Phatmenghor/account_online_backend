#!/bin/bash

# =========================================
# PROJECT CONFIGURATION
# =========================================
PROJECT_NAME="internal_dev"
JAR_NAME="internal_dev"
APP_PORT="8888"

# Database Settings
DB_HOST="192.168.103.106"
DB_PORT="5432"
DB_NAME="internal_dev"
DB_USERNAME="postgres"
DB_PASSWORD="123cp!@#"

# Java Settings
JAVA_VERSION="8"

# Directory Settings
UPLOADS_DIR="uploads"
LOGS_DIR="logs"

# Docker Settings
DOCKER_IMAGE_TAG="1.0"

# Git Repository Settings (for Git deployment)
GIT_REPO_URL="192.168.107.34:8090/internal_dev_backend.git"
GIT_BRANCH="development"
GIT_USERNAME="menghor.phat"
GIT_PASSWORD="Hour1819"

# Maven Settings (for Git deployment)
MAVEN_PROFILE=""
SKIP_TESTS="true"

# Cleanup Settings - Will be set by user input
KEEP_DOCKERFILE=""
KEEP_JAR=""
KEEP_SOURCE=""

# Deployment mode - Will be set by user
DEPLOYMENT_MODE=""

# =========================================
# SYSTEM CONFIGURATION
# =========================================

# Console Colors and Formatting
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
DIM='\033[2m'
NC='\033[0m'

# Unicode Characters
CHECK_MARK="✓"
CROSS_MARK="✗"
ARROW="→"
SPINNER_CHARS="⠋⠙⠹⠸⠼⠴⠦⠧⠇⠏"
PROGRESS_FULL="█"
PROGRESS_EMPTY="░"

# Container name and directories
CONTAINER_NAME="${PROJECT_NAME}_container"
DEPLOY_DIR=$(pwd)
SOURCE_DIR="${DEPLOY_DIR}/source"
LOCAL_JAR_PATH="${DEPLOY_DIR}/${JAR_NAME}.jar"

# Global variables
CURRENT_STEP=0
TOTAL_STEPS=0  # Will be set based on deployment mode
START_TIME=$(date +%s)

# =========================================
# CONSOLE FUNCTIONS
# =========================================

print_header() {
    local title="$1"
    local width=80
    local padding=$(( (width - ${#title} - 2) / 2 ))

    echo ""
    echo -e "${CYAN}╔$(printf '═%.0s' $(seq 1 $((width-2))))╗${NC}"
    printf "${CYAN}║${NC}%*s${BOLD}%s${NC}%*s${CYAN}║${NC}\n" $padding "" "$title" $padding ""
    echo -e "${CYAN}╚$(printf '═%.0s' $(seq 1 $((width-2))))╝${NC}"
    echo ""
}

show_step() {
    local step_name="$1"
    local step_desc="$2"
    CURRENT_STEP=$((CURRENT_STEP + 1))

    local progress=$((CURRENT_STEP * 100 / TOTAL_STEPS))
    local filled=$((progress / 5))
    local empty=$((20 - filled))

    echo ""
    echo -e "${BOLD}${BLUE}┌─ STEP ${CURRENT_STEP}/${TOTAL_STEPS}: ${step_name} $(printf '─%.0s' $(seq 1 $((60 - ${#step_name} - 15))))┐${NC}"
    echo -e "${BLUE}│${NC} ${DIM}${step_desc}${NC}"
    echo -e "${BLUE}│${NC}"

    printf "${BLUE}│${NC} Overall: [${NC}"
    printf "${GREEN}%*s${NC}" $filled "" | tr ' ' "$PROGRESS_FULL"
    printf "${GRAY}%*s${NC}" $empty "" | tr ' ' "$PROGRESS_EMPTY"
    printf "${BLUE}] ${GREEN}%d%%${NC}\n" $progress
    echo -e "${BLUE}└$(printf '─%.0s' $(seq 1 75))┘${NC}"
    echo ""
}

show_loading() {
    local message="$1"
    local duration="${2:-3}"

    local i=0
    local spinner_len=${#SPINNER_CHARS}

    for step in $(seq 1 $((duration * 4))); do
        local spinner_char=${SPINNER_CHARS:$((i % spinner_len)):1}
        printf "\r${YELLOW}${spinner_char}${NC} ${message}..."
        sleep 0.25
        i=$((i + 1))
    done

    printf "\r${GREEN}${CHECK_MARK}${NC} ${message} ${GREEN}completed!${NC}\n"
}

run_with_loading() {
    local message="$1"
    local command="$2"
    local show_output="${3:-false}"

    printf "${YELLOW}⟳${NC} ${message}..."

    if [ "$show_output" = "true" ]; then
        echo ""
        echo -e "${DIM}Running: ${command}${NC}"
        echo -e "${CYAN}$(printf '─%.0s' $(seq 1 60))${NC}"
    fi

    local temp_file=$(mktemp)
    eval "$command" > "$temp_file" 2>&1 &
    local cmd_pid=$!

    if [ "$show_output" = "false" ]; then
        local i=0
        while kill -0 $cmd_pid 2>/dev/null; do
            local spinner_char=${SPINNER_CHARS:$((i % ${#SPINNER_CHARS})):1}
            printf "\r${YELLOW}${spinner_char}${NC} ${message}..."
            sleep 0.2
            i=$((i + 1))
        done
    fi

    wait $cmd_pid
    local exit_code=$?

    if [ "$show_output" = "true" ]; then
        cat "$temp_file"
        echo -e "${CYAN}$(printf '─%.0s' $(seq 1 60))${NC}"
    fi

    if [ $exit_code -eq 0 ]; then
        printf "\r${GREEN}${CHECK_MARK}${NC} ${message} ${GREEN}completed!${NC}\n"
    else
        printf "\r${RED}${CROSS_MARK}${NC} ${message} ${RED}failed!${NC}\n"
        if [ "$show_output" = "false" ]; then
            echo -e "${RED}Error output:${NC}"
            echo -e "${RED}$(printf '─%.0s' $(seq 1 40))${NC}"
            cat "$temp_file"
            echo -e "${RED}$(printf '─%.0s' $(seq 1 40))${NC}"
        fi
    fi

    rm -f "$temp_file"
    return $exit_code
}

show_status() {
    local status="$1"
    local message="$2"
    local details="$3"

    case $status in
        "success"|"ok")
            echo -e "${GREEN}${CHECK_MARK}${NC} ${message}"
            ;;
        "error"|"fail")
            echo -e "${RED}${CROSS_MARK}${NC} ${message}"
            ;;
        "warning"|"warn")
            echo -e "${YELLOW}⚠${NC} ${message}"
            ;;
        "info")
            echo -e "${BLUE}ℹ${NC} ${message}"
            ;;
    esac

    if [ -n "$details" ]; then
        echo -e "${DIM}  ${ARROW} ${details}${NC}"
    fi
}

# =========================================
# USER INPUT FUNCTIONS
# =========================================

# =========================================
# USER INPUT FUNCTIONS
# =========================================

show_deployment_options() {
    echo ""
    print_header "DEPLOYMENT MODE SELECTION"

    echo -e "${BOLD}Choose your deployment method:${NC}"
    echo ""
    echo -e "${GREEN}[1]${NC} ${BOLD}Git Deployment${NC}"
    echo -e "    ${DIM}• Clone from Git repository${NC}"
    echo -e "    ${DIM}• Build JAR with Maven${NC}"
    echo -e "    ${DIM}• Full source code management${NC}"
    echo ""
    echo -e "${BLUE}[2]${NC} ${BOLD}Local JAR Deployment${NC}"
    echo -e "    ${DIM}• Use ${JAR_NAME}.jar from current folder${NC}"
    echo -e "    ${DIM}• Skip Git clone and Maven build${NC}"
    echo -e "    ${DIM}• Faster deployment process${NC}"
    echo ""
    echo -e "${YELLOW}[q]${NC} ${BOLD}Quit${NC}"
    echo ""
}

get_deployment_mode() {
    while true; do
        show_deployment_options
        echo -n -e "${CYAN}Select option [1/2/q]: ${NC}"
        read -r choice

        case $choice in
            1)
                DEPLOYMENT_MODE="git"
                TOTAL_STEPS=8
                echo -e "${GREEN}${CHECK_MARK}${NC} Selected: Git Deployment"
                break
                ;;
            2)
                DEPLOYMENT_MODE="local"
                TOTAL_STEPS=5
                echo -e "${GREEN}${CHECK_MARK}${NC} Selected: Local JAR Deployment"
                echo -e "${DIM}  ${ARROW} Will use: ${JAR_NAME}.jar from current directory${NC}"
                break
                ;;
            q|Q)
                echo -e "${YELLOW}Deployment cancelled by user${NC}"
                exit 0
                ;;
            *)
                echo -e "${RED}${CROSS_MARK}${NC} Invalid option. Please select 1, 2, or q"
                sleep 1
                clear
                ;;
        esac
    done
}

get_cleanup_preferences() {
    echo ""
    print_header "CLEANUP CONFIGURATION"
    echo -e "${BOLD}Choose what to keep after deployment:${NC}"
    echo ""

    # Ask about KEEP_DOCKERFILE
    while true; do
        echo -n -e "${CYAN}Keep Dockerfile after build? [y/n] (default: n): ${NC}"
        read -r keep_dockerfile_choice
        case ${keep_dockerfile_choice:-n} in
            [Yy]|[Yy][Ee][Ss])
                KEEP_DOCKERFILE="true"
                echo -e "${GREEN}${CHECK_MARK}${NC} Dockerfile will be preserved"
                break
                ;;
            [Nn]|[Nn][Oo])
                KEEP_DOCKERFILE="false"
                echo -e "${YELLOW}${CHECK_MARK}${NC} Dockerfile will be removed after deployment"
                break
                ;;
            *)
                echo -e "${RED}${CROSS_MARK}${NC} Please enter y or n"
                ;;
        esac
    done

    # Ask about KEEP_JAR
    while true; do
        echo -n -e "${CYAN}Keep JAR file after deployment? [y/n] (default: n): ${NC}"
        read -r keep_jar_choice
        case ${keep_jar_choice:-n} in
            [Yy]|[Yy][Ee][Ss])
                KEEP_JAR="true"
                echo -e "${GREEN}${CHECK_MARK}${NC} JAR file will be preserved"
                break
                ;;
            [Nn]|[Nn][Oo])
                KEEP_JAR="false"
                echo -e "${YELLOW}${CHECK_MARK}${NC} JAR file will be removed after deployment"
                break
                ;;
            *)
                echo -e "${RED}${CROSS_MARK}${NC} Please enter y or n"
                ;;
        esac
    done

    # Ask about KEEP_SOURCE
    while true; do
        echo -n -e "${CYAN}Keep source files after deployment? [y/n] (default: n): ${NC}"
        read -r keep_source_choice
        case ${keep_source_choice:-n} in
            [Yy]|[Yy][Ee][Ss])
                KEEP_SOURCE="true"
                echo -e "${GREEN}${CHECK_MARK}${NC} Source files will be preserved"
                break
                ;;
            [Nn]|[Nn][Oo])
                KEEP_SOURCE="false"
                echo -e "${YELLOW}${CHECK_MARK}${NC} Source files will be removed after deployment"
                break
                ;;
            *)
                echo -e "${RED}${CROSS_MARK}${NC} Please enter y or n"
                ;;
        esac
    done

    echo ""
}

# =========================================
# DEPLOYMENT FUNCTIONS
# =========================================

# =========================================
# DEPLOYMENT FUNCTIONS
# =========================================

# URL encoding function for Git credentials
url_encode() {
    local string="${1}"
    local strlen=${#string}
    local encoded=""
    local pos c o

    for (( pos=0 ; pos<strlen ; pos++ )); do
        c=${string:$pos:1}
        case "$c" in
            [-_.~a-zA-Z0-9] ) o="${c}" ;;
            * ) printf -v o '%%%02x' "'$c"
        esac
        encoded+="${o}"
    done
    echo "${encoded}"
}

enhanced_git_clone() {
    show_status "info" "Cloning repository" "Fresh clone for clean deployment"

    # URL encode the username and password
    local encoded_username=$(url_encode "$GIT_USERNAME")
    local encoded_password=$(url_encode "$GIT_PASSWORD")

    # Construct the clone command with properly encoded credentials
    local clone_cmd="git clone 'http://${encoded_username}:${encoded_password}@${GIT_REPO_URL}' '${SOURCE_DIR}'"

    if run_with_loading "Cloning repository" "$clone_cmd" false; then
        cd "$SOURCE_DIR"

        # Checkout target branch if needed
        local current_branch=$(git branch --show-current)
        if [ "$current_branch" != "$GIT_BRANCH" ]; then
            if run_with_loading "Switching to branch ${GIT_BRANCH}" "git checkout ${GIT_BRANCH}" false; then
                show_status "success" "Switched to branch: ${GIT_BRANCH}"
            else
                show_status "error" "Failed to checkout branch: ${GIT_BRANCH}"
                return 1
            fi
        fi

        show_status "success" "Repository cloned successfully"
        return 0
    else
        show_status "error" "Failed to clone repository"
        return 1
    fi
}

stop_port_services() {
    local port="$1"

    echo -e "${YELLOW}🔍${NC} ${BOLD}Checking for services on port ${port}...${NC}"

    # Check for Docker containers using the port
    local containers=$(docker ps --format "table {{.ID}}\t{{.Names}}\t{{.Ports}}" | grep ":${port}->" | awk '{print $2}' || true)

    if [ -n "$containers" ]; then
        echo -e "${CYAN}┌─ Found Active Containers ───────────────────────────┐${NC}"
        docker ps --format "table {{.ID}}\t{{.Names}}\t{{.Ports}}\t{{.Status}}" | grep ":${port}->"
        echo -e "${CYAN}└─────────────────────────────────────────────────────────┘${NC}"

        for container in $containers; do
            show_loading "Stopping container: $container" 2
            if docker stop "$container" > /dev/null 2>&1; then
                show_status "success" "Stopped container: $container"
                if docker rm "$container" > /dev/null 2>&1; then
                    show_status "success" "Removed container: $container"
                fi
            else
                show_status "warning" "Failed to stop container: $container"
            fi
        done
    fi

    # Check for other processes using the port
    local pids=$(lsof -t -i:${port} 2>/dev/null || true)

    if [ -n "$pids" ]; then
        echo -e "${YELLOW}⚠${NC} Found processes using port ${port}:"
        for pid in $pids; do
            local process_info=$(ps -p $pid -o pid,comm,args --no-headers 2>/dev/null || echo "Unknown process")
            echo -e "${DIM}  ${ARROW} PID $pid: $process_info${NC}"

            show_loading "Terminating process PID: $pid" 1
            if kill -TERM $pid 2>/dev/null; then
                sleep 2
                if kill -0 $pid 2>/dev/null; then
                    show_status "warning" "Force killing PID: $pid"
                    kill -KILL $pid 2>/dev/null || true
                fi
                show_status "success" "Terminated process PID: $pid"
            else
                show_status "warning" "Failed to terminate PID: $pid"
            fi
        done
    else
        show_status "success" "Port ${port} is available"
    fi
}

check_jar_file() {
    if [ -f "$LOCAL_JAR_PATH" ]; then
        local jar_size=$(du -h "$LOCAL_JAR_PATH" | cut -f1)
        local jar_modified=$(stat -c %y "$LOCAL_JAR_PATH" 2>/dev/null || stat -f %Sm "$LOCAL_JAR_PATH" 2>/dev/null || echo "Unknown")

        show_status "success" "JAR file found: ${JAR_NAME}.jar"
        echo -e "${DIM}  ${ARROW} Path: ${LOCAL_JAR_PATH}${NC}"
        echo -e "${DIM}  ${ARROW} Size: ${jar_size}${NC}"
        echo -e "${DIM}  ${ARROW} Modified: ${jar_modified}${NC}"
        return 0
    else
        show_status "error" "JAR file not found: ${JAR_NAME}.jar"
        echo -e "${DIM}  ${ARROW} Expected path: ${LOCAL_JAR_PATH}${NC}"
        echo -e "${DIM}  ${ARROW} Current directory: ${DEPLOY_DIR}${NC}"
        echo ""
        echo -e "${YELLOW}Available JAR files in current directory:${NC}"
        find . -maxdepth 1 -name "*.jar" -type f 2>/dev/null | head -10 || echo -e "${DIM}  No JAR files found${NC}"
        return 1
    fi
}

show_deployment_summary() {
    local end_time=$(date +%s)
    local duration=$((end_time - START_TIME))
    local minutes=$((duration / 60))
    local seconds=$((duration % 60))

    echo ""
    print_header "DEPLOYMENT COMPLETE"

    echo -e "${GREEN}${CHECK_MARK}${NC} Project: ${BOLD}${PROJECT_NAME}${NC}"
    echo -e "${GREEN}${CHECK_MARK}${NC} JAR File: ${BOLD}${JAR_NAME}.jar${NC}"
    echo -e "${GREEN}${CHECK_MARK}${NC} Port: ${BOLD}${APP_PORT}${NC}"
    echo -e "${GREEN}${CHECK_MARK}${NC} Container: ${BOLD}${CONTAINER_NAME}${NC}"
    echo -e "${GREEN}${CHECK_MARK}${NC} Duration: ${BOLD}${minutes}m ${seconds}s${NC}"

    echo ""
    echo -e "${CYAN}┌─ Quick Commands ──────────────────────────────────────┐${NC}"
    echo -e "${CYAN}│${NC} View logs:     ${YELLOW}docker logs -f ${CONTAINER_NAME}${NC}"
    echo -e "${CYAN}│${NC} Stop app:      ${YELLOW}docker stop ${CONTAINER_NAME}${NC}"
    echo -e "${CYAN}│${NC} Restart app:   ${YELLOW}docker restart ${CONTAINER_NAME}${NC}"
    echo -e "${CYAN}│${NC} Application:   ${GREEN}http://localhost:${APP_PORT}${NC}"
    echo -e "${CYAN}└─────────────────────────────────────────────────────────┘${NC}"
}

get_color_for_boolean() {
    if [ "$1" = "true" ]; then
        echo "${GREEN}"
    else
        echo "${YELLOW}"
    fi
}

# =========================================
# MAIN DEPLOYMENT PROCESS
# =========================================

# Clear screen and show header
clear
print_header "CAMDX CHECK DEPLOYMENT SYSTEM"

# Get user's choice for deployment mode
get_deployment_mode

# Get cleanup preferences
get_cleanup_preferences

# Show project info
echo ""
echo -e "${BOLD}Project:${NC} ${GREEN}${PROJECT_NAME}${NC}"
echo -e "${BOLD}Mode:${NC} ${GREEN}$(echo $DEPLOYMENT_MODE | tr '[:lower:]' '[:upper:]')${NC}"
if [ "$DEPLOYMENT_MODE" = "local" ]; then
    echo -e "${BOLD}JAR File:${NC} ${GREEN}${JAR_NAME}.jar${NC}"
else
    echo -e "${BOLD}Repository:${NC} ${GREEN}${GIT_REPO_URL}${NC}"
    echo -e "${BOLD}Branch:${NC} ${GREEN}${GIT_BRANCH}${NC}"
fi
echo -e "${BOLD}Target:${NC} ${GREEN}http://localhost:${APP_PORT}${NC}"
echo -e "${BOLD}Started:${NC} ${GREEN}$(date)${NC}"
echo ""

# Show cleanup configuration
echo -e "${CYAN}┌─ Cleanup Configuration ────────────────────────────────┐${NC}"
echo -e "${CYAN}│${NC} Keep Dockerfile: $(get_color_for_boolean "$KEEP_DOCKERFILE")${KEEP_DOCKERFILE}${NC}"
echo -e "${CYAN}│${NC} Keep JAR File: $(get_color_for_boolean "$KEEP_JAR")${KEEP_JAR}${NC}"
echo -e "${CYAN}│${NC} Keep Source: $(get_color_for_boolean "$KEEP_SOURCE")${KEEP_SOURCE}${NC}"
echo -e "${CYAN}└───────────────────────────────────────────────────────────┘${NC}"

# Check Docker installation
if ! command -v docker &> /dev/null; then
    show_status "error" "Docker is not installed"
    exit 1
fi

# Step 1: Port Management
show_step "Port Management" "Stopping services using port ${APP_PORT}"
stop_port_services "$APP_PORT"

if [ "$DEPLOYMENT_MODE" = "git" ]; then
    # GIT DEPLOYMENT PATH

    # Step 2: Source Code Management
    show_step "Source Code Management" "Cloning/updating repository from Git"

    # Always do fresh clone for clean deployment
    if [ -d "$SOURCE_DIR" ]; then
        show_status "info" "Removing existing source directory for fresh clone"
        rm -rf "$SOURCE_DIR"
    fi

    enhanced_git_clone || exit 1

    # Step 3: Clean Previous Builds
    show_step "Clean Previous Builds" "Removing old build artifacts with Maven clean"

    cd "$SOURCE_DIR"

    if run_with_loading "Cleaning previous Maven builds" "mvn clean -q" false; then
        show_status "success" "Previous builds cleaned successfully"
    else
        show_status "error" "Maven clean failed"
        exit 1
    fi

    # Step 4: Build Application
    show_step "Build Application" "Compiling source code with Maven"

    # Prepare Maven package command
    MAVEN_CMD="mvn package"
    if [ "$SKIP_TESTS" = "true" ]; then
        MAVEN_CMD="$MAVEN_CMD -DskipTests"
    fi
    if [ -n "$MAVEN_PROFILE" ]; then
        MAVEN_CMD="$MAVEN_CMD -P$MAVEN_PROFILE"
    fi

    show_status "info" "Maven package command: ${MAVEN_CMD}"

    if run_with_loading "Building application with Maven" "$MAVEN_CMD -q" true; then
        # Find and copy JAR
        JAR_FILE=$(find target -name "*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" | head -1)
        if [ -n "$JAR_FILE" ] && [ -f "$JAR_FILE" ]; then
            # Remove old JAR if exists
            if [ -f "${DEPLOY_DIR}/${JAR_NAME}.jar" ]; then
                show_loading "Removing old JAR file" 1
                rm -f "${DEPLOY_DIR}/${JAR_NAME}.jar"
                show_status "success" "Old JAR file removed"
            fi

            cp "$JAR_FILE" "${DEPLOY_DIR}/${JAR_NAME}.jar"
            show_status "success" "JAR built successfully" "$(basename "$JAR_FILE")"
        else
            show_status "error" "JAR file not found in target directory"
            exit 1
        fi
    else
        show_status "error" "Maven build failed"
        exit 1
    fi

else
    # LOCAL JAR DEPLOYMENT PATH

    # Step 2: JAR File Check
    show_step "JAR File Check" "Verifying ${JAR_NAME}.jar in current directory"
    check_jar_file || exit 1
fi

# Common steps for both deployment modes
cd "$DEPLOY_DIR"

# Step: Create Docker Configuration
show_step "Create Docker Configuration" "Generating Dockerfile for containerization"

cat > "${DEPLOY_DIR}/Dockerfile" << EOF
FROM staff_feedback:1.0
RUN rm -rf /app/*
COPY ${JAR_NAME}.jar /app/${JAR_NAME}.jar
WORKDIR /app
EXPOSE ${APP_PORT}
ENTRYPOINT ["java","-jar","${JAR_NAME}.jar"]
EOF

show_status "success" "Dockerfile created" "Using OpenJDK ${JAVA_VERSION}"

# Step: Build Docker Image
show_step "Build Docker Image" "Creating containerized application"

if run_with_loading "Building Docker image" "docker build --tag=${PROJECT_NAME}:${DOCKER_IMAGE_TAG} ." false; then
    show_status "success" "Docker image built" "${PROJECT_NAME}:${DOCKER_IMAGE_TAG}"
else
    show_status "error" "Docker image build failed"
    exit 1
fi

# Setup directories
mkdir -p "${DEPLOY_DIR}/${UPLOADS_DIR}"
mkdir -p "${DEPLOY_DIR}/${LOGS_DIR}"
chmod 755 "${DEPLOY_DIR}/${LOGS_DIR}"
show_status "success" "Directories created" "uploads/ and logs/"

# Step: Start Application Container
show_step "Start Application Container" "Launching application with database configuration"

START_CMD="docker run -v '${DEPLOY_DIR}/${UPLOADS_DIR}:/${UPLOADS_DIR}' -v '${DEPLOY_DIR}/${LOGS_DIR}:/app/logs' \
    -e 'SPRING_DATASOURCE_URL=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}' \
    -e 'SPRING_DATASOURCE_USERNAME=${DB_USERNAME}' \
    -e 'SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}' \
    -itd -p ${APP_PORT}:${APP_PORT} --name ${CONTAINER_NAME} ${PROJECT_NAME}:${DOCKER_IMAGE_TAG}"

if run_with_loading "Starting new container" "$START_CMD" false; then
    show_status "success" "Container started" "${CONTAINER_NAME}"
else
    show_status "error" "Failed to start container"
    exit 1
fi

# Show initial status
echo ""
echo -e "${BLUE}📋${NC} ${BOLD}Application started successfully!${NC}"
echo -e "${GREEN}${CHECK_MARK}${NC} Container: ${CONTAINER_NAME}"
echo -e "${GREEN}${CHECK_MARK}${NC} Port: ${APP_PORT}"
echo -e "${GREEN}${CHECK_MARK}${NC} Status: Running"

# Final summary
show_deployment_summary

# =========================================
# CLEANUP SECTION
# =========================================
print_header "POST-DEPLOYMENT CLEANUP"

# Cleanup source directory after successful deployment (only for Git mode)
if [ "$DEPLOYMENT_MODE" = "git" ] && [ "$KEEP_SOURCE" != "true" ]; then
    show_loading "Cleaning up source directory" 2
    rm -rf "$SOURCE_DIR"
    show_status "success" "Source directory cleaned up"
elif [ "$DEPLOYMENT_MODE" = "git" ]; then
    show_status "info" "Source directory preserved" "${SOURCE_DIR}"
fi

# Cleanup JAR file if requested
if [ "$KEEP_JAR" != "true" ]; then
    show_loading "Cleaning up JAR file" 1
    rm -f "${DEPLOY_DIR}/${JAR_NAME}.jar"
    show_status "success" "JAR file cleaned up"
else
    show_status "info" "JAR file preserved" "${DEPLOY_DIR}/${JAR_NAME}.jar"
fi

# Cleanup Dockerfile if requested
if [ "$KEEP_DOCKERFILE" != "true" ]; then
    show_loading "Cleaning up Dockerfile" 1
    rm -f "${DEPLOY_DIR}/Dockerfile"
    show_status "success" "Dockerfile cleaned up"
else
    show_status "info" "Dockerfile preserved" "${DEPLOY_DIR}/Dockerfile"
fi

# Show cleanup summary
echo ""
echo -e "${CYAN}┌─ Cleanup Summary ──────────────────────────────────────┐${NC}"
if [ "$DEPLOYMENT_MODE" = "git" ]; then
    echo -e "${CYAN}│${NC} Source Directory: $(get_color_for_boolean "$KEEP_SOURCE")$(if [ "$KEEP_SOURCE" = "true" ]; then echo "KEPT"; else echo "REMOVED"; fi)${NC}"
fi
echo -e "${CYAN}│${NC} JAR File: $(get_color_for_boolean "$KEEP_JAR")$(if [ "$KEEP_JAR" = "true" ]; then echo "KEPT"; else echo "REMOVED"; fi)${NC}"
echo -e "${CYAN}│${NC} Dockerfile: $(get_color_for_boolean "$KEEP_DOCKERFILE")$(if [ "$KEEP_DOCKERFILE" = "true" ]; then echo "KEPT"; else echo "REMOVED"; fi)${NC}"
echo -e "${CYAN}└───────────────────────────────────────────────────────────┘${NC}"

# Final status
echo ""
echo -e "${GREEN}${BOLD}🎉 DEPLOYMENT SUCCESSFUL! 🎉${NC}"
echo -e "${GREEN}Your application is now running at: ${BOLD}http://localhost:${APP_PORT}${NC}"
echo ""

# Show deployment mode specific final notes
if [ "$DEPLOYMENT_MODE" = "local" ]; then
    echo -e "${BLUE}ℹ${NC} ${BOLD}Local JAR Deployment Notes:${NC}"
    echo -e "${DIM}  ${ARROW} JAR File: ${JAR_NAME}.jar${NC}"
    echo -e "${DIM}  ${ARROW} Source Directory: ${DEPLOY_DIR}${NC}"
    echo -e "${DIM}  ${ARROW} No Git operations performed${NC}"
    echo -e "${DIM}  ${ARROW} No Maven build required${NC}"
else
    echo -e "${BLUE}ℹ${NC} ${BOLD}Git Deployment Notes:${NC}"
    echo -e "${DIM}  ${ARROW} Repository: ${GIT_REPO_URL}${NC}"
    echo -e "${DIM}  ${ARROW} Branch: ${GIT_BRANCH}${NC}"
    echo -e "${DIM}  ${ARROW} Fresh clone and build completed${NC}"
fi
echo ""