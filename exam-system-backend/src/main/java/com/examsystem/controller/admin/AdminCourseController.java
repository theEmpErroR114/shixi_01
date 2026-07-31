package com.examsystem.controller.admin;

import com.examsystem.dto.PageResult;
import com.examsystem.dto.Result;
import com.examsystem.entity.Course;
import com.examsystem.entity.Paper;
import com.examsystem.entity.Question;
import com.examsystem.service.AdminService;
import com.examsystem.service.PaperService;
import com.examsystem.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员课程管理控制器 — 提供课程的 CRUD 操作及课程下的题目/试卷查看。
 * <p>
 * 管理员可以：
 * <ul>
 *   <li>分页查询课程列表（支持关键词搜索）</li>
 *   <li>创建、编辑、删除课程</li>
 *   <li>查看某课程下的题目和试卷（只读查看，不涉及编辑）</li>
 * </ul>
 * <b>注意：</b>删除课程前会校验是否有关联的题目或试卷，如果有则拒绝删除。
 *
 * @see com.examsystem.service.AdminService    管理员业务逻辑
 * @see com.examsystem.service.QuestionService  题目服务
 * @see com.examsystem.service.PaperService     试卷服务
 */
@RestController
@RequestMapping("/api/admin/courses")
public class AdminCourseController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private PaperService paperService;

    /**
     * 分页查询课程列表，支持关键词搜索。
     *
     * @param keyword  搜索关键词（可选，匹配课程名称）
     * @param page     当前页码（默认第1页）
     * @param pageSize 每页条数（默认10条）
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<Course>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        List<Course> list = adminService.listCourses(keyword, page, pageSize);
        Long total = adminService.countCourses(keyword);
        return Result.success(PageResult.of(total, page, pageSize, list));
    }

    /**
     * 根据 ID 获取单个课程详情。
     *
     * @param id 课程ID
     * @return 课程实体
     */
    @GetMapping("/{id}")
    public Result<Course> getById(@PathVariable Long id) {
        return Result.success(adminService.getCourseById(id));
    }

    /**
     * 查看某课程下的所有题目（分页、筛选）。
     *
     * @param courseId     课程ID
     * @param questionType 题目类型：1=单选, 2=多选, 3=判断, 4=填空, 5=简答（可选）
     * @param difficulty   难度：1=易, 2=中, 3=难（可选）
     * @param keyword      搜索关键词（可选）
     * @param page         当前页码
     * @param pageSize     每页条数
     * @return 分页题目结果
     */
    @GetMapping("/{courseId}/questions")
    public Result<PageResult<Question>> listQuestions(
            @PathVariable Long courseId,
            @RequestParam(required = false) Integer questionType,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(questionService.listQuestions(courseId, questionType, difficulty, keyword, page, pageSize));
    }

    /**
     * 查看某课程下的所有试卷（分页、按状态筛选）。
     *
     * @param courseId 课程ID
     * @param status   试卷状态：0=未发布, 1=已发布, 2=已回收（可选）
     * @param page     当前页码
     * @param pageSize 每页条数
     * @return 分页试卷结果
     */
    @GetMapping("/{courseId}/papers")
    public Result<PageResult<Paper>> listPapers(
            @PathVariable Long courseId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(paperService.listPapers(status, courseId, page, pageSize));
    }

    /**
     * 创建新课程。
     *
     * @param course 课程实体（JSON 请求体）
     * @return 操作成功
     */
    @PostMapping
    public Result<?> create(@RequestBody Course course) {
        adminService.createCourse(course);
        return Result.success();
    }

    /**
     * 更新课程信息。
     *
     * @param id     课程ID（路径参数）
     * @param course 课程实体（JSON 请求体，包含要更新的字段）
     * @return 操作成功
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Course course) {
        course.setCourseId(id); // 确保路径 ID 与请求体一致
        adminService.updateCourse(course);
        return Result.success();
    }

    /**
     * 删除课程。
     * <p>
     * 如果课程下有关联的题目或试卷，Service 层会抛出 BusinessException 拒绝删除。
     *
     * @param id 课程ID
     * @return 操作成功
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        adminService.deleteCourse(id);
        return Result.success();
    }
}
