package uk.mattjlewis.helidon.testapp.openapi;

import java.io.IOException;
import java.util.logging.LogManager;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

import io.helidon.microprofile.openapi.IndexBuilder;
import uk.mattjlewis.helidon.testapp.model.Department;
import uk.mattjlewis.helidon.testapp.openapi.model.TestModel;

public class JandexTest {
	public static void main(String[] args) throws IOException {
		setupLogging();
		
		try (SeContainer container = SeContainerInitializer.newInstance().initialize()) {
			IndexBuilder ib = container.getBeanManager().getExtension(IndexBuilder.class);
			System.out.println("*** IndexBuilder instance hashCode: " + ib.hashCode());

			IndexView iv = ib.indexView();

			if (iv.getKnownClasses().isEmpty()) {
				System.out.println("No known classes in the Jandex index");
			} else {
				System.out.println("Known classes in discovered Jandex index:");
				iv.getKnownClasses().forEach(class_info -> System.out.println(class_info.name()));
			}

			findClass(iv, TestModel.class);
			findClass(iv, Department.class);
		}
	}

	private static void findClass(IndexView iv, Class<?> clz) {
		DotName dot_name = DotName.createSimple(clz.getName());
		ClassInfo class_info = iv.getClassByName(dot_name);

		if (class_info == null) {
			System.out.println(dot_name + " not found!");
		} else {
			System.out.println("Found '" + dot_name + "': class info: " + class_info.name());
		}
	}

	/**
	 * Configure Java logging from the logging.properties file
	 */
	private static void setupLogging() throws IOException {
		LogManager.getLogManager().readConfiguration(JandexTest.class.getResourceAsStream("/logging.properties"));
	}
}
