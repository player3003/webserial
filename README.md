# WebSerial MQTT 数据采集系统

基于 Spring Boot 的串口数据采集系统，通过 MQTT 协议接收设备数据，存储到 MongoDB，并通过 WebSocket 实时推送到前端。

## 📋 项目简介

本项目实现了一个完整的物联网数据采集和展示系统：
- **MQTT 订阅**：监听设备通过 MQTT 发送的串口数据
- **数据解析**：支持 JSON 和键值对格式（如 `TEMP:24.5;HUM:60`）
- **数据存储**：使用 MongoDB 持久化设备数据
- **实时推送**：通过 WebSocket 向前端实时推送最新数据
- **REST API**：提供历史数据查询接口

## 🏗️ 项目结构

```
webserial/
├── docker-compose.yml              # Docker 编排配置文件
├── pom.xml                         # Maven 项目配置
├── README.md                       # 项目说明文档
└── src/
    └── main/
        ├── java/com/example/webserial/
        │   ├── WebSerialApplication.java       # Spring Boot 主启动类
        │   ├── controller/
        │   │   └── DeviceDataController.java   # REST API 控制器
        │   ├── entity/
        │   │   └── SerialDataEntity.java       # MongoDB 数据实体
        │   ├── mqtt/
        │   │   └── MqttService.java            # MQTT 客户端服务
        │   ├── processor/
        │   │   └── SerialDataProcessor.java    # 数据处理器
        │   ├── repo/
        │   │   └── SerialDataRepository.java   # MongoDB 数据访问层
        │   └── websocket/
        │       ├── WebSocketConfig.java        # WebSocket 配置
        │       └── WebSocketPushService.java   # WebSocket 推送服务
        └── resources/
            ├── application.yml                 # 应用配置文件
            └── mosquitto.conf                  # Mosquitto MQTT 配置
```

### 核心模块说明

#### 1. MQTT 模块 (`mqtt/`)
- **MqttService.java**: MQTT 客户端，订阅指定主题接收设备数据
  - 连接 MQTT Broker
  - 订阅主题：`device/+/serial/raw`（通配符 `+` 匹配任意设备 ID）
  - 接收消息后委托给数据处理器

#### 2. 数据处理模块 (`processor/`)
- **SerialDataProcessor.java**: 数据解析和处理核心
  - 解析 MQTT 主题提取设备 ID
  - 支持 JSON 和键值对格式数据解析
  - 保存数据到 MongoDB
  - 通过 WebSocket 推送到前端

#### 3. 数据持久化模块 (`entity/` + `repo/`)
- **SerialDataEntity.java**: MongoDB 文档实体
  - `deviceId`: 设备标识
  - `timestamp`: 时间戳
  - `rawData`: 原始数据
  - `payload`: 解析后的数据（Map 格式）
- **SerialDataRepository.java**: MongoDB 数据访问接口

#### 4. WebSocket 模块 (`websocket/`)
- **WebSocketConfig.java**: WebSocket/STOMP 配置
  - 端点：`/ws`
  - 支持 SockJS 回退
- **WebSocketPushService.java**: 实时推送服务
  - 推送主题：`/topic/device/{deviceId}`

#### 5. REST API 模块 (`controller/`)
- **DeviceDataController.java**: HTTP REST 接口
  - `GET /api/device/{deviceId}/data?from={timestamp}&to={timestamp}`
  - 查询指定设备在时间范围内的历史数据

## 🔧 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring Boot | 3.2.3 | 应用框架 |
| Eclipse Paho | 1.2.5 | MQTT 客户端 |
| MongoDB | 6.0 | 数据库 |
| Eclipse Mosquitto | 2.0 | MQTT Broker |
| WebSocket (STOMP) | - | 实时通信 |
| Docker & Docker Compose | - | 容器化部署 |

## 🚀 快速开始

### 前置要求

- **Java 17+**（必需）
- **Docker Desktop**（必需）
- **VS Code + Extension Pack for Java**（推荐）或 **IntelliJ IDEA**

> ⚠️ **重要**：Maven 不需要单独安装，使用 VS Code 的 Maven for Java 插件或 IDE 内置 Maven 即可。

#### 检查安装状态

```bash
# 检查 Java
java -version

# 检查 Docker
docker --version
```

#### 安装指南

**1. 安装 Java 17**
- 下载：https://adoptium.net/zh-CN/temurin/releases/
- 选择 Java 17 LTS 版本
- 安装后验证：`java -version`

**2. 安装 Docker Desktop**
- 下载：https://www.docker.com/products/docker-desktop/
- 安装后需重启电脑
- 首次启动可能需要启用 WSL 2

**3. 安装开发工具（二选一）**

**选项 A：VS Code（推荐新手）**
1. 下载安装 VS Code：https://code.visualstudio.com/
2. 安装 "Extension Pack for Java" 插件（包含 Maven for Java）
3. 用 VS Code 打开项目文件夹

**选项 B：IntelliJ IDEA**
- 下载：https://www.jetbrains.com/idea/download/
- 自带 Maven，无需额外配置

### 1. 启动 Docker Desktop

**Windows 用户**：
1. 打开 Docker Desktop 应用程序
2. 等待 Docker Engine 启动完成（任务栏图标变为绿色）
3. 验证 Docker 运行状态：
   ```bash
   docker --version
   docker ps
   ```

**如果未安装 Docker Desktop**：
- 下载地址：https://www.docker.com/products/docker-desktop/
- 安装后需重启电脑
- 首次启动可能需要启用 WSL 2（Windows Subsystem for Linux）

### 2. 修改配置文件

> ⚠️ **重要**：在 Windows 本地运行时，需要修改 MongoDB 和 MQTT 的连接地址。

编辑 `src/main/resources/application.yml`，将容器名称改为 `localhost`：

```yaml
server:
  port: 8080

spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/webserialdb  # 改为 localhost

app:
  mqtt:
    broker: tcp://localhost:1883                  # 改为 localhost
    clientId: web-serial-server
    topic: device/+/serial/raw
```

**为什么要改为 localhost？**

当你的 Spring Boot 应用在 Windows 上直接运行（不在 Docker 容器内）时，无法通过容器名称（`mongo`、`mosquitto`）访问服务。但 Docker 已经把容器端口映射到了 `localhost`，所以需要使用 `localhost` 来连接。

### 3. 启动基础服务（MQTT Broker + MongoDB）

使用 Docker Compose 启动 Mosquitto 和 MongoDB：

```bash
# 在项目根目录执行
docker-compose up -d
```

**服务说明**：
- **Mosquitto MQTT Broker**
  - 端口：`1883`（MQTT）、`9001`（WebSocket MQTT）
  - 配置文件：`src/main/resources/mosquitto.conf`
  - 允许匿名连接
  
- **MongoDB**
  - 端口：`27017`
  - 数据库：`webserialdb`
  - 数据卷：`mongo_data`（持久化存储）

验证服务状态：
```bash
# 查看容器运行状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 4. 启动 Spring Boot 应用

#### 方式一：使用 VS Code（推荐）

1. 用 VS Code 打开项目文件夹 `webserial`
2. 打开 `src/main/java/com/example/webserial/WebSerialApplication.java`
3. 在 `main` 方法上方会出现 **"Run | Debug"** 链接
4. 点击 **"Run"** 启动应用

或者使用 Maven 侧边栏：
1. 在 VS Code 左侧找到 **"MAVEN"** 面板
2. 展开项目 → `Plugins` → `spring-boot`
3. 双击 `spring-boot:run`

#### 方式二：使用 IntelliJ IDEA

1. 用 IntelliJ IDEA 打开项目
2. 找到 `WebSerialApplication.java` 主类
3. 右键点击 → Run 'WebSerialApplication'

### 5. 验证服务

应用启动后，控制台会输出：
```
🔧 Initializing MQTT Service...
   Broker: tcp://localhost:1883
   Client ID: web-serial-server
   Topic Pattern: device/+/serial/raw
✅ MQTT connected and subscribed to: device/+/serial/raw
Tomcat started on port 8080 (http) with context path ''
Started WebSerialApplication in X.XXX seconds
```

**首次启动提示**：
- 第一次运行时，Maven 会下载所有依赖包
- 下载完成后，依赖会缓存到本地，以后启动只需几秒钟
- 耐心等待，不要中断下载过程

访问端点：
- **WebSocket**: `ws://localhost:8080/ws`
- **REST API**: `http://localhost:8080/api/device/{deviceId}/data?from=0&to={timestamp}`

## 📡 使用示例

### 1. 发送 MQTT 测试数据

#### 使用 Docker 容器发送（推荐）

```powershell
# 发送键值对格式数据
docker exec mosquitto mosquitto_pub -h localhost -t "device/sensor001/serial/raw" -m "TEMP:24.5;HUM:60;PRESSURE:1013"

# 发送 JSON 格式数据（注意 Windows PowerShell 中 JSON 的引号转义）
docker exec mosquitto mosquitto_pub -h localhost -t "device/sensor002/serial/raw" -m '{\"temperature\":25.3,\"humidity\":58,\"co2\":420}'
```

**成功标志**：应用终端会显示：
```
📥 MQTT Message Received - Topic: device/sensor001/serial/raw, Payload: TEMP:24.5;HUM:60;PRESSURE:1013
🔄 Processing message - Topic: device/sensor001/serial/raw, Raw: TEMP:24.5;HUM:60;PRESSURE:1013
   Device ID: sensor001
   Timestamp: 1764159387609
   Payload: {HUM=60, PRESSURE=1013, TEMP=24.5}
💾 Saving to MongoDB...
✅ Saved with ID: 6926ef9b2d848b779fc67e55
📡 Pushing to WebSocket...
✅ Process completed
```

#### 使用 MQTT 客户端工具（如 MQTTX）

1. 下载 MQTTX：https://mqttx.app/zh
2. 连接到 `localhost:1883`
3. 发布消息到主题 `device/sensor001/serial/raw`

### 2. 前端 WebSocket 订阅（JavaScript）

```javascript
// 使用 SockJS + STOMP.js
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    // 订阅特定设备的实时数据
    stompClient.subscribe('/topic/device/sensor001', function(message) {
        const data = JSON.parse(message.body);
        console.log('Received:', data);
        // data.deviceId, data.timestamp, data.payload
    });
});
```

### 3. 查询历史数据

#### 方式一：使用浏览器（最简单）

在浏览器地址栏输入：
```
http://localhost:8080/api/device/sensor001/data?from=0&to=9999999999999
```

#### 方式二：使用 PowerShell

```powershell
# 查询设备 sensor001 的所有历史数据
Invoke-RestMethod -Uri "http://localhost:8080/api/device/sensor001/data?from=0&to=9999999999999"
```

响应示例：
```json
[
  {
    "id": "674...",
    "deviceId": "sensor001",
    "timestamp": 1732579123456,
    "rawData": "TEMP:24.5;HUM:60",
    "payload": {
      "TEMP": 24.5,
      "HUM": 60
    }
  }
]
```

## ⚙️ 配置说明

### application.yml

```yaml
server:
  port: 8080                          # 应用端口

spring:
  data:
    mongodb:
      uri: mongodb://mongo:27017/webserialdb  # MongoDB 连接

app:
  mqtt:
    broker: tcp://mosquitto:1883      # MQTT Broker 地址
    clientId: web-serial-server       # MQTT 客户端 ID
    topic: device/+/serial/raw        # 订阅主题（+ 为通配符）
```

### 自定义配置

如需修改配置，可以：
1. 直接编辑 `src/main/resources/application.yml`
2. 或通过环境变量覆盖（如 `SPRING_DATA_MONGODB_URI`）
3. 或通过启动参数：`java -jar app.jar --app.mqtt.broker=tcp://192.168.1.100:1883`

## 🛠️ 开发指南

### 添加新的数据格式支持

修改 `SerialDataProcessor.java` 中的 `parseRawPayload()` 方法：

```java
private Map<String, Object> parseRawPayload(String raw) {
    // 添加自定义解析逻辑
    if (raw.startsWith("CUSTOM:")) {
        // 你的解析代码
    }
    // ...
}
```

### 扩展 REST API

在 `DeviceDataController.java` 中添加新的端点：

```java
@GetMapping("/{deviceId}/latest")
public SerialDataEntity getLatest(@PathVariable String deviceId) {
    // 实现逻辑
}
```

### 自定义 WebSocket 推送

修改 `WebSocketPushService.java` 的推送逻辑。

## 📦 部署建议

### 生产环境部署

1. **使用环境变量管理配置**
```bash
export MQTT_BROKER=tcp://production-mqtt:1883
export MONGODB_URI=mongodb://user:pass@production-mongo:27017/webserialdb
```

2. **启用认证**
- 配置 Mosquitto 用户名/密码
- MongoDB 启用访问控制

3. **资源限制**
在 `docker-compose.yml` 中添加资源限制：
```yaml
services:
  mosquitto:
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 512M
```

4. **日志管理**
- 配置 Spring Boot 日志级别
- 使用 ELK 或其他日志聚合工具

## 🐛 故障排查

### 1. MQTT 连接失败
- 检查 Mosquitto 容器是否运行：`docker ps`
- 查看 Mosquitto 日志：`docker logs mosquitto`
- 验证端口映射：`netstat -an | findstr 1883`（Windows）

### 2. MongoDB 连接失败
- 检查 MongoDB 容器：`docker ps`
- 测试连接：`docker exec mongo mongosh --eval "db.version()"`

### 3. WebSocket 连接失败
- 检查跨域配置（CORS）
- 验证 SockJS 端点：`http://localhost:8080/ws/info`

### 4. 数据未接收
- 确认 MQTT 主题格式正确：`device/{deviceId}/serial/raw`
- 查看应用日志确认是否收到消息（终端会显示 `📥 MQTT Message Received`）
- 检查数据库：`docker exec mongo mongosh webserialdb --eval "db.serial_data.countDocuments()"`

### 5. REST API 返回 500 错误
**问题**：访问 `/api/device/{deviceId}/data` 时报错
```
java.lang.IllegalArgumentException: Name for argument of type [java.lang.String] not specified
```

**解决方法**：在控制器的注解中明确指定参数名称：
```java
@GetMapping("/{deviceId}/data")
public List<SerialDataEntity> query(
        @PathVariable("deviceId") String deviceId,  // 明确指定参数名
        @RequestParam("from") long from,
        @RequestParam("to") long to) {
    // ...
}
```

### 6. 应用启动慢
- **首次运行**：Maven 需要下载依赖，可能需要 5-15 分钟
- **后续运行**：依赖已缓存，通常几秒钟即可启动
- 查看下载进度：观察 VS Code 输出窗口的日志

## 📄 许可证

本项目仅供学习参考使用。

## 👥 联系方式

如有问题或建议，请提交 Issue。

---

**最后更新**: 2025年11月26日
