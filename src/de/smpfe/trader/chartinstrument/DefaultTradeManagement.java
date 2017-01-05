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
import java.util.Collections;

import de.smpfe.trader.chartinstrument.enums.Market;
import de.smpfe.trader.data.order.Order;
import de.smpfe.trader.data.order.SimpleOrderType;

public class DefaultTradeManagement extends ATradeManagement
{
	public Order calculateStopTargets(Order order)
	{
		ArrayList<Integer> targets = new ArrayList<Integer>();
		ArrayList<Integer> stops = new ArrayList<Integer>();
		
		ArrayList<Integer> refPointTargets = new ArrayList<Integer>();
		ArrayList<Integer> refPointStops = new ArrayList<Integer>();
		
		for(Object o: getStopTargets().keySet())
		{
			AStopTarget stopTarget = (AStopTarget)o;
			StopTargetInfo sti = getStopTargets().get(o);
			
			switch (sti.getOrderType())
			{
				case IMMEDIATE:
					if(stopTarget.isStop())
					{
						if(sti.isRefPointEntryPrice())
							refPointStops.add(sti.getTrigger());
						else
							stops.add(sti.getTrigger());
					}
					else
					{
						if(sti.isRefPointEntryPrice())
							refPointTargets.add(sti.getTrigger());
						else
							targets.add(sti.getTrigger());
					}
					break;
				case ENDOFPERIOD:
					// need to be in market to execute these stops
					if(this.getCurrentOrderInfo().getPosition()!=Market.FLAT)
					{
						int entryPrice = getCurrentOrderInfo().getPrice();
						
						switch(order.getPosition())
						{   //TODO Entscheidung nicht hier!!
							case LONG:
								if(sti.isRefPointEntryPrice())
								{
									if((getCandle(sti.getReferenceTime()).getClose() <= sti.getTrigger()-entryPrice && stopTarget.isStop())
											|| (getCandle(sti.getReferenceTime()).getClose() >= sti.getTrigger()+entryPrice && stopTarget.isTarget()))
										return new Order();
								}
								else
								{
									if((getCandle(sti.getReferenceTime()).getClose() <= sti.getTrigger() && stopTarget.isStop())
											|| (getCandle(sti.getReferenceTime()).getClose() >= sti.getTrigger() && stopTarget.isTarget()))
										return new Order();
								}
								break;
							case SHORT:
								if(sti.isRefPointEntryPrice())
								{
									if((getCandle(sti.getReferenceTime()).getClose() <= sti.getTrigger()+entryPrice && stopTarget.isStop())
											|| (getCandle(sti.getReferenceTime()).getClose() >= sti.getTrigger()-entryPrice && stopTarget.isTarget()))
										return new Order();
								}
								else
								{
									if((getCandle(sti.getReferenceTime()).getClose() >= sti.getTrigger() && stopTarget.isStop())
											|| (getCandle(sti.getReferenceTime()).getClose() <= sti.getTrigger() && stopTarget.isTarget()))
										return new Order();
								}
								break;
						}
					}
					break;
			}
		}
		
		int refStop = -1;
		int refTarget = -1;
		int stop = -1;
		int target = -1;
		int entryPrice = getCurrentOrderInfo().getPosition()!=Market.FLAT? getCurrentOrderInfo().getPrice():order.getTrigger();
		
		if(order.getPosition()==Market.LONG)
		{
			if(!stops.isEmpty()) stop = Collections.max(stops);
			if(!targets.isEmpty()) target = Collections.min(targets);
			if(!refPointStops.isEmpty()) refStop = Collections.min(refPointStops);
			if(!refPointTargets.isEmpty()) refTarget = Collections.min(refPointTargets);
		}
		else if(order.getPosition()==Market.SHORT)
		{
			if(!stops.isEmpty()) stop = Collections.min(stops);
			if(!targets.isEmpty()) target = Collections.max(targets);
			if(!refPointStops.isEmpty()) refStop = Collections.min(refPointStops);
			if(!refPointTargets.isEmpty()) refTarget = Collections.min(refPointTargets);
		}
		
		if(refStop<0 || (Math.abs(stop-entryPrice)<refStop && stop>=0))
		{
			order.setStopTrigger(stop);
			order.setStopRefPointEntryPrice(false);
		}
		else if(refStop>=0)
		{
			order.setStopTrigger(refStop);
			order.setStopRefPointEntryPrice(true);
		}
		
		if(refTarget<0 || (Math.abs(target-entryPrice)<refTarget && target>=0))
		{
			order.setTargetTrigger(target);
			order.setTargetRefPointEntryPrice(false);
		}
		else if(refTarget>=0)
		{
			order.setTargetTrigger(refTarget);
			order.setTargetRefPointEntryPrice(true);
		}
		
		return order;
	}
	
	public Order getSentimentOrder()
	{
		Order order = getCurOrder().clone();
		
		switch(getCurrentOrderInfo().getPosition())
		{
			case FLAT:
				switch(getCurrentSentimentInfo().getSentiment())
				{
					case ENTRYLONG:
						switch(getCurrentSentimentInfo().getOrderType())
						{
							case ENDOFPERIOD:
								order = new Order(Market.LONG,SimpleOrderType.MARKET,getCurrentSentimentInfo().getTrigger());
								break;
							case STOPDURINGNEXTPERIOD:
								order = new Order(Market.LONG,SimpleOrderType.STOP,getCurrentSentimentInfo().getTrigger());
								break;
							case LIMITDURINGNEXTPERIOD:
								order = new Order(Market.LONG,SimpleOrderType.LIMIT,getCurrentSentimentInfo().getTrigger());
								break;
						}
						break;
					case ENTRYSHORT:
						switch(getCurrentSentimentInfo().getOrderType())
						{
							case ENDOFPERIOD:
								order = new Order(Market.SHORT,SimpleOrderType.MARKET,getCurrentSentimentInfo().getTrigger());
								break;
							case STOPDURINGNEXTPERIOD:
								order = new Order(Market.SHORT,SimpleOrderType.STOP,getCurrentSentimentInfo().getTrigger());
								break;
							case LIMITDURINGNEXTPERIOD:
								order = new Order(Market.SHORT,SimpleOrderType.LIMIT,getCurrentSentimentInfo().getTrigger());
								break;
						}
						break;
					default:
						if(order.getPosition()!=Market.FLAT)
							order = new Order();
						break;
				}
				break;
			case SHORT:
				switch(getCurrentSentimentInfo().getSentiment())
				{
					case ENTRYLONG:
						switch(getCurrentSentimentInfo().getOrderType())
						{
							case ENDOFPERIOD:
								order = new Order(Market.LONG,SimpleOrderType.MARKET,getCurrentSentimentInfo().getTrigger());
								break;
							case STOPDURINGNEXTPERIOD:
								order = new Order(Market.LONG,SimpleOrderType.STOP,getCurrentSentimentInfo().getTrigger());
								break;
							case LIMITDURINGNEXTPERIOD:
								order = new Order(Market.LONG,SimpleOrderType.LIMIT,getCurrentSentimentInfo().getTrigger());
								break;
						}
						break;
					case CLOSESHORT:
						order = new Order();
						break;
				}
				break;
			case LONG:
				switch(getCurrentSentimentInfo().getSentiment())
				{
					case CLOSELONG:
						order = new Order();
						break;
					case ENTRYSHORT:
						switch(getCurrentSentimentInfo().getOrderType())
						{
							case ENDOFPERIOD:
								order = new Order(Market.SHORT,SimpleOrderType.MARKET,getCurrentSentimentInfo().getTrigger());
								break;
							case STOPDURINGNEXTPERIOD:
								order = new Order(Market.SHORT,SimpleOrderType.STOP,getCurrentSentimentInfo().getTrigger());
								break;
							case LIMITDURINGNEXTPERIOD:
								order = new Order(Market.SHORT,SimpleOrderType.LIMIT,getCurrentSentimentInfo().getTrigger());
								break;
						}
						break;
				}
				break;
		}
		
		if(order.getTrigger() != getCurrentSentimentInfo().getTrigger())
			order.setTrigger(getCurrentSentimentInfo().getTrigger());
		
		return order;
	}

	public void interpret(ArrayList<Message> messages)
	{
		Order order = getSentimentOrder();

		order = calculateStopTargets(order);
		
		if(!order.equals(getCurOrder()))
			sendNewOrder(order);
	}

	@Override
	public void updateOnOrderFeedBack(OrderInfo oi) {
		//TODO
		switch(oi.getPosition())
		{
		case FLAT:
			break;
		case LONG:
			break;
		case SHORT:
			break;
		default:
			break;
		
		}
	}
}
