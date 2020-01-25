package uk.mattjlewis.helidon.testapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.junit.jupiter.api.Test;

import uk.mattjlewis.helidon.testapp.model.Department;
import uk.mattjlewis.helidon.testapp.model.Employee;

@SuppressWarnings("static-method")
public class DepartmentRestClientTest extends HelidonTestBase {
	private static final String DEPARTMENT_PATH = "department";

	@Test
	public void restClientDepartmentTest() {
		Client client = ClientBuilder.newClient();
		// Required to use PATCH
		client.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, Boolean.TRUE);
		WebTarget root = client.target("http://" + server.host() + ":" + server.port()).path("rest");

		// Create a department with employees
		List<Employee> employees = Arrays.asList(new Employee("Matt", "matt@test.org", "Coffee"),
				new Employee("Fred", "fred@test.org", "Beer"));
		Department dept = new Department("IT", "London", employees);
		Department created_dept = null;
		try (Response response = root.path(DEPARTMENT_PATH).request(MediaType.APPLICATION_JSON).post(Entity.json(dept))) {
			if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
				fail("Unexpected response status: " + response.getStatus());
			}
			System.out.println("Response location: " + response.getLocation());
			created_dept = response.readEntity(Department.class);
			assertNotNull(created_dept);
			assertNotNull(created_dept.getId());
			assertEquals(dept.getName(), created_dept.getName());
			assertEquals(dept.getLocation(), created_dept.getLocation());
		}

		// Find the department
		Department found_dept = null;
		try {
			found_dept = root.path(DEPARTMENT_PATH).path(created_dept.getId().toString())
					.request(MediaType.APPLICATION_JSON).get(Department.class);
			assertNotNull(found_dept);
			assertNotNull(found_dept.getId());
			assertEquals(dept.getName(), found_dept.getName());
			assertEquals(employees.size(), found_dept.getEmployees().size());
			assertEquals(dept.getEmployees().size(), found_dept.getEmployees().size());
			assertEquals(1, found_dept.getVersion().intValue());
		} catch (WebApplicationException e) {
			fail("Unexpected response status: " + e.getResponse().getStatus());
			// Here simply to avoid the compiler warning about a potential null pointer access
			return;
		}

		// Update the department
		found_dept.setName(dept.getName() + " - updated");
		try {
			Department updated_dept = root.path(DEPARTMENT_PATH).path(created_dept.getId().toString())
					.request(MediaType.APPLICATION_JSON)
					.method(HttpMethod.PATCH, Entity.json(found_dept), Department.class);
			assertNotNull(updated_dept);
			assertEquals(dept.getName() + " - updated", updated_dept.getName());
			assertEquals(found_dept.getVersion().intValue() + 1, updated_dept.getVersion().intValue());
		} catch (WebApplicationException e) {
			fail("Unexpected response status: " + e.getResponse().getStatus());
		}

		// Should trigger bean validation failure
		dept = new Department("012345678901234567890123456789", "London");
		try (Response response = root.path(DEPARTMENT_PATH).request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(dept, MediaType.APPLICATION_JSON))) {
			if (response.getStatus() != Response.Status.BAD_REQUEST.getStatusCode()) {
				fail("Unexpected response status: '" + response.getStatus());
			}
		}

		/*
		// Should pass bean validation but trigger database constraint violation
		dept = new Department("HR", "Reading",
				Arrays.asList(new Employee("Rod", "rod@test.org", "Water"),
						new Employee("Jane", "jane@test.org", "012345678901234567890123456789"),
						new Employee("Freddie", "freddie@test.org", "Tea")));
		try (Response response = root.path(DEPARTMENT_PATH).request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(dept, MediaType.APPLICATION_JSON))) {
			if (response.getStatus() != Response.Status.CONFLICT.getStatusCode()) {
				fail("Unexpected response status '" + response.getStatus());
			}
		}
		*/
	}
}
