package uk.mattjlewis.helidon.testapp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
public class RestSecurityTest extends HelidonTestBase {
	@Test
	public void restClientSecurityTest() {
		Client client = ClientBuilder.newClient();
		WebTarget root = client.target("http://" + server.host() + ":" + server.port()).path("rest");

		try (Response response = root.path("protected").request(MediaType.TEXT_PLAIN).get()) {
			assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
		}

		try (Response response = root.path("protected").request(MediaType.TEXT_PLAIN)
				.header("Authorization", createHttpBasicAuthToken(USER_USERNAME, USER_PASSWORD)).get()) {
			assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		}

		try (Response response = root.path("protected/admin").request(MediaType.TEXT_PLAIN)
				.header("Authorization", createHttpBasicAuthToken(ADMIN_USERNAME, ADMIN_PASSWORD)).get()) {
			assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		}

		try (Response response = root.path("protected/admin").request(MediaType.TEXT_PLAIN)
				.header("Authorization", createHttpBasicAuthToken(USER_USERNAME, USER_PASSWORD)).get()) {
			assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
		}

		try (Response response = root.path("protected/user").request(MediaType.TEXT_PLAIN)
				.header("Authorization", createHttpBasicAuthToken(USER_USERNAME, USER_PASSWORD)).get()) {
			assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		}
	}
}
