package com.examsystem.service;

import com.examsystem.dto.PageResult;
import com.examsystem.dto.PaperCreateRequest;
import com.examsystem.entity.Paper;

import java.util.List;

public interface PaperService {
    PageResult<Paper> listPapers(Integer status, Long courseId, Integer page, Integer pageSize);
    PageResult<Paper> listPapers(Integer status, Long courseId, Integer page, Integer pageSize, List<Long> courseIds);
    Paper getPaperDetail(Long paperId);
    void createPaper(PaperCreateRequest request, Long teacherId);
    void updatePaper(Long paperId, PaperCreateRequest request);
    void publishPaper(Long paperId);
    void recallPaper(Long paperId);
    void deletePaper(Long paperId);
}
