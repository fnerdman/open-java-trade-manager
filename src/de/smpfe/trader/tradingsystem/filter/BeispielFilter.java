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
package de.smpfe.trader.tradingsystem.filter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import de.smpfe.trader.chartinstrument.AFilter;
import de.smpfe.trader.chartinstrument.Message;
import de.smpfe.trader.chartinstrument.enums.FilterType;
import de.smpfe.trader.data.Dependency;
import de.smpfe.trader.data.indicator.IndicatorCandle;
import de.smpfe.trader.enums.TimeFrame;
import de.smpfe.trader.indicators.MA;

public class BeispielFilter extends AFilter
{

	public BeispielFilter()
	{
		// TODO created method registerForTimeFrame(TimeFrame)
		this.addDependencies(TimeFrame.MIN5, new Dependency[]{new Dependency(MA.class.getName())});
	}
	
	@Override
	public void interpret(ArrayList<Message> messages) 
	{	
		
		IndicatorCandle ic = getCandle(TimeFrame.MIN5);
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(ic.getStartTimeStamp());
		
		if(gc.get(Calendar.HOUR)>12)
			changeFilter(FilterType.FILTERON);
		else
			changeFilter(FilterType.FILTEROFF);
		
	}

	@Override
	public boolean updateOnChartData() {
		
		return true;
	}

	@Override
	public boolean updateOnSentimentChange() {
		
		return false;
	}

	@Override
	public boolean updateOnStopTargetChange() {
		
		return false;
	}

	@Override
	public boolean updateOnFilterChange() {
		
		return false;
	}

	@Override
	public boolean updateOnOrderFeedback() {
		
		return false;
	}

}
