package com.propets.lostfound.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LocationDto {
    private String county;
    private String city;
    private String street;
    private String building;
    private double longitude;
    private double latitude;
}
