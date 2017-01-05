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

package de.smpfe.trader.indicators;

import java.awt.Color;
import java.lang.reflect.Method;

import de.smpfe.trader.data.IIntraPeriod;
import de.smpfe.trader.data.Plotable;
import de.smpfe.trader.data.indicator.AIndicator;
import de.smpfe.trader.data.indicator.IndicatorCandle;
import de.smpfe.trader.data.parameters.IParameter;
import de.smpfe.trader.data.parameters.IntParameter;

public class HighLow extends AIndicator implements IIntraPeriod
{
	private static final int HIGHLOW = 0;
	
	private double lowSeries;
	private double highSeries;
	
	/* clone Constructor */
	
	public HighLow(int id, IndicatorCandle candle) {
		super(candle,id);
	}
	
	/* init Constructor */
	
	// equivalent im nanotrader (Initialize)
	public HighLow() 
	{
		super();
		
		IParameter[] parameters = new IParameter[1];
		parameters[HIGHLOW] = new IntParameter("HighLowSpan",50,1,1,5);
		
		setParameters(parameters);

		try {
			Plotable[] plotables = {
					new Plotable(this.getClass().getMethod("getHighSeries", null),Color.BLACK,true),
					new Plotable(this.getClass().getMethod("getLowSeries", null),Color.BLUE,true)
					};
			
			setPlotables(plotables);
		}
		catch (NoSuchMethodException nsme)
		{}
		
		
		
		setResetable();
	}
	
	public int getHighLowspan()
	{
		return (Integer)getParameterVal(HIGHLOW);
	}
	
	public double getHighSeries()
	{
		return highSeries;
	}
	
	public double getHighSeries(int ndx)
	{
		return get(ndx).getHighSeries();
	}
	
	public double getLowSeries()
	{
		return highSeries;
	}
	
	public double getLowSeries(int ndx)
	{
		return get(ndx).getHighSeries();
	}
	


	public void calculateIntraPeriod() 
	{
		calculate();
	}
	
	public void calculate() 
	{
		///////////////////////////////
		// HighLow Calculation
		///////////////////////////////

		// first candle
				if(candle.isFirst()){
					highSeries = High();
					lowSeries = Low();
				}
				else{
				if(	getHighLowspan()<candle.getIndex()){
					highSeries = Math.max(High(), getHighSeries(1));
					lowSeries = Math.min(Low(), getLowSeries(1));
				}
				else{
					highSeries = High();
					lowSeries = Low();
					for (int i=1; i<getHighLowspan();i++){
						highSeries = Math.max(High(i), highSeries);
						lowSeries = Math.min(Low(i), lowSeries);
					}
				}
				}
		
	}
	
	public HighLow get(int index)
	{
		return (HighLow)super.get(index);
	}

	public AIndicator clone(IndicatorCandle c)
	{
		HighLow result = new HighLow(id,c);
		return result;
	}

	public void reset(IndicatorCandle candle)
	{
		super.reset(candle);
	}
}
