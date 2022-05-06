package org.niels.master.serviceGraph.metrics;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.niels.master.serviceGraph.GraphVisualizer;
import org.niels.master.serviceGraph.ServiceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class MetricWriter {
    private static Logger logger = LoggerFactory.getLogger(MetricWriter.class);

    private Workbook workbook;
    private Sheet sheet;

    private ServiceModel serviceModel;

    public MetricWriter(ServiceModel serviceModel) {
        this.serviceModel = serviceModel;
    }

    public void createWorkbookWithMetrics() {
        this.workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Metrics");

        var calc = new MetricCalculator(serviceModel);

        var allServicesWithMetrics = serviceModel.getConfig().getServices().stream().map(s -> calc.calculateMetricForService(s)).collect(Collectors.toList());

        int currentRow = 0;
        int currentCell = 1;

        Row r = sheet.createRow(currentRow);

        for (var s : serviceModel.getConfig().getServices()) {
            r.createCell(currentCell).setCellValue(s.getName());
            currentCell++;
        }

        for (int i = 0; i < currentCell; i++) {
            this.sheet.autoSizeColumn(i);
        }

        currentRow++;


        for (Metric m : Arrays.asList(Metric.values())) {
            r = sheet.createRow(currentRow);

            r.createCell(0).setCellValue(m.toString());
            currentCell = 1;
            for (Map<Metric, Object> serviceWithMetrics : allServicesWithMetrics) {

                if (serviceWithMetrics.containsKey(m)) {
                    var value = serviceWithMetrics.get(m);

                    if (value instanceof Integer i) {
                        r.createCell(currentCell).setCellValue(i);
                    } else {
                        r.createCell(currentCell).setCellValue(value.toString());
                    }

                }
                currentCell++;
            }
            currentRow++;
        }

        this.addGraphImage();

    }

    public void writeToFile(File output) throws IOException {

        this.sheet.autoSizeColumn(0);

        FileOutputStream outputStream = new FileOutputStream(output);
        workbook.write(outputStream);
        workbook.close();
    }

    private void addGraphImage() {
        try {
            var imageSheet = workbook.createSheet("Graph");
            int graphImage = workbook.addPicture(GraphVisualizer.getGraphAsPng(this.serviceModel), Workbook.PICTURE_TYPE_PNG);

            XSSFDrawing drawing = (XSSFDrawing) imageSheet.createDrawingPatriarch();

            XSSFClientAnchor imageAnchor = new XSSFClientAnchor();
            imageAnchor.setCol1(0);
            imageAnchor.setCol2(1);
            imageAnchor.setRow1(0);
            imageAnchor.setRow2(1);

            var img = drawing.createPicture(imageAnchor, graphImage);

            imageSheet.autoSizeColumn(0);
            img.resize();

        } catch (Exception ex) {
            logger.error("Error adding graph to excel", ex);
        }
    }
}
