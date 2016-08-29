package org.baderlab.csplugins.enrichmentmap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * Guice module, makes the @AfterInjection annotation work.
 * 
 * @author mkucera
 *
 */
public class AfterInjectionModule extends AbstractModule {

	@Override
	protected void configure() {
		// Call methods annotated with @AfterInjection after injection, mainly used to create UIs
		bindListener(new AfterInjectionMatcher(), new TypeListener() {
			AfterInjectionInvoker invoker = new AfterInjectionInvoker();
			@Override
			public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
				encounter.register(invoker);
			}
		});
	}
	

	/**
	 * Guice matcher that matches types that have a method annotated with @AfterInjection
	 */
	static class AfterInjectionMatcher extends AbstractMatcher<TypeLiteral<?>> {
		@Override
		public boolean matches(TypeLiteral<?> typeLiteral) {
			Method[] methods = typeLiteral.getRawType().getDeclaredMethods();
			return Arrays.stream(methods).anyMatch(m -> m.isAnnotationPresent(AfterInjection.class));
		}
	}

	/**
	 * Invokes methods annotated with @AfterInjection
	 */
	static class AfterInjectionInvoker implements InjectionListener<Object> {
		@Override
		public void afterInjection(Object injectee) {
			Method[] methods = injectee.getClass().getDeclaredMethods();
			for(Method method : methods) {
				if(method.isAnnotationPresent(AfterInjection.class)) {
					try {
						method.setAccessible(true);
						method.invoke(injectee);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
}

