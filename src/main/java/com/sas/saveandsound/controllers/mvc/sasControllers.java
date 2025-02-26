package com.sas.saveandsound.controllers.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/SAS")
public class SaveAndSoundController {

    @GetMapping("")
    public String sasEndpoint() {
        return "index";
    }

    @GetMapping("/search")
    public String search(
        @RequestParam(name = "query", required = false, defaultValue = "") String query,
        Model model) {
        if (query.isEmpty()) {
            return "index";
        }
        model.addAttribute("name", query);
        return "search";
    }
    /*
    private final Sound[] sounds = new Sound[]{
        new Sound( "a1", "A1", "A2"),
        new Sound( "a1", "B2"),
        new Sound( "b1", "A1")
    };

    @GetMapping("/search")
    public Sound search(
            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "creator", defaultValue = "") String[] creator) {
        for (Sound sound : sounds) {
            if ((sound.getName().equals(name) || name.isEmpty()) &&
                    (Arrays.equals(sound.getCreator(), creator) || creator.length == 0)) {
                return sound;
            }
        }
        return null;
    }

    @GetMapping("/search/{userName}")
    public String searchWithPathVariable(@PathVariable String userName) {
        return "Hi, " + userName + "! Chang for test commit.";
    }

 */
}
