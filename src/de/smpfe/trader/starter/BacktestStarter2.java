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


package de.smpfe.trader.starter;

import java.io.File;

import javax.swing.tree.DefaultMutableTreeNode;

import de.smpfe.trader.backtest.Backtest;
import de.smpfe.trader.chartinstrument.DefaultTradeManagement;
import de.smpfe.trader.data.chart.Chart;
import de.smpfe.trader.data.parameters.IParameter;
import de.smpfe.trader.enums.TimeFrame;
import de.smpfe.trader.factories.IParameterSetter;
import de.smpfe.trader.factories.TradingSystemInfo;
import de.smpfe.trader.factories.TreeParser;
import de.smpfe.trader.indicators.MA;
import de.smpfe.trader.tradingsystem.sentimentstrategy.BeispielStrategy;
import de.smpfe.trader.tradingsystem.stoptarget.FixedStop;
import de.smpfe.trader.tradingsystem.stoptarget.FixedTarget;

public class BacktestStarter2
{

	public static void main(String[] args) 
	{
		//String dir = "files//eurusd2.txt";
		String dir = "files//MSCIEmergingMarketsEURSTRD.txt";
		
		Chart chart = new Chart(1000,TimeFrame.DAY);
		chart.parseFromFileDay(new File(dir));

		// 
		// �nderungen der Default Parameter
		//
		
		IParameterSetter setter = new IParameterSetter()
		{
			int defVal = 100;
			
			public void setParameter(DefaultMutableTreeNode[] treeNode)
			{
				TreeParser tp = new TreeParser(treeNode[0]);
				IParameter ip = tp.getNode(BeispielStrategy.class.getName()).getTimeFrame(TimeFrame.MIN5).getNode(MA.class.getName()).getParameter("MASpan");
				ip.setVal(defVal);
				defVal+=50;
			}
		};
		
		// 
		// Trading System Einstellungen
		//
		
		TradingSystemInfo system1 = new TradingSystemInfo();
		system1.sentimentStrategy = BeispielStrategy.class.getName();
		system1.tradeManagement = DefaultTradeManagement.class.getName();
		system1.stopTargets = new String[]{FixedStop.class.getName() };
		//system1.stopTargets = new String[]{FixedTarget.class.getName() };
		//,BeispielTarget.class.getName()};
		//system1.filters = new String[]{BeispielFilter.class.getName()};
		TradingSystemInfo[] tsinfo = new TradingSystemInfo[]{
				system1
		};
		
		
		// for(int k = 0;k<2;k++){   // f�r mehrere Parameterdurchl�ufe
		for(int k = 0;k<1;k++){		 // nur Default Parameterwerte
			Backtest bt = new Backtest();
			bt.setCandles(chart.getAchart());
			bt.backtest(tsinfo,setter);
		}

	}

}