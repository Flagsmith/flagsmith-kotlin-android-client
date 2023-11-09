package com.flagsmith.mockResponses

import com.flagsmith.entities.Trait
import com.flagsmith.mockResponses.endpoints.FlagsEndpoint
import com.flagsmith.mockResponses.endpoints.IdentityFlagsAndTraitsEndpoint
import com.flagsmith.mockResponses.endpoints.TraitsEndpoint
import org.mockserver.integration.ClientAndServer
import org.mockserver.matchers.Times
import org.mockserver.model.HttpError
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.MediaType
import java.util.concurrent.TimeUnit

enum class MockEndpoint(val path: String, val body: String) {
    GET_IDENTITIES(IdentityFlagsAndTraitsEndpoint("").path, MockResponses.getIdentities),
    GET_FLAGS(FlagsEndpoint.path, MockResponses.getFlags),
    SET_TRAIT(TraitsEndpoint(Trait(key = "", value = ""), "").path, MockResponses.setTrait)
}

fun ClientAndServer.mockResponseFor(endpoint: MockEndpoint) {
    `when`(request().withPath(endpoint.path), Times.once())
        .respond(
            response()
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(endpoint.body)
        )
}

fun ClientAndServer.mockDelayFor(endpoint: MockEndpoint) {
    `when`(request().withPath(endpoint.path), Times.once())
        .respond(
            response()
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(endpoint.body)
                .withDelay(TimeUnit.SECONDS, 8) // REQUEST_TIMEOUT_SECONDS is 4 in the client, so needs to be more
        )
}

fun ClientAndServer.mockFailureFor(endpoint: MockEndpoint) {
    `when`(request().withPath(endpoint.path), Times.once())
        .respond(
            response()
                .withStatusCode(500)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody("{error: \"Internal Server Error\"}")
        )
    Times.once()
}

fun ClientAndServer.mockDropConnection(endpoint: MockEndpoint) {
    `when`(request().withPath(endpoint.path), Times.once())
        .error(
            HttpError.error()
                .withDropConnection(true)
        )

}

object MockResponses {
    val getIdentities = """
        {
          "flags": [
            {
              "feature_state_value": null,
              "feature": {
                "type": "STANDARD",
                "name": "no-value",
                "id": 35506
              },
              "enabled": true
            },
            {
              "feature_state_value": 756,
              "feature": {
                "type": "STANDARD",
                "name": "with-value",
                "id": 35507
              },
              "enabled": true
            },
            {
              "feature_state_value": "",
              "feature": {
                "type": "STANDARD",
                "name": "with-value-just-person-enabled",
                "id": 35508
              },
              "enabled": true
            }
          ],
          "traits": [
            {
              "trait_value": "12345",
              "trait_key": "set-from-client"
            },
            {
              "trait_value": "electric pink",
              "trait_key": "favourite-colour"
            }
          ]
        }
    """.trimIndent()

    val getFlags = """
        [
          {
            "enabled": true,
            "feature": {
              "type": "STANDARD",
              "id": 35506,
              "name": "no-value"
            },
            "feature_state_value": null
          },
          {
            "enabled": true,
            "feature": {
              "type": "STANDARD",
              "id": 35507,
              "name": "with-value"
            },
            "feature_state_value": 7
          },
          {
            "enabled": false,
            "feature": {
              "type": "STANDARD",
              "id": 35508,
              "name": "with-value-just-person-enabled"
            },
            "feature_state_value": null
          }
        ]
    """.trimIndent()

    val setTrait = """
        {
          "trait_key": "set-from-client",
          "trait_value": "12345",
          "identity": {
            "identifier": "person"
          }
        }
    """.trimIndent()
}