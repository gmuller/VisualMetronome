package com.grantmuller;

import java.awt.Color;

import processing.core.PApplet;

public class Visualizer extends PApplet{
	
	private boolean reset;

	private float xLoc = 20;

	private boolean flash = false;

	private int location = 0;

	private float[] steps;
	
	private int backgroundColor;

	private int ballColor;
	
	private int flashColor;
	
	private float ballSize;
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Visualizer(int backgroundColor, int ballColor, int flashColor) {
		this.backgroundColor = backgroundColor;
		this.ballColor = ballColor;
		this.flashColor = flashColor;
	}

	public void setup() {
		stroke(Color.WHITE.getRGB());
		ellipseMode(RADIUS);
		noLoop();
		size(500, 100);
	}

	public void updateSize(int h, int w, int ppq) {
		ballSize = h/2 - 10;
		int tableSize = 2 * ppq * 4;
		float innerWidth = width - (2 * ballSize);
		float step = innerWidth/(ppq * 4f);
		steps = new float[tableSize];
		float start = ballSize;
		for (int i = 0; i < tableSize; i++) {
			steps[i] = start;
			if (i < (ppq * 4)) {
				start += step;
			} else {
				start -= step;
			}
		}
	}

	public void draw() {
		if (reset) {
			fill(backgroundColor);
			rect(0, 0, width, height);
			reset = false;
		}

		if (flash) {
			fill(flashColor);
			rect(0, 0, width, height);
			flash = false;
		}

		fill(backgroundColor, 80);
		rect(0, 0, width, height);
		fill(ballColor);
		ellipse(xLoc, height-ballSize - 5, ballSize, ballSize);
	}

	public void step (int pulseCount, int ppq, int divisor, int barlength){
		int size = 8/barlength;
		if (pulseCount % (size * ppq) == 0) {
			location = 0;
		}
		xLoc = steps[location];
		location += barlength;

		if (pulseCount % (size/2f * ppq * divisor) == 0) {
			flash = true;
		}
		redraw();
	}

	public void reset() {
		xLoc = ballSize;
		location = 0;
		reset = true;
		redraw();
	}

	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
		redraw();
	}

	public void setBallColor(int ballColor) {
		this.ballColor = ballColor;
		redraw();
	}

	public void setFlashColor(int flashColor) {
		this.flashColor = flashColor;
		redraw();
	}
	
	public int getBackgroundColor() {
		return backgroundColor;
	}

	public int getBallColor() {
		return ballColor;
	}

	public int getFlashColor() {
		return flashColor;
	}
}
