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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.NoSuchElementException;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import de.smpfe.trader.starter.Starter;
import de.smpfe.trader.data.Dependency;
import de.smpfe.trader.data.indicator.AIndicator;
import de.smpfe.trader.data.parameters.IParameter;

public class IndicatorFactory {
	
	private static String[] indi_names = {"de.smpfe.trader.indicators.ATR", "de.smpfe.trader.indicators.ISAR", "de.smpfe.trader.indicators.RenkoSAR$GlobalVars", "de.smpfe.trader.indicators.RenkoSAR"};
	private static String jar_dir = "jars//indicators.jar";
	
	private static HashMap<String,Class<AIndicator>> allIndicatorClasses;
	
	public IndicatorFactory() throws MalformedURLException, ClassNotFoundException
	{
		loadIndicatorClasses();
	}
	
	private DefaultMutableTreeNode[] rootNodes;
	
	public DefaultMutableTreeNode[] getRootNodes() {
		return rootNodes;
	}

	public DefaultMutableTreeNode[] loadIndicators(Dependency[][] dependencies) throws Exception
	{
		rootNodes = new DefaultMutableTreeNode[dependencies.length];
		
		for(int k = 0; k< dependencies.length; k++)
		{
			rootNodes[k] = new DefaultMutableTreeNode();
			for(Dependency d: dependencies[k])
			{
				rootNodes[k].add(getDependencyTree(d));
			}
		}
		
		return rootNodes;
	}
	
	private DefaultMutableTreeNode getDependencyTree(Dependency dependency) throws Exception
	{
		DefaultMutableTreeNode rootnode = new DefaultMutableTreeNode();
		
		if(dependency.isDirect())
		{
			AIndicator indi = getIndicatorClass(dependency.getDependency()).getConstructor(null).newInstance(null);
			rootnode.setUserObject(indi);
			Dependency[] deps = indi.getDependencies();
			
			if(deps!=null)
			{
				for(Dependency dep:deps)
				{
					rootnode.add(getDependencyTree(dep));
				}
			}
		}
		else
		{
			rootnode.setUserObject(dependency);
			for(String indicator: dependency.getDependencies())
			{
				//Dependency d = dependency.clone();
				//d.setDependency(indicator);
				// d ist hier immer indirect dependency, folglich wird getDependencyTree immer wieder aufgerufen
				// spontan:
				Dependency d = new Dependency(indicator);
				// muss aber noch den rest des codes checken
				rootnode.add(getDependencyTree(d));
			}
		}
		
		return rootnode;
	}
	
	public AIndicator[] finalizeIndis() throws Exception
	{
		// indirekte abhaengigkeiten aufloesen
		ArrayList<DefaultMutableTreeNode> indirectNodes = new ArrayList<DefaultMutableTreeNode>();
		for(DefaultMutableTreeNode firstnode: rootNodes)
		{
			Enumeration<DefaultMutableTreeNode> nodes = firstnode.breadthFirstEnumeration();
			while (nodes.hasMoreElements())
			{
				DefaultMutableTreeNode tmp = nodes.nextElement();
				if(tmp.getUserObject() instanceof Dependency)
				{
					indirectNodes.add(tmp);
				}
			}
		}
		
		for(DefaultMutableTreeNode node: indirectNodes)
		{
			String chosenIndicator = ((Dependency)node.getUserObject()).getDependency();
			
			for(int k = 0; k < node.getChildCount(); k++)
			{
				if(0==chosenIndicator.compareTo(((DefaultMutableTreeNode)node.getChildAt(k)).getUserObject().getClass().getName()))
				{
					((DefaultMutableTreeNode)node.getParent()).add((MutableTreeNode)node.getChildAt(k));
					node.removeFromParent();
					break;
				}
			}
			if(node.getParent()!=null) throw new Exception("no indirect dependency found");
		}
		
		
		// add all dependent indicators to one rootnode
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
		for(DefaultMutableTreeNode firstnode: rootNodes)
		{
			rootNode.add(firstnode);
		}
		
		// eliminate redundancy
		// first collect all same classes
		HashMap<Class,ArrayList<DefaultMutableTreeNode>> sameClasses
				= new HashMap<Class,ArrayList<DefaultMutableTreeNode>>();
		Enumeration<DefaultMutableTreeNode> nodes = rootNode.breadthFirstEnumeration();
		while (nodes.hasMoreElements())
		{
			DefaultMutableTreeNode tmp = nodes.nextElement();
			if(tmp.getUserObject() instanceof AIndicator)
			{
				AIndicator indi = (AIndicator)tmp.getUserObject();
				Class indiClass = indi.getClass();
				
				if(!sameClasses.containsKey(indiClass))
					sameClasses.put(indiClass, new ArrayList<DefaultMutableTreeNode>());
				sameClasses.get(indiClass).add(tmp);
			}
		}
		
		// then check whether they are redundant
		for(ArrayList<DefaultMutableTreeNode> sameIndis: sameClasses.values())
		{
			HashMap<String,AIndicator> uniques = new HashMap<String,AIndicator>();
			for(DefaultMutableTreeNode tn: sameIndis)
			{
				String unqiueID = getUniqueID(tn);
				if(uniques.containsKey(unqiueID))
					tn.setUserObject(uniques.get(unqiueID));
				else
					uniques.put(unqiueID, (AIndicator)tn.getUserObject());
			}
		}
		
		// iterate through tree to set all dependencies on indicators
		nodes = rootNode.breadthFirstEnumeration();
		while (nodes.hasMoreElements())
		{
			DefaultMutableTreeNode tmp = nodes.nextElement();
			if(tmp.getUserObject() instanceof AIndicator && tmp.getChildCount()>0)
			{
				AIndicator[] depIndis = new AIndicator[tmp.getChildCount()];
				for(int k = 0; k < tmp.getChildCount(); k++)
				{
					depIndis[k] = (AIndicator)((DefaultMutableTreeNode)tmp.getChildAt(k)).getUserObject();
				}
				((AIndicator)tmp.getUserObject()).setDependentIndicators(depIndis);
			}
		}
		
		// create new tree which can be deleted, to topologically sort the indicator array
		rootNode = new DefaultMutableTreeNode();
		for(DefaultMutableTreeNode firstnode: rootNodes)
		{
			rootNode.add(cloneTree(firstnode));
		}
		
		// Topological sort
		DefaultMutableTreeNode leafNode;
		ArrayList<AIndicator> indis = new ArrayList<AIndicator>();
		while((leafNode = rootNode.getFirstLeaf()) != rootNode)
		{
			if(leafNode.getUserObject() instanceof AIndicator)
			{
				AIndicator tmp = (AIndicator)leafNode.getUserObject();
				if(!indis.contains(tmp)) indis.add(tmp);
			}

			leafNode.removeFromParent();
		}

		return indis.toArray(new AIndicator[indis.size()]);
	}
	
	private String getUniqueID(DefaultMutableTreeNode tn) {
		AIndicator indi = (AIndicator)tn.getUserObject();
		String result = indi.getClass().getName();
		for(IParameter p: indi.getParameters())
		{
			result+=";"+p.getVal();
		}
		for(int k=0;k<tn.getChildCount();k++)
		{
			result+="|"+getUniqueID((DefaultMutableTreeNode)tn.getChildAt(k));
		}
		return result;
	}

	public AIndicator[][] getTopLevelIndicators()
	{
		AIndicator[][] indicators = new AIndicator[rootNodes.length][];
		
		int k = 0;
		for(DefaultMutableTreeNode tn: rootNodes)
		{
			indicators[k] = new AIndicator[tn.getChildCount()];
			for(int c = 0;c < tn.getChildCount(); c++)
			{
				indicators[k][c] = (AIndicator)((DefaultMutableTreeNode)tn.getChildAt(c)).getUserObject();
			}
			k++;
		}
		
		return indicators;
	}
	
	public DefaultMutableTreeNode cloneTree( DefaultMutableTreeNode root)
	{
		DefaultMutableTreeNode retVal = (DefaultMutableTreeNode) root.clone();
		for (Enumeration e = root.children(); e.hasMoreElements();) {
	         retVal.add(cloneTree((DefaultMutableTreeNode) e.nextElement()));
	    }
		return retVal;
	}

	private void loadIndicatorClasses() throws MalformedURLException, ClassNotFoundException
	{
		
		// DATABASE TODO
		
		allIndicatorClasses = new HashMap<String,Class<AIndicator>>();
    	
 
    	URLClassLoader ucl = new URLClassLoader(new URL[]{(new File(jar_dir)).toURI().toURL()},Starter.class.getClassLoader());
		
		Class<AIndicator> aClass = null;
		
		for(String s: indi_names)
		{
	        aClass = (Class<AIndicator>) Class.forName (s, true, ucl);
	        allIndicatorClasses.put(s, aClass);
	        
	        System.out.println("aClass.getName() = " + aClass.getName());
		}
	}
	
	private Class<AIndicator> getIndicatorClass(String classname) throws ClassNotFoundException
	{
		Class<AIndicator> tmpclass;
		
		if((tmpclass = allIndicatorClasses.get(classname)) == null)
		{
			return (Class<AIndicator>) Class.forName (classname);
		}
		
		return tmpclass;
	}
}
