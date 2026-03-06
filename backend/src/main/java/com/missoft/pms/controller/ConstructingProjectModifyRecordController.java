package com.missoft.pms.controller;

import com.missoft.pms.entity.ConstructingProjectModifyRecord;
import com.missoft.pms.service.ConstructingProjectModifyRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/constructing-project-modify-records")
@CrossOrigin(origins = "*")
public class ConstructingProjectModifyRecordController {

    @Autowired
    private ConstructingProjectModifyRecordService modifyRecordService;

    @GetMapping("/by-project/{projectId}")
    public ResponseEntity<List<ConstructingProjectModifyRecord>> listByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(modifyRecordService.listByProjectId(projectId));
    }
}
