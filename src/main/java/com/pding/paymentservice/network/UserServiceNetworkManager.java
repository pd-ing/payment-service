package com.pding.paymentservice.network;

import com.pding.paymentservice.payload.net.GetUserWithStripeIdResponseNet;
import com.pding.paymentservice.payload.net.GetUsersResponseNet;
import com.pding.paymentservice.payload.net.PublicUserNet;
import com.pding.paymentservice.payload.net.PublicUserWithStripeIdNet;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserServiceNetworkManager {

    private final WebClient webClient;

    @Value("${service.user.host}")
    private String userService;

    @Value("${service.user.host.admin}")
    private String userServiceAdmin;
    private final OkHttpClient client;

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
                        return Flux.fromIterable(resp.getResult());
                    } else {
                        return Flux.empty();
                    }
                });
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
                        return Flux.fromIterable(resp.getResult());
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
                        return Flux.just(resp.getResult());
                    } else {
                        return Flux.empty();
                    }
                });
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
}
