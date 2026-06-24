#!/bin/bash
# Spug post-release script for pk-backend-app.
# Local docker build on the release host (no CI / registry required).
# Requires: SPUG_DST_DIR. Optional: SPUG_ENV=test|prod

set -e

echo "=========================================="
echo "Post-release (pk-backend-app)"
echo "=========================================="
echo "Script fingerprint: pk-backend-app-spug-local-build-20260624-v2"
echo "Deploy mode: local docker build + docker run"
echo ""

if [ -z "${SPUG_DST_DIR}" ]; then
    echo "ERROR: SPUG_DST_DIR is not set"
    exit 1
fi

cd "${SPUG_DST_DIR}"
echo "Working directory: $(pwd)"

if [ -L "${SPUG_DST_DIR}" ]; then
    REAL_DIR=$(readlink -f "${SPUG_DST_DIR}")
    echo "Symlink detected, switching to: ${REAL_DIR}"
    cd "${REAL_DIR}"
fi

SPUG_ENV="${SPUG_ENV:-test}"
case "${SPUG_ENV}" in
    prod|production)
        SPRING_PROFILES_ACTIVE="prod"
        APP_CONTAINER="${PK_APP_CONTAINER:-pk-backend-app}"
        ;;
    test|dev|*)
        SPRING_PROFILES_ACTIVE="test"
        APP_CONTAINER="${PK_APP_CONTAINER:-pk-backend-app-dev}"
        ;;
esac

IMAGE_NAME="${PK_IMAGE_NAME:-pk-backend-app}"
APP_PORT="${PK_APP_PORT:-8831}"
DOCKER_NETWORK="${PK_DOCKER_NETWORK:-pk-network}"
CONFIG_DIR="${PK_CONFIG_DIR:-/www/config/pk-backend-app}"
LOG_DIR="${PK_LOG_DIR:-/www/logs/pk-backend-app}"
TIMESTAMP=$(date +%Y%m%d%H%M%S)
IMAGE_TAG="${SPUG_ENV}-${TIMESTAMP}"
IMAGE_TAG_LATEST="${SPUG_ENV}-latest"
HEALTH_URL="${PK_HEALTH_URL:-http://localhost:${APP_PORT}/actuator/health}"
HEALTH_MAX_WAIT="${PK_HEALTH_MAX_WAIT:-300}"
HEALTH_RETRY_INTERVAL="${PK_HEALTH_RETRY_INTERVAL:-5}"
DOCKER_NO_CACHE="${PK_DOCKER_NO_CACHE:-false}"
KEEP_IMAGES="${PK_KEEP_IMAGES:-5}"

echo ""
echo "Environment: ${SPUG_ENV} (SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE})"
echo "Container: ${APP_CONTAINER}"
echo "Image: ${IMAGE_NAME}:${IMAGE_TAG}"
echo "Port: ${APP_PORT}"
echo "Health check: ${HEALTH_URL}"
echo ""

if [ ! -f "Dockerfile" ]; then
    echo "ERROR: Dockerfile not found"
    exit 1
fi

if ! docker info >/dev/null 2>&1; then
    echo "ERROR: Docker is not available"
    exit 1
fi

mkdir -p "${CONFIG_DIR}" "${LOG_DIR}"

# ------------------------------------------
# Build image
# ------------------------------------------
echo ""
echo "=========================================="
echo "Build Docker image"
echo "=========================================="
echo "First build or --no-cache may take 5-15 minutes"
echo "Set PK_DOCKER_NO_CACHE=true after pom/Dockerfile changes"
echo ""

BUILD_ARGS=()
if [ "${DOCKER_NO_CACHE}" = "true" ]; then
    BUILD_ARGS+=(--no-cache)
fi

if ! docker build "${BUILD_ARGS[@]}" -t "${IMAGE_NAME}:${IMAGE_TAG}" .; then
    echo "ERROR: docker build failed"
    exit 1
fi

docker tag "${IMAGE_NAME}:${IMAGE_TAG}" "${IMAGE_NAME}:${IMAGE_TAG_LATEST}"
docker tag "${IMAGE_NAME}:${IMAGE_TAG}" "${IMAGE_NAME}:latest"

# ------------------------------------------
# Collect Spug environment variables
# ------------------------------------------
echo ""
echo "=========================================="
echo "Collect Spug environment variables"
echo "=========================================="

CONFIG_FILES=("pk-app/src/main/resources/application.yml")
case "${SPRING_PROFILES_ACTIVE}" in
    prod) CONFIG_FILES+=("pk-app/src/main/resources/application-prod.yml") ;;
    *)    CONFIG_FILES+=("pk-app/src/main/resources/application-test.yml") ;;
esac

NEEDED_KEYS=()
for config_file in "${CONFIG_FILES[@]}"; do
    [ -f "${config_file}" ] || continue
    echo "Scanning: ${config_file}"
    while IFS= read -r key; do
        [ -n "${key}" ] && NEEDED_KEYS+=("${key}")
    done < <(grep -oE '\$\{[A-Za-z0-9_]+(:[^}]*)?\}' "${config_file}" | sed 's/\${\([^:}]*\).*/\1/' | sort -u)
done

mapfile -t NEEDED_KEYS < <(printf '%s\n' "${NEEDED_KEYS[@]}" | sort -u)
echo "Required keys: ${#NEEDED_KEYS[@]}"

ENV_FILE=$(mktemp)
if [ ${#NEEDED_KEYS[@]} -gt 0 ]; then
    while IFS='=' read -r line || [ -n "${line}" ]; do
        [ -z "${line}" ] && continue
        key="${line%%=*}"
        value="${line#*=}"
        [ -z "${key}" ] && continue
        echo "${key}" | grep -qE '^(SPUG_DST_DIR|SPUG_APP_NAME|SPUG_APP_KEY|SPUG_ENV_KEY)$' && continue

        matched_key=""
        for needed_key in "${NEEDED_KEYS[@]}"; do
            if echo "${key}" | grep -qE "${needed_key}$"; then
                matched_key="${needed_key}"
                break
            fi
        done
        [ -n "${matched_key}" ] || continue
        echo "${value}" | grep -qE '(at |Caused by|Exception|\.java:|~\[|jar!)' && continue
        [ ${#value} -gt 10000 ] && continue
        value=$(echo "${value}" | sed 's/^"\(.*\)"$/\1/')
        printf '%s=%s\n' "${matched_key}" "${value}" >> "${ENV_FILE}"
    done < <(env)
fi

# Optional env file on host (KEY=VALUE per line)
HOST_ENV_FILE="${CONFIG_DIR}/env"
if [ -f "${HOST_ENV_FILE}" ]; then
    echo "Loading host env file: ${HOST_ENV_FILE}"
    while IFS='=' read -r env_key env_value || [ -n "${env_key}" ]; do
        [ -z "${env_key}" ] && continue
        [[ "${env_key}" =~ ^# ]] && continue
        printf '%s=%s\n' "${env_key}" "${env_value}" >> "${ENV_FILE}"
    done < "${HOST_ENV_FILE}"
fi

ENV_ARGS=()
if [ -f "${ENV_FILE}" ] && [ -s "${ENV_FILE}" ]; then
    while IFS='=' read -r env_key env_value || [ -n "${env_key}" ]; do
        [ -z "${env_key}" ] && continue
        ENV_ARGS+=("-e" "${env_key}=${env_value}")
    done < "${ENV_FILE}"
fi
rm -f "${ENV_FILE}"

if ! printf '%s\n' "${ENV_ARGS[@]}" | grep -q 'SPRING_PROFILES_ACTIVE='; then
    ENV_ARGS+=("-e" "SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}")
fi
if ! printf '%s\n' "${ENV_ARGS[@]}" | grep -q 'PK_APP_PORT='; then
    ENV_ARGS+=("-e" "PK_APP_PORT=${APP_PORT}")
fi

# Redis: containers cannot use localhost for host Redis
if ! printf '%s\n' "${ENV_ARGS[@]}" | grep -q 'PK_REDIS_HOST='; then
    ENV_ARGS+=("-e" "PK_REDIS_HOST=host.docker.internal")
    ENV_ARGS+=("-e" "PK_REDIS_PORT=6381")
    echo "WARN: PK_REDIS_HOST not set; defaulting to host.docker.internal:6381"
fi

echo "Collected env vars: $((${#ENV_ARGS[@]} / 2))"

# ------------------------------------------
# Network + container
# ------------------------------------------
if ! docker network inspect "${DOCKER_NETWORK}" >/dev/null 2>&1; then
    docker network create "${DOCKER_NETWORK}"
fi

docker rm -f "${APP_CONTAINER}" 2>/dev/null || true

docker run -d \
    --name "${APP_CONTAINER}" \
    --restart unless-stopped \
    --network "${DOCKER_NETWORK}" \
    --add-host=host.docker.internal:host-gateway \
    -p "${APP_PORT}:${APP_PORT}" \
    "${ENV_ARGS[@]}" \
    -v "${CONFIG_DIR}:/app/config:ro" \
    -v "${LOG_DIR}:/app/logs" \
    "${IMAGE_NAME}:${IMAGE_TAG_LATEST}"

# ------------------------------------------
# Health check
# ------------------------------------------
echo ""
echo "=========================================="
echo "Health check"
echo "=========================================="

sleep 3
HEALTH_OK=false
HEALTH_START_TS=$(date +%s)
ATTEMPT=0

while true; do
    ATTEMPT=$((ATTEMPT + 1))
    ELAPSED=$(( $(date +%s) - HEALTH_START_TS ))
    [ "${ELAPSED}" -ge "${HEALTH_MAX_WAIT}" ] && break

    if ! docker ps --format '{{.Names}}' | grep -qx "${APP_CONTAINER}"; then
        echo "ERROR: container is not running"
        docker logs "${APP_CONTAINER}" --tail 80 2>/dev/null || true
        exit 1
    fi

    HTTP_CODE=$(curl -s -o /tmp/pk-health.json -w "%{http_code}" --max-time 10 "${HEALTH_URL}" 2>/dev/null || echo "000")
    if [ "${HTTP_CODE}" = "200" ] && grep -q '"status":"UP"' /tmp/pk-health.json 2>/dev/null; then
        HEALTH_OK=true
        rm -f /tmp/pk-health.json
        echo "Health check passed (${ELAPSED}s)"
        break
    fi
    echo "  #${ATTEMPT} (${ELAPSED}s): HTTP ${HTTP_CODE}"
    rm -f /tmp/pk-health.json
    sleep "${HEALTH_RETRY_INTERVAL}"
done

if [ "${HEALTH_OK}" != "true" ]; then
    echo "ERROR: health check failed"
    docker logs "${APP_CONTAINER}" --tail 80 2>/dev/null || true
    echo ""
    docker logs "${APP_CONTAINER}" 2>/dev/null | \
        grep -E "(no main manifest|Started PkAppApplication|APPLICATION FAILED|Exception|URL must start with jdbc|Connection refused|Access denied)" | tail -20 || true
    echo ""
    echo "Check Spug env vars for test profile:"
    echo "  PK_BACKEND_MYSQL_HOST (jdbc url), PK_BACKEND_MYSQL_USER, PK_BACKEND_MYSQL_PASSWORD"
    echo "  PK_REDIS_HOST, PK_REDIS_PORT"
    echo "Or write them to ${CONFIG_DIR}/env"
    exit 1
fi

echo ""
echo "Release complete: ${APP_CONTAINER} (${IMAGE_NAME}:${IMAGE_TAG_LATEST})"
