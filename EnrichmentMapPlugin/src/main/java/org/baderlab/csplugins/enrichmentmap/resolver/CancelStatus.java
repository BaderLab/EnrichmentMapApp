package org.baderlab.csplugins.enrichmentmap.resolver;

@FunctionalInterface
public interface CancelStatus {

	public boolean isCancelled();
	
	public static CancelStatus notCancelable() {
		return new CancelStatus() {
			@Override public boolean isCancelled() { return false; }
		};
	}
}
