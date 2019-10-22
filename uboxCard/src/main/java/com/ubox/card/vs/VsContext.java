package com.ubox.card.vs;

public class VsContext {

	private volatile boolean sessionEffective;

	private static final VsContext instance = new VsContext();

	private VsContext() {
	}

	public static VsContext getInstance() {
		return instance;
	}

	public boolean isSessionEffective() {
		return sessionEffective;
	}

	public void setSessionEffective(boolean sessionEffective) {
		this.sessionEffective = sessionEffective;
	}

}
