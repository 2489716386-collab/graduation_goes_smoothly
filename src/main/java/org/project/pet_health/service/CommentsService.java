package org.project.pet_health.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.project.pet_health.entity.Comments;
import java.util.List;

public interface CommentsService extends IService<Comments> {
    Page<Comments> getAdminPage(Integer pageNum, Integer pageSize, Integer status, String content, String startDate, String endDate);
    void batchAuditComments(List<Long> commentIds, Integer status);
}
