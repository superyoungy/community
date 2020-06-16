package com.yc.community.dao;

import com.yc.community.entity.LoginTicket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
@Deprecated
public interface LoginTicketMapper {
    // 插入loginTicket
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    //查询loginTicket
    LoginTicket selectByTicket(String ticket);

    //改变loginTicket状态
    int updateStatus(String ticket, int status);
}
