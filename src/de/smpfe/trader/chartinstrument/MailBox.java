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
import java.util.Observer;

import de.smpfe.trader.chartinstrument.enums.FilterType;
import de.smpfe.trader.chartinstrument.enums.Market;
import de.smpfe.trader.chartinstrument.enums.SentiOrderType;
import de.smpfe.trader.chartinstrument.enums.Sentiment;
import de.smpfe.trader.chartinstrument.enums.StopTargetOrderType;
import de.smpfe.trader.data.Underlying;
import de.smpfe.trader.data.indicator.IndicatorCandle;
import de.smpfe.trader.data.order.OrderUpdateType;
import de.smpfe.trader.enums.TimeFrame;

public class MailBox extends Observable implements Observer
{
	private ArrayList<Message> mailBox;
	private AChartInstrument client;
	private Object mutex = null;
	
	public MailBox(AChartInstrument client)
	{
		mailBox = new ArrayList<Message>();
		this.client = client;
		addObserver(client);
	}

	public void update(Observable o, Object arg) 
	{
		Message m = (Message)arg;
		HashMap<String,Object> payload;
		
		
		ATradeManagement tmclient = null;
		if(client instanceof ATradeManagement)
			tmclient = (ATradeManagement)client;
		
		switch(m.getMessageType())
		{
			case DEPENDENCY:
				setChanged();
				break;
			case CANDLE:
				if(client.updateOnChartData())
					setChanged();

				IndicatorCandle ic = (IndicatorCandle)m.getPayload();
				client.updateCandle(ic);
				
				break;
			case ORDERCHANGED:
				
				payload = (HashMap<String,Object>)m.getPayload();
				OrderUpdateType out = (OrderUpdateType)payload.get("getUpdateType");
				
				switch(out)
				{
					case SENTIMENTCHANGED:
						
						if(client.updateOnSentimentChange())
							setChanged();
						
						if(tmclient!=null)
						{
							Sentiment sentiment = (Sentiment)payload.get("getSentiment");
							SentiOrderType orderType = (SentiOrderType)payload.get("getOrderType");
							Integer trigger = (Integer)payload.get("getTrigger");
							tmclient.updateSentiment(sentiment,orderType,trigger);
						}
						break;
					case STOPTARGETCHANGED:
						
						if(client.updateOnStopTargetChange())
							setChanged();
						
						if(tmclient!=null)
						{
							Object oType = payload.get("getOrderType");
							if(oType != null)
							{
								StopTargetOrderType orderType = (StopTargetOrderType)oType;
								TimeFrame referenceTime = (TimeFrame)payload.get("getReferenceTime");
								Integer trigger = (Integer)payload.get("getTrigger");
								Boolean refPointEntryPrice = (Boolean)payload.get("getRefPointEntryPrice");
								tmclient.updateStopTarget(m.getInvoker(),orderType,referenceTime,trigger,refPointEntryPrice);
							}
							else
								tmclient.removeStopTarget(m.getInvoker());
						}
						break;
					case FILTERCHANGED:
						
						if(client.updateOnFilterChange())
							setChanged();
						
						if(tmclient!=null)
						{
							FilterType filter = (FilterType)payload.get("getFilter");
							tmclient.updateFilter(m.getInvoker(),filter);
						}
						break;
				}
				break;
			case ORDERFEEDBACK:
				synchronized(mutex)
				{
					if(client.updateOnOrderFeedback())
						setChanged();
				}
				break;
			case UPDATE:
				
				if(mutex==null)
					mutex = o;
				
				if(hasChanged())
				{
					notifyObservers(mailBox);
					mailBox.clear();
				}
				break;
		}
	}

	public Object getMutex() {
		return mutex;
	}

}
