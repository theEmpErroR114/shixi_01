package com.examsystem.service.impl;

import com.examsystem.dto.PageResult;
import com.examsystem.dto.PaperCreateRequest;
import com.examsystem.entity.Paper;
import com.examsystem.entity.PaperQuestion;
import com.examsystem.exception.BusinessException;
import com.examsystem.mapper.ExamAnswerMapper;
import com.examsystem.mapper.ExamRecordMapper;
import com.examsystem.mapper.PaperMapper;
import com.examsystem.mapper.PaperQuestionMapper;
import com.examsystem.service.PaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 试卷业务实现 — 管理试卷的创建、编辑、发布、回收和删除。
 *
 * 试卷状态流转：
 * 0（未发布/草稿） --publish--> 1（已发布） --recall--> 2（已回收）
 *
 * 约束规则：
 * - 只有草稿状态(0)的试卷可以编辑和发布
 * - 只有已发布状态(1)的试卷可以回收
 * - 已发布状态(1)的试卷不允许删除
 * - 编辑试卷时如果传入questions字段，会先删除旧关联再插入新关联（replace模式）
 */
@Service
public class PaperServiceImpl implements PaperService {

    @Autowired
    private PaperMapper paperMapper;

    @Autowired
    private PaperQuestionMapper paperQuestionMapper;

    @Autowired
    private ExamAnswerMapper examAnswerMapper;

    @Autowired
    private ExamRecordMapper examRecordMapper;

    /**
     * 分页查询试卷（无课程权限过滤，供管理员使用）。
     *
     * @param status   试卷状态筛选：0=未发布，1=已发布，2=已回收，null=全部
     * @param courseId 课程ID筛选（可为null）
     * @param page     页码（从1开始）
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @Override
    public PageResult<Paper> listPapers(Integer status, Long courseId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<Paper> list = paperMapper.findByFilters(status, courseId, offset, pageSize);
        Long total = paperMapper.countByFilters(status, courseId);
        return PageResult.of(total, page, pageSize, list);
    }

    /**
     * 分页查询试卷（带课程权限过滤，供教师使用）。
     * 教师只能查看自己所属课程的试卷。
     *
     * @param status    试卷状态筛选
     * @param courseId  课程ID筛选
     * @param page      页码（从1开始）
     * @param pageSize  每页条数
     * @param courseIds 教师有权限的课程ID列表
     * @return 分页结果
     */
    @Override
    public PageResult<Paper> listPapers(Integer status, Long courseId, Integer page, Integer pageSize, List<Long> courseIds) {
        int offset = (page - 1) * pageSize;
        List<Paper> list = paperMapper.findByFiltersAndCourseIds(status, courseId, offset, pageSize, courseIds);
        Long total = paperMapper.countByFiltersAndCourseIds(status, courseId, courseIds);
        return PageResult.of(total, page, pageSize, list);
    }

    /**
     * 获取试卷详情，同时加载关联的试题列表（t_paper_question）。
     *
     * @param paperId 试卷ID
     * @return 试卷实体（questions字段已填充），不存在则返回null
     */
    @Override
    public Paper getPaperDetail(Long paperId) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper != null) {
            List<PaperQuestion> questions = paperQuestionMapper.selectByPaperId(paperId);
            paper.setQuestions(questions);
        }
        return paper;
    }

    /**
     * 创建试卷。事务操作包含两步：
     * 1. 插入试卷基本信息（初始状态=0草稿，默认总分100，默认时长60分钟）
     * 2. 如果前端传入了试题列表，批量插入试卷-试题关联
     *
     * @param request   创建请求（含试卷名称、课程、总分、时长、开始/截止日期、试题列表）
     * @param teacherId 创建者教师ID
     */
    @Override
    @Transactional
    public void createPaper(PaperCreateRequest request, Long teacherId) {
        Paper paper = new Paper();
        paper.setPaperName(request.getPaperName());
        paper.setCourseId(request.getCourseId());
        paper.setTeacherId(teacherId);
        // 默认值：总分100，时长60分钟
        paper.setTotalScore(request.getTotalScore() != null ? request.getTotalScore() : 100);
        paper.setDuration(request.getDuration() != null ? request.getDuration() : 60);
        // 初始状态为草稿
        paper.setStatus(0);
        paper.setStartDate(request.getStartDate());
        paper.setEndDate(request.getEndDate());
        paper.setCreateTime(LocalDateTime.now());
        paperMapper.insert(paper);

        // 保存试卷-试题关联，默认每题10分，排序号默认1
        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            List<PaperQuestion> pqList = new ArrayList<>();
            for (PaperCreateRequest.PaperQuestionItem item : request.getQuestions()) {
                PaperQuestion pq = new PaperQuestion();
                pq.setPaperId(paper.getPaperId());
                pq.setQuestionId(item.getQuestionId());
                pq.setScore(item.getScore() != null ? item.getScore() : 10);
                pq.setSortOrder(item.getSortOrder() != null ? item.getSortOrder() : 1);
                pqList.add(pq);
            }
            paperQuestionMapper.batchInsert(pqList);
        }
    }

    /**
     * 更新试卷。只有草稿状态(0)的试卷可以编辑。
     * 试题关联采用"先删后增"策略：先删除旧关联，再插入新关联。
     * 注意：更新试卷元数据时不能包含questions字段，否则会清空已有试题
     * （前端在仅编辑元数据时不传questions，在编辑试题时同时传入）。
     *
     * @param paperId 试卷ID
     * @param request 更新请求
     * @throws BusinessException 如果试卷不存在或状态不是草稿
     */
    @Override
    @Transactional
    public void updatePaper(Long paperId, PaperCreateRequest request) {
        Paper existing = paperMapper.selectById(paperId);
        if (existing == null) {
            throw new BusinessException("试卷不存在");
        }
        // 关键校验：只有草稿状态(0)的试卷可以编辑
        if (existing.getStatus() != 0) {
            throw new BusinessException("只有未发布的试卷可以编辑");
        }

        Paper paper = new Paper();
        paper.setPaperId(paperId);
        paper.setPaperName(request.getPaperName());
        paper.setCourseId(request.getCourseId());
        paper.setTotalScore(request.getTotalScore());
        paper.setDuration(request.getDuration());
        paper.setStartDate(request.getStartDate());
        paper.setEndDate(request.getEndDate());
        paperMapper.update(paper);

        // 如果前端传入了questions字段，则替换试题关联（先删后增）
        if (request.getQuestions() != null) {
            paperQuestionMapper.deleteByPaperId(paperId);
            if (!request.getQuestions().isEmpty()) {
                List<PaperQuestion> pqList = new ArrayList<>();
                for (PaperCreateRequest.PaperQuestionItem item : request.getQuestions()) {
                    PaperQuestion pq = new PaperQuestion();
                    pq.setPaperId(paperId);
                    pq.setQuestionId(item.getQuestionId());
                    pq.setScore(item.getScore() != null ? item.getScore() : 10);
                    pq.setSortOrder(item.getSortOrder() != null ? item.getSortOrder() : 1);
                    pqList.add(pq);
                }
                paperQuestionMapper.batchInsert(pqList);
            }
        }
    }

    /**
     * 发布试卷：状态从0（草稿）改为1（已发布）。
     * 发布后学生即可在前端看到该考试。
     *
     * @param paperId 试卷ID
     * @throws BusinessException 如果试卷不存在或状态不是草稿
     */
    @Override
    public void publishPaper(Long paperId) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        // 只有草稿状态可以发布
        if (paper.getStatus() != 0) {
            throw new BusinessException("只有未发布的试卷可以发布");
        }
        paperMapper.updateStatus(paperId, 1);
    }

    /**
     * 回收试卷：状态从1（已发布）改为2（已回收）。
     * 回收后学生无法继续参加该考试。
     *
     * @param paperId 试卷ID
     * @throws BusinessException 如果试卷不存在或状态不是已发布
     */
    @Override
    public void recallPaper(Long paperId) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        // 只有已发布状态可以回收
        if (paper.getStatus() != 1) {
            throw new BusinessException("只有已发布的试卷可以回收");
        }
        paperMapper.updateStatus(paperId, 2);
    }

    /**
     * 删除试卷。已发布的试卷（status=1）不允许删除。
     * 先删除试卷-试题关联，再删除试卷记录。
     *
     * @param paperId 试卷ID
     * @throws BusinessException 如果试卷不存在或已发布
     */
    @Override
    @Transactional
    public void deletePaper(Long paperId) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        // 已发布的试卷不允许删除，防止误删正在使用的考试
        if (paper.getStatus() == 1) {
            throw new BusinessException("已发布的试卷不能删除");
        }
        // 级联删除顺序：先删最外层的子表，逐层向内删除，最后删试卷本身
        // 1. 删除该试卷下所有考试记录的答题明细（t_exam_answer）
        examAnswerMapper.deleteByPaperId(paperId);
        // 2. 删除该试卷下所有考试记录（t_exam_record）
        examRecordMapper.deleteByPaperId(paperId);
        // 3. 删除试卷-题目关联（t_paper_question）
        paperQuestionMapper.deleteByPaperId(paperId);
        // 4. 删除试卷（t_paper）
        paperMapper.deleteById(paperId);
    }
}
