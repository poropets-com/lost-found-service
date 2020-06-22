package com.propets.lostfound.service.interfaces;

import com.propets.lostfound.dto.requestDto.SearchByFilterRequestDto;
import com.propets.lostfound.dto.responseDto.PostPageableResponseDto;
import com.propets.lostfound.dto.requestDto.PostRequestDto;
import com.propets.lostfound.dto.responseDto.PostResponseDto;

import java.util.List;

public interface IPostService {
    PostResponseDto addPost(String email, PostRequestDto postRequestDto);
    PostPageableResponseDto getLostOrFoundPets(boolean isLost, int pageNumber, int pageSize);
    PostResponseDto getPost(long id);
    PostPageableResponseDto searchPostByFilters(int pageNumber, int pageSize, SearchByFilterRequestDto filterRequestDto);
    PostResponseDto updatePost(String email,long id,PostRequestDto postRequestDto);
    PostResponseDto deletePost(String email,long id);
}
