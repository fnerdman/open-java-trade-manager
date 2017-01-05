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

import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.tree.DefaultMutableTreeNode;

import de.smpfe.trader.data.Dependency;
import de.smpfe.trader.data.indicator.AIndicator;
import de.smpfe.trader.data.parameters.IParameter;
import de.smpfe.trader.enums.TimeFrame;

public class TreeParser {
	
	private HashMap<String,IParameter> parameters = new HashMap<String,IParameter>();
	private HashMap<String,Dependency> dependencies = new HashMap<String,Dependency>();
	private HashMap<Object,ArrayList<DefaultMutableTreeNode>> childNodes = new HashMap<Object,ArrayList<DefaultMutableTreeNode>>();
	
	/*

	 * * Version 2:
	 * RootNode:
	 * UserObject is empty, TreeNodes Contain ChartInstrument Info
	 * ChartInstrument:
	 * UserObject = Object[] entry 0 = Name, entry 1 = parameter, TreeNodes contain TimeFrames
	 * TimeFrame:
	 * UserObject = TimeFrame, TreeNodes contain Indicators
	 * Indicator:
	 * UserObject = AIndicator or Dependency, TreeNodes might contain dependent indicator

	 * */

	public TreeParser(DefaultMutableTreeNode tn)
	{
		Object o = tn.getUserObject();
		if(o instanceof Object[])
		{
			for(IParameter p: (IParameter[])((Object[])o)[1])
			{
				parameters.put(p.getName(), p);
			}
		}
		else if(o instanceof AIndicator)
		{
			if(((AIndicator)o).getDependencies()!=null)
			{
				for(Dependency d: ((AIndicator)o).getDependencies())
				{
					dependencies.put(d.getName(), d);
				}
			}
			if(((AIndicator)o).getParameters()!=null)
			{
				for(IParameter p: ((AIndicator)o).getParameters())
				{
					parameters.put(p.getName(), p);
				}
			}
		}
		
		for(int k = 0; k < tn.getChildCount(); k++)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)tn.getChildAt(k);
			o = node.getUserObject();
			String name = null;
			if(o instanceof Object[])
				name = (String)((Object[])o)[0];
			else if(o instanceof AIndicator)
				name = o.getClass().getName();
			else if(o instanceof Dependency)
				name = ((Dependency)o).getName();
			else if(o instanceof TimeFrame)
			{
				childNodes.put(o, new ArrayList<DefaultMutableTreeNode>());
				childNodes.get(o).add(node);
			}
			
			if(name!=null)
			{
				if(!childNodes.containsKey(name))
					childNodes.put(name, new ArrayList<DefaultMutableTreeNode>());
				
				childNodes.get(name).add(node);
			}
		}
	}
	
	public TreeParser getTimeFrame(TimeFrame frame)
	{
		return getNode(frame);
	}
	
	public TreeParser getNode(Object name)
	{
		return getNode(name,0);
	}
	
	public TreeParser getNode(Object name, int index)
	{
		DefaultMutableTreeNode tn = childNodes.get(name).get(index);
		
		if(tn!=null)
			return new TreeParser(tn);
		
		return null;
	}
	
	public IParameter getParameter(String name)
	{
		return parameters.get(name);
	}
	
	public Dependency getDependency(String name)
	{
		return dependencies.get(name);
	}
}
