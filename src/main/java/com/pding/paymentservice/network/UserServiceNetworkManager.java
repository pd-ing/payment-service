package com.pding.paymentservice.network;

import com.pding.paymentservice.payload.net.GetUserWithStripeIdResponseNet;
import com.pding.paymentservice.payload.net.GetUsersResponseNet;
import com.pding.paymentservice.payload.net.PublicUserNet;
import com.pding.paymentservice.payload.net.PublicUserWithStripeIdNet;
import com.pding.paymentservice.security.AuthHelper;
import com.pding.paymentservice.security.jwt.JwtUtils;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UserServiceNetworkManager {

    private final WebClient webClient;

    @Value("${service.user.host}")
    private String userService;

    @Value("${service.backend.host}")
    private String backendHost;

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${service.user.host.admin}")
    private String userServiceAdmin;
    private final OkHttpClient client;
    @Autowired
    private AuthHelper authHelper;

    @Autowired
    public UserServiceNetworkManager(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
        this.client = new OkHttpClient();
    }

    public Flux<PublicUserNet> getUsersListFlux(List<String> userIds) throws Exception {
        if (userIds == null || userIds.isEmpty()) return Flux.empty();

        String param = userIds.stream().map(Object::toString).collect(Collectors.joining(","));
        return webClient.get()
                .uri(userService + "/usersList?userIds=" + param)
                .retrieve()
                .bodyToMono(GetUsersResponseNet.class)
                .flatMapMany(resp -> {
                    if (resp.getError() == null && resp.getResult() != null) {
                        Map<String, PublicUserNet> userMap = resp.getResult().stream()
                                .collect(Collectors.toMap(PublicUserNet::getId, Function.identity()));

                        return Flux.fromIterable(userIds)
                                .map(userId -> {
                                    PublicUserNet user = userMap.get(userId);
                                    if (user == null) {
                                        return new PublicUserNet(userId, null, null, null, null, null, null, null, null, null, null, null, null, null);
                                    }
                                    return user;
                                });
                    } else {
                        return Flux.fromIterable(userIds)
                                .map(userId -> {
                                    // Return null for each userId in case of error
                                    return null;
                                });
                    }
                })
                .filter(Objects::nonNull);
    }

    public Flux<PublicUserNet> getUsersListByEmailFlux(List<String> emails) throws Exception {
        if (emails == null || emails.isEmpty()) return Flux.empty();

        String param = emails.stream().map(Object::toString).collect(Collectors.joining(","));
        return webClient.get()
                .uri(userService + "/usersListByEmail?emails=" + param)
                .retrieve()
                .bodyToMono(GetUsersResponseNet.class)
                .flatMapMany(resp -> {
                    if (resp.getError() == null && resp.getResult() != null) {
                        Map<String, PublicUserNet> userMap = resp.getResult().stream()
                                .collect(Collectors.toMap(PublicUserNet::getEmail, Function.identity()));
                        return Flux.fromIterable(emails)
                                .map(userMap::get);
                    } else {
                        return Flux.empty();
                    }
                });
    }

    public Flux<List<PublicUserNet>> getUsersListFlux(Set<String> userIds) throws Exception {
        if (userIds == null || userIds.isEmpty()) return Flux.empty();

        String param = userIds.stream().map(Object::toString).collect(Collectors.joining(","));
        return webClient.get()
                .uri(userService + "/usersList?userIds=" + param)
                .retrieve()
                .bodyToMono(GetUsersResponseNet.class)
                .flatMapMany(resp -> {
                    if (resp.getError() == null && resp.getResult() != null) {
                        return Flux.just(resp.getResult())
                                .flatMapIterable(list -> list);
                    } else {
                        return Flux.empty();
                    }
                })
                .collectList()
                .flux();
    }

    public Flux<PublicUserWithStripeIdNet> getUsersListWithStripeFlux(List<String> userIds) throws Exception {
        if (userIds == null || userIds.isEmpty()) return Flux.empty();

        String param = userIds.stream().map(Object::toString).collect(Collectors.joining(","));
        return webClient.get()
                .uri(userServiceAdmin + "/publicUserWithStripeId?userIds=" + param)
                .retrieve()
                .bodyToMono(GetUserWithStripeIdResponseNet.class)
                .flatMapMany(resp -> {
                    if (resp.getErrorResponse() == null && resp.getPublicUserInfoWithStripeId() != null) {
                        return Flux.fromIterable(resp.getPublicUserInfoWithStripeId());
                    } else {
                        return Flux.empty();
                    }
                });
    }

    public Mono<Boolean> isExchangeAllowedWithW8BenDocument() throws Exception {
        return webClient.get()
                .uri(backendHost + "/api/user/w8ben/isExchangeAllowedWithW8BenDocument")
            .header("Authorization", "Bearer " + authHelper.getIdToken())
                .retrieve()
                .bodyToMono(Boolean.class);
    }
}
