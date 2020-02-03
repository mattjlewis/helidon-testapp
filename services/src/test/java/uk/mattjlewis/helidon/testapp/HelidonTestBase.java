package uk.mattjlewis.helidon.testapp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.LogManager;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.transaction.TransactionManager;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import io.helidon.microprofile.server.Server;
import uk.mattjlewis.helidon.testapp.services.rest.Main;

@Dependent
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
	
	@PersistenceUnit(unitName = "HelidonTestAppPuJta")
	EntityManagerFactory entityManagerFactoryJta;
	@PersistenceUnit(unitName = "HelidonTestAppPuLocal")
	EntityManagerFactory entityManagerFactoryResourceLocal;
	@Inject
	TransactionManager transactionManager;

	@BeforeAll
	public static void setup() {
		setupLogging();
		server = Server.create().start();
		cdiContainer = (SeContainer) CDI.current();
	}

	private static void setupLogging() {
		try {
			LogManager.getLogManager().readConfiguration(HelidonTestBase.class.getResourceAsStream("/logging.properties"));
		} catch (IOException e) {
			// Ignore
		}
	}

	@AfterAll
	public static void tearDown() {
		if (server != null) {
			server.stop();
			server = null;
		}
		if (cdiContainer != null) {
			cdiContainer.close();
			cdiContainer = null;
		}
	}
}
