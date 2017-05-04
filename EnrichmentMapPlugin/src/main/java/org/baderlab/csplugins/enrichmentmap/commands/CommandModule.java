package org.baderlab.csplugins.enrichmentmap.commands;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Provider;
import com.google.inject.Provides;

public class CommandModule extends AbstractModule {

	@BindingAnnotation @Retention(RUNTIME) public @interface BuildCommand { }
	@BindingAnnotation @Retention(RUNTIME) public @interface GSEACommand { }
	@BindingAnnotation @Retention(RUNTIME) public @interface ResolveCommand { }
	@BindingAnnotation @Retention(RUNTIME) public @interface PACommand { }
	
	
	@Override
	protected void configure() {
	}
	
	@Provides @BuildCommand
	public TaskFactory provideBuild(Provider<EMBuildCommandTask> taskProvider) {
		return createTaskFactory(taskProvider);
	}
	
	@Provides @GSEACommand
	public TaskFactory provideGSEA(Provider<EMGseaCommandTask> taskProvider) {
		return createTaskFactory(taskProvider);
	}
	
	@Provides @ResolveCommand
	public TaskFactory provideResolve(Provider<ResolverCommandTask> taskProvider) {
		return createTaskFactory(taskProvider);
	}
	
	@Provides @PACommand
	public TaskFactory providePA(Provider<PostAnalysisCommandTask> taskProvider) {
		return createTaskFactory(taskProvider);
	}

	
	private static TaskFactory createTaskFactory(Provider<? extends Task> taskProvider) {
		return new AbstractTaskFactory() {
			@Override
			public TaskIterator createTaskIterator() {
				return new TaskIterator(taskProvider.get());
			}
		};
	}
	
}
