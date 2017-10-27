package support;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by du on 06/04/17.
 */
public class GenerateReport {

    private static final String REPORT_FOLDER = "reportFiles/";
    private static List<ChartFrame> chartFrames;

    public static void main(String args[]){

        chartFrames = new ArrayList<>();

        File directory = new File(REPORT_FOLDER);
        File[] contents = directory.listFiles();

        List<File> files = Arrays.stream(contents).filter(file -> !file.getName().equals(".DS_Store")).collect(Collectors.toList());

        files.stream().forEach( file -> {

            BufferedReader bufferedReader = null;

            try {
                bufferedReader = new BufferedReader(new FileReader(file));
                String sResult = bufferedReader.readLine();

                Gson gson = new Gson();
                Type listType = new TypeToken<Graph>(){}.getType();
                Graph graph = gson.fromJson(sResult,  listType);

                createChart(graph, graph.title);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    private static ChartFrame createChart(Graph graph, String title){

        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();


        graph.results.stream().forEach(result -> {
            List<XYSeries> xySeries =  xySeriesCollection.getSeries();
            List<XYSeries> foundSerie = xySeries.stream().filter(xy -> xy.getKey().equals(result.variableName)).collect(Collectors.toList());
            if(foundSerie.size() > 0){
                foundSerie.get(0).add((double)result.x, (double)result.y);
            }
            else{
                XYSeries serie = new XYSeries(result.variableName);
                serie.add((double)result.x, (double)result.y);
                xySeriesCollection.addSeries(serie);
            }

        });


        XYSeries experimentAverage = new XYSeries("Experiments' Average");


        for (double i=1; i <= 900; i++){

            double finalI = i;

            List<Result> timeResult = graph.results.stream().filter(result -> ((double)result.x) == finalI).collect(Collectors.toList());

            final double[] meanY = {0};

            timeResult.stream().forEach(result -> {
                meanY[0] += (double)result.y;

            });

            experimentAverage.add(i, meanY[0]/timeResult.size());
        }

        xySeriesCollection.addSeries(experimentAverage);


        final JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                graph.xTitle,
                graph.yTitle,
                xySeriesCollection,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        //TimeSeries dataset3 = MovingAverage.createMovingAverage(t1, "LT", 49, 49);

        //XYDataset experimentAverage = MovingAverage.createMovingAverage(xySeriesCollection, "Experiment Average", 50, 0);


        XYPlot xyPlot = (XYPlot) chart.getPlot();
        //xyPlot.setDataset(0, experimentAverage);
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);
        XYLineAndShapeRenderer xyLineAndShapeRenderer = (XYLineAndShapeRenderer)xyPlot.getRenderer();
        xyLineAndShapeRenderer.setBaseShapesVisible(false);

        Font font3 = new Font("Dialog", Font.PLAIN, 20);
        xyPlot.getDomainAxis().setLabelFont(font3);
        xyPlot.getRangeAxis().setLabelFont(font3);

        NumberAxis domain = (NumberAxis) xyPlot.getDomainAxis();
        domain.setRange(0.00, 900);

        xyPlot.setBackgroundPaint(Color.lightGray);
        xyPlot.setDomainGridlinePaint(Color.white);
        xyPlot.setRangeGridlinePaint(Color.white);
        chart.setBackgroundPaint(Color.lightGray);

        ChartFrame frame = new ChartFrame(title, chart);
        frame.pack();
        frame.setVisible(true);

        return frame;
    }
}






