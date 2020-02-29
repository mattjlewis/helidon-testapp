package uk.mattjlewis.testapp.services.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceUnit;
import javax.transaction.Transactional;

import uk.mattjlewis.testapp.model.Department;
import uk.mattjlewis.testapp.model.Employee;
import uk.mattjlewis.testapp.services.jpa.BaseEntityRepository;
import uk.mattjlewis.testapp.services.service.qualifiers.ContainerManagedEmf;

@ApplicationScoped
@ContainerManagedEmf
public class DepartmentServiceContainerManagedEmf implements DepartmentServiceInterface {
	@PersistenceUnit(unitName = "HelidonTestAppPuJta")
	private EntityManagerFactory emf;

	@Override
	public String getImplementation() {
		return "Department service - container managed EMF & JTA";
	}

	@Override
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public Department create(final Department department) {
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			// Make sure the many to one relationship is set
			if (department.getEmployees() != null) {
				department.getEmployees().forEach(emp -> emp.setDepartment(department));
			}
			return BaseEntityRepository.create(em, department);
		} finally {
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
	}

	@Override
	public List<Department> getAll() {
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			return BaseEntityRepository.findAll(em, Department.class);
		} finally {
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
	}

	@Override
	@Transactional(Transactional.TxType.SUPPORTS)
	public Department get(final int id) {
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			return BaseEntityRepository.findById(em, Department.class, Integer.valueOf(id));
		} finally {
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
	}

	@Override
	@Transactional(Transactional.TxType.SUPPORTS)
	public Department findByName(final String name) {
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			return em.createNamedQuery("Department.findByName", Department.class).setParameter("name", name)
					.getSingleResult();
		} finally {
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
	}

	@Override
	@Transactional(Transactional.TxType.SUPPORTS)
	public List<Department> search(String name) {
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			return em.createNamedQuery("Department.searchByName", Department.class).setParameter("name", name)
					.getResultList();
		} finally {
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
	}

	@Override
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public Department update(final Department department) {
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			//return BaseEntityRepository.update(em, department.getId(), department);
			Department current = em.find(Department.class, department.getId());

			if (current == null) {
				throw new EntityNotFoundException("Department not found with id " + department.getId());
			}

			System.out.println("current version: " + current.getVersion());
			System.out.println("department version: " + department.getVersion());

			current.setLocation(department.getLocation());
			current.setName(department.getName());
			current.setLastUpdated(new Date());
			current.setVersion(department.getVersion());

			// Do something about the employees?
			//current.setEmployees(department.getEmployees());

			return current;
		} finally {
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
	}

	@Override
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public void delete(final int id) {
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			BaseEntityRepository.delete(em, Department.class, Integer.valueOf(id));
		} finally {
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
	}

	@Override
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public void addEmploye(int departmentId, Employee employee) {
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			Department dept = em.find(Department.class, Integer.valueOf(departmentId));
			if (dept == null) {
				throw new EntityNotFoundException("Department not found for id " + departmentId);
			}
			employee.setDepartment(dept);
			em.persist(employee);
			dept.getEmployees().add(employee);
			dept.setLastUpdated(new Date());
			// FIXME Do I need to do this?
			//em.merge(dept);
		} finally {
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
	}

	@Override
	@Transactional(Transactional.TxType.REQUIRED)
	public void updateEmployee(int departmentId, Employee employee) {
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			Department dept = em.find(Department.class, Integer.valueOf(departmentId));
			if (dept == null) {
				throw new EntityNotFoundException("Department not found for id " + departmentId);
			}
			Optional<Employee> opt_emp = dept.getEmployees().stream().filter(emp -> emp.getId().equals(employee.getId()))
					.findFirst();
			Employee emp = opt_emp.orElseThrow(() -> new EntityNotFoundException(
					"No such Employee with id " + employee.getId() + " in department " + departmentId));
			
			emp.setEmailAddress(employee.getEmailAddress());
			emp.setFavouriteDrink(employee.getFavouriteDrink());
			emp.setName(employee.getName());
	
			dept.setLastUpdated(new Date());
		} finally {
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
	}

	@Override
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public void removeEmployee(int departmentId, int employeeId) {
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			Department dept = em.find(Department.class, Integer.valueOf(departmentId));
			if (dept == null) {
				throw new EntityNotFoundException("Department not found for id " + departmentId);
			}
			Optional<Employee> opt_emp = dept.getEmployees().stream()
					.filter(emp -> emp.getId().intValue() == employeeId).findFirst();
			Employee emp = opt_emp.orElseThrow(() -> new EntityNotFoundException(
					"No such Employee with id " + employeeId + " in department " + departmentId));
			emp.setDepartment(null);
			dept.getEmployees().remove(emp);
			dept.setLastUpdated(new Date());
		} finally {
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
	}
}
