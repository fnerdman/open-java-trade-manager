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

public class BoolParameter implements IParameter<Boolean>{

	private String name;
	
	private boolean defval;
	private boolean actval;
	
	/*public Parameter<Integer>(String name, Integer maxval, Integer minval, Integer defval)
	{
		this.name = name;
		this.maxval = maxval;
		this.minval = minval;
		this.stepsize = stepsize;
		this.defval = defval;
		
		reset();
	}*/
	
	public BoolParameter(String name, boolean defval)
	{
		this.name = name;
		this.defval = defval;
		
		reset();
	}
	
	public void reset()
	{
		actval = defval;
	}
	
	public Boolean setVal(Boolean val)
	{
		return (actval = val);
	}
	
	public String getName()
	{
		return name;
	}
	
	public Boolean getVal()
	{
		return actval;
	}
	
	public Boolean getMinVal()
	{
		return false;
	}
	
	public Boolean getMaxVal()
	{
		return true;
	}
	
	public Boolean getStepSize()
	{
		return true;
	}
	
	
	public Boolean increase()
	{
		return setVal(!actval);
	}
	
	public Boolean decrease()
	{
		return setVal(!actval);
	}
	
	public BoolParameter clone()
	{
		return new BoolParameter(name, defval);
	}
}
