package com.coding24h.imessage.service;

import com.coding24h.imessage.entity.Message;
import java.util.List;

public interface MessageService {
    List<Message> getAllWithUserAndCategory();
    Message getById(Long id);
    void add(Message message);
    void update(Message message);
    void delete(Long id);
}