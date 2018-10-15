package org.baderlab.csplugins.enrichmentmap;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.baderlab.csplugins.enrichmentmap.actions.OpenEnrichmentMapAction;
import org.baderlab.csplugins.enrichmentmap.commands.ChartCommandTask;
import org.baderlab.csplugins.enrichmentmap.commands.DatasetShowCommandTask;
import org.baderlab.csplugins.enrichmentmap.commands.EMBuildCommandTask;
import org.baderlab.csplugins.enrichmentmap.commands.EMGseaCommandTask;
import org.baderlab.csplugins.enrichmentmap.commands.ExportModelJsonCommandTask;
import org.baderlab.csplugins.enrichmentmap.commands.PAKnownSignatureCommandTask;
import org.baderlab.csplugins.enrichmentmap.commands.ResolverCommandTask;
import org.baderlab.csplugins.enrichmentmap.commands.TableCommandTask;
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
	@BindingAnnotation @Retention(RUNTIME) public @interface DatasetShowCommand { }
	@BindingAnnotation @Retention(RUNTIME) public @interface DatasetHideCommand { }
	@BindingAnnotation @Retention(RUNTIME) public @interface ChartCommand { }
	
	@Override
	protected void configure() {
	}
	
	@Provides @BuildCommand
	public TaskFactory provideBuild(Provider<EMBuildCommandTask> taskProvider, OpenEnrichmentMapAction showTask) {
		return createTaskFactory(taskProvider, showTask);
	}
	
	@Provides @GSEACommand
	public TaskFactory provideGSEA(Provider<EMGseaCommandTask> taskProvider, OpenEnrichmentMapAction showTask) {
		return createTaskFactory(taskProvider, showTask);
	}
	
	@Provides @ResolveCommand
	public TaskFactory provideResolve(Provider<ResolverCommandTask> taskProvider, OpenEnrichmentMapAction showTask) {
		return createTaskFactory(taskProvider, showTask);
	}
	
	@Provides @PACommand
	public TaskFactory providePA(Provider<PAKnownSignatureCommandTask> taskProvider) {
		return createTaskFactory(taskProvider);
	}
	
	@Provides @JsonCommand
	public TaskFactory provideJson(Provider<ExportModelJsonCommandTask> taskProvider) {
		return createTaskFactory(taskProvider);
	}
	
	@Provides @BuildTableCommand
	public TaskFactory provideBuildTable(Provider<TableCommandTask> taskProvider, OpenEnrichmentMapAction showTask) {
		return createTaskFactory(taskProvider, showTask);
	}
	
	@Provides @DatasetShowCommand
	public TaskFactory provideDatasetShow(DatasetShowCommandTask.Factory taskFactory) {
		return createTaskFactory(() -> taskFactory.create(true));
	}
	
	@Provides @DatasetHideCommand
	public TaskFactory provideDatasetHide(DatasetShowCommandTask.Factory taskFactory) {
		return createTaskFactory(() -> taskFactory.create(false));
	}
	
	@Provides @ChartCommand
	public TaskFactory provideChart(Provider<ChartCommandTask> taskFactory) {
		return createTaskFactory(taskFactory);
	}
	
	
	
	private static TaskFactory createTaskFactory(Provider<? extends Task> taskProvider, Task ... tasks) {
		return new AbstractTaskFactory() {
			@Override
			public TaskIterator createTaskIterator() {
				TaskIterator taskIterator = new TaskIterator(taskProvider.get());
				for(Task task : tasks) {
					taskIterator.append(task);
				}
				return taskIterator;
			}
		};
	}
	
}
