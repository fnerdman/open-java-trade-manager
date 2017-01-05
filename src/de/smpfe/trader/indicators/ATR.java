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

public class ATR extends AIndicator implements IIntraPeriod
{
	private static final int ATR = 0;
	
	private double atr;
	
	/* clone Constructor */
	
	public ATR(int id, IndicatorCandle candle) {
		super(candle,id);
	}
	
	/* init Constructor */
	
	// equivalent im nanotrader (Initialize)
	public ATR() 
	{
		super();
		
		IParameter[] parameters = new IParameter[1];
		parameters[ATR] = new IntParameter("AtrSpan",500,1,1,100);
		
		setParameters(parameters);
		
		try {
			Method foo = this.getClass().getMethod("getAtr", null);
			Plotable[] plotables = {new Plotable(foo,Color.GREEN)};
			//setPlotables(plotables);
		}
		catch (NoSuchMethodException nsme)
		{}
		
		
		setResetable();
	}
	
	public int getAtrspan()
	{
		return (Integer)getParameterVal(ATR);
	}
	
	public double getAtr()
	{
		return atr;
	}
	
	public double getAtr(int ndx)
	{
		return get(ndx).getAtr();
	}

	public void calculateIntraPeriod() 
	{
		calculate();
	}
	
	public void calculate() 
	{
		///////////////////////////////
		// ATR Calculation
		///////////////////////////////

		/*		true range		*/
		int tr;

		// first candle
		if(candle.isFirst())
			tr = High()-Low();
		else
			tr= Math.max(Math.max(High()-Low(),
					Math.abs(High()-Close(1))),
						Math.abs(Low()-Close(1)));   

		/*		average true range		*/

		// first candle
		if(candle.isFirst())
			atr = tr;
		/* 
			EMA mit _atrspan vielen Perioden
			new = (old*(_atrspan-1)+tr)/_atrspan 
		*/
		else 
		{
			atr = getAtr(1) + (2.0/(1+getAtrspan()))*(tr-getAtr(1));
		}
	}
	
	public ATR get(int index)
	{
		return (ATR)super.get(index);
	}

	public AIndicator clone(IndicatorCandle c)
	{
		ATR result = new ATR(id,c);
		return result;
	}

	public void reset(IndicatorCandle candle)
	{
		super.reset(candle);
	}
}
