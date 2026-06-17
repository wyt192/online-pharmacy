# Online Pharmacy

线上购药系统后端示例项目，基于 Java 17 和 Spring Boot 4.1.0 开发。项目实现了药品信息服务、购物车与订单、智能药师咨询三个模块，并使用 H2 内存数据库提供本地测试数据。

## 功能模块

- 药品信息服务：药品搜索、药品详情、库存展示、处方药标识。
- 购物车与订单：加入购物车、查看购物车、提交订单、查看订单详情。
- 智能药师咨询：支持本地 Mock 回复，预留并实现 DeepSeek HTTP 调用结构，高风险问题提示转人工药师或线下就医。
- 通用能力：统一返回结果、业务异常、全局异常处理、参数校验。
- 单元测试：覆盖 Result、Service、Controller、Repository 和 AI 模块核心逻辑。

## 技术栈

- Java 17
- Spring Boot 4.1.0
- Spring MVC
- Spring Data JPA
- Jakarta Validation
- H2 Database
- Maven Wrapper
- JUnit 5
- Mockito
- AssertJ
- MockMvc

## 项目结构

```text
src/main/java/com/example/online_pharmacy
├── ai              # 智能药师咨询模块
├── cart            # 购物车模块
├── common          # 统一返回、异常、全局异常处理
├── consultation    # 咨询记录与风险等级
├── drug            # 药品信息服务模块
├── order           # 订单模块
└── OnlinePharmacyApplication.java

src/test/java/com/example/online_pharmacy
├── ai              # AI 模块测试
├── cart            # 购物车测试
├── common          # 通用组件测试
├── drug            # 药品模块测试
├── order           # 订单模块测试
├── repository      # Repository 数据层测试
└── TestFixtures.java

src/main/resources
├── application.properties
└── data.sql        # H2 初始化药品测试数据
```

## 快速启动

确认本机已安装 JDK 17，然后在项目根目录执行：

```powershell
.\mvnw.cmd spring-boot:run
```

默认服务地址：

```text
http://localhost:8080
```

H2 控制台：

```text
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:pharmacy_db
User Name: sa
Password: 留空
```

## 配置说明

主要配置文件：

```text
src/main/resources/application.properties
```

AI 默认使用 Mock 模式：

```properties
ai.provider=mock
```

切换 DeepSeek 时，不要把 API Key 写进代码或配置文件，使用环境变量：

PowerShell：

```powershell
$env:DEEPSEEK_API_KEY="sk-你的APIKey"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--ai.provider=deepseek"
```

cmd：

```cmd
set "DEEPSEEK_API_KEY=sk-你的APIKey"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--ai.provider=deepseek"
```

DeepSeek 地址和模型通过配置读取：

```properties
deepseek.base-url=https://api.deepseek.com
deepseek.api-key=${DEEPSEEK_API_KEY:}
deepseek.model=deepseek-v4-pro
```

## 单元测试

运行全部测试：

```powershell
.\mvnw.cmd test
```

运行单个测试类：

```powershell
.\mvnw.cmd -Dtest=ResultTest test
.\mvnw.cmd -Dtest=OrderServiceTest test
```

运行单个测试方法：

```powershell
.\mvnw.cmd -Dtest=ResultTest#successShouldCreateSuccessfulResultWithData test
```

不要使用 `javac XxxTest.java && java XxxTest` 运行 JUnit 测试。测试依赖由 Maven 管理，必须通过 Maven 或 IDE 的 JUnit 测试入口运行。

当前测试覆盖：

- `ResultTest`：统一返回结果。
- `DrugServiceTest`：药品详情、搜索、异常场景。
- `CartServiceTest`：加入购物车、数量合并、库存校验、金额统计。
- `OrderServiceTest`：提交订单、扣减库存、清空购物车、订单详情。
- `MockAiClientTest`：Mock AI 风险等级规则。
- `AiPharmacistServiceTest`：AI 咨询、风险升级、记录保存。
- `DrugControllerTest`、`CartControllerTest`、`OrderControllerTest`、`AiPharmacistControllerTest`：接口层参数校验和统一响应。
- `RepositoryTest`：药品、购物车、订单、咨询记录的数据层查询。

测试报告位置：

```text
target/surefire-reports/
```

通过标准：

```text
BUILD SUCCESS
Failures: 0
Errors: 0
```

## 接口清单

统一返回结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1780000000000
}
```

### 药品模块

搜索药品：

```http
GET /api/drugs/search?keyword=Ibuprofen&page=0&size=10
```

可选查询参数：

- `keyword`：药品名称、厂家、描述关键字。
- `category`：药品分类。
- `prescriptionRequired`：是否处方药，`true` 或 `false`。
- `page`：页码，从 0 开始。
- `size`：每页数量，1 到 100。

查看药品详情：

```http
GET /api/drugs/1
```

### 购物车模块

加入购物车：

```http
POST /api/cart/items
Content-Type: application/json

{
  "userId": 1,
  "drugId": 1,
  "quantity": 2
}
```

查看购物车：

```http
GET /api/cart?userId=1
```

### 订单模块

提交订单：

```http
POST /api/orders/checkout
Content-Type: application/json

{
  "userId": 1
}
```

查看订单详情：

```http
GET /api/orders/1?userId=1
```

### 智能药师咨询

提交咨询问题：

```http
POST /api/ai/pharmacist/consult
Content-Type: application/json; charset=utf-8

{
  "userId": 1,
  "question": "孕妇感冒可以吃布洛芬吗？"
}
```

返回字段：

- `answer`：用药建议。
- `riskLevel`：风险等级，`LOW`、`MEDIUM`、`HIGH`。
- `needHumanPharmacist`：是否需要人工药师。
- `disclaimer`：免责声明。

## 本地测试数据

启动时会通过 `src/main/resources/data.sql` 初始化 5 条药品数据，包含非处方药和处方药：

- Paracetamol Tablets
- Ibuprofen Capsules
- Amoxicillin Capsules
- Loratadine Tablets
- Metformin Tablets

## 注意事项

- 当前项目使用简单 `userId` 模拟登录用户，未实现完整登录鉴权。
- 第三方依赖只通过 `pom.xml` 配置管理，不需要提交依赖包本体。
- `target/` 是 Maven 构建产物，不需要手动维护，也不建议放入提交压缩包。
- 不要把真实 DeepSeek API Key 写入代码、配置文件或 README。
- 智能药师回答仅用于课程项目演示，不能替代医生诊断或执业药师判断。
