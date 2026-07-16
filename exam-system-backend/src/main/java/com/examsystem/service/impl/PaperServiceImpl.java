package com.examsystem.service.impl;

import com.examsystem.dto.PageResult;
import com.examsystem.dto.PaperCreateRequest;
import com.examsystem.entity.Paper;
import com.examsystem.entity.PaperQuestion;
import com.examsystem.exception.BusinessException;
import com.examsystem.mapper.PaperMapper;
import com.examsystem.mapper.PaperQuestionMapper;
import com.examsystem.service.PaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaperServiceImpl implements PaperService {

    @Autowired
    private PaperMapper paperMapper;

    @Autowired
    private PaperQuestionMapper paperQuestionMapper;

    @Override
    public PageResult<Paper> listPapers(Long teacherId, Integer status, Long courseId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<Paper> list = paperMapper.findByFilters(teacherId, status, courseId, offset, pageSize);
        Long total = paperMapper.countByFilters(teacherId, status, courseId);
        return PageResult.of(total, page, pageSize, list);
    }

    @Override
    public Paper getPaperDetail(Long paperId) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper != null) {
            List<PaperQuestion> questions = paperQuestionMapper.selectByPaperId(paperId);
            paper.setQuestions(questions);
        }
        return paper;
    }

    @Override
    @Transactional
    public void createPaper(PaperCreateRequest request, Long teacherId) {
        Paper paper = new Paper();
        paper.setPaperName(request.getPaperName());
        paper.setCourseId(request.getCourseId());
        paper.setTeacherId(teacherId);
        paper.setTotalScore(request.getTotalScore() != null ? request.getTotalScore() : 100);
        paper.setDuration(request.getDuration() != null ? request.getDuration() : 60);
        paper.setStatus(0);
        paper.setCreateTime(LocalDateTime.now());
        paperMapper.insert(paper);

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

    @Override
    @Transactional
    public void updatePaper(Long paperId, PaperCreateRequest request) {
        Paper existing = paperMapper.selectById(paperId);
        if (existing == null) {
            throw new BusinessException("试卷不存在");
        }
        if (existing.getStatus() != 0) {
            throw new BusinessException("只有未发布的试卷可以编辑");
        }

        Paper paper = new Paper();
        paper.setPaperId(paperId);
        paper.setPaperName(request.getPaperName());
        paper.setCourseId(request.getCourseId());
        paper.setTotalScore(request.getTotalScore());
        paper.setDuration(request.getDuration());
        paperMapper.update(paper);

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

    @Override
    public void publishPaper(Long paperId) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        if (paper.getStatus() != 0) {
            throw new BusinessException("只有未发布的试卷可以发布");
        }
        paperMapper.updateStatus(paperId, 1);
    }

    @Override
    public void recallPaper(Long paperId) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        if (paper.getStatus() != 1) {
            throw new BusinessException("只有已发布的试卷可以回收");
        }
        paperMapper.updateStatus(paperId, 2);
    }

    @Override
    @Transactional
    public void deletePaper(Long paperId) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        if (paper.getStatus() == 1) {
            throw new BusinessException("已发布的试卷不能删除");
        }
        paperQuestionMapper.deleteByPaperId(paperId);
        paperMapper.deleteById(paperId);
    }
}
