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

public class MA extends AIndicator implements IIntraPeriod
{
	private static final int maspan = 0;
	
	private double ma;
	
	/* clone Constructor */
	
	public MA(int id, IndicatorCandle candle) {
		super(candle,id);
	}
	
	/* init Constructor */
	
	// equivalent im nanotrader (Initialize)
	public MA() 
	{
		super();
		
		IParameter[] parameters = new IParameter[1];
		parameters[maspan] = new IntParameter("MASpan",500,1,1,100);
		
		//getParameters().length;
		
		setParameters(parameters);
		
		try {
			Method foo = this.getClass().getMethod("getMA", null);
			Plotable[] plotables = {new Plotable(foo,Color.GREEN,true)};
			setPlotables(plotables);
		}
		catch (NoSuchMethodException nsme)
		{}
		
		
		setResetable();
	}
	
	public int getMAspan()
	{
		return (Integer)getParameterVal(maspan);
	}
	
	public double getMA()
	{
		return ma;
	}
	
	public double getMA(int ndx)
	{
		return get(ndx).getMA();
	}

	public void calculateIntraPeriod() 
	{
		calculate();
	}
	
	public void calculate() 
	{
		///////////////////////////////
		// MA Calculation
		///////////////////////////////

		/*
		// slow version
		if(candle.isFirst())   // first candle
			ma = Close();
		else
		{
			int anzcandles = Math.min(getMAspan(), candle.getFrameIndex()-1);
			double foo = 0;
			for (int i = 0; i < anzcandles; i++)
			{
				foo += Close(i);
			}
			
			ma = foo/anzcandles;
		}
		*/
		
		// ema = (getMAspan()-1)/getMAspan()*getMA(1)+(1/getMAspan())*Close();
		
		// fast version
		if(candle.isFirst())   // first candle
			ma = Close();
		else if(candle.getIndex() < getMAspan())
			ma = (candle.getIndex()*getMA(1)+Close())/(candle.getIndex()+1);
		else
			ma = (getMAspan()*getMA(1)+Close() - Close(getMAspan()))/getMAspan();
	}
	
	public MA get(int index)
	{
		return (MA)super.get(index);
	}

	public AIndicator clone(IndicatorCandle c)
	{
		MA result = new MA(id,c);
		return result;
	}

	public void reset(IndicatorCandle candle)
	{
		super.reset(candle);
	}
}
