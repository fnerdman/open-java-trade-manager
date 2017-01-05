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
package de.smpfe.trader.tradingsystem.sentimentstrategy;

import java.util.ArrayList;
import java.util.Date;

import de.smpfe.trader.chartinstrument.ASentimentStrategy;
import de.smpfe.trader.chartinstrument.Message;
import de.smpfe.trader.chartinstrument.enums.SentiOrderType;
import de.smpfe.trader.chartinstrument.enums.Sentiment;
import de.smpfe.trader.data.Dependency;
import de.smpfe.trader.enums.TimeFrame;
import de.smpfe.trader.indicators.EMA;
import de.smpfe.trader.indicators.MA;

public class BeispielStrategy extends ASentimentStrategy
{

	public BeispielStrategy()
	{
		super();
		this.addDependencies(TimeFrame.MIN5, new Dependency[]{
				new Dependency("de.smpfe.trader.indicators.RenkoSAR"),
				new Dependency("de.smpfe.trader.indicators.ATR"),
				new Dependency("de.smpfe.trader.indicators.MA"),
				new Dependency("de.smpfe.trader.indicators.EMA")
		});
		
		this.addDependencies(TimeFrame.MIN10, new Dependency[]{
				new Dependency("de.smpfe.trader.indicators.RenkoSAR"),
				new Dependency("de.smpfe.trader.indicators.ATR"),
				new Dependency("de.smpfe.trader.indicators.MA"),
				new Dependency("de.smpfe.trader.indicators.EMA")
		});
		
		try {
			this.changeOrderType(SentiOrderType.ENDOFPERIOD);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//TODO 
		// changeOrderType f�r Exit sentiment
	}
	public void interpret(ArrayList<Message> messages) 
	{	
		MA ma5 = (MA)this.getIndicators(TimeFrame.MIN5)[2];
		EMA ma10 = (EMA)this.getIndicators(TimeFrame.MIN5)[3];
		
		if(ma5.getCandle().isFirst())
			return;
		
		double dma5 = ma5.getMA();
		double dma10 = ma10.getEma();
		double ddma5 = ma5.getMA(1);
		double ddma10 = ma10.getEma(1);

		
		if((dma5>dma10) && (ddma5<=ddma10))
		{
			System.out.println("ENTRYSHORT " + new Date(ma5.getCandle().getStartTimeStamp()));
			if(this.getSentiment()!=Sentiment.ENTRYSHORT)
			{
				this.changeSentiment(Sentiment.ENTRYSHORT);
            	//this.setTrigger(this.getCandle(TimeFrame.MIN5).getClose());
				//this.setTrigger(-1);
			}			
		}
		else if((dma5<dma10) && (ddma5>=ddma10))
		{
			System.out.println("ENTRYLONG " + new Date(ma5.getCandle().getStartTimeStamp()));
			if(this.getSentiment()!=Sentiment.ENTRYLONG)
			{
				this.changeSentiment(Sentiment.ENTRYLONG);
				//this.setTrigger(this.getCandle(TimeFrame.MIN5).getClose());
				//this.setTrigger(-1);
			}
		}
		else
		{
			//System.out.println("NEUTRAL " + new Date(ma5.getCandle().getStartTimeStamp()));
			this.changeSentiment(Sentiment.NEUTRAL);
		}
	}

	
	public boolean updateOnChartData() {
		
		return true;
	}

	
	public boolean updateOnSentimentChange() {
		
		return false;
	}

	
	public boolean updateOnStopTargetChange() {
		
		return false;
	}

	
	public boolean updateOnFilterChange() {
		
		return false;
	}

	
	public boolean updateOnOrderFeedback() {
		
		return false;
	}

}
