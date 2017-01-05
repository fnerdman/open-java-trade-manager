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


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import de.smpfe.trader.enums.TimeFrame;

public class Candle
{
	private int high, low, open, close, volume;
	private long startTimeStamp;
	private TimeFrame timeFrame;
	private boolean isIntraPeriod = false;
	private int ndx;
	
	/* Constructors START */
	
	public Candle(int h, int l, int s, int e, int vol, TimeFrame t, long st, int ndx) {
		high = h;
		low = l;
		open = s;
		close = e;
		volume = vol;
		timeFrame = t;
		startTimeStamp = st;
		this.ndx = ndx;
	}
	
	public Candle(int open, TimeFrame timeFrame, long startTime, int ndx) 
	{
		reset(open, timeFrame, startTime, ndx);
	}
	
	public Candle(Candle candle) 
	{
		this(candle.getOpen(),candle.getTimeFrame(),candle.getStartTimeStamp(), candle.getIndex());
		
		setClose(candle.getClose());
		setHigh(candle.getHigh());
		setLow(candle.getLow());
	}
	
	/* Constructors END */
	
	// methods
	public String toString()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy HH:mm");
	
		return "StartTime: " + sdf.format(new Date(startTimeStamp)) + ", Index: " + ndx + ", Open: " + open + ", Close: " + close + ", High: " + high + ", Low: " + low + ", Volume: " + volume; 
	}
	
	public boolean isComplete() 
	{
		// ist die Candle beendet?

		Calendar tmp = new GregorianCalendar();
		return tmp.after(getEndTime());

	}//Candle.isComplete(candle) || candle.isComplete
	
	public static boolean isComplete(Candle c) 
	{
		// ist die Candle beendet?
		Calendar tmp = new GregorianCalendar();
		return tmp.after(c.getEndTime());
	}
	
	public void reset(int open, TimeFrame timeFrame, long startTime, int ndx)
	{
	
		this.open = open;
		this.close = open;
		this.ndx = ndx;
		this.setLow(open);		//this.low=open;
		this.setHigh(open);
		this.timeFrame = timeFrame;
		this.startTimeStamp = startTime;
	}
	
	public void reset(Candle candle) 
	{
		reset(candle.getOpen(),candle.getTimeFrame(),candle.getStartTimeStamp(), candle.getIndex());
		
		setClose(candle.getClose());
		setHigh(candle.getHigh());
		setLow(candle.getLow());
	}
	
	public void update(int price) {
		if (price > getHigh())
			setHigh(price);
		if (price < getLow())
			setLow(price);
		setClose(price);
	}
	
	public void update(Candle bc) {
		reset(bc);
	}
	
	/*public int getStartMinutes() {
		return new Date(startTimeStamp).get(Calendar.MINUTE);
	}
	
	public int getStartHours() {
		return startTimeStamp.get(Calendar.HOUR_OF_DAY);
	}*/

	public Calendar getEndTime() {
		
		Calendar endTime = getStartTime();
		endTime.add(timeFrame.getTimeType(), timeFrame.getAmount());
		
		return endTime;
	}
	
	// getter and setter
	public Calendar getStartTime() 
	{
		Calendar result = new GregorianCalendar();
		result.setTimeInMillis(startTimeStamp);
		return result;
	}
	
	public long getStartTimeStamp() {
		return startTimeStamp;
	}
	
	public void setStartTimeStamp(long startTime) {
		this.startTimeStamp = startTime;
	}

	public TimeFrame getTimeFrame() {
		return timeFrame;
	}

	public void setHigh(int high) {
		this.high = high;
	}

	public int getHigh() {
		return high;
	}

	public void setLow(int low) {
		this.low = low;
	}

	public int getLow() {
		return low;
	}
	public int getOpen() {
		return open;
	}

	public void setOpen(int open) {
		this.open = open;
	}

	public int getClose() {
		return close;
	}

	public void setClose(int close) {
		this.close = close;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public int getVolume() {
		return volume;
	}

	public boolean isIntraPeriod() {
		return isIntraPeriod;
	}

	public void setIntraPeriod(boolean isIntraPeriod) {
		this.isIntraPeriod = isIntraPeriod;
	}

	public int getIndex() {
		return ndx;
	}

	public void setIndex(int ndx) {
		this.ndx = ndx;
	}
	
	public int getDistance(Candle c)
	{
		return getIndex() - c.getIndex();
	}
}
