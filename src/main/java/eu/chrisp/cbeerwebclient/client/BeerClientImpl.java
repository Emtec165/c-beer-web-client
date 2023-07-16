package eu.chrisp.cbeerwebclient.client;

import eu.chrisp.cbeerwebclient.model.BeerDto;
import eu.chrisp.cbeerwebclient.model.BeerPagedList;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

import static eu.chrisp.cbeerwebclient.config.WebClientProperties.BEER_V1_PATH;
import static eu.chrisp.cbeerwebclient.config.WebClientProperties.BEER_V1_PATH_GET_BY_ID;
import static eu.chrisp.cbeerwebclient.config.WebClientProperties.BEER_V1_UPC_PATH;

@Service
@RequiredArgsConstructor
class BeerClientImpl implements BeerClient {

    private final WebClient webClient;

    @Override
    public Mono<BeerPagedList> listBeers(Integer pageNumber, Integer pageSize, String beerName, String beerStyle, Boolean showInventoryOnHand) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(BEER_V1_PATH)
                        .queryParamIfPresent("pageNumber", Optional.ofNullable(pageNumber))
                        .queryParamIfPresent("pageSize", Optional.ofNullable(pageSize))
                        .queryParamIfPresent("beerName", Optional.ofNullable(beerName))
                        .queryParamIfPresent("beerStyle", Optional.ofNullable(beerStyle))
                        .queryParamIfPresent("showInventoryOnHand", Optional.ofNullable(showInventoryOnHand))
                        .build()
                )
                .retrieve()
                .bodyToMono(BeerPagedList.class);
    }

    @Override
    public Mono<BeerDto> getBeerById(UUID id, Boolean showInventoryOnHand) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(BEER_V1_PATH_GET_BY_ID)
                        .queryParamIfPresent("showInventoryOnHand", Optional.ofNullable(showInventoryOnHand))
                        .build(id))
                .retrieve()
                .bodyToMono(BeerDto.class);
    }

    @Override
    public Mono<BeerDto> getBeerByUPC(String upc) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(BEER_V1_UPC_PATH).build(upc))
                .retrieve()
                .bodyToMono(BeerDto.class);
    }

    @Override
    public Mono<ResponseEntity<Void>> createBeer(BeerDto beerDto) {
        return webClient.post()
                .uri(BEER_V1_PATH)
                .bodyValue(beerDto)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public Mono<ResponseEntity<Void>> updateBeer(UUID id, BeerDto beerDto) {
        return webClient.put()
                .uri(BEER_V1_PATH + "/" + id)
                .bodyValue(beerDto)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteBeerById(UUID id) {
        return webClient.delete()
                .uri(BEER_V1_PATH + "/" + id)
                .retrieve()
                .toBodilessEntity();
    }
}
