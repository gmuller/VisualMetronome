package com.grantmuller;

import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRadioButtonMenuItem;

import processing.core.PApplet;
import rwmidi.MidiInput;
import rwmidi.MidiInputDevice;
import rwmidi.RWMidi;
import rwmidi.SyncEvent;

public class VisualMetronome implements ActionListener {

	private JFrame frame;
	
	EmbeddedProcessing embed;
	
	MidiInput syncIn;
	
	int pulseCount = 0;
	
	boolean started = false;

	Long previousPulseTime = 0L;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					VisualMetronome window = new VisualMetronome();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public VisualMetronome() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 500, 75);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new GridLayout(0, 1, 0, 0));

		embed = new EmbeddedProcessing();
		frame.getContentPane().add(embed);
		embed.init();

		JMenuBar menuBar = new JMenuBar();
		JMenu settingsMenu = new JMenu("Settings");
		menuBar.add(settingsMenu);

		JMenu PPQ = new JMenu("PPQ");
		ButtonGroup ppqGroup = new ButtonGroup();
		String[] ppqOptions = {"24","96"};
		for (String ppq : ppqOptions) {
			JRadioButtonMenuItem ppqOpt = new JRadioButtonMenuItem(ppq);
			ppqOpt.setActionCommand("ppq" + ppq);
			ppqOpt.addActionListener(this);
			if (ppq.equals("24")) {
				ppqOpt.setSelected(true);
			}
			PPQ.add(ppqOpt);
			ppqGroup.add(ppqOpt);
		}
		settingsMenu.add(PPQ);

		ButtonGroup midiGroup = new ButtonGroup();
		JMenu MIDI = new JMenu("Midi Input");
		MidiInputDevice devices[] = RWMidi.getInputDevices();
		for (int i = 0; i < devices.length; i++) {
			String deviceName = devices[i].getName();
			JRadioButtonMenuItem midiIn = new JRadioButtonMenuItem(deviceName);
			midiIn.setActionCommand("midi---"+ i);
			midiIn.addActionListener(this);
			if (i == 0) midiIn.setSelected(true);
			MIDI.add(midiIn);
			midiGroup.add(midiIn);
		}
		settingsMenu.add(MIDI);
		frame.setJMenuBar(menuBar);
		
		syncIn =  RWMidi.getInputDevices()[0].createInput();
		if (syncIn != null){
			syncIn.plug(this, "processEvents");
		}
	}
	
	public void processEvents(SyncEvent syncEvent){
		switch (syncEvent.getStatus()){
		case SyncEvent.TIMING_CLOCK:
			pulseCount++;
			embed.step();
			break;
		case SyncEvent.START: 
			started = true; 
			break;
		case SyncEvent.STOP:
			embed.reset();
			pulseCount = 0;
			started = false; 
			break;
		case SyncEvent.SONG_POSITION_POINTER:
			break;
		}
	}

	class EmbeddedProcessing extends PApplet {

		boolean reset;

		float xLoc = 20;

		float xDir = 1;

		float radius = 20;
		
		boolean flash = false;
		
		int divisor = 4;
		
		float ppq = 24;
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void setup() {
			stroke(255);
			ellipseMode(RADIUS);
			noLoop();
		}

		public void draw() {
			if (reset) {
				fill(0);
				rect(0, 0, width, height);
				reset = false;
			}
			
			if (flash) {
				fill(255, 255, 255, 50);
				rect(0, 0, width, height);
			}
			
			fill(0, 0, 0, 50);
			rect(0, 0, width, height);
			fill(255, 0, 0);
			ellipse(xLoc, radius, radius, radius);
		}

		public void step (){
			float innerWidth = width - (2 * radius);
			xLoc = xLoc + (innerWidth/ppq * xDir);
			if (pulseCount % ppq == 0) {
				xDir *= -1;
			}
			
			if (pulseCount % (ppq * divisor) == 0) {
				flash = true;
			}
			redraw();
		}

		public void reset() {
			xLoc = radius;
			xDir = 1;
			reset = true;
			redraw();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.contains("ppq")) {
			embed.ppq = Integer.valueOf(command.substring(3, command.length()));
		}

		if (command.contains("midi---")){
			int midiIn = Integer.valueOf(command.split("---")[1]);
			syncIn.closeMidi();
			syncIn =  RWMidi.getInputDevices()[midiIn].createInput();
			if (syncIn != null){
				syncIn.plug(this, "processEvents");
			}
		}
	}
}
