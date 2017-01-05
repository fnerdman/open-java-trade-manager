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
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.event.*;
import org.jfree.chart.plot.*;
import org.jfree.data.*;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataset;

public class ChartScrollBar extends JScrollBar implements AdjustmentListener, AxisChangeListener, MouseListener, DatasetChangeListener
{
   private JFreeChart chart;
   private XYPlot plot;
   private double ratio;
   private boolean updating = false;
   
   public ChartScrollBar(int orientation, JFreeChart chart)
   {
      this(orientation, chart, null);
   }
   
   public ChartScrollBar(int orientation, JFreeChart chart, XYPlot plot)
   {
      super(orientation);
      this.chart = chart;
      if (plot == null)
         this.plot = chart.getXYPlot();
      else
         this.plot = plot;
      if (getXYPlot() != null && getValueAxis() != null)
      {
         getValueAxis().addChangeListener(this);
         addAdjustmentListener(this);
         if (getXYPlot().getDataset() != null)
            getXYPlot().getDataset().addChangeListener(this);
         axisUpdate();
         addMouseListener(this);
      }
   }

   public XYPlot getXYPlot()
   {
      return plot;
   }
   
   public ValueAxis getValueAxis()
   {
      if (orientation == VERTICAL)
         return (ValueAxis)getXYPlot().getRangeAxis();
      return (ValueAxis)getXYPlot().getDomainAxis();
   }
   
   public Dataset getDataset()
   {
      return getXYPlot().getDataset();
   }
   
   public Range getDataRange()
   {
      return getXYPlot().getDataRange(getValueAxis());
   }
   
   public double getDataMinimum()
   {
      return getDataRange().getLowerBound();
   }
   
   public double getDataMaximum()
   {
      return getDataRange().getUpperBound();
   }
   
   public double getViewMinimum()
   {
      return getValueAxis().getLowerBound();
   }
   
   public double getViewMaximum()
   {
      return getValueAxis().getUpperBound();
   }
   
   public double getViewLength()
   {
      return getValueAxis().getRange().getLength();
   }
   
   public double getDisplayMaximum()
   {
      return getDataMaximum();//Math.max(getDataMaximum(), getViewMaximum());
   }

   public double getDisplayMinimum()
   {
      return getDataMinimum();//Math.min(getDataMinimum(), getViewMinimum());
   }
   
   private double displayMin;
   private double displayMax;
   private double viewLength;
   static private int STEPS = 100000; // 1000000 could be Integer.MAX_VALUE if you like, but this makes debugging a little easier
   Color oldColor;
   
   public void axisUpdate()
   {
      ValueAxis va = getValueAxis();
      if (va.isAutoRange())
      {
         if (oldColor == null)
            oldColor = getBackground();
         setBackground(oldColor.brighter());
      }
      else if (oldColor != null)
      {
         setBackground(oldColor);
         oldColor = null;
      }
      if (updating)
         return;
      updating = true;
      displayMin = 0;
      displayMax = 0;
      viewLength = 0;
      double viewMin = 0;
      double viewMax = 0;
      ratio = 1;
      Range dataRange = getDataRange();
      if (dataRange != null)
      {
         displayMin = getDisplayMinimum();
         displayMax = getDisplayMaximum();
         viewMin = getViewMinimum();
         viewMax = getViewMaximum();
         //ValueAxis va = getValueAxis();
         if (va instanceof DateAxis)
         {
            Timeline tl = ((DateAxis)va).getTimeline();
            displayMin = tl.toTimelineValue((long)displayMin);
            displayMax = tl.toTimelineValue((long)displayMax);
            viewMin = tl.toTimelineValue((long)viewMin);
            viewMax = tl.toTimelineValue((long)viewMax);
         }
         viewLength = viewMax - viewMin;
         ratio = STEPS / (displayMax - displayMin);
      }
      
      int newMin = 0;
      int newMax = STEPS;
      int newExtent = (int)(viewLength * ratio);
      int newValue;
      if (orientation == VERTICAL)
         newValue = (int)((displayMax - viewMax) * ratio);
      else
         newValue = (int)((viewMin - displayMin) * ratio);
      //System.out.println("ChartScrollBar.axisUpdate(): newValue: " + newValue + " newExtent: " + newExtent + " newMin: " + newMin + " newMax: " + newMax);
      setValues(newValue, newExtent, newMin, newMax);
      
      
      CombinedDomainXYPlot plot0 = (CombinedDomainXYPlot) chart.getPlot();
      OHLCDataset dataset = (OHLCDataset)plot0.getDataset(0);
      
      //chart
      Range r = getXYPlot().getDataRange((ValueAxis)getXYPlot().getRangeAxis());
      //int minimum = (int)getDataMinimum();
      //int maximum = (int)getDataMaximum();
      //double lowestLow = getLowestLow(dataset,minimum,maximum);
      //double highestHigh = getHighestHigh(dataset,minimum,maximum);

      // TODO getXYPlot().getRangeAxis().setRange(r.getLowerBound()*0.95, r.getUpperBound()*1.05);
      
      updating = false;
   }
   
   private double getLowestLow(OHLCDataset dataset, int start, int end){
	    double lowest;
	    lowest = dataset.getLowValue(0,start);
	    for(int i=1;i<=end;i++){
	        if(dataset.getLowValue(0,i) < lowest){
	            lowest = dataset.getLowValue(0,i);
	        }
	    }

	    return lowest;
	}


	private double getHighestHigh(OHLCDataset dataset, int start, int end){
	    double highest;
	    highest = dataset.getHighValue(0,start);
	    for(int i=1;i<=end;i++){
	        if(dataset.getLowValue(0,i) > highest){
	            highest = dataset.getHighValue(0,i);
	        }
	    }

	    return highest;
	}   

   
   public void axisChanged(AxisChangeEvent event)
   {
      //System.out.println("ChartScrollBar.axisChanged()");
      axisUpdate();
   }

   public void datasetChanged(DatasetChangeEvent event)
   {
      //System.out.println("ChartScrollBar.datasetChanged()");
      axisUpdate();
   }

   public void adjustmentValueChanged(AdjustmentEvent e)
   {
      if (updating)
         return;
      updating = true;
      double start, end;
      if (orientation == VERTICAL)
      {
         end = displayMax - (getValue() / ratio);
         start = end - viewLength;
      }
      else
      {
         start = getValue() / ratio + displayMin;
         end = start + viewLength;
      }
      
      if (end > start)
      {
         ValueAxis va = getValueAxis();
         if (va instanceof DateAxis)
         {
            Timeline tl = ((DateAxis)va).getTimeline();
            start = tl.toMillisecond((long)start);
            end = tl.toMillisecond((long)end);
            //System.out.println("********** converting start=" + new java.util.Date((long)start) + " end=" + new java.util.Date((long)end) + " **********");
         }
         getValueAxis().setRange(start, end);
      }
      updating = false;
   }
   
   public void zoomFull()
   {
      getValueAxis().setAutoRange(true);
      //getValueAxis().autoAdjustRange();
   }
   
   public void mouseClicked(MouseEvent e)
   {
      if (e.getButton() == MouseEvent.BUTTON3)
      {
         zoomFull();
      }
   }

   public void mouseEntered(MouseEvent e)
   {
   }

   public void mouseExited(MouseEvent e)
   {
   }

   public void mousePressed(MouseEvent e)
   {
   }

   public void mouseReleased(MouseEvent e)
   {
   }
}