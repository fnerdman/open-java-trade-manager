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
import java.util.ArrayList;

import de.smpfe.trader.data.Dependency;
import de.smpfe.trader.data.Plotable;
import de.smpfe.trader.data.indicator.AIndicator;
import de.smpfe.trader.data.indicator.IndicatorCandle;
import de.smpfe.trader.data.parameters.BoolParameter;
import de.smpfe.trader.data.parameters.DoubleParameter;
import de.smpfe.trader.data.parameters.IParameter;
import de.smpfe.trader.data.parameters.IntParameter;

public class MinMax extends AIndicator 
{
	/*	PARAMETER CONSTS	*/
	private static final int CHOICE = 0; // choose extended
	//TODO private static final int CHOSAR = 1; // choose sar
	
	/*	DEPENDENCY CONSTS	*/
	
	private static final int SARID = 0;
	
	/*	GLOBALVARS CONSTS	*/
	
	private static final int GLOBVARCONST = 0;
	
	private class Sixpack
	{
		int[] zahlen = new int[6];
	}
	
	// die klasse Globalvars wird hier wie in c 'struct' benutzt, aequivalent zu numeric/bool variablen in express
	private class GlobalVars
	{
		private ArrayList<Integer> _maxextrbar = new ArrayList<Integer>();
		private ArrayList<Integer> _minextrbar = new ArrayList<Integer>();
		
		private int _tempmaxbar =0, _tempminbar =0; //absolute bar numbers
	}

	// series
	private int unique;
	private int test;
	private int tempMinMax;
	
	public Sixpack extrema = new Sixpack();	
			// contains all extrema with even numbers being maxima and uneven minima
			// if first extrema is minima, entry 0 is negative

	/* clone Constructor */
	
	public MinMax(IndicatorCandle candle, int id) 
	{
		super(candle,id);
	}
	
	/* init Constructor */
	public MinMax() {
		super();
		
		Dependency[] dependencies = new Dependency[1];
		
		//dependencies[SARID] = new Dependency(MacdSAR.class.getName());
		//dependencies[SARID] = new Dependency(RenkoATR.class.getName());
		//dependencies[SARID] = new Dependency(ISAR.class.getName(), new String[] {  RenkoSAR.class.getName() , MacdSAR.class.getName() });
		dependencies[SARID] = new Dependency(ISAR.class.getName(), new String[] {  MacdSAR.class.getName(),  RenkoSAR.class.getName() });

		//TODO: MacdSAR und RenkoATR vertauscht f�hrt zu Chart mit nicht gut aufgel�sten Preisen
		
		setDependencies(dependencies);
		
		
		IParameter[] parameters = new IParameter[1];
	
		parameters[CHOICE] = new BoolParameter("ChooseExtended",true);
		//TODO parameters[CHOSAR] = new BoolParameter("ChooseSar",true);
		
		setParameters(parameters);
		
		try {
			Plotable[] plotables = {
					new Plotable(this.getClass().getMethod("getDirection", null),Color.BLACK),
					new Plotable(this.getClass().getMethod("getUniqueness", null),Color.BLACK),
					new Plotable(this.getClass().getMethod("getStatus", null),Color.BLACK),
					new Plotable(this.getClass().getMethod("getTempMinMax", null),Color.RED,true),
					new Plotable(this.getClass().getMethod("getTest", null),Color.RED)
					//TODO test Series nicht drucken, wenn das nicht gew�nscht ist, z.B- test=null
					};
			
			setPlotables(plotables);
		}
		catch (NoSuchMethodException nsme)
		{}
		
		Object[] globalVars = new Object[1];
		
		globalVars[GLOBVARCONST] = new GlobalVars();
		
		setGlobalVars(globalVars);
		
		setResetable();
	}

	public void calculate() 
	{
		///////////////////////////////
		// Calculation MinMax
		///////////////////////////////
		
		//ISAR MySAR = (ISAR)getDependentIndicator(SARID);
		
		boolean ChooseExtended = (Boolean)getParameterVal(CHOICE);
		
		//local vars 
		//int _tempextr,_tempextrbar;
		
		unique = calcUnique();
		
		////////////////////////////////////////
		// intitialization
		////////////////////////////////////////

		
		// wait for first direction
		if (candle.isFirst() || getDirection(1) == 0) 
		{
			
			if(HighAbs(getGV()._tempmaxbar) <= High(0))
				getGV()._tempmaxbar = candle.getIndex();
			if(LowAbs(getGV()._tempminbar) >= Low(0))      
				getGV()._tempminbar = candle.getIndex();

			test=5;
			//test= 2+((getDirection(0)  != 0)  ?1:0 )  + getDirection(0);
			
			// initialize
			if(getDirection(0)  != 0)
			{	
				getGV()._minextrbar.add(getGV()._tempminbar);
				getGV()._maxextrbar.add(getGV()._tempmaxbar);
				test=10;
				//test=getGV()._maxextrbar.size();
				

				// exception handling when status directly hits direction of our next extrema
				// note: if _tempminbar == _tempmaxbar this issue will not be addressed

				if(getGV()._tempminbar < getGV()._tempmaxbar && getDirection(0) == -1)
				{  
					getGV()._tempminbar = getGV()._tempmaxbar + ( (!ChooseExtended||getMinextrbarBackAbs() ==getGV()._tempmaxbar) ?1:0 );

					
					for(int k=(getGV()._tempmaxbar+1);k<=candle.getIndex();k++)
					{
						if(LowAbs(k)  <= LowAbs(getGV()._tempminbar))
								getGV()._tempminbar = k;
					}
					
					getGV()._minextrbar.add(getGV()._tempminbar);
				}
				else if(getGV()._tempminbar > getGV()._tempmaxbar && getDirection(0) == 1)
				{
					getGV()._tempmaxbar = getGV()._tempminbar + ( (!ChooseExtended||getMaxextrbarBackAbs() ==getGV()._tempminbar) ?1:0 );

					for(int k=(getGV()._tempminbar+1);k<=candle.getIndex();k++)
					{
						if( HighAbs(k)  >= HighAbs(getGV()._tempmaxbar))
							getGV()._tempmaxbar = k;
					}

					getGV()._maxextrbar.add(getGV()._tempmaxbar);
				}
			}

			for(int k=0;k<6;k++)
			{
				extrema.zahlen[k] = -1;
			}

			tempMinMax=Low(0)-100;
			//test=-1;
			//test=getGV()._maxextrbar.size();
			return;
		}
		
		////////////////////////////////////////
		// main minmax process
		////////////////////////////////////////
		
		
		if(getStatus(1) == 1)
		{
			if(HighAbs(getGV()._tempmaxbar) <= High(0))
			{
				getGV()._tempmaxbar = candle.getIndex();
			}

			if(getStatus(0) == -1)
			{
				// set new
				getGV()._tempminbar = getGV()._tempmaxbar + ( (!ChooseExtended||getMinextrbarBackAbs() ==getGV()._tempmaxbar) ?1:0 );

				for(int k=(getGV()._tempmaxbar+1);k<=candle.getIndex();k++)
				{
					if(LowAbs(k) <= LowAbs(getGV()._tempminbar))
						getGV()._tempminbar = k;
				}
				
				getGV()._minextrbar.add(getGV()._tempminbar);
			}
		}
		else
		{
			if(LowAbs(getGV()._tempminbar) >= Low(0))
			{
				getGV()._tempminbar = candle.getIndex();
			}

			if(getStatus(0) == 1)
			{
				// set new
				getGV()._tempmaxbar = getGV()._tempminbar + ( (!ChooseExtended||getMaxextrbarBackAbs() ==getGV()._tempminbar) ?1:0 );

				for(int k=(getGV()._tempminbar+1);k<=candle.getIndex();k++)
				{
					if(HighAbs(k) >= HighAbs(getGV()._tempmaxbar))
						getGV()._tempmaxbar = k;
				}

				getGV()._maxextrbar.add(getGV()._tempmaxbar);
			}
		}

		if(!getGV()._maxextrbar.isEmpty()) getGV()._maxextrbar.set(getGV()._maxextrbar.size()-1, getGV()._tempmaxbar);
		if(!getGV()._minextrbar.isEmpty()) getGV()._minextrbar.set(getGV()._minextrbar.size()-1, getGV()._tempminbar);
		//TODO .size = .end - .begin???

		int maxndx = 0;
		int minndx = 0;

		Boolean max;
		if(!getGV()._maxextrbar.isEmpty())
		{
		 maxndx = getGV()._maxextrbar.size()-1;
		 minndx = getGV()._minextrbar.size()-1;
		}

		if ((maxndx >=2) && (minndx>=2) &&  (!getGV()._maxextrbar.isEmpty()))
		{
			if(getMaxextrbarBackAbs() > getMinextrbarBackAbs()) max = true;
			else if(getMaxextrbarBackAbs() < getMinextrbarBackAbs()) max = false;
			else if(getGV()._maxextrbar.get(getGV()._maxextrbar.size()-2) > getGV()._minextrbar.get(getGV()._minextrbar.size()-2)) max = true;
			else max = false;

			if(max)
			{
				for(int k=0;k<3;k++)
				{
					extrema.zahlen[k*2]   = getGV()._maxextrbar.get(maxndx);
					extrema.zahlen[(k*2)+1]  = getGV()._minextrbar.get(minndx);
					maxndx--;
					minndx--;
				}
				tempMinMax=HighAbs(extrema.zahlen[0]);
				//test=LowAbs(extrema.zahlen[1]);
			}
			else
			{
				for(int k=0;k<3;k++)
				{
					extrema.zahlen[k*2]   = getGV()._minextrbar.get(minndx);
					extrema.zahlen[(k*2)+1]  = getGV()._maxextrbar.get(maxndx);
					maxndx--;
					minndx--;
				}
				tempMinMax=LowAbs(extrema.zahlen[0]);
				//test=HighAbs(extrema.zahlen[1]);
			}
			
			//if(test<10000) test=Low(0)-100;
		}
		else
		{
			for(int k=0;k<6;k++)
			{
				extrema.zahlen[k] = -1;
			}
			tempMinMax=Low(0)+100;
			test=getGV()._maxextrbar.size();
		}	
		
		//test=getGV()._maxextrbar.size();
		test=  getDirection(0);
		
		//test=getGV()._tempmaxbar;
	}
	
	private int calcUnique()
	{
		if ((candle.isFirst()) || getDirection(0) == 0 || getGV()._minextrbar.isEmpty() || getGV()._maxextrbar.isEmpty())
			return 1;

		// 'uniqueness process' active
		if (getUniqueness(1) == -1)
		{
			if(
				(getDirection(1)*getDirection(0) == -1)
			||	(getDirection(1) == 1 && High(getMaxextrbarBack()) <= High(0))
			||	(getDirection(1) == -1 && Low(getMinextrbarBack()) >= Low(0))
			)
				return 1;

			return -1;
		}

		if(getDirection(1) == getDirection(0))
		{
			if(getDirection(0) == 1)
			{
				if(Low(getMinextrbarBack()) >= Low(0))
					return -1;
			}
			else
			{
				if(High(getMaxextrbarBack()) <= High(0))
					return -1;
			}
		}

		return 1;
		
	}
	
	///////////////////////////////////////////////////
	// clone und reset
	///////////////////////////////////////////////////////

	public AIndicator clone(IndicatorCandle c)
	{
		MinMax mima = new MinMax(c,id);
		return mima;
	}
	
	public void reset(IndicatorCandle candle)
	{
		super.reset(candle);
	}
	
	/////////////////////////////////////////////////////
	// getter und setter
	////////////////////////////////////////////////////////
	
	public MinMax get(int index)
	{
		return (MinMax)super.get(index);
	}
	
	public int getDirection() 
	{
		return  ((ISAR)getDependentIndicator(SARID)).getDirection();
	}
	
	public int getDirection(int ndx)
	{
		return get(ndx).getDirection();
	}

	public int getStatus() 
	{
		return unique * getDirection();
	}
	
	public int getStatus(int ndx)
	{
		return get(ndx).getStatus();
	}

	
	public int getTest() 
	{
		return test;
	}
	
	public int getTempMinMax() 
	{
		return tempMinMax;
	}
	
	public int getUniqueness() 
	{
		return unique;
	}
	
	public int getUniqueness(int ndx)
	{
		return get(ndx).getUniqueness();
	}
	
	public void setUnique(int unique) 
	{
		this.unique = unique;
	}
	
	
	public GlobalVars getGV()
	{
		return (GlobalVars) getGlobalVar(GLOBVARCONST);
	}
	
	public int getMaxextrbarAbs(int ndx) 
	{
		return getGV()._maxextrbar.get(ndx);
	}
	
	public int getMinextrbarAbs(int ndx) 
	{
		return getGV()._minextrbar.get(ndx);
	}
	
	public int getNmaxextrbar() 
	{
		return getGV()._maxextrbar.size();
	}
	
	public int getNminextrbar() 
	{
		return getGV()._minextrbar.size();
	}
	
	public int getMaxextrbarBack() 
	{
		return candle.getIndex() - getGV()._maxextrbar.get(getNmaxextrbar()-1);
	}
	
	public int getMinextrbarBack() 
	{
		return candle.getIndex() - getGV()._minextrbar.get(getNminextrbar()-1);
	}
	
	public int getMaxextrbarBackAbs() 
	{
		return  getGV()._maxextrbar.get(getNmaxextrbar()-1);
	}
	
	public int getMinextrbarBackAbs() 
	{
		return  getGV()._minextrbar.get(getNminextrbar()-1);
	}
	
	
	public Sixpack getExtrema() 
	{
		return extrema;
	}
	
	public int getExtrema(int bar, int nth)
	{
		return get(bar).getExtrema().zahlen[nth];
	}
	
	
	public int getTempTime(int bar) 
	{
		return Math.max(getExtrema(bar,0),0);
	}
	
	public int getLastTime(int bar) 
	{
		return Math.max(getExtrema(bar,1),0);
	}
	
	public int getBeforeLastTime(int bar) 
	{
		return Math.max(getExtrema(bar,2),0);
	}
	
	public int getTempExtremum(int bar) 
	{
		if (getStatus(bar)==1)
		return HighAbs(getTempTime(bar));
		else
			return LowAbs(getTempTime(bar));
	}
	
	public int getLastExtremum(int bar) 
	{
		if (getStatus(bar)==-1)
		return HighAbs(getLastTime(bar));
		else
			return LowAbs(getLastTime(bar));
	}
	
	public int getBeforeLastExtremum(int bar) 
	{
		if (getStatus(bar)==1)
		return HighAbs(getBeforeLastTime(bar));
		else
			return LowAbs(getBeforeLastTime(bar));
	}
	
	public int HighAbs(int bar) 
	{
		return High(candle.getIndex() -bar);
	}
	
	public int LowAbs(int bar) 
	{
		return Low(candle.getIndex() -bar);
	}
	
	public int OpenAbs(int bar) 
	{
		return Open(candle.getIndex() -bar);
	}
	
	public int CloseAbs(int bar) 
	{
		return Close(candle.getIndex() -bar);
	}
		
}
