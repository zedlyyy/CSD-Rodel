package example;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class LedgerBenchmarks extends Simulation {

    HttpProtocolBuilder httpProtocol = http
        .baseUrl("https://localhost:8080")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json");

        ScenarioBuilder test = scenario("Create/Get Test")
        .exec(
        http("Create Account")
          .post("/endpoints/createaccount")
          .body(StringBody(
            "{\n" +
            "  \"accountId\": \"user1\",\n" +
            "  \"dataSigned\": \"something\",\n" +
            "  \"publicKey\": \"something\",\n" +
            "  \"timeStamp\": \"2025-05-07T12:00:00\"\n" +
            "}"
          )).asJson()
          .check(status().is(200))
    )
        .pause(1)
        .repeat(50).on(
            exec(
                http("Get Balance")
                  .get("/endpoints/getbalance")
                  .queryParam("accountId", "user1")
                  .queryParam("dataSigned", "something")
                  .queryParam("publicKey", "something")
                  .queryParam("timeStamp", "2025-05-07T12:00:00")
                  .check(status().is(200))
            )
        );

    {
        setUp(
            test.injectOpen(rampUsers(10).during(10))
        ).protocols(httpProtocol);
    }
}
