package com.neo9.skills.reactor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class ReactorApplication {

	private static List<Integer> fillListBlocking() {
		List<Integer> res = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			res.add(i);
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return res;
	}

	public static void main(String[] args) {
		SpringApplication.run(ReactorApplication.class, args);
		Flux<String> alphaFLux = Flux.just("a", "b", "c", "d", "e");
		Flux<Integer> intFLux = Flux.just(1, 2, 3, 4, 5);


		// subscribeOn example
		Mono.just("Hello")
						.doFirst(() -> System.out.println("first Thread : " + Thread.currentThread().getName()))
						.doAfterTerminate(() -> System.out.println("after scheduler Thread : " + Thread.currentThread().getName()))
						.map(in -> fillListBlocking())
						.doOnNext(in -> in.forEach(System.out::println))
						.subscribeOn(Schedulers.boundedElastic())
						.subscribe();


		// publishOn example
		Mono.just("Hello")
						.doFirst(() -> System.out.println("first Thread : " + Thread.currentThread().getName()))
						.publishOn(Schedulers.boundedElastic())
						.doAfterTerminate(() -> System.out.println("after scheduler Thread : " + Thread.currentThread().getName()))
						.map(in -> fillListBlocking())
						.doOnNext(in -> in.forEach(System.out::println))
						.subscribe();

		// Zip example
		Flux.zip(alphaFLux, intFLux, (a, i) -> a + ":" + i)
						.doOnNext(System.out::println)
						.subscribe();

		// Merge example
		Flux.merge(alphaFLux.delayElements(Duration.of(5, ChronoUnit.MILLIS)), intFLux.delayElements(Duration.of(5, ChronoUnit.MILLIS)))
						.doOnNext(System.out::println)
						.subscribe();

		// Concat example
		Flux.concat(alphaFLux, intFLux)
						.doOnNext(System.out::println)
						.subscribe();

		// Filter example
		intFLux.filter(in -> in % 2 == 0)
						.doOnNext(System.out::println)
						.subscribe();

		// Mono error handler
		Mono.just("hello")
						.flatMap(in -> Mono.error(new IllegalArgumentException("Exceptions are good")))
						.doOnNext(out -> System.out.println("not allowed"))
						.doOnError(IllegalArgumentException.class, e -> System.out.println(e.getMessage()))
						.subscribe();
		Mono.just("hello")
						.map(in -> {
							throw new IllegalArgumentException("Exceptions are good");
						})
						.doOnNext(out -> System.out.println("next"))
						.doOnError(IllegalArgumentException.class, e -> System.out.println(e.getMessage()))
						.subscribe();

		// Mono flatMap example
		Mono<String> str1 = Mono.just("Neo9 Skillz");
		Mono.just("Hello")
						.flatMap(in -> str1)
						.doOnNext(System.out::println)
						.subscribe();


		// Flux map example
		Flux<Integer> squared = Flux.range(1, 50).map(x -> x * x);
		squared.subscribe(x -> System.out.print(x + ", "));
		// -> will display 1, 4, 9, 16, ..., 9801, 10000

		// Mono map example with empty
		Mono.just("Hello")
						.flatMap(in -> Mono.empty())
						//.defaultIfEmpty("Another try !")
						.doOnNext(System.out::println)
						.subscribe();

		// Mono map example
		Mono.just("Hello")
						.map(in -> in + " World !")
						.doOnNext(System.out::println)
						.subscribe();
	}

}
