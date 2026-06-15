package com.missoft.pms.service;

import com.missoft.pms.dto.AfterServiceEventDTO;
import com.missoft.pms.entity.AfterServiceEvent;
import com.missoft.pms.entity.AfterServiceProject;
import com.missoft.pms.config.UserContextHolder;
import com.missoft.pms.repository.AfterServiceDeliverableRepository;
import com.missoft.pms.repository.AfterServiceEventRepository;
import com.missoft.pms.repository.AfterServiceProjectParticipantRepository;
import com.missoft.pms.repository.AfterServiceProjectRepository;
import com.missoft.pms.repository.UserRepository;
import com.missoft.pms.service.AfterServiceDeliverableFileService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AfterServiceEventService {

    @Autowired
    private AfterServiceEventRepository afterServiceEventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AfterServiceDeliverableFileService afterServiceDeliverableFileService;

    @Autowired
    private AfterServiceDeliverableRepository afterServiceDeliverableRepository;

    @Autowired
    private AfterServiceProjectRepository afterServiceProjectRepository;

    @Autowired
    private AfterServiceProjectParticipantRepository afterServiceProjectParticipantRepository;

    public Page<AfterServiceEventDTO> getEvents(Long afterServiceProjectId, Pageable pageable) {
        Page<AfterServiceEvent> page = afterServiceEventRepository.findByAfterServiceProjectId(afterServiceProjectId, pageable);
        return page.map(this::toDTO);
    }

    public List<AfterServiceEventDTO> getEventsAll(Long afterServiceProjectId) {
        return afterServiceEventRepository.findByAfterServiceProjectId(afterServiceProjectId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public AfterServiceEventDTO createEvent(AfterServiceEventDTO dto) {
        AfterServiceEvent entity = new AfterServiceEvent();
        BeanUtils.copyProperties(dto, entity);
        entity.setEventId(null);
        Long operatorUserId = resolveOperatorUserId(null);
        if (operatorUserId != null) {
            entity.setCreateUser(operatorUserId);
            entity.setUpdateUser(operatorUserId);
        }
        AfterServiceEvent saved = afterServiceEventRepository.save(entity);
        return toDTO(saved);
    }

    public AfterServiceEventDTO updateEvent(Long id, AfterServiceEventDTO dto) {
        return updateEvent(id, dto, null);
    }

    public AfterServiceEventDTO updateEvent(Long id, AfterServiceEventDTO dto, Long operatorUserId) {
        AfterServiceEvent entity = afterServiceEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        Long originalCreateUser = entity.getCreateUser();
        Long resolvedOperatorUserId = resolveOperatorUserId(operatorUserId);
        assertParticipantCanModify(entity, resolvedOperatorUserId);
        BeanUtils.copyProperties(dto, entity, "eventId", "createTime", "updateTime", "createUser", "updateUser");
        entity.setEventId(id);
        entity.setCreateUser(originalCreateUser);
        if (resolvedOperatorUserId != null) {
            entity.setUpdateUser(resolvedOperatorUserId);
        }
        AfterServiceEvent saved = afterServiceEventRepository.save(entity);
        return toDTO(saved);
    }

    public void deleteEvent(Long id) {
        deleteEvent(id, null);
    }

    public void deleteEvent(Long id, Long operatorUserId) {
        AfterServiceEvent entity = afterServiceEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        Long resolvedOperatorUserId = resolveOperatorUserId(operatorUserId);
        assertParticipantCanModify(entity, resolvedOperatorUserId);
        try {
            Long projectId = entity.getAfterServiceProjectId();
            Long eventId = entity.getEventId();
            if (projectId != null && eventId != null) {
                afterServiceDeliverableFileService.deleteByEvent(projectId, eventId);
            }
        } catch (Exception ignore) {}
        afterServiceEventRepository.deleteById(id);
    }

    private AfterServiceEventDTO toDTO(AfterServiceEvent entity) {
        AfterServiceEventDTO dto = new AfterServiceEventDTO();
        BeanUtils.copyProperties(entity, dto);
        if (entity.getDirector() != null) {
            userRepository.findById(entity.getDirector())
                    .ifPresent(user -> dto.setDirectorName(user.getName()));
        }
        if (entity.getEventId() != null) {
            dto.setHasFiles(afterServiceDeliverableRepository.existsByEventId(entity.getEventId()));
        }
        return dto;
    }

    private Long resolveOperatorUserId(Long preferUserId) {
        if (preferUserId != null) {
            return preferUserId;
        }
        return UserContextHolder.getCurrentUserId();
    }

    private void assertParticipantCanModify(AfterServiceEvent entity, Long operatorUserId) {
        if (entity == null || operatorUserId == null) {
            return;
        }
        if (isSystemAdmin(operatorUserId)) {
            return;
        }
        Long projectId = entity.getAfterServiceProjectId();
        if (projectId == null) {
            return;
        }
        AfterServiceProject project = afterServiceProjectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return;
        }
        boolean isSaleDirector = java.util.Objects.equals(operatorUserId, project.getSaleDirector());
        boolean isServiceDirector = java.util.Objects.equals(operatorUserId, project.getServiceDirector());
        if (isSaleDirector || isServiceDirector) {
            return;
        }
        boolean isParticipant = afterServiceProjectParticipantRepository.existsByProjectIdAndUserId(projectId, operatorUserId);
        if (!isParticipant) {
            return;
        }
        boolean isCreator = java.util.Objects.equals(operatorUserId, entity.getCreateUser());
        if (!isCreator) {
            throw new RuntimeException("项目参与人仅可编辑或删除本人新增的运维事件");
        }
    }

    private boolean isSystemAdmin(Long operatorUserId) {
        if (operatorUserId == null) {
            return false;
        }
        var user = userRepository.findById(operatorUserId).orElse(null);
        if (user == null || user.getUserName() == null) {
            return false;
        }
        return "admin".equalsIgnoreCase(user.getUserName().trim());
    }
}
