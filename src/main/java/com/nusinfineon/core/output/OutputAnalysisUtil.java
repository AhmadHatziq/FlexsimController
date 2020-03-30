package com.nusinfineon.core.output;

import static com.nusinfineon.util.Directories.PRODUCT_KEY_COST_FILE_DIR;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class OutputAnalysisUtil {

    public static void saveDailyOutputSheet(String sheetName, TreeMap<Double, Double> treeMapOfDayToOutput,
                                            Workbook excelWorkbook) {
        final int DAY_COLUMN = 0;
        final int OUTPUT_COLUMN = 1;
        int rowIndex = 0;

        // Deletes sheet if it already exists
        if (excelWorkbook.getSheet(sheetName) != null) {
            excelWorkbook.removeSheetAt(excelWorkbook.getSheetIndex(sheetName));
        }

        // Create sheet and rows of headers
        Sheet sheetToWrite = excelWorkbook.createSheet(sheetName);
        Row headerRow = sheetToWrite.createRow(rowIndex);

        Cell dayCell = headerRow.createCell(DAY_COLUMN, CellType.STRING);
        dayCell.setCellValue("Day");

        Cell outputCell = headerRow.createCell(OUTPUT_COLUMN, CellType.STRING);
        outputCell.setCellValue("Output");

        rowIndex = rowIndex + 1;
        for (Double day: treeMapOfDayToOutput.keySet()) {
            Double output = treeMapOfDayToOutput.get(day);

            Row newRow = sheetToWrite.createRow(rowIndex);

            dayCell = newRow.createCell(DAY_COLUMN, CellType.NUMERIC);
            dayCell.setCellValue(day);

            outputCell = newRow.createCell(OUTPUT_COLUMN, CellType.NUMERIC);
            outputCell.setCellValue(output);

            rowIndex = rowIndex + 1;
        }
    }

    public static void saveProductCycleTimeFromDailyThroughputToNewSheet(String sheetName,
                                                                         TreeMap<String, Double> treeMapOfProductToAverageCycleTimesFromDailyThroughput,
                                                                         Workbook excelWorkbook) {
        final int PRODUCT_ID_COLUMN = 0;
        final int CYCLETIME_COLUMN = 1;
        int rowIndex = 0;

        // Deletes sheet if it already exists
        if (excelWorkbook.getSheet(sheetName) != null) {
            excelWorkbook.removeSheetAt(excelWorkbook.getSheetIndex(sheetName));
        }

        // Create sheet and rows of headers
        Sheet sheetToWrite = excelWorkbook.createSheet(sheetName);
        Row headerRow = sheetToWrite.createRow(rowIndex);

        Cell productCell = headerRow.createCell(PRODUCT_ID_COLUMN, CellType.STRING);
        productCell.setCellValue("Product ID");

        Cell cycleTimeCell = headerRow.createCell(CYCLETIME_COLUMN, CellType.STRING);
        cycleTimeCell.setCellValue("Time In System");

        rowIndex = rowIndex + 1;
        for (String productID: treeMapOfProductToAverageCycleTimesFromDailyThroughput.keySet()) {
            Double cycleTime = treeMapOfProductToAverageCycleTimesFromDailyThroughput.get(productID);

            Row newRow = sheetToWrite.createRow(rowIndex);

            productCell = newRow.createCell(PRODUCT_ID_COLUMN, CellType.STRING);
            productCell.setCellValue(productID);

            cycleTimeCell = newRow.createCell(CYCLETIME_COLUMN, CellType.NUMERIC);
            cycleTimeCell.setCellValue(cycleTime);

            rowIndex = rowIndex + 1;
        }
    }



    public static void saveProductThroughputToNewSheet(String sheetName, TreeMap<String, Double> treeMapOfAverageThroughput,
                                                       Workbook excelWorkbook) {
        final int PRODUCT_ID_COLUMN = 0;
        final int THROUGHPUT_COLUMN = 1;
        int rowIndex = 0;

        // Deletes sheet if it already exists
        if (excelWorkbook.getSheet(sheetName) != null) {
            excelWorkbook.removeSheetAt(excelWorkbook.getSheetIndex(sheetName));
        }

        // Create sheet and rows of headers
        Sheet sheetToWrite = excelWorkbook.createSheet(sheetName);
        Row headerRow = sheetToWrite.createRow(rowIndex);

        Cell productCell = headerRow.createCell(PRODUCT_ID_COLUMN, CellType.STRING);
        productCell.setCellValue("Product ID");

        Cell throughputCell = headerRow.createCell(THROUGHPUT_COLUMN, CellType.STRING);
        throughputCell.setCellValue("Throughput");

        rowIndex = rowIndex + 1;
        for (String productID: treeMapOfAverageThroughput.keySet()) {
            Double cycleTime = treeMapOfAverageThroughput.get(productID);

            Row newRow = sheetToWrite.createRow(rowIndex);

            productCell = newRow.createCell(PRODUCT_ID_COLUMN, CellType.STRING);
            productCell.setCellValue(productID);

            throughputCell = newRow.createCell(THROUGHPUT_COLUMN, CellType.NUMERIC);
            throughputCell.setCellValue(cycleTime);

            rowIndex = rowIndex + 1;
        }
    }

    public static void saveRunTypeAndUtilizationRatesTONewSheet(String sheetName, String runtype,
                                                                TreeMap<String, Double> mapOfSummaryStatistics,
                                                                Workbook excelWorkbook) {
        final int HEADER_ROW_INDEX = 0;
        final int SUMMARY_ROW_INDEX = 1;

        // Deletes sheet if it already exists
        if (excelWorkbook.getSheet(sheetName) != null) {
            excelWorkbook.removeSheetAt(excelWorkbook.getSheetIndex(sheetName));
        }

        // Create sheet and rows
        Sheet sheetToWrite = excelWorkbook.createSheet(sheetName);
        Row headerRow = sheetToWrite.createRow(HEADER_ROW_INDEX);
        Row summaryRow = sheetToWrite.createRow(SUMMARY_ROW_INDEX);

        // Write simulation run data
        Cell headerCell = headerRow.createCell(0, CellType.STRING);
        Cell summaryCell = summaryRow.createCell(0, CellType.STRING);
        headerCell.setCellValue("RUN_TYPE");
        summaryCell.setCellValue(runtype);

        // Write the  summary data
        int columnCount = 1;
        for (String category: mapOfSummaryStatistics.keySet()) {
            Double statistic = mapOfSummaryStatistics.get(category);
            headerCell = headerRow.createCell(columnCount, CellType.STRING);
            summaryCell = summaryRow.createCell(columnCount, CellType.STRING);
            headerCell.setCellValue(category);
            summaryCell.setCellValue(statistic);
            columnCount = columnCount + 1;
        }
    }

    public static void saveProductCycleTimeToNewSheet(String sheetName, TreeMap<String, Double> treeMapOfAverageCycleTimes,
                                                      Workbook excelWorkbook) {
        final int PRODUCT_ID_COLUMN = 0;
        final int CYCLE_TIME_COLUMN = 1;
        int rowIndex = 0;

        // Deletes sheet if it already exists
        if (excelWorkbook.getSheet(sheetName) != null) {
            excelWorkbook.removeSheetAt(excelWorkbook.getSheetIndex(sheetName));
        }

        // Create sheet and rows of headers
        Sheet sheetToWrite = excelWorkbook.createSheet(sheetName);
        Row headerRow = sheetToWrite.createRow(rowIndex);

        Cell productCell = headerRow.createCell(PRODUCT_ID_COLUMN, CellType.STRING);
        productCell.setCellValue("Product ID");

        Cell cycleTimeCell = headerRow.createCell(CYCLE_TIME_COLUMN, CellType.STRING);
        cycleTimeCell.setCellValue("Cycle Time");

        // Exit out if null. Null as sheet doesn't exist in the original excel run. Only exit after writing blank columns.
        if (treeMapOfAverageCycleTimes == null) {
            return;
        }

        rowIndex = rowIndex + 1;
        for (String productID: treeMapOfAverageCycleTimes.keySet()) {
            Double cycleTime = treeMapOfAverageCycleTimes.get(productID);

            Row newRow = sheetToWrite.createRow(rowIndex);

            productCell = newRow.createCell(PRODUCT_ID_COLUMN, CellType.STRING);
            productCell.setCellValue(productID);

            cycleTimeCell = newRow.createCell(CYCLE_TIME_COLUMN, CellType.NUMERIC);
            cycleTimeCell.setCellValue(cycleTime);

            rowIndex = rowIndex + 1;
        }
    }

    public static File getProductKeyCostExcelFileFromRelativeDirectory() throws IOException {
        URL productCostFile = OutputAnalysisUtil.class.getResource(PRODUCT_KEY_COST_FILE_DIR);
        File tempOutputFile = Files.createTempFile("product_key_cost_temp", ".xlsx").toFile();
        FileUtils.copyURLToFile(productCostFile, tempOutputFile);
        return tempOutputFile;
    }

    /**
     * Creates a new sheet inside the given excel workbook. Will write values from the provided hash map to the sheet.
     * @param sheetName Name of the new sheet to save the data in.
     * @param hashMapOfValuesToWrite Hash map of String to Double values to write.
     * @param excelWorkbook Workbook to write data to.
     */
    public static void saveStringDoubleHashMapToNewSheet(String sheetName, HashMap<String, Double> hashMapOfValuesToWrite,
                                                         Workbook excelWorkbook) {
        // Deletes sheet if it already exists
        if (excelWorkbook.getSheet(sheetName) != null) {
            excelWorkbook.removeSheetAt(excelWorkbook.getSheetIndex(sheetName));
        }

        Sheet sheetToWrite = excelWorkbook.createSheet(sheetName);

        int rowIndex = 0;

        for (String columnName: hashMapOfValuesToWrite.keySet()) {
            Row row = sheetToWrite.createRow(rowIndex);

            // Write column name
            Cell cell = row.createCell(0, CellType.STRING);
            cell.setCellValue(columnName);

            // Write corresponding value
            cell = row.createCell(1, CellType.NUMERIC);
            cell.setCellValue(hashMapOfValuesToWrite.get(columnName));

            rowIndex ++;
        }
    }

    /**
     * Creates a new sheet inside the given excel workbook. Will write values from the provided hash map to the sheet.
     * @param sheetName Name of the new sheet to save the data in.
     * @param hashMapOfValuesToWrite Hash map of String to Double values to write.
     * @param excelWorkbook Workbook to write data to.
     */
    public static void saveStringLongHashMapToNewSheet(String sheetName, HashMap<String, Long> hashMapOfValuesToWrite,
                                                         Workbook excelWorkbook) {
        // Deletes sheet if it already exists
        if (excelWorkbook.getSheet(sheetName) != null) {
            excelWorkbook.removeSheetAt(excelWorkbook.getSheetIndex(sheetName));
        }

        Sheet sheetToWrite = excelWorkbook.createSheet(sheetName);

        int rowIndex = 0;

        for (String columnName: hashMapOfValuesToWrite.keySet()) {
            Row row = sheetToWrite.createRow(rowIndex);

            // Write column name
            Cell cell = row.createCell(0, CellType.STRING);
            cell.setCellValue(columnName);

            // Write corresponding value
            cell = row.createCell(1, CellType.NUMERIC);
            cell.setCellValue(hashMapOfValuesToWrite.get(columnName));

            rowIndex ++;
        }
    }

    /**
     * Converts a hashmap of string-long to string-double.
     * @param hashMapOfLongValues Hash map of string-long values.
     * @return Hash map of string-double values.
     */
    public static HashMap<String, Double> convertHashMapWithLongValueToDouble(HashMap<String, Long> hashMapOfLongValues) {
        HashMap<String, Double> hashMapOfDoubleValues = new HashMap<String, Double>();

        for (String s: hashMapOfLongValues.keySet()) {
            Double value = (double) hashMapOfLongValues.get(s);

            hashMapOfDoubleValues.put(s, value);
        }
        return hashMapOfDoubleValues;
    }

    /**
     * Copies an input file to a destination file via byte stream.
     * @param source The source file to be copied.
     * @param destination The destination file to contain the source file. Can be empty.
     * @throws IOException
     */
    public static void copyFileUsingStream(File source, File destination) throws IOException {
        try (InputStream is = new FileInputStream(source); OutputStream os = new FileOutputStream(destination)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }

    /**
     * Helper function to print all sheets and the corresponding headers in a single excel workbook.
     * @param excelFilePath String representing the filepath pointing to an excel workbook.
     * @throws IOException
     */
    public static void getSheetNameAndColumnsFromExcelFile(String excelFilePath) throws IOException {
        File excelFile = new File(excelFilePath);

        if (!excelFile.exists()) {
            throw new IOException("File not found in: " + excelFilePath);
        }

        Workbook workbook = WorkbookFactory.create(excelFile);

        HashMap<String, ArrayList<String>> mapOfSheetToColumns = new HashMap<>();

        for (Sheet sheet: workbook) {
            ArrayList<String> columnNames = new ArrayList<String>();
            Row firstRow = sheet.getRow(0);

            for (int cellIndex = 0; cellIndex < firstRow.getPhysicalNumberOfCells(); cellIndex ++) {
                Cell cell = firstRow.getCell(cellIndex);
                String columnName = cell.getStringCellValue();
                columnNames.add(columnName);
            }
            mapOfSheetToColumns.put(sheet.getSheetName(), columnNames);
        }

        for (String sheetName : mapOfSheetToColumns.keySet() ) {
            System.out.println("\nSheet name: " + sheetName);
            for (String columnName : mapOfSheetToColumns.get(sheetName) ) {
                System.out.println(columnName);
            }
        }
        workbook.close();
    }

    /**
     * Helper method to print all sheets available in an Excel workbook.
     * @param excelFilePath String representing the filepath pointing to an excel workbook.
     * @throws IOException
     */
    public static void accessOriginalInputFile(String excelFilePath) throws IOException {
        File excelFile = new File(excelFilePath);

        if (!excelFile.exists()) {
            throw new IOException("File not found in: " + excelFilePath);
        }

        Workbook workbook = WorkbookFactory.create(excelFile);

        ArrayList<String> sheetNames = new ArrayList<String>();

        for (Sheet sheet: workbook) {
            sheetNames.add(sheet.getSheetName());
        }

        sheetNames.sort(String::compareToIgnoreCase);

        for (String s: sheetNames) {
            System.out.println(s);
        }
        workbook.close();
    }

    /**
     * Returns the string stack trace of an exception object.
     * @param e An exception object.
     * @return String
     */
    public static String ExceptionToString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();
        return exceptionAsString;
    }

    /**
     * Converts a File's toString() into just the file itself. Removes directory and file extension.
     *
     * @param filePath from File.toString(). Sample: 'C:\Users\Ahmad\Documents\NUS\IE 3100M\Data Files\summary_appended\output_24.xlsx'
     * @return Just the file name ie 'output_24'
     */
    public static String fileStringToFileName(String filePath) {
        String[] strings = filePath.split("\\\\");
        String fileNameWithExtension = strings[strings.length - 1];
        String fileName = fileNameWithExtension.split("\\.")[0];
        return fileName;
    }

    /**
     * Returns the median of an array of double values
     * @param doubleValues Array of double values
     * @return Median value within the array.
     */
    public static Double medianOfDoubleList(ArrayList<Double> doubleValues) {
        Double median = -1.0;
        if ((doubleValues.size() % 2) == 0) {
            // Even sized array
            Double middleRank = doubleValues.size() / 2.0;
            int upperIndex = (int) Math.round(middleRank);
            int  lowerIndex = upperIndex + 1;
            System.out.println(upperIndex);
            System.out.println(lowerIndex);
            median = (doubleValues.get(upperIndex - 1) + doubleValues.get(lowerIndex - 1) ) / 2.0;
        } else {
            // Odd sized array
            int middleRank = (doubleValues.size() + 1) / 2;
            median = doubleValues.get(middleRank - 1);
        }
        return median;
    }

    public static HashMap<String, Integer> getMappingOfHeadersToIndex(Row headerRow, ArrayList<String> headers) {
        HashMap<String, Integer> mapOfColumns = new HashMap<>();

        for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
            Cell cell = headerRow.getCell(i);

            if (cell != null) {
                String headerString = cell.getStringCellValue();
                if (headers.contains(headerString)) {
                    mapOfColumns.put(headerString, i);
                }
            }
        }
        return mapOfColumns;
    }

    public static void writeUtilizationRate(Sheet destinationUtilizationSheet, Row sourceRow, HashMap<String, Integer> mapOfUtilColumnHeaders, int destinationRowIndex) {
        // Create new row
        Row newRow = destinationUtilizationSheet.createRow(destinationRowIndex);

        // Add the row content. Obtain the index from the hash map and write data to a new cell
        Cell cell = newRow.createCell(0, CellType.STRING);
        cell.setCellValue(sourceRow.getCell(mapOfUtilColumnHeaders.get("RUN_TYPE")).getStringCellValue());

        cell = newRow.createCell(1, CellType.NUMERIC);
        cell.setCellValue(sourceRow.getCell(mapOfUtilColumnHeaders.get("UTILIZATION_RATE_IDLE")).getNumericCellValue());

        cell = newRow.createCell(2, CellType.NUMERIC);
        cell.setCellValue(sourceRow.getCell(mapOfUtilColumnHeaders.get("UTILIZATION_RATE_PROCESSING")).getNumericCellValue());

        cell = newRow.createCell(3, CellType.NUMERIC);
        cell.setCellValue(sourceRow.getCell(mapOfUtilColumnHeaders.get("UTILIZATION_RATE_SETUP")).getNumericCellValue());

        cell = newRow.createCell(4, CellType.NUMERIC);
        cell.setCellValue(sourceRow.getCell(mapOfUtilColumnHeaders.get("UTILIZATION_RATE_WAITING FOR OPERATOR")).getNumericCellValue());

        cell = newRow.createCell(5, CellType.NUMERIC);
        cell.setCellValue(sourceRow.getCell(mapOfUtilColumnHeaders.get("UTILIZATION_RATE_WAITING FOR TRANSPORTER")).getNumericCellValue());
    }

    public static void saveProductOutputAndWorth(String sheetName, TreeMap<String, ArrayList<Double>> productToOutputAndWorth,
                                                 Workbook destinationWorkbook) {
        final int PRODUCT_COLUMN = 0;
        final int OUTPUT_COLUMN = 1;
        final int WORTH_COLUMN = 2;
        int rowIndex = 0;

        // Deletes sheet if it already exists
        if (destinationWorkbook.getSheet(sheetName) != null) {
            destinationWorkbook.removeSheetAt(destinationWorkbook.getSheetIndex(sheetName));
        }

        // Write the headers
        // Create sheet and rows of headers
        Sheet sheetToWrite = destinationWorkbook.createSheet(sheetName);
        Row headerRow = sheetToWrite.createRow(rowIndex);

        Cell productCell = headerRow.createCell(PRODUCT_COLUMN, CellType.STRING);
        productCell.setCellValue("Product");

        Cell outputCell = headerRow.createCell(OUTPUT_COLUMN, CellType.STRING);
        outputCell.setCellValue("Output");

        Cell worthCell = headerRow.createCell(WORTH_COLUMN, CellType.STRING);
        worthCell.setCellValue("Worth");

        rowIndex = rowIndex + 1;
        for (String product: productToOutputAndWorth.keySet()) {
            ArrayList<Double> outputAndWorth = productToOutputAndWorth.get(product);
            Double output = outputAndWorth.get(0);
            Double worth = outputAndWorth.get(1);

            Row newRow = sheetToWrite.createRow(rowIndex);

            productCell = newRow.createCell(PRODUCT_COLUMN, CellType.STRING);
            productCell.setCellValue(product);

            outputCell = newRow.createCell(OUTPUT_COLUMN, CellType.NUMERIC);
            outputCell.setCellValue(output);

            worthCell = newRow.createCell(WORTH_COLUMN, CellType.NUMERIC);
            worthCell.setCellValue(worth);

            rowIndex = rowIndex + 1;
        }
    }
}
