//package com.sas.saveandsound.controllers.mvc;
//
//import com.sas.saveandsound.models.SoundEntity;
//import com.sas.saveandsound.service.SoundService;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//@Controller
//@RequestMapping("/SAS")
//public class SasControllers {
//
//    private final SoundService soundService;
//
//    public SasControllers(SoundService soundService) {
//        this.soundService = soundService;
//    }
//
//    @GetMapping("/search")
//    public String search(@RequestParam(value = "query", required = false, defaultValue = "") String query, Model model) {
//        SoundEntity result = soundService.search(query);
//        if (result != null) {
//            model.addAttribute("soundEntity", result);
//            return "search"; // Показываем страницу search.html
//        }
//        return "index"; // Если не найдено, вернем главную
//    }
//
//    @GetMapping("")
//    public String sasEndpoint() {
//        return "index";
//    }
//
//}
