package com.propets.lostfound.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class PostPageableResponseDto {
    private int itemsOnPage;
    private int currentPage;
    private int itemsTotal;
    private List<PostResponseDto>posts;
}
