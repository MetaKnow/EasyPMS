package com.missoft.pms.service;

import com.missoft.pms.entity.InterfaceEntity;
import com.missoft.pms.repository.InterfaceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InterfaceService {
    private final InterfaceRepository repository;

    public InterfaceService(InterfaceRepository repository) {
        this.repository = repository;
    }

    public InterfaceEntity create(InterfaceEntity entity) {
        return repository.save(entity);
    }

    public List<InterfaceEntity> listByProject(Long projectId) {
        return repository.findByProjectId(projectId);
    }
}