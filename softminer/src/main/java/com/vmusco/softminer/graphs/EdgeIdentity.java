package com.vmusco.softminer.graphs;

/**
 * A simple class which maps two edges names together
 * @author Vincenzo Musco - http://www.vmusco.com
 *
 */
public class EdgeIdentity {
	private String from;
	private String to;
	
	public EdgeIdentity(String from, String to) {
		super();
		this.from = from;
		this.to = to;
	}

	public String getTo() {
		return to;
	}
	
	public void setTo(String to) {
		this.to = to;
	}
	
	public void setFrom(String from) {
		this.from = from;
	}
	
	public String getFrom() {
		return from;
	}
}
