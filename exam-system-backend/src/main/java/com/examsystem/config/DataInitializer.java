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

/**
 * 数据库种子数据初始化器。
 * <p>
 * 实现 {@link CommandLineRunner} 接口，在 Spring Boot 应用启动后自动执行。
 * 通过检查 admin 用户是否存在来判断数据库是否已初始化过（幂等性保证），
 * 如果未初始化则插入默认的 6 个账户、2 门课程、3 道题目以及课程分配关系。
 * <p>
 * 默认密码统一为 {@code 123456}，使用 BCrypt 加密存储。
 * <p>
 * <b>注意：</b>除这 6 个默认账户外，不应在此处或其它地方创建额外的测试账户。
 *
 * @see PasswordUtil BCrypt 密码工具类
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    /** 所有种子账户的默认明文密码 */
    private static final String DEFAULT_PASSWORD = "123456";
    /** 加密后的默认密码，静态常量避免每次重复加密 */
    private static final String ENCODED_PASSWORD = PasswordUtil.encode(DEFAULT_PASSWORD);

    @Autowired private AdminMapper adminMapper;
    @Autowired private TeacherMapper teacherMapper;
    @Autowired private StudentMapper studentMapper;
    @Autowired private CourseMapper courseMapper;
    @Autowired private QuestionMapper questionMapper;
    @Autowired private QuestionOptionMapper questionOptionMapper;
    @Autowired private TeacherCourseMapper teacherCourseMapper;       // 教师-课程多对多关联
    @Autowired private StudentCourseMapper studentCourseMapper;       // 学生-课程多对多关联

    /**
     * 应用启动后自动调用，执行种子数据插入。
     * <p>
     * 初始化顺序：管理员 → 教师 → 学生 → 课程 → 题目（含选项）→ 教师分配课程 → 学生选课。
     * 如果 admin 用户已存在，说明数据库已初始化过，直接跳过（幂等性保证）。
     *
     * @param args 命令行参数（未使用）
     */
    @Override
    public void run(String... args) {
        // 幂等性检查：如果管理员账户已存在，说明种子数据已初始化，跳过
        if (adminMapper.findByUsername("admin") != null) {
            log.info("Seed data already exists, skipping initialization.");
            return;
        }

        log.info("Initializing seed data...");

        // ========== 1. 创建管理员账户 ==========
        Admin admin = new Admin();
        admin.setUsername("admin");
        admin.setPassword(ENCODED_PASSWORD);
        admin.setRealName("系统管理员");
        admin.setPhone("13800000000");
        admin.setCreateTime(LocalDateTime.now());
        adminMapper.insert(admin);
        log.info("Created admin: admin / {}", DEFAULT_PASSWORD);

        // ========== 2. 创建教师账户 ==========
        Teacher t1 = new Teacher();
        t1.setUsername("teacher_wang");
        t1.setPassword(ENCODED_PASSWORD);
        t1.setRealName("王老师");
        t1.setGender("F");               // 女
        t1.setPhone("13800000001");
        t1.setSubject("Java程序设计");
        t1.setStatus(1);                 // 1=启用
        t1.setCreateBy(admin.getAdminId());
        t1.setCreateTime(LocalDateTime.now());
        teacherMapper.insert(t1);

        Teacher t2 = new Teacher();
        t2.setUsername("teacher_li");
        t2.setPassword(ENCODED_PASSWORD);
        t2.setRealName("李老师");
        t2.setGender("M");               // 男
        t2.setPhone("13800000002");
        t2.setSubject("数据库原理");
        t2.setStatus(1);                 // 1=启用
        t2.setCreateBy(admin.getAdminId());
        t2.setCreateTime(LocalDateTime.now());
        teacherMapper.insert(t2);
        log.info("Created 2 teachers");

        // ========== 3. 创建学生账户 ==========
        Student s1 = createStudent("stu_zhang", "张三", "M", "软件2101班", "13900000001", admin.getAdminId());
        Student s2 = createStudent("stu_liu", "刘芳", "F", "软件2101班", "13900000002", admin.getAdminId());
        Student s3 = createStudent("stu_chen", "陈明", "M", "软件2102班", "13900000003", admin.getAdminId());
        log.info("Created 3 students");

        // ========== 4. 创建课程 ==========
        Course c1 = createCourse("Java程序设计", "面向对象编程与Java基础语法");
        Course c2 = createCourse("数据库原理", "关系型数据库设计与SQL语言");
        log.info("Created 2 courses");

        // ========== 5. 创建题目（含选项） ==========
        // 单选题：Java定义类关键字，难度=易，答案=A
        Question q1 = createQuestion(c1.getCourseId(), 1, "Java中定义一个类使用以下哪个关键字？", "A", "class 是Java中定义类的关键字。", 1, t1.getTeacherId());
        questionMapper.insert(q1);
        insertOptions(q1.getQuestionId(), new String[][]{{"A", "class", "1"}, {"B", "struct", "0"}, {"C", "define", "0"}, {"D", "object", "0"}});

        // 判断题：Java解释型语言，难度=中，答案=对；判断题无选项，答案存储在 answer 字段
        Question q2 = createQuestion(c1.getCourseId(), 3, "Java是一种解释型语言。", "对", "Java代码先编译为字节码，再由JVM解释执行，兼具编译与解释特性。", 2, t1.getTeacherId());
        questionMapper.insert(q2);

        // 单选题：MySQL主键约束，难度=易，答案=B
        Question q3 = createQuestion(c2.getCourseId(), 1, "在MySQL中，用于唯一标识表中一行数据的约束是？", "B", "主键（PRIMARY KEY）用于唯一标识一行记录。", 1, t2.getTeacherId());
        questionMapper.insert(q3);
        insertOptions(q3.getQuestionId(), new String[][]{{"A", "外键", "0"}, {"B", "主键", "1"}, {"C", "索引", "0"}, {"D", "视图", "0"}});
        log.info("Created 3 questions");

        // ========== 6. 教师-课程分配（多对多） ==========
        teacherCourseMapper.insert(t1.getTeacherId(), c1.getCourseId()); // 王老师 -> Java程序设计
        teacherCourseMapper.insert(t2.getTeacherId(), c2.getCourseId()); // 李老师 -> 数据库原理
        log.info("Assigned courses to teachers");

        // ========== 7. 学生-课程选课（多对多） ==========
        studentCourseMapper.insert(s1.getStudentId(), c1.getCourseId()); // 张三 -> Java程序设计
        studentCourseMapper.insert(s1.getStudentId(), c2.getCourseId()); // 张三 -> 数据库原理
        studentCourseMapper.insert(s2.getStudentId(), c1.getCourseId()); // 刘芳 -> Java程序设计
        studentCourseMapper.insert(s3.getStudentId(), c2.getCourseId()); // 陈明 -> 数据库原理
        log.info("Assigned courses to students");
    }

    /**
     * 创建学生实体并插入数据库。
     *
     * @param username   登录用户名
     * @param realName   真实姓名
     * @param gender     性别（M=男, F=女）
     * @param className  班级名称
     * @param phone      手机号
     * @param createBy   创建者ID（管理员ID）
     * @return 已插入的 Student 对象（含自增主键 studentId）
     */
    private Student createStudent(String username, String realName, String gender, String className, String phone, Long createBy) {
        Student s = new Student();
        s.setUsername(username);
        s.setPassword(ENCODED_PASSWORD);
        s.setRealName(realName);
        s.setGender(gender);
        s.setClassName(className);
        s.setPhone(phone);
        s.setStatus(1);          // 1=启用，默认启用
        s.setCreateBy(createBy);
        s.setCreateTime(LocalDateTime.now());
        studentMapper.insert(s);
        return s;
    }

    /**
     * 创建课程实体并插入数据库。
     *
     * @param name 课程名称
     * @param desc 课程描述
     * @return 已插入的 Course 对象（含自增主键 courseId）
     */
    private Course createCourse(String name, String desc) {
        Course c = new Course();
        c.setCourseName(name);
        c.setDescription(desc);
        c.setCreateTime(LocalDateTime.now());
        courseMapper.insert(c);
        return c;
    }

    /**
     * 构建题目实体（不插入数据库，由调用方决定是否插入）。
     *
     * @param courseId   所属课程ID
     * @param type       题目类型：1=单选, 2=多选, 3=判断, 4=填空, 5=简答
     * @param content    题目内容
     * @param answer     正确答案
     * @param analysis   题目解析
     * @param difficulty 难度：1=易, 2=中, 3=难
     * @param teacherId  创建教师ID
     * @return 构建好的 Question 对象
     */
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

    /**
     * 批量插入题目选项。
     *
     * @param questionId 所属题目ID
     * @param options    选项数组，每个元素为 [标签, 内容, 是否正确答案(1/0)]
     */
    private void insertOptions(Long questionId, String[][] options) {
        for (String[] opt : options) {
            QuestionOption o = new QuestionOption();
            o.setQuestionId(questionId);
            o.setOptionLabel(opt[0]);                                    // 选项标签，如 A/B/C/D
            o.setOptionContent(opt[1]);                                  // 选项内容文本
            o.setIsCorrect(Integer.parseInt(opt[2]));                    // 1=正确答案，0=错误答案
            questionOptionMapper.insert(o);
        }
    }
}
