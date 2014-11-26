/*
 * Copyright (C) 2011-2014 Aestas/IT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aestasit.jmx.dsl

import groovy.util.GroovyMBean

import java.awt.Color

import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel 
import org.jfree.chart.JFreeChart 
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.general.Dataset 
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries 
import org.jfree.data.time.TimeSeriesCollection 
import org.jfree.data.time.TimeTableXYDataset 
import org.jfree.chart.axis.DateAxis 
import org.jfree.chart.plot.PlotOrientation as Orientation

class ChartDelegate extends MonitoringDelegate implements Runnable {
  
  def title              = "Chart"
  def numericalAttribute = null
  def nameAttribute      = null 
  def xAxisLabel         = ""
  def yAxisLabel         = "Y"
  def type               = "Bar"
  def showLegend         = true
  def showTooltips       = true
  def showUrls           = true                  
  def multiplier         = 1
  def difference         = false
  def orientation        = "VERTICAL"
  def lowerBound         = null
  def upperBound         = null 
  
  private def Dataset dataset                = null
  private def previousValues                 = [:]  
  
  private def ChartPanel drawPanel           = null
  private def JFreeChart chart               = null
  
  ChartDelegate(modules, context) {
    super(modules, context)
    println "Creating chart."
  }
  
  void poll() {
    
    createDataset()
    createChart()
    
    // Disable chart panel notification to prevent flickering.
    chart.setNotify(false)
    
    // Prepare data set for updates.
    prepareDataset()
    
    def pollStart = new Date()
    
    // Process each mbean in the list.
    // println "Processing " + modules.size() + " mbean(s) for '" + title + "' chart"
    modules.each { GroovyMBean m ->
      
      // Gather mbean attribute values.
      def attributes = [numericalAttribute]
      if (nameAttribute) {
        attributes.add(nameAttribute)
      }
      def values = getValues(m, attributes)
      
      // Apply values to the data set.
      addValuesToDataset(pollStart, values[0], nameAttribute ? values[1] : null, m)
      
    }
    
    // Enable chart panel notification to update the chart.
    chart.setNotify(true)
    chart.fireChartChanged()
    
  }
  
  def void onStart() {
    createDataset()
    createChart()
    // Create chart draw panel if it is not done yet. 
    if (!drawPanel) {
      println "Creating new panel for chart data."      
      drawPanel = new ChartPanel(chart, true)
      context.swing.canvas.add(drawPanel)
      drawPanel.border = context.swing.lineBorder(color:Color.BLACK, thickness:1, parent:true)
    }
  }
  
  private createChart() {
    createDataset()
    if (!chart) {      
      switch (type) {
        case "Bar":
          println "Creating bar chart."
          chart = ChartFactory.createBarChart3D(title, xAxisLabel, yAxisLabel, dataset, Orientation."${orientation}", showLegend, showTooltips, showUrls)
          setBounds(chart.getCategoryPlot())
          break;
        case "Time":
          println "Creating time chart."
          chart = ChartFactory.createTimeSeriesChart(title, xAxisLabel, yAxisLabel, dataset, showLegend, showTooltips, showUrls)
          chart.getXYPlot().setOrientation(Orientation."${orientation}")
          setBounds(chart.getXYPlot())
          break;
        case "StackedTime":
          println "Creating stacked time chart."
          chart = ChartFactory.createStackedXYAreaChart(title, xAxisLabel, yAxisLabel, dataset, Orientation."${orientation}", showLegend, showTooltips, showUrls);
          chart.getXYPlot().setDomainAxis(new DateAxis())
          setBounds(chart.getXYPlot())          
          break;
      }
    }
  }
  
  def setBounds(plot) {
    if (lowerBound) {
      plot.getRangeAxis().setLowerBound(lowerBound)
    }
    if (upperBound) {
      plot.getRangeAxis().setUpperBound(upperBound)
    }
  }
  
  
  def createDataset() {
    if (!dataset) {
      switch (type) {
        case "Bar":
          dataset = new DefaultCategoryDataset()
          break;
        case "Time":
          dataset = new TimeSeriesCollection()
          break;
        case "StackedTime":
          dataset = new TimeTableXYDataset()
          break;
      }
    }
  }
  
  private prepareDataset() {
    switch (type) {
      case "Bar":
        dataset.clear()
        break;
      case "Time":
        break;
      case "StackedTime":
        break;
    }
  }
  
  private addValuesToDataset(timeValue, numericalValue, nameValue, GroovyMBean m) {
    if (numericalValue) {
      
      numericalValue = numericalValue * multiplier
      
      if (difference) {
        def previousValue = previousValues.get(m.name().getCanonicalName())
        def originalValue = numericalValue
        if (previousValue) {
          numericalValue  = numericalValue - previousValue
        } else {
          numericalValue  = 0
        }
        previousValues.put(m.name().getCanonicalName(), originalValue)
      }
      
      switch (type) {
        case "Bar":
          addBarChartValue(m, numericalValue, nameValue)        
          break;
        case "Time":
          addTimeChartValue(m, timeValue, numericalValue, nameValue)
          break;
        case "StackedTime":
          addStackedTimeChartValue(m, timeValue, numericalValue, nameValue)
          break;
      }
    }
  }
  
  def addBarChartValue(m, numericalValue, nameValue) {
    def categoryName   = getServerName(m)
    dataset.addValue(numericalValue, nameValue, categoryName)
  }
  
  def addTimeChartValue(m, timeValue, numericalValue, nameValue) {
    def time           = new Second(timeValue)
    def seriesName     = getServerName(m)
    if (nameValue) {
      if (!isSingleServerContext()) {
        seriesName += "." + nameValue
      } else {
        seriesName = nameValue
      }
    }
    if (dataset.getSeries(seriesName) == null) {
      // Add series if it's missing.
      dataset.addSeries(new TimeSeries(seriesName))
    }
    TimeSeries series = dataset.getSeries(seriesName)
    series.addOrUpdate(time, numericalValue)
  }
  
  def addStackedTimeChartValue(m, timeValue, numericalValue, nameValue) {
    def time           = new Second(timeValue)
    def seriesName     = getServerName(m)
    if (nameValue) {
      if (!isSingleServerContext()) {
        seriesName += "." + nameValue
      } else {
        seriesName = nameValue
      }
    }
    dataset.add(time, numericalValue, seriesName)
  }
}
