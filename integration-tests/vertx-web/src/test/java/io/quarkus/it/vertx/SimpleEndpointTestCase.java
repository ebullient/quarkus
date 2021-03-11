package io.quarkus.it.vertx;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(SimpleEndpoint.class)
public class SimpleEndpointTestCase {

    @Test
    public void testEndpoint() throws Exception {
        when().get("/person").then().statusCode(200)
                .body("name", is("Jan"))
                .header("content-type", "application/json");
        when().get("/pet").then().statusCode(200)
                .body("name", is("Jack"))
                .header("content-type", "application/json");
        when().get("/pong").then().statusCode(200)
                .body("name", is("ping"))
                .header("content-type", "application/json");
        given().body("{\"name\":\"pi\"}").post("/data").then().statusCode(200)
                .body("name", is("pipi"))
                .header("content-type", "application/json");
        when().get("/item/123").then().statusCode(200)
                .body(equalTo("message with id 123"));
        when().get("/item/1/123").then().statusCode(200)
                .body(equalTo("message with id 1 and sub 123"));

        // Relative path to metrics endpoint as TestHttpEndpoint scopes everything to /simple
        when().get("../q/metrics").then().statusCode(200)
                .body(containsString(
                        "http_server_requests_seconds_count{method=\"GET\",outcome=\"SUCCESS\",status=\"200\",uri=\"/simple/item/{id}/{sub}\""))
                .body(containsString(
                        "http_server_requests_seconds_count{method=\"GET\",outcome=\"SUCCESS\",status=\"200\",uri=\"/simple/item/{id}\""));
        when().get("../q/metrics").then()
                .statusCode(200)
                .body(containsString(
                        "http_server_requests_seconds_count{method=\"GET\",outcome=\"SUCCESS\",status=\"200\",uri=\"/q/metrics\""));
    }

}
