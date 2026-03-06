package com.missoft.pms.service;

import com.missoft.pms.entity.ConstructingProjectModifyRecord;
import com.missoft.pms.repository.ConstructingProjectModifyRecordRepository;
import com.missoft.pms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ConstructingProjectModifyRecordService {

    @Autowired
    private ConstructingProjectModifyRecordRepository recordRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ConstructingProjectModifyRecord> listByProjectId(Long projectId) {
        if (projectId == null) return List.of();
        return recordRepository.findByProjectIdOrderByRecordIdDesc(projectId);
    }

    public ConstructingProjectModifyRecord createRecord(Long projectId, Long modifyUser, String modifyAction, String beforeValue, String afterValue) {
        if (projectId == null) throw new IllegalArgumentException("projectId 不能为空");
        if (modifyUser == null) throw new IllegalArgumentException("modifyUser 不能为空");
        if (modifyAction == null || modifyAction.trim().isEmpty()) throw new IllegalArgumentException("modifyAction 不能为空");
        ConstructingProjectModifyRecord record = new ConstructingProjectModifyRecord();
        record.setProjectId(projectId);
        record.setModifyUser(modifyUser);
        record.setModifyAction(modifyAction);
        record.setModifyDescription(buildDescription(modifyUser, beforeValue, afterValue));
        record.setModifyDate(LocalDate.now());
        return recordRepository.save(record);
    }

    private String buildDescription(Long modifyUser, String beforeValue, String afterValue) {
        String userLabel = resolveUserLabel(modifyUser);
        String beforeText = normalizeValue(beforeValue);
        String afterText = normalizeValue(afterValue);
        return "修改人: " + userLabel + ", 修改前: " + beforeText + ", 修改后: " + afterText;
    }

    private String resolveUserLabel(Long userId) {
        if (userId == null) return "未知用户";
        var user = userRepository.findById(userId).orElse(null);
        if (user == null) return "用户ID=" + userId;
        String name = user.getName();
        if (name != null && !name.trim().isEmpty()) return name.trim() + "(ID=" + userId + ")";
        String userName = user.getUserName();
        if (userName != null && !userName.trim().isEmpty()) return userName.trim() + "(ID=" + userId + ")";
        return "用户ID=" + userId;
    }

    private String normalizeValue(String value) {
        if (value == null) return "无";
        String v = value.trim();
        return v.isEmpty() ? "无" : v;
    }
}
