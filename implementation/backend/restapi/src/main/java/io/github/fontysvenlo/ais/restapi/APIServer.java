package io.github.fontysvenlo.ais.restapi;

import static io.javalin.apibuilder.ApiBuilder.crud;

import io.javalin.Javalin;
import io.github.fontysvenlo.ais.businesslogic.api.BusinessLogic;
import io.javalin.http.Context;
import org.springframework.web.bind.annotation.CrossOrigin;


import java.util.Map;
@CrossOrigin(origins = "http://localhost")

public class APIServer {

    private final BusinessLogic businessLogic;

    public APIServer(BusinessLogic businessLogic) {
        this.businessLogic = businessLogic;
    }

    public void start(ServerConfig configuration) {
        var app = Javalin.create(config -> {
            config.router.contextPath = "/api/v1";
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.allowHost("http://localhost:" + configuration.cors(), "127.0.0.1:" + configuration.cors());

                });
            });
            config.router.apiBuilder(() -> {
                crud("customers/{customer-id}", new CustomerResource(businessLogic.getCustomerManager()));
            });
        });
        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            ctx.status(422).json(Map.of("error", e.getMessage()));
        });

        app.start(configuration.port());
    }

}