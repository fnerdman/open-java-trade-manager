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
package de.smpfe.trader.chartinstrument;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import de.smpfe.trader.data.indicator.IndicatorCandle;
import de.smpfe.trader.data.parameters.IParameter;
import de.smpfe.trader.enums.TimeFrame;

public abstract class AChartInstrument extends Observable  implements IInterpretable, IChartInstrument, Observer {

	private MailBox mailBox = new MailBox(this);
	private HashMap<TimeFrame,IndicatorCandle> candles = new HashMap<TimeFrame,IndicatorCandle>();
	private IParameter[] parameters = new IParameter[0];
	
	protected IndicatorCandle getCandle(TimeFrame tf)
	{
		return candles.get(tf);
	}
	
	public void updateCandle(IndicatorCandle ic) 
	{	
		candles.put(ic.getTimeFrame(), ic);
	}
	
	public TimeFrame getLowestTimeFrame()
	{
		for(TimeFrame tf: TimeFrame.values())
		{
			if(candles.get(tf)!= null)
				return tf;
		}
		
		return null;
	}


	public MailBox getMailBox() {
		return mailBox;
	}


	protected void setParameters(IParameter[] parameters) {
		this.parameters = parameters;
	}


	public IParameter[] getParameters() {
		return parameters;
	}

}
