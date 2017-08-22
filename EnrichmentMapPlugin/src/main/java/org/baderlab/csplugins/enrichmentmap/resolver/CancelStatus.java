package org.baderlab.csplugins.enrichmentmap.resolver;

public interface CancelStatus {

	public void cancel();
	
	public boolean isCancelled();
	
	
	public static CancelStatus notCancelable() {
		return new CancelStatus() {
			@Override public boolean isCancelled() { return false; }
			@Override public void cancel() { }
		};
	}
}
