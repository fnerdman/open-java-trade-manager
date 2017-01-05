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
package de.smpfe.trader.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import de.smpfe.trader.backtest.OrderEventType;
import de.smpfe.trader.chartinstrument.Message;
import de.smpfe.trader.chartinstrument.enums.Market;
import de.smpfe.trader.data.Plotable;
import de.smpfe.trader.data.chart.Candle;
import de.smpfe.trader.data.chart.Chart;
import de.smpfe.trader.data.indicator.AIndicator;
import de.smpfe.trader.data.indicator.IndicatorCandle;
import de.smpfe.trader.data.indicator.RollingFrameChart;
import de.smpfe.trader.data.order.Order;
import de.smpfe.trader.enums.TimeFrame;
import de.smpfe.trader.factories.IndicatorFactory;

public class ChartDataCollector implements Observer
{
	private IndicatorFactory ifact;
	private Chart chart;
	public IndicatorFactory getIfact() {
		return ifact;
	}

	public ArrayList<Candle>  getCandles() {
		return candles;
	}

	private TimeFrame timeFrame;

	private HashMap<String, ArrayList<Double>> plots;
	private HashMap<String,Boolean> onChart;
	private ArrayList<Candle> candles;
	
	private ArrayList<Integer> entryLong;
	private ArrayList<Integer> entryShort;

	private ArrayList<Integer> confirmEntry;
	private ArrayList<Integer> exit;
	private HashMap<Integer,Double> position;
	private HashMap<Integer,Integer> entryPrices;
	private HashMap<Integer,Integer> exitPrices;
	private OrderEventType lastEvent = OrderEventType.EXIT;
	private int index;

	
	public void addValue(Plotable plotable, Number value)
	{
		// TODO plotable is no unique identifier
		ArrayList<Double> plot = plots.get(plotable.getMethodToPlot().getName());
		
		if(plot == null)
		{
			plot = new ArrayList<Double>();
			plots.put(plotable.getMethodToPlot().getName(), plot);
			onChart.put(plotable.getMethodToPlot().getName(), plotable.isOnChart());
		}
		
		plot.add(value.doubleValue());
	}
	
	public HashMap<String, ArrayList<Double>> getPlots() 
	{
		return plots;
	}
	
	public ChartDataCollector()
	{
		this(null,null);
	}
	
	public ChartDataCollector(IndicatorFactory ifact2, Chart chart)
	{
		this.ifact = ifact2;
		this.chart = chart;
		if(chart!=null) this.timeFrame = chart.getTimeframe();
		
		candles = new ArrayList<Candle>();
		plots = new HashMap<String, ArrayList<Double>>();
		onChart = new HashMap<String,Boolean>();
		
		entryLong = new ArrayList<Integer> ();
		entryShort = new ArrayList<Integer> ();

		confirmEntry = new ArrayList<Integer> ();
		exit = new ArrayList<Integer> ();
		
		position = new HashMap<Integer,Double>();
		entryPrices = new HashMap<Integer,Integer>();
		exitPrices = new HashMap<Integer,Integer>();
	}
	
	public void calculate()
	{
		AIndicator[] indis = null;
		
		try
		{	
			// feuer eine ladung indicatoren
			indis = ifact.finalizeIndis();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
		// instantiere den chart
		RollingFrameChart rcchart = new RollingFrameChart(timeFrame,indis);
		
		// add main als chartlistner
		rcchart.addObserver(this);
		// rcchart.addObserver(new Starter());
		
		// berechne die indikatoren auf den basic candles
		chart.addObserver(rcchart);
		chart.runThroughChart();
	}
	
	public void update(Observable arg0, Object arg1) 
	{
		if(arg1 instanceof Message)
		{
			candleComplete((IndicatorCandle)((Message)arg1).getPayload());
		}
		else if(arg1 instanceof OrderEventType)
		{
			switch((OrderEventType)arg1)
			{
			case CONFIRMENTRY:
				confirmEntry.add(index);
				position.put(index, position.get(Collections.max(position.keySet()))*2);
				entryPrices.put(index, ((OrderEventType)arg1).getPrice());
				break;
			case ENTRYLONG:
				if(lastEvent!=OrderEventType.ENTRYLONG)
				{
					entryLong.add(index);
					position.put(index, 0.5);
				}
				break;
			case ENTRYSHORT:
				if(lastEvent!=OrderEventType.ENTRYSHORT)
				{
					entryShort.add(index);
					position.put(index, -0.5);
				}
				break;
			case EXIT:
				exit.add(index);
				position.put(index, 0.0);
				exitPrices.put(index, ((OrderEventType)arg1).getPrice());
				break;
			case FLAT:
				position.put(index, 0.0);
				break;
			default:
				break;
			
			}
			lastEvent = (OrderEventType)arg1;
		}
	}
	public void candleComplete(IndicatorCandle ic) 
	{		
		index = ic.getIndex();
		candles.add(ic.cloneSimpleCandle());
		for(AIndicator i: ic.getIndicators().values())
		{
			//System.out.print("Indicator: " + i.getClass().getSimpleName() + ": ");
			try 
			{
				Plotable[] plots = i.getPlotables();

				if(plots!=null)
				{
					for(Plotable p: plots)
					{
						addValue(p, (Number)p.getMethodToPlot().invoke(i,null));
						//System.out.print(p.getMethodToPlot() + ": " + i.getClass().getMethod(p.getMethodToPlot()).invoke(i,null) + " " );
						
					}
				}
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			//System.out.println();
		}
	}

	public HashMap<String, Boolean> getOnChart() {
		return onChart;
	}
	
	public ArrayList<Integer> getEntryLong() {
		return entryLong;
	}

	public ArrayList<Integer> getEntryShort() {
		return entryShort;
	}

	public ArrayList<Integer> getConfirmEntry() {
		return confirmEntry;
	}

	public ArrayList<Integer> getExit() {
		return exit;
	}
	
	public HashMap<Integer,Double> getPosition()
	{
		return position;
	}

	public HashMap<Integer, Integer> getEntryPrices() {
		return entryPrices;
	}
	public HashMap<Integer, Integer> getExitPrices() {
		return exitPrices;
	}
}
