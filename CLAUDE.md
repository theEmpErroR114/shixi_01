# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

课程习题测验系统 (Course Exercise Quiz System) — a Spring Boot + MyBatis + MySQL web application with three user roles: Admin, Teacher, Student. The frontend is 13 static HTML pages served from the same Spring Boot server (port 8080):
- Login: `login.html`
- Admin (4): `admin_dashboard.html`, `admin_teachers.html`, `admin_students.html`, `admin_courses.html`
- Teacher (4): `teacher_dashboard.html`, `teacher_questions.html`, `teacher_exams.html`, `teacher_students.html`
- Student (4): `student_dashboard.html`, `student_practice.html`, `student_exam.html`, `student_results.html`

## Build & Run

### macOS / Linux

```bash
# Build (from exam-system-backend/)
mvn clean package -DskipTests

# Run
java -jar exam-system-backend/target/exam-system-backend-1.0.0.jar

# All-in-one: kill old, rebuild, restart
pkill -f "exam-system-backend"; sleep 1
mvn -f exam-system-backend/pom.xml clean package -DskipTests -q
java -jar exam-system-backend/target/exam-system-backend-1.0.0.jar &

# Test an API
curl -s -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"role":"admin","username":"admin","password":"123456"}'

# Access the app
# Open http://localhost:8080/login.html
```

### Windows

```cmd
:: 一键构建+启动（推荐）
cd exam-system-backend
build-and-run.bat

:: 停止服务
stop.bat

:: 或手动执行：
::   构建：  mvn clean package -DskipTests
::   启动：  start java -jar target\exam-system-backend-1.0.0.jar
::   停止：  netstat -ano | findstr ":8080"  然后  taskkill /F /PID <PID>
```

> 🟡 **Windows 组员配置**：如果 Maven 无法解析依赖，复制项目根目录的 `maven-settings-template.xml` 到 `%USERPROFILE%\.m2\settings.xml`。国内阿里云镜像可能未缓存 `mybatis-spring-boot-starter:4.0.1` 等最新版本。

**Tech stack:** Spring Boot 4.0.7 (Java 26), MyBatis 4.0.1, MySQL 8.x, BCrypt for passwords, Session/Cookie auth, vanilla JS + Tailwind CSS + Iconify Icon frontend.

**Frontend CDN:** `cdn.tailwindcss.com` (Tailwind CSS via CDN). Iconify Icon is **self-hosted** (`iconify-icon.min.js` in `static/`) because `cdn.jsdelivr.net` is blocked in China.

**Default accounts (password: `123456`):** `admin`, `teacher_wang`, `teacher_li`, `stu_zhang`, `stu_liu`, `stu_chen`

> 🔴 **NEVER create test/temporary accounts.** Do not insert test teachers, students, or any other test data into the database. Only the 6 default accounts above should exist. Use these default accounts for all testing and verification.

## Architecture

```
exam-system-backend/src/main/java/com/examsystem/
├── config/          WebMvcConfig (CORS + interceptors), DataInitializer (seed data)
├── interceptor/     LoginInterceptor (session check), RoleInterceptor (role check)
├── controller/      AuthController, CourseController + admin/, teacher/, student/ subdirs
├── service/         Interfaces + impl/ subdir — business logic
├── mapper/          13 MyBatis mapper interfaces (includes TeacherCourseMapper, StudentCourseMapper)
├── entity/          13 entity classes, 1:1 mapping to DB tables
├── dto/             Request/response DTOs: Result, PageResult, LoginRequest, etc.
├── enums/           RoleEnum, QuestionTypeEnum, PaperStatusEnum, DifficultyEnum
├── exception/       BusinessException, GlobalExceptionHandler
└── util/            PasswordUtil (BCrypt), SessionUtil (session key constants)

resources/
├── static/          13 HTML pages (served from Spring Boot)
├── db/schema.sql    13 table DDL (CREATE IF NOT EXISTS)
└── mapper/          13 MyBatis XML mapping files
```

## Database

13 tables: `t_admin`, `t_teacher`, `t_student`, `t_course`, `t_question`, `t_question_option`, `t_paper`, `t_paper_question`, `t_practice_record`, `t_exam_record`, `t_exam_answer`, `t_teacher_course`, `t_student_course`. Full DDL + seed data in `02_数据库设计.md` and `resources/db/schema.sql`. All PKs use BIGINT AUTO_INCREMENT. Seed data is inserted by `DataInitializer.java` on first run (checks if admin exists).

**Course assignment tables** (many-to-many): `t_teacher_course` links teachers to courses they teach; `t_student_course` links students to courses they take. These drive permission filtering: teachers only see/manage their assigned courses, students only practice/take exams for their enrolled courses.

**Questions and papers are course-bound, not teacher-bound.** `t_question.teacher_id` and `t_paper.teacher_id` are display-only fields (recording who created the content) with NO foreign key constraint. All query filtering is done by `course_id` + `t_teacher_course`, not by `teacher_id`. Deleting a teacher does NOT affect their questions/papers.

> ⚠️ **Database setup required before first run**: `schema.sql` is a manual DDL script — it is NOT auto-executed. Before starting the app for the first time:
> 1. Create the database: `CREATE DATABASE IF NOT EXISTS exam_system DEFAULT CHARACTER SET utf8mb4;`
> 2. Run `resources/db/schema.sql` against MySQL
> 3. Update credentials in `application.yml` if different from `root` / `11111111`
> 4. On first startup, `DataInitializer` will seed the default accounts (checks if admin exists)
>
> For subsequent schema changes after the database is initialized, use manual `ALTER TABLE` statements. Always verify the DB schema matches `schema.sql`.
>
> 🟡 **Note**: `02_数据库设计.md` in the project root is outdated — it describes 11 tables (missing `t_teacher_course` and `t_student_course`) and includes FK constraints (`fk_question_teacher`, `fk_paper_teacher`) that do NOT exist in the actual schema. Trust `schema.sql` over `02_数据库设计.md`.

## API Pattern

Every response: `{"code":200,"data":{...},"message":"success"}`. Error codes: 400/401/403/500.

Auth: `POST /api/auth/login` with `{role, username, password}` → sets session. All other `/api/**` require login (LoginInterceptor). Role paths (`/api/admin/**`, `/api/teacher/**`, `/api/student/**`) are checked by RoleInterceptor.
- `PUT /api/auth/change-password` — change own password `{oldPassword, newPassword}` (all roles)
- `DELETE /api/admin/teachers/{id}` — delete teacher (removes course links; questions and papers preserved — `teacher_id` has no FK constraint)
- `DELETE /api/admin/students/{id}` — delete student (removes course links; practice/exam records preserved — `student_id` has no FK constraint)

**Course-aware endpoints:**
- `GET /api/courses` — all courses (public, used by admin)
- `GET /api/teacher/courses` — courses assigned to logged-in teacher
- `GET /api/student/courses` — courses enrolled by logged-in student
- `GET /api/admin/teachers/{id}/courses` — get teacher's assigned courses
- `PUT /api/admin/teachers/{id}/courses` — assign courses to teacher `{courseIds: [1,2]}`
- `GET /api/admin/students/{id}/courses` — get student's enrolled courses
- `PUT /api/admin/students/{id}/courses` — assign courses to student `{courseIds: [1,2]}`
- `GET /api/admin/courses/{courseId}/questions` — admin: list questions in a course (supports type/difficulty/keyword/pagination)
- `GET /api/admin/courses/{courseId}/papers` — admin: list papers in a course (supports status/pagination)

Question types: 1=单选, 2=多选, 3=判断, 4=填空, 5=简答. Paper status: 0=未发布, 1=已发布, 2=已回收. Difficulty: 1=易, 2=中, 3=难. Gender: `M`/`F`.

## Frontend Key Rules

1. The HTML files under `resources/static/` are what the app serves — edit these. There may be stale copies at the project root (`teacher_exams.html`) and in `exam_system/` directories — ignore those, they are not served.
8. **CORS is wide-open**: `WebMvcConfig` allows all origins (`allowedOriginPatterns("*")`) with credentials. This is intentional for development; restrict in production.
2. All fetches need `credentials: 'include'` (cookie-based session). 401 response means redirect to `login.html`.
3. API response fields are camelCase. **Never use `.id`** — each entity has its own PK name (see Common Bugs section for the full table).
4. Gender is `M`/`F` string, NOT `1`/`0`.
5. Question type, difficulty, and paper status are integers, NOT enum strings.
6. Every API response is `{code, data, message}`. See Common Bugs section for how to unwrap `data` (it differs by endpoint) and always check `res.code !== 200`.
7. CDN: `cdn.tailwindcss.com` (do NOT change unless broken). Iconify Icon is served locally from `iconify-icon.min.js` — do NOT revert to jsdelivr CDN (blocked in China).

## ⚠️ HTML Editing — Tag Balancing (CRITICAL)

**This is the #1 source of layout-breaking bugs.** When modifying HTML, always follow these rules:

1. **Never use sed/Python string replace for multi-element HTML edits.** Use the Edit tool, which does exact string matching. If the Edit tool can't match, the indentation is wrong — check with `cat -e` or `python3 -c "print(repr(line))"` to see exact whitespace (tabs vs spaces). This project's HTML files use **tab indentation** in some files and **4-space indentation** in others.

2. **Always replace opening + closing tags together.** If you replace a `<div>` wrapper, replace both the opening `<div ...>` AND its corresponding `</div>` in a single Edit call. Never split them across separate edits.

3. **After every HTML edit, verify tag nesting.** Run: `node -e "const fs=require('fs');const h=fs.readFileSync('file.html','utf-8');const re=/<script[^>]*>([\\s\\S]*?)<\\/script>/g;let m,s=[];while((m=re.exec(h))!==null)s.push(m[1]);const js=s[s.length-1];const o=(js.match(/\{/g)||[]).length;const c=(js.match(/\}/g)||[]).length;console.log('Braces:',o,'{',c,'}',o===c?'OK':'UNBALANCED!');try{new Function(js);console.log('JS syntax: OK')}catch(e){console.log('JS ERROR:',e.message)}"` — this checks both JS syntax and brace balance in one shot.
   Or manually trace the nesting: for each `<div ...>` opening, confirm exactly one `</div>` closes it. Look at the indent level — matching tags should be at the same indent level.

4. **When removing an outer wrapper div, remove BOTH the opening tag AND its closing `</div>`.** This happened twice already (admin pages and teacher_exams.html) — the opening tag was deleted but the closing `</div>` was left behind, breaking flex layouts.

5. **Prefer Edit over Bash for HTML changes.** The Edit tool gives you an exact-match guarantee — if it succeeds, the replacement is precise. Bash sed/awk operate on regex and can silently produce broken partial replacements.

## Frontend Shared UI Patterns

These patterns are used across admin AND teacher pages. Keep them consistent.

**User dropdown (all admin + teacher pages):** The top-right header area has a clickable user menu (`id="userMenuTrigger"`) with avatar + name + chevron-down icon, and a dropdown (`id="userDropdown"` with class `user-dropdown`). CSS: `.user-dropdown { display: none; position: absolute; ... }` and `.user-dropdown.show { display: block; }`. JS: click on trigger toggles `.show`; document-level click listener removes it. On teacher pages, the dropdown contains "设置" → opens settings menu → "修改密码" → opens `id="passwordModal"`. The logout link (`href="login.html"`) is at the bottom of the dropdown.

**Admin teacher/student edit modal:** The edit modal now integrates course assignment (checkboxes) and password reset (button at bottom), replacing the separate "分配课程" and "重置密码" buttons that were previously in the table operations column. Operation column is: 编辑 | 启用/禁用 | 删除.

**Sidebar toggle (admin pages only):** Each admin page has `id="sidebar"` on the sidebar `<div>`, `id="menuToggle"` on the hamburger icon, and CSS `.sidebar.collapsed { display: none; }`. The toggle JS switches the `collapsed` class.

**KPI cards (teacher dashboard):** Four stat cards in a 4-column grid showing question count, paper count (with draft subtotal), published exams, and student count.

## Common Bugs & Anti-Patterns (LEARN FROM PAST MISTAKES)

These bugs occurred **repeatedly** across multiple pages in this project. Read before writing any frontend or backend code.

### 🔴 CRITICAL: `.id` — Every Entity Uses Its Own PK Name

This is the **#1 most frequent bug**. Do NOT use `.id` on API response objects. Each entity has its own primary key field:

| Entity | PK Field | Wrong |
|--------|----------|-------|
| Course | `courseId` | ~~`c.id`~~ |
| Question | `questionId` | ~~`q.id`~~ |
| Paper | `paperId` | ~~`p.id`~~ |
| Admin | `adminId` | ~~`a.id`~~ |
| Teacher | `teacherId` | ~~`t.id`~~ |
| Student | `studentId` | ~~`s.id`~~ |

**Every time** you write `.id` on an API response object, STOP and check the entity class. This bug has occurred in: course tree rendering, question edit button onclick, question delete button onclick, question ID display, question edit form population, paper card onclick handlers.

### 🔴 CRITICAL: Always Check `res.code` After API Calls

`fetch()` only rejects on network errors. HTTP 400/500 responses still resolve. **Every** `.then()` must check:
```javascript
.then(function(res) {
    if (res.code !== 200) {
        alert('操作失败：' + (res.message || '未知错误'));
        return;
    }
    // ... actual success logic
})
```
This was missing in: `savePaper()`, `saveQuestion()`, and multiple other places. Without it, the user sees "success" but nothing actually happened.

### 🔴 CRITICAL: Question Type & Difficulty Are Integers, NOT Strings

```javascript
// WRONG — these will break filtering, comparison, and API calls
questionType === 'MULTI_CHOICE'    // 2 !== 'MULTI_CHOICE'
difficulty === 'EASY'              // 1 !== 'EASY'
status === 'PUBLISHED'             // 1 !== 'PUBLISHED'
var noOptions = type === 'FILL_BLANK'  // '4' !== 'FILL_BLANK'

// RIGHT — use integer comparison
questionType == 2                  // Multi-choice
difficulty == 1                    // Easy
status == 1                        // Published
var noOptions = type == 3 || type == 4 || type == 5  // True/False, Fill, Subjective
```

This bug appeared in: difficulty filter buttons, question type display logic, question save validation, dashboard active exams query, loadActiveExams status parameter.

### 🔴 CRITICAL: JavaScript `str.replace` in Python — Match the RIGHT Occurrence

When using Python to modify HTML/JS files, `str.replace('</script>', ...)` will match ALL `</script>` tags — including CDN script tags. Always:
1. Use `re.finditer` to list ALL matches first
2. Verify which occurrence you're targeting
3. Use exact context strings to uniquely identify the target

Examples of failures:
- `content.replace('</script>', ...)` inserted JS into `<script src="...tailwindcss.js">` tags
- `content.replace('    loadPapers();\n', ...)` matched the `loadPapers()` inside `filterPapers()` instead of the standalone call at end of script

### 🔴 CRITICAL: Verify Function Scope Before Inserting Code

When inserting JS functions into existing code via Python:
1. Check brace depth at insertion point: `js[:insertion_point].count('{') - js[:insertion_point].count('}')`
2. Depth must be 0 for global-scope functions
3. If depth > 0, you're inserting inside another function — add `}` to close it first

The `editPaperQuestions` function was inserted inside `filterPapers()` and took 5+ rounds of debugging to fix.

### 🟡 `isCorrect` Is Integer (1/0), Not Boolean

```javascript
// WRONG
isCorrect: check ? check.checked : false     // sends boolean true/false
isCorrect: check.checked                      // sends boolean

// RIGHT  
isCorrect: check && check.checked ? 1 : 0    // sends integer 1/0
```

Backend `QuestionOption.isCorrect` is `Integer`. Jackson does NOT coerce `true` → `1`.

### 🟡 Select Values Are Strings — `parseInt` Before Sending

HTML `<select>` values are always strings. Backend DTOs expect `Integer`/`Long`:
```javascript
// WRONG
courseId: courseId,           // "1" — Jackson will fail to deserialize to Long/Integer
questionType: questionType,   // "2" — same
difficulty: difficulty,       // "3" — same

// RIGHT
courseId: parseInt(courseId),
questionType: parseInt(questionType),
difficulty: parseInt(difficulty),
```

### 🟡 `innerHTML = optionsHtml` Overwrites Default `<option>`

When populating a `<select>` dynamically, always prepend the placeholder:
```javascript
var optionsHtml = '<option value="">请选择课程</option>';  // ← MUST include
for (var i = 0; i < items.length; i++) {
    optionsHtml += '<option value="' + items[i].xxx + '">...</option>';
}
selectElement.innerHTML = optionsHtml;
```

### 🟡 Backend Java Changes Require Server Restart

After editing `.java` files:
1. `mvn clean package -DskipTests` — compiles `.java` → `.class`
2. `pkill -f "exam-system-backend"` — **KILL OLD PROCESS** (this step was frequently forgotten)
3. `java -jar ...` — start with new bytecode

Skipping step 2 means old bytecode keeps running and new fields/methods don't appear in API responses.

### 🟡 Auto-Derive Answer from Options for Single/Multi Choice

For single-choice (type=1) and multi-choice (type=2), the answer should be auto-generated from which options are checked, NOT manually typed. Both the answer input field should be hidden for these types, and `saveQuestion()` should derive `body.answer` from `collectOptions()`:
```javascript
var correctLabels = [];
for (var i = 0; i < body.options.length; i++) {
    if (body.options[i].isCorrect) correctLabels.push(body.options[i].optionLabel);
}
if (correctLabels.length === 0) { alert('请勾选正确答案'); return; }
body.answer = correctLabels.join(',');  // "A" for single, "A,C" for multi
```

### 🟡 Paper Question Score Must Not Exceed Paper Total Score

Validate before saving paper questions that `sum(scores) <= paper.totalScore`. Block saving if exceeded. Also validate on publish that `sum(scores) === paper.totalScore` (exact match required).

### 🟡 Default Form Values Must Use Numeric Strings

When setting select values for new forms, use numeric strings matching the option values:
```javascript
// WRONG
document.getElementById('qType').value = 'SINGLE_CHOICE';  // no option has this value
document.getElementById('qDifficulty').value = 'EASY';     // no option has this value

// RIGHT
document.getElementById('qType').value = '1';   // 单选题
document.getElementById('qDifficulty').value = '1';  // 易
```

### 🟡 API Response Data Unwrapping

Different endpoints return different `data` shapes:
- `GET /api/courses` → `res.data` is the array directly (NOT `res.data.list`)
- `GET /api/teacher/courses` → `res.data` is the array directly
- `GET /api/student/courses` → `res.data` is the array directly
- `GET /api/admin/courses` → `res.data.list` is the array (PageResult wrapper)
- `GET /api/admin/teachers/{id}/courses` → `res.data` is the array directly
- `GET /api/admin/students/{id}/courses` → `res.data` is the array directly
- `GET /api/teacher/papers` → `res.data.list` is the array (PageResult wrapper)
- `GET /api/teacher/questions` → `res.data.list` is the array (PageResult wrapper)
- `GET /api/teacher/papers/{id}` → `res.data` is the single Paper object
- `GET /api/teacher/questions/{id}` → `res.data` is the single Question object

Always verify: `res.data.list` for paginated endpoints, `res.data` for single-entity and list endpoints.

### 🟡 Frontend: `console.log` Errors — Always Check Browser Console

After every HTML/JS edit, ask yourself: "Will this actually parse?" Use the JS syntax checker:
```bash
node -e "const fs=require('fs');const h=fs.readFileSync('file.html','utf-8');const re=/<script[^>]*>([\\s\\S]*?)<\\/script>/g;let m,s=[];while((m=re.exec(h))!==null)s.push(m[1]);const js=s[s.length-1];try{new Function(js);console.log('JS OK')}catch(e){console.log('JS ERROR:',e.message)}"
```
A single misplaced `});` or `)` will break ALL JavaScript on the page — all buttons become unresponsive.

### 🟡 Promise Chain Validation — Don't Let `return;` Fall Through

When validating inside a `.then()` and returning early on failure, the promise chain still continues:
```javascript
// WRONG — validation fails, returns undefined, next .then() runs with undefined res
apiGet(...).then(function(paper) {
    if (invalid) { alert('error'); return; }  // returns undefined → next then
    return apiPut(...);                         // returns promise
}).then(function(res) {
    res.code !== 200  // TypeError if res is undefined from validation failure!
}).catch(function() { alert('网络错误'); });

// RIGHT — nest the API call's then/catch inside the validation branch
apiGet(...).then(function(paper) {
    if (invalid) { alert('error'); return; }
    return apiPut(...).then(function(res) {
        // handle success
    }).catch(function() {
        // handle error
    });
});
```
This caused the "保存失败，请检查网络连接" appearing after the score validation alert.

### 🟡 Paper Edit Must Not Clear Existing Questions

The `savePaper()` function edits paper metadata (name, course, duration, score). It must NOT include `questions: []` in the request body — doing so clears all previously assigned questions. Questions are managed separately via `savePaperQuestions()`.

### 🟡 Paper Publish/Recall Needs Visible Feedback

Always show a success `alert()` after publishing or recalling a paper. Silent success leaves the user uncertain. Also enhance the `confirm()` message to explain consequences (e.g., "发布后学生即可参加测验" / "回收后学生将无法继续参加").

### 🟡 Python Script Edits on HTML — Watch for CDN `<script>` Tags

When using Python `str.replace('</script>', ...)` or regex to insert JS, always target the LAST `</script>` (use `content.rfind('</script>')`). Script tags from CDN `<script src="...tailwindcss.js"></script>` also end with `</script>` — replacing the first one injects functions into the CDN script tag where they won't execute. This happened to all 4 teacher pages and required a full rewrite.

### 🟡 Question Options Must Have Content

Single-choice and multi-choice options must have non-empty content. Validate both frontend (in `saveQuestion()`) and backend (in `QuestionServiceImpl.createQuestion/updateQuestion`).

### 🟡 No Unused Mapper Methods

After refactoring, always check for orphaned mapper methods (Java interface + XML). Methods like `deleteByQuestionIds`, `deleteAllByTeacherId`, `deleteByStudentId`, `deleteByExamRecordId` were left behind after changing the delete strategy and caused no issues at compile time but are dead code that misleads future developers.

### 🔴 CRITICAL: Delete Question — Must Delete FK References First

`t_question` is referenced by FOREIGN KEY from **4 tables**: `t_paper_question`, `t_practice_record`, `t_exam_answer`, `t_question_option`. Before deleting a question, you MUST delete rows from ALL these tables in the correct order, or MySQL will reject the DELETE with a FK constraint error:

```java
// Correct order in QuestionServiceImpl.deleteQuestion():
paperQuestionMapper.deleteByQuestionId(questionId);   // 1. 试卷关联
practiceRecordMapper.deleteByQuestionId(questionId);   // 2. 练习记录
examAnswerMapper.deleteByQuestionId(questionId);       // 3. 考试答案
questionOptionMapper.deleteByQuestionId(questionId);   // 4. 选项
questionMapper.deleteById(questionId);                 // 5. 题目本身
```

Each of these requires a mapper interface method + XML `<delete>` statement. Also, **frontend `deleteQuestion()` must check `res.code` and show feedback** — without it, FK errors are silently ignored and the question appears to not be deleted.

### 🔴 CRITICAL: Frontend Field Names Must Match Backend JSON Exactly

Backend entity fields use camelCase. For `QuestionOption`: the JSON fields are `optionLabel` and `optionContent`. The frontend must read these exact names:

```javascript
// WRONG — these will always be undefined, option content will be blank
var label = opt.label;
var content = opt.content;

// RIGHT — match the backend entity field names
var label = opt.optionLabel;
var content = opt.optionContent;
```

This bug caused ALL option content to render as empty strings across the student practice page.

### 🔴 CRITICAL: Boolean Fields — Jackson Serializes `Boolean isCorrect` as `isCorrect`

Lombok generates `getIsCorrect()` for `Boolean isCorrect`. Jackson preserves the "is" prefix, producing `{"isCorrect": true}` not `{"correct": true}`. Frontend must read BOTH:

```javascript
// Check both — backend may send either depending on the DTO
var isCorrect = result.isCorrect || result.correct;
```

This affected `finishPractice()` and `reviewWrongQuestions()` — `result.correct` was undefined, causing correctCount to always be 0.

### 🔴 CRITICAL: True/False Questions — Auto-Generate Default Options

True/false questions (type=3) typically have NO options stored in `t_question_option`. The practice service (`PracticeServiceImpl.generatePractice()`) must auto-generate two default options when the DB returns empty:

```java
// In generatePractice(), after loading options:
if ((options == null || options.isEmpty()) && q.getQuestionType() == 3) {
    options = new ArrayList<>();
    // optionLabel "A"/"B", optionContent "对"/"错"
    // Content must match what teacher form stores: 对/错 NOT 正确/错误
}
```

Also, when submitting a true/false answer, send the **optionContent** ("对"/"错") not optionLabel ("A"/"B"), because the DB answer field stores "对"/"错".

### 🔴 CRITICAL: Multi-Choice Must Have ≥2 Correct Options

Validate in both frontend AND backend that type=2 questions have at least 2 options marked as correct:

```javascript
// Frontend (teacher_questions.html saveQuestion)
if (questionType == 2 && correctLabels.length < 2) {
    alert('多选题必须至少选择两个正确答案'); return;
}
```
```java
// Backend (QuestionServiceImpl.createQuestion/updateQuestion)
if (dto.getQuestionType() == 2) {
    long correctCount = dto.getOptions().stream()
        .filter(opt -> opt.getIsCorrect() != null && opt.getIsCorrect() == 1).count();
    if (correctCount < 2) throw new BusinessException("多选题必须至少选择两个正确答案");
}
```

### 🟡 Student Practice — Text Input Required for Fill-Blank / Short-Answer

Question types 4 (fill-blank) and 5 (short-answer) have NO options in the database. `renderQuestion()` must render a `<textarea>` for these types, not just option buttons. Without this, the student sees the question but has no way to input an answer.

### 🟡 Student Practice — Confirm-Then-Next Answer Flow

The practice page must follow a **two-step per-question** flow, not immediate submission:

1. Student selects/types answer → bottom button shows **"确认答案"**
2. Student clicks "确认答案" → answer submitted to backend → feedback displayed (✓/✗, correct answer, analysis) → bottom button changes to **"下一题"** (or "完成练习" for last question)
3. Student clicks "下一题" → navigate to next question

Bottom button state machine:
```
未确认 → [上一题] [确认答案]
已确认 → [上一题] [下一题]  or  [上一题] [完成练习]
```

**Never** immediately submit on option click. Options/input should be `disabled` after confirmation.

### 🟡 Student Practice — Multi-Choice Toggle Behavior

Multi-choice (type=2) options must toggle on/off independently (not single-select). Store comma-separated labels in `answers[index]` (e.g., `"A,C"`). Sort before submitting to match backend's `sortChars()` comparison. Require ≥2 before confirming.

### 🟡 Student Practice — Review Mode (查看错题解析)

When entering review mode from results page:
- **Do NOT call `startTimer()`** — this makes it feel like re-taking the test
- Show "复习模式" label instead of timer
- Answer card: 🟢 green = correct, 🔴 red = wrong, ⬜ gray = unanswered
- Each question shows: student answer, correct answer, analysis
- All answer inputs/buttons are disabled (read-only)
- Bottom button becomes "返回结果" to go back to results page
- `renderAnswerCard()` must use `answerResults[i].isCorrect || answerResults[i].correct` for color coding

### 🟡 Async API + Button State — Always Re-enable After Response

When a button triggers an async API call and is disabled to prevent double-clicks, the `renderQuestion()` that runs after the response MUST explicitly set `disabled = false`. Forgetting this leaves the button visually normal but unclickable — user sees "下一题" but clicking does nothing:

```javascript
// In renderQuestion(), always re-enable the button:
var nextBtn = document.getElementById('nextBtn');
nextBtn.disabled = false;  // ← REQUIRED, otherwise button stays disabled from "提交中..." state
```

## Key Constraints

- **Course assignment is required**: Teachers must be assigned courses via `t_teacher_course` before they can create questions/papers. Students must be enrolled in courses via `t_student_course` before they can practice/take exams.
- Teachers only see questions/papers for their assigned courses (filtered via `t_teacher_course`).
- Teachers only see students enrolled in their assigned courses (filtered via `t_student_course` JOIN `t_teacher_course`).
- Students only see practice/exam content for their enrolled courses.
- Students only see available exams from their enrolled courses.
- Admin manages accounts and course assignments but has no access to teaching content.
- Paper editing only allowed when `status=0` (draft).
- Practice returns questions WITHOUT answers (`answer` and `analysis` are null).
- Auto-grading: single-choice and true/false use exact match; multi-choice sorts both strings before comparing; fill-blank/short-answer return reference answer for self-evaluation.
- Deleting a course is blocked if it has associated questions or papers.
- Single-choice and multi-choice questions must have at least 2 options with non-empty content (validated both frontend and backend).
- **Multi-choice questions (type=2) must have at least 2 correct options** (validated both frontend and backend).
- **Deleting a question requires deleting FK-referenced rows first**: `t_paper_question` → `t_practice_record` → `t_exam_answer` → `t_question_option` → `t_question`. Must add `deleteByQuestionId` to all relevant mappers.
- **True/false questions (type=3) may have no options in DB**. Practice service must auto-generate default options with content "对"/"错" (matching the teacher form dropdown values, NOT "正确"/"错误").
- **Student practice flow is confirm-then-next**: select answer → "确认答案" → see feedback → "下一题". Never submit immediately on option click.
- **Deleting a teacher does NOT affect questions/papers**: `teacher_id` has no FK constraint — it's a plain display-only field. Questions/papers remain accessible to other teachers of the same course via `course_id` + `t_teacher_course`.
- **Deleting a student preserves records**: `t_practice_record.student_id` and `t_exam_record.student_id` have FK constraints with `ON DELETE SET NULL`. When a student is deleted, their practice/exam records are preserved (student_id set to NULL).
- 🔴 **NEVER create test/temporary accounts.** Only the 6 default accounts should exist. Use them for all testing.
