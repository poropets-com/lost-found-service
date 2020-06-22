package com.propets.lostfound.rest;

import com.propets.lostfound.service.interfaces.IImaggaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.propets.lostfound.api.ApiConstants.*;

@RestController
public class ImaggaController {
    @Autowired
    IImaggaService imaggaService;
    
    @GetMapping(value = "propets"+"/{lang}"+"/search/v1"+TAGS_COLORS)
    List<String>getTagsAndColors(@PathVariable String lang,
                                 @RequestParam String image_url){
        return imaggaService.getTagsAndColors(lang,image_url);
    }
}
