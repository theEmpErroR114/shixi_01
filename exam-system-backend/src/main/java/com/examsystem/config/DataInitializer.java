package com.examsystem.config;

import com.examsystem.entity.*;
import com.examsystem.mapper.*;
import com.examsystem.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String DEFAULT_PASSWORD = "123456";
    private static final String ENCODED_PASSWORD = PasswordUtil.encode(DEFAULT_PASSWORD);

    @Autowired private AdminMapper adminMapper;
    @Autowired private TeacherMapper teacherMapper;
    @Autowired private StudentMapper studentMapper;
    @Autowired private CourseMapper courseMapper;
    @Autowired private QuestionMapper questionMapper;
    @Autowired private QuestionOptionMapper questionOptionMapper;
    @Autowired private TeacherCourseMapper teacherCourseMapper;
    @Autowired private StudentCourseMapper studentCourseMapper;

    @Override
    public void run(String... args) {
        if (adminMapper.findByUsername("admin") != null) {
            log.info("Seed data already exists, skipping initialization.");
            return;
        }

        log.info("Initializing seed data...");

        // 1. Admin
        Admin admin = new Admin();
        admin.setUsername("admin");
        admin.setPassword(ENCODED_PASSWORD);
        admin.setRealName("系统管理员");
        admin.setPhone("13800000000");
        admin.setCreateTime(LocalDateTime.now());
        adminMapper.insert(admin);
        log.info("Created admin: admin / {}", DEFAULT_PASSWORD);

        // 2. Teachers
        Teacher t1 = new Teacher();
        t1.setUsername("teacher_wang");
        t1.setPassword(ENCODED_PASSWORD);
        t1.setRealName("王老师");
        t1.setGender("F");
        t1.setPhone("13800000001");
        t1.setSubject("Java程序设计");
        t1.setStatus(1);
        t1.setCreateBy(admin.getAdminId());
        t1.setCreateTime(LocalDateTime.now());
        teacherMapper.insert(t1);

        Teacher t2 = new Teacher();
        t2.setUsername("teacher_li");
        t2.setPassword(ENCODED_PASSWORD);
        t2.setRealName("李老师");
        t2.setGender("M");
        t2.setPhone("13800000002");
        t2.setSubject("数据库原理");
        t2.setStatus(1);
        t2.setCreateBy(admin.getAdminId());
        t2.setCreateTime(LocalDateTime.now());
        teacherMapper.insert(t2);
        log.info("Created 2 teachers");

        // 3. Students
        Student s1 = createStudent("stu_zhang", "张三", "M", "软件2101班", "13900000001", admin.getAdminId());
        Student s2 = createStudent("stu_liu", "刘芳", "F", "软件2101班", "13900000002", admin.getAdminId());
        Student s3 = createStudent("stu_chen", "陈明", "M", "软件2102班", "13900000003", admin.getAdminId());
        log.info("Created 3 students");

        // 4. Courses
        Course c1 = createCourse("Java程序设计", "面向对象编程与Java基础语法");
        Course c2 = createCourse("数据库原理", "关系型数据库设计与SQL语言");
        log.info("Created 2 courses");

        // 5. Questions
        Question q1 = createQuestion(c1.getCourseId(), 1, "Java中定义一个类使用以下哪个关键字？", "A", "class 是Java中定义类的关键字。", 1, t1.getTeacherId());
        questionMapper.insert(q1);
        insertOptions(q1.getQuestionId(), new String[][]{{"A", "class", "1"}, {"B", "struct", "0"}, {"C", "define", "0"}, {"D", "object", "0"}});

        Question q2 = createQuestion(c1.getCourseId(), 3, "Java是一种解释型语言。", "对", "Java代码先编译为字节码，再由JVM解释执行，兼具编译与解释特性。", 2, t1.getTeacherId());
        questionMapper.insert(q2);

        Question q3 = createQuestion(c2.getCourseId(), 1, "在MySQL中，用于唯一标识表中一行数据的约束是？", "B", "主键（PRIMARY KEY）用于唯一标识一行记录。", 1, t2.getTeacherId());
        questionMapper.insert(q3);
        insertOptions(q3.getQuestionId(), new String[][]{{"A", "外键", "0"}, {"B", "主键", "1"}, {"C", "索引", "0"}, {"D", "视图", "0"}});
        log.info("Created 3 questions");

        // 6. Teacher-Course assignments
        teacherCourseMapper.insert(t1.getTeacherId(), c1.getCourseId()); // 王老师 -> Java程序设计
        teacherCourseMapper.insert(t2.getTeacherId(), c2.getCourseId()); // 李老师 -> 数据库原理
        log.info("Assigned courses to teachers");

        // 7. Student-Course assignments
        studentCourseMapper.insert(s1.getStudentId(), c1.getCourseId()); // 张三 -> Java程序设计
        studentCourseMapper.insert(s1.getStudentId(), c2.getCourseId()); // 张三 -> 数据库原理
        studentCourseMapper.insert(s2.getStudentId(), c1.getCourseId()); // 刘芳 -> Java程序设计
        studentCourseMapper.insert(s3.getStudentId(), c2.getCourseId()); // 陈明 -> 数据库原理
        log.info("Assigned courses to students");
    }

    private Student createStudent(String username, String realName, String gender, String className, String phone, Long createBy) {
        Student s = new Student();
        s.setUsername(username);
        s.setPassword(ENCODED_PASSWORD);
        s.setRealName(realName);
        s.setGender(gender);
        s.setClassName(className);
        s.setPhone(phone);
        s.setStatus(1);
        s.setCreateBy(createBy);
        s.setCreateTime(LocalDateTime.now());
        studentMapper.insert(s);
        return s;
    }

    private Course createCourse(String name, String desc) {
        Course c = new Course();
        c.setCourseName(name);
        c.setDescription(desc);
        c.setCreateTime(LocalDateTime.now());
        courseMapper.insert(c);
        return c;
    }

    private Question createQuestion(Long courseId, Integer type, String content, String answer, String analysis, Integer difficulty, Long teacherId) {
        Question q = new Question();
        q.setCourseId(courseId);
        q.setQuestionType(type);
        q.setContent(content);
        q.setAnswer(answer);
        q.setAnalysis(analysis);
        q.setDifficulty(difficulty);
        q.setTeacherId(teacherId);
        q.setCreateTime(LocalDateTime.now());
        return q;
    }

    private void insertOptions(Long questionId, String[][] options) {
        for (String[] opt : options) {
            QuestionOption o = new QuestionOption();
            o.setQuestionId(questionId);
            o.setOptionLabel(opt[0]);
            o.setOptionContent(opt[1]);
            o.setIsCorrect(Integer.parseInt(opt[2]));
            questionOptionMapper.insert(o);
        }
    }
}
