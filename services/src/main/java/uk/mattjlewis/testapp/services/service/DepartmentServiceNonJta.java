package uk.mattjlewis.testapp.services.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceUnit;

import uk.mattjlewis.testapp.model.Department;
import uk.mattjlewis.testapp.model.Employee;
import uk.mattjlewis.testapp.services.jpa.BaseEntityRepository;
import uk.mattjlewis.testapp.services.service.qualifiers.ResourceLocal;

@ApplicationScoped
@ResourceLocal
public class DepartmentServiceNonJta implements DepartmentServiceInterface {
	@PersistenceUnit(unitName = "HelidonTestAppPuLocal")
	private EntityManagerFactory emf;

	@Override
	public String getImplementation() {
		return "Department service - application managed EMF & resource local";
	}

	@Override
	public Department create(final Department department) {
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		try {
			// Make sure the many to one relationship is set
			if (department.getEmployees() != null) {
				department.getEmployees().forEach(emp -> emp.setDepartment(department));
			}
			var created = BaseEntityRepository.create(em, department);
			tx.commit();
			return created;
		} catch (Exception e) {
			if (tx.isActive()) {
				tx.rollback();
			}
			throw e;
		} finally {
			em.close();
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
	public Department get(final int id) {
		EntityManager em = emf.createEntityManager();
		try {
			return BaseEntityRepository.findById(em, Department.class, Integer.valueOf(id));
		} finally {
			em.close();
		}
	}

	@Override
	public Department findByName(final String name) {
		EntityManager em = emf.createEntityManager();
		try {
			return em.createNamedQuery("Department.findByName", Department.class).setParameter("name", name)
					.getSingleResult();
		} finally {
			em.close();
		}
	}

	@Override
	public List<Department> search(String name) {
		EntityManager em = emf.createEntityManager();
		try {
			return em.createNamedQuery("Department.searchByName", Department.class).setParameter("name", name)
					.getResultList();
		} finally {
			em.close();
		}
	}

	@Override
	public Department update(final Department department) {
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		try {
			Department merged = BaseEntityRepository.update(em, department.getId(), department);
			tx.commit();
			return merged;
		} catch (Exception e) {
			if (tx.isActive()) {
				tx.rollback();
			}
			throw e;
		} finally {
			em.close();
		}
	}

	@Override
	public void delete(final int id) {
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		try {
			BaseEntityRepository.delete(em, Department.class, Integer.valueOf(id));
			tx.commit();
		} catch (Exception e) {
			if (tx.isActive()) {
				tx.rollback();
			}
			throw e;
		} finally {
			em.close();
		}
	}

	@Override
	public void addEmploye(int departmentId, Employee employee) {
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		try {
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
			tx.commit();
		} catch (Exception e) {
			if (tx.isActive()) {
				tx.rollback();
			}
			throw e;
		} finally {
			em.close();
		}
	}

	@Override
	public void updateEmployee(int departmentId, Employee employee) {
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		try {
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
			tx.commit();
		} catch (Exception e) {
			if (tx.isActive()) {
				tx.rollback();
			}
			throw e;
		} finally {
			em.close();
		}
	}

	@Override
	public void removeEmployee(int departmentId, int employeeId) {
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		try {
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
			tx.commit();
		} catch (Exception e) {
			if (tx.isActive()) {
				tx.rollback();
			}
			throw e;
		} finally {
			em.close();
		}
	}
}
