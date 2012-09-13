package com.grantmuller;

import processing.core.PApplet;

public class Visualizer extends PApplet {
	
	private boolean reset;

	private float xLoc = 20;

	private float radius = 20;

	private boolean flash = false;

	private int location = 0;

	private float[] steps;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void setup() {
		stroke(255);
		ellipseMode(RADIUS);
		noLoop();
		size(500, 100);
	}

	public void updateSize(int w, int ppq) {
		float innerWidth = width - (2 * radius);
		float step = innerWidth/ppq;
		steps = new float[(int) ppq * 2];
		float start = radius;
		for (int i = 0; i < (2 * ppq); i++) {
			steps[i] = start;
			if (i < ppq) {
				start += step;
			} else {
				start -= step;
			}
			System.out.println("Step " + i + " is : " + start);
		}
	}

	public void draw() {
		if (reset) {
			fill(0);
			rect(0, 0, width, height);
			reset = false;
		}

		if (flash) {
			fill(255, 255, 255);
			rect(0, 0, width, height);
			flash = false;
		}

		fill(0, 0, 0, 50);
		rect(0, 0, width, height);
		fill(255, 0, 0);
		ellipse(xLoc, radius, radius, radius);
	}

	public void step (int pulseCount, int ppq, int divisor){
		if (pulseCount % (2 * ppq) == 0) {
			location = 0;
		}
		xLoc = steps[location];
		location++;

		if (pulseCount % (ppq * divisor) == 0) {
			flash = true;
		}
		redraw();
	}

	public void reset() {
		xLoc = radius;
		location = 0;
		reset = true;
		redraw();
	}
}
