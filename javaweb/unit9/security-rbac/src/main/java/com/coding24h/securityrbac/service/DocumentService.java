package com.coding24h.securityrbac.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class DocumentService {

    @PreAuthorize("hasAuthority('document:view')")
    public String viewDocument(Long docId) {
        return "文档内容（ID:" + docId + "）—— 您拥有查看权限。";
    }

    @PreAuthorize("hasAuthority('document:edit')")
    public String editDocument(Long docId) {
        return "文档（ID:" + docId + "）已被编辑。";
    }
}
