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
package de.smpfe.trader.indicators;

import java.awt.Color;
import java.lang.reflect.Method;

import de.smpfe.trader.data.IIntraPeriod;
import de.smpfe.trader.data.Plotable;
import de.smpfe.trader.data.indicator.AIndicator;
import de.smpfe.trader.data.indicator.IndicatorCandle;
import de.smpfe.trader.data.parameters.IParameter;
import de.smpfe.trader.data.parameters.IntParameter;

//Damit Klasse EMA alle Funktionen von Klasse AIndicator benutzen kann
//und damit Interface IIntraPeriod nutzbar ist.
public class EMA extends AIndicator implements IIntraPeriod {

	private static final int emaspan = 0;
	private double ema;

	// Damit man mit dieser Klasse Emas konstruieren kann
	// id gibt an welcher EMA gemeint ist.
	public EMA(int id, IndicatorCandle candle) {
		super(candle, id);
	}

	// Initialisierung, keine Vorgänger

	public EMA() {
		super();

		// Für die Userparameter, hier gibt es nur 2 Parameter,
		// die Anzahl der Perioden über die der Ema gehen soll
		IParameter[] parameters = new IParameter[1];
		parameters[emaspan] = new IntParameter("EmaSpan", 500, 1, 1, 50);

		setParameters(parameters);

		// macht Zeichnung
		try {
			Method foo = this.getClass().getMethod("getEma", null);
			Plotable[] plotables = { new Plotable(foo, Color.GREEN, true) };
			setPlotables(plotables);
		} catch (NoSuchMethodException nsme) {
		}

		setResetable();
	}

	// Funktion um an den Parameterwert zu kommen

	public int getEmaspan() {

		return (Integer) getParameterVal(emaspan);
	}

	public double getEma() {
		return ema;
	}

	// Funktion um auf EMA zuzugreifen
	// get(ndx) ruft Objekt von vor ndx Perioden auf und getEma()
	// ist die Funktion welche den Inhalt liefert, also den Wert von Ema
	// an der Stelle
	// "." links Objekt rechts Funktion
	public double getEma(int ndx) {
		return get(ndx).getEma();
	}

	// muss Zuverfügung gestellt werden erzwingt durch IntParameter
	@Override
	public void calculateIntraPeriod() {
		calculate();
	}

	// Hier kommen die eigentlichen Berechnungen rein
	@Override
	public void calculate() {
		// TODO: weight als globale Variable der Serie definieren
		double weight = 2.0 / (getEmaspan() + 1);

		// erste Periode
		if (candle.isFirst()) {
			ema = Close();
		} else {
			ema = weight * Close(1) + (1 - weight) * getEma(1);
		}

	}

	// Wenn neue Periode kommt muss ein neuer Ema angefertigt werden,
	// dafür muss vemrutlich der alte geclont werden
	@Override
	public AIndicator clone(IndicatorCandle c) {
		// TODO Auto-generated method stub
		EMA result = new EMA(id, c);
		return result;
	}

	@Override
	public EMA get(int index) {
		return (EMA) super.get(index);
	}

	@Override
	public void reset(IndicatorCandle candle) {
		super.reset(candle);
	}

}