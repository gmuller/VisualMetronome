package com.grantmuller;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Calendar;
import java.util.Hashtable;

import javax.swing.ButtonGroup;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import rwmidi.MidiInput;
import rwmidi.MidiInputDevice;
import rwmidi.RWMidi;
import rwmidi.SyncEvent;

public class VisualMetronome implements ActionListener, ChangeListener {

	private JFrame frame;

	private Visualizer visualizer;

	private MidiInput syncIn;

	private static int[] divisors = {1, 2, 3, 4, 5, 6, 7, 9, 11, 13};

	private static int[] barlengths = {1, 2, 4, 8};

	private static String[] ppqOptions = {"24","96"};

	private int pulseCount = 0;

	private int divisor = 4;

	private int barLength = 4;

	private int ppq = 24;

	private JColorChooser bgChooser;

	private JColorChooser ballColorChooser;

	private JColorChooser flashColorChooser;

	private JSlider opacitySlider;

	private boolean started;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		Calendar expiry = Calendar.getInstance();
		expiry.set(2013, 1, 31, 0, 0); // Expire at 31 Jan 2013
		Calendar now = Calendar.getInstance();
		if (now.after(expiry)) {
			JOptionPane.showMessageDialog(null,
					"The Visual Metronome Trial has Expired.\n Please contact Bob Lawliss (boblawliss@gmail.com) for more information.");
			System.exit(0);
		} else {
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
		frame.setBounds(100, 100, 500, 110);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.getContentPane().setLayout(new GridLayout(0, 1, 0, 0));

		visualizer = new Visualizer(Color.BLACK.getRGB(), Color.RED.getRGB(), Color.GREEN.getRGB());
		frame.getContentPane().add(visualizer);
		frame.getContentPane().getWidth();
		visualizer.init();
		frame.addComponentListener(
				new ComponentAdapter(){ 
					int w, h;

					@Override 
					public void componentResized(ComponentEvent e) { 
						JFrame f = (JFrame)e.getComponent(); 
						w = f.getContentPane().getWidth();
						h = f.getContentPane().getHeight();
						visualizer.updateSize(h, w, ppq);
						if (!started) {
							visualizer.reset();
						}
					} 
				});

		// Add Midi Settings
		JMenuBar menuBar = new JMenuBar();
		JMenu midiSettingsMenu = new JMenu("MIDI Settings");
		menuBar.add(midiSettingsMenu);

		JMenu PPQ = new JMenu("PPQ");
		ButtonGroup ppqGroup = new ButtonGroup();
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
		midiSettingsMenu.add(PPQ);

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
		midiSettingsMenu.add(MIDI);

		ButtonGroup divisorGroup = new ButtonGroup();
		JMenu divisorMenu = new JMenu("Beat Division");
		for (int i : divisors) {
			JRadioButtonMenuItem divisor = new JRadioButtonMenuItem(String.valueOf(i));
			divisor.setActionCommand("divisor---"+ i);
			divisor.addActionListener(this);
			if (i == 4) divisor.setSelected(true);
			divisorMenu.add(divisor);
			divisorGroup.add(divisor);
		}
		midiSettingsMenu.add(divisorMenu);

		ButtonGroup barlengthGroup = new ButtonGroup();
		JMenu barLengthMenu = new JMenu("Bar Length");
		for (int i : barlengths) {
			JRadioButtonMenuItem bar = new JRadioButtonMenuItem(String.valueOf(i));
			bar.setActionCommand("bar---"+ i);
			bar.addActionListener(this);
			if (i == 4) bar.setSelected(true);
			barLengthMenu.add(bar);
			barlengthGroup.add(bar);
		}
		midiSettingsMenu.add(barLengthMenu);

		//Add Color and Size Settings
		JMenu visualSettings = new JMenu("Visuals");
		menuBar.add(visualSettings);

		JMenu bgColorSettings = new JMenu("Background");
		visualSettings.add(bgColorSettings);
		this.bgChooser = new JColorChooser(new Color(visualizer.getBackgroundColor()));
		this.bgChooser.getSelectionModel().addChangeListener(this);
		bgColorSettings.add(bgChooser);

		JMenu ballColorSettings = new JMenu("Ball");
		visualSettings.add(ballColorSettings);
		this.ballColorChooser = new JColorChooser(new Color(visualizer.getBallColor()));
		this.ballColorChooser.getSelectionModel().addChangeListener(this);
		ballColorSettings.add(ballColorChooser);

		JMenu flashColorSettings = new JMenu("Flash");
		visualSettings.add(flashColorSettings);
		this.flashColorChooser = new JColorChooser(new Color(visualizer.getFlashColor()));
		this.flashColorChooser.getSelectionModel().addChangeListener(this);
		flashColorSettings.add(flashColorChooser);

		JMenu opacitySettings = new JMenu("Tail Length");
		visualSettings.add(opacitySettings);

		//Create the label table
		Hashtable<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
		labels.put(20, new JLabel("Long"));
		labels.put(255, new JLabel("None"));

		this.opacitySlider = new JSlider(JSlider.VERTICAL,
				20, 255, visualizer.getOpacity());
		this.opacitySlider.addChangeListener(this);
		this.opacitySlider.setMajorTickSpacing(20);
		this.opacitySlider.setPaintTicks(true);
		this.opacitySlider.setLabelTable(labels);
		opacitySlider.setPaintLabels(true);
		opacitySettings.add(opacitySlider);

		frame.setJMenuBar(menuBar);
		syncIn =  RWMidi.getInputDevices()[0].createInput();
		if (syncIn != null){
			syncIn.plug(this, "processEvents");
		}
	}

	public void processEvents(SyncEvent syncEvent){
		switch (syncEvent.getStatus()){
		case SyncEvent.TIMING_CLOCK:
			if (started) {
				pulseCount++;
				visualizer.step(pulseCount, ppq, divisor, barLength);
			}
			break;
		case SyncEvent.START:
			started = true;
			break;
		case SyncEvent.STOP:
			started = false;
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
			visualizer.updateSize(frame.getContentPane().getHeight(), frame.getContentPane().getWidth(), ppq);
		}

		if (command.contains("midi---")){
			int midiIn = Integer.valueOf(command.split("---")[1]);
			syncIn.closeMidi();
			syncIn =  RWMidi.getInputDevices()[midiIn].createInput();
			if (syncIn != null){
				syncIn.plug(this, "processEvents");
			}
		}

		if (command.contains("divisor---")){
			divisor = Integer.valueOf(command.split("---")[1]);
		}

		if (command.contains("bar---")){
			barLength = Integer.valueOf(command.split("---")[1]);
		}
	}

	public void stateChanged(ChangeEvent e) {
		Color bgColor = bgChooser.getColor();
		visualizer.setBackgroundColor(bgColor.getRGB());

		Color ballColor = ballColorChooser.getColor();
		visualizer.setBallColor(ballColor.getRGB());

		Color flashColor = flashColorChooser.getColor();
		visualizer.setFlashColor(flashColor.getRGB());

		visualizer.setOpacity(opacitySlider.getValue());
	}
}
