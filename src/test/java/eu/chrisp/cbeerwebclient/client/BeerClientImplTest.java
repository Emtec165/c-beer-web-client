package eu.chrisp.cbeerwebclient.client;

import eu.chrisp.cbeerwebclient.config.WebClientConfig;
import eu.chrisp.cbeerwebclient.model.BeerDto;
import eu.chrisp.cbeerwebclient.model.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BeerClientImplTest {

    BeerClientImpl beerClient;

    @BeforeEach
    void setUp() {
        beerClient = new BeerClientImpl(new WebClientConfig().webClient());
    }

    @Test
    void listBeers() {
//        Given & When
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null, null, null, null);

//        Then
        BeerPagedList pagedList = beerPagedListMono.block();
        assertThat(pagedList).isNotNull();
        assertThat(pagedList.getContent().size()).isGreaterThan(0);
    }

    @Test
    void listBeersPageSize10() {
//        Given & When
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(1, 10, null, null, null);

//        Then
        BeerPagedList pagedList = beerPagedListMono.block();
        assertThat(pagedList).isNotNull();
        assertThat(pagedList.getContent().size()).isEqualTo(10);
    }

    @Test
    void listBeersPageSize20() {
//        Given & When
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(10, 20, null, null, null);

//        Then
        BeerPagedList pagedList = beerPagedListMono.block();
        assertThat(pagedList).isNotNull();
        assertThat(pagedList.getContent().size()).isEqualTo(0);
    }

    @Test
    void functionalTestGetBeerById() throws InterruptedException {

        AtomicReference<String> beerName = new AtomicReference<>();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        beerClient.listBeers(0, 1, null, null, null)
                .map(beerPagedList -> beerPagedList.getContent().get(0).getId())
                .map(beerId -> beerClient.getBeerById(beerId, false))
                .flatMap(mono -> mono)
                .subscribe(beerDto -> {
                    beerName.set(beerDto.getBeerName());
                    countDownLatch.countDown();
                });

        countDownLatch.await();
        assertThat(beerName.get()).isEqualTo("Mango Bobs");
    }

    @Test
    void getBeerById() {
//        Given
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(0, 1, null, null, null);
        UUID id = beerPagedListMono.block().getContent().get(0).getId();

//        When
        Mono<BeerDto> beerDtoMono = beerClient.getBeerById(id, false);

//        Then
        BeerDto beerDto = beerDtoMono.block();
        assertThat(beerDto).isNotNull();
        assertThat(beerDto.getId()).isEqualTo(id);
    }

    @Test
    void getBeerByUPC() {
//        Given
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(0, 1, null, null, null);
        String upc = beerPagedListMono.block().getContent().get(0).getUpc();

//        When
        Mono<BeerDto> beerDtoMono = beerClient.getBeerByUPC(upc);

//        Then
        BeerDto beerDto = beerDtoMono.block();
        assertThat(beerDto).isNotNull();
        assertThat(beerDto.getUpc()).isEqualTo(upc);
    }

    @Test
    void createBeer() {
//        Given
        BeerDto beerDto = BeerDto.builder()
                .beerName("Dogfishhead 90 Min IPA")
                .beerStyle("IPA")
                .upc("213123412")
                .price(BigDecimal.TEN)
                .build();

//        When
        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.createBeer(beerDto);

//        Then
        ResponseEntity responseEntity = responseEntityMono.block();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String location = responseEntity.getHeaders().get("Location").get(0);
        assertThat(location).isNotBlank();
    }

    @Test
    void updateBeer() {
//        Given
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(0, 1, null, null, null);
        UUID id = beerPagedListMono.block().getContent().get(0).getId();

        BeerDto beerDto = BeerDto.builder()
                .beerName("Ala Ma Kota")
                .beerStyle("GOSE")
                .upc("12232323")
                .price(BigDecimal.TEN)
                .build();
//        When
        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.updateBeer(id, beerDto);

//        Then
        ResponseEntity responseEntity = responseEntityMono.block();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void deleteBeerById() {
//        Given
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(0, 1, null, null, null);
        UUID id = beerPagedListMono.block().getContent().get(0).getId();

//        When
        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.deleteBeerById(id);

//        Then
        ResponseEntity<Void> responseEntity = responseEntityMono.block();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void deleteBeerById_notFound() {
//        Given
        UUID id = UUID.randomUUID();

//        When & Then
        assertThrows(WebClientResponseException.class, () -> {
            Mono<ResponseEntity<Void>> responseEntityMono = beerClient.deleteBeerById(id);
            ResponseEntity<Void> responseEntity = responseEntityMono.block();
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        });
    }

    @Test
    void deleteBeerById_handleException() {
//        Given
        UUID id = UUID.randomUUID();

//        When
        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.deleteBeerById(id);

//        Then
        ResponseEntity<Void> responseEntity = responseEntityMono
                .onErrorResume(throwable -> {
                    if (throwable instanceof WebClientResponseException) {
                        WebClientResponseException exception = (WebClientResponseException) throwable;
                        return Mono.just(ResponseEntity.status(exception.getStatusCode()).build());
                    } else {
                        throw new RuntimeException(throwable);
                    }
                })
                .block();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}