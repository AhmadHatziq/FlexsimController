package com.nusinfineon.core;

import static com.nusinfineon.util.Directories.TABLEAU_EXCEL_FILE_NAME;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.nusinfineon.exceptions.CustomException;
import com.nusinfineon.core.output.OutputAnalysisCalculation;
import com.nusinfineon.core.output.OutputAnalysisUtil;

public class OutputCore {

    private static final Logger LOGGER = Logger.getLogger(OutputCore.class.getName());

    public OutputCore() {
        // Empty constructor
    }

    /**
     * Main execute function of OutputCore to process output files and tableau-excel-file
     * @throws IOException
     * @throws CustomException
     */
    public void execute(File folderDirectory, File destinationDirectory) throws IOException, CustomException {
        // Generate output statistics for all excel files in a folder
        appendSummaryStatisticsOfFolderOFExcelFiles(folderDirectory);

        // Generate the tableau excel file from the folder of excel files (with output data appended)
        generateTableauExcelFile(folderDirectory, destinationDirectory);
    }

    /**
     * Wrapper function to handle all files in a specified folder.
     * @param folderDirectory Directory of a folder with excel files to be processed.
     * @throws CustomException if argument is not a directory.
     */
    private void appendSummaryStatisticsOfFolderOFExcelFiles(File folderDirectory) throws IOException, CustomException {
        if (!folderDirectory.isDirectory()) {
            throw new CustomException(folderDirectory.toString() + " is not a directory");
        }
        LOGGER.info("Accessing folder: " + folderDirectory.toString());

        // Process all files in the directory and append their respective summary statistics
        for (File file: folderDirectory.listFiles()) {
            if (file.exists() && (!file.isDirectory())) {
                appendSummaryStatisticsOfSingleOutputExcelFile(file);
            }
        }
    }

    /**
     * Generates a single excel file to be used with Tableau. Summarizes the output file data for each excel file.
     * Saves the file into "src/main/resources/sample-output-files/tableau-excel-file/tableau-excel-file.xlsx"
     */
    private void generateTableauExcelFile(File folderOfExcelFiles, File destinationDirectory)
            throws IOException, CustomException {
        LOGGER.info("Starting generateTableauExcelFile method");

        // Generate a list of excel files from the folder
        if (!folderOfExcelFiles.isDirectory()) {
            throw new CustomException("Argument for generateTableauExcelFile() method is not a folder");
        }

        ArrayList<File> excelFiles = new ArrayList<File>();
        for (File file: folderOfExcelFiles.listFiles()) {
            if (file.exists() && (!file.isDirectory())) {
                excelFiles.add(file);
            }
        }
        LOGGER.info("No. of excel files to process: " + excelFiles.size());

        // Create the destination excel file
        final File destinationFile = new File( destinationDirectory + "/" + TABLEAU_EXCEL_FILE_NAME);
        if (!destinationFile.exists()) {
            // Delete if there is a file present
            destinationFile.createNewFile();
        }

        // Create the summary workbook
        Workbook destinationWorkbook = new XSSFWorkbook();

        // Create the summary utilization sheet ========================================================================

        // Create the sheet and write column names
        Sheet destinationUtilizationSheet = destinationWorkbook.createSheet("IBIS_UTILIZATION");

        // Write column headers
        final String[] UTILIZATION_COLUMN_HEADERS = {"Run Type", "Idle Rate", "Processing Rate", "Setup Rate",
                "Waiting for Operator Rate", "Waiting for Transporter Rate"};
        Row headerRow = destinationUtilizationSheet.createRow(0);
        for (int i = 0; i < UTILIZATION_COLUMN_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i, CellType.STRING);
            cell.setCellValue(UTILIZATION_COLUMN_HEADERS[i]);
        }

        // Store the headers of the excel file containing the summarized rates
        ArrayList<String> SOURCE_UTILIZATION_COLUMN_HEADERS = new ArrayList<>(Arrays.asList("RUN_TYPE", "UTILIZATION_RATE_IDLE",
                "UTILIZATION_RATE_PROCESSING", "UTILIZATION_RATE_SETUP", "UTILIZATION_RATE_WAITING FOR OPERATOR",
                "UTILIZATION_RATE_WAITING FOR TRANSPORTER"));

        // Iterate through each excel file and write the utilization data
        final String SOURCE_UTILIZATION_SHEET = "RUN_TYPE_AND_IBIS_UTILIZATION";
        int destinationRowCount = 1;
        for (File excelFile: excelFiles) {
            Workbook sourceWorkbook = WorkbookFactory.create(excelFile);
            Sheet sourceUtilizationSheet = sourceWorkbook.getSheet(SOURCE_UTILIZATION_SHEET);
            Row sourceRow = sourceUtilizationSheet.getRow(1);

            // Get index corresponding with column headers
            headerRow = sourceUtilizationSheet.getRow(0);
            HashMap<String, Integer> mapOfUtilColumnHeaders = OutputAnalysisUtil.getMappingOfHeadersToIndex(headerRow, SOURCE_UTILIZATION_COLUMN_HEADERS);

            // Write the data to a new row if there are valid headers
            if (mapOfUtilColumnHeaders.size() == 6) {
                OutputAnalysisUtil.writeUtilizationRate(destinationUtilizationSheet, sourceRow, mapOfUtilColumnHeaders, destinationRowCount);
            } else {
                String fileName = OutputAnalysisUtil.fileStringToFileName(excelFile.toString());
                LOGGER.info(fileName + " doesn't contain entries for " + SOURCE_UTILIZATION_SHEET);
            }
            sourceWorkbook.close();
            destinationRowCount = destinationRowCount + 1;
        }

        // End of writing utilization rates to summary workbook

        // Create the Stay Time Sheet ==================================================================================
        Sheet destinationStayTimeSheet = destinationWorkbook.createSheet("STAY_TIME");

        // Write column headers
        final String[] STAY_TIME_COLUMN_HEADERS = {"Run Type", "Stay Time", "Product ID"};
        headerRow = destinationStayTimeSheet.createRow(0);
        for (int i = 0; i < STAY_TIME_COLUMN_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i, CellType.STRING);
            cell.setCellValue(STAY_TIME_COLUMN_HEADERS[i]);
        }

        // Iterate through the excel files and write the stay time data
        final String SOURCE_STAYTIME_SHEET = "PRODUCT_STAY_TIME";
        final int STAY_TIME_PRODUCT_ID_COLUMN_INDEX = 0;
        final int STAY_TIME_PRODUCT_STAYIME_INDEX = 1;

        destinationRowCount = 1;
        for (File excelFile: excelFiles) {
            Workbook sourceWorkbook = WorkbookFactory.create(excelFile);
            Sheet sourceStaytimeSheet = sourceWorkbook.getSheet(SOURCE_STAYTIME_SHEET);
            String runType = OutputAnalysisUtil.fileStringToFileName(excelFile.toString());

            if (sourceStaytimeSheet != null) {

                // Iterate through all rows and insert to the new sheet
                for (int i = 1; i < sourceStaytimeSheet.getPhysicalNumberOfRows(); i++) {
                    Row sourceRow = sourceStaytimeSheet.getRow(i);

                    Cell productCell = sourceRow.getCell(STAY_TIME_PRODUCT_ID_COLUMN_INDEX);
                    Cell staytimeCell = sourceRow.getCell(STAY_TIME_PRODUCT_STAYIME_INDEX);

                    if (productCell != null) {
                        // Extract values
                        String productId = productCell.getStringCellValue();
                        Double productStayTime = staytimeCell.getNumericCellValue();

                        // Write to the destination sheet
                        Row newStaytimeRow = destinationStayTimeSheet.createRow(destinationRowCount);

                        Cell destinationRuntypeCell = newStaytimeRow.createCell(0, CellType.STRING);
                        destinationRuntypeCell.setCellValue(runType);

                        Cell destinationStayTimeCell = newStaytimeRow.createCell(1, CellType.NUMERIC);
                        destinationStayTimeCell.setCellValue(productStayTime);

                        Cell destinationProductCell = newStaytimeRow.createCell(2, CellType.STRING);
                        destinationProductCell.setCellValue(productId);

                        destinationRowCount = destinationRowCount + 1;
                    }
                }
            }
            sourceWorkbook.close();
        }

        // End of writing stay time to sheet

        // Create the Time in System Sheet ==================================================================================
        Sheet destinationTimeInSystemSheet = destinationWorkbook.createSheet("TIME_IN_SYSTEM");

        // Write column headers
        final String[] TIME_IN_SYSTEM_COLUMN_HEADERS = {"Run Type", "Time In System", "Product ID"};
        headerRow = destinationTimeInSystemSheet.createRow(0);
        for (int i = 0; i < TIME_IN_SYSTEM_COLUMN_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i, CellType.STRING);
            cell.setCellValue(TIME_IN_SYSTEM_COLUMN_HEADERS[i]);
        }

        // Iterate through the excel files and write the stay time data
        final String SOURCE_TIME_IN_SYSTEM_SHEET = "PRODUCT_TIME_IN_SYSTEM";
        final int TIME_IN_SYSTEM_PRODUCT_ID_COLUMN_INDEX = 0;
        final int TIME_IN_SYSTEM_PRODUCT_STAYIME_INDEX = 1;

        destinationRowCount = 1;
        for (File excelFile: excelFiles) {
            Workbook sourceWorkbook = WorkbookFactory.create(excelFile);
            Sheet sourceTimeInSystemSheet = sourceWorkbook.getSheet(SOURCE_TIME_IN_SYSTEM_SHEET);
            String runType = OutputAnalysisUtil.fileStringToFileName(excelFile.toString());

            // Iterate through all rows and insert to the new sheet
            for (int i = 1; i < sourceTimeInSystemSheet.getPhysicalNumberOfRows(); i++) {
                Row sourceRow = sourceTimeInSystemSheet.getRow(i);

                Cell productCell = sourceRow.getCell(TIME_IN_SYSTEM_PRODUCT_ID_COLUMN_INDEX);
                Cell staytimeCell = sourceRow.getCell(TIME_IN_SYSTEM_PRODUCT_STAYIME_INDEX);

                if (productCell != null) {
                    // Extract values
                    String productId = productCell.getStringCellValue();
                    Double productStayTime = staytimeCell.getNumericCellValue();

                    // Write to the destination sheet
                    Row newTimeInSystemRow = destinationTimeInSystemSheet.createRow(destinationRowCount);

                    Cell destinationRuntypeCell = newTimeInSystemRow.createCell(0, CellType.STRING);
                    destinationRuntypeCell.setCellValue(runType);

                    Cell destinationStayTimeCell = newTimeInSystemRow.createCell(1, CellType.NUMERIC);
                    destinationStayTimeCell.setCellValue(productStayTime);

                    Cell destinationProductCell = newTimeInSystemRow.createCell(2, CellType.STRING);
                    destinationProductCell.setCellValue(productId);

                    destinationRowCount = destinationRowCount + 1;
                }
            }
            sourceWorkbook.close();
        }
        // End of writing product stay times section

        // Create Product throughput Sheet =============================================================================
        Sheet destinationThroughputSheet = destinationWorkbook.createSheet("THROUGHPUT");

        // Write column headers
        final String[] THROUGHPUT_COLUMN_HEADERS = {"Run Type", "Throughput", "Product ID"};
        headerRow = destinationThroughputSheet.createRow(0);
        for (int i = 0; i < THROUGHPUT_COLUMN_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i, CellType.STRING);
            cell.setCellValue(THROUGHPUT_COLUMN_HEADERS[i]);
        }

        // Iterate through the excel files and write the throughput data
        final String SOURCE_THROUGHPUT_SHEET = "PRODUCT_THROUGHPUT";
        final int THROUHGPUT_PRODUCT_ID_COLUMN_INDEX = 0;
        final int THROUGHPUT_PRODUCT_THROUGHPUT_INDEX = 1;

        destinationRowCount = 1;
        for (File excelFile: excelFiles) {
            Workbook sourceWorkbook = WorkbookFactory.create(excelFile);
            Sheet sourceThroughputSheet = sourceWorkbook.getSheet(SOURCE_THROUGHPUT_SHEET);
            String runType = OutputAnalysisUtil.fileStringToFileName(excelFile.toString());

            // Iterate through all rows and insert to the new sheet
            for (int i = 1; i < sourceThroughputSheet.getPhysicalNumberOfRows(); i++) {
                Row sourceRow = sourceThroughputSheet.getRow(i);

                Cell productCell = sourceRow.getCell(THROUHGPUT_PRODUCT_ID_COLUMN_INDEX);
                Cell throughputCell = sourceRow.getCell(THROUGHPUT_PRODUCT_THROUGHPUT_INDEX);

                if (productCell != null) {
                    // Extract values
                    String productId = productCell.getStringCellValue();
                    Double productThroughput = throughputCell.getNumericCellValue();

                    // Write to the destination sheet
                    Row newThroughputRow = destinationThroughputSheet.createRow(destinationRowCount);

                    Cell destinationRuntypeCell = newThroughputRow.createCell(0, CellType.STRING);
                    destinationRuntypeCell.setCellValue(runType);

                    Cell destinationSThroughputCell = newThroughputRow.createCell(1, CellType.NUMERIC);
                    destinationSThroughputCell.setCellValue(productThroughput);

                    Cell destinationProductCell = newThroughputRow.createCell(2, CellType.STRING);
                    destinationProductCell.setCellValue(productId);

                    destinationRowCount = destinationRowCount + 1;
                }
            }
            sourceWorkbook.close();
        }
        // End of writing product THROUGHPUT section

        // Create daily throughput sheet ===============================================================================
        Sheet destinationDailyThroughputSheet = destinationWorkbook.createSheet("THROUGHPUT_DAILY");
        final String DAILY_THROUGHPUT_SOURCE_SHEET = "DAILY_OUTPUT";
        final int DAY_COLUMN_INDEX = 0;
        final int DAILY_THROUGHPUT_INDEX = 1;

        // Write column headers
        final String[] DAILY_THROUGHPUT_COLUMN_HEADERS = {"Day"};
        headerRow = destinationDailyThroughputSheet.createRow(0);
        for (int i = 0; i < DAILY_THROUGHPUT_COLUMN_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i, CellType.STRING);
            cell.setCellValue(DAILY_THROUGHPUT_COLUMN_HEADERS[i]);
        }

        // Obtain the run types, which are used as column headers
        int columnIndex = 1;
        for (int i = 0; i < excelFiles.size(); i++) {
            File excelFile = excelFiles.get(i);
            String runType = OutputAnalysisUtil.fileStringToFileName(excelFile.toString());
            Cell cell = headerRow.createCell(columnIndex, CellType.STRING);
            cell.setCellValue(runType);
            columnIndex = columnIndex + 1;
        }

        // Populate day index based on the first excel file. Assumes all runs have the same run length
        File firstExcelFile = excelFiles.get(0);
        Workbook sourceWorkbook = WorkbookFactory.create(firstExcelFile);
        Sheet firstSheet = sourceWorkbook.getSheet(DAILY_THROUGHPUT_SOURCE_SHEET);

        destinationRowCount = 1;
        for (int i = 1; i < firstSheet.getPhysicalNumberOfRows(); i++) {
            Row currentRow = firstSheet.getRow(i);
            Cell dayCell = currentRow.getCell(DAY_COLUMN_INDEX);
            if (dayCell != null) {
                Double day = dayCell.getNumericCellValue();
                Row newDailyThroughputRow = destinationDailyThroughputSheet.createRow(destinationRowCount);
                Cell newDayCelL = newDailyThroughputRow.createCell(DAY_COLUMN_INDEX, CellType.NUMERIC);
                newDayCelL.setCellValue(day);
                destinationRowCount ++;
            }
        }
        sourceWorkbook.close();

        // Write the daily throughput values from each excel file
        for (int  i = 0; i < excelFiles.size(); i++) {
            File excelFile = excelFiles.get(i);
            sourceWorkbook = WorkbookFactory.create(excelFile);
            Sheet dailyThroughputSheet = sourceWorkbook.getSheet(DAILY_THROUGHPUT_SOURCE_SHEET);

            destinationRowCount = 1;
            for (int j = 1; j < dailyThroughputSheet.getPhysicalNumberOfRows(); j++) {
                Row currentRow = dailyThroughputSheet.getRow(j);
                Cell dailyThroughputCell = currentRow.getCell(DAILY_THROUGHPUT_INDEX);
                if (dailyThroughputCell != null) {
                    Double dailyThroughput = dailyThroughputCell.getNumericCellValue();

                    // Assumes row has already been created
                    // Write the data to destination sheet
                    Row destinationRow = destinationDailyThroughputSheet.getRow(destinationRowCount);
                    if (destinationRow == null) {
                        destinationRow = destinationDailyThroughputSheet.createRow(destinationRowCount);
                    }

                    Cell destinationThroughputCell = destinationRow.createCell(i + 1, CellType.NUMERIC);
                    destinationThroughputCell.setCellValue(dailyThroughput);

                    destinationRowCount ++;
                }
            }
            sourceWorkbook.close();
        }
        // End of writing daily product THROUGHPUT section

        // Create product output and worth sheet ===============================================================================
        Sheet destinationProductWorthSheet = destinationWorkbook.createSheet("PRODUCT_OUTPUT_WORTH");
        final String PRODUCT_WORTH_SOURCE_SHEET = "PRODUCT_OUTPUT_WORTH";
        final int PRODUCT_INDEX = 0;
        final int OUTPUT_INDEX = 1;
        final int WORTH_INDEX = 2;

        // Write column headers
        final String[] PRODUCT_WORTH_COLUMN_HEADERS = {"Run Type", "Product", "Output", "Worth"};
        headerRow = destinationProductWorthSheet.createRow(0);
        for (int i = 0; i < PRODUCT_WORTH_COLUMN_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i, CellType.STRING);
            cell.setCellValue(PRODUCT_WORTH_COLUMN_HEADERS[i]);
        }

        destinationRowCount = 1;
        for (File excelFile: excelFiles) {
            sourceWorkbook = WorkbookFactory.create(excelFile);
            Sheet productWorthSheet = sourceWorkbook.getSheet(PRODUCT_WORTH_SOURCE_SHEET);
            String runType = OutputAnalysisUtil.fileStringToFileName(excelFile.toString());

            for (int i = 1; i < productWorthSheet.getPhysicalNumberOfRows(); i++) {
                Row currentRow = productWorthSheet.getRow(i);
                Cell productCell = currentRow.getCell(PRODUCT_INDEX);
                Cell outputCell = currentRow.getCell(OUTPUT_INDEX);
                Cell worthCell = currentRow.getCell(WORTH_INDEX);

                if (productCell != null) {
                    String product = productCell.getStringCellValue();
                    Double output = outputCell.getNumericCellValue();
                    Double worth = worthCell.getNumericCellValue();

                    // Write to the destination sheet
                    Row newWorthRow = destinationProductWorthSheet.createRow(destinationRowCount);

                    Cell destinationRuntypeCell = newWorthRow.createCell(0, CellType.STRING);
                    destinationRuntypeCell.setCellValue(runType);

                    Cell destinationSProductCell = newWorthRow.createCell(1, CellType.STRING);
                    destinationSProductCell.setCellValue(product);

                    Cell destinationOutputCell = newWorthRow.createCell(2, CellType.NUMERIC);
                    destinationOutputCell.setCellValue(output);

                    Cell destinationWorthCell = newWorthRow.createCell(3, CellType.NUMERIC);
                    destinationWorthCell.setCellValue(worth);

                    destinationRowCount = destinationRowCount + 1;
                }
            }
            sourceWorkbook.close();
        }

        // Saves the workbook ==========================================================================================
        FileOutputStream outputStream = new FileOutputStream(destinationFile);
        destinationWorkbook.write(outputStream);
        outputStream.close();
        destinationWorkbook.close();
        LOGGER.info("SUCCESSFULLY generated Tableau Excel file at: " + destinationFile.toString());
    }

    /**
     * Generates the summary statistic for a single excel file.
     * Sample usage:
     * "
     * File outputExcelFile01 = new File("src/main/resources/sample-output-files/output-files-with-summary-data/output_min_size_20.xlsx");
     * appendSummaryStatisticsOfSingleOutputExcelFile(outputExcelFile01);
     * "
     * @param outputExcelFile Output excel file of a single simulation run.
     * @throws IOException
     */
    private void appendSummaryStatisticsOfSingleOutputExcelFile(File outputExcelFile) throws IOException {
        LOGGER.info("Starting output summary generation");

        // Creates a temp excel file for referencing
        File originalInputFile = outputExcelFile;
        File tempOutputFile = new File(originalInputFile.toString() + "temp.xlsx");
        OutputAnalysisUtil.copyFileUsingStream(originalInputFile, tempOutputFile);
        LOGGER.info("Successfully generated temporary copy of output file.");

        if (!tempOutputFile.exists()) {
            throw new IOException("File not found in: " + originalInputFile.toString());
        }

        TreeMap<String, Double> mapOfUtilizationRates = new TreeMap<String, Double>();
        Workbook workbook = WorkbookFactory.create(tempOutputFile);
        LOGGER.info("Successfully created Workbook from temporary copy of output file");

        try {
            // ==================   Get average utilization rates of IBIS Ovens ============================================
            final String UTIL_RES_REP = "Util Res Rep";
            Sheet utilSheet = workbook.getSheet(UTIL_RES_REP);

            TreeMap<String, Double> treeMapOfAverageUtilizationRates = null;
            if (utilSheet == null) {
                String fileName = OutputAnalysisUtil.fileStringToFileName(tempOutputFile.toString());
                LOGGER.info(fileName + " doesn't contain sheet " + UTIL_RES_REP);
                treeMapOfAverageUtilizationRates = new TreeMap<String, Double>();
            } else {
                treeMapOfAverageUtilizationRates = OutputAnalysisCalculation.calculateAverageIbisOvenUtilRate(utilSheet);
            }

            mapOfUtilizationRates.putAll(treeMapOfAverageUtilizationRates);
            // =========================== End of section on IBIS Oven utilization rates ===================================

            // ====================== Get cycle time data  ============================================================
            final String THROUGHPUT_PRODUCT_REP = "Throughput Product Rep";
            Sheet cycleTimeSheet = workbook.getSheet(THROUGHPUT_PRODUCT_REP);

            TreeMap<String, Double> treeMapOfProductToAverageCycleTimesFromThroughputProduct = null;

            if (cycleTimeSheet == null) {
                // throw new IOException("Excel file doesn't contain sheet: " + THROUGHPUT_PRODUCT_REP);
                String fileName = OutputAnalysisUtil.fileStringToFileName(tempOutputFile.toString());
                LOGGER.info(fileName + " doesn't contain sheet " + THROUGHPUT_PRODUCT_REP);
            } else {
                treeMapOfProductToAverageCycleTimesFromThroughputProduct = OutputAnalysisCalculation.calculateProductCycleTimeFromThroughputProduct(cycleTimeSheet);
            }
            // ====================== End of section on cycle time summary statistics =====================================

            // ===============================  Get throughput data ====================================================
            final String DAILY_THROUGHPUT_PRODUCT_REP = "Daily Throughput Product Rep";
            Sheet throughputSheet = workbook.getSheet(DAILY_THROUGHPUT_PRODUCT_REP);

            if (throughputSheet == null) {
                // throw new IOException("Excel file doesn't contain sheet: " + DAILY_THROUGHPUT_PRODUCT_REP);
                String fileName = OutputAnalysisUtil.fileStringToFileName(tempOutputFile.toString());
                LOGGER.info(fileName + " doesn't contain sheet " + DAILY_THROUGHPUT_PRODUCT_REP);
            }
            TreeMap<String, Double> treeMapOfProductToAverageThroughput = OutputAnalysisCalculation.calculateProductThroughput(throughputSheet);
            // ====================== End of section on throughput data ================================================

            //=============================== Get daily throughput =====================================================
            // Day is stored as a numeric double
            TreeMap<Double, Double> treeMapOfDayToOutput = OutputAnalysisCalculation.calculateDailyThroughput(throughputSheet);
            // ======================== End of section on daily throughput =============================================

            // ======================= Get cycle time from "Daily Throughput Product Rep" =====================================
            TreeMap<String, Double> treeMapOfProductToAverageCycleTimesFromDailyThroughput = OutputAnalysisCalculation.calculateProductCycleTimeFromDailyThroughput(throughputSheet);
            // ======================== End of cycle time section ==============================================================

            // Get product worth. Total worth = Output x Associated Cost
            File productCostFile = OutputAnalysisUtil.getProductKeyCostExcelFileFromRelativeDirectory();
            Workbook productCostWorkbook = WorkbookFactory.create(productCostFile);
            Sheet productCostSheet = productCostWorkbook.getSheetAt(0);
            //Sheet dailyProductThroughputSheet = workbook.getSheet(DAILY_THROUGHPUT_PRODUCT_REP);
            TreeMap<String, ArrayList<Double>> treeMapOfProductToOutputAndCost = OutputAnalysisCalculation.calculateTotalProductWorth(throughputSheet, productCostSheet);
            productCostWorkbook.close();
            productCostFile.delete();
            // End of section on calculating product throughput worth

            // Save all the data to their respective sheets =============================================================

            // =========================== Extract file name ie run specs ==============================================
            String runType = OutputAnalysisUtil.fileStringToFileName(originalInputFile.toString());

            // Saves the simulation run type and utilization rate to a new sheet
            OutputAnalysisUtil.saveRunTypeAndUtilizationRatesTONewSheet("RUN_TYPE_AND_IBIS_UTILIZATION", runType, mapOfUtilizationRates, workbook);

            // Saves the product cycle time to a new sheet
            OutputAnalysisUtil.saveProductCycleTimeToNewSheet("PRODUCT_STAY_TIME", treeMapOfProductToAverageCycleTimesFromThroughputProduct, workbook);

            // Saves the product throughput to a new sheet
            OutputAnalysisUtil.saveProductThroughputToNewSheet("PRODUCT_THROUGHPUT", treeMapOfProductToAverageThroughput, workbook);

            // Saves the daily output to a new sheet
            OutputAnalysisUtil.saveDailyOutputSheet("DAILY_OUTPUT", treeMapOfDayToOutput, workbook);

            // Save the product cycle time (from daily product throughput) to a new sheet
            OutputAnalysisUtil.saveProductCycleTimeFromDailyThroughputToNewSheet("PRODUCT_TIME_IN_SYSTEM", treeMapOfProductToAverageCycleTimesFromDailyThroughput, workbook );

            // Save the product output and worth to a new sheet
            OutputAnalysisUtil.saveProductOutputAndWorth("PRODUCT_OUTPUT_WORTH", treeMapOfProductToOutputAndCost, workbook);

            // Saves the current edited workbook by overwriting the original file
            FileOutputStream outputStream = new FileOutputStream(originalInputFile.toString());
            workbook.write(outputStream);
            outputStream.close();

            // Perform closing operations
            workbook.close();
            tempOutputFile.delete();
            LOGGER.info("Closed workbook and deleted temporary excel file.");
            LOGGER.info("SUCCESSFULLY generated Output statistics for " + originalInputFile.getName() + "\n" +
                                "=========================================================================================");
        } catch (CustomException e) {
            LOGGER.severe("EXCEPTION: " + e.getMessage() + ".");
            workbook.close();
            tempOutputFile.delete();
            LOGGER.info("Closed workbook and deleted temporary excel file.");
        }
    }
}
