package org.baderlab.csplugins.enrichmentmap.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.FinishStatus.Type;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.util.ListSingleSelection;

public class TaskUtil {
	private TaskUtil() {}
	
	public static TaskObserver onFail(Consumer<FinishStatus> consumer) {
		return new TaskObserver() {
			@Override
			public void taskFinished(ObservableTask task) { }
			@Override
			public void allFinished(FinishStatus finishStatus) {
				if(finishStatus.getType() == Type.FAILED) {
					consumer.accept(finishStatus);
				}
			}
		};
	}
	
	public static <T> TaskObserver allFinished(Consumer<FinishStatus> consumer) {
		return new TaskObserver() {
			@Override
			public void taskFinished(ObservableTask task) {
			}

			@Override
			public void allFinished(FinishStatus finishStatus) { 
				consumer.accept(finishStatus);
			}
		};
	}
	
	public static <T> TaskObserver taskFinished(Consumer<ObservableTask> consumer) {
		return new TaskObserver() {
			@Override
			public void taskFinished(ObservableTask task) {
				consumer.accept(task);
			}

			@Override
			public void allFinished(FinishStatus finishStatus) { }
		};
	}
	
	public static <T> TaskObserver taskFinished(Class<T> taskType, Consumer<T> consumer) {
		return new TaskObserver() {
			@Override
			public void taskFinished(ObservableTask task) {
				if(taskType.isInstance(task)) {
					consumer.accept(taskType.cast(task));
				}
			}

			@Override
			public void allFinished(FinishStatus finishStatus) { }
		};
	}

	public static <T> TaskObserver taskFinished(Class<T> taskType, Consumer<T> finishConsumer, Consumer<FinishStatus> failConsumer) {
		return new TaskObserver() {
			@Override
			public void taskFinished(ObservableTask task) {
				if(taskType.isInstance(task)) {
					finishConsumer.accept(taskType.cast(task));
				}
			}
			@Override
			public void allFinished(FinishStatus finishStatus) { 
				if(finishStatus.getType() == Type.FAILED) {
					failConsumer.accept(finishStatus);
				}
			}
		};
	}
	
	public static ListSingleSelection<String> lssFromEnum(Enum<?> ... values) {
		List<String> names = new ArrayList<>(values.length);
		for(Enum<?> value : values) {
			names.add(value.name());
		}
		return new ListSingleSelection<>(names);
	}
	
	
	public static ListSingleSelection<String> lssFromEnumWithDefault(Enum<?>[] allValues, Enum<?> defaultValue) {
		List<Enum<?>> values = new ArrayList<>(Arrays.asList(allValues));
		if(defaultValue != null) {
			values.remove(defaultValue);
			values.add(0, defaultValue);
		}
		
		List<String> names = new ArrayList<>(values.size());
		for(Enum<?> value : values) {
			names.add(value.name());
		}
		
		return new ListSingleSelection<>(names);
	}
}
