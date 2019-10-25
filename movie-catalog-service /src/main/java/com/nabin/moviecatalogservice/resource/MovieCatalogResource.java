package com.nabin.moviecatalogservice.resource;

import com.nabin.moviecatalogservice.model.CatalogItem;
import com.nabin.moviecatalogservice.model.Movie;
import com.nabin.moviecatalogservice.model.Rating;
import com.nabin.moviecatalogservice.model.UserRating;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/catalog")
public class MovieCatalogResource {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WebClient.Builder webClientBuilder;
    @RequestMapping("/{userId}")
    @HystrixCommand(fallbackMethod = "getFallbackCatalog")
    public List<CatalogItem> getCatalog(@PathVariable("userId") String userId){
       // WebClient.Builder builder = WebClient.builder();
        UserRating ratings = restTemplate.getForObject("http://movie-rating-service/ratingsdata/"+userId, UserRating.class);
        return ratings.getUserRatings().stream().map(rating -> {
            //For each movie id, call movie info service and get details
            Movie movie = restTemplate.getForObject("http://movie-info-service/movies/"+rating.getMovieId(), Movie.class);
            System.out.println(movie);
//          Movie movie =  webClientBuilder.build()
//                    .get()
//                    .uri("http://localhost:8087/movies/"+rating.getMovieId(),String.class)
//                    .retrieve()
//                    .bodyToMono(Movie.class)
//                    .block();
            //put them all together
           return new CatalogItem(movie.getName(), "Test Desc",rating.getRating());
        })
                .collect(Collectors.toList());

    }

    public List<CatalogItem> getFallbackCatalog(@PathVariable("userId") String userId){
        return Arrays.asList(new CatalogItem("No movie","",0));
    }
}
