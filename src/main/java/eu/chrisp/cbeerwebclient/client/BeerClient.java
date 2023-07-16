package eu.chrisp.cbeerwebclient.client;

import eu.chrisp.cbeerwebclient.model.BeerDto;
import eu.chrisp.cbeerwebclient.model.BeerPagedList;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BeerClient {

    Mono<BeerPagedList> listBeers(Integer pageNumber, Integer pageSize, String beerName, String beerStyle, Boolean showInventoryOnHand); //TODO: ASAP add parameters

    Mono<BeerDto> getBeerById(UUID id, Boolean showInventoryOnHand);

    Mono<BeerDto> getBeerByUPC(String upc);

    Mono<ResponseEntity<Void>> createBeer(BeerDto beerDto);

    Mono<ResponseEntity<Void>> updateBeer(UUID id, BeerDto beerDto);

    Mono<ResponseEntity<Void>> deleteBeerById(UUID id);

}
