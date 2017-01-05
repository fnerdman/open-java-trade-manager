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
import java.util.Iterator;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import de.smpfe.trader.chartinstrument.AChartInstrument;
import de.smpfe.trader.chartinstrument.ADependentInstrument;
import de.smpfe.trader.chartinstrument.AFilter;
import de.smpfe.trader.chartinstrument.ASentimentStrategy;
import de.smpfe.trader.chartinstrument.AStopTarget;
import de.smpfe.trader.chartinstrument.ATradeManagement;
import de.smpfe.trader.data.Dependency;
import de.smpfe.trader.data.Underlying;
import de.smpfe.trader.data.indicator.AIndicator;
import de.smpfe.trader.data.indicator.RollingFrameChart;
import de.smpfe.trader.data.parameters.IParameter;
import de.smpfe.trader.enums.TimeFrame;

/**
 * creates one/multiple Trading systems for one underlying
 */
public class TradingSystemFactory {
	/**
	 * data structure:
	 * one ASentimentStrategy has
	 * * AStopTarget[]
	 * * AFilter[]
	 * * ATradeManagement
	 * 
	 * vorgehen:
	 * 
	 * zunaechst werden alle objekte erstellt und miteinander verbunden (referenzen herstellen)
	 * dann wird festegestellt, wer welche charts mit welchen indikatoren braucht
	 * dann werden die charts erstellt, und darauf mittels der ifactory die indikatoren
	 * diese muessen dann noch auf die einzelnen instrumente verlinkt werden.
	 * 
	 * was ist mit parametern?
	 * 
	 * 
	 * 
	 */
	
	public DefaultMutableTreeNode[] getTradingSystemNodes() {
		return tradingSystems;
	}
	
	private HashMap<TimeFrame,IndicatorFactory> factories;
	private DefaultMutableTreeNode[] tradingSystems;
	private HashMap<TimeFrame,HashMap<ADependentInstrument,Dependency[]>> indiDeps;
	private HashMap<ADependentInstrument,DefaultMutableTreeNode> treeNodeRef;
	private ArrayList<AChartInstrument> allChartInstruments;
	
	
	public void loadTradingSystems(ATradeInterface ti, TradingSystemInfo[] tssettings) throws Exception
	{
		treeNodeRef = new HashMap<ADependentInstrument,DefaultMutableTreeNode>();
		tradingSystems = new DefaultMutableTreeNode[tssettings.length];
		indiDeps = new HashMap<TimeFrame,HashMap<ADependentInstrument,Dependency[]>>();
		allChartInstruments = new ArrayList<AChartInstrument>();
		
		ASentimentStrategy[] sentimentStrategies = new ASentimentStrategy[tssettings.length];
		ArrayList<ADependentInstrument> depInstrs = new ArrayList<ADependentInstrument>();
		
		for(int k = 0; k < tssettings.length; k++)
		{
			tradingSystems[k] = new DefaultMutableTreeNode();
			sentimentStrategies[k] = generateTradingSystem(ti, tradingSystems[k], tssettings[k]);
			
			depInstrs.add(sentimentStrategies[k]);
			depInstrs.addAll(sentimentStrategies[k].getStopTargets());
			depInstrs.addAll(sentimentStrategies[k].getFilters());
		}
		
		for(ADependentInstrument depInstr: depInstrs)
		{
			for(TimeFrame tf: depInstr.getDependencies().keySet())
			{
				if(!indiDeps.containsKey(tf))
					indiDeps.put(tf, new HashMap<ADependentInstrument,Dependency[]>());
				
				indiDeps.get(tf).put(depInstr, depInstr.getDependencies().get(tf));
			}
		}
		
		factories = new HashMap<TimeFrame,IndicatorFactory>();
		/*
		 * Problem:
		 * 
		 * Die in der IFactory hergestellte TreeNode muss passend an die hier existierende angebunden werden
		 * + es muessen spaeter aus dem AIndicator[][] array die observer richtig angemeldet werden
		 * */
		for(TimeFrame tf: indiDeps.keySet())
		{
			IndicatorFactory ifact = new IndicatorFactory();
			factories.put(tf, ifact);
			
			HashMap<ADependentInstrument,Dependency[]> hMap = indiDeps.get(tf);
			Dependency[][] deps = new Dependency[hMap.size()][];
			int k = 0;
			for(ADependentInstrument d: hMap.keySet())
			{
				deps[k]=hMap.get(d);
				k++;
			}
			ifact.loadIndicators(deps);
			
			
			// copy info into tree
			DefaultMutableTreeNode[] treeNodes = ifact.getRootNodes();
			Iterator<ADependentInstrument> it = indiDeps.get(tf).keySet().iterator();
			for(DefaultMutableTreeNode treeNode: treeNodes)
			{
				treeNode.setUserObject(tf);
				treeNodeRef.get(it.next()).add(treeNode);
			}
		}
	}
	
	public void finalizeTradingSystem(Underlying u) throws Exception
	{
		for(TimeFrame tf: indiDeps.keySet())
		{
			IndicatorFactory ifact = factories.get(tf);
			RollingFrameChart rfc = u.addChart(tf,ifact.finalizeIndis());
			
			AIndicator[][] depIndis = ifact.getTopLevelIndicators();
			Iterator<ADependentInstrument> it = indiDeps.get(tf).keySet().iterator();
			for(AIndicator[] depIndi: depIndis)
			{
				ADependentInstrument depInst = it.next();
				depInst.setIndicators(tf,depIndi);
				rfc.addObserver(depInst.getMailBox());
				
				for(AIndicator indi: depIndi)
				{
					indi.addObserver(depInst.getMailBox());
				}
			}
			for(AChartInstrument tm: allChartInstruments)
			{
				if(tm instanceof ATradeManagement)
					rfc.addObserver(tm.getMailBox());
			}
		}
		
		for(AChartInstrument tm: allChartInstruments)
		{
			u.addInstrumentObserver(tm.getMailBox());
		}
	}
	
	private ASentimentStrategy generateTradingSystem(ATradeInterface ti, DefaultMutableTreeNode tradingSystems2, TradingSystemInfo tssettings) throws Exception
	{
		ASentimentStrategy sentimentStrategy = 
				(ASentimentStrategy) getClass(tssettings.sentimentStrategy).newInstance();
		ATradeManagement tradeManagement = 
				(ATradeManagement) getClass(tssettings.tradeManagement).newInstance();
		
		ArrayList<AStopTarget> stopTargets = new ArrayList<AStopTarget>();
		ArrayList<AFilter> filters = new ArrayList<AFilter>();
		if(tssettings.stopTargets!=null)
		{
			for(String is: tssettings.stopTargets)
			{
				AStopTarget stopTarget = (AStopTarget)getClass(is).newInstance();
				stopTarget.setTradeManagement(tradeManagement);
				stopTargets.add(stopTarget);
			}
		}
		
		if(tssettings.filters!=null)
		{
			for(String is: tssettings.filters)
			{
				AFilter filter = (AFilter)getClass(is).newInstance();
				filter.setTradeManagement(tradeManagement);
				filters.add(filter);
			}
		}
		
		sentimentStrategy.setStopTargets(stopTargets);
		sentimentStrategy.setFilters(filters);
		sentimentStrategy.setTradeManagement(tradeManagement);
		
		
		// let everybody listen to everybody
		ArrayList<AChartInstrument> all = new ArrayList<AChartInstrument>();
		all.add(tradeManagement);
		all.add(sentimentStrategy);
		all.addAll(filters);
		all.addAll(stopTargets);
		allChartInstruments.addAll(all);
		
		for(AChartInstrument ci0: all)
		{
			for(AChartInstrument ci1: all)
			{
				ci0.addObserver(ci1.getMailBox());
			}
			
			// add to treenode
			DefaultMutableTreeNode tmpNode = new DefaultMutableTreeNode();
			Object[] data = new Object[2];
			data[0] = ci0.getClass().getName();
			data[1] = ci0.getParameters();
			tmpNode.setUserObject(data);
			tradingSystems2.add(tmpNode);
			if(ci0 instanceof ADependentInstrument)
				treeNodeRef.put((ADependentInstrument)ci0, tmpNode);
		}
		
		// let trademanagement listen to tradeinterface
		tradeManagement.getOrderObservable().addObserver(ti);
		ti.addObserver(tradeManagement);
		
		return sentimentStrategy;
	}

	private Class getClass(String className) throws ClassNotFoundException {
		return Class.forName (className);
	}

}
