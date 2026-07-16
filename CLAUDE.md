# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

课程习题测验系统 (Course Exercise Quiz System) — a Spring Boot + MyBatis + MySQL web application with three user roles: Admin, Teacher, Student. The frontend is 12 static HTML pages served from the same Spring Boot server (port 8080):
- Login: `login.html`
- Admin (3): `admin_dashboard.html`, `admin_teachers.html`, `admin_students.html`
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

**Tech stack:** Spring Boot 4.0.7 (Java 26), MyBatis 4.0.1, MySQL 8.x, BCrypt for passwords, Session/Cookie auth, vanilla JS + Tailwind CSS frontend.

**Default accounts (password: `123456`):** `admin`, `teacher_wang`, `teacher_li`, `stu_zhang`, `stu_liu`, `stu_chen`

## Architecture

```
exam-system-backend/src/main/java/com/examsystem/
├── config/          WebMvcConfig (CORS + interceptors), DataInitializer (seed data)
├── interceptor/     LoginInterceptor (session check), RoleInterceptor (role check)
├── controller/      AuthController, CourseController + admin/, teacher/, student/ subdirs
├── service/         Interfaces + impl/ subdir — business logic
├── mapper/          11 MyBatis mapper interfaces
├── entity/          11 entity classes, 1:1 mapping to DB tables
├── dto/             Request/response DTOs: Result, PageResult, LoginRequest, etc.
├── enums/           RoleEnum, QuestionTypeEnum, PaperStatusEnum, DifficultyEnum
├── exception/       BusinessException, GlobalExceptionHandler
└── util/            PasswordUtil (BCrypt), SessionUtil (session key constants)

resources/
├── static/          12 HTML pages (served from Spring Boot)
├── db/schema.sql    11 table DDL (CREATE IF NOT EXISTS)
└── mapper/          11 MyBatis XML mapping files
```

## Database

11 tables: `t_admin`, `t_teacher`, `t_student`, `t_course`, `t_question`, `t_question_option`, `t_paper`, `t_paper_question`, `t_practice_record`, `t_exam_record`, `t_exam_answer`. Full DDL + seed data in `02_数据库设计.md` and `resources/db/schema.sql`. All PKs use BIGINT AUTO_INCREMENT. FK relationships documented in the design doc. Seed data is inserted by `DataInitializer.java` on first run (checks if admin exists).

## API Pattern

Every response: `{"code":200,"data":{...},"message":"success"}`. Error codes: 400/401/403/500.

Auth: `POST /api/auth/login` with `{role, username, password}` → sets session. All other `/api/**` require login (LoginInterceptor). Role paths (`/api/admin/**`, `/api/teacher/**`, `/api/student/**`) are checked by RoleInterceptor.

Question types: 1=单选, 2=多选, 3=判断, 4=填空, 5=简答. Paper status: 0=未发布, 1=已发布, 2=已回收. Difficulty: 1=易, 2=中, 3=难. Gender: `M`/`F`.

## Frontend Key Rules

1. The HTML files under `resources/static/` are what the app serves — edit these, not the `exam_system/` copies.
2. Every `apiGet/apiPost/apiPut/apiDelete` returns `{code, data, message}`. Always unwrap with `var actual = response.data || response;` before using.
3. API response fields are camelCase: `teacherId`, `studentId`, `courseId`, `questionId`, `paperId`, `realName`, `className`, `createTime`. Never use `.id`.
4. Gender is `M`/`F` string, NOT `1`/`0`.
5. Question type and difficulty are integers, NOT strings like `"SINGLE_CHOICE"` or `"EASY"`.
6. Session is cookie-based — all fetches need `credentials: 'include'`. 401 means redirect to `login.html`.

## Frontend Shared UI Patterns

All three admin pages share these patterns (keep them consistent across pages):

**Sidebar toggle:** Each admin page has `id="sidebar"` on the sidebar `<div>`, `id="menuToggle"` on the hamburger icon, and CSS `.sidebar.collapsed { display: none; }`. The toggle JS switches the `collapsed` class.

**User dropdown:** Each admin page has a user menu in the top-right (`id="userMenuTrigger"` on the clickable area, `id="userDropdown"` on the dropdown). Click toggles `.show` on the dropdown; clicking elsewhere closes it (`e.stopPropagation()` + document-level listener).

## Key Constraints

- Teachers only see their own questions/papers (filtered by `teacher_id` from session).
- Students only see their own practice/exam records.
- Admin manages accounts but has no access to teaching content.
- Paper editing only allowed when `status=0` (draft).
- Practice returns questions WITHOUT answers (`answer` and `analysis` are null).
- Auto-grading: single-choice and true/false use exact match; multi-choice sorts both strings before comparing; fill-blank/short-answer return reference answer for self-evaluation.
