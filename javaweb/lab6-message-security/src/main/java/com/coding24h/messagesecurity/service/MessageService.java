package com.coding24h.messagesecurity.service;

import java.util.List;

import com.coding24h.messagesecurity.entity.Message;
import com.coding24h.messagesecurity.entity.User;
import com.coding24h.messagesecurity.mapper.MessageMapper;
import com.coding24h.messagesecurity.mapper.UserMapper;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;

    MessageService(MessageMapper messageMapper, UserMapper userMapper) {
        this.messageMapper = messageMapper;
        this.userMapper = userMapper;
    }

    public List<Message> getMessagesForUser(Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(g -> g.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return messageMapper.findAllMessages();
        } else {
            User user = userMapper.findByUsername(auth.getName());
            return messageMapper.findMessagesByUserId(user.getId());
        }
    }

    @PreAuthorize("hasAuthority('message:delete') or (hasAuthority('message:delete_own') and @messageService.isMessageOwner(#messageId,authentication))")
    public void deleteMessage(Long messageId, Authentication authentication) {
        messageMapper.deleteMessageById(messageId);
    }

    public boolean isMessageOwner(Long messageId, Authentication authentication) {
        User currentUser = userMapper.findByUsername(authentication.getName());
        Long messageOwnerId = messageMapper.findUserIdByMessageId(messageId);
        return currentUser.getId().equals(messageOwnerId);
    }

    public void sendMessage(String content, Authentication authentication) {
        User user = userMapper.findByUsername(authentication.getName());
        Message msg = new Message();
        msg.setContent(content);
        msg.setUserId(user.getId());
        messageMapper.insertMessage(msg);
    }
}
