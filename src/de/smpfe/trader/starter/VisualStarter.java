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

import de.smpfe.trader.data.Dependency;
import de.smpfe.trader.data.chart.Chart;
import de.smpfe.trader.enums.TimeFrame;
import de.smpfe.trader.factories.IndicatorFactory;
import de.smpfe.trader.gui.ChartDataCollector;
import de.smpfe.trader.gui.ChartJDialog;

public class VisualStarter  {

	public static void main(String[] args) throws Exception
	{
		Dependency[][] deps = new Dependency[][]{
				new Dependency[]
				{
				new Dependency("de.smpfe.trader.indicators.RenkoSAR"),
				new Dependency("de.smpfe.trader.indicators.ATR"),
				new Dependency("de.smpfe.trader.indicators.MA"),
				new Dependency("de.smpfe.trader.indicators.EMA")
				},
				new Dependency[]
				{
				new Dependency("de.smpfe.trader.indicators.RenkoSAR"),
				new Dependency("de.smpfe.trader.indicators.ATR"),
				new Dependency("de.smpfe.trader.indicators.MA"),
				//new Dependency("de.smpfe.trader.indicators.EMA")
				}
			};
		loadChart(deps);
	}

	private static void loadChart(Dependency[][] deps) throws Exception
	{
		String dir = "files//eurusd.intra";

		IndicatorFactory ifact = new IndicatorFactory();

		ifact.loadIndicators(deps);
		Chart chart = new Chart(10000,TimeFrame.MIN5);
		chart.parseFromFile(new File(dir));
		// chart plotter ist observer beim rolling frame chart und nimmt die informationen der indikatoren
		// (serien) waehrend sie berechenet werden auf, um dann spaeter fuer den
		// Graphischen chart dies informatioenn bereit zu stellen

		ChartDataCollector cc = new ChartDataCollector(ifact,chart);
		cc.calculate();

		// der graphische chart wird dargestellt:
		ChartJDialog cjd = new ChartJDialog(null,"testChart",false,cc);
		cjd.setVisible(true);

	}
}
