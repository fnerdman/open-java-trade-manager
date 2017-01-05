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
package de.smpfe.trader.chartinstrument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;

import de.smpfe.trader.chartinstrument.enums.FilterType;
import de.smpfe.trader.chartinstrument.enums.Market;
import de.smpfe.trader.chartinstrument.enums.SentiOrderType;
import de.smpfe.trader.chartinstrument.enums.Sentiment;
import de.smpfe.trader.chartinstrument.enums.StopTargetOrderType;
import de.smpfe.trader.data.indicator.IndicatorCandle;
import de.smpfe.trader.data.order.Order;
import de.smpfe.trader.enums.TimeFrame;

public abstract class ATradeManagement extends AChartInstrument
{


	private HashMap<Object,FilterType> filters = new HashMap<Object,FilterType>();
	private FilterType[] filterTypes = new FilterType[2];
	private SentimentInfo[] sentimentInfo = new SentimentInfo[2];
	private OrderInfo[] orderInfo = new OrderInfo[2];
	private HashMap<Object,StopTargetInfo> stopTargets = new HashMap<Object,StopTargetInfo>();
	private Order[] orders = new Order[2];
	private OrderObservable ordObs;
	
	public ATradeManagement()
	{
		setCurOrder(new Order());
		setCurrentFilter(FilterType.FILTEROFF);
		orderInfo[0] = new OrderInfo(Market.FLAT,null,0,0);
		sentimentInfo[0] = new SentimentInfo(Sentiment.NEUTRAL,null,-1);
		ordObs = new OrderObservable();
	}

	public final void update(Observable o, Object arg) 
	{
		if(o instanceof MailBox)
		{
			updateFilterType();
			interpret((ArrayList<Message>)arg);
		}
		else
		{
			HashMap<String,Object> payload = (HashMap<String,Object>)arg;
			Market position = (Market)payload.get("getPosition");
			int price = (Integer)payload.get("getPrice");
			long timeStamp = (Long)payload.get("getTimeStamp");
			
			synchronized(getMailBox().getMutex())
			{
				updateOrder(arg, position, timeStamp, price);
			}
		}
	}
	
	protected void sendNewOrder(Order order) {
		
		switch(getCurrentFilter())
		{
			case FILTERON:
				if(getCurOrder().getPosition() == Market.FLAT)
					return;
				break;
			case BUYFILTER:
				if(getCurOrder().getPosition() == Market.FLAT && order.getPosition() == Market.LONG)
					return;
				break;
			case SELLFILTER:
				if(getCurOrder().getPosition() == Market.FLAT && order.getPosition() == Market.SHORT)
					return;
				break;
			default:
				break;
		}
		
		setCurOrder(order);
		ordObs.notifyNewOrder(order);
	}

	public void updateFilter(Object id, FilterType type)
	{
		filters.put(id, type);
	}
	
	private void updateFilterType()
	{
		FilterType type = FilterType.FILTEROFF;
		
		for(FilterType ftype: filters.values())
		{
			if(ftype == FilterType.BUYFILTER)
			{
				if(type == FilterType.SELLFILTER)
				{
					type = FilterType.FILTERON;
					break;
				}
				type = FilterType.BUYFILTER;
			}
			else if(ftype == FilterType.SELLFILTER)
			{
				if(type == FilterType.BUYFILTER)
				{
					type = FilterType.FILTERON;
					break;
				}
				type = FilterType.SELLFILTER;
			}
			else if(ftype == FilterType.FILTERON)
			{
				type = FilterType.FILTERON;
				break;
			}
		}
		
		setCurrentFilter(type);
	}
	
	private void setCurrentFilter(FilterType type) {
		filterTypes[1] = filterTypes[0];
		filterTypes[0] = type;
	}

	public FilterType getCurrentFilter()
	{
		return filterTypes[0];
	}
	
	public FilterType getLastFilter()
	{
		return filterTypes[1];
	}
	
	public SentimentInfo getCurrentSentimentInfo()
	{
		return sentimentInfo[0];
	}
	
	public SentimentInfo getLastSentimentInfo()
	{
		return sentimentInfo[1];
	}

	public void updateSentiment(Sentiment sentiment, SentiOrderType orderType, int trigger) 
	{
		sentimentInfo[1] = getCurrentSentimentInfo();
		sentimentInfo[0] = new SentimentInfo(sentiment,orderType,trigger);
	}

	public void updateStopTarget(Object invoker, StopTargetOrderType orderType, TimeFrame refTime, Integer trigger, Boolean refPointEntryPrice) 
	{
		stopTargets.put(invoker, new StopTargetInfo(orderType,refTime,trigger,refPointEntryPrice));
	}
	
	public void removeStopTarget(Object invoker)
	{
		stopTargets.remove(invoker);
	}
	
	public HashMap<Object,StopTargetInfo> getStopTargets()
	{
		return stopTargets;
	}
	
	public OrderInfo getCurrentOrderInfo()
	{
		return orderInfo[0];
	}
	
	public OrderInfo getLastOrderInfo()
	{
		return orderInfo[1];
	}
	
	public abstract void updateOnOrderFeedBack(OrderInfo oi);

	public void updateOrder(Object payload, Market position, long timeStamp, int price) 
	{
		orderInfo[1] = getCurrentOrderInfo();
		
		IndicatorCandle candle = getCandle(getLowestTimeFrame());
		while(candle.getStartTimeStamp() > timeStamp)
		{
			candle = candle.getLast();
		}
		orderInfo[0] = new OrderInfo(position,candle,timeStamp,price);
		
		updateOnOrderFeedBack(orderInfo[0]);
		
		Message m = new Message(payload, MessageType.ORDERFEEDBACK);
		setChanged();
		notifyObservers(m);
	}
	
	public final boolean updateOnChartData()
	{
		return true;
	}
	
	public final boolean updateOnSentimentChange()
	{
		return true;
	}
	
	public final boolean updateOnStopTargetChange()
	{
		return true;
	}
	
	public final boolean updateOnFilterChange()
	{
		return true;
	}
	
	public final boolean updateOnOrderFeedback()
	{
		return false;
	}
	
	protected void setCurOrder(Order curOrder) {
		this.orders[1] = orders[0];
		orders[0] = curOrder;
	}

	public Order getCurOrder() {
		return orders[0];
	}
	
	public Order getLastOrder()
	{
		return orders[1];
	}
	
	public Observable getOrderObservable()
	{
		return ordObs;
	}

	public class StopTargetInfo
	{
		private StopTargetOrderType orderType;
		private TimeFrame refTime;
		private int trigger;
		private boolean refPointEntryPrice;
		
		public StopTargetInfo(StopTargetOrderType orderType, int trigger, boolean refPointEntryPrice)
		{
			this(orderType,null,trigger,refPointEntryPrice);
		}
		
		public StopTargetInfo(StopTargetOrderType orderType, TimeFrame refTime, int trigger, boolean refPointEntryPrice)
		{
			this.orderType = orderType;
			this.refTime = refTime;
			this.trigger = trigger;
			this.refPointEntryPrice = refPointEntryPrice;
		}
		
		public StopTargetOrderType getOrderType() {
			return orderType;
		}
		public int getTrigger() {
			return trigger;
		}

		public TimeFrame getReferenceTime() {
			return refTime;
		}

		public boolean isRefPointEntryPrice() {
			return refPointEntryPrice;
		}
	}
	
	public class SentimentInfo 
	{
		private Sentiment sentiment;
		private SentiOrderType orderType;
		private int trigger;
		
		
		public SentimentInfo(Sentiment sentiment, SentiOrderType orderType, int trigger)
		{
			this.trigger = trigger;
			this.orderType = orderType;
			this.sentiment=sentiment;
		}

		public Sentiment getSentiment() {
			return sentiment;
		}
		
		public SentiOrderType getOrderType() {
			return orderType;
		}
		public int getTrigger() {
			return trigger;
		}
	}
	
	public class OrderInfo
	{
		private Market position;
		private IndicatorCandle candle;
		private long timeStamp;
		private int price;
		
		public OrderInfo(Market position, IndicatorCandle candle, long timeStamp, int price)
		{
			this.position = position;
			this.price = price;
			this.candle = candle;
			this.timeStamp = timeStamp;
		}

		public Market getPosition() {
			return position;
		}

		public int getPrice() {
			return price;
		}

		public IndicatorCandle getCandle() {
			return candle;
		}

		public long getTimeStamp() {
			return timeStamp;
		}
	}
	
	public class OrderObservable extends Observable
	{
		public void notifyNewOrder(Order order)
		{
			setChanged();
			notifyObservers(order);
		}
	}
}
