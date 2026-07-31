package com.examsystem.controller.teacher;

import com.examsystem.dto.PageResult;
import com.examsystem.dto.PaperCreateRequest;
import com.examsystem.dto.Result;
import com.examsystem.entity.Paper;
import com.examsystem.exception.BusinessException;
import com.examsystem.mapper.TeacherCourseMapper;
import com.examsystem.service.PaperService;
import com.examsystem.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师试卷管理控制器 — 提供试卷的 CRUD 及发布/回收操作。
 * <p>
 * 教师只能操作自己所授课程下的试卷（通过 t_teacher_course 表进行权限控制）。
 * 试卷生命周期：<b>草稿(0) → 发布(1) → 回收(2)</b>。
 * 只有草稿状态的试卷可以编辑和删除，已发布的试卷只能回收。
 *
 * @see com.examsystem.service.PaperService 试卷业务逻辑
 */
@RestController
@RequestMapping("/api/teacher/papers")
public class TeacherPaperController {

    @Autowired
    private PaperService paperService;

    @Autowired
    private TeacherCourseMapper teacherCourseMapper;

    /**
     * 分页查询教师可管理的试卷列表。
     * <p>
     * 如果未指定 courseId，则查询教师所有关联课程下的试卷；
     * 如果指定了 courseId，则只查询该课程下的试卷。
     *
     * @param status   试卷状态筛选：0=未发布, 1=已发布, 2=已回收（可选）
     * @param courseId 课程ID筛选（可选）
     * @param page     当前页码
     * @param pageSize 每页条数
     * @param session  HTTP Session
     * @return 分页试卷结果
     */
    @GetMapping
    public Result<PageResult<Paper>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long courseId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpSession session) {
        Long teacherId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        List<Long> courseIds = null;
        if (courseId == null) {
            // 未指定课程：获取教师所有关联课程
            courseIds = teacherCourseMapper.selectCourseIdsByTeacherId(teacherId);
            if (courseIds.isEmpty()) {
                // 教师未分配任何课程，返回空结果
                return Result.success(PageResult.of(0L, page, pageSize, java.util.Collections.emptyList()));
            }
        }
        return Result.success(paperService.listPapers(status, courseId, page, pageSize, courseIds));
    }

    /**
     * 获取试卷详情（含题目列表）。
     *
     * @param id 试卷ID
     * @return 试卷详情
     */
    @GetMapping("/{id}")
    public Result<Paper> getById(@PathVariable Long id) {
        return Result.success(paperService.getPaperDetail(id));
    }

    /**
     * 创建新试卷。
     * <p>
     * 创建前会校验教师是否有该课程的操作权限。
     *
     * @param request 试卷创建请求（含课程ID、名称、时长、总分等）
     * @param session HTTP Session
     * @return 操作成功
     * @throws BusinessException 如果教师没有该课程的权限
     */
    @PostMapping
    public Result<?> create(@RequestBody PaperCreateRequest request, HttpSession session) {
        Long teacherId = (Long) session.getAttribute(SessionUtil.SESSION_USER_ID);
        // 权限校验：教师只能在自己被分配的课程下创建试卷
        List<Long> allowedCourseIds = teacherCourseMapper.selectCourseIdsByTeacherId(teacherId);
        if (!allowedCourseIds.contains(request.getCourseId())) {
            throw new BusinessException("您没有该课程的权限");
        }
        paperService.createPaper(request, teacherId);
        return Result.success();
    }

    /**
     * 更新试卷信息（仅草稿状态可编辑）。
     *
     * @param id      试卷ID
     * @param request 试卷更新请求
     * @return 操作成功
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody PaperCreateRequest request) {
        paperService.updatePaper(id, request);
        return Result.success();
    }

    /**
     * 发布试卷。发布后学生即可在可用考试中看到该试卷并参加考试。
     *
     * @param id 试卷ID
     * @return 操作成功
     */
    @PutMapping("/{id}/publish")
    public Result<?> publish(@PathVariable Long id) {
        paperService.publishPaper(id);
        return Result.success();
    }

    /**
     * 回收试卷。回收后学生无法继续参加该考试。
     *
     * @param id 试卷ID
     * @return 操作成功
     */
    @PutMapping("/{id}/recall")
    public Result<?> recall(@PathVariable Long id) {
        paperService.recallPaper(id);
        return Result.success();
    }

    /**
     * 删除试卷（仅草稿状态可删除）。
     *
     * @param id 试卷ID
     * @return 操作成功
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        paperService.deletePaper(id);
        return Result.success();
    }
}
