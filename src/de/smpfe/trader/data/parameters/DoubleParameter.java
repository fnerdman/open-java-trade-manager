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

public class DoubleParameter implements IParameter<Double>{

	private String name;
	
	private double maxval;
	private double minval;
	private double defval;
	private double actval;
	private double stepsize;
	
	/*public Parameter<Integer>(String name, Integer maxval, Integer minval, Integer defval)
	{
		this.name = name;
		this.maxval = maxval;
		this.minval = minval;
		this.stepsize = stepsize;
		this.defval = defval;
		
		reset();
	}*/
	
	public DoubleParameter(String name, double maxval, double minval, double stepsize, double defval)
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
	
	public Double setVal(Double val)
	{
		return (actval = (Math.round(Math.round(1.0/stepsize)*Math.min(Math.max(val, minval),maxval)))*stepsize);
	}
	
	public String getName()
	{
		return name;
	}
	
	public Double getVal()
	{
		return actval;
	}
	
	public Double getMinVal()
	{
		return minval;
	}
	
	public Double getMaxVal()
	{
		return maxval;
	}
	
	public Double getStepSize()
	{
		return stepsize;
	}
	
	public Double increase()
	{
		return setVal(actval+stepsize);
	}
	
	public Double decrease()
	{
		return setVal(actval-stepsize);
	}
	
	public DoubleParameter clone()
	{
		return new DoubleParameter(name, maxval, minval, stepsize, defval);
	}
}
