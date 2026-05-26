package com.example.OFFUPI.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashbordController
{
    @GetMapping("/")
    public String dashboard(){
        return "Dashboard";
    }
}
