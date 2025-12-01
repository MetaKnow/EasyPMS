package com.missoft.pms.service;

import com.missoft.pms.dto.AfterServiceEventDTO;
import com.missoft.pms.entity.AfterServiceEvent;
import com.missoft.pms.repository.AfterServiceDeliverableRepository;
import com.missoft.pms.repository.AfterServiceEventRepository;
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
        AfterServiceEvent saved = afterServiceEventRepository.save(entity);
        return toDTO(saved);
    }

    public AfterServiceEventDTO updateEvent(Long id, AfterServiceEventDTO dto) {
        AfterServiceEvent entity = afterServiceEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        BeanUtils.copyProperties(dto, entity, "eventId", "createTime", "updateTime");
        entity.setEventId(id);
        AfterServiceEvent saved = afterServiceEventRepository.save(entity);
        return toDTO(saved);
    }

    public void deleteEvent(Long id) {
        AfterServiceEvent entity = afterServiceEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
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
}
