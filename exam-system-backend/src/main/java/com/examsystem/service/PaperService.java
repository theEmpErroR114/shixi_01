package com.examsystem.service;

import com.examsystem.dto.PageResult;
import com.examsystem.dto.PaperCreateRequest;
import com.examsystem.entity.Paper;

import java.util.List;

/**
 * 试卷业务接口 — 负责试卷的CRUD、发布/回收管理。
 * 试卷状态：0=未发布（草稿）、1=已发布（学生可作答）、2=已回收。
 * 试卷只有在草稿状态（status=0）时才能编辑。
 */
public interface PaperService {

    /**
     * 分页查询试卷列表（无课程权限过滤，供管理员使用）。
     *
     * @param status   试卷状态：0=未发布，1=已发布，2=已回收，null=全部
     * @param courseId 课程ID筛选（可为null）
     * @param page     页码（从1开始）
     * @param pageSize 每页条数
     * @return 分页结果（含总数和列表）
     */
    PageResult<Paper> listPapers(Integer status, Long courseId, Integer page, Integer pageSize);

    /**
     * 分页查询试卷列表（带课程权限过滤，供教师使用）。
     * 教师只能查看自己所属课程的试卷。
     *
     * @param status    试卷状态
     * @param courseId  课程ID筛选
     * @param page      页码
     * @param pageSize  每页条数
     * @param courseIds 教师有权限的课程ID列表
     * @return 分页结果
     */
    PageResult<Paper> listPapers(Integer status, Long courseId, Integer page, Integer pageSize, List<Long> courseIds);

    /**
     * 获取试卷详情，含已关联的试题列表（t_paper_question）和每题分值。
     *
     * @param paperId 试卷ID
     * @return 试卷实体（questions字段已填充），不存在则返回null
     */
    Paper getPaperDetail(Long paperId);

    /**
     * 创建试卷（初始状态为草稿status=0）。同时保存试卷基本信息和试题关联。
     *
     * @param request   创建请求（含试卷名称、课程、总分、时长、开始/截止日期、试题列表）
     * @param teacherId 创建者教师ID
     */
    void createPaper(PaperCreateRequest request, Long teacherId);

    /**
     * 更新试卷信息。只有草稿状态（status=0）的试卷可以编辑。
     * 如果请求中包含questions字段，会先删除旧试题关联再插入新数据。
     *
     * @param paperId 试卷ID
     * @param request 更新请求
     * @throws BusinessException 如果试卷不存在或已发布
     */
    void updatePaper(Long paperId, PaperCreateRequest request);

    /**
     * 发布试卷，将状态从0（未发布）改为1（已发布）。
     * 发布后学生即可在前端看到并参加考试。
     *
     * @param paperId 试卷ID
     * @throws BusinessException 如果试卷不存在或状态不是草稿
     */
    void publishPaper(Long paperId);

    /**
     * 回收试卷，将状态从1（已发布）改为2（已回收）。
     * 回收后学生无法继续参加该考试。
     *
     * @param paperId 试卷ID
     * @throws BusinessException 如果试卷不存在或状态不是已发布
     */
    void recallPaper(Long paperId);

    /**
     * 删除试卷。已发布的试卷（status=1）不允许删除。
     * 先删除试卷-试题关联（t_paper_question），再删除试卷记录。
     *
     * @param paperId 试卷ID
     * @throws BusinessException 如果试卷不存在或已发布
     */
    void deletePaper(Long paperId);
}
