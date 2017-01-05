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
package de.smpfe.trader.data.order;

import de.smpfe.trader.chartinstrument.enums.Market;

public class Order 
{
	public String toString()
	{
		return "Position: " + position + " Trigger: " + trigger + " orderType: " + orderType + " stopTrigger: " 
						+ stoptrigger + " targetTrigger " + targettrigger; 
	}
	private Integer trigger = -1;
	private SimpleOrderType orderType;
	
	
	private Market position;
		
	private Integer stoptrigger = -1;
	private Integer targettrigger = -1;
	
	private boolean stopRefPointEntryPrice = false;
	private boolean targetRefPointEntryPrice = false;
	
	/** 
	 * Exit Order
	 */
	public Order()
	{
		this(Market.FLAT,SimpleOrderType.MARKET,-1);
	}
	
	/**
	 * Update Order
	 * @param position
	 */
	public Order(Market position)
	{
		this(position,null,-1);
	}
	
	/**
	 * New Order
	 * @param position
	 * @param orderType
	 * @param trigger
	 */
	public Order(Market position, SimpleOrderType orderType, Integer trigger)
	{
		this.position = position;
		this.orderType = orderType;
		this.trigger = trigger;
	}
	
	public boolean isUpdate()
	{
		return orderType==null;
	}
	
	public Integer getStopTrigger() {
		return stoptrigger;
	}
	public void setStopTrigger(Integer stoptrigger) {
		this.stoptrigger = stoptrigger;
	}
	public Integer getTargetTrigger() {
		return targettrigger;
	}
	public void setTargetTrigger(Integer targettrigger) {
		this.targettrigger = targettrigger;
	}
	public Integer getTrigger() {
		return trigger;
	}
	public SimpleOrderType getOrderType() {
		return orderType;
	}
	public Market getPosition() {
		return position;
	}
	
	public void setPosition(Market position) {
		this.position = position;
	}

	public boolean equals(Object o)
	{
		Order order = (Order)o;
		
		if(	   getPosition() == order.getPosition()
			&& getOrderType() == order.getOrderType()
			&& getTrigger() == order.getTrigger()
			&& getStopTrigger() == order.getStopTrigger()
			&& getTargetTrigger() == order.getTargetTrigger()
			)
			return true;
		
		return false;
	}
	
	public Order clone()
	{
		Order order = new Order(getPosition(),getOrderType(),getTrigger());
		order.setStopTrigger(getStopTrigger());
		order.setTargetTrigger(getTargetTrigger());
		
		return order;
	}

	public void setTrigger(int trigger) {
		this.trigger = trigger;
	}

	public boolean isStopRefPointEntryPrice() {
		return stopRefPointEntryPrice;
	}

	public void setStopRefPointEntryPrice(boolean stopRefPointEntryPrice) {
		this.stopRefPointEntryPrice = stopRefPointEntryPrice;
	}

	public boolean isTargetRefPointEntryPrice() {
		return targetRefPointEntryPrice;
	}

	public void setTargetRefPointEntryPrice(boolean targetRefPointEntryPrice) {
		this.targetRefPointEntryPrice = targetRefPointEntryPrice;
	}
}
