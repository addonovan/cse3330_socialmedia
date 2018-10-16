package edu.uta.cse3330.socialmediasite.controller;

import edu.uta.cse3330.socialmediasite.DatabaseDriver;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Home {

    @GetMapping("/")
    public String list(Model model) {
        System.out.println("list() called!");

        model.addAttribute("profiles", DatabaseDriver.listProfiles());
        return "profile_list";
    }


}
