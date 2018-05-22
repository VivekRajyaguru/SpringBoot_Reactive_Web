package com.example;

import java.time.Duration;
import java.util.Date;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpringBootReactiveReactorApplication {

	@Bean
	RouterFunction<ServerResponse> routerFunction(Moviehandler moviehandler) {
		return route(GET("/movies"), moviehandler::all)
							  .andRoute(GET("/movies/{id}"), moviehandler::byId)
							  .andRoute(GET("/movies/{id}/events"), moviehandler::event);
	}
	
	 @Bean
	  public WebFilter corsFilter() {
	    return (ServerWebExchange ctx, WebFilterChain chain) -> {
	      ServerHttpRequest request = ctx.getRequest();
	      if (CorsUtils.isCorsRequest(request)) {
	        ServerHttpResponse response = ctx.getResponse();
	        HttpHeaders headers = response.getHeaders();
	        headers.add("Access-Control-Allow-Origin", "*");
	        headers.add("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS ");
	        headers.add("Access-Control-Max-Age", "3600");
	        headers.add("Access-Control-Allow-Headers","*");
	        if (request.getMethod() == HttpMethod.OPTIONS) {
	          response.setStatusCode(HttpStatus.OK);
	          return Mono.empty();
	        }
	      }
	      return chain.filter(ctx);
	    };
	  }
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactiveReactorApplication.class, args);
	}
}



@Component
class Moviehandler {
	private final MovieService movieService;
	
	public Moviehandler(MovieService movieService) {
		this.movieService = movieService;
	}
	
	public Mono<ServerResponse> all(ServerRequest serverRequest) {
		return ServerResponse.ok().body(movieService.getAllMovies(), Movie.class);
	}
	
	public Mono<ServerResponse> byId(ServerRequest serverRequest) {
		return ServerResponse.ok().body(movieService.getMovieById(serverRequest.pathVariable("id")), Movie.class);
	}
	
	public Mono<ServerResponse> event(ServerRequest serverRequest) {
		return ServerResponse.ok().contentType(MediaType.TEXT_EVENT_STREAM)
				.body(movieService.getMovieEvent(serverRequest.pathVariable("id")), MovieEvent.class);
	}
	
	
}

@Service
class SampleData implements ApplicationRunner {

	private final MovieRepository movieRepo;
	
	SampleData(MovieRepository movieRepo) {
		this.movieRepo = movieRepo;
	}
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		Flux<Movie> movieFlux = Flux.just("Star-Wars", "Startrek", "Titanic", "Termintor", "Back to the future")
			.map(e -> new Movie(null, e))
			.flatMap(movieRepo::save);
		
		movieRepo.deleteAll()
				 .thenMany(movieFlux)
				 .thenMany(movieRepo.findAll())
				 .subscribe(System.out::println);
	}
	
}

@Service
class MovieService {
	
	private final MovieRepository movieRepo;
	
	public MovieService(MovieRepository movieRepo) {
		this.movieRepo = movieRepo;
	}
	
	public Flux<Movie> getAllMovies() {
		return this.movieRepo.findAll();
	}
	
	public Mono<Movie> getMovieById(String id) {
		return this.movieRepo.findById(id);
	}
	
	public Flux<MovieEvent> getMovieEvent(String movieId) {
		return Flux.<MovieEvent>generate(sink -> sink.next(new MovieEvent(movieId, new Date())))
				.delayElements(Duration.ofSeconds(1L));
	}
	
}

interface MovieRepository extends ReactiveMongoRepository<Movie, String> {
	Flux<Movie> findByTitle(String title);
}


/*@RestController
class MovieController {
	private final MovieService movieService;
	
	public MovieController(MovieService movieService) {
		this.movieService = movieService;
	}
	
	@GetMapping
	public Flux<Movie> all() {
		return this.movieService.getAllMovies();
	}
	
	@GetMapping("/{id}")
	public Mono<Movie> byId(@PathVariable("id") String id) {
		return this.movieService.getMovieById(id);
	}
	
	@GetMapping(value= "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<MovieEvent> events(@PathVariable("id") String id) {
		return this.movieService.getMovieEvent(id);
	}
}*/

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
class MovieEvent {
	private String movieId;
	private Date dateViewed;
	
}

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
class Movie {
	@Id
	private String id;
	
	private String title;
}
