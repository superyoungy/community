package com.yc.community.quartz;

import com.yc.community.entity.DiscussPost;
import com.yc.community.service.DiscussPostService;
import com.yc.community.service.ElasticsearchService;
import com.yc.community.service.LikeService;
import com.yc.community.util.CommunityConstant;
import com.yc.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements Job, CommunityConstant {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            logger.error("初始化牛客纪元失败：" + e);
            throw new RuntimeException("初始化牛客纪元失败", e);
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(postScoreKey);

        if (operations.size() == 0) {
            logger.info("[任务取消] 没有需要刷新的帖子！");
            return;
        }

        logger.info("[任务开始] 正在刷新帖子分数：" + operations.size());

        while (operations.size() != 0) {
            refreshPostScore((Integer) operations.pop());
        }

        logger.info("[任务结束] 帖子分数刷新完毕！");
    }

    private void refreshPostScore(Integer postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        if (post == null) {
            logger.error("帖子不存在！id = " + postId);
            return;
        }

        boolean wonderful = (int) post.getStatus() == 1;
        int commentCount = post.getCommentCount();
        long likeCount = likeService.getLikeCountEntity(ENTITY_TYPE_POST, post.getId());

        //分数 = 帖子权重 + 距离天数
        double w = (wonderful ? 75 : 0) + 10 * commentCount + 2 * likeCount;
        double score = (w > 1 ? Math.log10(w) : 0) +
                (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 60 * 60 * 24);

        discussPostService.updateScore(postId, score);
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);

    }
}
