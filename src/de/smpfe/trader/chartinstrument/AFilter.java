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
import de.smpfe.trader.data.order.OrderUpdateType;

public abstract class AFilter extends ADependentInstrument
{
	private FilterType filter = FilterType.FILTEROFF;
	
	public final void update(Observable o, Object arg) 
	{
		super.update(o, arg);
		
		interpret((ArrayList<Message>)arg);
		
		if(hasChanged())
		{
			Message message = new Message(this,MessageType.ORDERCHANGED);
			HashMap<String,Object> payload = new HashMap<String,Object>();
			payload.put("getUpdateType", OrderUpdateType.FILTERCHANGED);
			payload.put("getFilter", filter);
			message.setPayload(payload);
			
			notifyObservers(message);
		}
	}

	protected void changeFilter(FilterType filter) {
		this.filter = filter;
		setChanged();
	}

	public FilterType getFilter() {
		return filter;
	}
}
