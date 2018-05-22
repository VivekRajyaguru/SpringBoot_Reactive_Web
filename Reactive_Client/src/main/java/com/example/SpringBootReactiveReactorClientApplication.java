package com.example;

import java.util.Date;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

@Log
@SpringBootApplication
public class SpringBootReactiveReactorClientApplication {

	@Bean
	WebClient client() {
		return WebClient.create("http://localhost:8080/movies");
	}
	
	@Bean
	CommandLineRunner runner(WebClient client) {
		return args -> {
			client.get()
				  .uri("")
				  .retrieve()
				  .bodyToFlux(Movie.class)
				  .filter(movie -> movie.getTitle().equals("Startrek"))
				  .flatMap(
						  movie -> client.get()
						  				 .uri("/{id}/events", movie.getId())
						  				 .retrieve().bodyToFlux(MovieEvent.class))
						  				 .subscribe(movieEvent -> log.info(movieEvent.toString()));
		};
	}
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactiveReactorClientApplication.class, args);
	}
}



@Data
@AllArgsConstructor
@NoArgsConstructor
class MovieEvent {
	private String movieId;
	private Date dateViewed;
	
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Movie {
	private String id;
	private String title;
}