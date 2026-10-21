package com.coding24h.imessage.service.impl;

import com.coding24h.imessage.entity.Message;
import com.coding24h.imessage.mapper.MessageMapper;
import com.coding24h.imessage.service.MessageService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    private final MessageMapper messageMapper;

    MessageServiceImpl(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    @Override
    public List<Message> getAllWithUserAndCategory() {
        return messageMapper.selectAllWithUserAndCategory();
    }

    @Override
    public Message getById(Long id) {
        return messageMapper.selectById(id);
    }

    @Override
    public void add(Message message) {
        messageMapper.insert(message);
    }

    @Override
    public void update(Message message) {
        messageMapper.update(message);
    }

    @Override
    public void delete(Long id) {
        messageMapper.deleteById(id);
    }
}