package com.yc.community.service;

import com.yc.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    private SimpleDateFormat df =  new SimpleDateFormat("yyyyMMdd");

    public void recordUV(String ip) {
        if (ip == null) {
            throw new IllegalArgumentException("参数为空！");
        } else {
            String UVKey = RedisKeyUtil.getUvKey(df.format(new Date()));
            redisTemplate.opsForHyperLogLog().add(UVKey, ip);
        }
    }

    public long calculateUV(Date beginDate, Date endDate) {
        if (beginDate == null || endDate == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        Calendar calendar = Calendar.getInstance();
        List<String> keyList = new ArrayList<>();
        calendar.setTime(beginDate);
        while (!calendar.getTime().after(endDate)) {
            String UVKey = RedisKeyUtil.getUvKey(df.format(calendar.getTime()));
            keyList.add(UVKey);
            calendar.add(Calendar.DATE, 1);
        }

        String UVAmongKey = RedisKeyUtil.getUvKey(df.format(beginDate), df.format(endDate));
        redisTemplate.opsForHyperLogLog().union(UVAmongKey, keyList.toArray());

        return redisTemplate.opsForHyperLogLog().size(UVAmongKey);
    }

    public void recordDAU(int userId) {
        if (userService.findUserById(userId) == null) {
            throw new IllegalArgumentException("用户不存在！");
        } else {
            String DAUKey = RedisKeyUtil.getDauKey(df.format(new Date()));
            redisTemplate.opsForValue().setBit(DAUKey, userId, true);
        }
    }

    public long calculateDAU(Date beginDate, Date endDate) {
        if (beginDate == null || endDate == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        Calendar calendar = Calendar.getInstance();
        List<byte[]> keyList = new ArrayList<>();
        calendar.setTime(beginDate);
        while (!calendar.getTime().after(endDate)) {
            String DAUKey = RedisKeyUtil.getDauKey(df.format(calendar.getTime()));
            keyList.add(DAUKey.getBytes());
            calendar.add(Calendar.DATE, 1);
        }

        String DAUAmongKey = RedisKeyUtil.getDauKey(df.format(beginDate), df.format(endDate));
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        DAUAmongKey.getBytes(), keyList.toArray(new byte[0][0]));
                return connection.bitCount(DAUAmongKey.getBytes());
            }
        });
    }
}
