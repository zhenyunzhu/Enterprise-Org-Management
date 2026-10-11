# ============================================================================
# tilas 智能学习辅助系统 —— 后端服务镜像
# ----------------------------------------------------------------------------
# 多阶段构建：
#   阶段 1 (builder) 使用 Maven + JDK 21 编译多模块工程并打包可执行 JAR
#   阶段 2 (runtime) 仅保留 JRE 21，镜像体积更小、攻击面更低
#
# 构建（在工程根目录执行）：
#   docker build -t tilas/tilas-web-management:1.0.0 .
# 或直接使用 docker compose：
#   docker compose up -d --build
# ============================================================================

# ---------------------------------------------------------------------------
# 阶段 1：构建
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# 1) 先复制所有 POM，最大化利用 Docker 层缓存，源码变更时无需重新下载依赖
COPY pom.xml ./
COPY tilas-pojo/pom.xml           tilas-pojo/
COPY tilas-utils/pom.xml          tilas-utils/
COPY tilas-web-management/pom.xml tilas-web-management/
# 预热依赖缓存（模块间依赖尚未安装，失败可忽略）
RUN mvn -B -q dependency:go-offline -DskipTests || true

# 2) 复制源码并打包（跳过测试，测试需依赖真实数据库）
COPY tilas-pojo/src           tilas-pojo/src
COPY tilas-utils/src          tilas-utils/src
COPY tilas-web-management/src tilas-web-management/src
RUN mvn -B clean package -DskipTests

# ---------------------------------------------------------------------------
# 阶段 2：运行
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre

LABEL maintainer="tilas" \
      description="tilas 智能学习辅助系统 - 后端服务"

# 时区、JVM 参数、日志目录均可通过环境变量覆盖
ENV TZ=Asia/Shanghai \
    LOG_HOME=/app/logs \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai"

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app
COPY --from=builder /build/tilas-web-management/target/tilas-web-management.jar app.jar

# 以非 root 用户运行，降低容器逃逸风险
RUN mkdir -p /app/logs \
    && useradd -r -u 1001 -g root tilas \
    && chown -R tilas:root /app
USER tilas

EXPOSE 8080

# 轻量存活探测：仅检测 8080 端口是否可建立 TCP 连接，无需额外安装工具
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD ["bash", "-c", "exec 3<>/dev/tcp/127.0.0.1/8080 || exit 1"]

# 使用 sh -c 以便 JAVA_OPTS 在运行时展开
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
