#!/usr/bin/env bash

set -euo pipefail

GIT_REPO="${PK_GIT_REPO:-https://github.com/estrendidn-dev/pk-backend-app}"
GIT_SYNC="${PK_GIT_SYNC:-true}"
PROJECT_DIR="${PK_PROJECT_DIR:-/opt/pk-backend-app-src}"
CONFIG_DIR="${PK_CONFIG_DIR:-/www/config/pk-backend-app}"

# Resolve profile early so it can pick the git branch before clone/pull.
peek_env_file_value() {
    local want=$1
    local file=$2
    local line key value
    [ -f "${file}" ] || return 1
    while IFS= read -r line || [ -n "${line}" ]; do
        [ -z "${line}" ] && continue
        [[ "${line}" =~ ^[[:space:]]*# ]] && continue
        key="${line%%=*}"
        value="${line#*=}"
        if [ "${key}" = "${want}" ] && [ -n "${value}" ]; then
            echo "${value}"
            return 0
        fi
    done < "${file}"
    return 1
}

PROFILE_HINT="${SPRING_PROFILES_ACTIVE:-}"
if [ -z "${PROFILE_HINT}" ]; then
    PROFILE_HINT="$(peek_env_file_value SPRING_PROFILES_ACTIVE "${CONFIG_DIR}/env" 2>/dev/null || true)"
fi
PROFILE_HINT="$(echo "${PROFILE_HINT:-test}" | tr '[:upper:]' '[:lower:]' | tr -d '[:space:]')"
case "${PROFILE_HINT}" in
    production) PROFILE_HINT="prod" ;;
esac

# Spring Boot profile (application-*.yml only has test/prod)
case "${PROFILE_HINT}" in
    main|prod) SPRING_PROFILE_RUNTIME="prod" ;;
    test|dev|"") SPRING_PROFILE_RUNTIME="test" ;;
    *)
        echo "ERROR: unsupported SPRING_PROFILES_ACTIVE='${PROFILE_HINT}' (use test, prod, or main)"
        exit 1
        ;;
esac

if [ -n "${PK_GIT_BRANCH:-}" ]; then
    GIT_BRANCH="${PK_GIT_BRANCH}"
    GIT_BRANCH_SOURCE="PK_GIT_BRANCH"
else
    # main -> branch main; prod -> branch prod; test -> branch test
    GIT_BRANCH="${PROFILE_HINT}"
    GIT_BRANCH_SOURCE="SPRING_PROFILES_ACTIVE=${PROFILE_HINT}"
fi

IMAGE_NAME="${PK_IMAGE_NAME:-pk-backend-app}"
APP_PORT="${PK_APP_PORT:-8080}"
DOCKER_NETWORK="${PK_DOCKER_NETWORK:-pk-network}"
LOG_DIR="${PK_LOG_DIR:-/www/logs/pk-backend-app}"
REDIS_CONTAINER="${PK_REDIS_CONTAINER:-pk-credit-redis}"
HEALTH_URL="${PK_HEALTH_URL:-http://127.0.0.1:${APP_PORT}/actuator/health}"
HEALTH_MAX_WAIT="${PK_HEALTH_MAX_WAIT:-300}"
HEALTH_RETRY_INTERVAL="${PK_HEALTH_RETRY_INTERVAL:-5}"
KEEP_IMAGES="${PK_KEEP_IMAGES:-3}"
TIMESTAMP="$(date +%Y%m%d%H%M%S)"

echo "=========================================="
echo "pk-backend-app deploy"
echo "=========================================="

sync_git_repo() {
    local repo=$1
    local dir=$2
    local branch=$3

    if ! command -v git >/dev/null 2>&1; then
        echo "ERROR: git is not installed"
        exit 1
    fi

    if [ -d "${dir}/.git" ]; then
        echo "  Updating existing clone..."
        cd "${dir}"
        git remote set-url origin "${repo}"
        # Shallow clone may only have the first branch; fetch target branch explicitly.
        if ! git fetch --depth 1 origin "${branch}"; then
            echo "ERROR: git fetch origin ${branch} failed (remote branch missing?)"
            exit 1
        fi
        if ! git rev-parse --verify --quiet "origin/${branch}" >/dev/null 2>&1; then
            # Some git versions leave tip only on FETCH_HEAD after shallow fetch
            if ! git rev-parse --verify --quiet FETCH_HEAD >/dev/null 2>&1; then
                echo "ERROR: remote branch '${branch}' not found on ${repo}"
                exit 1
            fi
            git checkout -B "${branch}" FETCH_HEAD
        else
            git checkout -B "${branch}" "origin/${branch}"
        fi
        git reset --hard "origin/${branch}" 2>/dev/null || git reset --hard FETCH_HEAD
    elif [ -d "${dir}" ] && [ -n "$(ls -A "${dir}" 2>/dev/null)" ]; then
        echo "ERROR: ${dir} exists but is not a git repository"
        echo "Pick an empty path that does not exist yet, e.g.:"
        echo "  PK_PROJECT_DIR=/opt/pk-backend-app-src ./develop.sh"
        exit 1
    else
        echo "  Cloning ${repo} (branch ${branch})..."
        mkdir -p "$(dirname "${dir}")"
        if ! git clone --branch "${branch}" --depth 1 "${repo}" "${dir}"; then
            echo "ERROR: git clone --branch ${branch} failed (remote branch missing?)"
            exit 1
        fi
        cd "${dir}"
    fi
    echo "  HEAD: $(git rev-parse --short HEAD) $(git log -1 --format='%s')"
}

# ---------- pre-deploy: sync source from GitHub ----------
echo ""
echo "Pre-deploy: source code"
echo "  PK_GIT_REPO=${GIT_REPO}"
echo "  PK_GIT_BRANCH=${GIT_BRANCH} (${GIT_BRANCH_SOURCE})"
echo "  SPRING_PROFILES_ACTIVE hint=${PROFILE_HINT} -> runtime=${SPRING_PROFILE_RUNTIME}"
echo "  PK_PROJECT_DIR=${PROJECT_DIR}"
echo "  PK_GIT_SYNC=${GIT_SYNC}"

if [ "${GIT_SYNC}" = "true" ]; then
    sync_git_repo "${GIT_REPO}" "${PROJECT_DIR}" "${GIT_BRANCH}"
else
    if [ ! -d "${PROJECT_DIR}" ]; then
        echo "ERROR: PK_GIT_SYNC=false but PROJECT_DIR does not exist: ${PROJECT_DIR}"
        exit 1
    fi
    cd "${PROJECT_DIR}"
    echo "  Using local tree (no git sync): $(pwd)"
fi

echo "  Working directory: $(pwd)"

if [ ! -f Dockerfile ]; then
    echo "ERROR: Dockerfile not found in ${PROJECT_DIR}"
    exit 1
fi

if ! docker info >/dev/null 2>&1; then
    echo "ERROR: Docker is not available"
    exit 1
fi

mkdir -p "${CONFIG_DIR}" "${LOG_DIR}"

# ---------- env collection ----------
mask_value() {
    case "$1" in
        *PASSWORD*|*SECRET*|*KEY*) echo "***" ;;
        *)
            if [ "${#2}" -gt 100 ]; then
                echo "${2:0:100}..."
            else
                echo "$2"
            fi
            ;;
    esac
}

ENV_FILE="$(mktemp)"
trap 'rm -f "${ENV_FILE}"' EXIT

set_env() {
    local key=$1
    local value=$2
    [ -z "${key}" ] && return 0
    [ -z "${value}" ] && return 0
    echo "${value}" | grep -qE '(Caused by|Exception|\.java:)' && return 0
    grep -v "^${key}=" "${ENV_FILE}" > "${ENV_FILE}.tmp" 2>/dev/null || true
    mv "${ENV_FILE}.tmp" "${ENV_FILE}"
    printf '%s=%s\n' "${key}" "${value}" >> "${ENV_FILE}"
}

has_env() {
    grep -q "^$1=" "${ENV_FILE}" 2>/dev/null
}

get_env() {
    grep "^$1=" "${ENV_FILE}" 2>/dev/null | tail -n1 | cut -d= -f2-
}

lookup_shell_env() {
    local want=$1
    local key value
    if [ -n "${!want:-}" ]; then
        echo "${!want}"
        return 0
    fi
    while IFS='=' read -r key value; do
        [ -z "${key}" ] && continue
        if [ "${key}" = "${want}" ]; then
            echo "${value}"
            return 0
        fi
    done < <(env)
    return 1
}

trim() {
    local s="${1:-}"
    s="${s#"${s%%[![:space:]]*}"}"
    s="${s%"${s##*[![:space:]]}"}"
    s="${s#\"}"; s="${s%\"}"
    s="${s#\'}"; s="${s%\'}"
    echo "${s}"
}

echo ""
echo "Collecting environment variables"

# Spug config center API (optional)
SPUG_URL="${PK_SPUG_URL:-${SPUG_URL:-https://spug.ng-serve.com}}"
SPUG_URL="${SPUG_URL%/}"
SPUG_CFG_COUNT=0

if [ -n "${SPUG_API_TOKEN:-}" ]; then
    echo "  Fetching Spug config center (${SPUG_URL})"
    SPUG_CFG_TMP="$(mktemp)"
    SPUG_CFG_URL="${SPUG_URL}/api/apis/config/?apiToken=${SPUG_API_TOKEN}&format=env&noPrefix=1"
    HTTP_CODE="$(curl -sS -o "${SPUG_CFG_TMP}" -w '%{http_code}' --max-time 20 "${SPUG_CFG_URL}" 2>/dev/null || echo "000")"
    HTTP_CODE="$(echo "${HTTP_CODE}" | grep -oE '[0-9]{3}$' || echo "000")"
    if [ "${HTTP_CODE}" != "200" ]; then
        echo "  WARN: Spug config fetch failed HTTP ${HTTP_CODE}"
    else
        while IFS= read -r line || [ -n "${line}" ]; do
            [ -z "${line}" ] && continue
            [[ "${line}" =~ ^[[:space:]]*# ]] && continue
            if [[ "${line}" =~ ^([A-Za-z_][A-Za-z0-9_]*)[[:space:]]*=[[:space:]]*(.*)$ ]]; then
                set_env "${BASH_REMATCH[1]}" "${BASH_REMATCH[2]//$'\r'/}"
                SPUG_CFG_COUNT=$((SPUG_CFG_COUNT + 1))
            fi
        done < "${SPUG_CFG_TMP}"
        echo "  Loaded ${SPUG_CFG_COUNT} keys from Spug config center"
    fi
    rm -f "${SPUG_CFG_TMP}"
else
    echo "  SPUG_API_TOKEN not set; skipping Spug config center"
fi

# Host env file (overrides Spug config)
HOST_ENV_FILE="${CONFIG_DIR}/env"
if [ -f "${HOST_ENV_FILE}" ]; then
    echo "  Loading ${HOST_ENV_FILE}"
    while IFS= read -r line || [ -n "${line}" ]; do
        [ -z "${line}" ] && continue
        [[ "${line}" =~ ^[[:space:]]*# ]] && continue
        key="${line%%=*}"
        value="${line#*=}"
        set_env "${key}" "${value}"
    done < "${HOST_ENV_FILE}"
else
    echo "  No ${HOST_ENV_FILE} (optional)"
fi

PASS_KEYS=(
    SPRING_PROFILES_ACTIVE
    PK_APP_PORT
    PK_BACKEND_MYSQL_HOST PK_BACKEND_MYSQL_USER PK_BACKEND_MYSQL_PASSWORD
    PK_DB_MAXIMUM_POOL_SIZE
    PK_REDIS_HOST PK_REDIS_PORT
    PK_SLS_ENABLED PK_SLS_ENDPOINT PK_SLS_PROJECT
    PK_SLS_ACCESS_KEY_ID PK_SLS_ACCESS_KEY_SECRET PK_SLS_LOGSTORE
    PK_BIOMETRIC_OSS_ENABLED PK_BIOMETRIC_OSS_ENDPOINT PK_BIOMETRIC_OSS_BUCKET
    PK_BIOMETRIC_OSS_ACCESS_KEY_ID PK_BIOMETRIC_OSS_ACCESS_KEY_SECRET
    PK_OCR_ACCESS_KEY PK_OCR_SECRET_KEY PK_OCR_DEV_LENDER_SYNC_ENABLED
    PK_AUTH_JWT_SECRET PK_FIELD_ENCRYPTION_KEY
    PK_CALLBACK_CLIENT_ID PK_CALLBACK_CLIENT_SECRET
    PK_DEBUG_USER_PROGRESS_ENABLED PK_DEBUG_USER_PROGRESS_TOKEN
    PK_LENDER_APIPARTNER_READ_TIMEOUT_MS
)

for key in "${PASS_KEYS[@]}"; do
    if val="$(lookup_shell_env "${key}" 2>/dev/null || true)"; then
        [ -n "${val}" ] && set_env "${key}" "${val}"
    fi
done

if ! has_env PK_LENDER_APIPARTNER_READ_TIMEOUT_MS; then
    if val="$(lookup_shell_env PK_LENDER_PENDANAAN_READ_TIMEOUT_MS 2>/dev/null || true)"; then
        [ -n "${val}" ] && set_env "PK_LENDER_APIPARTNER_READ_TIMEOUT_MS" "${val}"
    elif has_env PK_LENDER_PENDANAAN_READ_TIMEOUT_MS; then
        set_env "PK_LENDER_APIPARTNER_READ_TIMEOUT_MS" "$(get_env PK_LENDER_PENDANAAN_READ_TIMEOUT_MS)"
    fi
fi

# Deploy knobs from env file / shell (override script defaults)
if has_env PK_APP_PORT; then
    APP_PORT="$(get_env PK_APP_PORT)"
elif val="$(lookup_shell_env PK_APP_PORT 2>/dev/null || true)"; then
    [ -n "${val}" ] && APP_PORT="${val}"
fi
HEALTH_URL="${PK_HEALTH_URL:-http://127.0.0.1:${APP_PORT}/actuator/health}"

if has_env PK_REDIS_CONTAINER; then
    REDIS_CONTAINER="$(get_env PK_REDIS_CONTAINER)"
elif val="$(lookup_shell_env PK_REDIS_CONTAINER 2>/dev/null || true)"; then
    [ -n "${val}" ] && REDIS_CONTAINER="${val}"
fi

if has_env PK_APP_CONTAINER; then
    APP_CONTAINER_OVERRIDE="$(get_env PK_APP_CONTAINER)"
elif val="$(lookup_shell_env PK_APP_CONTAINER 2>/dev/null || true)"; then
    APP_CONTAINER_OVERRIDE="${val}"
else
    APP_CONTAINER_OVERRIDE=""
fi

# ---------- profile ----------
PROFILE_RAW="$(get_env SPRING_PROFILES_ACTIVE 2>/dev/null || true)"
[ -z "${PROFILE_RAW}" ] && PROFILE_RAW="${SPRING_PROFILES_ACTIVE:-}"
PROFILE_RAW="$(trim "${PROFILE_RAW}")"
PROFILE_RAW_LC="$(echo "${PROFILE_RAW}" | tr '[:upper:]' '[:lower:]')"

case "${PROFILE_RAW_LC}" in
    main|prod|production)
        SPRING_PROFILES_ACTIVE="prod"
        APP_CONTAINER="${APP_CONTAINER_OVERRIDE:-pk-backend-app}"
        PROFILE_TAG="prod"
        ;;
    test|dev|"")
        SPRING_PROFILES_ACTIVE="test"
        APP_CONTAINER="${APP_CONTAINER_OVERRIDE:-pk-backend-app-dev}"
        PROFILE_TAG="test"
        ;;
    *)
        echo "ERROR: unsupported SPRING_PROFILES_ACTIVE='${PROFILE_RAW}' (use test, prod, or main)"
        exit 1
        ;;
esac

IMAGE_TAG="${PROFILE_TAG}-${TIMESTAMP}"
IMAGE_TAG_LATEST="${PROFILE_TAG}-latest"

set_env "SPRING_PROFILES_ACTIVE" "${SPRING_PROFILES_ACTIVE}"
set_env "PK_APP_PORT" "${APP_PORT}"

echo ""
echo "Deploy target"
echo "  SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}"
echo "  container=${APP_CONTAINER}"
echo "  port=${APP_PORT}"
echo "  redis_container=${REDIS_CONTAINER}"
echo "  image=${IMAGE_NAME}:${IMAGE_TAG}"
echo "  config=${CONFIG_DIR}"
echo "  logs=${LOG_DIR}"

if [ "${SPRING_PROFILES_ACTIVE}" = "prod" ]; then
    PROD_REQUIRED=(
        PK_BACKEND_MYSQL_HOST PK_BACKEND_MYSQL_USER PK_BACKEND_MYSQL_PASSWORD
        PK_DB_MAXIMUM_POOL_SIZE
        PK_REDIS_HOST PK_REDIS_PORT
        PK_DEBUG_USER_PROGRESS_ENABLED PK_DEBUG_USER_PROGRESS_TOKEN
        PK_LENDER_APIPARTNER_READ_TIMEOUT_MS
        PK_CALLBACK_CLIENT_ID PK_CALLBACK_CLIENT_SECRET
        PK_BIOMETRIC_OSS_ENABLED PK_BIOMETRIC_OSS_ENDPOINT PK_BIOMETRIC_OSS_BUCKET
        PK_BIOMETRIC_OSS_ACCESS_KEY_ID PK_BIOMETRIC_OSS_ACCESS_KEY_SECRET
        PK_OCR_DEV_LENDER_SYNC_ENABLED
        PK_SLS_ENABLED PK_SLS_ENDPOINT PK_SLS_PROJECT
        PK_SLS_ACCESS_KEY_ID PK_SLS_ACCESS_KEY_SECRET
    )
    PROD_MISSING=()
    for key in "${PROD_REQUIRED[@]}"; do
        has_env "${key}" || PROD_MISSING+=("${key}")
    done
    if [ "${#PROD_MISSING[@]}" -gt 0 ]; then
        echo "ERROR: prod profile missing required env vars:"
        for key in "${PROD_MISSING[@]}"; do
            echo "  - ${key}"
        done
        exit 1
    fi
    if [ "$(get_env PK_DEBUG_USER_PROGRESS_ENABLED)" = "true" ] || \
       [ "$(get_env PK_OCR_DEV_LENDER_SYNC_ENABLED)" = "true" ]; then
        echo "WARN: debug/dev flags enabled on prod:"
        echo "  PK_DEBUG_USER_PROGRESS_ENABLED=$(get_env PK_DEBUG_USER_PROGRESS_ENABLED)"
        echo "  PK_OCR_DEV_LENDER_SYNC_ENABLED=$(get_env PK_OCR_DEV_LENDER_SYNC_ENABLED)"
    fi
fi

# Redis on same Docker network
if docker ps --format '{{.Names}}' | grep -qx "${REDIS_CONTAINER}"; then
    docker network inspect "${DOCKER_NETWORK}" >/dev/null 2>&1 || docker network create "${DOCKER_NETWORK}"
    docker network connect "${DOCKER_NETWORK}" "${REDIS_CONTAINER}" 2>/dev/null || true
    current_redis="$(get_env PK_REDIS_HOST 2>/dev/null || true)"
    if [ -z "${current_redis}" ] || [ "${current_redis}" = "localhost" ] || \
       [ "${current_redis}" = "127.0.0.1" ] || [ "${current_redis}" = "host.docker.internal" ]; then
        set_env "PK_REDIS_HOST" "${REDIS_CONTAINER}"
        set_env "PK_REDIS_PORT" "6379"
        echo "  Redis -> ${REDIS_CONTAINER}:6379 (Docker network)"
    fi
elif ! has_env PK_REDIS_HOST; then
    echo "ERROR: Redis container ${REDIS_CONTAINER} not running and PK_REDIS_HOST not set"
    echo "Check: docker ps --format '{{.Names}}' | grep -i redis"
    echo "Then set in ${HOST_ENV_FILE} or command line:"
    echo "  PK_REDIS_CONTAINER=<exact-container-name>"
    echo "or:"
    echo "  PK_REDIS_HOST=<host> PK_REDIS_PORT=6379"
    exit 1
fi

if ! has_env PK_BACKEND_MYSQL_HOST || ! has_env PK_BACKEND_MYSQL_USER || ! has_env PK_BACKEND_MYSQL_PASSWORD; then
    echo "ERROR: missing PK_BACKEND_MYSQL_HOST / USER / PASSWORD"
    echo "Set them in ${HOST_ENV_FILE} or export before running deploy.sh"
    exit 1
fi

jdbc="$(get_env PK_BACKEND_MYSQL_HOST)"
if ! echo "${jdbc}" | grep -q '^jdbc:'; then
    echo "ERROR: PK_BACKEND_MYSQL_HOST must be a jdbc: URL"
    exit 1
fi

ENV_ARGS=()
while IFS= read -r line; do
    [ -z "${line}" ] && continue
    ENV_ARGS+=("-e" "${line}")
done < "${ENV_FILE}"

echo ""
echo "Container env (${#ENV_ARGS[@]} args, secrets masked):"
while IFS= read -r line; do
    [ -z "${line}" ] && continue
    k="${line%%=*}"
    v="${line#*=}"
    echo "  ${k}=$(mask_value "${k}" "${v}")"
done < "${ENV_FILE}"

# ---------- build (keep old container running until image is verified) ----------
echo ""
echo "=========================================="
echo "Build Docker image"
echo "=========================================="
BUILD_ARGS=()
if [ "${PK_DOCKER_NO_CACHE:-false}" = "true" ]; then
    BUILD_ARGS+=(--no-cache)
    echo "  --no-cache enabled"
fi

# bash set -u: empty array expansion is unbound on some versions
docker build ${BUILD_ARGS[@]+"${BUILD_ARGS[@]}"} -t "${IMAGE_NAME}:${IMAGE_TAG}" .
docker tag "${IMAGE_NAME}:${IMAGE_TAG}" "${IMAGE_NAME}:${IMAGE_TAG_LATEST}"
docker tag "${IMAGE_NAME}:${IMAGE_TAG}" "${IMAGE_NAME}:latest"
echo "  Built ${IMAGE_NAME}:${IMAGE_TAG}"

echo ""
echo "Verify image"
if ! docker run --rm --entrypoint sh "${IMAGE_NAME}:${IMAGE_TAG}" -c 'test -f /app/app.jar && ls -lh /app/app.jar'; then
    echo "ERROR: /app/app.jar missing in new image"
    exit 1
fi

# ---------- switch container ----------
echo ""
echo "=========================================="
echo "Switch container"
echo "=========================================="
docker network inspect "${DOCKER_NETWORK}" >/dev/null 2>&1 || docker network create "${DOCKER_NETWORK}"

echo "  Release port ${APP_PORT}..."
while IFS= read -r holder; do
    [ -z "${holder}" ] && continue
    echo "    stop ${holder}"
    docker rm -f "${holder}" >/dev/null 2>&1 || true
done < <(docker ps -a --filter "publish=${APP_PORT}" --format '{{.Names}}' 2>/dev/null || true)

docker rm -f "${APP_CONTAINER}" 2>/dev/null || true
if [ "${APP_CONTAINER}" = "pk-backend-app" ]; then
    docker rm -f pk-backend-app-dev 2>/dev/null || true
elif [ "${APP_CONTAINER}" = "pk-backend-app-dev" ]; then
    docker rm -f pk-backend-app 2>/dev/null || true
fi

docker run -d \
    --name "${APP_CONTAINER}" \
    --restart unless-stopped \
    --network "${DOCKER_NETWORK}" \
    -p "${APP_PORT}:${APP_PORT}" \
    "${ENV_ARGS[@]}" \
    -v "${CONFIG_DIR}:/app/config:ro" \
    -v "${LOG_DIR}:/app/logs" \
    "${IMAGE_NAME}:${IMAGE_TAG_LATEST}"

sleep 3
docker logs "${APP_CONTAINER}" --tail 40 2>/dev/null || true

# ---------- health check ----------
echo ""
echo "=========================================="
echo "Health check ${HEALTH_URL}"
echo "=========================================="

http_code() {
    local url=$1
    local out
    out="$(curl -s -o /tmp/pk-health.json -w '%{http_code}' --max-time 8 "${url}" 2>/dev/null)" || true
    echo "${out:-000}" | grep -oE '[0-9]{3}$' || echo "000"
}

probe_inside() {
    docker exec "${APP_CONTAINER}" sh -c "
        if command -v wget >/dev/null 2>&1; then
            wget -qO- --timeout=5 'http://127.0.0.1:${APP_PORT}/actuator/health'
        elif command -v curl >/dev/null 2>&1; then
            curl -sf --max-time 5 'http://127.0.0.1:${APP_PORT}/actuator/health'
        elif command -v nc >/dev/null 2>&1; then
            nc -z -w 2 127.0.0.1 ${APP_PORT} && echo '{\"status\":\"PORT_OPEN\"}'
        else
            exit 1
        fi
    " 2>/dev/null
}

HEALTH_OK=false
START_TS="$(date +%s)"
ATTEMPT=0

while true; do
    ATTEMPT=$((ATTEMPT + 1))
    ELAPSED=$(( $(date +%s) - START_TS ))
    [ "${ELAPSED}" -ge "${HEALTH_MAX_WAIT}" ] && break

    if ! docker ps --format '{{.Names}}' | grep -qx "${APP_CONTAINER}"; then
        echo "ERROR: container exited"
        docker logs "${APP_CONTAINER}" --tail 100 2>/dev/null || true
        exit 1
    fi

    CODE="$(http_code "${HEALTH_URL}")"
    if [ "${CODE}" = "200" ] && grep -q '"status":"UP"' /tmp/pk-health.json 2>/dev/null; then
        HEALTH_OK=true
        echo "  Passed (${ELAPSED}s)"
        break
    fi

    INNER="$(probe_inside || true)"
    if echo "${INNER}" | grep -q '"status":"UP"'; then
        HEALTH_OK=true
        echo "  Passed in-container (${ELAPSED}s); host ${HEALTH_URL} may be blocked"
        break
    fi

    if [ $((ATTEMPT % 6)) -eq 0 ]; then
        echo "  #${ATTEMPT} (${ELAPSED}s) HTTP ${CODE}"
        docker logs "${APP_CONTAINER}" 2>/dev/null | \
            grep -E 'Started PkAppApplication|APPLICATION FAILED|Access denied|Connection refused|Redis|Hikari' | tail -5 || true
    else
        echo "  #${ATTEMPT} (${ELAPSED}s) HTTP ${CODE}"
    fi
    sleep "${HEALTH_RETRY_INTERVAL}"
done
rm -f /tmp/pk-health.json

if [ "${HEALTH_OK}" != "true" ]; then
    echo "ERROR: health check failed"
    docker logs "${APP_CONTAINER}" --tail 150 2>/dev/null || true
    exit 1
fi

# ---------- prune old images ----------
mapfile -t OLD_TAGS < <(docker images "${IMAGE_NAME}" --format '{{.Tag}}' | grep -E "^${PROFILE_TAG}-[0-9]{14}$" | sort -r || true)
idx=0
for tag in "${OLD_TAGS[@]:-}"; do
    [ -z "${tag}" ] && continue
    idx=$((idx + 1))
    [ "${idx}" -le "${KEEP_IMAGES}" ] && continue
    docker rmi "${IMAGE_NAME}:${tag}" >/dev/null 2>&1 || true
done
docker image prune -f >/dev/null 2>&1 || true

echo ""
echo "=========================================="
echo "Deploy complete"
echo "  profile=${SPRING_PROFILES_ACTIVE}"
echo "  container=${APP_CONTAINER}"
echo "  image=${IMAGE_NAME}:${IMAGE_TAG_LATEST}"
echo "  health=${HEALTH_URL}"
echo "=========================================="
