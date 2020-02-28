package uk.mattjlewis.testapp.services.rest;

import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import io.helidon.security.Grant;
import io.helidon.security.SecurityContext;
import io.helidon.security.Subject;
import io.helidon.security.annotations.Authenticated;
import io.helidon.security.annotations.Authorized;

@ApplicationScoped
@Path("protected")
@SuppressWarnings("static-method")
@Produces(MediaType.TEXT_PLAIN)
@Authenticated
@DenyAll
public class ProtectedResource {
	@GET
	@Authorized(false)
	public String securityContextTest(@Context SecurityContext context) {
		Subject sub = context.user().get();
		
		var grants = sub.grants(Grant.class);
		if (grants == null || grants.isEmpty()) {
			System.out.println("No grants for class " + Grant.class);
		} else {
			System.out.println("Grants for Grant.class:");
			grants.forEach(System.out::println);
		}
		
		return "Protected Resource\n"
				+ "Id: " + context.id() + "\n"
				+ "Username: " + context.userName() + "\n"
				+ "Service name: " + context.serviceName() + "\n"
				+ "User Subject: " + context.user().get() + "\n"
				+ "User Principle: " + context.userPrincipal().get() + "\n"
				+ "Has role 'Application/HelidonTxTestSP': " + context.isUserInRole("Application/HelidonTxTestSP") + "\n"
				+ "Has role 'Application/user': " + context.isUserInRole("Application/user") + "\n"
				+ "Has role 'Internal/everyone': " + context.isUserInRole("Internal/everyone") + "\n"
				+ "Has role 'HelidonTxTestUser': " + context.isUserInRole("HelidonTxTestUser") + "\n"
				+ "Has role 'admin': " + context.isUserInRole("admin") + "\n"
				+ "Has role 'user': " + context.isUserInRole("user") + "\n"
				+ "ABAC Attribute Names: " + context.userPrincipal().get().abacAttributeNames();
	}

	@GET
	@Path("applicationHelidon")
	@Authorized
	@RolesAllowed("Application/HelidonTxTestSP")
	public String applicationHelidon(@Context SecurityContext context) {
		return "User '" + context.userName() + "' has role Application/HelidonTxTestSP";
	}

	@GET
	@Path("applicationUser")
	@Authorized
	@RolesAllowed("Application/user")
	public String applicationUser(@Context SecurityContext context) {
		return "User '" + context.userName() + "' has role Application/user";
	}

	@GET
	@Path("everyone")
	@Authorized
	@RolesAllowed("Internal/everyone")
	public String everyone(@Context SecurityContext context) {
		return "User '" + context.userName() + "' has role Internal/everyone";
	}

	@GET
	@Path("helidonTxTestUser")
	@Authorized
	@RolesAllowed("HelidonTxTestUser")
	public String helidonTxTestUser(@Context SecurityContext context) {
		return "User '" + context.userName() + "' has role HelidonTxTestUser";
	}

	@GET
	@Path("admin")
	@Authorized
	@RolesAllowed("admin")
	public String adminRole(@Context SecurityContext context) {
		return "User '" + context.userName() + "' has role admin";
	}

	@GET
	@Path("user")
	@Authorized
	@RolesAllowed("user")
	public String userRole(@Context SecurityContext context) {
		return "User '" + context.userName() + "' has role user";
	}
}
