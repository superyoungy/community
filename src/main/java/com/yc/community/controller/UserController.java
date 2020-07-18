package com.yc.community.controller;

import com.yc.community.annotation.LoginRequired;
import com.yc.community.entity.User;
import com.yc.community.service.FollowService;
import com.yc.community.service.LikeService;
import com.yc.community.service.UserService;
import com.yc.community.util.CommunityConstant;
import com.yc.community.util.CommunityUtil;
import com.yc.community.util.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${community.path.upload}")
    private String uploadPath;

    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage() {
        return "site/setting";
    }

    @LoginRequired
    @PostMapping("/upload")
    public String updateSettings(Model model, MultipartFile headerImg) {
        //验证文件是否为空
        if (headerImg == null) {
            model.addAttribute("error", "未选择文件！");
            return "site/setting";
        }

        //验证文件名格式
        String originalFilename = headerImg.getOriginalFilename();
        int i = originalFilename.lastIndexOf(".");
        if (i < 0 || i == originalFilename.length()-1) {
            model.addAttribute("error", "文件格式不正确！");
            return "site/setting";
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //替换文件名
        String s = CommunityUtil.generateUUID();
        String filename = s + suffix;

        //上传文件
        File dest = new File(uploadPath + "/" + filename);
        try {
            headerImg.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败:" + e.getMessage());
            throw new  RuntimeException("上传文件失败，服务器发生异常！");
        }

        //更改头像路径
        User user = hostHolder.getUser();
        if (user != null) {
            String headerUrl = domain + contextPath + "/user/headerUrl/" + filename;
            userService.updateHeader(user.getId(), headerUrl);
        }
        return "redirect:/index";
    }

    @GetMapping("/headerUrl/{filename}")
    public void getHeaderUrl(@PathVariable String filename, HttpServletResponse response) {
        String suffix = filename.substring(filename.lastIndexOf("."));
        response.setContentType("/image" + suffix);
        try (
                ServletOutputStream fos = response.getOutputStream();
                FileInputStream fis = new FileInputStream(new File(uploadPath + "/" + filename));
        ) {
            byte[] bytes = new byte[1024];
            int len;
            while ((len = fis.read(bytes)) != -1) {
                fos.write(bytes, 0, len);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    @LoginRequired
    @PostMapping("/updatePassword")
    public String updatePassword(Model model, String originPassword, String newPassword, String repetition) {
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user.getId(), originPassword, newPassword, repetition);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "密码修改成功！");
            model.addAttribute("target", "/index");
            return "site/operate-result";
        } else {
            model.addAttribute("originPasswordMsg", map.get("originPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "site/setting";
        }
    }

    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);

        int likeCount = likeService.getLikeCountUser(userId);
        model.addAttribute("likeCount", likeCount);

        //关注多少人
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);

        //被多少人关注
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);

        //是否已关注
        int followStatus = followService.findFollowStatus(ENTITY_TYPE_USER, userId);
        model.addAttribute("followStatus", followStatus);

        return "site/profile";

    }

}
