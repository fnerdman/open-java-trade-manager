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

package de.smpfe.trader.data.chart;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Observable;
import java.util.StringTokenizer;

import de.smpfe.trader.enums.TimeFrame;

public class Chart extends Observable{
	private ArrayList<Candle> achart;
	
	private int tickfactor;  // Bedeutung: Skalierungsfaktor f�r Preise,
							 // damit alle Modellpreise Integer werden
	private TimeFrame timeframe;   // z.B. MIN5
	
	
	// constructor
	public Chart( int tickfactor, TimeFrame timeframe) {
		this.tickfactor = tickfactor;
		this.timeframe = timeframe;
		achart = new ArrayList<Candle>();
	}
	
	public void runThroughChart()
	{
		PriceElement pe;
		for(Candle c: achart)
		{   // vorgegebene Reihenfolge: Open, High, Low, Close!!
			pe = new PriceElement(c.getOpen(),c.getVolume(),1,c.getStartTimeStamp());
			setChanged();
			notifyObservers(pe);
			pe = new PriceElement(c.getHigh(),c.getVolume(),1,c.getStartTimeStamp());
			setChanged();
			notifyObservers(pe);
			pe = new PriceElement(c.getLow(),c.getVolume(),1,c.getStartTimeStamp());
			setChanged();
			notifyObservers(pe);
			pe = new PriceElement(c.getClose(),c.getVolume(),1,c.getStartTimeStamp());
			setChanged();
			notifyObservers(pe);
		}
	}
	
	// methods
	public int  length() {
		// number candels in chart
		return achart.size();
	}
	
	public void add(Candle c) {
		achart.add(c);
	}
	
	public String printchart(){
		String res = new String();
		for (Candle c: achart){
			res += "\n" + c.toString();
		}
		return res;
		
	}
	
	public void parseFromFile(File selectedDataFile)
	{
		Candle candle;
		BufferedReader br = null;
		String line = "";
		int ndx = 0;
		try {
			br = new BufferedReader(new FileReader(selectedDataFile));
			line = br.readLine();
			do {
				line = br.readLine();
				if (line != null && line.length() > 0) {
					
					StringTokenizer st = new StringTokenizer(line, ";");
					
					String datum = st.nextToken();
					String uhrzeit = st.nextToken();
					
					SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy HH:mm");

					candle = new Candle(
								(int)Math.round(Double.parseDouble(st.nextToken())*tickfactor), //Open
								timeframe,
								sdf.parse(datum + " " + uhrzeit).getTime(),
								ndx++
								);
					
					candle.setClose((int)Math.round(Double.parseDouble(st.nextToken())*tickfactor));
					candle.setHigh((int)Math.round(Double.parseDouble(st.nextToken())*tickfactor));
					candle.setLow((int)Math.round(Double.parseDouble(st.nextToken())*tickfactor));
					candle.setVolume(Integer.parseInt(st.nextToken()));
					
					
					achart.add(candle);
				}
			} while (line != null && line.length() > 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			br.close();
		} catch (IOException e) {
		}
	}

	public void parseFromFileDay(File selectedDataFile)
	{
		Candle candle;
		BufferedReader br = null;
		String line = "";
		int ndx = 0;
		try {
			br = new BufferedReader(new FileReader(selectedDataFile));
			line = br.readLine();
			do {
				line = br.readLine();
				if (line != null && line.length() > 0) {
					
					StringTokenizer st = new StringTokenizer(line, ";");
					
					String datum = st.nextToken();
					//String uhrzeit = st.nextToken();
					
					SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy");

					candle = new Candle(
								(int)Math.round(Double.parseDouble(st.nextToken())*tickfactor), //Open
								timeframe,
								sdf.parse(datum).getTime(),
								ndx++
								);
					
					candle.setClose((int)Math.round(Double.parseDouble(st.nextToken())*tickfactor));
					candle.setHigh((int)Math.round(Double.parseDouble(st.nextToken())*tickfactor));
					candle.setLow((int)Math.round(Double.parseDouble(st.nextToken())*tickfactor));
					candle.setVolume(Integer.parseInt(st.nextToken()));
					
					
					achart.add(candle);
				}
			} while (line != null && line.length() > 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			br.close();
		} catch (IOException e) {
		}
	}
	
	
	
	// getter und setter
	public ArrayList<Candle> getAchart() {
		return achart;
	}

	public void setAchart(ArrayList<Candle> achart) {
		this.achart = achart;
	}

	public int getTickfactor() {
		return tickfactor;
	}

	public void setTickfactor(int tickfactor) {
		this.tickfactor = tickfactor;
	}

	public TimeFrame getTimeframe() {
		return timeframe;
	}

	public void setTimeframe(TimeFrame timeframe) {
		this.timeframe = timeframe;
	}
	
	// Zugriff auf high, low, open, close, volume
	
	// open
	public int  open(int index) throws IndexOutOfBoundsException {
		// Open[index] aus Express: decending index
		int foo;
		
		if ((index <= length()-1) && (index >= 0))
		    foo = achart.get(length()-1-index).getOpen();
		else 
		{
			foo = 0;
			throw new IndexOutOfBoundsException("Chart wurde mit falschem index aufgerufen");
		}
		return foo;
	}
	
	public int  openAsc(int index) {
		// Open(index) aus Plugin;  index ascending
		int foo;
		
		if ((index <= length()-1) && (index >= 0))
		    foo = achart.get(index).getOpen();
		else 
		{
			foo = 0;
			throw new IndexOutOfBoundsException("Chart wurde mit falschem index aufgerufen");
		}
		return foo;
	}
	
	// close
		public int  close(int index) throws IndexOutOfBoundsException {
			// Close[index] aus Express: decending index
			int foo;
			
			if ((index <= length()-1) && (index >= 0))
			    foo = achart.get(length()-1-index).getClose();
			else 
			{
				foo = 0;
				throw new IndexOutOfBoundsException("Chart wurde mit falschem index aufgerufen");
			}
			return foo;
		}
		
		public int  closeAsc(int index) {
			// Close(index) aus Plugin
			int foo;
			
			if ((index <= length()-1) && (index >= 0))
			    foo = achart.get(index).getClose();
			else 
			{
				foo = 0;
				throw new IndexOutOfBoundsException("Chart wurde mit falschem index aufgerufen");
			}
			return foo;
		}
		
		// high
		public int  high(int index) throws IndexOutOfBoundsException {
			// High[index] aus Express: decending index
			int foo;
			
			if ((index <= length()-1) && (index >= 0))
			    foo = achart.get(length()-1-index).getHigh();
			else 
			{
				foo = 0;
				throw new IndexOutOfBoundsException("Chart wurde mit falschem index aufgerufen");
			}
			return foo;
		}
		
		public int  highAsc(int index) {
			// High(index) aus Plugin
			int foo;
			
			if ((index <= length()-1) && (index >= 0))
			    foo = achart.get(index).getHigh();
			else 
			{
				foo = 0;
				throw new IndexOutOfBoundsException("Chart wurde mit falschem index aufgerufen");
			}
			return foo;
		}
		
		// low
		public int  low(int index) throws IndexOutOfBoundsException {
			// Low[index] aus Express: decending index
			int foo;
			
			if ((index <= length()-1) && (index >= 0))
			    foo = achart.get(length()-1-index).getLow();
			else 
			{
				foo = 0;
				throw new IndexOutOfBoundsException("Chart wurde mit falschem index aufgerufen");
			}
			return foo;
		}
		
		public int  lowAsc(int index) {
			// Low(index) aus Plugin
			int foo;
			
			if ((index <= length()-1) && (index >= 0))
			    foo = achart.get(index).getLow();
			else 
			{
				foo = 0;
				throw new IndexOutOfBoundsException("Chart wurde mit falschem index aufgerufen");
			}
			return foo;
		}
		
		// volume
		public int  volume(int index) throws IndexOutOfBoundsException {
			// Volume[index] aus Express: decending index
			int foo;
			
			if ((index <= length()-1) && (index >= 0))
			    foo = achart.get(length()-1-index).getVolume();
			else 
			{
				foo = 0;
				throw new IndexOutOfBoundsException("Chart wurde mit falschem index aufgerufen");
			}
			return foo;
		}
		
		public int  volumeAsc(int index) {
			// Volume(index) aus Plugin
			int foo;
			
			if ((index <= length()-1) && (index >= 0))
			    foo = achart.get(index).getVolume();
			else 
			{
				foo = 0;
				throw new IndexOutOfBoundsException("Chart wurde mit falschem index aufgerufen");
			}
			return foo;
		}
	

}
