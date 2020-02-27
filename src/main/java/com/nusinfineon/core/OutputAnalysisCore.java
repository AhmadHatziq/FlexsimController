package com.nusinfineon.core;

import com.nusinfineon.exceptions.CustomException;
import com.nusinfineon.util.OutputAnalysisCalculation;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.nusinfineon.util.OutputAnalysisUtil;

public class OutputAnalysisCore {

    private final static Logger LOGGER = Logger.getLogger(OutputAnalysisCore.class.getName());

    public static void main(String[] args) throws IOException {
        // Declare output file and cost file paths
        String productKeyCostExcelFilePath = "C:\\Users\\Ahmad\\Documents\\NUS\\IE 3100M\\Data Files\\product_key_cost.xlsx";
        String outputExcelFilePath1 = "C:\\Users\\Ahmad\\Documents\\NUS\\IE 3100M\\Data Files\\output_files_with_summary\\output_min_size_20.xlsx";
        String outputExcelFilePath2 = "C:\\Users\\Ahmad\\Documents\\NUS\\IE 3100M\\Data Files\\output_files_with_summary\\output_min_size_21.xlsx";
        String outputExcelFilePath3 = "C:\\Users\\Ahmad\\Documents\\NUS\\IE 3100M\\Data Files\\output_files_with_summary\\output_min_size_22.xlsx";
        String outputExcelFilePath4 = "C:\\Users\\Ahmad\\Documents\\NUS\\IE 3100M\\Data Files\\output_files_with_summary\\output_min_size_23.xlsx";
        String outputExcelFilePath5 = "C:\\Users\\Ahmad\\Documents\\NUS\\IE 3100M\\Data Files\\output_files_with_summary\\output_min_size_24.xlsx";
        String outputExcelFilePath6 = "C:\\Users\\Ahmad\\Documents\\NUS\\IE 3100M\\Data Files\\output_files_with_summary\\output_shortest_queue_resource_selection.xlsx";

        // Execute the summary statistics. Summary data will be appended to the output excel file.
        getOutputSummaryStatistics(outputExcelFilePath1, productKeyCostExcelFilePath);
        getOutputSummaryStatistics(outputExcelFilePath2, productKeyCostExcelFilePath);
        getOutputSummaryStatistics(outputExcelFilePath3, productKeyCostExcelFilePath);
        getOutputSummaryStatistics(outputExcelFilePath4, productKeyCostExcelFilePath);
        getOutputSummaryStatistics(outputExcelFilePath5, productKeyCostExcelFilePath);
        getOutputSummaryStatistics(outputExcelFilePath6, productKeyCostExcelFilePath);

    }

    public static void getOutputSummaryStatistics(String outputExcelFilePath, String productKeyCostExcelFilePath) throws IOException {
        LOGGER.info("Starting output summary generation");

        // Creates a temp excel file for referencing
        File originalInputFile = new File(outputExcelFilePath);
        File tempOutputFile = new File(outputExcelFilePath + "temp.xlsx");
        OutputAnalysisUtil.copyFileUsingStream(originalInputFile, tempOutputFile);
        LOGGER.info("Successfully generated temporary copy of output file.");

        if (!tempOutputFile.exists()) {
            throw new IOException("File not found in: " + outputExcelFilePath);
        }

        TreeMap<String, Double> mapOfSummaryStatistics = new TreeMap<String, Double>();
        Workbook workbook = WorkbookFactory.create(tempOutputFile);
        LOGGER.info("Successfully created Workbook from temporary copy of output file");

        //TODO: Implement check if all sheets are present in the workbook

        try {

            // ==================   Get average utilization rates of IBIS Ovens ============================================
            final String UTIL_RES_REP = "Util Res Rep";
            Sheet utilSheet = workbook.getSheet(UTIL_RES_REP);
            if (utilSheet == null) {
                throw new IOException("Excel file doesn't contain sheet: " + UTIL_RES_REP);
            }
            TreeMap<String, Double> treeMapOfAverageUtilizationRates = OutputAnalysisCalculation.calculateAverageIbisOvenUtilRate(utilSheet);
            mapOfSummaryStatistics.putAll(treeMapOfAverageUtilizationRates);
            // OutputAnalysisUtil.saveStringDoubleHashMapToNewSheet("IBIS AVG UTIL SUMMARY", treeMapOfAverageUtilizationRates, workbook);
            // =========================== End of section on IBIS Oven utilization rates ===================================

            // =============================== Get Product throughput from Daily throughput Resource =======================
            final String DAILY_THROUGHPUT_RES_REP = "Daily Throughput Res Rep";
            Sheet dailyThroughputSheet = workbook.getSheet(DAILY_THROUGHPUT_RES_REP);
            if (dailyThroughputSheet == null) {
                throw new IOException("Excel file doesn't contain sheet: " + DAILY_THROUGHPUT_RES_REP);
            }
            TreeMap<String, Double> treeMapOfSummarizedDailyThroughputByResource = OutputAnalysisCalculation.calculateThroughputBasedOnDailyThroughputByResource(dailyThroughputSheet);
            mapOfSummaryStatistics.putAll(treeMapOfSummarizedDailyThroughputByResource);
            // OutputAnalysisUtil.saveStringDoubleHashMapToNewSheet("THROUGHPUT FROM DAILY", treeMapOfSummarizedDailyThroughputByResource, workbook);
            // =========================== End of section on Summarizing Daily Throughput ==================================

            // =============================== Get average cycle time of products ==========================================
            final String THROUGHPUT_PRODUCT_REP = "Throughput Product Rep";
            Sheet productCycleTimeSheet = workbook.getSheet(THROUGHPUT_PRODUCT_REP);
            TreeMap<String, Double> treeMapOfAverageProductCycleTime = OutputAnalysisCalculation.calculateAverageProductCycleTime(productCycleTimeSheet);
            mapOfSummaryStatistics.putAll(treeMapOfAverageProductCycleTime);
            // OutputAnalysisUtil.saveStringDoubleHashMapToNewSheet("AVERAGE CYCLE TIME", treeMapOfAverageProductCycleTime, workbook);
            // =============================== End of Cycle Time Calculation ===============================================

            // =============================== Get value of throughput =====================================================

            // Read the sheet from product-cost excel file
            final String DAILY_THROUGHPUT_PRODUCT_REP = "Daily Throughput Product Rep";
            File productCostFile = new File(productKeyCostExcelFilePath);
            Workbook productCostWorkbook = WorkbookFactory.create(productCostFile);
            Sheet productCostSheet = productCostWorkbook.getSheetAt(0);
            Sheet dailyProductThroughputSheet = workbook.getSheet(DAILY_THROUGHPUT_PRODUCT_REP);
            TreeMap<String, Double> treeMapOfTotalThroughputWorth = OutputAnalysisCalculation.calculateTotalProductWorth(dailyProductThroughputSheet, productCostSheet);
            productCostWorkbook.close();
            mapOfSummaryStatistics.putAll(treeMapOfTotalThroughputWorth);
            // OutputAnalysisUtil.saveStringDoubleHashMapToNewSheet("TOTAL WORTH", treeMapOfTotalThroughputWorth, workbook);
            // =========================== End of throughput worth calculation =============================================

            // =========================== Get Product Throughput from Daily Throughput Product ============================
            //final String DAILY_THROUGHPUT_PRODUCT_REP = "Daily Throughput Product Rep";
            TreeMap<String, Double> treeMapOfSummarizedDailyThroughputByProduct = OutputAnalysisCalculation.calculateThroughputBasedOnDailyThroughputByProduct(dailyProductThroughputSheet);
            mapOfSummaryStatistics.putAll(treeMapOfSummarizedDailyThroughputByProduct);
            // OutputAnalysisUtil.saveStringLongHashMapToNewSheet("THROUGHPUT FROM PRODUCT", treeMapOfSummarizedDailyThroughputByProduct, workbook);
            // =========================== End of Product Throughput from Daily Throughput Product =========================

            // ========================== Get Product Throughput from "Throughput Res Rep" =================================
            final String THROUGHPUT_RES_REP = "Throughput Res Rep";
            Sheet throughputResourceSheet = workbook.getSheet(THROUGHPUT_RES_REP);
            TreeMap<String, Double> treeMapOfSummarizedThroughputByFlexsim = OutputAnalysisCalculation.calculateThroughputBasedOnThroughputByResource(throughputResourceSheet);
            mapOfSummaryStatistics.putAll(treeMapOfSummarizedThroughputByFlexsim);
            // OutputAnalysisUttreeMapOfSummarizedDailyThroughputByProductil.saveStringDoubleHashMapToNewSheet("THROUGHPUT FROM RES FLEXSIM", treeMapOfSummarizedThroughputByFlexsim, workbook);

            // =========================== Extract run data remarks to a new sheet ==============================================
            String runType = OutputAnalysisUtil.fileStringToFileName(originalInputFile.toString());

            // Saves all the extracted information to a new sheet.
            OutputAnalysisUtil.saveOverallOutputDataToNewSheet("OVERALL_SUMMARY", runType, mapOfSummaryStatistics, workbook);

            // Saves the current edited workbook by overwriting the original file
            FileOutputStream outputStream = new FileOutputStream(outputExcelFilePath);
            workbook.write(outputStream);
            outputStream.close();

            // Perform closing operations
            workbook.close();
            tempOutputFile.delete();
            LOGGER.info("Closed workbook and deleted temporary excel file.");

            LOGGER.info("Output statistics for " + originalInputFile.toString() + " generated successfully");

        } catch (CustomException e) {

            LOGGER.severe("EXCEPTION: " + e.getMessage() + ".");

            workbook.close();
            tempOutputFile.delete();
            LOGGER.info("Closed workbook and deleted temporary excel file.");

        }

    }

}

