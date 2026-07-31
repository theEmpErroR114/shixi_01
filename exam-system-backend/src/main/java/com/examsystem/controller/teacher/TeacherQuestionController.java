package com.examsystem.controller.teacher;

import com.examsystem.dto.PageResult;
import com.examsystem.dto.QuestionDTO;
import com.examsystem.dto.Result;
import com.examsystem.entity.Question;
import com.examsystem.exception.BusinessException;
import com.examsystem.mapper.TeacherCourseMapper;
import com.examsystem.service.QuestionService;
import com.examsystem.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师题目管理控制器 — 提供题目的 CRUD 操作及多条件筛选查询。
 * <p>
 * 教师只能操作自己所授课程下的题目（通过 t_teacher_course 表进行权限控制）。
 * 题目类型：1=单选, 2=多选, 3=判断, 4=填空, 5=简答。
 * 难度等级：1=易, 2=中, 3=难。
 * <p>
 * <b>注意：</b>删除题目时必须先删除外键关联数据（试卷关联、练习记录、考试答案、选项），
 * 否则 MySQL 外键约束会阻止删除。
 *
 * @see com.examsystem.service.QuestionService 题目业务逻辑
 */
@RestController
@RequestMapping("/api/teacher/questions")
public class TeacherQuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private TeacherCourseMapper teacherCourseMapper;

    /**
     * 分页查询教师可管理的题目列表，支持多条件筛选。
     * <p>
     * 如果未指定 courseId，则查询教师所有关联课程下的题目；
     * 如果指定了 courseId，则只查询该课程下的题目。
     *
     * @param courseId     课程ID筛选（可选）
     * @param questionType 题目类型筛选（可选）
     * @param difficulty   难度筛选（可选）
     * @param keyword      关键词搜索（可选，匹配题目内容）
     * @param page         当前页码
     * @param pageSize     每页条数
     * @param session      HTTP Session
     * @return 分页题目结果（不含选项详情）
     */
    @GetMapping
    public Result<PageResult<Question>> list(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Integer questionType,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpSession session) {
        Long teacherId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        // 如果没传 courseId，只查教师关联课程下的题目
        List<Long> courseIds = null;
        if (courseId == null) {
            courseIds = teacherCourseMapper.selectCourseIdsByTeacherId(teacherId);
            if (courseIds.isEmpty()) {
                // 教师未分配任何课程，返回空结果
                return Result.success(PageResult.of(0L, page, pageSize, java.util.Collections.emptyList()));
            }
        }
        return Result.success(questionService.listQuestions(courseId, questionType, difficulty, keyword, page, pageSize, courseIds));
    }

    /**
     * 获取题目详情（含选项列表）。
     *
     * @param id 题目ID
     * @return 题目详情（含选项）
     */
    @GetMapping("/{id}")
    public Result<Question> getById(@PathVariable Long id) {
        return Result.success(questionService.getQuestionDetail(id));
    }

    /**
     * 创建新题目（含选项）。
     * <p>
     * 创建前会校验教师是否有该课程的操作权限。
     * 单选和多选题必须包含至少 2 个选项，多选题至少 2 个正确答案。
     *
     * @param dto     题目DTO（含题目内容和选项列表）
     * @param session HTTP Session
     * @return 操作成功
     * @throws BusinessException 如果教师没有该课程的权限或数据校验失败
     */
    @PostMapping
    public Result<?> create(@RequestBody QuestionDTO dto, HttpSession session) {
        Long teacherId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        // 权限校验：教师只能在自己被分配的课程下创建题目
        List<Long> allowedCourseIds = teacherCourseMapper.selectCourseIdsByTeacherId(teacherId);
        if (!allowedCourseIds.contains(dto.getCourseId())) {
            throw new BusinessException("您没有该课程的权限");
        }
        questionService.createQuestion(dto, teacherId);
        return Result.success();
    }

    /**
     * 更新题目信息（含选项的全量替换）。
     *
     * @param id  题目ID
     * @param dto 题目DTO
     * @return 操作成功
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody QuestionDTO dto) {
        questionService.updateQuestion(id, dto);
        return Result.success();
    }

    /**
     * 删除题目。
     * <p>
     * 删除前会自动清除所有外键关联数据（试卷题目关联、练习记录、考试答案、选项），
     * 然后删除题目本身。如果某一步失败，整个操作会回滚。
     *
     * @param id 题目ID
     * @return 操作成功
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return Result.success();
    }
}
