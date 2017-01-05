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
package de.smpfe.trader.backtest;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Observable;

import javax.swing.tree.DefaultMutableTreeNode;

import de.smpfe.trader.chartinstrument.DefaultTradeManagement;
import de.smpfe.trader.chartinstrument.enums.Market;
import de.smpfe.trader.data.Underlying;
import de.smpfe.trader.data.chart.Candle;
import de.smpfe.trader.data.chart.Chart;
import de.smpfe.trader.data.chart.PriceElement;
import de.smpfe.trader.data.order.Order;
import de.smpfe.trader.enums.TimeFrame;
import de.smpfe.trader.factories.ATradeInterface;
import de.smpfe.trader.factories.IParameterSetter;
import de.smpfe.trader.factories.TradingSystemFactory;
import de.smpfe.trader.factories.TradingSystemInfo;
import de.smpfe.trader.gui.ChartDataCollector;
import de.smpfe.trader.gui.ChartJDialog;
import de.smpfe.trader.tradingsystem.filter.BeispielFilter;
import de.smpfe.trader.tradingsystem.sentimentstrategy.BeispielStrategy;
import de.smpfe.trader.tradingsystem.stoptarget.BeispielStop;
import de.smpfe.trader.tradingsystem.stoptarget.BeispielTarget;
import de.smpfe.trader.tradingsystem.stoptarget.FixedStop;
import de.smpfe.trader.tradingsystem.stoptarget.FixedTarget;

public class Backtest extends Observable
{
	// chart data
	private ArrayList<Candle> candles;
	private int[] ticks;
	private HashMap<Long,Integer> timeStampIndex;
	private static final boolean startGui = true;
	
	
	private BacktestTradeInterface ti;
	
	// trading system data
	// private TradingSystemInfo[] tsinfo;
	
	public void backtest(TradingSystemInfo[] tsinfo, IParameterSetter setter)
	{
		
		Underlying u = null;
		try {
			u=initBacktest(tsinfo, "EUR/USD", setter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ChartDataCollector cc = null;
		if(startGui)
		{
			cc = new ChartDataCollector();
			u.getLowestCandleChart().addObserver(cc);
			getTradeInterface().getGUIObservable().addObserver(cc);
		}
		
		startCloseSamePeriodBacktest();

		if(startGui)
		{
			// der graphische chart wird dargestellt:
			ChartJDialog cjd = new ChartJDialog(null,"testChart",false,cc);
			cjd.setVisible(true);
		}
		
	}
	
	public Backtest()
	{
		ti = new BacktestTradeInterface();
	}
	
	public Underlying initBacktest(TradingSystemInfo[] tsinfo, String undId, IParameterSetter setter) throws Exception
	{
		Underlying u = new Underlying(undId);
		addObserver(u);
		
		TradingSystemFactory tsf = new TradingSystemFactory();
		tsf.loadTradingSystems(ti, tsinfo);
		setter.setParameter(tsf.getTradingSystemNodes());
		tsf.finalizeTradingSystem(u);
		
		return u;
	}
	
	private void assertPrice(PriceElement pe)
	{
		ti.assertPrice(pe);
	}
	
	public void startCloseSamePeriodBacktest()
	{
		PriceElement pe;
		PriceElement lastClose = null;
		for(Candle c: candles)
		{
			pe = new PriceElement(c.getOpen(),c.getVolume(),1,c.getStartTimeStamp());
			setChanged();
			notifyObservers(pe);
			
			if(lastClose!=null)
				assertPrice(lastClose);
			
			pe = new PriceElement(c.getHigh(),c.getVolume(),1,c.getStartTimeStamp());
			setChanged();
			notifyObservers(pe);
			pe = new PriceElement(c.getLow(),c.getVolume(),1,c.getStartTimeStamp());
			setChanged();
			notifyObservers(pe);
			pe = new PriceElement(c.getClose(),c.getVolume(),1,c.getStartTimeStamp());
			setChanged();
			notifyObservers(pe);
			
			lastClose = pe;
		}
	}
	
	public BacktestTradeInterface getTradeInterface()
	{
		return ti;
	}

	public ArrayList<Candle> getCandles() {
		return candles;
	}

	public void setCandles(ArrayList<Candle> candles) {
		this.candles = candles;
	}

	public int[] getTicks() {
		return ticks;
	}

	public void setTicks(int[] ticks) {
		this.ticks = ticks;
	}

	public HashMap<Long,Integer> getTimeStampIndex() {
		return timeStampIndex;
	}

	public void setTimeStampIndex(HashMap<Long,Integer> timeStampIndex) {
		this.timeStampIndex = timeStampIndex;
	}

	private class BacktestTradeInterface extends ATradeInterface
	{
		private Order order = new Order();
		private Market position = Market.FLAT; 
		private GUIObservable guiObs = new GUIObservable();
		private int price;
		
		
		public void updateOrder(Order order) {
			System.out.println("New Order: " + order);
			this.order = order;
		}
		
		private class GUIObservable extends Observable
		{
			public void setOrderChanged(OrderEventType oet)
			{
				setChanged();
				notifyObservers(oet);
			}
		}
		
		public Observable getGUIObservable()
		{
			return guiObs;
		}
		
		private void confirmOrder(PriceElement pe, Market pos)
		{
			HashMap<String,Object> payload = new HashMap<String,Object>();
			payload.put("getPosition", pos);
			payload.put("getPrice", pe.getPrice());
			payload.put("getTimeStamp", pe.getTime());
			setChanged();
			notifyObservers(payload);
			position = pos;
			price = pe.getPrice();
			System.out.println("Order Confirmation: Position: " + position + " Price: " + price + " Date: " + new Date(pe.getTime()));
		}
		
		private void leaveMarket(PriceElement pe)
		{
			confirmOrder(pe, Market.FLAT);
			OrderEventType oet = OrderEventType.EXIT;
			oet.setPrice(pe.getPrice());
			guiObs.setOrderChanged(oet);
		}
		
		private void entryLong(PriceElement pe)
		{
			confirmOrder(pe, Market.LONG);
			OrderEventType oet = OrderEventType.CONFIRMENTRY;
			oet.setPrice(pe.getPrice());
			guiObs.setOrderChanged(oet);
			//order.setPosition(Market.FLAT);
		}
		
		private void entryShort(PriceElement pe)
		{
			confirmOrder(pe, Market.SHORT);
			OrderEventType oet = OrderEventType.CONFIRMENTRY;
			oet.setPrice(pe.getPrice());
			guiObs.setOrderChanged(oet);
		}
		
		public void assertPrice(PriceElement pe)
		{

			if(order.getPosition()!=position)
			{
				switch(position)
				{
				case FLAT:
					switch(order.getPosition())
					{
					case LONG:
						OrderEventType oet = OrderEventType.ENTRYLONG;
						oet.setPrice(order.getTrigger());
						guiObs.setOrderChanged(oet);
						switch(order.getOrderType())
						{
						case MARKET:
							entryLong(pe);
							break;
						case STOP:
							if(pe.getPrice()>=order.getTrigger())
								entryLong(pe);
							break;
						case LIMIT:
							if(pe.getPrice()<=order.getTrigger())
								entryLong(pe);
							break;
						}
						break;
					case SHORT:
						oet = OrderEventType.ENTRYSHORT;
						oet.setPrice(order.getTrigger());
						guiObs.setOrderChanged(oet);
						switch(order.getOrderType())
						{
						case MARKET:
							entryShort(pe);
							break;
						case STOP:
							if(pe.getPrice()<=order.getTrigger())
								entryShort(pe);
							break;
						case LIMIT:
							if(pe.getPrice()>=order.getTrigger())
								entryShort(pe);
							break;
						}
						break;
					default:
						break;
					}
					break;
				case LONG:
					switch(order.getPosition())
					{
					case FLAT:
						leaveMarket(pe);
						break;
					case SHORT:
						leaveMarket(pe);
						OrderEventType oet = OrderEventType.ENTRYSHORT;
						oet.setPrice(order.getTrigger());
						guiObs.setOrderChanged(oet);
						switch(order.getOrderType())
						{
						case MARKET:
							entryShort(pe);
							break;
						case STOP:
							if(pe.getPrice()<=order.getTrigger())
								entryShort(pe);
							break;
						case LIMIT:
							if(pe.getPrice()>=order.getTrigger())
								entryShort(pe);
							break;
						}
						break;
					default:
						break;
					}
					break;
				case SHORT:
					switch(order.getPosition())
					{
					case FLAT:
						leaveMarket(pe);
						break;
					case LONG:
						leaveMarket(pe);
						OrderEventType oet = OrderEventType.ENTRYLONG;
						oet.setPrice(order.getTrigger());
						guiObs.setOrderChanged(oet);
						switch(order.getOrderType())
						{
						case MARKET:
							entryLong(pe);
							break;
						case STOP:
							if(pe.getPrice()>=order.getTrigger())
								entryLong(pe);
							break;
						case LIMIT:
							if(pe.getPrice()<=order.getTrigger())
								entryLong(pe);
							break;
						}
						break;
					default:
						break;
					}
					break;
				}
			}
			else
			{
				switch(position)
				{
				case LONG:
					if(order.getStopTrigger()>=0)
					{
						int stop = order.getStopTrigger();
						if(order.isStopRefPointEntryPrice())
							stop = price-stop;
						
						if(stop>=pe.getPrice())
						{
							System.out.println("Stoptrigger: "+ stop+" position=LONG price="+price);
							leaveMarket(pe);
						}
					}
					if(order.getTargetTrigger()>=0)
					{
						int target = order.getTargetTrigger();
						if(order.isTargetRefPointEntryPrice())
							target += price;
						
						if(target<=pe.getPrice())
							leaveMarket(pe);
					}
					break;
				case SHORT:
					if(order.getStopTrigger()>=0)
					{
						int stop = order.getStopTrigger();
						if(order.isStopRefPointEntryPrice())
							stop += price;
						
						if(stop<=pe.getPrice())
						{
							System.out.println("Stoptrigger: "+ stop+" position=SHORT price="+price+" refpoint="+order.isStopRefPointEntryPrice());
							leaveMarket(pe);
						}
					}
					if(order.getTargetTrigger()>=0)
					{
						int target = order.getTargetTrigger();
						if(order.isTargetRefPointEntryPrice())
							target = price-target;
						
						if(target>=pe.getPrice())
							leaveMarket(pe);
					}
					break;
				default:
					break;
				}
			}
		}
		
	}
	
}
