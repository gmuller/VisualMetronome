package com.grantmuller;

import java.awt.Color;

import processing.core.PApplet;

public class Visualizer extends PApplet{

	private float xLoc = 20;
	
	private float yLoc;

	private int location = 0;

	private float[] steps;

	private int backgroundColor;

	private int ballColor;

	private int flashColor;

	private float ballSize;
	
	private int fillColor;

	private int opacity = 80;
	
	private int barlength = 4;
	
	private int size;
	
	private int xLocationSize; 
	
	private float flashSize;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Visualizer(int backgroundColor, int ballColor, int flashColor, int ppq, int barLength, int divisor) {
		this.backgroundColor = backgroundColor;
		this.ballColor = ballColor;
		this.flashColor = flashColor;
		this.updateLengths(barLength, ppq, divisor);
	}

	public void setup() {
		stroke(Color.WHITE.getRGB());
		ellipseMode(RADIUS);
		//noLoop();
		size(500, 100);
	}

	public void updateSize(int h, int w, int ppq) {
		ballSize = h/2 - 10;
		yLoc = h - ballSize - 5;
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
		fill(fillColor);
		rect(0, 0, width, height);
		fill(ballColor);
		ellipse(xLoc, yLoc, ballSize, ballSize);
	}

	public void step (int pulseCount) {
		if (pulseCount % xLocationSize == 0) {
			location = 0;
		}
		xLoc = steps[location];
		location += barlength;
		//System.out.println(location +" : " + pulseCount + " : " + xLoc);
		fillColor = color(backgroundColor, opacity);
		if (pulseCount % flashSize == 0) {
			fillColor = flashColor;
		}
		//redraw();
	}

	public void reset() {
		xLoc = ballSize;
		location = 0;
		fillColor = backgroundColor;
		redraw();
	}

	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
		//redraw();
	}

	public void setBallColor(int ballColor) {
		this.ballColor = ballColor;
		redraw();
	}

	public void setFlashColor(int flashColor) {
		this.flashColor = flashColor;
		//redraw();
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

	public int getOpacity() {
		return opacity;
	}

	public void setOpacity(int opacity) {
		this.opacity = opacity;
	}
	
	public void updateLengths(int barLength, int ppq, int divisor) {
		this.barlength = barLength;
		this.size = 8/barLength;
		this.xLocationSize = size * ppq;
		this.flashSize = size/2f * ppq * divisor;
	}
}
