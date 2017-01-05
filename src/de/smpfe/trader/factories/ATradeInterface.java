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
package de.smpfe.trader.factories;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import de.smpfe.trader.chartinstrument.enums.Market;
import de.smpfe.trader.data.order.Order;

public abstract class ATradeInterface extends Observable implements Observer, ITradeInterface
{
	public final void update(Observable o, Object arg) 
	{
		updateOrder((Order)arg);	
	}
	
	protected void confirmOrder(final Market position, final int price, final long timeStamp)
	{
		final ATradeInterface ti = this;
		new Thread(){
			public void run()
			{
				HashMap<String,Object> payload = new HashMap<String,Object>();
				payload.put("getPosition",position);
				payload.put("getPrice",price);
				payload.put("getTimeStamp",timeStamp);
				
				ti.notifyObservers(payload);
			}
		}.start();
	}
}
