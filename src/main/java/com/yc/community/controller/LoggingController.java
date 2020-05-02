package com.yc.community.controller;

import com.google.code.kaptcha.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

@Controller
public class LoggingController {
    @Autowired
    private Producer producer;

    private static final Logger logger = LoggerFactory.getLogger(LoggingController.class);

//    @RequestMapping(path = "/login", method = RequestMethod.POST)
//    public String login(String userName, String password, String code, boolean rememberme,
//                        Model model, HttpSession session, HttpServletResponse response) {
//        //检查验证码
//        String kaptcha = (String) session.getAttribute("kaptcha");
//        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
//            model.addAttribute("codeMsg", "验证码不正确");
//            return "/site/login";
//        }
//        return "/site/login";
//    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "site/login";
    }

    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
        String text = producer.createText();
        BufferedImage image = producer.createImage(text);
        session.setAttribute("kaptcha", text);
        response.setContentType("image/png");
        try {
            OutputStream outputStream = response.getOutputStream();
            ImageIO.write(image,"png",outputStream);
        } catch (IOException e) {
            logger.error("响应验证码失败" + e.getMessage());
        }//sprringMVC会自动关闭流

    }
}
