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

public class MacdSAR extends ISAR 
{
	/*	PARAMETER CONSTS	*/
	
	private static final int PRATRE = 0; // Percental or ATR or Relative
	private static final int DISINT = 1; // Distance or  integral  
	private static final int DELPRO = 2; // for percental  multiple
	private static final int DELATR = 3; // for ATR multiple
	private static final int DELREL = 4; // for relative multiple
	
	/*	DEPENDENCY CONSTS	*/
	
	private static final int ATRID = 0;
	private static final int MACDID = 1;
	
	/*	GLOBALVARS CONSTS	*/
	
	private static final int myGlobVars = 0;
	
	// die Klasse Globalvars wird hier wie in c 'struct' benutzt, aequivalent zu numeric/bool Variablen in Express
	private class GlobalVars
	{		
		public double integral;
		public double expmaAbsMACDSignal;
	}

	// series
	private int direction;
	//private double test;
	
	
	/* clone Constructor */
	
	public MacdSAR(IndicatorCandle candle, int id) 
	{
		super(candle,id);
	}
	
	/* init Constructor */
	public MacdSAR() {
		super();
		
		Dependency[] dependencies = new Dependency[2];	
		
		dependencies[ATRID] = new Dependency(ATR.class.getName());
		dependencies[MACDID] = new Dependency(MACD.class.getName());
		
		setDependencies(dependencies);
		
		
		IParameter[] parameters = new IParameter[5];
				
		parameters[PRATRE] = new IntParameter("PercAtrRel",2,0,1,2);
				// = 0 for percental distance of delta 
				// = 1 for ATR multiple of delta
				// = 2 for relative to abs(Macd-Signal) multiple of delta 	
		parameters[DISINT] = new BoolParameter("DistOrInteg",true);
				// = 0 for Distance or  =1 for integral  
		parameters[DELPRO] = new DoubleParameter("DeltaPercMult",1,0.01,0.001,0.02); // for percental  multiple
		parameters[DELATR] = new DoubleParameter("DeltaATRMult",1,0.1,0.01,0.3);  // for ATR multiple		
		parameters[DELREL] = new DoubleParameter("DeltaRelMult",1,0.1,0.01,0.3);  // for relative multiple		
		
		
		setParameters(parameters);
		
		try {
			Plotable[] plotables = {
					new Plotable(this.getClass().getMethod("getDirection", null),Color.BLACK),
				//	new Plotable(this.getClass().getMethod("getTest", null),Color.BLACK),
					};
			
			setPlotables(plotables);
		}
		catch (NoSuchMethodException nsme)
		{}
		
		
		
		
		Object[] globalVars = new Object[1];
		
		globalVars[myGlobVars] = new GlobalVars();
		
		setGlobalVars(globalVars);
		
		setResetable();
	}

	public void calculate() 
	{
		
		ATR atr = (ATR)getDependentIndicator(ATRID);
		MACD MyMacd = (MACD)getDependentIndicator(MACDID);

		///////////////////////////////
		// Calculation MacdSAR
		///////////////////////////////
		
		float scaleIntegTodist = 0.4f;
		double cap_delta;
		double ddelta;

		
		double PercAtrRel = (Integer)getParameterVal(PRATRE);
		boolean DistOrInteg = (Boolean)getParameterVal(DISINT);
		double DeltaPercMult = (Double)getParameterVal(DELPRO);
		double DeltaATRMult = (Double)getParameterVal(DELATR);
		double DeltaRelMult = (Double)getParameterVal(DELREL);
		
		
		
		/////////////////////////////////////////////////////////////////////
		////// _integral  Calculation
		////////////////////////////////////////////////////////////////////
		if(!candle.isFirst()) 
		{   
			MyMacd.getMacd(1);
			MyMacd.getSignal(1);
			
			if( ((MyMacd.getMacd()- MyMacd.getSignal()) * (MyMacd.getMacd(1) - MyMacd.getSignal(1))) > 0 ) 
				getGV().integral = getGV().integral + (MyMacd.getMacd()- MyMacd.getSignal());
			else getGV().integral =  MyMacd.getMacd()- MyMacd.getSignal();
		}
		else
			getGV().integral = 0;
		
		/////////////////////////////////////////////////////////////////////
		////// exp ma for abs(Macd-Signal)  Calculation
		////////////////////////////////////////////////////////////////////

	    if(!candle.isFirst())
	    	getGV().expmaAbsMACDSignal = getGV().expmaAbsMACDSignal + (2.0/(1+100))*( Math.abs(MyMacd.getMacd()- MyMacd.getSignal())   - getGV().expmaAbsMACDSignal);	
		else 
			getGV().expmaAbsMACDSignal = 0;

	   
		////////////////////////////////////////////////////////////////////
		///////////  Calculate cap_delta and ddelta
		////////////////////////////////////////////////////////////////////
		if(PercAtrRel == 1) 
			cap_delta = DeltaATRMult * atr.getAtr();  // for ATR distance
		else if(PercAtrRel == 0) 
			cap_delta = DeltaPercMult/100 * Close();  // for percental distance 
		else
			cap_delta = 10*DeltaRelMult *  getGV().expmaAbsMACDSignal;  // for percental distance of  exponential MA of abs(Macd-Signal)

		

		if(DistOrInteg) 
			ddelta = scaleIntegTodist*getGV().integral; // use _integral distance
		else 
			ddelta = MyMacd.getMacd()- MyMacd.getSignal();   // use distance

		///////////////////////////////////////////////////////////////////
		// Calculate Direction
		////////////////////////////////////////////////////////////////////

		 // so far no Direction
		if(candle.isFirst())
			direction = 0; 
		else
			direction = getDirection(1);


		 // update Direction
		if( candle.isFirst() || getDirection(1) == 0 )
		{
			  // the first  nonzero direction is up
			if(ddelta > cap_delta) direction = 1;  
			// the first nonzero  direction is down	
			if(ddelta < (-cap_delta)) direction = -1; 
		}
		else
		{
			if ((getDirection(1) ==1) && ddelta < (-cap_delta)) 
				direction = -1;

			if ((getDirection(1) == -1) &&  ddelta > cap_delta)
				direction = 1;
		}

		
		
	}
	
	public MacdSAR get(int index)
	{
		return (MacdSAR)super.get(index);
	}

	public AIndicator clone(IndicatorCandle c)
	{
		MacdSAR foo = new MacdSAR(c,id);
		return foo;
	}
	
	public void reset(IndicatorCandle candle)
	{
		super.reset(candle);
	}
	
	public int getDirection() 
	{
		return direction;
	}
	
	public int getDirection(int ndx)
	{
		return get(ndx).getDirection();
	}
	
	/*
	public double getTest() 
	{
		return test;
	}
	*/
	
	
	public GlobalVars getGV()
	{
		return (GlobalVars) getGlobalVar(myGlobVars);
	}
}
