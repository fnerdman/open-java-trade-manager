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

package de.smpfe.trader.data;

public class Dependency
{
	private String name;
	private String[] dependencies;

	public Dependency(String dependency)
	{
		this(dependency, new String[] {dependency});
	}

	public Dependency(String name, String[] dependencies)
	{
		this.name = name;
		this.dependencies = dependencies.clone();
	}

	public boolean isDirect()
	{
		return dependencies.length == 1;
	}

	public String getName()
	{
		return name;
	}

	public String[] getDependencies()
	{
		return dependencies;
	}
	
	public void setDependency(int ndx)
	{
		String tmp = dependencies[ndx];
		dependencies[ndx] = dependencies[0];
		dependencies[0] = tmp;
	}
	
	public void setDependency(String dep)
	{
		for(int i = 0;i<dependencies.length;i++)
		{
			if(dep.compareTo(dependencies[i])==0)
			{
				setDependency(i);
				break;
			}
		}
	}

	public String getDependency()
	{
		return dependencies[0];
	}
	
	public String toString()
	{
		return name;
	}
	
	public Dependency clone()
	{
		return new Dependency(name,getDependencies());
	}
}