package com.ningct.nowcodercommunity.controller;

import com.ningct.nowcodercommunity.service.DataService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.Date;

@Controller
public class DataController {
    @Resource
    private DataService dataService;

    @RequestMapping(path = "/data",method = {RequestMethod.GET,RequestMethod.POST})
    public String getDataPage(){
        return "site/admin/data";
    }

    @RequestMapping(path = "/data/uv",method = RequestMethod.POST)
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                        Model model){
        long res = dataService.caculateUV(startDate, endDate);
        System.out.println(res);
        model.addAttribute("uvResult",res);
        model.addAttribute("uvStartDate",startDate);
        model.addAttribute("uvEndDate",endDate);
        return "forward:/data";
    }
    @RequestMapping(path = "/data/dau",method = RequestMethod.POST)
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                         @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                         Model model){
        long res = dataService.caculateDau(startDate, endDate);
        model.addAttribute("dauResult",res);
        model.addAttribute("dauStartDate",startDate);
        model.addAttribute("dauEndDate",endDate);
        return "forward:/data";
    }

}
