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

package de.smpfe.trader.data.indicator;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

import de.smpfe.trader.chartinstrument.Message;
import de.smpfe.trader.chartinstrument.MessageType;
import de.smpfe.trader.data.Dependency;
import de.smpfe.trader.data.IChartInstrument;
import de.smpfe.trader.data.Plotable;
import de.smpfe.trader.data.parameters.IParameter;

public abstract class AIndicator implements IChartInstrument
{
	//
	private static Integer id_counter = 0;
	
	protected int id;
	
	//candle
	protected IndicatorCandle candle;
	
	private static ConcurrentHashMap<Integer,Dependency[]> dependencies = new ConcurrentHashMap<Integer,Dependency[]>();
	private static ConcurrentHashMap<Integer,IParameter[]> parameters = new ConcurrentHashMap<Integer,IParameter[]>();
	private static ConcurrentHashMap<Integer,Plotable[]> plotables = new ConcurrentHashMap<Integer,Plotable[]>();
	
	private static ConcurrentHashMap<Integer,AIndicator[]> indicators = new ConcurrentHashMap<Integer,AIndicator[]> ();
	private static ConcurrentHashMap<Integer,Object[]> globalVars = new ConcurrentHashMap<Integer,Object[]> ();
	
	private static ConcurrentHashMap<Integer,IndicatorObservable> observable = new ConcurrentHashMap<Integer,IndicatorObservable> ();
	private static ConcurrentHashMap<Integer,Boolean> resetable = new ConcurrentHashMap<Integer,Boolean> ();
	// init constructor
	public AIndicator() 
	{
		synchronized(id_counter)
		{
			id_counter++;
			id = id_counter;
		}
		
		//reset
		dependencies.remove(id);
		parameters.remove(id);
		plotables.remove(id);
		indicators.remove(id);
		globalVars.remove(id);
		resetable.put(id, false);
		
		observable.put(id, new IndicatorObservable());
	}
	
	// clone constructor
	public AIndicator(IndicatorCandle candle, int id) 
	{
		this.candle = candle;
		this.id = id;
	}
	
	// reseter
	
	public void reset(IndicatorCandle candle)
	{
		this.candle = candle;
	}
	
	/*			Parameter			*/
	
	public IParameter getParameter(int ndx)
	{
		return parameters.get(id)[ndx];
	}
	
	public Object getParameterVal(int ndx)
	{
		return parameters.get(id)[ndx].getVal();
	}
	
	
	/*			DependencyIndicator			*/
	
	public void setDependentIndicators(AIndicator[] indicators)
	{
		AIndicator.indicators.put(id, indicators);
	}
	
	public AIndicator getDependentIndicator(int ndx)
	{
		return indicators.get(id)[ndx].get(candle);
	}

	/*			GlobalVars			*/
	
	protected void setGlobalVars(Object[] o)
	{
		globalVars.put(id, o);
	}
	
	protected Object getGlobalVar(Integer ndx)
	{
		return globalVars.get(id)[ndx];
	}
	
	/*			IChartInstrument			*/
	
	public Dependency[] getDependencies()
	{
		return dependencies.get(id);
	}
	public IParameter[] getParameters()
	{
		return parameters.get(id);
	}
	public Plotable[] getPlotables()
	{
		return plotables.get(id);
	}
	
	public void setDependencies(Dependency[] dependencies)
	{
		AIndicator.dependencies.put(id, dependencies);
	}
	public void setParameters(IParameter[] parameters)
	{
		AIndicator.parameters.put(id, parameters);
	}
	public void setPlotables(Plotable[] plotables)
	{
		AIndicator.plotables.put(id, plotables);
	}
	
	/*			other Getters			*/
	
	public boolean isResetable()
	{
		return resetable.get(id);
	}
	
	protected void setResetable()
	{
		resetable.put(id, true);// = true;
	}
	
	public int getId()
	{
		return id;
	}
	
	public AIndicator get(int index)
	{
		return candle.get(index).getIndicator(id);
	}
	
	public AIndicator getNext()
	{
		return candle.getNext().getIndicator(id);
	}
	
	public AIndicator getLast()
	{
		return candle.getLast().getIndicator(id);
	}
	
	public AIndicator get(IndicatorCandle c)
	{
		return c.getIndicator(id);
	}
	
	public IndicatorCandle getCandle() 
	{
		return candle;
	}
	
	public int Open()
	{
		return Open(0);
	}
	
	public int Open(int ndx)
	{
		return candle.get(ndx).getOpen();
	}
	
	public int Close()
	{
		return Close(0);
	}
	
	public int Close(int ndx)
	{
		return candle.get(ndx).getClose();
	}
	
	public int High()
	{
		return High(0);
	}
	
	public int High(int ndx)
	{
		return candle.get(ndx).getHigh();
	}
	
	public int Low()
	{
		return Low(0);
	}
	
	public int Low(int ndx)
	{
		return candle.get(ndx).getLow();
	}
	
	/* Observable */
	
	private class IndicatorObservable extends Observable
	{
		public void setChangedPublic()
		{
			setChanged();
		}
	}
	
	protected void setChanged()
	{
		observable.get(id).setChangedPublic();
	}
	public void notifyObservers()
	{
		observable.get(id).notifyObservers(new Message(this,MessageType.DEPENDENCY));
	}
	public synchronized void addObserver(Observer o)
	{
		observable.get(id).addObserver(o);
	}
	public synchronized void deleteObserver(java.util.Observer o)
	{
		observable.get(id).deleteObserver(o);
	}
	public synchronized void deleteObservers()
	{
		observable.get(id).deleteObservers();
	}
	
	public abstract void calculate();
	public abstract AIndicator clone(IndicatorCandle c);

	
}
