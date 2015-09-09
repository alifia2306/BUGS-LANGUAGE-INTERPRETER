package bugs;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.Timer;

public class BugView extends JPanel implements ActionListener{
	Interpreter interpreter;
	Timer timer = new Timer(1000, this);
	BugView(Interpreter interpreter){
		this.interpreter = interpreter;
		timer.start();
	}
	
	
	/**
	 * Paints a triangle to represent this Bug.
	 * 
	 * @param g Where to paint this Bug.
	 */
	@Override
	public synchronized void paint(Graphics g) {
		
		
		int size = interpreter.cmd.size();

		for(int i = 0; i < interpreter.cmd.size(); i++) {
			
			if (interpreter.cmd.get(i) == null) {
				System.out.println("Wrong " + i);
				continue;
			}
			
			System.out.println("Correct " + i);
		    g.setColor(interpreter.cmd.get(i).color);
			int x1 = (int) scaleX(interpreter.cmd.get(i).x1);
			int y1 = (int) scaleY(interpreter.cmd.get(i).y1);
			int x2 = (int) scaleX(interpreter.cmd.get(i).x2);
			int y2 = (int) scaleY(interpreter.cmd.get(i).y2);
			g.drawLine(x1, y1, x2, y2);
		}
		
		
		for(int j = 0; j < interpreter.Bugs.size(); j++){
			 if (interpreter.Bugs.get(j).BugColor == null) continue;
			    g.setColor(interpreter.Bugs.get(j).BugColor);
			    
			    int x4 = (int) (scaleX(interpreter.Bugs.get(j).x) + (computeDeltaX(12, (int)interpreter.Bugs.get(j).angle)));
			    int x5 = (int) (scaleX(interpreter.Bugs.get(j).x) + (computeDeltaX(6, (int)interpreter.Bugs.get(j).angle - 135)));
			    int x6 = (int) (scaleX(interpreter.Bugs.get(j).x) + (computeDeltaX(6, (int)interpreter.Bugs.get(j).angle + 135)));
			    
			    int y4 = (int) (scaleY(interpreter.Bugs.get(j).y) + (computeDeltaY(12, (int)interpreter.Bugs.get(j).angle)));
			    int y5 = (int) (scaleY(interpreter.Bugs.get(j).y) + (computeDeltaY(6, (int)interpreter.Bugs.get(j).angle - 135)));
			    int y6 = (int) (scaleY(interpreter.Bugs.get(j).y) + (computeDeltaY(6, (int)interpreter.Bugs.get(j).angle + 135)));
			    g.fillPolygon(new int[] { x4, x5, x6 }, new int[] { y4, y5, y6 }, 3);
		}


	}
	
	public double scaleX(double x){
		return (x /100)* this.getWidth();
	}

	public double scaleY(double y){
		return (y /100) * this.getHeight();
	}
	
	/**
	 * Computes how much to move to add to this Bug's x-coordinate,
	 * in order to displace the Bug by "distance" pixels in 
	 * direction "degrees".
	 * 
	 * @param distance The distance to move.
	 * @param degrees The direction in which to move.
	 * @return The amount to be added to the x-coordinate.
	 */
	private static double computeDeltaX(int distance, int degrees) {
	    double radians = Math.toRadians(degrees);
	    return distance * Math.cos(radians);
	}

	/**
	 * Computes how much to move to add to this Bug's y-coordinate,
	 * in order to displace the Bug by "distance" pixels in 
	 * direction "degrees.
	 * 
	 * @param distance The distance to move.
	 * @param degrees The direction in which to move.
	 * @return The amount to be added to the y-coordinate.
	 */
	private static double computeDeltaY(int distance, int degrees) {
	    double radians = Math.toRadians(degrees);
	    return distance * Math.sin(-radians);
	}
	
	/**
	 * Action Performed method to repaint graphics.
	 */

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == timer){
			repaint();
		}
		
	}
}