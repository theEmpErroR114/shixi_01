# 课程习题测验系统 — API 接口文档

> 每个接口标注了对应的后端 Controller 文件，方便快速定位源码。

---

## 目录

- [通用说明](#通用说明)
- [1. 认证模块（Auth）](#1-认证模块auth)
- [2. 公共接口](#2-公共接口)
- [3. 管理员接口（Admin）](#3-管理员接口admin)
- [4. 教师接口（Teacher）](#4-教师接口teacher)
- [5. 学生接口（Student）](#5-学生接口student)
- [附录：数据结构速查表](#附录数据结构速查表)

---

## 通用说明

### 请求规范

- **Base URL**: `http://localhost:8080`
- **所有 `/api/**` 路径（除登录）都需要登录**，请求必须携带 Cookie（Session）
- **前端 fetch 必须设置** `credentials: 'include'`
- 请求体/响应体均为 `application/json; charset=UTF-8`

### 统一响应格式

每个接口都返回以下 JSON 结构（代码定义于 `dto/Result.java`）：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 / 业务逻辑错误 |
| 401 | 未登录 |
| 403 | 角色权限不足 |
| 500 | 服务器内部错误 |

### 分页响应格式

分页接口的 `data` 字段结构（代码定义于 `dto/PageResult.java`）：

```json
{
  "list": [ ... ],
  "total": 50,
  "page": 1,
  "pageSize": 10
}
```

### 认证机制

来源：`config/WebMvcConfig.java`（拦截器注册）、`interceptor/LoginInterceptor.java`、`interceptor/RoleInterceptor.java`

| 路径前缀 | 允许的角色 |
|---------|-----------|
| `/api/auth/**` | 所有人（登录除外） |
| `/api/courses` | 所有已登录用户 |
| `/api/admin/**` | 仅 admin |
| `/api/teacher/**` | teacher（admin 也可，见代码） |
| `/api/student/**` | student（admin 也可，见代码） |

---

## 1. 认证模块（Auth）

> 源码：`controller/AuthController.java` → `service/impl/AuthServiceImpl.java`

### 1.1 登录

```
POST /api/auth/login
```

**请求体**（`dto/LoginRequest.java`）：

```json
{
  "role": "admin",
  "username": "admin",
  "password": "123456"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| role | String | 是 | `"admin"` / `"teacher"` / `"student"` |
| username | String | 是 | 登录账号 |
| password | String | 是 | 明文密码 |

**成功响应**（`dto/LoginUserVO.java`）：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "username": "admin",
    "realName": "系统管理员",
    "role": "admin"
  }
}
```

**错误码**：

| 场景 | code | message |
|------|------|---------|
| 账号或密码错误 | 400 | 账号或密码错误 |
| 教师/学生被禁用 | 400 | 该账号已被禁用，请联系管理员 |
| 角色类型无效 | 400 | 未知的角色类型 |

**Session 写入字段**（来源：`util/SessionUtil.java`）：

| Session Key | 值 |
|-------------|-----|
| `userId` | 用户ID（Long） |
| `role` | "admin"/"teacher"/"student" |
| `realName` | 用户姓名 |
| `username` | 登录账号 |

---

### 1.2 登出

```
POST /api/auth/logout
```

无请求体。销毁当前 Session。

**响应**：

```json
{"code": 200, "message": "success", "data": null}
```

---

### 1.3 获取当前用户

```
GET /api/auth/current-user
```

从 Session 读取当前登录用户信息，不查数据库。

**响应**（`dto/LoginUserVO.java`）：

```json
{
  "code": 200,
  "data": {
    "userId": 1,
    "username": "admin",
    "realName": "系统管理员",
    "role": "admin"
  }
}
```

未登录时返回：`{"code": 401, "message": "未登录", "data": null}`

---

### 1.4 修改密码

```
PUT /api/auth/change-password
```

**请求体**：

```json
{
  "oldPassword": "123456",
  "newPassword": "1234567"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldPassword | String | 是 | 原密码 |
| newPassword | String | 是 | 新密码，至少4位 |

**错误**：

| code | message |
|------|---------|
| 400 | 密码不能为空 |
| 400 | 新密码长度至少4位 |
| 400 | 原密码错误 |

---

## 2. 公共接口

### 2.1 获取全部课程列表

> 源码：`controller/CourseController.java`

```
GET /api/courses
```

无需参数。`data` 直接返回课程数组（非分页）。

**响应**：

```json
{
  "code": 200,
  "data": [
    {
      "courseId": 1,
      "courseName": "Java程序设计",
      "description": "面向对象编程与Java基础语法",
      "createTime": "2026-07-20T14:22:33"
    },
    {
      "courseId": 2,
      "courseName": "数据库原理",
      "description": "关系型数据库设计与SQL语言",
      "createTime": "2026-07-20T14:22:33"
    }
  ]
}
```

---

## 3. 管理员接口（Admin）

### 3.1 仪表盘统计

> 源码：`controller/admin/AdminDashboardController.java`

```
GET /api/admin/dashboard/stats
```

**响应**（`dto/DashboardStatsVO.java`）：

```json
{
  "code": 200,
  "data": {
    "teacherCount": 2,
    "activeTeacherCount": 2,
    "studentCount": 3,
    "activeStudentCount": 3
  }
}
```

---

### 3.2 教师管理

> 源码：`controller/admin/AdminTeacherController.java`

#### 3.2.1 教师列表

```
GET /api/admin/teachers
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 否 | 搜索关键字（姓名/用户名） |
| status | Integer | 否 | 1=启用 0=禁用，不传查全部 |
| page | Integer | 否 | 默认1 |
| pageSize | Integer | 否 | 默认10 |

**响应**：分页格式，`list` 中每个元素为 Teacher 对象

```json
{
  "teacherId": 1,
  "username": "teacher_wang",
  "realName": "王老师",
  "gender": "F",
  "phone": "13800000001",
  "subject": "Java程序设计",
  "status": 1,
  "createTime": "2026-07-20T14:22:33"
}
```

#### 3.2.2 教师详情

```
GET /api/admin/teachers/{id}
```

#### 3.2.3 新增教师

```
POST /api/admin/teachers
```

**请求体**（必填：username、password、realName）：

```json
{
  "username": "teacher_zhao",
  "password": "123456",
  "realName": "赵老师",
  "gender": "M",
  "phone": "13800000003",
  "subject": "计算机网络"
}
```

密码自动 BCrypt 加密存储。

#### 3.2.4 编辑教师

```
PUT /api/admin/teachers/{id}
```

请求体同新增（不含密码。密码修改见重置密码接口）。

#### 3.2.5 启用/禁用教师

```
PUT /api/admin/teachers/{id}/status
```

**请求体**：

```json
{"status": 0}
```

> 1=启用，0=禁用。禁用后该教师无法登录。

#### 3.2.6 重置教师密码

```
PUT /api/admin/teachers/{id}/reset-password
```

无请求体。密码重置为 `123456`（BCrypt 加密）。

#### 3.2.7 获取教师的课程

```
GET /api/admin/teachers/{id}/courses
```

`data` 直接返回 Course 数组。

#### 3.2.8 为教师分配课程

```
PUT /api/admin/teachers/{id}/courses
```

**请求体**：

```json
{"courseIds": [1, 2]}
```

> 会先清空原有分配，再按传入的 courseIds 重新分配。

#### 3.2.9 删除教师

```
DELETE /api/admin/teachers/{id}
```

> 删除后：`t_teacher_course` 关联记录删除，该教师创建的题目和试卷保留（`teacher_id` 无 FK 约束）。

---

### 3.3 学生管理

> 源码：`controller/admin/AdminStudentController.java`

#### 3.3.1 学生列表

```
GET /api/admin/students
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 否 | 搜索关键字 |
| className | String | 否 | 班级筛选 |
| status | Integer | 否 | 1=启用 0=禁用 |
| page | Integer | 否 | 默认1 |
| pageSize | Integer | 否 | 默认10 |

**响应**：分页格式

```json
{
  "studentId": 1,
  "username": "stu_zhang",
  "realName": "张三",
  "gender": "M",
  "className": "软件2101班",
  "phone": "13900000001",
  "status": 1,
  "createTime": "2026-07-20T14:22:33"
}
```

#### 3.3.2 学生详情

```
GET /api/admin/students/{id}
```

#### 3.3.3 新增学生

```
POST /api/admin/students
```

**请求体**：

```json
{
  "username": "stu_wang",
  "password": "123456",
  "realName": "王五",
  "gender": "M",
  "className": "软件2101班",
  "phone": "13900000004"
}
```

#### 3.3.4 编辑学生

```
PUT /api/admin/students/{id}
```

#### 3.3.5 启用/禁用学生

```
PUT /api/admin/students/{id}/status
```

**请求体**：`{"status": 0}`

#### 3.3.6 重置学生密码

```
PUT /api/admin/students/{id}/reset-password
```

密码重置为 `123456`。

#### 3.3.7 获取学生的课程

```
GET /api/admin/students/{id}/courses
```

#### 3.3.8 为学生分配课程

```
PUT /api/admin/students/{id}/courses
```

**请求体**：`{"courseIds": [1, 2]}`

#### 3.3.9 删除学生

```
DELETE /api/admin/students/{id}
```

> 删除后：`t_student_course` 关联删除，练习/考试记录保留（`student_id` 设为 NULL，`ON DELETE SET NULL`）。

---

### 3.4 课程管理

> 源码：`controller/admin/AdminCourseController.java` + `service/impl/PaperServiceImpl.java` + `service/impl/QuestionServiceImpl.java`

#### 3.4.1 课程列表（分页）

```
GET /api/admin/courses
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 否 | 课程名搜索 |
| page | Integer | 否 | 默认1 |
| pageSize | Integer | 否 | 默认10 |

#### 3.4.2 课程详情

```
GET /api/admin/courses/{id}
```

#### 3.4.3 新增课程

```
POST /api/admin/courses
```

**请求体**：

```json
{"courseName": "操作系统", "description": "操作系统原理"}
```

#### 3.4.4 编辑课程

```
PUT /api/admin/courses/{id}
```

#### 3.4.5 删除课程

```
DELETE /api/admin/courses/{id}
```

> 有题目或试卷的课程不能删除，返回 400 错误。

#### 3.4.6 查看课程下的题目

```
GET /api/admin/courses/{courseId}/questions
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| questionType | Integer | 否 | 1单选/2多选/3判断/4填空/5简答 |
| difficulty | Integer | 否 | 1易/2中/3难 |
| keyword | String | 否 | 题干关键词搜索 |
| page | Integer | 否 | 默认1 |
| pageSize | Integer | 否 | 默认10 |

**响应**：分页格式，`list` 中每个 Question 对象包含题目信息（不含 `options` 详情和 `answer`/`analysis`，仅列表展示）。

#### 3.4.7 查看课程下的试卷

```
GET /api/admin/courses/{courseId}/papers
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | 否 | 0未发布/1已发布/2已回收 |
| page | Integer | 否 | 默认1 |
| pageSize | Integer | 否 | 默认10 |

**响应**：分页格式，`list` 中每个 Paper 对象含试卷名、总分、时长、日期、状态、创建时间等。

---

## 4. 教师接口（Teacher）

### 4.1 教师课程

> 源码：`controller/teacher/TeacherCourseController.java`

```
GET /api/teacher/courses
```

返回当前教师被分配的所有课程。`data` 直接为 Course 数组（非分页）。

---

### 4.2 仪表盘统计

> 源码：`controller/teacher/TeacherDashboardController.java`

```
GET /api/teacher/dashboard/stats
```

**响应**（`dto/DashboardStatsVO.java`）：

```json
{
  "code": 200,
  "data": {
    "questionCount": 50,
    "paperCount": 10,
    "draftPaperCount": 5,
    "publishedPaperCount": 5,
    "activeExamCount": 5,
    "studentCount": 30
  }
}
```

> 所有统计数据按教师关联课程（`t_teacher_course`）过滤。

---

### 4.3 题库管理

> 源码：`controller/teacher/TeacherQuestionController.java` → `service/impl/QuestionServiceImpl.java`

#### 4.3.1 题目列表

```
GET /api/teacher/questions
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| courseId | Long | 否 | 课程筛选，不传则查全部已分配课程 |
| questionType | Integer | 否 | 1~5 |
| difficulty | Integer | 否 | 1~3 |
| keyword | String | 否 | 题干搜索 |
| page | Integer | 否 | 默认1 |
| pageSize | Integer | 否 | 默认10 |

> 权限：自动按当前教师的关联课程ID过滤。

**响应**：分页格式，每项含 `questionId`、`content`、`questionType`、`difficulty`、`courseName`、`teacherName`、`createTime`。

#### 4.3.2 题目详情（含选项）

```
GET /api/teacher/questions/{id}
```

`data` 为单个 Question 对象，`options` 数组包含所有选项。

#### 4.3.3 新增题目

```
POST /api/teacher/questions
```

**请求体**（`dto/QuestionDTO.java`）：

```json
{
  "courseId": 1,
  "questionType": 1,
  "content": "Java中定义一个类使用哪个关键字？",
  "answer": "A",
  "analysis": "class 是Java中定义类的关键字。",
  "difficulty": 1,
  "options": [
    {"optionLabel": "A", "optionContent": "class", "isCorrect": 1},
    {"optionLabel": "B", "optionContent": "struct", "isCorrect": 0},
    {"optionLabel": "C", "optionContent": "define", "isCorrect": 0},
    {"optionLabel": "D", "optionContent": "object", "isCorrect": 0}
  ]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| courseId | Long | 是 | 所属课程（必须在教师关联课程内） |
| questionType | Integer | 是 | 1单选/2多选/3判断/4填空/5简答 |
| content | String | 是 | 题干 |
| answer | String | 否 | 答案（单选"A"，多选"A,C"；判断/填空/简答按内容） |
| analysis | String | 否 | 解析 |
| difficulty | Integer | 否 | 默认1 |
| options | Array | type=1/2时必填 | 选项数组 |

**校验规则**：
- 单选(type=1)/多选(type=2) 至少2个选项，选项内容非空
- 多选题(type=2) 至少2个选项标为正确（`isCorrect=1`）
- `courseId` 必须在教师关联课程中，否则返回 400

**自动处理**：单选题/多选题答案可从 `isCorrect=1` 的选项自动推导，前端 `teacher_questions.html` 有此逻辑。

#### 4.3.4 编辑题目

```
PUT /api/teacher/questions/{id}
```

请求体同新增。**更新时先删旧选项再插入新选项**（`QuestionServiceImpl.updateQuestion()` 中 `questionOptionMapper.deleteByQuestionId()` 后 `batchInsert()`）。

#### 4.3.5 删除题目

```
DELETE /api/teacher/questions/{id}
```

> 内部按顺序删除关联表：`t_paper_question` → `t_practice_record` → `t_exam_answer` → `t_question_option` → `t_question`。整个过程在 `@Transactional` 中执行（源码：`QuestionServiceImpl.deleteQuestion()` 第149-158行）。

---

### 4.4 试卷管理

> 源码：`controller/teacher/TeacherPaperController.java` → `service/impl/PaperServiceImpl.java`

#### 4.4.1 试卷列表

```
GET /api/teacher/papers
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | 否 | 0未发布/1已发布/2已回收 |
| courseId | Long | 否 | 课程筛选 |
| page | Integer | 否 | 默认1 |
| pageSize | Integer | 否 | 默认10 |

> 权限：按教师关联课程过滤。

**响应**：分页格式，每项含 `paperName`、`totalScore`、`duration`、`status`、`startDate`、`endDate`、`courseName`、`teacherName`、`createTime`。

#### 4.4.2 试卷详情（含题目列表）

```
GET /api/teacher/papers/{id}
```

`data` 为 Paper 对象，`questions` 数组含每题 `questionId`、`score`、`sortOrder`。

#### 4.4.3 新建试卷

```
POST /api/teacher/papers
```

**请求体**（`dto/PaperCreateRequest.java`）：

```json
{
  "paperName": "Java第一章测验",
  "courseId": 1,
  "duration": 45,
  "totalScore": 100,
  "startDate": "2026-08-01T09:00:00",
  "endDate": "2026-08-07T23:59:00",
  "questions": [
    {"questionId": 1, "score": 10, "sortOrder": 1},
    {"questionId": 2, "score": 15, "sortOrder": 2}
  ]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| paperName | String | 是 | 试卷名称 |
| courseId | Long | 是 | 所属课程 |
| duration | Integer | 否 | 考试时长（分钟），默认60 |
| totalScore | Integer | 否 | 总分，默认100 |
| startDate | String | 否 | 开始时间，格式 `yyyy-MM-dd'T'HH:mm:ss` |
| endDate | String | 否 | 截止时间，格式 `yyyy-MM-dd'T'HH:mm:ss` |
| questions | Array | 否 | 题目列表，每项含：questionId、score、sortOrder |

> **注意**：创建试卷时如果传了 `questions`，会一并写入 `t_paper_question`；编辑时**不要**传 `questions`，否则会覆盖已有（编辑题目用单独逻辑）。

**新建后状态为 0（未发布）**。

#### 4.4.4 编辑试卷

```
PUT /api/teacher/papers/{id}
```

> 仅 未发布(status=0) 的试卷可编辑。编辑不包括题目修改，只改试卷基本信息和日期。

#### 4.4.5 发布试卷

```
PUT /api/teacher/papers/{id}/publish
```

> 仅 未发布(status=0) 可发布。发布后学生可见。

#### 4.4.6 回收试卷

```
PUT /api/teacher/papers/{id}/recall
```

> 仅 已发布(status=1) 可回收。回收后学生不可参加。

#### 4.4.7 删除试卷

```
DELETE /api/teacher/papers/{id}
```

> 已发布(status=1) 的试卷不能删除。删除时同时清理 `t_paper_question` 关联。

---

### 4.5 学生成绩查看

> 源码：`controller/teacher/TeacherStudentController.java`

#### 4.5.1 学生成绩统计列表

```
GET /api/teacher/students/stats
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 否 | 学生姓名搜索 |
| page | Integer | 否 | 默认1 |
| pageSize | Integer | 否 | 默认10 |

> 只返回选了该教师课程的学生。

**响应**（分页，`dto/StudentStatsVO.java`）：

```json
{
  "studentId": 1,
  "realName": "张三",
  "className": "软件2101班",
  "practiceCount": 50,
  "practiceCorrectRate": 0.75,
  "examCount": 3,
  "examAvgScore": 82.5
}
```

#### 4.5.2 学生详情（含考试成绩明细）

```
GET /api/teacher/students/{studentId}/detail
```

**响应**（`dto/StudentDetailVO.java`）：

```json
{
  "studentId": 1,
  "realName": "张三",
  "className": "软件2101班",
  "gender": "M",
  "phone": "13900000001",
  "practiceCount": 50,
  "practiceCorrectRate": 0.75,
  "examCount": 3,
  "examAvgScore": 82.5,
  "examScores": [
    {"paperName": "Java第一章测验", "score": 85.0, "totalScore": 100, "submitTime": "2026-07-25T10:30:00"}
  ]
}
```

#### 4.5.3 某试卷的成绩列表

```
GET /api/teacher/papers/{paperId}/scores
```

**响应**：`data` 为数组，每项含学生信息和得分。

---

## 5. 学生接口（Student）

### 5.1 学生课程

> 源码：`controller/student/StudentCourseController.java`

```
GET /api/student/courses
```

返回当前学生的已选课程。`data` 直接为 Course 数组。

---

### 5.2 仪表盘

> 源码：`controller/student/StudentDashboardController.java`

#### 5.2.1 统计

```
GET /api/student/dashboard/stats
```

**响应**：

```json
{
  "code": 200,
  "data": {
    "practiceCount": 50,
    "examCount": 3
  }
}
```

#### 5.2.2 即将开始的考试

```
GET /api/student/dashboard/upcoming-exams
```

> 条件：已发布 + startDate 在未来 + 学生未考过。按 startDate 升序。

#### 5.2.3 最近练习

```
GET /api/student/dashboard/recent-practice
```

返回最近5条练习记录。

---

### 5.3 练习

> 源码：`controller/student/StudentPracticeController.java` → `service/impl/PracticeServiceImpl.java`

#### 5.3.1 随机生成练习题目

```
POST /api/student/practice/generate
```

**请求体**（`dto/PracticeConfigRequest.java`）：

```json
{
  "courseId": 1,
  "questionType": 1,
  "difficulty": 2,
  "count": 10
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| courseId | Long | 否 | 不传则从所有已选课程随机 |
| questionType | Integer | 否 | 1~5，不传则不限 |
| difficulty | Integer | 否 | 1~3，不传则不限 |
| count | Integer | 否 | 默认10 |

> 权限：`courseId` 必须在学生已选课程中。**返回的题目不含 `answer` 和 `analysis`**（防止作弊）。

**SQL 随机方式**（源码：`mapper/QuestionMapper.xml` 的 `findRandom`）：`ORDER BY RAND() LIMIT #{count}`。

**判断题自动补选项**（源码：`PracticeServiceImpl.generatePractice()` 第49-60行）：判断题(type=3)若数据库无选项，自动生成 "对"/"错" 两个选项。

**响应**：`data` 为 Question 数组（不含答案和解析）。

#### 5.3.2 提交单题答案

```
POST /api/student/practice/submit
```

**请求体**（`dto/PracticeSubmitRequest.java`）：

```json
{
  "questionId": 1,
  "studentAnswer": "A"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| questionId | Long | 题目ID |
| studentAnswer | String | 学生答案：单选"A"，多选"A,C"，判断"对"/"错"，填空/简答为文本 |

**响应**（`dto/PracticeResultVO.java`）：

```json
{
  "code": 200,
  "data": {
    "questionId": 1,
    "content": "Java中定义一个类使用以下哪个关键字？",
    "studentAnswer": "A",
    "correctAnswer": "A",
    "isCorrect": true,
    "analysis": "class 是Java中定义类的关键字。",
    "options": [
      {"optionLabel": "A", "optionContent": "class"},
      {"optionLabel": "B", "optionContent": "struct"},
      {"optionLabel": "C", "optionContent": "define"},
      {"optionLabel": "D", "optionContent": "object"}
    ]
  }
}
```

> **判分逻辑**（源码：`PracticeServiceImpl.checkAnswer()`）：
> - 单选/判断：`equalsIgnoreCase` 比较
> - 多选：两端排序后比较（"B,A" ↔ "A,B"）
> - 填空/简答：精确匹配

#### 5.3.3 练习历史

```
GET /api/student/practice/history
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| courseId | Long | 否 | 课程筛选 |
| page | Integer | 否 | 默认1 |
| pageSize | Integer | 否 | 默认20 |

---

### 5.4 考试

> 源码：`controller/student/StudentExamController.java` → `service/impl/ExamServiceImpl.java`

#### 5.4.1 可用考试列表

```
GET /api/student/exams/available
```

> 返回条件：已发布 + 日期有效期内 + 学生未考过 + 学生已选课程。

#### 5.4.2 开始考试

```
POST /api/student/exams/{paperId}/start
```

> 验证：试卷已发布、日期范围有效、学生未考过。成功后创建 `ExamRecord`（status=0 进行中）。

**响应**：

```json
{"code": 200, "data": {"examRecordId": 1}}
```

**错误**：

| message |
|---------|
| 该试卷不可作答 |
| 该考试尚未开始 |
| 该考试已截止 |
| 你已完成该测验 |

#### 5.4.3 获取考试题目

```
GET /api/student/exams/{examRecordId}/detail
```

> 验证：`examRecordId` 归属当前学生、状态为进行中(0)。**返回的题目不含 `answer` 和 `analysis`**。

#### 5.4.4 提交考试

```
POST /api/student/exams/{examRecordId}/submit
```

**请求体**（`dto/ExamSubmitRequest.java`）：

```json
{
  "answers": [
    {"questionId": 1, "studentAnswer": "A"},
    {"questionId": 2, "studentAnswer": "对"},
    {"questionId": 3, "studentAnswer": "A,C"}
  ]
}
```

> 未作答的题目 `studentAnswer` 为空字符串 `""`。

**响应**（`dto/ExamResultVO.java`）：

```json
{
  "code": 200,
  "data": {
    "examRecordId": 1,
    "paperName": "Java第一章测验",
    "courseName": "Java程序设计",
    "totalScore": 85.0,
    "paperTotalScore": 100,
    "usedMinutes": 23,
    "questionCount": 10,
    "correctCount": 8,
    "answers": [
      {
        "questionId": 1,
        "content": "Java中定义一个类使用以下哪个关键字？",
        "questionType": 1,
        "studentAnswer": "A",
        "correctAnswer": "A",
        "analysis": "class 是Java中定义类的关键字。",
        "isCorrect": 1,
        "score": 10.0,
        "options": [ ... ]
      }
    ]
  }
}
```

> **判分**：每题按 `t_paper_question.score` 计分。总分由后端累加，不信任前端传值。

#### 5.4.5 查看考试结果

```
GET /api/student/exams/{examRecordId}/result
```

> 仅已交卷(status=1)可查。响应结构同提交。

---

### 5.5 成绩记录

> 源码：`controller/student/StudentResultController.java`

#### 5.5.1 考试历史列表

```
GET /api/student/results/exams
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 默认1 |
| pageSize | Integer | 否 | 默认10 |

**响应**：分页格式，每项含 `examRecordId`、`paperName`、`courseName`、`totalScore`、`startTime`、`submitTime`、`status`。

#### 5.5.2 考试结果详情

```
GET /api/student/results/exams/{examRecordId}
```

同 5.4.5。

---

## 附录：数据结构速查表

### 实体主键速查

| 实体 | 主键字段 | Java类 | 表名 |
|------|---------|--------|------|
| Admin | `adminId` | `entity/Admin.java` | `t_admin` |
| Teacher | `teacherId` | `entity/Teacher.java` | `t_teacher` |
| Student | `studentId` | `entity/Student.java` | `t_student` |
| Course | `courseId` | `entity/Course.java` | `t_course` |
| Question | `questionId` | `entity/Question.java` | `t_question` |
| QuestionOption | `optionId` | `entity/QuestionOption.java` | `t_question_option` |
| Paper | `paperId` | `entity/Paper.java` | `t_paper` |
| PaperQuestion | `id` | `entity/PaperQuestion.java` | `t_paper_question` |
| PracticeRecord | `recordId` | `entity/PracticeRecord.java` | `t_practice_record` |
| ExamRecord | `examRecordId` | `entity/ExamRecord.java` | `t_exam_record` |
| ExamAnswer | `answerId` | `entity/ExamAnswer.java` | `t_exam_answer` |
| TeacherCourse | `id` | `entity/TeacherCourse.java` | `t_teacher_course` |
| StudentCourse | `id` | `entity/StudentCourse.java` | `t_student_course` |

> ⚠️ 前端绝不能使用 `.id` 取主键，必须用各实体自己的主键名。

### 枚举常量

| 枚举类 | 取值 |
|--------|------|
| `enums/QuestionTypeEnum.java` | 1=单选, 2=多选, 3=判断, 4=填空, 5=简答 |
| `enums/DifficultyEnum.java` | 1=易, 2=中, 3=难 |
| `enums/PaperStatusEnum.java` | 0=未发布, 1=已发布, 2=已回收 |
| `enums/RoleEnum.java` | admin, teacher, student |

### Option 字段说明

`t_question_option` 表（`entity/QuestionOption.java`）：

| 字段 | 类型 | 说明 |
|------|------|------|
| optionLabel | String | A/B/C/D |
| optionContent | String | 选项文字内容 |
| isCorrect | Integer | 1=正确答案, 0=错误答案 |

> ⚠️ `isCorrect` 是 Integer（1/0），不是 Boolean。前端发送时必须传数字。

### 数据库外键策略

| 关系 | 策略 |
|------|------|
| `t_student.create_by` → `t_admin` | FK |
| `t_teacher.create_by` → `t_admin` | FK |
| `t_question.course_id` → `t_course` | FK (RESTRICT) |
| `t_question.teacher_id` | **无FK** |
| `t_paper.course_id` → `t_course` | FK (RESTRICT) |
| `t_paper.teacher_id` | **无FK** |
| `t_paper_question.paper_id` → `t_paper` | FK |
| `t_paper_question.question_id` → `t_question` | FK (RESTRICT) |
| `t_question_option.question_id` → `t_question` | FK |
| `t_practice_record.student_id` → `t_student` | FK `ON DELETE SET NULL` |
| `t_exam_record.student_id` → `t_student` | FK `ON DELETE SET NULL` |
| `t_teacher_course.teacher_id/course_id` | FK |
| `t_student_course.student_id/course_id` | FK |

### 接口完整一览

| 模块 | 方法 | URL | 权限 |
|------|------|-----|------|
| Auth | POST | `/api/auth/login` | 公开 |
| Auth | POST | `/api/auth/logout` | 登录 |
| Auth | GET | `/api/auth/current-user` | 登录 |
| Auth | PUT | `/api/auth/change-password` | 登录 |
| 公共 | GET | `/api/courses` | 登录 |
| Admin | GET | `/api/admin/dashboard/stats` | admin |
| Admin | GET/POST | `/api/admin/teachers` | admin |
| Admin | GET/PUT/DELETE | `/api/admin/teachers/{id}` | admin |
| Admin | PUT | `/api/admin/teachers/{id}/status` | admin |
| Admin | PUT | `/api/admin/teachers/{id}/reset-password` | admin |
| Admin | GET/PUT | `/api/admin/teachers/{id}/courses` | admin |
| Admin | GET/POST | `/api/admin/students` | admin |
| Admin | GET/PUT/DELETE | `/api/admin/students/{id}` | admin |
| Admin | PUT | `/api/admin/students/{id}/status` | admin |
| Admin | PUT | `/api/admin/students/{id}/reset-password` | admin |
| Admin | GET/PUT | `/api/admin/students/{id}/courses` | admin |
| Admin | GET/POST | `/api/admin/courses` | admin |
| Admin | GET/PUT/DELETE | `/api/admin/courses/{id}` | admin |
| Admin | GET | `/api/admin/courses/{id}/questions` | admin |
| Admin | GET | `/api/admin/courses/{id}/papers` | admin |
| Teacher | GET | `/api/teacher/courses` | teacher |
| Teacher | GET | `/api/teacher/dashboard/stats` | teacher |
| Teacher | GET/POST | `/api/teacher/questions` | teacher |
| Teacher | GET/PUT/DELETE | `/api/teacher/questions/{id}` | teacher |
| Teacher | GET/POST | `/api/teacher/papers` | teacher |
| Teacher | GET/PUT/DELETE | `/api/teacher/papers/{id}` | teacher |
| Teacher | PUT | `/api/teacher/papers/{id}/publish` | teacher |
| Teacher | PUT | `/api/teacher/papers/{id}/recall` | teacher |
| Teacher | GET | `/api/teacher/students/stats` | teacher |
| Teacher | GET | `/api/teacher/students/{id}/detail` | teacher |
| Teacher | GET | `/api/teacher/papers/{id}/scores` | teacher |
| Student | GET | `/api/student/courses` | student |
| Student | GET | `/api/student/dashboard/stats` | student |
| Student | GET | `/api/student/dashboard/upcoming-exams` | student |
| Student | GET | `/api/student/dashboard/recent-practice` | student |
| Student | POST | `/api/student/practice/generate` | student |
| Student | POST | `/api/student/practice/submit` | student |
| Student | GET | `/api/student/practice/history` | student |
| Student | GET | `/api/student/exams/available` | student |
| Student | POST | `/api/student/exams/{id}/start` | student |
| Student | GET | `/api/student/exams/{id}/detail` | student |
| Student | POST | `/api/student/exams/{id}/submit` | student |
| Student | GET | `/api/student/exams/{id}/result` | student |
| Student | GET | `/api/student/results/exams` | student |
| Student | GET | `/api/student/results/exams/{id}` | student |

**总计：47个接口**
