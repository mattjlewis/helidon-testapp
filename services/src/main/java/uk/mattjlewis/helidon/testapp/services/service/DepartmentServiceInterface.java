package uk.mattjlewis.helidon.testapp.services.service;

import uk.mattjlewis.helidon.testapp.model.Department;
import uk.mattjlewis.helidon.testapp.model.Employee;

public interface DepartmentServiceInterface {
	String getImplementation();
	
	Department create(final Department department);

	Department get(final int id);

	Department findByName(final String name);

	Department update(final Department department);

	void remove(final int id);

	void addEmploye(int departmentId, Employee employee);

	void removeEmployee(int departmentId, int employeeId);
}
