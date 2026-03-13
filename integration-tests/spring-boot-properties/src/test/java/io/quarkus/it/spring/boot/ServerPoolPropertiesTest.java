package io.quarkus.it.spring.boot;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ServerPoolPropertiesTest {

    @Test
    void shouldHaveDefaultServer() {
        when().get("/server-pool/default-server")
                .then()
                .body(is(equalTo("us-east-1")));
    }

    @Test
    void shouldHaveServerMapEntries() {
        when().get("/server-pool/servers/primary")
                .then()
                .body(is(equalTo("us-east-1")));

        when().get("/server-pool/servers/secondary")
                .then()
                .body(is(equalTo("eu-west-1")));

        when().get("/server-pool/servers/failover")
                .then()
                .body(is(equalTo("ap-south-1")));
    }

    @Test
    void shouldHaveCorrectServerMapSize() {
        when().get("/server-pool/servers/size")
                .then()
                .body(is(equalTo("3")));
    }

    @Test
    void shouldHaveLabelMapEntry() {
        when().get("/server-pool/labels/env")
                .then()
                .body(is(equalTo("production")));
    }
}
