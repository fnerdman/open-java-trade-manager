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
import java.util.HashMap;

import de.smpfe.trader.data.IIntraPeriod;
import de.smpfe.trader.data.chart.Candle;

public class IndicatorCandle extends Candle 
{
	private IndicatorCandle last, next;
	private AIndicator[] indicator;
	private HashMap<Integer,AIndicator> indicators;
	
	// read only pointer!!!
	// werden hier nicht veraendert, sind teil des rolling frames
	// 
	
	private RollingFrameChart rfc;
	/* Constructors START */
	
	public IndicatorCandle(RollingFrameChart rfc, Candle candle) 
	{
		super(candle);
		
		this.rfc = rfc;
		indicators = new HashMap<Integer,AIndicator>();
	}
	/* Constructors END */
	
	
	// reset
	public void reset(Candle candle)
	{
		super.reset(candle);
		for(int k = 0;k<indicator.length;k++)
		{
			if(indicator[k].isResetable())
				indicator[k].reset(this);
			else
				indicator[k] = indicator[k].clone(this);
			
			indicators.put(indicator[k].getId(), indicator[k]);
		}
	}

	public void setLast(IndicatorCandle last) {
		this.last = last;
	}

	public void setNext(IndicatorCandle next) {
		this.next = next;
	}

	public IndicatorCandle getLast() {
		return this.last;
	}

	public IndicatorCandle getNext() {
		return this.next;
	}

	public void complete() {
		calculateIndicators();
	}

	public void addIndicators(AIndicator[] is) 
	{
		indicator = new AIndicator[is.length];
		for(int k = 0;k<is.length;k++)
		{
			indicator[k] = is[k].clone(this);
		}
		for(AIndicator i: indicator)
		{
			indicators.put(i.getId(),i);
		}
	}

	public HashMap<Integer,AIndicator> getIndicators() {
		return indicators;
	}
	
	public AIndicator getIndicator(int id) {
		return indicators.get(id);
	}

	private void calculateIndicators() 
	{
		for(AIndicator indi: indicator){
			
			if(isIntraPeriod())
			{
				if(indi instanceof IIntraPeriod)
					((IIntraPeriod)indi).calculateIntraPeriod();
			}
			else
				indi.calculate();
			
			indi.notifyObservers();
		}
	}
	
	public int getFrameIndex()
	{
		return Math.min(getIndex(), rfc.getLimit());
	}

	/*
	public IndicatorCandle clone() {
		IndicatorCandle result = new IndicatorCandle(getOpen(), getTimeFrame(), getStartTimeStamp());
		result.indicators.putAll(indicators);
		result.last = last;
		result.next = next;
		return result;
	}
	*/
	
	public IndicatorCandle get(int index)
	{
		return rfc.getCandle(getIndex(), index);
	}
	
	public boolean isCurrent()
	{
		return next==null;
	}
	
	public boolean isFirst()
	{
		return last==null;
	}


	public Candle cloneSimpleCandle() {
		return new Candle(this);
	}
}
