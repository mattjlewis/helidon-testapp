package uk.mattjlewis.helidon.testapp;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import io.helidon.microprofile.server.Server;

public abstract class HelidonTestBase {
	static final String USER_USERNAME = "user1";
	static final String USER_PASSWORD = "password";
	static final String ADMIN_USERNAME = "admin";
	static final String ADMIN_PASSWORD = "password";

	public static String createHttpBasicAuthToken(String username, String password) {
		return "Basic "
				+ Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
	}

	static SeContainer cdiContainer;
	static Server server;

	@BeforeAll
	public static void setup() {
		cdiContainer = SeContainerInitializer.newInstance().initialize();
		assertNotNull(cdiContainer);

		server = Server.create().start();
	}

	@AfterAll
	public static void tearDown() {
		if (server != null) {
			server.stop();
			server = null;
		}
		if (cdiContainer != null) {
			cdiContainer.close();
		}
	}
}
