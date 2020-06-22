package com.propets.lostfound.maps;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class MapsService {
    @Value("${google.maps.key}")
    private  String key ;
    
   
    public MapResult getLocationByCoordinates(double[]coordinates) {
        GeoApiContext context=new GeoApiContext.Builder().apiKey(key).build();
        GeocodingResult[] results= new GeocodingResult[0];
        try {
            results = GeocodingApi.reverseGeocode(context,new LatLng(coordinates[1],coordinates[0])).await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        MapResult mapResult=new MapResult();
        double[]coord=new double[2];
        for (GeocodingResult result : results) {
            coord[0]=result.geometry.location.lng;
            coord[1]=result.geometry.location.lat;
            mapResult.setAddress(result.formattedAddress.toString());
            mapResult.setCoordinates(coord);
        }
        context.shutdown();
        return mapResult;
    }
    
    public MapResult getLocationByAddress(String address) {
        GeoApiContext context=new GeoApiContext.Builder().apiKey(key).build();
        GeocodingResult[] results= new GeocodingResult[0];
        try {
            results = GeocodingApi.geocode(context,address).await();
        } catch (Exception e) {
            e.printStackTrace();
        } 
        MapResult mapResult=new MapResult();
        double[]coordinates=new double[2];
        for (GeocodingResult result : results) {
            coordinates[0]=result.geometry.location.lng;
            coordinates[1]=result.geometry.location.lat;
            mapResult.setAddress(result.formattedAddress.toString());
            mapResult.setCoordinates(coordinates);
        }
        context.shutdown();
       return mapResult;
    }
}
