package com.sas.saveandsound.controllers.rest;

import com.sas.saveandsound.models.SoundEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/SAS")
public class SasRestControllers {

//    @GetMapping("/search")
//    public SoundEntity search(
//            @RequestParam(name = "query", required = false, defaultValue = "") String query,
//            Model model) {
//        if (query.isEmpty()) {
//            return null;
//        }
//        return new SoundEntity(query, "query");
//    }

//    private final SoundEntity[] sounds = new SoundEntity[]{
//        new SoundEntity( "a1", "A1", "A2"),
//        new SoundEntity( "a1", "B2"),
//        new SoundEntity( "b1", "A1")
//    };
//
//    @GetMapping("/search")
//    public SoundEntity search(
////        @RequestParam(value = "name", defaultValue = "") String name,
////        @RequestParam(value = "creator", defaultValue = "") String[] creator,
//        @RequestParam(value = "query", defaultValue = "", required = false) String query) {
////        for (SoundEntity sound : sounds) {
////            if ((sound.getName().equals(name) || name.isEmpty()) &&
////                    (Arrays.equals(sound.getCreator(), creator) || creator.length == 0)) {
////                return sound;
////            }
////        }
//        for (SoundEntity sound : sounds) {
//            if ((sound.getName().equals(query) || query.isEmpty())) {
//                return sound;
//            }
//        }
//        return null;
//    }
//
//    @GetMapping("/search/{userName}")
//    public String searchWithPathVariable(@PathVariable String userName) {
//        return "Hi, " + userName + "! Chang for test commit.";
//    }
}
