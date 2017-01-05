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
package de.smpfe.trader.data;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import de.smpfe.trader.chartinstrument.Message;
import de.smpfe.trader.chartinstrument.MessageType;
import de.smpfe.trader.data.indicator.AIndicator;
import de.smpfe.trader.data.indicator.RollingFrameChart;
import de.smpfe.trader.enums.TimeFrame;

public class Underlying extends Observable implements Observer
{	
	private String id;
	private InstrumentObservable instrObserv;
	private final Object lockObj = new Object();
	private HashMap<TimeFrame,RollingFrameChart> charts;
	
	public Underlying(String id)
	{
		this.id = id;
		instrObserv = new InstrumentObservable();
		charts = new HashMap<TimeFrame,RollingFrameChart>();
	}

	public void update(Observable o, Object arg) 
	{
		setChanged();
		// arg allways PriceElement!
		notifyObservers(arg);
		instrObserv.notifyInstruments(lockObj);
	}
	
	public void addInstrumentObserver(Observer o)
	{
		instrObserv.addObserver(o);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public RollingFrameChart addChart(TimeFrame tf, AIndicator[] indis) {
		RollingFrameChart rfc = new RollingFrameChart(tf, indis);
		addObserver(rfc);
		charts.put(tf, rfc);
		return rfc;
	}
	
	public RollingFrameChart getLowestCandleChart()
	{
		for(TimeFrame tf: TimeFrame.values())
		{
			if(charts.containsKey(tf))
				return charts.get(tf);
		}
		return null;
	}
	
	public class InstrumentObservable extends Observable
	{
		public void notifyInstruments(Object o)
		{
			setChanged();
			//synchronized(o) TODO
			{
				notifyObservers(new Message(o,MessageType.UPDATE));
			}
		}
	}
}
