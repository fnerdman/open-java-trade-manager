/*******************************************************************************
 * MIT License
 *
 * Copyright (c) 2017 Frieder Paape
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/

package de.smpfe.trader.data.chart;

import java.util.Date;

public class PriceElement 
{
	private long time;
	private int price;
	private int volume;
	private int ticks;
	private boolean intraPeriod;
	
	/* Constructors START */
	public PriceElement(int value,int volume,int ticks)
	{
		this(value,volume,ticks,new Date().getTime());
	}
	
	public PriceElement(int value, int volume, int ticks,  long time)
	{
		this.price = value;
		this.ticks = ticks;
		this.volume = volume;
		this.time = time;
		setIntraPeriod(false);
	}
	
	/* Constructors END */
	
	// getter and setter
	
	public long getTime() {
		return time;
	}

	public int getPrice() {
		return price;
	}

	public int getTicks() {
		return ticks;
	}
	
	public int getVolume(){
		return volume;
	}


	public boolean isIntraPeriod() {
		return intraPeriod;
	}

	public void setIntraPeriod(boolean intraPeriod) {
		this.intraPeriod = intraPeriod;
	}
}
