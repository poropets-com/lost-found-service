package com.propets.lostfound.service;

import com.propets.lostfound.dto.LocationDto;
import com.propets.lostfound.dto.imaggaApi.colorsApi.ColorsApiResult;
import com.propets.lostfound.dto.imaggaApi.tagsApi.TagsApiResult;
import com.propets.lostfound.dto.requestDto.PostRequestDto;
import com.propets.lostfound.dto.requestDto.SearchByFilterRequestDto;
import com.propets.lostfound.dto.responseDto.PostPageableResponseDto;
import com.propets.lostfound.dto.responseDto.PostResponseDto;
import com.propets.lostfound.kafka.service.KafkaProducer;
import com.propets.lostfound.maps.MapResult;
import com.propets.lostfound.maps.MapsService;
import com.propets.lostfound.model.Location;
import com.propets.lostfound.model.PostEntity;
import com.propets.lostfound.model.SequenceId;
import com.propets.lostfound.repo.PostRepository;
import com.propets.lostfound.repo.SequenceIdRepository;
import com.propets.lostfound.service.interfaces.IImaggaService;
import com.propets.lostfound.service.interfaces.IPostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class PostService implements IPostService , IImaggaService {


    private static final String LOST ="LOST" ;
    private static final String FOUND = "FOUND";
    private static final String ADD_ACTION ="add" ;
    private static final String UPDATE_ACTION ="update" ;
    private static final String DELETE_ACTION ="delete" ;
    private final SequenceIdRepository sequenceIdRepository;
    private final PostRepository postRepository;
    private final KafkaProducer kafkaProducer;
    private final MapsService mapsService;

    @Autowired
    public PostService(SequenceIdRepository sequenceIdRepository, PostRepository postRepository, KafkaProducer kafkaProducer, MapsService mapsService) {
        this.sequenceIdRepository = sequenceIdRepository;
        this.postRepository = postRepository;
        this.kafkaProducer = kafkaProducer;
        this.mapsService = mapsService;
    }

    @Override
    public PostResponseDto addPost(String email, PostRequestDto request) {
        long id=getNextId();
        LocalDate postDate=LocalDate.now();
        String postType=request.getTypePost().equalsIgnoreCase("lost")?LOST:FOUND;
        Location location=getLocation(request);
        List<String>images=request.getPhotos()==null?new ArrayList<>():request.getPhotos();
        List<String>tags=request.getTags()==null?new ArrayList<>():request.getTags();
        PostEntity postEntity=new PostEntity(id,postType,request.getType(),location,images,tags,email,postDate);
        postRepository.save(postEntity);
        log.info("IN addPost- user {} successfully add new post id {}",email,id);
        
        kafkaProducer.sendPost(ADD_ACTION,id,postType,tags);
        return toPostResponseDto(postEntity);
    }

    private PostResponseDto toPostResponseDto(PostEntity postEntity) {
        LocationDto locationDto= toLocationDto(postEntity.getLocation());
        return new PostResponseDto(postEntity.getId(),
                postEntity.getPostType(),
                postEntity.getType(),
                locationDto,
                postEntity.getImages(),
                postEntity.getTags(),
                postEntity.getEmail(),
                postEntity.getPostDate());
    }

    private LocationDto toLocationDto(Location location) {
        String[]address=location.getAddress().split(",");
        String[]streetBuilding=address[0].split(" ");
        String building = streetBuilding[2];
        String street=streetBuilding[0]+" "+streetBuilding[1];
        String city=address[1].trim();
        String country=address[2].trim();
        return new LocationDto(country,city,street,building,location.getCoordinates()[0],location.getCoordinates()[1]);
    }

    private Location getLocation(PostRequestDto request) {
        String address=getStringAddress(request);
        double[]coordinates=new double[2];
        if ((request.getLocation().getLongitude()==0 || request.getLocation().getLatitude()==0)
                && address==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Location must include full address or coordinates");
        }
        if (address==null){
            coordinates[0]=request.getLocation().getLongitude();
            coordinates[1]=request.getLocation().getLatitude();
            MapResult res = mapsService.getLocationByCoordinates(coordinates);
            return new Location(res.getAddress(),res.getCoordinates());
        }
        if (request.getLocation().getLongitude()==0 || request.getLocation().getLatitude()==0){
            MapResult res = mapsService.getLocationByAddress(address);
            return new Location(res.getAddress(),res.getCoordinates());
        }
        coordinates[0]=request.getLocation().getLongitude();
        coordinates[1]=request.getLocation().getLatitude();
        return new Location(address,coordinates);
    }

    private String getStringAddress(PostRequestDto request) {
        if (request.getLocation().getCity()==null||request.getLocation().getCity().isEmpty()){
            return null;
        }
        StringBuilder sb=new StringBuilder();
        sb.append(request.getLocation().getBuilding()==null||request.getLocation().getBuilding().isEmpty()?"1 ":request.getLocation().getBuilding()+" ");
        sb.append(request.getLocation().getStreet()==null||request.getLocation().getStreet().isEmpty()?"Herzl, ":request.getLocation().getStreet()+", ");
        sb.append(request.getLocation().getCity()+", ");
        sb.append(request.getLocation().getCounty()==null||request.getLocation().getCounty().isEmpty()?" Israel":request.getLocation().getCounty());
        return sb.toString();
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public PostPageableResponseDto getLostOrFoundPets(boolean isLost,int pageNumber, int pageSize) {
        Pageable pageable=PageRequest.of(pageNumber,pageSize, Sort.by("postDate").descending());
        Page<PostEntity>posts=
                isLost?postRepository.findAllByPostType(LOST,pageable):postRepository.findAllByPostType(FOUND,pageable);
        if (posts.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Data not found");
        }
        return new PostPageableResponseDto(posts.getNumberOfElements(),
                pageNumber,
                (int) posts.getTotalElements(),
                toPostResponseDtoList(posts.get()));
    }

    private List<PostResponseDto> toPostResponseDtoList(Stream<PostEntity> postEntityStream) {
        return postEntityStream.map(p->toPostResponseDto(p)).collect(Collectors.toList());
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public PostResponseDto getPost(long id) {
        PostEntity postEntity=postRepository.findById(id).orElse(null);
        if (postEntity==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Data by id: "+id+" not found");
        }
        return toPostResponseDto(postEntity);
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public PostPageableResponseDto searchPostByFilters(int pageNumber, int pageSize,SearchByFilterRequestDto filterRequestDto) {
       //TODO
        Pageable pageable=PageRequest.of(pageNumber,pageSize,Sort.by("postDate").descending());
        Point point=new Point(filterRequestDto.getLocation().getLongitude(),filterRequestDto.getLocation().getLatitude());
        Distance distance=new Distance(filterRequestDto.getRadiusSearching(), Metrics.KILOMETERS);
        Page<PostEntity>postEntities=
                postRepository.findAllByPostTypeAndTypeAndCoordinatesNear(
                        filterRequestDto.getTypePost(),
                        filterRequestDto.getType(),
                        point,distance,pageable);
        return new PostPageableResponseDto(postEntities.getSize(),
                pageNumber,
                (int) postEntities.getTotalElements(),
                toPostResponseDtoList(postEntities.get()));
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public PostResponseDto updatePost(String email, long id, PostRequestDto postRequestDto) {
        PostEntity postEntity=postRepository.findById(id).orElse(null);
        if (postEntity==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Data  id: "+id+" not found");
        }
        if (!postEntity.getEmail().equals(email)){
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Only Author can update or delete post");
        }
        postEntity=updatePostData(postEntity,postRequestDto);
        postRepository.save(postEntity);
        return toPostResponseDto(postEntity);
    }

    private PostEntity updatePostData(PostEntity post, PostRequestDto updates) {
        if (!post.getPostType().equalsIgnoreCase(updates.getTypePost())){
            post.setPostType(updates.getTypePost());
        }
        post.setType(updates.getType());
        String address=getStringAddress(updates);
        if (!post.getLocation().getAddress().equalsIgnoreCase(address)||
                Double.compare(post.getLocation().getCoordinates()[0],updates.getLocation().getLongitude())!=0 ||
                Double.compare(post.getLocation().getCoordinates()[1],updates.getLocation().getLatitude())!=0 ){
            post.setLocation(getLocation(updates));
        }
        post.setImages(updates.getPhotos());
        if (!post.getTags().equals(updates.getTags())){
            post.setTags(updates.getTags());
            kafkaProducer.sendPost(UPDATE_ACTION,post.getId(),post.getPostType(),updates.getTags());
        }
        return post;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public PostResponseDto deletePost(String email, long id) {
        PostEntity postEntity=postRepository.findById(id).orElse(null);
        if (postEntity==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Data  id: "+id+" not found");
        }
        if (!postEntity.getEmail().equals(email)){
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Only Author can update or delete post");
        }
        kafkaProducer.sendPost(DELETE_ACTION,postEntity.getId(),postEntity.getPostType(),postEntity.getTags());
        SequenceId sequenceId = sequenceIdRepository.findById(1).orElse(null);
        sequenceId.getNextId().remove(postEntity.getId());
        postRepository.deleteById(postEntity.getId());
        return toPostResponseDto(postEntity);
    }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Value("${imagga.auth}")
    String imaggaAuth;
//    @Value("${imagga.limit}")
//    int imaggaLimit;
    @Value("${imagga.confidence}")
    double imaggaConfidence;
    //TODO inject language for imagga tag current implementation using default english 
    private static final String IMAGGA = "https://api.imagga.com/v2";
    private static final String COLORS="/colors?extract_overall_colors=0&image_url=";
    private static final String TAGS="/tags?image_url=";
    @Override
    public List<String> getTagsAndColors(String language,String imageUrl) {
        ResponseEntity<TagsApiResult>responseTags=(ResponseEntity<TagsApiResult>)getImaggaResponse(TagsApiResult.class,language,imageUrl);
        ResponseEntity<ColorsApiResult> responseColors= (ResponseEntity<ColorsApiResult>) getImaggaResponse(ColorsApiResult.class,language,imageUrl);
        if (!responseColors.getBody().status.type.equals("success") &&
                !responseTags.getBody().status.type.equals("success")){
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Image processing service unavailable ");
        }
        List<String>tags=getTags(responseTags,language);
        List<String>colors=getColors(responseColors);
        tags.addAll(colors);
        return tags;
    }

    private List<String> getColors(ResponseEntity<ColorsApiResult> responseColors) {
        if (!responseColors.getBody().status.type.equals("success")){
            return new ArrayList<>();
        }
        return Arrays.stream(responseColors.getBody().result.colors.foreground_colors).
                map(c->c.closest_palette_color_parent).collect(Collectors.toList());
    }

    private List<String> getTags(ResponseEntity<TagsApiResult> responseTags,String language) {
        if (!responseTags.getBody().status.type.equals("success")){
            return new ArrayList<>();
        }
        return Arrays.stream(responseTags.getBody().result.tags).filter(t->t.confidence>imaggaConfidence).
                map(t->t.tag.get("en")).collect(Collectors.toList());
    }

    private ResponseEntity<?> getImaggaResponse(Class<?> type,String language, String imageUrl) {
        RestTemplate restTemplate = new RestTemplate();
        RequestEntity<Void> requestColors= null;
        String endPoint=type.equals(ColorsApiResult.class)?IMAGGA+COLORS:IMAGGA+TAGS;
        try {
            requestColors = RequestEntity.get(new URI(endPoint+imageUrl)).
                    header("Authorization","Basic "+ imaggaAuth).
                    build();
        } catch (URISyntaxException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Check image url");
        }
        return restTemplate.exchange(requestColors, type);
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private long getNextId() {
        long lastId=1;
        SequenceId sequenceId=sequenceIdRepository.findById(1).orElse(null);
        if (sequenceId==null){
            sequenceId=new SequenceId(1,new ArrayList<>(Arrays.asList(lastId)));
            sequenceIdRepository.save(sequenceId);
            return lastId;
        }
        if (sequenceId.getNextId().size()>1){
            lastId=sequenceId.getNextId().get(1);
            sequenceId.getNextId().remove(1);
            sequenceIdRepository.save(sequenceId);
            return lastId;
        }
        lastId=sequenceId.getNextId().get(0)+1;
        sequenceId.getNextId().remove(0);
        sequenceId.getNextId().add(lastId);
        sequenceIdRepository.save(sequenceId);
        return lastId;
    }

}
