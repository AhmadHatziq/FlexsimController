package com.nusinfineon.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import org.apache.commons.math3.stat.descriptive.rank.*;

import java.awt.geom.Arc2D;
import java.io.IOException;
import java.util.*;

import com.nusinfineon.exceptions.CustomException;

/**
 * This class provides the core logic and code that goes into processing the output excel file and generating relevant
 * summary statistics.
 */
public class OutputAnalysisCalculation {

    public static TreeMap<String, Double> calculateProductThroughput(Sheet throughputSheet) throws CustomException {
        try {
            // Declare column names used
            final String PRODUCT_ID = "Product";
            final String QTY_OUT = "Qty Out";
            final String TIME_IN_SYSTEM = "Time In System";

            // Get column index for required columns
            int qtyOutColumnIndex = -1;
            int productColumnIndex = -1;
            int timeColumnIndex = -1;
            Row headerRow = throughputSheet.getRow(0);
            for (int cellIndex = 0; cellIndex < headerRow.getPhysicalNumberOfCells(); cellIndex++) {
                String cellValue = headerRow.getCell(cellIndex).getStringCellValue();
                switch (cellValue) {
                    case PRODUCT_ID:
                        productColumnIndex = cellIndex;
                        break;
                    case QTY_OUT:
                        qtyOutColumnIndex = cellIndex;
                        break;
                    case TIME_IN_SYSTEM:
                        timeColumnIndex = cellIndex;
                        break;
                    default:
                        break;
                } // End of switch case block
            } // End of for loop block

            // Iterate through the rows and get counts and sum of throughput.
            // Throughput = Time-in-system / Qty out
            HashMap<String, Double> productThroughputs = new HashMap<>();
            HashMap<String, Integer> productRowCounts = new HashMap();
            for (int rowIndex = 1; rowIndex < throughputSheet.getPhysicalNumberOfRows(); rowIndex++) {
                Row currentRow = throughputSheet.getRow(rowIndex);
                Cell qtyCell = currentRow.getCell(qtyOutColumnIndex);

                if ( (qtyCell != null) ) { // To avoid empty or non-existing rows
                    Double qtyValue = qtyCell.getNumericCellValue();
                    Double timeInSystem = currentRow.getCell(timeColumnIndex).getNumericCellValue();
                    if ( (qtyValue > 0.0) && (timeInSystem > 0.0)) {
                        String product = currentRow.getCell(productColumnIndex).getStringCellValue();
                        Double throughput = qtyValue / timeInSystem;

                        if (productRowCounts.containsKey(product)) {
                            Integer currentCount = productRowCounts.get(product);
                            productRowCounts.put(product, currentCount + 1);
                        } else {
                            productRowCounts.put(product, 1);
                        }

                        if (productThroughputs.containsKey(product)) {
                            Double currentThroughput = productThroughputs.get(product);
                            productThroughputs.put(product, currentThroughput + throughput );
                        } else {
                            productThroughputs.put(product, throughput );
                        }

                    }
                } // End of If statement

            } // End of for loop

            // Get average throughputs for each product
            TreeMap<String, Double> productAverageThroughput = new TreeMap<>();
            for (String product: productRowCounts.keySet()) {
                Integer count = productRowCounts.get(product);
                Double throughputSum = productThroughputs.get(product);
                Double averageThroughput = throughputSum / count;
                productAverageThroughput.put(product, averageThroughput);
            }
            return productAverageThroughput;
        } catch (Exception e) {
            throw new CustomException(OutputAnalysisUtil.ExceptionToString(e));
        }
    }

    public static TreeMap<String, Double> calculateProductCycleTimeFromDailyThroughput(Sheet throughputSheet)
        throws CustomException {
        try {
            final String PRODUCT_ID = "Product";
            final String TIME_IN_SYSTEM = "Time In System";

            // Get column index for required columns
            int timeColumnIndex = -1;
            int productColumnIndex = -1;
            Row headerRow = throughputSheet.getRow(0);
            for (int cellIndex = 0; cellIndex < headerRow.getPhysicalNumberOfCells(); cellIndex++) {
                String cellValue = headerRow.getCell(cellIndex).getStringCellValue();
                switch (cellValue) {
                    case PRODUCT_ID:
                        productColumnIndex = cellIndex;
                        break;
                    case TIME_IN_SYSTEM:
                        timeColumnIndex = cellIndex;
                        break;
                    default:
                        break;
                } // End of switch case block
            } // End of for loop block

            // Obtain counts of product rows and sum up total "time in system".
            TreeMap<String, Integer> mapOfProductRowCounts = new TreeMap<>();
            TreeMap<String, Double> mapOfTimeInSystem = new TreeMap<>();
            for (int rowIndex = 1; rowIndex < throughputSheet.getPhysicalNumberOfRows(); rowIndex++) {
                Row currentRow = throughputSheet.getRow(rowIndex);
                Cell timeCell = currentRow.getCell(timeColumnIndex);

                if (timeCell != null) {
                    Double timeValue = timeCell.getNumericCellValue();
                    if ((timeValue > 0.0) ) {
                        String product = currentRow.getCell(productColumnIndex).getStringCellValue();
                        // Increment the count
                        if (mapOfProductRowCounts.containsKey(product)) {
                            Integer currentCount = mapOfProductRowCounts.get(product);
                            mapOfProductRowCounts.put(product, currentCount + 1);
                        } else {
                            mapOfProductRowCounts.put(product,  1);
                        }
                        // Sum up the total "Time in system"
                        if (mapOfTimeInSystem.containsKey(product)) {
                            Double currentTimeSum = mapOfTimeInSystem.get(product);
                            mapOfTimeInSystem.put(product, currentTimeSum + timeValue);
                        } else {
                            mapOfTimeInSystem.put(product, timeValue);
                        }
                    }
                }

            } // end of for loop

            // Get the average time-in-system for each product
            TreeMap<String, Double> mapOfProductToAverageTimeInSystem = new TreeMap<>();
            for (String product: mapOfProductRowCounts.keySet()) {
                Integer counts = mapOfProductRowCounts.get(product);
                Double timeSum = mapOfTimeInSystem.get(product);
                Double averageTimeInSystem = timeSum / counts;
                mapOfProductToAverageTimeInSystem.put(product, averageTimeInSystem);
            }

            return mapOfProductToAverageTimeInSystem;
        } catch (Exception e) {
            throw new CustomException(OutputAnalysisUtil.ExceptionToString(e));
        }
    }

    public static TreeMap<Double, Double> calculateDailyThroughput(Sheet throughputSheet)
        throws CustomException {
        try {
            final String PRODUCT_ID = "Product";
            final String QTY_OUT = "Qty Out";
            final String DAY = "Day";

            // Get column index for required columns
            int qtyOutColumnIndex = -1;
            int productColumnIndex = -1;
            int dayColumnIndex = -1;
            Row headerRow = throughputSheet.getRow(0);
            for (int cellIndex = 0; cellIndex < headerRow.getPhysicalNumberOfCells(); cellIndex++) {
                String cellValue = headerRow.getCell(cellIndex).getStringCellValue();
                switch (cellValue) {
                    case PRODUCT_ID:
                        productColumnIndex = cellIndex;
                        break;
                    case QTY_OUT:
                        qtyOutColumnIndex = cellIndex;
                        break;
                    case DAY:
                        dayColumnIndex = cellIndex;
                        break;
                    default:
                        break;
                } // End of switch case block
            } // End of for loop block

            // Iterate through the rows and get sum of quantity out for each day
            TreeMap<Double, Double> dailyOutputs = new TreeMap<>();

            for (int rowIndex = 1; rowIndex < throughputSheet.getPhysicalNumberOfRows(); rowIndex++) {
                Row currentRow = throughputSheet.getRow(rowIndex);
                Cell qtyCell = currentRow.getCell(qtyOutColumnIndex);

                if (qtyCell != null) {
                    Double qtyValue = qtyCell.getNumericCellValue();
                    if ((qtyValue > 0.0) ) {
                        Double day = currentRow.getCell(dayColumnIndex).getNumericCellValue();
                        if (dailyOutputs.containsKey(day)) {
                          Double currentOutput = dailyOutputs.get(day);
                          dailyOutputs.put(day, currentOutput + qtyValue);
                        } else {
                            dailyOutputs.put(day,  qtyValue);
                        }
                    }

                }

            } // end of for loop
            return dailyOutputs;


        } catch (Exception e) {
            throw new CustomException(OutputAnalysisUtil.ExceptionToString(e));
        }
    }

    public static TreeMap<String, Double> calculateProductCycleTimeFromThroughputProduct(Sheet cycleTimeSheet)
        throws CustomException {

        try {
            // Declare column name required
            final String cycleTimeColumnName = "StayTime Average (hr)";
            final String productColumnName = "Product";
            int cycleTimeIndex = -1;
            int prooductIndex = -1;

            // Get index which the column belongs to
            Row headerRow = cycleTimeSheet.getRow(0);
            for (int cellIndex = 0; cellIndex < headerRow.getPhysicalNumberOfCells(); cellIndex++) {
                Cell cell = headerRow.getCell(cellIndex);
                if (cell != null) {
                    String cellValue = headerRow.getCell(cellIndex).getStringCellValue();
                    switch (cellValue) {
                        case cycleTimeColumnName:
                            cycleTimeIndex = cellIndex;
                            break;
                        case productColumnName:
                            prooductIndex = cellIndex;
                        default:
                            break;
                    } // End of switch case block
                }
            }

            // Declare maps to store product id counts and
            TreeMap<String, Integer> productIDCounts = new TreeMap<String, Integer>();
            TreeMap<String, Double> productIDTotalCycleTime = new TreeMap<String, Double>();

            // Iterate through the rows and obtain the number of counts for each unique product ID and their cumulative CTs
            for (int rowIndex = 1; rowIndex < cycleTimeSheet.getPhysicalNumberOfRows(); rowIndex++) {
                Row currentRow = cycleTimeSheet.getRow(rowIndex);

                // Obtain number of counts for each product ID. End goal is to get the total for each product and get average CT for each product.
                if (currentRow.getCell(prooductIndex) != null) {
                    String productID = currentRow.getCell(prooductIndex).getStringCellValue();
                    if (productIDCounts.containsKey(productID)) {
                        Integer currentCount = productIDCounts.get(productID);
                        productIDCounts.put(productID, currentCount + 1);
                    } else {
                        productIDCounts.put(productID, 1);
                    }
                }

                // Obtain the cumulative total for each product ID
                if (currentRow.getCell(cycleTimeIndex) != null) {
                    String productID = currentRow.getCell(prooductIndex).getStringCellValue();
                    Double productCycleTimeAverage = currentRow.getCell(cycleTimeIndex).getNumericCellValue();
                    if (productIDTotalCycleTime.containsKey(productID)) {
                        Double currentTotal = productIDTotalCycleTime.get(productID);
                        productIDTotalCycleTime.put(productID, currentTotal + productCycleTimeAverage);
                    } else {
                        productIDTotalCycleTime.put(productID, productCycleTimeAverage);
                    }
                }

            } // End of for loop

            // Obtain the average cycle time for each product
            TreeMap<String, Double> averageCycleTimeForEachProduct = new TreeMap<String, Double>();

            for (String productID: productIDCounts.keySet()) {
                Integer productCounts = productIDCounts.get(productID);
                Double productTotal = productIDTotalCycleTime.get(productID);
                Double productAverageCT = productTotal / (double) productCounts;
                averageCycleTimeForEachProduct.put(productID, productAverageCT);
            }

            return averageCycleTimeForEachProduct;

        } catch (Exception e) {
            throw new CustomException(OutputAnalysisUtil.ExceptionToString(e));
        }

    }

    /**
     * Calculates the overall worth of products that have passed through.
     *
     * Logic: From the Daily Throughput sheet, looks at the product key and quantity out columns.
     * Sums up the overall product quantity outputted and multiplies it with the product's corresponding
     * cost in the product-cost table, a locally stored excel file.
     *
     * @param dailyThroughputSheet Daily Throughput sheet.
     * @param productCostSheet Product cost sheet.
     * @return
     */
    public static TreeMap<String, Double> calculateTotalProductWorth(Sheet dailyThroughputSheet, Sheet productCostSheet)
            throws CustomException {

        try {

            // Read from product-key-cost table and store data
            HashMap<String, Double> mapOfProductToCost = new HashMap<String, Double>();
            for (int rowIndex = 1; rowIndex < productCostSheet.getPhysicalNumberOfRows(); rowIndex++) {
                Row row = productCostSheet.getRow(rowIndex);
                String productKey = row.getCell(1).getStringCellValue();
                Double productCost = row.getCell(2).getNumericCellValue();
                mapOfProductToCost.put(productKey, productCost);
            }

            // Get Counts of Product that has been processed / outputted
            final String QTY_OUT_COLUMN = "Qty Out";
            final String PRODUCT_COLUMN = "Product";

            // Get column index for QTY_OUT_COLUMN index
            int qtyOutColumnIndex = -1;
            int productColumnIndex = -1;
            Row headerRow = dailyThroughputSheet.getRow(0);
            for (int cellIndex = 0; cellIndex < headerRow.getPhysicalNumberOfCells(); cellIndex++) {
                String cellValue = headerRow.getCell(cellIndex).getStringCellValue();
                switch (cellValue) {
                    case QTY_OUT_COLUMN:
                        qtyOutColumnIndex = cellIndex;
                        break;
                    case PRODUCT_COLUMN:
                        productColumnIndex = cellIndex;
                        break;
                    default:
                        break;
                } // End of switch case block
            } // End of for loop block

            // Get unique cumulative counts for each product
            HashMap<String, Double> mapOfProductOutCounts = new HashMap<String, Double>();
            for (int rowIndex = 1; rowIndex < dailyThroughputSheet.getPhysicalNumberOfRows(); rowIndex++) {
                Row row = dailyThroughputSheet.getRow(rowIndex);
                if (row.getCell(productColumnIndex) != null) {
                    String productKey = row.getCell(productColumnIndex).getStringCellValue().trim();
                    Double productQty = row.getCell(qtyOutColumnIndex).getNumericCellValue();

                    if (mapOfProductOutCounts.containsKey(productKey)) {
                        // Update entry
                        Double currentQty = mapOfProductOutCounts.get(productKey);
                        mapOfProductOutCounts.put(productKey, currentQty + productQty);
                    } else {
                        // Create a new entry
                        mapOfProductOutCounts.put(productKey, productQty);
                    }
                }
            } // End of fot loop

            // Get the total worth of products outputted.
            Double totalWorth = 0.0;
            for (String productKey : mapOfProductOutCounts.keySet()) {
                Double productCost = 0.0;
                // If an associated cost exists
                if (mapOfProductToCost.containsKey(productKey)) {
                    productCost = mapOfProductToCost.get(productKey);
                } else {
                    productCost = OutputAnalysisCalculation.getAverageCostOfProductHashMap(mapOfProductToCost);
                }

                Double productWorth = productCost * mapOfProductOutCounts.get(productKey);
                totalWorth += productWorth;
            }

            TreeMap<String, Double> totalWorthMap = new TreeMap<String, Double>();
            totalWorthMap.put("THROUGHPUT_WORTH", totalWorth);

           return totalWorthMap;

        } catch (Exception e) {
            throw new CustomException(OutputAnalysisUtil.ExceptionToString(e));
        }
    }

    public static TreeMap<String, Double> calculateAverageProductCycleTime(Sheet productCycleTimeSheet) throws CustomException {

        try {

            final String STAYTIME_AVERAGE_COLUMN = "StayTime Average (hr)";
            final String STAYTIME_MIN_COLUMN = "StayTime Min (hr)";
            final String STAYTIME_MAX_COLUMN = "StayTime Max (hr)";

            // Get index of column names and initialize counts
            HashMap<String, Integer> mapOfStayTimeToIndex = new HashMap<String, Integer>();
            HashMap<String, Long> mapOfStayTimeToCounts = new HashMap<String, Long>();
            Row headerRow = productCycleTimeSheet.getRow(0);
            for (int cellIndex = 0; cellIndex < headerRow.getPhysicalNumberOfCells(); cellIndex++) {
                String cellValue = headerRow.getCell(cellIndex).getStringCellValue();
                switch (cellValue) {
                    case STAYTIME_AVERAGE_COLUMN:
                        mapOfStayTimeToIndex.put(STAYTIME_AVERAGE_COLUMN, cellIndex);
                        mapOfStayTimeToCounts.put(STAYTIME_AVERAGE_COLUMN, Long.valueOf(0));
                        break;
                    case STAYTIME_MIN_COLUMN:
                        mapOfStayTimeToIndex.put(STAYTIME_MIN_COLUMN, cellIndex);
                        mapOfStayTimeToCounts.put(STAYTIME_MIN_COLUMN, Long.valueOf(0));
                        break;
                    case STAYTIME_MAX_COLUMN:
                        mapOfStayTimeToIndex.put(STAYTIME_MAX_COLUMN, cellIndex);
                        mapOfStayTimeToCounts.put(STAYTIME_MAX_COLUMN, Long.valueOf(0));
                        break;
                    default:
                        break;
                } // End of switch case block
            } // End of for loop block

            // Iterate through all rows and get the counts
            for (int rowIndex = 1; rowIndex < productCycleTimeSheet.getPhysicalNumberOfRows(); rowIndex++) {
                Row currentRow = productCycleTimeSheet.getRow(rowIndex);

                // Populate counts
                for (String columnName : mapOfStayTimeToIndex.keySet()) {
                    int columnIndex = mapOfStayTimeToIndex.get(columnName);

                    if (currentRow.getCell(columnIndex) != null) {
                        long cellValue = (long) (currentRow.getCell(columnIndex).getNumericCellValue());
                        long currentCount = mapOfStayTimeToCounts.get(columnName);
                        mapOfStayTimeToCounts.put(columnName, currentCount + cellValue);
                    }
                }

            }

            // Average out the Stay-Cycle Time
            TreeMap<String, Double> mapOfStayTimeToAverageStayTime = new TreeMap<String, Double>();
            for (String cycleTime : mapOfStayTimeToCounts.keySet()) {
                long currentValue = mapOfStayTimeToCounts.get(cycleTime);
                Double averageCycleTime = (double) currentValue / (double) (productCycleTimeSheet.getPhysicalNumberOfRows() - 1);
                cycleTime = "CYCLETIME_" + cycleTime.toUpperCase();
                mapOfStayTimeToAverageStayTime.put(cycleTime, averageCycleTime);
            }

            return mapOfStayTimeToAverageStayTime;

        } catch (Exception e) {
            throw new CustomException(OutputAnalysisUtil.ExceptionToString(e));
        }
    }

    /**
     * Obtains a hash map of Average Utilization Rates of IBIS Oven.
     *
     *  Logic: Extract Column Headers first. Then, filter by IBIS rows and sum up the cells corresponding to each
     *  header. Lastly, average out the numbers and only extract "averaged values" > 0.
     *
     * @param utilizationSheet Excel sheet that has the IBIS Utilization rates.
     * @return A hash map of utilization categories to their respective rates.
     * @throws IOException
     */
    public static TreeMap<String, Double> calculateAverageIbisOvenUtilRate(Sheet utilizationSheet) throws CustomException {

        try {

            // Hard code the columns that we are interested in.
            final String WAITING_FOR_OPERATOR = "waiting for operator";
            final String IDLE = "idle";
            final String PROCESSING = "processing";
            final String WAITING_FOR_TRANSPORTER = "waiting for transporter";
            final String SETUP = "setup";
            final String[] UTIL_COLUMNS  = new String[]{WAITING_FOR_OPERATOR, IDLE, PROCESSING, SETUP, WAITING_FOR_TRANSPORTER};
            List<String> UTIL_COLUMNS_LIST = Arrays.asList(UTIL_COLUMNS);

            // Get column names and corresponding index. Assumes first 2 columns are "platform" and "name"
            HashMap<Integer, String> mapOfIndexToColumnName = new HashMap<Integer, String>();
            HashMap<String, Double> mapOfColumnNameToTotalUtilRate = new HashMap<String, Double>();
            Row headerRow = utilizationSheet.getRow(0);
            for (int cellIndex = 2; cellIndex < headerRow.getPhysicalNumberOfCells(); cellIndex++) {
                String columnName = headerRow.getCell(cellIndex).getStringCellValue();
                mapOfIndexToColumnName.put(cellIndex, columnName);
                mapOfColumnNameToTotalUtilRate.put(columnName, 0.0);
            }

            // Get all rows regarding IBIS.
            ArrayList<Row> ibisRows = new ArrayList<Row>();
            for (int rowIndex = 1; rowIndex < utilizationSheet.getPhysicalNumberOfRows(); rowIndex++) {

                Row currentRow = utilizationSheet.getRow(rowIndex);

                if (currentRow.getCell(0) != null) {

                    String platformType = currentRow.getCell(0).getStringCellValue();

                    if ((platformType != null) && (platformType.equals("IBIS"))) {
                        ibisRows.add(currentRow);
                    }

                }
            }

            // Get the average utilization rate for each row
            for (Row ibisRow : ibisRows) {
                // Increment the counts for each row.
                for (Integer index : mapOfIndexToColumnName.keySet()) {
                    String columnName = mapOfIndexToColumnName.get(index);
                    Double cellValue = ibisRow.getCell(index).getNumericCellValue();
                    Double currentUtilRate = mapOfColumnNameToTotalUtilRate.get(columnName);
                    mapOfColumnNameToTotalUtilRate.put(columnName, currentUtilRate + cellValue);
                }
            }
            TreeMap<String, Double> mapOfColumnNameToAverageUtilRate = new TreeMap<>();
            for (String columnName : mapOfColumnNameToTotalUtilRate.keySet()) {
                Double rateSum = mapOfColumnNameToTotalUtilRate.get(columnName);
                if (UTIL_COLUMNS_LIST.contains(columnName)) { // Only select the columns that have been pre-determined to be > 0 for IBIS rows.
                    Double averageRate = rateSum / ibisRows.size();
                    mapOfColumnNameToAverageUtilRate.put("UTILIZATION_RATE_" + columnName.toUpperCase(), averageRate);
                }
            }

            return mapOfColumnNameToAverageUtilRate;

        } catch (Exception e) {
            throw new CustomException(OutputAnalysisUtil.ExceptionToString(e));
        }
    }

    public static TreeMap<String, Double> calculateThroughputBasedOnThroughputByResource(Sheet throughputResourceSheet)
            throws CustomException {

        try {

            final String PLATFORM = "Platform";
            final String EQUIPMENT = "Equipment";
            final String TOTAL_INPUT = "Total Input";
            final String TOTAL_OUTPUT = "Total Output";

            // Obtain column indexes
            HashMap<String, Integer> mapOfColumnsToIndex = new HashMap<String, Integer>();

            Row headerRow = throughputResourceSheet.getRow(0);
            for (int cellIndex = 0; cellIndex < headerRow.getPhysicalNumberOfCells(); cellIndex++) {
                String cellValue = headerRow.getCell(cellIndex).getStringCellValue();
                switch (cellValue) {
                    case PLATFORM:
                        mapOfColumnsToIndex.put(PLATFORM, cellIndex);
                        break;
                    case EQUIPMENT:
                        mapOfColumnsToIndex.put(EQUIPMENT, cellIndex);
                        break;
                    case TOTAL_INPUT:
                        mapOfColumnsToIndex.put(TOTAL_INPUT, cellIndex);
                        break;
                    case TOTAL_OUTPUT:
                        mapOfColumnsToIndex.put(TOTAL_OUTPUT, cellIndex);
                        break;
                    default:
                        break;
                } // End of switch case block
            } // End of for loop block

            // FlexSim has already summarized the outputs and inputs of each resource.
            // The summarized values have a "-" in their EQUIPMENT Column
            // Iterate through all rows and look for a "-".
            TreeMap<String, Double> mapOfPlatformToTotalThroughput = new TreeMap<String, Double>();
            for (int rowIndex = 1; rowIndex < throughputResourceSheet.getPhysicalNumberOfRows(); rowIndex++) {
                Row currentRow = throughputResourceSheet.getRow(rowIndex);

                // Checks if product cell is valid
                if ( (currentRow.getCell(mapOfColumnsToIndex.get(EQUIPMENT)) != null) &&  (currentRow.getCell(mapOfColumnsToIndex.get(EQUIPMENT)).getStringCellValue().equals("-")) ) {
                    // Extract the value and store in a hash map
                    String platformType = currentRow.getCell(mapOfColumnsToIndex.get(PLATFORM)).getStringCellValue();
                    Double platformTotalInput = currentRow.getCell(mapOfColumnsToIndex.get(TOTAL_INPUT)).getNumericCellValue();
                    Double platformTotalOutput = currentRow.getCell(mapOfColumnsToIndex.get(TOTAL_OUTPUT)).getNumericCellValue();

                    switch (platformType) {
                        case "IBIS":
                            mapOfPlatformToTotalThroughput.put("THROUGHPUT_FROM_FLEXSIM_IBIS_TOTAL_INPUT", platformTotalInput);
                            mapOfPlatformToTotalThroughput.put("THROUGHPUT_FROM_FLEXSIM_IBIS_TOTAL_OUTPUT", platformTotalOutput);
                            break;
                        case "ETM":
                            mapOfPlatformToTotalThroughput.put("THROUGHPUT_FROM_FLEXSIM_ETN_TOTAL_INPUT", platformTotalInput);
                            mapOfPlatformToTotalThroughput.put("THROUGHPUT_FROM_FLEXSIM_ETN_TOTAL_OUTPUT", platformTotalOutput);
                            break;
                        case "MIS":
                            mapOfPlatformToTotalThroughput.put("THROUGHPUT_FROM_FLEXSIM_MIS_TOTAL_INPUT", platformTotalInput);
                            mapOfPlatformToTotalThroughput.put("THROUGHPUT_FROM_FLEXSIM_ETN_TOTAL_OUTPUT", platformTotalOutput);
                            break;
                        case "JTS":
                            mapOfPlatformToTotalThroughput.put("THROUGHPUT_FROM_FLEXSIM_JTS_TOTAL_INPUT", platformTotalInput);
                            mapOfPlatformToTotalThroughput.put("THROUGHPUT_FROM_FLEXSIM_JTS_TOTAL_OUTPUT", platformTotalOutput);
                            break;
                        default:
                            break;
                    } // End of switch case

                } // End of if statement
            } // End of for loop
            return mapOfPlatformToTotalThroughput;

        } catch (Exception e) {
            throw new CustomException(OutputAnalysisUtil.ExceptionToString(e));
        }

    }

    //TODO: Take average
    public static TreeMap<String, Double> calculateThroughputBasedOnDailyThroughputByProduct(Sheet dailyThroughputProductSheet)
            throws CustomException {

        try {

            final String QTY_IN = "Qty In";
            final String QTY_OUT = "Qty Out";
            final String LOTS_IN = "Lots In";
            final String LOTS_OUT = "Lots Out";
            final String PRODUCT = "Product";

            // Get index of column names and initialize counts
            HashMap<String, Integer> mapOfColumnsToIndex = new HashMap<String, Integer>();
            TreeMap<String, Double> mapOfCounts = new TreeMap<String, Double>();
            int productColumnIndex = -1;

            Row headerRow = dailyThroughputProductSheet.getRow(0);
            for (int cellIndex = 0; cellIndex < headerRow.getPhysicalNumberOfCells(); cellIndex++) {
                String cellValue = headerRow.getCell(cellIndex).getStringCellValue();
                switch (cellValue) {
                    case QTY_IN:
                        mapOfColumnsToIndex.put(QTY_IN, cellIndex);
                        mapOfCounts.put(QTY_IN, Double.valueOf(0));
                        break;
                    case QTY_OUT:
                        mapOfColumnsToIndex.put(QTY_OUT, cellIndex);
                        mapOfCounts.put(QTY_OUT, Double.valueOf(0));
                        break;
                    case LOTS_IN:
                        mapOfColumnsToIndex.put(LOTS_IN, cellIndex);
                        mapOfCounts.put(LOTS_IN, Double.valueOf(0));
                        break;
                    case LOTS_OUT:
                        mapOfColumnsToIndex.put(LOTS_OUT, cellIndex);
                        mapOfCounts.put(LOTS_OUT, Double.valueOf(0));
                        break;
                    case PRODUCT:
                        productColumnIndex = cellIndex;
                    default:
                        break;
                } // End of switch case block
            } // End of for loop block

            // Iterate through all rows and get the counts
            for (int rowIndex = 1; rowIndex < dailyThroughputProductSheet.getPhysicalNumberOfRows(); rowIndex++) {
                Row currentRow = dailyThroughputProductSheet.getRow(rowIndex);

                // Checks if product cell is valid
                if (currentRow.getCell(productColumnIndex) != null) {
                    // Populate counts
                    for (String columnName : mapOfColumnsToIndex.keySet()) {
                        int columnIndex = mapOfColumnsToIndex.get(columnName);
                        double cellValue = (currentRow.getCell(columnIndex).getNumericCellValue());
                        double currentCount = mapOfCounts.get(columnName);
                        mapOfCounts.put(columnName, currentCount + cellValue);
                    }
                }
            }

            TreeMap<String, Double> mapOfCountsFinal = new TreeMap<String, Double>();
            // Renames entries in the map
            for (String columnName : mapOfCounts.keySet()) {
                String newColumnName = "THROUGHPUT_BY_PRODUCT_SHEET_" + columnName.toUpperCase();
                double count = mapOfCounts.get(columnName);
                mapOfCountsFinal.put(newColumnName, count);
            }

            return mapOfCountsFinal;

        } catch (Exception e) {
            throw new CustomException(OutputAnalysisUtil.ExceptionToString(e));
        }

    }

    /**
     *
     *  Logic: Looks at the input and output for the whole factory.
     *  Input = "Load/Burn In/Transfer Normal Qty" + "Load/Burn In/Transfer YRTP Qty"
     *  Output = "Unload Normal Qty" + "Unload YRTP Qty"
     *
     * @param dailyThroughputResourceSheet
     * @return
     */
    public static TreeMap<String, Double> calculateThroughputBasedOnDailyThroughputByResource(Sheet dailyThroughputResourceSheet)
            throws CustomException {

        try {

            final String LOAD_NORMAL_COLUMN = "Load/Burn In/Transfer Normal Qty";
            final String LOAD_YRTP_COLUMN = "Load/Burn In/Transfer YRTP Qty";
            final String UNLOAD_NORMAL_COLUMN = "Unload Normal Qty";
            final String UNLOAD_YRTP_COLUMN = "Unload YRTP Qty";

            // Get index of column names and initialize counts
            HashMap<String, Integer> mapOfThroughputToIndex = new HashMap<String, Integer>();
            HashMap<String, Long> mapOfThroughputToCounts = new HashMap<String, Long>();
            Row headerRow = dailyThroughputResourceSheet.getRow(0);
            for (int cellIndex = 0; cellIndex < headerRow.getPhysicalNumberOfCells(); cellIndex++) {
                String cellValue = headerRow.getCell(cellIndex).getStringCellValue();
                switch (cellValue) {
                    case LOAD_NORMAL_COLUMN:
                        mapOfThroughputToIndex.put(LOAD_NORMAL_COLUMN, cellIndex);
                        mapOfThroughputToCounts.put(LOAD_NORMAL_COLUMN, Long.valueOf(0));
                        break;
                    case LOAD_YRTP_COLUMN:
                        mapOfThroughputToIndex.put(LOAD_YRTP_COLUMN, cellIndex);
                        mapOfThroughputToCounts.put(LOAD_YRTP_COLUMN, Long.valueOf(0));
                        break;
                    case UNLOAD_NORMAL_COLUMN:
                        mapOfThroughputToIndex.put(UNLOAD_NORMAL_COLUMN, cellIndex);
                        mapOfThroughputToCounts.put(UNLOAD_NORMAL_COLUMN, Long.valueOf(0));
                        break;
                    case UNLOAD_YRTP_COLUMN:
                        mapOfThroughputToIndex.put(UNLOAD_YRTP_COLUMN, cellIndex);
                        mapOfThroughputToCounts.put(UNLOAD_YRTP_COLUMN, Long.valueOf(0));
                        break;
                    default:
                        break;
                } // End of switch case block
            } // End of for loop block

            //TODO: Filter by IBIS Machines only

            // Iterate through all rows and get the counts
            for (int rowIndex = 1; rowIndex < dailyThroughputResourceSheet.getPhysicalNumberOfRows(); rowIndex++) {
                Row currentRow = dailyThroughputResourceSheet.getRow(rowIndex);

                // Populate counts
                for (String columnName : mapOfThroughputToIndex.keySet()) {
                    int columnIndex = mapOfThroughputToIndex.get(columnName);
                    long cellValue = (long) (currentRow.getCell(columnIndex).getNumericCellValue());
                    long currentCount = mapOfThroughputToCounts.get(columnName);
                    mapOfThroughputToCounts.put(columnName, currentCount + cellValue);
                }
            }

            // Sum up to input and output
            TreeMap<String, Double> mapOfInputAndOutput = new TreeMap<String, Double>();
            mapOfInputAndOutput.put("THROUGHPUT_BY_RESOURCE_TOTAL_INPUT", (double) mapOfThroughputToCounts.get(LOAD_NORMAL_COLUMN)
                    + mapOfThroughputToCounts.get(LOAD_YRTP_COLUMN));
            mapOfInputAndOutput.put("THROUGHPUT_BY_RESOURCE_TOTAL_OUTPUT", (double) mapOfThroughputToCounts.get(UNLOAD_NORMAL_COLUMN)
                    + mapOfThroughputToCounts.get(UNLOAD_YRTP_COLUMN));

            return mapOfInputAndOutput;

        } catch (Exception e) {
            throw new CustomException(OutputAnalysisUtil.ExceptionToString(e));
        }
    }

    /**
     * Calculates the average cost (value) of a String-Double hash map.
     * @param mapOfProductToCost Hash map of product to cost.
     * @return Double.
     */
    public static Double getAverageCostOfProductHashMap(HashMap<String, Double> mapOfProductToCost) {
        Double sum = 0.0;
        for (String product: mapOfProductToCost.keySet()) {
            sum += mapOfProductToCost.get(product);
        }
        return (sum / mapOfProductToCost.size());
    }
}
