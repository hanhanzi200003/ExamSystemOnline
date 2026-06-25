# 在线考试系统

Spring Boot 3 构建的在线考试平台，支持教师出题组卷、学生在线考试、客观题自动判分与主观题人工阅卷，采用 Redis 缓存提升考试会话性能。

---

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 运行环境 |
| Spring Boot | 3.3.5 | 应用框架 |
| Spring Security | 6.3.4 | 认证授权 (JWT + RBAC) |
| Spring Data JPA | 3.3.5 | ORM / 数据持久层 |
| MySQL | 8.0.33 | 关系型数据库 |
| Redis | 7.2.4 | 考试会话答案缓存 |
| jjwt | 0.11.5 | 无状态 JWT 令牌认证 |
| Apache POI | 5.2.3 | Excel 成绩导出 |
| SpringDoc OpenAPI | 2.3.0 | Swagger 接口文档自动生成 |
| HikariCP | 5.1.0 | 数据库连接池 |
| Lombok | 1.18.34 | 编译期代码生成 |
| Fastjson2 | 2.0.48 | JSON 序列化与反序列化 |
| EasyCaptcha | 1.6.2 | 登录图形验证码生成 |
| Commons Lang3 | 3.14.0 | 通用工具类库 |

### 前端

纯 HTML / CSS / JavaScript 单页应用，使用 Remix Icon 图标库，CDN 引入，无构建工具。

---

## 系统架构

```text
+-------+  JWT Token   +----------------------------------+
| 前端  | -----------> |  Spring Security Filter Chain    |
+-------+              +----------------------------------+
                              |
         +--------------------+----------------------+
         |                    |                      |
+--------v--------+  +--------v--------+  +---------v---------+
|  AuthController |  |  REST API 接口  |  | 静态资源 (HTML)   |
|  /api/auth/*    |  |  权限控制注解   |  | /exam.html 等     |
+-----------------+  +-----------------+  +-------------------+
                              |
         +--------------------+----------------------+
         |           |            |           |
+--------v--+  +-----v-----+  +--v----+  +---v------+
| login     |  | examgroup  |  | exam  |  | exam     |
| 用户模块  |  | 分组模块   |  | manage|  | question |
+-----------+  +-----------+  | 考试  |  | bank     |
                              | 管理  |  | 题库模块 |
+----------+  +-----------+  +-------+  +----------+
| exam     |  | exam      |  | exam  |
| paper    |  | session   |  | score |
| 组卷模块 |  | 考试会话  |  | 成绩   |
+----------+  +-----------+  | 阅卷   |
                             +--------+

         +--------------------+----------------------+
         |                    |                      |
+--------v--------+  +--------v--------+  +---------v---------+
|     MySQL       |  |     Redis       |  |  JPA / Hibernate  |
|  exam_system DB |  |  答案缓存       |  |  ddl-auto: update  |
+-----------------+  +-----------------+  +-------------------+
```

## 功能模块

### 1. 用户与认证 (`login`)

- 支持邮箱 / 手机号注册，BCrypt 密码加密
- 双角色体系：TEACHER（教师）与 STUDENT（学生）
- JWT 无状态认证，学生令牌 7 天有效，教师令牌 30 天有效
- 基于 Spring Security 的 RBAC 权限控制，URL 级别 + 方法级 `@PreAuthorize`
- 账号注销（教师/学生分别级联清理关联数据）
- 图形验证码（EasyCaptcha）

### 2. 题库管理 (`examquestionbank`)

- 题目 CRUD，每道题归属创建者（教师）
- 五种题型：单选题、多选题、判断题、填空题、简答题
- 每道题包含题干、选项、标准答案、解析
- 支持按题型筛选

### 3. 组卷模块 (`exampaper`)

- 手动组卷：教师从题库中选题并指定每题分值
- 自动组卷：系统根据题型/数量/分值条件自动生成试卷
- 试卷包含题目副本（PaperQuestion / PaperOption / PaperAnswer），与题库解耦
- 自动计算试卷总分

### 4. 考试管理 (`exammanage`)

- 创建考试：绑定试卷、设置考试时间窗口（最早开始时间 / 最晚开始时间）、考试时长
- 考试分配给班级/组，学生仅可见所属班级的考试
- 教师管理自己创建的考试

### 5. 考试会话 (`examsession`) -- 核心模块

- 考试开始后为每位学生生成独立会话 (ExamSession)，包含 UUID 会话令牌
- 试卷题目与选项复制到会话表 (ExamSessionQuestion)，与原始试卷隔离
- 答案缓存策略：
  - 考试过程中，答案实时写入 Redis（`exam:answer:{token}` 键），2 小时 TTL
  - 提交时批量从 Redis 读取并持久化到 MySQL
- 支持断线恢复：通过 sessionToken 恢复考试状态
- 考试超时自动提交：`@Scheduled(fixedRate = 60000)` 每分钟轮询，超时会话自动交卷
- 服务分层设计，Facade 模式集成多个子服务：
  - `ExamSessionLifecycleService` -- 会话生命周期
  - `ExamPaperCopyService` -- 试卷复制
  - `AnswerInitializationService` -- 答案初始化
  - `RedisAnswerCacheService` -- Redis 缓存
  - `DatabaseAnswerPersistService` -- 数据库持久化

### 6. 成绩与阅卷 (`examscore`)

- 客观题自动判分：单选、多选、判断、填空由系统自动比对标准答案
- 主观题人工阅卷：教师对简答题逐一打分
- 教师端：查看待批阅学生列表，逐题/逐人批阅
- 学生端：查看已批阅试卷及每道题的得分与解析
- 成绩统计：总分、各题得分详情
- 成绩 Excel 导出（Apache POI）

### 7. 班级分组 (`examgroup`)

- 教师创建班级，自动生成 6 位班级码
- 学生通过班级码加入班级
- 成员管理、班级转让、解散班级

---

## 数据库设计

核心实体关系：

- **User** -- 用户表（教师/学生），含 username、password、role、email、phone 等
- **Question / Option / Answer** -- 题库表（题目、选项、标准答案）
- **Paper / PaperQuestion / PaperOption / PaperAnswer** -- 试卷表（试卷题目快照）
- **Exam** -- 考试表，关联 Paper 和 Group
- **Group / GroupMember** -- 班级表
- **ExamSession / ExamSessionQuestion / ExamSessionAnswer** -- 考试会话表（考生作答记录）
- **ExamRecord / ExamScore / ExamScoreDetail** -- 成绩表（总分与明细）

---

## 项目结构

```text
src/main/java/com/example/exam_system/
├── ExamSystemApplication.java          # 启动类，启用定时任务
├── login/                              # 用户认证模块
│   ├── config/                         # SecurityConfig, JwtFilter, RedisConfig, WebConfig
│   ├── controller/                     # AuthController, UserController, TeacherUserController
│   ├── service/                        # AuthService, UserService, TokenService, PasswordService
│   ├── entity/                         # User
│   ├── repository/                     # UserRepository
│   ├── dto/                            # LoginRequest, LoginResponse, CreateUser, UpdateUser
│   ├── utils/                          # JwtUtil, SecurityValidator
│   └── exception/                      # GlobalExceptionHandler
├── examquestionbank/                   # 题库模块
│   ├── controller/                     # QuestionBankController
│   ├── service/                        # QuestionBankService
│   ├── entity/                         # Question, Option, Answer
│   ├── enums/                          # QuestionType (SINGLE_CHOICE, MULTIPLE_CHOICE 等)
│   └── dto/                            # QuestionCreateDTO, QuestionDTO
├── exampaper/                          # 组卷模块
│   ├── controller/                     # PaperController
│   ├── service/                        # PaperService
│   ├── entity/                         # Paper, PaperQuestion, PaperOption, PaperAnswer
│   ├── enums/                          # PaperType (MANUAL, AUTO)
│   └── dto/                            # ManualPaperCreateDTO, AutoPaperCreateDTO, PaperDetailDTO
├── exammanage/                         # 考试管理模块
│   ├── controller/                     # ExamController
│   ├── service/                        # ExamService
│   ├── entity/                         # Exam
│   └── dto/                            # ExamCreateManualDTO, ExamDetailDTO, ExamListDTO
├── examsession/                        # 考试会话模块
│   ├── controller/                     # ExamSessionController
│   ├── service/                        # ExamSessionFacadeService, RedisAnswerCacheService,
│   │                                   # ExamSessionLifecycleService, ExamPaperCopyService,
│   │                                   # AnswerInitializationService, DatabaseAnswerPersistService
│   ├── scheduler/                      # ExamTimeoutScheduler (定时自动交卷)
│   ├── entity/                         # ExamSession, ExamSessionQuestion, ExamSessionAnswer
│   └── dto/                            # ExamSessionDTO, AnswerSubmitDTO, ExamAnswerCache
├── examscore/                          # 成绩与阅卷模块
│   ├── controller/                     # ExamScoreController, ExamRecordController
│   ├── service/                        # ExamScoreService, ExamRecordService, ExcelExportService
│   ├── entity/                         # ExamRecord, ExamScore, ExamScoreDetail
│   └── dto/                            # ScoreResultDTO, GradingQuestionDTO, ManualGradeDTO
└── examgroup/                          # 班级分组模块
    ├── controller/                     # GroupController
    ├── service/                        # GroupService
    ├── entity/                         # Group, GroupMember
    ├── mapper/                         # GroupMapper (DTO 转换)
    └── dto/                            # GroupCreateDTO, GroupDTO, GroupDetailDTO, JoinGroupDTO
```

---

## 快速开始

### 环境要求

- JDK 17.0.8
- Maven 3.9.5
- MySQL 8.0.33
- Redis 7.2.4

### 数据库初始化

```sql
CREATE DATABASE exam_system DEFAULT CHARACTER SET utf8mb4;
CREATE USER 'exam_user'@'%' IDENTIFIED BY '123';
GRANT ALL PRIVILEGES ON exam_system.* TO 'exam_user'@'%';
FLUSH PRIVILEGES;
```

项目启动后 JPA 会自动建表（`ddl-auto: update`）。

### 配置修改

编辑 `src/main/resources/application.properties`，修改数据库与 Redis 连接信息：

```properties
spring.datasource.url=jdbc:mysql://{YOUR_HOST}:3306/exam_system?...
spring.datasource.username=exam_user
spring.datasource.password=123

spring.data.redis.host={YOUR_REDIS_HOST}
spring.data.redis.port=6379
spring.data.redis.password={YOUR_REDIS_PASSWORD}
```

### 构建与运行

```bash
# 编译
mvn clean package -DskipTests

# 运行
java -jar target/exam-system-0.0.1-SNAPSHOT.jar

# 或直接使用 Maven 插件
mvn spring-boot:run
```

启动后访问：
- 学生端：`http://localhost:8080/exam_student.html`
- 教师端：`http://localhost:8080/exam_teacher.html`
- Swagger 文档：`http://localhost:8080/swagger-ui.html`

---

## 接口文档

项目集成 SpringDoc OpenAPI，启动后访问 `/swagger-ui.html` 查看完整 REST API 文档。

核心接口一览：

| 模块 | 路径前缀 | 权限 |
|------|----------|------|
| 认证 | `/api/auth/**` | 公开 |
| 用户管理 | `/api/users/**` | TEACHER |
| 题库 | `/api/question-bank/**` | TEACHER |
| 试卷 | `/api/papers/**` | TEACHER |
| 考试管理 | `/api/exams/teacher/**` | TEACHER |
| 考试管理 | `/api/exams/student/**` | STUDENT |
| 考试会话 | `/api/exam-session/**` | 已认证 |
| 成绩阅卷 | `/api/exam-scores/**` | TEACHER |
| 成绩查询 | `/api/exam-records/**` | 已认证 |
| 班级管理 | `/api/groups/**` | TEACHER / STUDENT |

---

## 关键设计决策

1. **试卷题目快照机制**：组卷时将题库题目复制到 PaperQuestion 等表，即使题库后续修改也不影响已发布的试卷。
2. **考试会话题目隔离**：考生开始考试时再复制一份到 ExamSessionQuestion，防止不同考生之间互相干扰。
3. **Redis 答案缓存**：考试中高频写操作先落 Redis，提交时批量刷入 MySQL，降低数据库压力，同时支持考试恢复。
4. **定时自动交卷**：`@Scheduled` 每分钟扫描超时会话，自动触发提交流程，确保考试公平。
5. **Facade 服务模式**：ExamSession 模块拆分为 6 个子服务，Facade 统一编排，职责清晰。
6. **无状态架构**：`SessionCreationPolicy.STATELESS`，JWT 令牌承载身份信息，水平扩展友好。
