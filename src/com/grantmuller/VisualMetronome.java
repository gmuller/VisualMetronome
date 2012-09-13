package com.grantmuller;

import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRadioButtonMenuItem;

import rwmidi.MidiInput;
import rwmidi.MidiInputDevice;
import rwmidi.RWMidi;
import rwmidi.SyncEvent;

public class VisualMetronome implements ActionListener {

	private JFrame frame;

	Visualizer visualizer;

	MidiInput syncIn;

	int pulseCount = 0;

	int divisor = 4;

	int ppq = 24;

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
		frame.setBounds(100, 100, 500, 100);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new GridLayout(0, 1, 0, 0));

		visualizer = new Visualizer();
		frame.getContentPane().add(visualizer);
		visualizer.init();
		frame.addComponentListener(
				new ComponentAdapter(){ 
					int w;

					@Override 
					public void componentResized(ComponentEvent e) { 
						JFrame f = (JFrame)e.getComponent(); 
						w = f.getSize().width;
						visualizer.updateSize(w, ppq);
					} 
				});

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
			visualizer.step(pulseCount, ppq, divisor);
			break;
		case SyncEvent.START:
			break;
		case SyncEvent.STOP:
			visualizer.reset();
			pulseCount = 0;
			break;
		case SyncEvent.SONG_POSITION_POINTER:
			break;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.contains("ppq")) {
			ppq = Integer.valueOf(command.substring(3, command.length()));
			visualizer.updateSize(frame.getSize().width, ppq);
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
