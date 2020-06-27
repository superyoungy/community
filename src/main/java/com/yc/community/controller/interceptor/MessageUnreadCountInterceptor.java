package com.yc.community.controller.interceptor;

import com.yc.community.service.MessageService;
import com.yc.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MessageUnreadCountInterceptor implements HandlerInterceptor {
    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (hostHolder.getUser() != null && modelAndView != null) {
            int letterUnreadCount = messageService.findLetterUnreadCount(hostHolder.getUser().getId(), null);
            int noticeUnreadCount = messageService.findNoticeUnreadCount(hostHolder.getUser().getId(), null);
            int messageUnreadCountAll = letterUnreadCount + noticeUnreadCount;
            modelAndView.addObject("messageUnreadCountAll", messageUnreadCountAll);
        }
    }
}
