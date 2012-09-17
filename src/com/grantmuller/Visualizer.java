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
	
	private BallSize ballSize;
	
	public boolean started;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Visualizer(int backgroundColor, int ballColor, int flashColor) {
		this.backgroundColor = backgroundColor;
		this.ballColor = ballColor;
		this.flashColor = flashColor;
		ballSize = BallSize.MEDIUM;
		this.started = false;
	}

	public void setup() {
		stroke(Color.WHITE.getRGB());
		ellipseMode(RADIUS);
		noLoop();
		size(500, 110);
	}

	public void updateSize(int w, int ppq) {
		int tableSize = 2 * ppq * 4;
		float innerWidth = width - (2 * ballSize.size);
		float step = innerWidth/(ppq * 4f);
		steps = new float[tableSize];
		float start = ballSize.size;
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
		ellipse(xLoc, height-ballSize.size - 5, ballSize.size, ballSize.size);
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
		xLoc = ballSize.size;
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
	
	public void setBallSize(BallSize ballSize) {
		this.ballSize = ballSize;
		if (!started) {
			reset();
		}
	}
	
	enum BallSize {
		SMALL("Small", 10, 90),
		MEDIUM("Medium", 20, 110),
		LARGE("Large", 40, 150),
		EXTRA_LARGE("X-Large", 80, 230);
		
		String displayName;
		
		int size;
		
		int containerSize;
		
		BallSize(String name, int size, int containerSize) {
			this.displayName = name;
			this.size = size;
			this.containerSize = containerSize;
		}
	}
}
