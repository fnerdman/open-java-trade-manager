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

import de.smpfe.trader.data.Dependency;
import de.smpfe.trader.data.Plotable;
import de.smpfe.trader.data.indicator.AIndicator;
import de.smpfe.trader.data.indicator.IndicatorCandle;
import de.smpfe.trader.data.parameters.BoolParameter;
import de.smpfe.trader.data.parameters.DoubleParameter;
import de.smpfe.trader.data.parameters.IParameter;
import de.smpfe.trader.data.parameters.IntParameter;

public class MACD extends AIndicator 
{
	/*	PARAMETER CONSTS	*/

	private static final int TIMSCA = 0;
		
	/*	DEPENDENCY CONSTS	*/
	
	//private static final int ATRID = 0;
	
	/*	GLOBALVARS CONSTS	*/
	
	private static final int myGLOBVAR = 0;
	
	// die klasse Globalvars wird hier wie in c 'struct' benutzt, aewivqlent zu numeric/bool variablen in express
	private class GlobalVars
	{
		public double fastparam;
		public double slowparam;
		public double triggerparam;

		public double shortmean;
		public double longmean;		
	}

	// series
	private double macd;
	private double signal;
	private double diff;   //macd-signal

	/* clone Constructor */
	
	public MACD(IndicatorCandle candle, int id) 
	{
		super(candle,id);
	}
	
	/* init Constructor */
	public MACD() {
		super();
		
		IParameter[] parameters = new IParameter[1];
		
		parameters[TIMSCA] = new DoubleParameter("TimeScale",10,0.1,0.1,1);
		
		setParameters(parameters);
		
		
		try {
			Plotable[] plotables = {
				//	new Plotable(this.getClass().getMethod("getSignal", null),Color.BLACK),
				//	new Plotable(this.getClass().getMethod("getMacd", null),Color.BLACK),
					new Plotable(this.getClass().getMethod("getDiff", null),Color.BLACK),
					};
			
			setPlotables(plotables);
		}
		catch (NoSuchMethodException nsme)
		{}
		//TODO getDiff l�sst sich nicht einkommentieren; auch try catch einkommentieren geht nicht
		
		
		Object[] globalVars = new Object[1];
		
		globalVars[myGLOBVAR] = new GlobalVars();
		
		setGlobalVars(globalVars);
		
		setResetable();
	}

	public void calculate() 
	{

		///////////////////////////////
		// Calculation Macd
		///////////////////////////////

		double TimeScale = (Double)getParameterVal(TIMSCA);
		
 		if (candle.isFirst())   // first candle
 		{
 			getGV().shortmean = Close();
 			getGV().longmean = Close();
 			getGV().fastparam = 12 * TimeScale;
 			getGV().slowparam = 26 * TimeScale;
 			getGV().triggerparam = 9 * TimeScale;	
 			macd = 0;
 			signal = 0;
 		}
		else 
		{
			getGV().shortmean = getGV().shortmean + (2.0/(1+getGV().fastparam))*(Close()-getGV().shortmean);
			getGV().longmean = getGV().longmean + (2.0/(1+getGV().slowparam))*(Close()-getGV().longmean);
			macd = getGV().shortmean - getGV().longmean;
			signal = getSignal(1) + (2.0/(1+getGV().triggerparam))*(macd-getSignal(1));
		}
 		
 		diff = macd - signal;
			
 	}
	
	public MACD get(int index)
	{
		return (MACD)super.get(index);
	}

	public AIndicator clone(IndicatorCandle c)
	{
		MACD foo = new MACD(c,id);
		return foo;
	}
	
	public void reset(IndicatorCandle candle)
	{
		super.reset(candle);
	}
	
 	public double getDiff() 
	{
		return diff;
	}
	
	
	public double getMacd() 
	{
		return macd;
	}
	
	public double getMacd(int ndx)
	{
		return get(ndx).getMacd();
	}

	
	public double getSignal() 
	{
		return signal;
	}
	
	public double getSignal(int ndx)
	{
		return get(ndx).getSignal();
	}

	
	
/*	public void setAnker(double anker) 
	{
		this.anker = anker;
	}
	*/
	
	public GlobalVars getGV()
	{
		return (GlobalVars) getGlobalVar(myGLOBVAR);
	}
}
