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
package de.smpfe.trader.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeEventType;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.time.Day;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;

import de.smpfe.trader.data.chart.Candle;

public class ChartJDialog extends javax.swing.JDialog 
{/*
	public enum Interval {
        Daily,
        Weekly,
        Monthly
    }*/
    
    public enum Zoom {
        Days7,
        Month1,
        Months3,
        Months6,
        Year1,
        Year5,
        Year10,
        All
    }
    
    /** Creates new form ChartJDialog */
    public ChartJDialog(java.awt.Frame parent, String title, boolean modal, ChartDataCollector cc) {
        super(parent, title, modal);
                
        initComponents();

        // Must initialized first before any other operations. Our objective
        // is to show this chart as fast as possible. Hence, we will pass in
        // null first, till we sure what are we going to create.
        this.chartPanel = new ChartPanel(null, true, true, true, true, true);
        this.chartDatas = cc.getCandles();
        this.cp = cc;
        
        // Yellow box and chart resizing (#2969416)
        //
        // paradoxoff :
        // If the available size for a ChartPanel exceeds the dimensions defined
        // by the maximumDrawWidth and maximumDrawHeight attributes, the
        // ChartPanel will draw the chart in a Rectangle defined by the maximum
        // sizes and then scale it to fill the available size.
        // All you need to do is to avoid that scaling by using sufficiently
        // large values for the maximumDrawWidth and maximumDrawHeight, e. g.
        // by using the Dimension returned from
        // Toolkit.getDefaultToolkit().getScreenSize()
        // http://www.jfree.org/phpBB2/viewtopic.php?f=3&t=30059
        final Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        this.chartPanel.setMaximumDrawWidth((int)Math.round(dimension.getWidth()));
        this.chartPanel.setMaximumDrawHeight((int)Math.round(dimension.getHeight()));
 
        // Make chartPanel able to receive key event.
        // So that we may use arrow key to move around yellow information box.
        this.chartPanel.setFocusable(true);
        this.chartPanel.requestFocus();

        final org.jdesktop.jxlayer.JXLayer<ChartPanel> layer = new org.jdesktop.jxlayer.JXLayer<ChartPanel>(this.chartPanel);
        this.chartLayerUI = new ChartLayerUI<ChartPanel>(this);
        layer.setUI(this.chartLayerUI);

        priceOHLCDataset = getOHLCDataset(this.chartDatas);
        
        // Call after JXLayer has been initialized. changeType assumes JXLayer 
        // is ready.
        if (this.candlestickChart == null) {
            this.candlestickChart = this.createCandlestickChart(this.priceOHLCDataset);
        }
        
        
        ChartScrollBar csb = new ChartScrollBar(0,candlestickChart);//,priceOHLCDataset.);
        
        //chartPanel.set
        chartPanel.setChart(this.candlestickChart);
        // Make chartPanel able to receive key event.
        // So that we may use arrow key to move around yellow information box.
        chartPanel.requestFocus();            
        if (this.chartLayerUI != null) {
            this.chartLayerUI.updateTraceInfos();
        }
        
        //plotIndicatorsOnChart(cc);
        //plotIndicatorsSeperate(cc);
        plotIndicators();

        getContentPane().add(layer, java.awt.BorderLayout.CENTER);
        getContentPane().add(csb,java.awt.BorderLayout.SOUTH);
        
        //XYPlot plot = (XYPlot)candlestickChart.getPlot();//.get
        //plot.getRangeAxis().setAutoRange(true);
        // Handle resize.
        this.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e) {
                ChartJDialog.this.chartLayerUI.updateTraceInfos();
            }
        });
        
        
    
    }
    
    private void plotIndicators()
    {
    	HashMap<String,ArrayList<Double>> plots = cp.getPlots();

    	int k=1;
        for(String p: plots.keySet())
        {
        	if(cp.getOnChart().get(p))
        	{
        		plotIndicatorOnChart(plots.get(p).toArray(new Double[plots.get(p).size()]), p, k);
        		k++;
        	}
        		
        }
        
        plotTradeBubblesOnChart(cp.getEntryLong(),"entryLong",k++,0);

        plotTradeBubblesOnChart(cp.getEntryShort(),"entryShort",k++,1);

        plotTradeBubblesOnChart(cp.getConfirmEntry(),"confirmEntry",k++,2);

        plotTradeBubblesOnChart(cp.getExit(),"exit",k++,3);

        for(String p: plots.keySet())
        {
        	if(!cp.getOnChart().get(p))
        		plotIndicatorSeperate(plots.get(p).toArray(new Double[plots.get(p).size()]), p);
        }
        
        plotSeperatePosition(cp.getPosition());
        plotEquitySeperate(cp.getPosition(),cp.getEntryPrices(),cp.getExitPrices());
        
        applyChartTheme(this.candlestickChart);
        
    }
    
    private void plotTradeBubblesOnChart(ArrayList<Integer> toPlot, String p, int k, int j)
    {
    	final Plot main_plot = (Plot)((CombinedDomainXYPlot)this.candlestickChart.getPlot()).getSubplots().get(0);
        final XYPlot plot = (XYPlot) main_plot;
        
    	final TimeSeries series = new TimeSeries(p);
		///*
		for(Integer i: toPlot)
		{
			switch(j)
			{
			case 0:
				series.add(new Minute(new Date(chartDatas.get(i).getStartTimeStamp())),chartDatas.get(i).getOpen());
				break;
			case 1:
				series.add(new Minute(new Date(chartDatas.get(i).getStartTimeStamp())),chartDatas.get(i).getHigh());
				break;
			case 2:
				series.add(new Minute(new Date(chartDatas.get(i).getStartTimeStamp())),chartDatas.get(i).getLow());
				break;
			case 3:
				series.add(new Minute(new Date(chartDatas.get(i).getStartTimeStamp())),chartDatas.get(i).getClose());
				break;
			}
			
		}
		/*
		for (int i = 0; i < defaultHighLowDataset.getSeriesCount(); i++) 
		{
            series.add(new Minute(defaultHighLowDataset.getXDate(0, i)),plot[i]);
        }
		*/
		XYDataset dataSet = new TimeSeriesCollection(series);
		
		plot.setDataset(k, dataSet);
    	XYItemRenderer ir = new XYShapeRenderer();
    	//ir.s
    	
    	plot.setRenderer(k, ir);
    }
    
    private void plotIndicatorOnChart(Double[] toPlot, String p, int k)
    {
    	final Plot main_plot = (Plot)((CombinedDomainXYPlot)this.candlestickChart.getPlot()).getSubplots().get(0);
        final XYPlot plot = (XYPlot) main_plot;
    	
    	plot.setDataset(k, createIndicatorPlot(p,toPlot));
    	XYItemRenderer ir = new XYLineAndShapeRenderer(true,false);
        
        plot.setRenderer(k, ir);
    }
    
    private void plotSeperate(XYDataset dataset, String p)
    {
    	NumberAxis rangeAxis1 = new NumberAxis(p);
		rangeAxis1.setAutoRangeIncludesZero(false);     // override default
		rangeAxis1.setLowerMargin(0.40);                // to leave room for volume bars
		DecimalFormat format = new DecimalFormat("0");
		rangeAxis1.setNumberFormatOverride(format);
		
		final ValueAxis timeAxis = new DateAxis("Date");
		timeAxis.setLowerMargin(0.02);                  // reduce the default margins
		timeAxis.setUpperMargin(0.02);
		
		XYPlot plot = new XYPlot(dataset, timeAxis, rangeAxis1, null);
		
		XYItemRenderer renderer1 = new XYLineAndShapeRenderer(true, false);
		renderer1.setBaseToolTipGenerator(
		    new StandardXYToolTipGenerator(
		        StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
		        new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0.00#")
		    )
		);
		plot.setRenderer(0, renderer1);
		
		final CombinedDomainXYPlot cplot1 = (CombinedDomainXYPlot)this.candlestickChart.getPlot();
		if (plot != null) cplot1.add(plot, 1);      // weight is 1.
    }
    
    private void plotSeperatePosition(HashMap<Integer,Double> toPlot)
    {
    	final TimeSeries series = new TimeSeries("Position");
    	double curVal = 0.0;
    	for(int i=0;i<chartDatas.size();i++)
    	{
    		if(toPlot.get(i)!=null)
    		{
    			curVal=toPlot.get(i);
    		}
    		series.add(new Minute(new Date(chartDatas.get(i).getStartTimeStamp())),curVal);
    	}
    	
    	plotSeperate(new TimeSeriesCollection(series),"Position");
    }
    
    private void plotEquitySeperate(HashMap<Integer,Double> position,HashMap<Integer,Integer> entryPrices,HashMap<Integer,Integer> exitPrices)
    {
    	final TimeSeries series = new TimeSeries("Equity");
    	double curVal = 0.0;
    	boolean inMarket = false;
    	int refPrice = 0;
    	int equity = 0;
    	//TODO exit entry exit
    	for(int i=0;i<chartDatas.size();i++)
    	{
    		if(curVal!=0)
    		{
    			if(exitPrices.get(i)!=null)
	    		{
	    			equity +=(exitPrices.get(i)-refPrice)*(curVal>0?1:-1);
					inMarket = false;
	    		}
    			if(entryPrices.get(i)!=null)
	    		{
	    				refPrice = entryPrices.get(i);
	    				inMarket = true;
	    		}
    		}
    		else
    		{
    			if(entryPrices.get(i)!=null)
	    		{
	    				refPrice = entryPrices.get(i);
	    				inMarket = true;
	    		}
	    		if(exitPrices.get(i)!=null)
	    		{
	    			equity +=(exitPrices.get(i)-refPrice)*(curVal>0?1:-1);
					inMarket = false;
	    		}
    		}
    		
    		if(position.get(i)!=null)
    		{
    			curVal=position.get(i);
    		}
    		
    		//equity berechnung
    		if(inMarket)
    			equity +=(chartDatas.get(i).getClose()-refPrice)*(curVal>0?1:-1);
    		
    		refPrice = chartDatas.get(i).getClose();
    		
    		series.add(new Minute(new Date(chartDatas.get(i).getStartTimeStamp())),equity);
    	}
    	
    	plotSeperate(new TimeSeriesCollection(series),"Equity");
    }
    
	private void plotIndicatorSeperate(Double[] toPlot, String p) 
	{
		XYDataset dataset = createIndicatorPlot(p,toPlot);
		plotSeperate(dataset,p);
	}
	
	private JFreeChart createCandlestickChart(OHLCDataset priceOHLCDataset) {
        final String title = "Chart";
        
        final ValueAxis timeAxis = new DateAxis("Date");
        final NumberAxis valueAxis = new NumberAxis("Price");
        valueAxis.setAutoRangeIncludesZero(false);
        valueAxis.setUpperMargin(0.0);
        valueAxis.setLowerMargin(0.0);
        XYPlot plot = new XYPlot(priceOHLCDataset, timeAxis, valueAxis, null);

        final CandlestickRenderer candlestickRenderer = new CandlestickRenderer();
        plot.setRenderer(candlestickRenderer);
        //plot.getRangeAxis().setAutoRange(true);
        
        
        // Give good width when zoom in, but too slow in calculation.
        ((CandlestickRenderer)plot.getRenderer()).setAutoWidthMethod(CandlestickRenderer.WIDTHMETHOD_SMALLEST);

        CombinedDomainXYPlot cplot = new CombinedDomainXYPlot(timeAxis);
        cplot.add(plot, 3);
        cplot.setGap(8.0);

        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, cplot, true);

        applyChartTheme(chart);

        // Handle zooming event.
        chart.addChangeListener(this.getChartChangeListner());

        return chart;        
    }

	private OHLCDataset getOHLCDataset(List<Candle> chartDatas) {

        final int size = chartDatas.size();
        
        Date[] date = new Date[size];
        double[] high = new double[size];
        double[] low = new double[size];
        double[] open = new double[size];
        double[] close = new double[size];
        double[] volume = new double[size];

        int i = 0;
        for(Candle chartData : chartDatas) {
            date[i] = new Date(chartData.getStartTimeStamp());
            high[i] = chartData.getHigh();
            low[i] = chartData.getLow();
            open[i] = chartData.getOpen();
            close[i] = chartData.getClose();
            volume[i] = chartData.getVolume();
            i++;
        }
        
        return new DefaultHighLowDataset("Price", date, high, low, open, close, volume);
    }
	
	private XYDataset createIndicatorPlot(String name, Double[] plot)
	{
		final TimeSeries series = new TimeSeries(name);
		///*
		for (int i = 0; i < chartDatas.size(); i++) 
		{
            series.add(new Minute(new Date(chartDatas.get(i).getStartTimeStamp())),plot[i]);
        }
		/*
		for (int i = 0; i < defaultHighLowDataset.getSeriesCount(); i++) 
		{
            series.add(new Minute(defaultHighLowDataset.getXDate(0, i)),plot[i]);
        }
		*/
        return new TimeSeriesCollection(series);
	}

    /**
     * Calculate and update high low value labels, according to current displayed
     * time range. This is a time consuming method, and shall be called by
     * user thread.
     */
    private void _updateHighLowJLabels() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ChartJDialog.this.jLabel2.setText("");
                ChartJDialog.this.jLabel4.setText("");
            }
        });

        final ValueAxis valueAxis = this.getPlot().getDomainAxis();
        final Range range = valueAxis.getRange();
        final long lowerBound = (long)range.getLowerBound();
        final long upperBound = (long)range.getUpperBound();
        final DefaultHighLowDataset defaultHighLowDataset = (DefaultHighLowDataset)this.priceOHLCDataset;

        // Perform binary search, to located day in price time series, which
        // is equal or lesser than upperBound.
        int low = 0;
        int high = defaultHighLowDataset.getItemCount(0) - 1;
        long best_dist = Long.MAX_VALUE;
        int best_mid = -1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            long v = defaultHighLowDataset.getXDate(0, mid).getTime();

            if (v > upperBound) {
                high = mid - 1;
            }
            else if (v < upperBound) {
                low = mid + 1;
                long dist = upperBound - v;
                if (dist < best_dist) {
                    best_dist = dist;
                    best_mid = mid;
                }
            }
            else {
                best_dist = 0;
                best_mid = mid;
                break;
            }
        }

        if (best_mid < 0) {
            return;
        }

        double high_last_price = Double.NEGATIVE_INFINITY;
        double low_last_price = Double.MAX_VALUE;
        for (int i = best_mid; i >= 0; i--) {
            final long time = defaultHighLowDataset.getXDate(0, i).getTime();
            if (time < lowerBound) {
                break;
            }
            if (high_last_price < defaultHighLowDataset.getHighValue(0, i)) {
                high_last_price = defaultHighLowDataset.getHighValue(0, i);
            }
            if (low_last_price > defaultHighLowDataset.getLowValue(0, i)) {
                low_last_price = defaultHighLowDataset.getLowValue(0, i);
            }
        }

        final double h = high_last_price;
        final double l = low_last_price;
        if (high_last_price >= low_last_price) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ChartJDialog.this.jLabel2.setText(stockPriceDecimalFormat(h));
                    ChartJDialog.this.jLabel4.setText(stockPriceDecimalFormat(l));
                }
            });
        }
    }

    /**
     * Reset all day labels back to plain font.
     */
    private void resetAllDayLabels() {
        JLabel[] labels = {jLabel9, jLabel10, jLabel11, jLabel12, jLabel13, jLabel14, jLabel5, jLabel6};
        for (JLabel label : labels) {
            final Font oldFont = label.getFont();
            // Reset BOLD attribute.
            final Font font = oldFont.deriveFont(oldFont.getStyle() & ~Font.BOLD);
            label.setFont(font);
        }
    }

    /**
    
	
	/*
	 * Calculate and update high low value labels, according to current displayed
     * time range. This method will return immediately, as the calculating and
     * updating task by performed by user thread.
     */
    private void updateHighLowJLabels() {
        updateHighLowLabelsPool.execute(new Runnable() {
            @Override
            public void run() {
                ChartJDialog.this._updateHighLowJLabels();
            }
        });
    }

    /**
     * Zoom in to this chart according to zoom information, and update day labels
     * as well.
     * @param zoom zoom information
     */
    private void zoom(Zoom z) {
        switch(z) {
        case Days7:
            this._zoom(Calendar.DATE, -7);
            /* Reset first. */
            this.resetAllDayLabels();
            this.jLabel9.setFont(getBoldFont(this.jLabel9.getFont()));
            break;

        case Month1:
            this._zoom(Calendar.MONTH, -1);
            /* Reset first. */
            this.resetAllDayLabels();
            this.jLabel10.setFont(getBoldFont(this.jLabel10.getFont()));
            break;

        case Months3:
            this._zoom(Calendar.MONTH, -3);
            /* Reset first. */
            this.resetAllDayLabels();
            this.jLabel11.setFont(getBoldFont(this.jLabel11.getFont()));
            break;

        case Months6:
            this._zoom(Calendar.MONTH, -6);
            /* Reset first. */
            this.resetAllDayLabels();
            this.jLabel12.setFont(getBoldFont(this.jLabel12.getFont()));
            break;

        case Year1:
            this._zoom(Calendar.YEAR, -1);
            /* Reset first. */
            this.resetAllDayLabels();
            this.jLabel13.setFont(getBoldFont(this.jLabel13.getFont()));
            break;

        case Year5:
            this._zoom(Calendar.YEAR, -5);
            /* Reset first. */
            this.resetAllDayLabels();
            this.jLabel5.setFont(getBoldFont(this.jLabel5.getFont()));
            break;

        case Year10:
            this._zoom(Calendar.YEAR, -10);
            /* Reset first. */
            this.resetAllDayLabels();
            this.jLabel6.setFont(getBoldFont(this.jLabel6.getFont()));
            break;

        case All:
            this.chartPanel.restoreAutoBounds();
            /* Reset first. */
            this.resetAllDayLabels();
            /* Bold the target. */
            this.jLabel14.setFont(getBoldFont(this.jLabel14.getFont()));
            break;
        }
    }

    public static Font getBoldFont(Font font) {
        return font.deriveFont(font.getStyle() | Font.BOLD);
    }
    
    /**
     * Zoom in to this chart with specific amount of time.
     * @param field the calendar field.
     * @param amount the amount of date or time to be added to the field.
     */
    private void _zoom(int field, int amount) {
        this.chartPanel.restoreAutoBounds();
        
        final DefaultHighLowDataset defaultHighLowDataset = (DefaultHighLowDataset)this.priceOHLCDataset;

        final int itemCount = priceOHLCDataset.getItemCount(0);//chartDatas.size();
        final Date date = defaultHighLowDataset.getXDate(0, itemCount-1);//chartDatas.get(itemCount-1).getStartTime();
        // Candle stick takes up half day space.
        // Volume price chart's volume information takes up whole day space.
        // TODO convert to end time
        final long end = date.getTime();
        final Calendar calendar = Calendar.getInstance();
        // -1. Calendar's month is 0 based but JFreeChart's month is 1 based.
        calendar.setTime(date);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(field, amount);
        // Candle stick takes up half day space.
        // Volume price chart's volume information does not take up any space.
        final long start = Math.max(0, calendar.getTimeInMillis() - (1000 * 60 * 60 * 12));
        final ValueAxis valueAxis = this.getPlot().getDomainAxis();

        if (itemCount > 0) {
            if (start <  defaultHighLowDataset.getXDate(0, 0).getTime()) {
                // To prevent zoom-out too much.
                // This happens when user demands for 10 years zoom, where we
                // are only having 5 years data.
                return;
            }
        }
        
        valueAxis.setRange(start, end);

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double max_volume = Double.MIN_VALUE;
        for (int i = itemCount - 1; i >= 0; i--) {
            final long d = defaultHighLowDataset.getXDate(0, itemCount-1).getTime();
            if (d < start) {
                break;
            }
            final double high = defaultHighLowDataset.getHighValue(0, i);
            final double low = defaultHighLowDataset.getLowValue(0, i);
            final double volume = defaultHighLowDataset.getVolumeValue(0, i);

            if (max < high) {
                max = high;
            }
            if (min > low) {
                min = low;
            }
            if (max_volume < volume) {
                max_volume = volume;
            }
        }

        if (min > max) {
            return;
        }

        final ValueAxis rangeAxis = this.getPlot().getRangeAxis();
        final Range rangeAxisRange = rangeAxis.getRange();
        // Increase each side by 1%
        double tolerance = 0.01 * (max - min);
        // The tolerance must within range [0.01, 1.0]
        tolerance = Math.min(Math.max(0.01, tolerance), 1.0);
        // The range must within the original chart range.
        min = Math.max(rangeAxisRange.getLowerBound(), min - tolerance);
        max = Math.min(rangeAxisRange.getUpperBound(), max + tolerance);

        this.getPlot().getRangeAxis().setRange(min, max);

        if (this.getPlot().getRangeAxisCount() > 1) {
            final double volumeUpperBound = this.getPlot().getRangeAxis(1).getRange().getUpperBound();
            final double suggestedVolumneUpperBound = max_volume * 4;
            // To prevent over zoom-in.
            if (suggestedVolumneUpperBound < volumeUpperBound) {
                this.getPlot().getRangeAxis(1).setRange(0, suggestedVolumneUpperBound);
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel4 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel9 = new HyperlinkLikedJLabel();
        jLabel10 = new HyperlinkLikedJLabel();
        jLabel11 = new HyperlinkLikedJLabel();
        jLabel12 = new HyperlinkLikedJLabel();
        jLabel13 = new HyperlinkLikedJLabel();
        jLabel5 = new HyperlinkLikedJLabel();
        jLabel6 = new HyperlinkLikedJLabel();
        jLabel14 = new HyperlinkLikedJLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.BorderLayout(5, 5));

        jPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 2, 5));
        jPanel4.setLayout(new java.awt.BorderLayout(5, 5));

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 160));
        jPanel2.setPreferredSize(new java.awt.Dimension(120, 33));

        jPanel1.setLayout(new java.awt.GridLayout(2, 1, 0, 5));

        jPanel3.setLayout(new java.awt.GridLayout(2, 1, 0, 5));

        jLabel2.setFont(jLabel2.getFont().deriveFont(jLabel2.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel2.setForeground(new java.awt.Color(50, 50, 0));
        jPanel3.add(jLabel2);

        jLabel4.setFont(jLabel4.getFont().deriveFont(jLabel4.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel4.setForeground(new java.awt.Color(200, 0, 50));
        jPanel3.add(jLabel4);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.add(jPanel2, java.awt.BorderLayout.WEST);

        jPanel4.add(jPanel5, java.awt.BorderLayout.EAST);

        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 10));

        jLabel9.setText("7d"); // NOI18N
        jLabel9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel9MouseClicked(evt);
            }
        });
        jPanel6.add(jLabel9);

        jLabel10.setText("1m"); // NOI18N
        jLabel10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel10MouseClicked(evt);
            }
        });
        jPanel6.add(jLabel10);

        jLabel11.setText("3m"); // NOI18N
        jLabel11.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel11MouseClicked(evt);
            }
        });
        jPanel6.add(jLabel11);

        jLabel12.setText("6m"); // NOI18N
        jLabel12.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel12MouseClicked(evt);
            }
        });
        jPanel6.add(jLabel12);

        jLabel13.setText("1y"); // NOI18N
        jLabel13.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel13MouseClicked(evt);
            }
        });
        jPanel6.add(jLabel13);

        jLabel5.setText("5y"); // NOI18N
        jLabel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel5MouseClicked(evt);
            }
        });
        jPanel6.add(jLabel5);

        jLabel6.setText("10y"); // NOI18N
        jLabel6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel6MouseClicked(evt);
            }
        });
        jPanel6.add(jLabel6);

        jLabel14.setText("all"); // NOI18N
        jLabel14.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel14MouseClicked(evt);
            }
        });
        jPanel6.add(jLabel14);

        jPanel4.add(jPanel6, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel4, java.awt.BorderLayout.SOUTH);

        jMenu2.setText("Placeholder"); // NOI18N
        jMenu2.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
            }
        });
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-750)/2, (screenSize.height-600)/2, 750, 600);
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
    }//GEN-LAST:event_jMenuItem1ActionPerformed
    
    private void jMenu2MenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_jMenu2MenuSelected
    }//GEN-LAST:event_jMenu2MenuSelected

    private void jLabel9MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel9MouseClicked
        this.zoom(Zoom.Days7);
    }//GEN-LAST:event_jLabel9MouseClicked

    private void jLabel10MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel10MouseClicked
        this.zoom(Zoom.Month1);
    }//GEN-LAST:event_jLabel10MouseClicked

    private void jLabel11MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel11MouseClicked
        this.zoom(Zoom.Months3);
    }//GEN-LAST:event_jLabel11MouseClicked

    private void jLabel12MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel12MouseClicked
        this.zoom(Zoom.Months6);
    }//GEN-LAST:event_jLabel12MouseClicked

    private void jLabel13MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel13MouseClicked
        this.zoom(Zoom.Year1);
    }//GEN-LAST:event_jLabel13MouseClicked
    
    private void jLabel5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseClicked
        this.zoom(Zoom.Year5);
    }//GEN-LAST:event_jLabel5MouseClicked

    private void jLabel6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MouseClicked
        this.zoom(Zoom.Year10);
    }//GEN-LAST:event_jLabel6MouseClicked

    private void jLabel14MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel14MouseClicked
        this.zoom(Zoom.All);
    }//GEN-LAST:event_jLabel14MouseClicked
    
	
	public List<Candle> getChartDatas() 
	{
        return Collections.unmodifiableList(chartDatas);
    }

    public ChartPanel getChartPanel() 
    {
    	return this.chartPanel;
    }
	
	private ChartChangeListener getChartChangeListner() {
        return new ChartChangeListener() {
            @Override
            public void chartChanged(ChartChangeEvent event) {
                // Is weird. This works well for zoom-in. When we add new CCI or
                // RIS. This event function will be triggered too. However, the
                // returned draw area will always be the old draw area, unless
                // you move your move over.
                // Even I try to capture event.getType() == ChartChangeEventType.NEW_DATASET
                // also doesn't work.
                if (event.getType() == ChartChangeEventType.GENERAL) {
                    ChartJDialog.this.chartLayerUI.updateTraceInfos();
                    // Re-calculating high low value.
                    ChartJDialog.this.updateHighLowJLabels();
                }
            }
        };
    }
	
	/*
	 * Returns the current main plot of this chart dialog.
     *
     * @return the current main plot of this chart dialog
     */
    public XYPlot getPlot() {
        final JFreeChart chart = this.chartPanel.getChart();
        final CombinedDomainXYPlot cplot = (CombinedDomainXYPlot) chart.getPlot();
        final XYPlot plot = (XYPlot) cplot.getSubplots().get(0);
        return plot;
    }

    /**
     * Returns the plot at the specified position in this chart dialog.
     *
     * @param index index of the plot to return
     * @return the plot at the specified position in this chart dialog
     */
    public XYPlot getPlot(int index) {
        final JFreeChart chart = this.chartPanel.getChart();
        final CombinedDomainXYPlot cplot = (CombinedDomainXYPlot) chart.getPlot();
        final XYPlot plot = (XYPlot) cplot.getSubplots().get(index);
        return plot;
    }

    /**
     * Returns the number of plots in this chart dialog.
     *
     * @return the number of plots in this chart dialog
     */
    public int getPlotSize() {
    	final JFreeChart chart = this.chartPanel.getChart();
        final CombinedDomainXYPlot cplot = (CombinedDomainXYPlot) chart.getPlot();
        return cplot.getSubplots().size();
    }
	
    	
    private List<Candle> chartDatas;
    private ChartDataCollector cp;

    private final ChartPanel chartPanel;
	
    private OHLCDataset priceOHLCDataset;
    private JFreeChart candlestickChart;
    
    /*
    * Thread pool, used to hold threads to update high low labels.
    */
   private final Executor updateHighLowLabelsPool = Executors.newFixedThreadPool(1);

   /* Overlay layer. */
   private final ChartLayerUI<ChartPanel> chartLayerUI;
   
// Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JLabel jLabel10;
   private javax.swing.JLabel jLabel11;
   private javax.swing.JLabel jLabel12;
   private javax.swing.JLabel jLabel13;
   private javax.swing.JLabel jLabel14;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabel4;
   private javax.swing.JLabel jLabel5;
   private javax.swing.JLabel jLabel6;
   private javax.swing.JLabel jLabel9;
   private javax.swing.JMenu	jMenu2;
   private javax.swing.JMenuBar jMenuBar1;
   private javax.swing.JPanel jPanel1;
   private javax.swing.JPanel jPanel2;
   private javax.swing.JPanel jPanel3;
   private javax.swing.JPanel jPanel4;
   private javax.swing.JPanel jPanel5;
   private javax.swing.JPanel jPanel6;
   // End of variables declaration//GEN-END:variables
   
   
   
	
	/**
     * Applying chart theme based on given JFreeChart.
     *
     * @param chart the JFreeChart
     */
    public static void applyChartTheme(JFreeChart chart) {
        final StandardChartTheme chartTheme = (StandardChartTheme)org.jfree.chart.StandardChartTheme.createJFreeTheme();
        chartTheme.setXYBarPainter(barPainter);
        chartTheme.setShadowVisible(false);
        chartTheme.setPlotBackgroundPaint(Color.WHITE);
        chartTheme.setDomainGridlinePaint(Color.LIGHT_GRAY);
        chartTheme.setRangeGridlinePaint(Color.LIGHT_GRAY);
        chartTheme.setPlotOutlinePaint(Color.LIGHT_GRAY);

        if (chart.getPlot() instanceof CombinedDomainXYPlot) {
            @SuppressWarnings("unchecked")
            List<Plot> plots = ((CombinedDomainXYPlot)chart.getPlot()).getSubplots();
            for (Plot plot : plots) {
                final int domainAxisCount = ((XYPlot)plot).getDomainAxisCount();
                final int rangeAxisCount = ((XYPlot)plot).getRangeAxisCount();
                for (int i = 0; i < domainAxisCount; i++) {
                    ((XYPlot)plot).getDomainAxis(i).setAxisLinePaint(Color.LIGHT_GRAY);
                    ((XYPlot)plot).getDomainAxis(i).setTickMarkPaint(Color.LIGHT_GRAY);
                }
                for (int i = 0; i < rangeAxisCount; i++) {
                    ((XYPlot)plot).getRangeAxis(i).setAxisLinePaint(Color.LIGHT_GRAY);
                    ((XYPlot)plot).getRangeAxis(i).setTickMarkPaint(Color.LIGHT_GRAY);
                }
            }
        }
        else {
            final Plot plot = chart.getPlot();
            if (plot instanceof XYPlot) {            
                final org.jfree.chart.plot.XYPlot xyPlot = (org.jfree.chart.plot.XYPlot)plot;
                final int domainAxisCount = xyPlot.getDomainAxisCount();
                final int rangeAxisCount = xyPlot.getRangeAxisCount();
                for (int i = 0; i < domainAxisCount; i++) {
                    xyPlot.getDomainAxis(i).setAxisLinePaint(Color.LIGHT_GRAY);
                    xyPlot.getDomainAxis(i).setTickMarkPaint(Color.LIGHT_GRAY);
                }
                for (int i = 0; i < rangeAxisCount; i++) {
                    xyPlot.getRangeAxis(i).setAxisLinePaint(Color.LIGHT_GRAY);
                    xyPlot.getRangeAxis(i).setTickMarkPaint(Color.LIGHT_GRAY);
                }                
            }
            //else if (plot instanceof org.jfree.chart.plot.PiePlot) {
            //    final org.jfree.chart.plot.PiePlot piePlot = (org.jfree.chart.plot.PiePlot)plot;
            //    
            //}
        }

        chartTheme.apply(chart);
    }
    
    private static final org.jfree.chart.renderer.xy.StandardXYBarPainter barPainter = new org.jfree.chart.renderer.xy.StandardXYBarPainter();
    
    public static String stockPriceDecimalFormat(Object value) {
        // 0.1   -> "0.10"
        // 0.01  -> "0.01"
        // 0.001 -> "0.001"
        final DecimalFormat decimalFormat = new DecimalFormat("0.00#");
        return decimalFormat.format(value);
    }

    public static String stockPriceDecimalFormat(double value) {
        // 0.1   -> "0.10"
        // 0.01  -> "0.01"
        // 0.001 -> "0.001"
        DecimalFormat decimalFormat = new DecimalFormat("0.00#");
        return decimalFormat.format(value);
    }

	public ChartDataCollector getCPlotter() {
		return cp;
	}
}
