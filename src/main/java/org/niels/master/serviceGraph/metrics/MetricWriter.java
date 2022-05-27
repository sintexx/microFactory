package org.niels.master.serviceGraph.metrics;

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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MetricWriter {
    private static Logger logger = LoggerFactory.getLogger(MetricWriter.class);

    private Workbook workbook;

    private ServiceModel serviceModel;

    public MetricWriter(ServiceModel serviceModel) {
        this.serviceModel = serviceModel;
    }

    public void createWorkbookWithMetrics() {
        this.workbook = new XSSFWorkbook();

        var serviceMetricCalculator = new ServiceMetricCalculator(serviceModel);

        var allServicesWithMetrics = serviceModel
                .getConfig().getServices().stream()
                .map(s -> new ServiceWithMetrics(s, serviceMetricCalculator.calculateMetricForService(s))).collect(Collectors.toList());



        this.addServiceMetrics(allServicesWithMetrics);
        this.addAverageHandlingMetrics(allServicesWithMetrics);
        this.addHandlingWorkload();
        this.addGraphImage();

    }

    private void addAverageHandlingMetrics(List<ServiceWithMetrics> allServicesWithMetrics) {
        var sheet = workbook.createSheet("Metric Averages per handling");

        var allHandlings = this.serviceModel.getAllHandlings();

        var handlingMetricCalculator = new HandlingMetricCalculator(allServicesWithMetrics);

        var allHandlingsWithMetricAverages = allHandlings.stream().map(s -> handlingMetricCalculator.getAveragesOfMetricsPerHandling(s)).collect(Collectors.toList());

        createHeaderRow(sheet, allHandlings);

        var currentRow = 1;

        for (Metric m : Arrays.asList(Metric.values())) {
            var r = sheet.createRow(currentRow);

            r.createCell(0).setCellValue(m.toString());
            var currentCell = 1;
            for (var handlingWithMetricAverages : allHandlingsWithMetricAverages) {

                if (handlingWithMetricAverages.containsKey(m)) {
                    var value =handlingWithMetricAverages.get(m);

                    if (value instanceof Integer i) {
                        r.createCell(currentCell).setCellValue(i);

                    } else if (value instanceof Double i) {
                        r.createCell(currentCell).setCellValue(i);
                    }
                    else {
                        r.createCell(currentCell).setCellValue(value.toString());
                    }

                }
                currentCell++;
            }
            currentRow++;
        }
        sheet.autoSizeColumn(0);

    }

    private void addHandlingWorkload() {
        var sheet = workbook.createSheet("Handling Workloads");

        var calc = new HandlingWorkloadCalculator(this.serviceModel.getConfig().getServices());


        Row r = sheet.createRow(0);

        r.createCell(1).setCellValue("dbGetSingle");
        r.createCell(2).setCellValue("dbGetList");
        r.createCell(3).setCellValue("dbSaveSingle");
        r.createCell(4).setCellValue("dbSaveList");
        r.createCell(5).setCellValue("calculateIterations");

        int currentRow = 1;

        for (String handling : this.serviceModel.getAllHandlings()) {
            r = sheet.createRow(currentRow);

            var workload = calc.calculateWorkloadOfHandling(handling);

            r.createCell(0).setCellValue(handling);
            r.createCell(1).setCellValue(workload.getDbGetSingle());
            r.createCell(2).setCellValue(workload.getDbGetList());
            r.createCell(3).setCellValue(workload.getDbSaveSingle());
            r.createCell(4).setCellValue(workload.getDbSaveList());
            r.createCell(5).setCellValue(workload.getCalculateIterations());

            currentRow++;
        }

        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }

    }

    private void addServiceMetrics(List<ServiceWithMetrics> allServicesWithMetrics) {
        var sheet = workbook.createSheet("Service Metrics");

        createHeaderRow(sheet, serviceModel.getConfig().getServices().stream().map(s -> s.getName()).collect(Collectors.toList()));

        var currentRow = 1;

        for (Metric m : Arrays.asList(Metric.values())) {
            var r = sheet.createRow(currentRow);

            r.createCell(0).setCellValue(m.toString());
            var currentCell = 1;
            for (var serviceWithMetrics : allServicesWithMetrics) {

                if (serviceWithMetrics.getMetrics().containsKey(m)) {
                    var value = serviceWithMetrics.getMetrics().get(m);

                    if (value instanceof Integer i) {
                        r.createCell(currentCell).setCellValue(i);
                    } else if (value instanceof Double d) {
                        r.createCell(currentCell).setCellValue(d);
                    }
                    else {
                        r.createCell(currentCell).setCellValue(value.toString());
                    }

                }
                currentCell++;
            }
            currentRow++;
        }

        sheet.autoSizeColumn(0);

    }

    private void createHeaderRow(Sheet sheet, Collection<String> names) {
        int currentCell = 1;

        Row r = sheet.createRow(0);

        for (var s : names) {
            r.createCell(currentCell).setCellValue(s);
            currentCell++;
        }

        for (int i = 0; i < currentCell; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    public void writeToFile(File output) throws IOException {
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
            img.resize(6);

        } catch (Exception ex) {
            logger.error("Error adding graph to excel", ex);
        }
    }
}
