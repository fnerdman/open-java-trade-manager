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
import java.util.Observable;
import java.util.Observer;

import de.smpfe.trader.chartinstrument.DefaultTradeManagement;
import de.smpfe.trader.data.Plotable;
import de.smpfe.trader.data.Underlying;
import de.smpfe.trader.data.chart.Chart;
import de.smpfe.trader.data.indicator.AIndicator;
import de.smpfe.trader.data.indicator.IndicatorCandle;
import de.smpfe.trader.data.order.Order;
import de.smpfe.trader.enums.TimeFrame;
import de.smpfe.trader.factories.ATradeInterface;
import de.smpfe.trader.factories.TradingSystemFactory;
import de.smpfe.trader.factories.TradingSystemInfo;
import de.smpfe.trader.tradingsystem.sentimentstrategy.BeispielStrategy;

public class TradingSystemStarter implements Observer
{
	public class TestTradeInterface extends ATradeInterface
	{
		public void updateOrder(Order order) {
			System.out.println(order);
		}	
	}

	public static void main(String[] args) throws Exception
	{
		String dir = "files//eurusd.intra";

		Underlying u = new Underlying("EUR/USD");
		
		TradingSystemInfo system1 = new TradingSystemInfo();
		system1.sentimentStrategy = BeispielStrategy.class.getName();
		system1.tradeManagement = DefaultTradeManagement.class.getName();
		TradingSystemInfo[] tssettings = new TradingSystemInfo[]{
				system1
		};
		
		TradingSystemStarter starter = new TradingSystemStarter();
		TestTradeInterface tti = starter.new TestTradeInterface();
		
		TradingSystemFactory tsf = new TradingSystemFactory();
		tsf.loadTradingSystems(tti, tssettings);
		tsf.finalizeTradingSystem(u);
		
		Chart chart = new Chart(10000,TimeFrame.MIN5);
		chart.parseFromFile(new File(dir));

		
		// add main als chartlistner
		// rcchart.addObserver(starter);

		// berechne die indikatoren auf den basic candles
		chart.addObserver(u);

		chart.runThroughChart();
	}

	@Override
	public void update(Observable arg0, Object arg1)
	{
		if(arg1 instanceof IndicatorCandle)
		{
			IndicatorCandle candle = (IndicatorCandle)arg1;
			System.out.println(candle.toString());
			System.out.println(candle.getTimeFrame());
			for(AIndicator indi: candle.getIndicators().values())
			{
				System.out.print("Indicator: " + indi.getClass().getSimpleName() + " Plotables: ");
				for(Plotable p: indi.getPlotables())
				{
					try {
						System.out.print(p.getMethodToPlot().invoke(indi, null)+"; ");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				System.out.println();
			}
			System.out.println();
		}
	}
}
