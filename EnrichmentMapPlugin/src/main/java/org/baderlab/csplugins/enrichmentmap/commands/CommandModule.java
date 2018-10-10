package org.baderlab.csplugins.enrichmentmap.commands;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.baderlab.csplugins.enrichmentmap.actions.OpenEnrichmentMapAction;
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
	@BindingAnnotation @Retention(RUNTIME) public @interface JsonCommand { }
	@BindingAnnotation @Retention(RUNTIME) public @interface BuildTableCommand { }
	@BindingAnnotation @Retention(RUNTIME) public @interface DatasetShow { }
	@BindingAnnotation @Retention(RUNTIME) public @interface DatasetHide { }
	
	@Override
	protected void configure() {
	}
	
	@Provides @BuildCommand
	public TaskFactory provideBuild(Provider<EMBuildCommandTask> taskProvider, OpenEnrichmentMapAction showTask) {
		return createTaskFactory(taskProvider.get(), showTask);
	}
	
	@Provides @GSEACommand
	public TaskFactory provideGSEA(Provider<EMGseaCommandTask> taskProvider, OpenEnrichmentMapAction showTask) {
		return createTaskFactory(taskProvider.get(), showTask);
	}
	
	@Provides @ResolveCommand
	public TaskFactory provideResolve(Provider<ResolverCommandTask> taskProvider, OpenEnrichmentMapAction showTask) {
		return createTaskFactory(taskProvider.get(), showTask);
	}
	
	@Provides @PACommand
	public TaskFactory providePA(Provider<PAKnownSignatureCommandTask> taskProvider) {
		return createTaskFactory(taskProvider.get(), null);
	}
	
	@Provides @JsonCommand
	public TaskFactory provideJson(Provider<ExportModelJsonCommandTask> taskProvider) {
		return createTaskFactory(taskProvider.get(), null);
	}
	
	@Provides @BuildTableCommand
	public TaskFactory provideBuildTable(Provider<TableCommandTask> taskProvider, OpenEnrichmentMapAction showTask) {
		return createTaskFactory(taskProvider.get(), showTask);
	}
	
	@Provides @DatasetShow
	public TaskFactory provideDatasetShow(DatasetShowCommandTask.Factory taskFactory) {
		return createTaskFactory(taskFactory.create(true), null);
	}
	
	@Provides @DatasetHide
	public TaskFactory provideDatasetHide(DatasetShowCommandTask.Factory taskFactory) {
		return createTaskFactory(taskFactory.create(false), null);
	}
	
	private static TaskFactory createTaskFactory(Task task, OpenEnrichmentMapAction showTask) {
		return new AbstractTaskFactory() {
			@Override
			public TaskIterator createTaskIterator() {
				TaskIterator tasks = new TaskIterator(task);
				if(showTask != null)
					tasks.append(showTask);
				return tasks;
			}
		};
	}
	
}
