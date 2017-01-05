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

package de.smpfe.trader.data.parameters;

public class IntParameter implements IParameter<Integer>{

	private String name;
	
	private int maxval;
	private int minval;
	private int defval;
	private int actval;
	private int stepsize;
	
	/*public Parameter<Integer>(String name, Integer maxval, Integer minval, Integer defval)
	{
		this.name = name;
		this.maxval = maxval;
		this.minval = minval;
		this.stepsize = stepsize;
		this.defval = defval;
		
		reset();
	}*/
	
	public IntParameter(String name, int maxval, int minval, int stepsize, int defval)
	{
		this.name = name;
		this.maxval = maxval;
		this.minval = minval;
		this.stepsize = stepsize;
		this.defval = defval;
		
		reset();
	}
	
	public void reset()
	{
		actval = defval;
	}
	
	public Integer setVal(Integer val)
	{
		return (actval = Math.min(Math.max(val, minval),maxval));
	}
	
	public String getName()
	{
		return name;
	}
	
	public Integer getVal()
	{
		return actval;
	}
	
	public Integer getMinVal()
	{
		return minval;
	}
	
	public Integer getMaxVal()
	{
		return maxval;
	}
	
	public Integer getStepSize()
	{
		return stepsize;
	}
	
	public Integer increase()
	{
		return setVal(actval+stepsize);
	}
	
	public Integer decrease()
	{
		return setVal(actval-stepsize);
	}
	
	public IntParameter clone()
	{
		return new IntParameter(name, maxval, minval, stepsize, defval);
	}
}
