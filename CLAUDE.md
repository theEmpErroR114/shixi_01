# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

课程习题测验系统 (Course Exercise Quiz System) — a Spring Boot + MyBatis + MySQL web application with three user roles: Admin, Teacher, Student. The frontend is 13 static HTML pages served from the same Spring Boot server (port 8080):
- Login: `login.html`
- Admin (4): `admin_dashboard.html`, `admin_teachers.html`, `admin_students.html`, `admin_courses.html`
- Teacher (4): `teacher_dashboard.html`, `teacher_questions.html`, `teacher_exams.html`, `teacher_students.html`
- Student (4): `student_dashboard.html`, `student_practice.html`, `student_exam.html`, `student_results.html`

## Build & Run

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

**Tech stack:** Spring Boot 4.0.7 (Java 26), MyBatis 4.0.1, MySQL 8.x, BCrypt for passwords, Session/Cookie auth, vanilla JS + Tailwind CSS frontend (CDN: `cdn.tailwindcss.com` + `cdn.jsdelivr.net/npm/iconify-icon@2`).

> 🔴 **Windows team members**: If Maven can't resolve `mybatis-spring-boot-starter:4.0.1` or other recent dependencies, copy `maven-settings-template.xml` to `%USERPROFILE%\.m2\settings.xml`. The Aliyun mirror commonly used in China may not have cached the latest versions. Also ensure JDK 26 is installed.

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

> ⚠️ `schema.sql` only executes on first run (when DataInitializer detects no admin exists). Any schema changes needed after the database has been initialized require manual `ALTER TABLE` statements. Always check if the DB schema matches `schema.sql`.

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

1. The HTML files under `resources/static/` are what the app serves — edit these, not the `exam_system/` copies.
2. All fetches need `credentials: 'include'` (cookie-based session). 401 response means redirect to `login.html`.
3. API response fields are camelCase. **Never use `.id`** — each entity has its own PK name (see Common Bugs section for the full table).
4. Gender is `M`/`F` string, NOT `1`/`0`.
5. Question type, difficulty, and paper status are integers, NOT enum strings.
6. Every API response is `{code, data, message}`. See Common Bugs section for how to unwrap `data` (it differs by endpoint) and always check `res.code !== 200`.
7. CDN sources (do NOT change unless broken): `cdn.tailwindcss.com` and `cdn.jsdelivr.net/npm/iconify-icon@2/dist/iconify-icon.min.js`.

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
- **Deleting a teacher does NOT affect questions/papers**: `teacher_id` has no FK constraint — it's a plain display-only field. Questions/papers remain accessible to other teachers of the same course via `course_id` + `t_teacher_course`.
- **Deleting a student preserves records**: `t_practice_record.student_id` and `t_exam_record.student_id` are plain fields with no FK constraint. Practice and exam records are preserved.
- 🔴 **NEVER create test/temporary accounts.** Only the 6 default accounts should exist. Use them for all testing.
