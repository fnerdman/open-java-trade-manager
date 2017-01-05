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

import de.smpfe.trader.chartinstrument.enums.StopTargetOrderType;
import de.smpfe.trader.data.order.OrderUpdateType;
import de.smpfe.trader.enums.TimeFrame;

public abstract class AStopTarget extends ADependentInstrument
{
	private StopTargetOrderType orderType = StopTargetOrderType.IMMEDIATE;  //default
	private TimeFrame referenceTime;
	private int trigger;
	private boolean removeStopTarget = false;
	private boolean isTarget = false;
	private boolean refPointEntryPrice = false;
	
	public AStopTarget()
	{
		this(false);
	}
	
	public AStopTarget(boolean isTarget)
	{
		this.isTarget = isTarget;
	}
	
	public final void update(Observable o, Object arg) 
	{
		super.update(o, arg);
		
		interpret((ArrayList<Message>)arg);
		
		if(hasChanged())
		{
			Message message = new Message(this,MessageType.ORDERCHANGED);
			HashMap<String,Object> payload = new HashMap<String,Object>();
			payload.put("getUpdateType", OrderUpdateType.STOPTARGETCHANGED);
			if(removeStopTarget)
			{
				removeStopTarget = false;
			}
			else
			{
				payload.put("getOrderType", orderType);
				payload.put("getReferenceTime", referenceTime);
				payload.put("getTrigger", trigger);
				payload.put("getRefPointEntryPrice", refPointEntryPrice);
			}
			message.setPayload(payload);
			
			notifyObservers(message);
		}
	}
	
	/**
	 * Removes current stop if there is one, NOTE: if this method is called, all subsequent 
	 * changes to the stop value during the same period before and after will have no effect
	 */
	public void removeStopTarget()
	{
		removeStopTarget=true;
		setChanged();
	}

	protected void changeOrderType(StopTargetOrderType orderType) throws Exception {
		if(orderType==StopTargetOrderType.ENDOFPERIOD && this.getReferenceTime()==null)
			throw new Exception("no reference time specified");
		this.orderType = orderType;
		setChanged();
	}

	public StopTargetOrderType getOrderType() {
		return orderType;
	}
	
	protected void setReferenceTime(TimeFrame tf)
	{
		referenceTime = tf;
		setChanged();
	}
	
	public TimeFrame getReferenceTime()
	{
		return referenceTime;
	}
	
	protected void setTrigger(int trigger) {
		this.trigger = trigger;
		setChanged();
	}

	public int getTrigger() {
		return trigger;
	}

	public boolean isStop() {
		return !isTarget;
	}

	public boolean isTarget() {
		return isTarget;
	}

	public boolean isRefPointEntryPrice() {
		return refPointEntryPrice;
	}

	/**
	 * set to true if triggers reference point is the EntryPrice 
	 * */
	protected void setRefPointEntryPrice(boolean refPointEntryPrice) {
		this.refPointEntryPrice = refPointEntryPrice;
	}
}
