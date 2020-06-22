package com.propets.lostfound.dto.responseDto;

import com.propets.lostfound.dto.LocationDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PostResponseDto {
    private long id;
    private String typePost;
    private String type;
    private LocationDto location;
    private List<String> photos;
    private List<String>tags;
    private String user;
    private LocalDate datePost;
}
