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
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import de.smpfe.trader.data.Dependency;
import de.smpfe.trader.data.Plotable;
import de.smpfe.trader.data.chart.Chart;
import de.smpfe.trader.data.indicator.AIndicator;
import de.smpfe.trader.data.indicator.IndicatorCandle;
import de.smpfe.trader.data.indicator.RollingFrameChart;
import de.smpfe.trader.enums.TimeFrame;
import de.smpfe.trader.factories.IndicatorFactory;
import de.smpfe.trader.factories.TreeParser;
import de.smpfe.trader.indicators.MA;

public class Starter implements Observer
{

	public static void main(String[] args) throws Exception
	{
		String dir = "files//eurusd.intra";

		IndicatorFactory ifact = new IndicatorFactory();

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

		// Aufruf der Factory
		ifact.loadIndicators(deps);
		// Alternative 1
		//ifact.loadIndicators(new String[]{loadindi[0],loadindi[1]});// oder
		//Alternative 2 geht auch, da ATR von RenkoSAR abh�ngig
		//ifact.loadIndicators(new String[]{loadindi[0]});
		TreeParser tp = new TreeParser(ifact.getRootNodes()[1]);
		
		tp.getNode(MA.class.getName()).getParameter("MASpan").setVal(50);

		// load indiactors laedt parameter und abhaengigkeiten der einezlenen indicatoren
		// in eine datenstrucktur, von der aus man sie aendern kann (GUI)

		// fire indikators benutzt diese datenstrucktur und generiert einen array von
		// echten indikatoren, die dann auf den chart geladen werden koennen,
		// sortiert nach dem element das keine abhaengigkeiten hat, bis zu dem element das
		// am meisten abhaengikeiten hat
		AIndicator[] aindis = ifact.finalizeIndis();
		//indis[0] = new ATR();
		//indis[1] = new RenkoSAR();
		//indis[1].setDependentIndicators(indis);

		// Parameter k�nnen noch nachtr�glich ge�ndert werden
		// nicht empfohlen, nur fuer schnelle tests!
		// aindis.get(0).getParameters()[0].setVal(0);

		// l�dt den Chart
		Chart chart = new Chart(10000,TimeFrame.MIN5);
		chart.parseFromFile(new File(dir));

		// inizialisiert den RollingFrameChart
		RollingFrameChart rcchart = new RollingFrameChart(chart.getAchart().get(0).getTimeFrame(), aindis);

		Starter starter = new Starter();
		// add main als chartlistner
		rcchart.addObserver(starter);

		// berechne die indikatoren auf den basic candles
		chart.addObserver(rcchart);

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
