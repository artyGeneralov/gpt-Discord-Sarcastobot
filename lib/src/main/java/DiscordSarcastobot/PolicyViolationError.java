package DiscordSarcastobot;

public class PolicyViolationError extends Exception{
	public PolicyViolationError() {
		this("PolicyViolationError");
	}

	public PolicyViolationError(String m) {
		super(m);
	}
	
	
}
