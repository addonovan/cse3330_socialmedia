package com.addonovan.cse3330.controller;

import com.addonovan.cse3330.DatabaseDriver;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Home {

    @GetMapping("/")
    public String list(Model model) {
        model.addAttribute("profiles", DatabaseDriver.listProfiles());
        return "profile_list";
    }


}
