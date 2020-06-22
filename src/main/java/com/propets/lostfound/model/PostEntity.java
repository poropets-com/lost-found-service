package com.propets.lostfound.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "lost&found")
public class PostEntity {
    @Id
    private long id;
    @Indexed
    private String postType;
    @Indexed
    private String type;
    private Location location;
    private List<String>images;
    private List<String>tags;
    private String email;
    private LocalDate postDate;
    
}
