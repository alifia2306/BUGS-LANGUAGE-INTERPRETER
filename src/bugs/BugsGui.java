package bugs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.Timer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;




/**
 * GUI for Bugs language.
 * @author Dave Matuszek
 * @version 2015
 */
/**
 * @author ahaidry
 *
 */
/**
 * @author ahaidry
 *
 */
public class BugsGui extends JFrame{
    private static final long serialVersionUID = 1L;
    JPanel display;
    JSlider speedControl;
    int speed;
    JButton stepButton;
    JButton runButton;
    JButton pauseButton;
    JButton resetButton;
    static String prog;
    static Interpreter interpreter;
    
    /**
     * GUI constructor.
     */
    public BugsGui() {
        super();
        prog = "";
        interpreter = new Interpreter();
        setSize(600, 600);
        setLayout(new BorderLayout());
        createAndInstallMenus();
        createDisplayPanel();
        createControlPanel();
        initializeButtons();
        setVisible(true);
       
        

    }

    /**
     * Method for creating and installing buttons
     */
    private void createAndInstallMenus() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");        
        JMenu helpMenu = new JMenu("Help");
        JMenuItem quitMenuItem = new JMenuItem("Quit");
        JMenuItem loadMenuItem = new JMenuItem("Load");
        JMenuItem helpMenuItem = new JMenuItem("Help");
        
        menuBar.add(fileMenu);
        fileMenu.add(loadMenuItem);
        fileMenu.add(quitMenuItem);
        quitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                quit();
            }});
        
        
	      
        loadMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
            	prog = SimpleIO.load();
            	interpreter.Bugs.clear();
            	interpreter.bugs.clear();
            	interpreter.cmd.clear();
            	interpreter.startThings();
            }});
        
        menuBar.add(helpMenu);
        helpMenu.add(helpMenuItem);
        helpMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                help();
            }});
        
        this.setJMenuBar(menuBar);
    }

    
    /**
     * Method to create display Panel
     */
    private void createDisplayPanel() {
        display = new BugView(interpreter);
        add(display, BorderLayout.CENTER);
    }

    /**
     * Method to create control Panel
     */
    private void createControlPanel() {
        JPanel controlPanel = new JPanel();
        
        addSpeedLabel(controlPanel);       
        addSpeedControl(controlPanel);
        addStepButton(controlPanel);
        addRunButton(controlPanel);
        addPauseButton(controlPanel);
        addResetButton(controlPanel);
        
        add(controlPanel, BorderLayout.SOUTH);
    }

    /**
     * Method to add Speed Label
     */
    private void addSpeedLabel(JPanel controlPanel) {
        controlPanel.add(new JLabel("Speed:"));
    }

    /**
     * Method to add Speed Control
     */
    private void addSpeedControl(JPanel controlPanel) {
        speedControl = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 50);
        speed = 50;
        speedControl.setMajorTickSpacing(10);
        speedControl.setMinorTickSpacing(5);
        speedControl.setPaintTicks(true);
        speedControl.setPaintLabels(true);
        speedControl.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent arg0) {
            	int sliderValue = speedControl.getValue();
            	int timeDelay = (1000 - (10*sliderValue));
                resetSpeed(timeDelay);
            }
        });
        controlPanel.add(speedControl);
    }

    /**
     * Method to add Step button
     */
    private void addStepButton(JPanel controlPanel) {
        stepButton = new JButton("Step");
        stepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stepAnimation();
            }
        });
        controlPanel.add(stepButton);
    }
    
    /**
     * Method to add Run button
     */
    private void addRunButton(JPanel controlPanel) {
        runButton = new JButton("Run");
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runAnimation();
            }
        });
        controlPanel.add(runButton);
    }
    
    /**
     * Method to add Pause button
     */
    private void addPauseButton(JPanel controlPanel) {
        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pauseAnimation();
            }
        });
        controlPanel.add(pauseButton);
    }
    
    /**
     * Method to add Reset button
     */
    private void addResetButton(JPanel controlPanel) {
        resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetAnimation();
            }
        });
        controlPanel.add(resetButton);
    }
    
    private void initializeButtons() {
        stepButton.setEnabled(true);
        runButton.setEnabled(true);
        pauseButton.setEnabled(true);
        resetButton.setEnabled(true);
        

    }
    
    /**
     * Method to add Initialize button
     */
    private void resetSpeed(int value) {
        interpreter.delay = value;
    }

    /**
     * Method to add step animation
     */
    protected void stepAnimation() {
        runButton.setEnabled(true);
        interpreter.unblockAllBugs();
        display.repaint();
        stepButton.setEnabled(true);
        runButton.setEnabled(true);
        pauseButton.setEnabled(true);
        resetButton.setEnabled(true);
    }
    
    /**
     * Method to add run animation
     */
    protected void runAnimation() {
    	interpreter.blocked = 0;
    	interpreter.start();
        stepButton.setEnabled(true);
        runButton.setEnabled(true);
        pauseButton.setEnabled(true);
        resetButton.setEnabled(true);
    }
    
    /**
     * Method to addpause animation
     */
    protected void pauseAnimation() {
       Interpreter.blocked = 1;
        stepButton.setEnabled(true);
        runButton.setEnabled(true);
        pauseButton.setEnabled(true);
        resetButton.setEnabled(true);
    }
    
    /**
     * Method to reset animation
     */
    protected void resetAnimation() {
    	

    	for(Bug bug : interpreter.Bugs){
    		interpreter.terminateBug(bug);
    	}
    	interpreter.Bugs.clear();
    	interpreter.bugs.clear();
    	interpreter.cmd.clear();
    	
    	interpreter.startThings();

        stepButton.setEnabled(true);
        runButton.setEnabled(true);
        pauseButton.setEnabled(true);
        resetButton.setEnabled(true);
    }

    protected void help() {
        // TODO Auto-generated method stub
    }
    
    /**
     * Method to quit
     */
    protected void quit() {
        System.exit(0);
    }

    /** Main method
     * @param args
     */
    public static void main(String[] args) {
        new BugsGui();
    }
    


}