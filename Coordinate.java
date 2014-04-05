/* 
 * Coordinate.java 
 * 
 * Version: 
 *     $Id$ 
 * 
 * Revisions: 
 *     $Log$ 
 */
/**
 * This program encapsulates the coordinates of a peer
 *
 * @author      Shobhit Dutia
 */

import java.io.Serializable;


public class Coordinate implements Serializable {

	private static final long serialVersionUID = 1L;
	double lx, ly, hx, hy;
	
	/**
	 * get lower x coordinate.
	 * 
	 * @return 	 lower x coordinate
	 */

	public double getLx() {
		return lx;
	}
	
	/**
	 * set lower x coordinate.
	 * 
	 * @param lower x coordinate
	 */

	public void setLx(double lx) {
		this.lx = lx;
	}

	/**
	 * get lower y coordinate.
	 * 
	 * @return 	 lower y coordinate
	 */

	public double getLy() {
		return ly;
	}
	
	/**
	 * set lower y coordinate.
	 * 
	 * @param lower y coordinate
	 */

	public void setLy(double ly) {
		this.ly = ly;
	}

	/**
	 * get higher x coordinate.
	 * 
	 * @return  higher x coordinate
	 */

	public double getHx() {
		return hx;
	}
		
	/**
	 * set higher x coordinate.
	 * 
	 * @param higher x coordinate
	 */

	public void setHx(double hx) {
		this.hx = hx;
	}

	/**
	 * get higher y coordinate.
	 * 
	 * @return 	 lower x coordinate
	 */

	public double getHy() {
		return hy;
	}
	
	/**
	 * set higher y coordinate.
	 * 
	 * @param higher y coordinate
	 */

	public void setHy(double hy) {
		this.hy = hy;
	}
	
}
