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

import de.smpfe.trader.data.Dependency;
import de.smpfe.trader.data.indicator.AIndicator;
import de.smpfe.trader.data.indicator.IndicatorCandle;
import de.smpfe.trader.enums.TimeFrame;

public abstract class ADependentInstrument extends AChartInstrument
{
	private HashMap<TimeFrame,Dependency[]> dependencies = new HashMap<TimeFrame,Dependency[]>();
	private HashMap<TimeFrame,AIndicator[]> indicators = new HashMap<TimeFrame,AIndicator[]>();
	private ATradeManagement tradeManagement;
	
	public void update(Observable o, Object arg) 
	{
		// update dependencies
		for(TimeFrame tf: dependencies.keySet())
		{
			IndicatorCandle ic = getCandle(tf);
			AIndicator[] tfindis = indicators.get(tf);
			for(int k = 0; k < tfindis.length; k++)
			{
				tfindis[k] = tfindis[k].get(ic);
			}
		}
	}
	
	protected void addDependencies(TimeFrame tf, Dependency[] deps)
	{
		dependencies.put(tf, deps);
	}

	public HashMap<TimeFrame,Dependency[]> getDependencies() {
		return dependencies;
	}

	public void setTradeManagement(ATradeManagement tradeManagement) {
		this.tradeManagement = tradeManagement;
	}

	public ATradeManagement getTradeManagement() {
		return tradeManagement;
	}

	public void setIndicators(TimeFrame tf, AIndicator[] depIndi) {
		indicators.put(tf, depIndi);
	}
	
	public AIndicator[] getIndicators(TimeFrame tf)
	{
		return indicators.get(tf);
	}
}
