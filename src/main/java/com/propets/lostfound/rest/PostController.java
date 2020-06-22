package com.propets.lostfound.rest;

import com.propets.lostfound.dto.requestDto.PostRequestDto;
import com.propets.lostfound.dto.requestDto.SearchByFilterRequestDto;
import com.propets.lostfound.dto.responseDto.PostPageableResponseDto;
import com.propets.lostfound.dto.responseDto.PostResponseDto;
import com.propets.lostfound.service.interfaces.IPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.propets.lostfound.api.ApiConstants.*;

@RestController
public class PostController {
    
    @Autowired
    private IPostService postService;
    
    @PostMapping(value = PREFIX+POST+"/{email}")
    PostResponseDto addNewLost(@PathVariable String email,
                               @RequestBody PostRequestDto requestDto){
        return postService.addPost(email,requestDto);
    }
    
    @GetMapping(value = PREFIX+LOSTS)
    PostPageableResponseDto getLostPets(@RequestParam int pageNumber,
                                        @RequestParam int pageSize){
        return postService.getLostOrFoundPets(true,pageNumber,pageSize);
    }
    @GetMapping(value = PREFIX+FINDS)
    PostPageableResponseDto getFoundPets(@RequestParam int pageNumber,
                                        @RequestParam int pageSize){
        return postService.getLostOrFoundPets(false,pageNumber,pageSize);
    }
    @GetMapping(value = PREFIX+"/{id}")
    PostResponseDto getPostById(@PathVariable long id){
        return postService.getPost(id);
    }
    
    @PostMapping(value = PREFIX+POSTS+FILTER)
    PostPageableResponseDto searchLostByFilters(@RequestParam int pageNumber,
                                                @RequestParam int pageSize,
                                                @RequestBody SearchByFilterRequestDto requestDto){
        return postService.searchPostByFilters(pageNumber,pageSize,requestDto);
    }
    
    @PutMapping(value = PREFIX+"/{email}"+"/{id}")
    PostResponseDto updatePost(@PathVariable String email,
                               @PathVariable long id,
                               @RequestBody PostRequestDto requestDto){
        return postService.updatePost(email,id,requestDto);
    }


    @DeleteMapping(value = PREFIX+"/{email}"+"/{id}")
    PostResponseDto deletePost(@PathVariable String email,
                               @PathVariable long id){
        return postService.deletePost(email,id);
    }
    
}
