package io.quarkus.it.spring.boot;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class AppRoutesPropertiesTest {

    @Test
    void shouldHaveVersion() {
        when().get("/app-routes/version")
                .then()
                .body(is(equalTo("2.0")));
    }

    @Test
    void shouldHaveDisabledList() {
        when().get("/app-routes/disabled/size")
                .then()
                .body(is(equalTo("2")));

        when().get("/app-routes/disabled/0")
                .then()
                .body(is(equalTo("legacy-api")));

        when().get("/app-routes/disabled/1")
                .then()
                .body(is(equalTo("debug-endpoint")));
    }

    @Test
    void shouldHaveUserServiceRoute() {
        when().get("/app-routes/routes/user-service/path")
                .then()
                .body(is(equalTo("/api/users")));

        when().get("/app-routes/routes/user-service/timeout")
                .then()
                .body(is(equalTo("30")));

        when().get("/app-routes/routes/user-service/methods/size")
                .then()
                .body(is(equalTo("2")));

        when().get("/app-routes/routes/user-service/methods/0")
                .then()
                .body(is(equalTo("GET")));
    }

    @Test
    void shouldHaveOrderServiceRoute() {
        when().get("/app-routes/routes/order-service/path")
                .then()
                .body(is(equalTo("/api/orders")));

        when().get("/app-routes/routes/order-service/timeout")
                .then()
                .body(is(equalTo("60")));

        when().get("/app-routes/routes/order-service/methods/size")
                .then()
                .body(is(equalTo("3")));
    }
}
