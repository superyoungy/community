package com.yc.community.controller;

import com.yc.community.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
public class DataController {
    @Autowired
    private DataService dataService;

    @RequestMapping(path = "/data", method = {RequestMethod.GET, RequestMethod.POST})
    public String getDataPage() {
        return "site/admin/data";
    }

    @PostMapping("/data/uv")
    public String calculateUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date beginDate,
                              @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                              Model model) {
        long count = dataService.calculateUV(beginDate, endDate);
        model.addAttribute("uvCount", count);
        model.addAttribute("uvBeginDate", beginDate);
        model.addAttribute("uvEndDate", endDate);

        return "forward:/data";
    }

    @PostMapping("/data/dau")
    public String calculateDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date beginDate,
                              @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                              Model model) {
        long count = dataService.calculateDAU(beginDate, endDate);
        model.addAttribute("dauCount", count);
        model.addAttribute("dauBeginDate", beginDate);
        model.addAttribute("dauEndDate", endDate);

        return "forward:/data";

    }
}
