package com.yc.community.controller;

import com.yc.community.entity.Event;
import com.yc.community.event.EventProducer;
import com.yc.community.util.CommunityConstant;
import com.yc.community.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ShareController implements CommunityConstant {
    @Value("${wk.image.command}")
    private String wkImageCmd;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);

    @Autowired
    private EventProducer eventProducer;

    @GetMapping("/share")
    @ResponseBody
    public String share(String url) {

        if (StringUtils.isBlank(url)) {
            return "分享路径不能为空！";
        }

        String fileName = CommunityUtil.generateUUID();
        String suffix = ".png";

        Event event = new Event()
                .setTopic(TOPIC_SHARE)
                .addData("fileName", fileName)
                .addData("suffix", suffix)
                .addData("url", url);
        eventProducer.fireEvent(event);

        Map<String, Object> map = new HashMap<>();
        map.put("shareUrl", domain + contextPath + "/share/image/" + fileName);
        return CommunityUtil.getJSONString(0, null, map);
    }

    @GetMapping("/share/image/{fileName}")
    public void getShareImage(@PathVariable String fileName, HttpServletResponse response) {
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("文件名不能为空！");
        }

        String suffix = ".png";
        String filePath = wkImageStorage + "/" + fileName + suffix;
        response.setContentType("image/png");
        try {
            ServletOutputStream fos = response.getOutputStream();
            File file = new File(filePath);
            FileInputStream fin = new FileInputStream(file);

            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = fin.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
        } catch (IOException e) {
            logger.error("获取长图失败" + e.getMessage());
        }

    }

}
