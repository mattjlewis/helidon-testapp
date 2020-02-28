package uk.mattjlewis.testapp.cdi;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionTarget;

public class CdiExtension implements Extension {
	/*
	 * private <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> event) {
	 * System.out.println("*** processAnnotatedType for class " + event.getAnnotatedType().getJavaClass().getName());
	 * event.getAnnotatedType().getAnnotations().forEach(System.out::println); }
	 */

	private <T, X> void processInjectionPoint(@Observes ProcessInjectionPoint<T, X> event) {
		System.out.println("*** processInjectionPoint. bean: " + event.getInjectionPoint().getBean().getName()
				+ ", type: " + event.getInjectionPoint().getType().getTypeName() + ", member: "
				+ event.getInjectionPoint().getMember().getName());
	}

	private <T, X> void processInjectionTarget(@Observes ProcessInjectionTarget<X> event) {
		System.out.println("*** processInjectionTarget");
		event.getInjectionTarget().getInjectionPoints().forEach(
				ip -> System.out.format("Bean: %s, member: %s%n", ip.getBean().getName(), ip.getMember().getName()));
	}
}
