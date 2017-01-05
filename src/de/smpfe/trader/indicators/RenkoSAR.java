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

public class RenkoSAR extends ISAR 
{
	/*	PARAMETER CONSTS	*/
	
	private static final int ATRMTP = 0;
	private static final int BDYRNG = 1;
	private static final int BOXSZE = 2;
	private static final int PRCATR = 3;
	private static final int REVAMT = 4;
	
	/*	DEPENDENCY CONSTS	*/
	
	private static final int ATRID = 0;
	
	/*	GLOBALVARS CONSTS	*/
	
	private static final int ANKERS = 0;
	
	// die klasse Globalvars wird hier wie in c 'struct' benutzt, aewivqlent zu numeric/bool variablen in express
	private class GlobalVars
	{
		public int ankerLow;
		public int ankerHigh;
	}

	// series
	private int direction;
	private double anker;

	/* clone Constructor */
	
	public RenkoSAR(IndicatorCandle candle, int id) 
	{
		super(candle,id);
	}
	
	/* init Constructor */
	public RenkoSAR() {
		super();
		
		Dependency[] dependencies = new Dependency[1];
		
		dependencies[ATRID] = new Dependency(ATR.class.getName());
		
		setDependencies(dependencies);
		
		
		IParameter[] parameters = new IParameter[5];
		
		parameters[ATRMTP] = new DoubleParameter("AtrMultiplier",10,0.1,0.1,1);
		parameters[BDYRNG] = new BoolParameter("BodyOrRange",true);
		parameters[BOXSZE] = new DoubleParameter("pBoxSize",10,0.1,0.1,0.3);
		parameters[PRCATR] = new BoolParameter("PercOrAtr",true);
		parameters[REVAMT] = new IntParameter("ReversalAmount",10,1,1,3);
		
		setParameters(parameters);
		
		try {
			Plotable[] plotables = {
					//new Plotable(this.getClass().getMethod("getDirection", null),Color.BLACK),
					new Plotable(this.getClass().getMethod("getAnker", null),Color.BLUE,true)
					};
			
			setPlotables(plotables);
		}
		catch (NoSuchMethodException nsme)
		{}
		
		
		
		
		Object[] globalVars = new Object[1];
		
		globalVars[ANKERS] = new GlobalVars();
		
		setGlobalVars(globalVars);
		
		setResetable();
	}

	public void calculate() 
	{
		
		ATR atr = (ATR)getDependentIndicator(ATRID);

		///////////////////////////////
		// Calculation Renko
		///////////////////////////////

		////////////////////////////////////////////////////
		// _relLow und _relHigh bestimmen 
		///////////////////////////////////////////////////
		
		int _relLow;
		int _relHigh;
		double _minreversal;
		
		double atrMultiplier = (Double)getParameterVal(ATRMTP);
		boolean bodyOrRange = (Boolean)getParameterVal(BDYRNG);
		double pBoxSize = (Double)getParameterVal(BOXSZE);
		boolean percOrAtr = (Boolean)getParameterVal(PRCATR);
		int reversalAmount = (Integer)getParameterVal(REVAMT);

		if(bodyOrRange)
		{	
			// range
			_relLow = Low();
			_relHigh = High();
		}
		else
		{	
			// body
			_relLow =  Math.min(Open(),Close());
			_relHigh = Math.max(Open(),Close());
		}

		///////////////////////////////
		// determine the boxsize (currentboxsize)
		///////////////////////////////

		if(percOrAtr)
			_minreversal = atr.getAtr() * atrMultiplier * reversalAmount;
		else
			_minreversal = (pBoxSize/100) * Close() * reversalAmount;

		if(candle.getLast() == null)
		{
			// initialize
			 direction = 0;
			 anker = (double)(_relLow+_relHigh)/2;
			 getGV().ankerHigh = _relHigh;
		}
		else
		{
			 direction = this.get(1).getDirection();
			 anker =  this.get(1).getAnker();
			 
			////////////////////////////////////////////////////
			// Direction bestimmen
			////////////////////////////////////////////////////
			 
			 if (direction == 0)
			{
				////////////////////////////////////////////////////
				 // init direction
				 ///////////////////////////////////////////////////

				 int temp3;

				 getGV().ankerLow = Math.min(getGV().ankerLow, _relLow);
				 getGV().ankerHigh = Math.max(getGV().ankerHigh, _relHigh);

				 temp3 = _relHigh-getGV().ankerLow;
				 
				 if  (temp3 > _minreversal)
				 {
					 direction = 1;
					 
					 // neuer Anker fuer Up-Richtung
					 anker = getGV().ankerHigh;
					 // init Anker muss das sein???
					 int j = 1;
					 try
					 {
						 while(get(j)!=null)
						 {
							 get(j).setAnker(getGV().ankerLow);
							 j++;
						 }  
					 }
					 catch(Exception e){}
				 }
				 // (_ankerHigh - _relLow ) > _minreversal
				 temp3 = getGV().ankerHigh-_relLow;
				 if  (temp3 > _minreversal)
				 {
					 direction = -1;

					 // neuer Anker fuer Down-Richtung
					 anker = getGV().ankerLow;
					 // init Anker muss das sein???
					 int j = 1;
					 try
					 {
						 while(get(j)!=null)
						 {
							 get(j).setAnker(getGV().ankerHigh);
							 j++;
						 }    
					 }
					 catch(Exception e){}
				 }
			}
			else
			{
				///////////////////////////////////////////////////
				// anker nachziehen oder reversal direction
				//////////////////////////////////////////////////

				double temp3;

				if (this.get(1).getDirection() == 1)
				{
					 if ((_relHigh - this.get(1).getAnker()) > Double.MIN_NORMAL)
						 anker = _relHigh; 
					 else 
					 {
						temp3 = this.get(1).getAnker()-_relLow;
						if  ((temp3 - _minreversal) > Double.MIN_NORMAL)
						{
						 // reversal
							direction = -1;         
							anker = _relLow;
						}
					 }
				}
				else 
				{
					  if  ((this.get(1).getAnker()-_relLow) > Double.MIN_NORMAL)
						  anker = _relLow;      
					  else 
					  {	  
						  temp3 = _relHigh-this.get(1).getAnker();
						  if  ((temp3 - _minreversal)  > Double.MIN_NORMAL)
						 {
						  // reversal 
							direction = 1;       
							anker = _relHigh; 
						 }
					  }
				}
			}
		}
	}
	
	public RenkoSAR get(int index)
	{
		return (RenkoSAR)super.get(index);
	}

	public AIndicator clone(IndicatorCandle c)
	{
		RenkoSAR ratr = new RenkoSAR(c,id);
		return ratr;
	}
	
	public void reset(IndicatorCandle candle)
	{
		super.reset(candle);
	}
	
	public int getDirection() 
	{
		return direction;
	}
	
	public double getAnker() 
	{
		return anker;
	}
	
	public void setAnker(double anker) 
	{
		this.anker = anker;
	}
	
	public GlobalVars getGV()
	{
		return (GlobalVars) getGlobalVar(ANKERS);
	}
}
