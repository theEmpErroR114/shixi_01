package com.examsystem.service.impl;

import com.examsystem.dto.ExamResultVO;
import com.examsystem.dto.ExamSubmitRequest;
import com.examsystem.dto.PracticeResultVO;
import com.examsystem.entity.*;
import com.examsystem.exception.BusinessException;
import com.examsystem.mapper.*;
import com.examsystem.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamServiceImpl implements ExamService {

    @Autowired
    private PaperMapper paperMapper;

    @Autowired
    private PaperQuestionMapper paperQuestionMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionOptionMapper questionOptionMapper;

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Autowired
    private ExamAnswerMapper examAnswerMapper;

    @Autowired
    private StudentCourseMapper studentCourseMapper;

    @Override
    public List<Paper> getAvailableExams(Long studentId) {
        // 只返回学生关联课程的已发布试卷
        List<Long> enrolledCourseIds = studentCourseMapper.selectCourseIdsByStudentId(studentId);
        if (enrolledCourseIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Paper> papers = paperMapper.findByFilters(null, 1, null, null, null);
        return papers.stream().filter(p -> {
            if (!enrolledCourseIds.contains(p.getCourseId())) {
                return false;
            }
            List<ExamRecord> records = examRecordMapper.selectByStudentIdAndPaperId(studentId, p.getPaperId(), 1);
            return records.isEmpty();
        }).collect(Collectors.toList());
    }

    @Override
    public Long startExam(Long studentId, Long paperId) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null || paper.getStatus() != 1) {
            throw new BusinessException("该试卷不可作答");
        }
        List<ExamRecord> completed = examRecordMapper.selectByStudentIdAndPaperId(studentId, paperId, 1);
        if (!completed.isEmpty()) {
            throw new BusinessException("你已完成该测验");
        }
        ExamRecord record = new ExamRecord();
        record.setStudentId(studentId);
        record.setPaperId(paperId);
        record.setStartTime(LocalDateTime.now());
        record.setStatus(0);
        examRecordMapper.insert(record);
        return record.getExamRecordId();
    }

    @Override
    public List<Question> getExamQuestions(Long examRecordId, Long studentId) {
        ExamRecord record = examRecordMapper.selectById(examRecordId);
        if (record == null || !record.getStudentId().equals(studentId)) {
            throw new BusinessException("无权访问该测验");
        }
        if (record.getStatus() != 0) {
            throw new BusinessException("该测验已提交或已过期");
        }
        List<PaperQuestion> pqList = paperQuestionMapper.selectByPaperId(record.getPaperId());
        List<Question> questions = new ArrayList<>();
        for (PaperQuestion pq : pqList) {
            Question q = questionMapper.selectById(pq.getQuestionId());
            q.setOptions(questionOptionMapper.selectByQuestionId(pq.getQuestionId()));
            q.setAnswer(null);
            q.setAnalysis(null);
            questions.add(q);
        }
        return questions;
    }

    @Override
    @Transactional
    public ExamResultVO submitExam(Long examRecordId, Long studentId, ExamSubmitRequest request) {
        ExamRecord record = examRecordMapper.selectById(examRecordId);
        if (record == null || !record.getStudentId().equals(studentId)) {
            throw new BusinessException("无权访问该测验");
        }
        if (record.getStatus() != 0) {
            throw new BusinessException("该测验已提交，请勿重复提交");
        }

        List<PaperQuestion> pqList = paperQuestionMapper.selectByPaperId(record.getPaperId());

        BigDecimal totalScore = BigDecimal.ZERO;
        List<ExamAnswer> answers = new ArrayList<>();

        for (PaperQuestion pq : pqList) {
            Question q = questionMapper.selectById(pq.getQuestionId());
            ExamSubmitRequest.ExamAnswerItem answerItem = request.getAnswers().stream()
                    .filter(a -> a.getQuestionId().equals(pq.getQuestionId()))
                    .findFirst().orElse(null);

            String studentAnswer = answerItem != null ? answerItem.getStudentAnswer() : "";
            boolean isCorrect = checkAnswer(studentAnswer, q.getAnswer(), q.getQuestionType());

            ExamAnswer answer = new ExamAnswer();
            answer.setExamRecordId(examRecordId);
            answer.setQuestionId(pq.getQuestionId());
            answer.setStudentAnswer(studentAnswer);
            answer.setIsCorrect(isCorrect ? 1 : 0);
            BigDecimal score = isCorrect ? BigDecimal.valueOf(pq.getScore()) : BigDecimal.ZERO;
            answer.setScore(score);
            answers.add(answer);
            totalScore = totalScore.add(score);
        }

        examAnswerMapper.batchInsert(answers);
        examRecordMapper.updateTotalScore(examRecordId, totalScore);
        examRecordMapper.updateStatus(examRecordId, 1, LocalDateTime.now());

        return buildResult(examRecordId, studentId);
    }

    @Override
    public ExamResultVO getExamResult(Long examRecordId, Long studentId) {
        ExamRecord record = examRecordMapper.selectById(examRecordId);
        if (record == null || !record.getStudentId().equals(studentId)) {
            throw new BusinessException("无权查看该成绩");
        }
        if (record.getStatus() != 1) {
            throw new BusinessException("该测验尚未提交");
        }
        return buildResult(examRecordId, studentId);
    }

    @Override
    public List<ExamRecord> getExamHistory(Long studentId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        return examRecordMapper.selectByStudentIdWithPage(studentId, offset, pageSize);
    }

    @Override
    public Long countExamHistory(Long studentId) {
        return examRecordMapper.countByStudentId(studentId);
    }

    private ExamResultVO buildResult(Long examRecordId, Long studentId) {
        ExamRecord record = examRecordMapper.selectById(examRecordId);
        List<ExamAnswer> answers = examAnswerMapper.selectByExamRecordId(examRecordId);

        ExamResultVO vo = new ExamResultVO();
        vo.setExamRecordId(examRecordId);
        vo.setPaperName(record.getPaperName());
        vo.setCourseName(record.getCourseName());
        vo.setTotalScore(record.getTotalScore());
        vo.setPaperTotalScore(record.getPaperTotalScore());
        if (record.getStartTime() != null && record.getSubmitTime() != null) {
            long minutes = java.time.Duration.between(record.getStartTime(), record.getSubmitTime()).toMinutes();
            vo.setUsedMinutes((int) minutes);
        }

        long correctCount = answers.stream().filter(a -> a.getIsCorrect() != null && a.getIsCorrect() == 1).count();
        vo.setCorrectCount((int) correctCount);
        vo.setQuestionCount(answers.size());

        List<ExamResultVO.ExamAnswerDetail> details = new ArrayList<>();
        for (ExamAnswer answer : answers) {
            ExamResultVO.ExamAnswerDetail detail = new ExamResultVO.ExamAnswerDetail();
            detail.setQuestionId(answer.getQuestionId());
            detail.setContent(answer.getQuestionContent());
            detail.setQuestionType(answer.getQuestionType());
            detail.setStudentAnswer(answer.getStudentAnswer());
            detail.setCorrectAnswer(answer.getCorrectAnswer());
            detail.setAnalysis(answer.getAnalysis());
            detail.setIsCorrect(answer.getIsCorrect());
            detail.setScore(answer.getScore());
            List<QuestionOption> options = questionOptionMapper.selectByQuestionId(answer.getQuestionId());
            if (options != null && !options.isEmpty()) {
                detail.setOptions(options.stream().map(o -> {
                    PracticeResultVO.OptionVO ov = new PracticeResultVO.OptionVO();
                    ov.setOptionLabel(o.getOptionLabel());
                    ov.setOptionContent(o.getOptionContent());
                    return ov;
                }).collect(Collectors.toList()));
            }
            details.add(detail);
        }
        vo.setAnswers(details);
        return vo;
    }

    private boolean checkAnswer(String studentAnswer, String correctAnswer, Integer questionType) {
        if (studentAnswer == null || correctAnswer == null) return false;
        if (questionType == null) return studentAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
        switch (questionType) {
            case 1:
            case 3:
                return studentAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
            case 2:
                return sortChars(studentAnswer.trim().toUpperCase())
                        .equals(sortChars(correctAnswer.trim().toUpperCase()));
            default:
                return studentAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
        }
    }

    private String sortChars(String s) {
        return s.chars().sorted()
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
