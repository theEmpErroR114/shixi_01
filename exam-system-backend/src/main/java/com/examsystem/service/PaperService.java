package com.examsystem.service;

import com.examsystem.dto.PageResult;
import com.examsystem.dto.PaperCreateRequest;
import com.examsystem.entity.Paper;

public interface PaperService {
    PageResult<Paper> listPapers(Long teacherId, Integer status, Long courseId, Integer page, Integer pageSize);
    Paper getPaperDetail(Long paperId);
    void createPaper(PaperCreateRequest request, Long teacherId);
    void updatePaper(Long paperId, PaperCreateRequest request);
    void publishPaper(Long paperId);
    void recallPaper(Long paperId);
    void deletePaper(Long paperId);
}
