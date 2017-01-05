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

package de.smpfe.trader.data.indicator;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Observable;
import java.util.Observer;

import de.smpfe.trader.chartinstrument.Message;
import de.smpfe.trader.chartinstrument.MessageType;
import de.smpfe.trader.data.chart.Candle;
import de.smpfe.trader.data.chart.PriceElement;
import de.smpfe.trader.enums.TimeFrame;

public class RollingFrameChart extends Observable implements Observer
{
	private static final int limit = 500;
	private int last;
	private int lastAbs;
	private IndicatorCandle[] frameCandles;
	private boolean wasIntraPeriodUpdate = false;
	private Candle c;
	private long nextCandleStart;
	
	protected TimeFrame timeFrame;
	// ursprungsindikatoren
	protected AIndicator[] indicators;
	
	public RollingFrameChart(TimeFrame timeFrame, AIndicator[] indicators)
	{
		this.timeFrame = timeFrame;
		this.indicators = indicators;
		frameCandles = new IndicatorCandle[limit];
		last = -1;
	}
	
	public void addCandles(Candle[] bcs)
	{
		for(Candle bc: bcs)
		{
			addCandle(bc);
		}
	}
	
	public void addCandles(ArrayList<Candle> bcs)
	{
		for(Candle bc: bcs)
		{
			addCandle(bc);
		}
	}
	
	public void addCandle(Candle bc)
	{
		int beforlast = last;
		last=(last+(wasIntraPeriodUpdate?0:1))%limit;
		int next = (last+1)%limit;
		
		IndicatorCandle tmp = frameCandles[last];
		
		// frame ist noch nicht voll, neue candle wird initzialisiert
		if(tmp==null)
		{
			tmp = new IndicatorCandle(this,bc);
			tmp.addIndicators(indicators);
			
			frameCandles[last] = tmp;
		}
		// alte candle ist vorhanden, wird einfach geresetett
		else if(!wasIntraPeriodUpdate)
		{
			tmp.reset(bc);
		}
		// wurde bereits geresetet, da intraperiod update
		else
		{   // update der Candle Daten intraperiod 
			tmp.update(bc);
		}
		
		tmp.setNext(null);
		
		if(frameCandles[next]!=null)
			frameCandles[next].setLast(null);
		
		if(beforlast >= 0)
		{
			if(frameCandles[beforlast]!=null)
				frameCandles[beforlast].setNext(tmp);
			
			tmp.setLast(frameCandles[beforlast]);
		}
		
		lastAbs = tmp.getIndex();
		// berechnet indicatoren
		tmp.complete();
		
		// benachrichtigt Observer (zur Zeit nur der Starter; sp�ter z.B. Handelssysteme)
		Message m = new Message(this, MessageType.CANDLE);
		m.setPayload(tmp);
		setChanged();
	    notifyObservers(m);
	    
	    wasIntraPeriodUpdate = false;
	}
	
	public IndicatorCandle getCandle(int cindex, int offset)
	{	
		offset = Math.max(offset, 0);
		
		if(cindex-offset<0 || cindex-offset<=lastAbs-limit)
			throw new IndexOutOfBoundsException("lastAbs: "+lastAbs+" cindex: "+cindex+" offset: "+offset+" limit: "+limit);
		
		
		IndicatorCandle tmp = frameCandles[(cindex-offset)%limit];
		// cindex-offset>last-limit
		//int maxoffset = mod(cindex-(last+1),limit);
		
		return tmp;
		
	}
	
	private int mod(int x, int y)
	{
	    int result = x % y;
	    return result < 0? result + y : result;
	}

	public void update(Observable underlying, Object o) 
	{
		PriceElement pe = (PriceElement)o;
		
		if(c==null)
		{
			c = new Candle(pe.getPrice(), timeFrame, timeFrame.getNearestStartTime(pe.getTime()), 0);
			nextCandleStart = timeFrame.getNearestEndTime(pe.getTime());
		}
		
		c.setIntraPeriod(pe.isIntraPeriod());
		if(c.isIntraPeriod())
			addCandle(c);
		
		if(pe.getTime()>=nextCandleStart)
		{
			addCandle(c);
			c.reset(pe.getPrice(), timeFrame, timeFrame.getNearestStartTime(pe.getTime()), c.getIndex()+1);
			nextCandleStart = timeFrame.getNearestEndTime(pe.getTime());
		}
		else
		{
			c.update(pe.getPrice());
		}

		if(pe.isIntraPeriod())
			wasIntraPeriodUpdate=true;
	}
	

	public TimeFrame getTimeFrame() {
		return timeFrame;
	}

	public static int getLimit() {
		return limit;
	}
}
