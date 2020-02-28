package uk.mattjlewis.testapp.openapi;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexView;

import uk.mattjlewis.testapp.openapi.model.TestModel;
import uk.mattjlewis.testapp.model.Department;
import uk.mattjlewis.testapp.services.rest.DepartmentResource;

public class JandexTest {
    private static final String INDEX_PATH = "/META-INF/jandex.idx";
    
	public static void main(String[] args) throws IOException {
		setupLogging();
		
        try (InputStream jandexIS = new FileInputStream("../model/target/classes" + INDEX_PATH)) {
        	IndexView iv = new IndexReader(jandexIS).read();
			dumpIndexView("Model classes dir", iv);
        }
		
        try (InputStream jandexIS = new FileInputStream("../services/target/classes" + INDEX_PATH)) {
        	IndexView iv = new IndexReader(jandexIS).read();
			dumpIndexView("Services classes dir", iv);
        }
		
        try (InputStream jandexIS = DepartmentResource.class.getResourceAsStream(INDEX_PATH)) {
        	IndexView iv = new IndexReader(jandexIS).read();
			dumpIndexView("DepartmentResource.class", iv);
        }
		
        try (InputStream jandexIS = Department.class.getResourceAsStream(INDEX_PATH)) {
        	IndexView iv = new IndexReader(jandexIS).read();
			dumpIndexView("Department.class", iv);
        }
		
        try (InputStream jandexIS = TestModel.class.getResourceAsStream(INDEX_PATH)) {
        	IndexView iv = new IndexReader(jandexIS).read();
			dumpIndexView("TestModel.class", iv);
        }
		
        try (InputStream jandexIS = JandexTest.class.getResourceAsStream(INDEX_PATH)) {
        	IndexView iv = new IndexReader(jandexIS).read();
			dumpIndexView("JandexTest.class", iv);
        }
	}

	private static void dumpIndexView(String source, IndexView iv) {
		if (iv.getKnownClasses().isEmpty()) {
			System.out.println("Source: " + source + ". No known classes in the Jandex index");
		} else {
			System.out.println("Source: " + source + ". Known classes in discovered Jandex index:");
			iv.getKnownClasses().forEach(class_info -> System.out.println(class_info.name()));
		}

		findClass(iv, Department.class);
		findClass(iv, TestModel.class);
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
