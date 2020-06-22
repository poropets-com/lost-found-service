package com.propets.lostfound.dto.requestDto;

import com.propets.lostfound.dto.LocationDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PostRequestDto {
    private String typePost;
    private String type;
    private LocationDto location;
    private List<String>photos;
    private List<String>tags;
}
