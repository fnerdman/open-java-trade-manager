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

package de.smpfe.trader.enums;

import java.util.Calendar;
import java.util.GregorianCalendar;

public enum TimeFrame
{
	/*
	 * WARNING!!!
	 * Times must be in ascending order, otherwise dependent code
	 * will be corrupted!
	 * */
	SEK30(Calendar.SECOND,30),
	MIN1(Calendar.MINUTE,1),
	MIN2(Calendar.MINUTE,2),
	MIN3(Calendar.MINUTE,3),
	MIN5(Calendar.MINUTE,5),
	MIN10(Calendar.MINUTE,10),
	MIN15(Calendar.MINUTE,15),
	MIN30(Calendar.MINUTE,30),
	HOUR1(Calendar.HOUR,1),
	HOUR2(Calendar.HOUR,2),
	HOUR4(Calendar.HOUR,4),
	HOUR6(Calendar.HOUR,6),
	HOUR12(Calendar.HOUR,12),
	DAY(Calendar.HOUR,24);
	
	private int timeType, amount;

	private TimeFrame(int timeType, int amount)
	{
		this.timeType = timeType;
		this.amount = amount;
	}
	
	public int getTimeType() {
		return timeType;
	}

	public int getAmount() {
		return amount;
	}
	
	public String toString(){
		return super.toString() + " timeType: "+ timeType + " amount: " + amount;
	}
	
	public long getNearestStartTime(long time)
	{
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(time);
		int val = (cal.get(timeType)/amount)*amount;
		cal.set(timeType, val);
		switch(timeType)
		{
		case Calendar.SECOND:
			break;
		case Calendar.MINUTE:
			cal.set(Calendar.SECOND, 0);
			break;
		case Calendar.HOUR:
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, 0);
			break;
		}
		
		return cal.getTimeInMillis();
	}
	
	public long getNearestEndTime(long time)
	{
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(time);
		int val = ((cal.get(timeType)/amount)+1)*amount;
		cal.set(timeType, val);
		//Math.round()
		switch(timeType)
		{
		case Calendar.SECOND:
			break;
		case Calendar.MINUTE:
			cal.set(Calendar.SECOND, 0);
			break;
		case Calendar.HOUR:
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, 0);
			break;
		}
		
		return cal.getTimeInMillis();
	}
}
