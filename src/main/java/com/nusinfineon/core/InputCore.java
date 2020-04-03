package com.nusinfineon.core;

import static com.nusinfineon.util.Directories.INPUT_EXCEL_SHEETS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.nusinfineon.core.input.LotEntry.GenericLotEntry;
import com.nusinfineon.core.input.LotEntry.LotEntry;
import com.nusinfineon.core.input.LotEntry.MJLotEntry;
import com.nusinfineon.core.input.LotEntry.SPTLotEntry;
import com.nusinfineon.exceptions.CustomException;
import com.nusinfineon.util.LotSequencingRule;

/**
 * Class represents the core functionality involved in interfacing with a Microsoft Excel document for the
 * purposes of setting the lot sequencing rule, varying the batch size and inputting settings.
 */
public class InputCore {

    private static final String MASTER_XLSX_FILE_NAME = "temp_master_input_";
    private static final String PRODUCT_INFO_SHEET_NAME = "Product Info and Eqpt Matrix";
    private static final String MIN_BIB_COLUMN_NAME = "BIB Slot Utilization Min";

    private static final String LOT_INFO_SHEET_NAME = "Actual Lot Info";
    private static final int LOT_INFO_LOT_COLUMN = 0;
    private static final int LOT_INFO_PRODUCT_KEY_COLUMN = 1;
    private static final int LOT_INFO_LOTSIZE_COLUMN = 2;
    private static final int LOT_INFO_PRODUCTION_LOCATION_COLUMN = 3;
    private static final int LOT_INFO_PERIOD_COLUMN = 4;
    private static final int LOT_INFO_NEW_COLUMN = 5;

    private static final String PROCESS_TIME_SHEET_NAME = "Process Time";
    private static final int PROCESS_TIME_PRODUCT_KEY_COLUMN = 0;
    private static final int PROCESS_TIME_PROCESS_TYPE_COLUMN = 1;
    private static final String PROCESS_TIME_PROCESS_TYPE_BURNIN = "Burn In";
    private static final int PROCESS_TIME_COLUMN = 6;

    private static final String SETTINGS_SHEET_NAME = "Settings";
    private static final int SETTINGS_PARAMETER_COLUMN = 0;
    private static final int SETTINGS_VALUE_COLUMN = 1;
    private static final String BATCH_SIZE_PARAMETER = "Burn In BIB Batch Size";
    private static final String RESOURCE_SELECT_CRITERIA_PARAMETER = "Resource Select Criteria";
    private static final String LOT_SELECTION_CRITERIA_PARAMETER = "Lot Selection Criteria for Loading";
    private static final String TROLLEY_LOCATION_SELECT_CRITERIA_PARAMETER = "Trolley Location Select Criteria";
    private static final String BIB_LOAD_ON_LOT_CRITERIA_PARAMETER = "BIB Load on Lot Criteria";

    private static final int MAX_ALLOWABLE_BATCH_SIZE = 24;

    private static final Logger LOGGER = Logger.getLogger(InputCore.class.getName());

    private File originalInputExcelFile;
    private File tempCopyOriginalInputExcelFile;
    private ArrayList<Integer> listOfMinBatchSizes;
    private ArrayList<File> excelInputFiles;
    private HashMap<LotSequencingRule, Boolean> lotSequencingRules;
    private String resourceSelectCriteria;
    private String lotSelectionCriteria;
    private String trolleyLocationSelectCriteria;
    private String bibLoadOnLotCriteria;

    /**
     * Creates an object from the user defined Strings.
     */
    public InputCore(String excelFilePath, HashMap<LotSequencingRule, Boolean> lotSequencingRules,
                     String batchSizeMinString, String batchSizeMaxString, String batchSizeStepString,
                     String resourceSelectCriteria, String lotSelectionCriteria,
                     String trolleyLocationSelectCriteria, String bibLoadOnLotCriteria){

        this.originalInputExcelFile = new File(excelFilePath);
        int minBatchSize = Integer.parseInt(batchSizeMinString);
        int maxBatchSize = Integer.parseInt(batchSizeMaxString);
        int batchStep = Integer.parseInt(batchSizeStepString);

        this.lotSequencingRules = lotSequencingRules;

        // Calculates the exact batch size needed and adds to an array.
        this.listOfMinBatchSizes = new ArrayList<>();
        for (int i = minBatchSize; i <= maxBatchSize; i = batchStep + i) {
            listOfMinBatchSizes.add(i);
        }

        this.resourceSelectCriteria = resourceSelectCriteria;
        this.lotSelectionCriteria = lotSelectionCriteria;
        this.trolleyLocationSelectCriteria = trolleyLocationSelectCriteria;
        this.bibLoadOnLotCriteria = bibLoadOnLotCriteria;

        this.excelInputFiles = new ArrayList<>();
    } // End of Constructor

    /**
     * Main execute function of InputCore to process input files
     * @throws IOException
     * @throws CustomException
     */
    public ArrayList<File> execute() throws IOException, CustomException {

        checkValidInputFile();

        // Generate single random seed for randomise lot sequencing rule
        long randomSeed = new Random().nextLong();

        // Iterate through rules
        for (Map.Entry<LotSequencingRule, Boolean> rule : lotSequencingRules.entrySet()) {
            // If rule is selected
            if (rule.getValue()) {
                // Iterate through batch sizes
                for (int minBatchSize : listOfMinBatchSizes) {
                    LOGGER.info("Writing temp Input excel file for min batch size " + minBatchSize
                            + ", " + rule.getKey().toString());

                    createCopyOfInputFile(); // Uses the copy of the input file as a reference
                    LOGGER.info("Successfully created a copy of main Input excel file. Now processing...");

                    // Create the workbook from a copy of the original excel file
                    Workbook workbook = WorkbookFactory.create(this.tempCopyOriginalInputExcelFile);

                    // Edit batch size
                    editMinBatchSize(workbook, minBatchSize);

                    // Lot sequencing on Actual Lot Info
                    processLotSequencing(workbook, rule.getKey(), randomSeed);

                    // Edit settings
                    editSettings(workbook);

                    // Saves the workbook and close the stream
                    String fileName = rule.getKey().toString() + "_" + minBatchSize + "_min_size_";
                    File singleBatchExcelFileDestination = Files.createTempFile(fileName, ".xlsx").toFile();
                    FileOutputStream outputStream = new FileOutputStream(singleBatchExcelFileDestination.toString());
                    workbook.write(outputStream);
                    workbook.close();
                    LOGGER.info("Successfully processed " + singleBatchExcelFileDestination.toString());

                    // Adds the file into the array
                    excelInputFiles.add(singleBatchExcelFileDestination);
                } // End of for-loop for batch sizes
            }
        } // End of for-loop for sequencing rules

        return excelInputFiles;
    } // End of execute method

    /**
     * Checks if input file is valid.
     * @throws IOException
     * @throws CustomException
     */
    private void checkValidInputFile() throws IOException, CustomException {
        Workbook workbook = WorkbookFactory.create(this.originalInputExcelFile);
        ArrayList<String> missingSheets = new ArrayList<>();

        // Check for missing sheets
        for (String sheet : INPUT_EXCEL_SHEETS) {
            if (workbook.getSheet(sheet) == null) {
                missingSheets.add(sheet);
            }
        }

        // Show missing sheets as exception message
        if (!missingSheets.isEmpty()) {
            StringBuilder message = new StringBuilder("The following sheets are missing from the input Excel file:\n");
                for (String sheet : missingSheets) {
                    message.append(sheet).append("\n");
                }
            workbook.close();
            throw new CustomException(message.toString());
        }

        workbook.close();
    }

    /**
     * Creates a copy of the master file and stores it in a temporary working directory.
     * Sample directory is: 'C:\Users\hatzi\AppData\Local\Temp\temp_master_input5569509737681448400.xlsx'.
     * @throws IOException
     */
    private void createCopyOfInputFile() throws IOException {
        Path masterTempFilePath = Files.createTempFile(MASTER_XLSX_FILE_NAME, ".xlsx");
        this.tempCopyOriginalInputExcelFile = masterTempFilePath.toFile();
        copyFileUsingStream(originalInputExcelFile, tempCopyOriginalInputExcelFile);
    }

    /**
     * Copies a file from source to destination using an input stream.
     * @param source Source file
     * @param destination Destination file
     * @throws IOException
     */
    private static void copyFileUsingStream(File source, File destination) throws IOException {
        try (InputStream is = new FileInputStream(source); OutputStream os = new FileOutputStream(destination)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }

    /**
     * Edits min batch size in Input excel file based on given batch size
     * @param workbook Workbook to edit
     * @param batchNumber Batch size
     * @throws CustomException
     */
    private void editMinBatchSize(Workbook workbook, int batchNumber) throws CustomException {
        // Access Product Info & Eqpt Matrix sheet
        Sheet productInfoSheet = workbook.getSheet(PRODUCT_INFO_SHEET_NAME);

        // Obtain the column index matching the column name
        int columnIndex = -1;
        Row firstRow = productInfoSheet.getRow(0);
        for (int cellIndex = 0; cellIndex < firstRow.getPhysicalNumberOfCells(); cellIndex++) {
            Cell cell = firstRow.getCell(cellIndex);
            if (cell.getStringCellValue().trim().equals(MIN_BIB_COLUMN_NAME)) {
                columnIndex = cellIndex;
            }
        }

        if (columnIndex == -1) {
            throw new CustomException("Column " + MIN_BIB_COLUMN_NAME + " not found in excel");
        }

        // Set the cell values to the batch number for all the rows
        for (int rowIndex = 1; rowIndex < productInfoSheet.getPhysicalNumberOfRows(); rowIndex++ ) {
            Row row = productInfoSheet.getRow(rowIndex);
            Cell cell = row.getCell(columnIndex);
            cell.setCellType(CellType.NUMERIC);
            cell.setCellValue(batchNumber);
        }
    }

    /**
     * Processes the lot sequencing on Actual Lot Info sheet
     * @param workbook Workbook to edit
     */
    private void processLotSequencing(Workbook workbook, LotSequencingRule rule, long seed) {
        // Access Actual Lot Info sheet
        Sheet lotInfoSheet = workbook.getSheet(LOT_INFO_SHEET_NAME);

        ArrayList<LotEntry> lotList = new ArrayList<>();

        switch (rule) {
        case SPT:
            // Access Process Time sheet
            Sheet processTimeSheet = workbook.getSheet(PROCESS_TIME_SHEET_NAME);
            // Get list sorted by shortest processing time
            lotList = shortestProcessingTime(lotInfoSheet, processTimeSheet);
            break;
        case MJ:
            // Get list sorted by most jobs
            lotList = mostJobs(lotInfoSheet);
            break;
        case RAND:
            // Get list sorted randomly
            lotList = randomSequence(lotInfoSheet, seed);
            break;
        default:
            break;
        }

        if (!rule.equals(LotSequencingRule.FCFS)) {
            // Edit Actual Lot Info sheet with sorted list
            editActualLotInfoList(lotList, lotInfoSheet);
        }
    }

    /**
     * Gets all lots and sorts by Shortest Processing Time per period
     * @param lotInfoSheet Actual Lot Info sheet
     * @param processTimeSheet Process Time sheet
     * @return Sorted list of lots
     */
    private ArrayList<LotEntry> shortestProcessingTime(Sheet lotInfoSheet, Sheet processTimeSheet) {
        ArrayList<LotEntry> lotList = new ArrayList<>();

        // Copy corresponding burn-in processing time from Process Time sheet to a new column in Actual Lot Info
        for (Row lotInfoRow : lotInfoSheet) {
            String productKey = lotInfoRow.getCell(LOT_INFO_PRODUCT_KEY_COLUMN).getStringCellValue().trim();
            for (Row processTimeRow : processTimeSheet) {
                String processTimeProductKey =
                        processTimeRow.getCell(PROCESS_TIME_PRODUCT_KEY_COLUMN).getStringCellValue().trim();
                if (isBurnInTime(processTimeRow) && processTimeProductKey.equals(productKey)) {
                    double processTime = processTimeRow.getCell(PROCESS_TIME_COLUMN).getNumericCellValue();
                    lotInfoRow.createCell(LOT_INFO_NEW_COLUMN).setCellValue(processTime);
                }
            }
        }

        double currentPeriod = 0.0;
        ArrayList<LotEntry> subLotList = new ArrayList<>();
        for (Row lotInfoRow : lotInfoSheet) {
            if (lotInfoRow.getCell(LOT_INFO_LOT_COLUMN) == null ||
                    lotInfoRow.getCell(LOT_INFO_LOT_COLUMN).getCellType() == CellType.BLANK) {
                break;
            }
            if (lotInfoRow.getRowNum() != 0) {
                if (currentPeriod != lotInfoRow.getCell(LOT_INFO_PERIOD_COLUMN).getNumericCellValue()) {
                    Collections.sort(subLotList);
                    lotList.addAll(subLotList);
                    subLotList = new ArrayList<>();
                }
                LotEntry entry = new SPTLotEntry(lotInfoRow.getCell(LOT_INFO_LOT_COLUMN).getStringCellValue().trim(),
                        lotInfoRow.getCell(LOT_INFO_PRODUCT_KEY_COLUMN).getStringCellValue().trim(),
                        lotInfoRow.getCell(LOT_INFO_LOTSIZE_COLUMN).getNumericCellValue(),
                        lotInfoRow.getCell(LOT_INFO_PRODUCTION_LOCATION_COLUMN).getStringCellValue().trim(),
                        lotInfoRow.getCell(LOT_INFO_PERIOD_COLUMN).getNumericCellValue(),
                        lotInfoRow.getCell(LOT_INFO_NEW_COLUMN).getNumericCellValue());
                subLotList.add(entry);
                currentPeriod = lotInfoRow.getCell(LOT_INFO_PERIOD_COLUMN).getNumericCellValue();
            }
        }
        Collections.sort(subLotList);
        lotList.addAll(subLotList);

        return lotList;
    }

    /**
     * Gets all lots and sorts randomly per period
     * @param lotInfoSheet Actual Lot Info sheet
     * @return Sorted list of lots
     */
    private ArrayList<LotEntry> mostJobs(Sheet lotInfoSheet) {
        ArrayList<LotEntry> lotList = new ArrayList<>();
        double currentPeriod = 0.0;
        ArrayList<LotEntry> subLotList = new ArrayList<>();
        for (Row lotInfoRow : lotInfoSheet) {
            if (lotInfoRow.getCell(LOT_INFO_LOT_COLUMN) == null ||
                    lotInfoRow.getCell(LOT_INFO_LOT_COLUMN).getCellType() == CellType.BLANK) {
                break;
            }
            if (lotInfoRow.getRowNum() != 0) {
                if (currentPeriod != lotInfoRow.getCell(LOT_INFO_PERIOD_COLUMN).getNumericCellValue()) {
                    Collections.sort(subLotList, Collections.reverseOrder());
                    lotList.addAll(subLotList);
                    subLotList = new ArrayList<>();
                }
                LotEntry entry = new MJLotEntry(lotInfoRow.getCell(LOT_INFO_LOT_COLUMN).getStringCellValue().trim(),
                        lotInfoRow.getCell(LOT_INFO_PRODUCT_KEY_COLUMN).getStringCellValue().trim(),
                        lotInfoRow.getCell(LOT_INFO_LOTSIZE_COLUMN).getNumericCellValue(),
                        lotInfoRow.getCell(LOT_INFO_PRODUCTION_LOCATION_COLUMN).getStringCellValue().trim(),
                        lotInfoRow.getCell(LOT_INFO_PERIOD_COLUMN).getNumericCellValue());
                subLotList.add(entry);
                currentPeriod = lotInfoRow.getCell(LOT_INFO_PERIOD_COLUMN).getNumericCellValue();
            }
        }
        Collections.sort(subLotList, Collections.reverseOrder());
        lotList.addAll(subLotList);

        return lotList;
    }

    /**
     * Gets all lots and sorts randomly per period
     * @param lotInfoSheet Actual Lot Info sheet
     * @return Sorted list of lots
     */
    private ArrayList<LotEntry> randomSequence(Sheet lotInfoSheet, long seed) {
        ArrayList<LotEntry> lotList = new ArrayList<>();
        double currentPeriod = 0.0;
        ArrayList<LotEntry> subLotList = new ArrayList<>();
        for (Row lotInfoRow : lotInfoSheet) {
            if (lotInfoRow.getCell(LOT_INFO_LOT_COLUMN) == null ||
                    lotInfoRow.getCell(LOT_INFO_LOT_COLUMN).getCellType() == CellType.BLANK) {
                break;
            }
            if (lotInfoRow.getRowNum() != 0) {
                if (currentPeriod != lotInfoRow.getCell(LOT_INFO_PERIOD_COLUMN).getNumericCellValue()) {
                    Collections.shuffle(subLotList, new Random(seed));
                    lotList.addAll(subLotList);
                    subLotList = new ArrayList<>();
                }
                LotEntry entry = new GenericLotEntry(lotInfoRow.getCell(LOT_INFO_LOT_COLUMN).getStringCellValue().trim(),
                        lotInfoRow.getCell(LOT_INFO_PRODUCT_KEY_COLUMN).getStringCellValue().trim(),
                        lotInfoRow.getCell(LOT_INFO_LOTSIZE_COLUMN).getNumericCellValue(),
                        lotInfoRow.getCell(LOT_INFO_PRODUCTION_LOCATION_COLUMN).getStringCellValue().trim(),
                        lotInfoRow.getCell(LOT_INFO_PERIOD_COLUMN).getNumericCellValue());
                subLotList.add(entry);
                currentPeriod = lotInfoRow.getCell(LOT_INFO_PERIOD_COLUMN).getNumericCellValue();
            }
        }
        Collections.shuffle(subLotList, new Random(seed));
        lotList.addAll(subLotList);

        return lotList;
    }

    /**
     * Clears Actual Lot Info sheet and repopulates with newly sorted list
     * @param lotList Sorted lot list
     * @param lotInfoSheet Actual Lot Info sheet
     */
    private void editActualLotInfoList(ArrayList<LotEntry> lotList, Sheet lotInfoSheet) {
        // Clear list
        for (int i = lotInfoSheet.getLastRowNum(); i > 0; i--) {
            lotInfoSheet.removeRow(lotInfoSheet.getRow(i));
        }

        // Populate sorted list
        int newRow = 1;
        for (LotEntry lotEntry : lotList) {
            Row lotInfoRow = lotInfoSheet.createRow(newRow);
            lotInfoRow.createCell(LOT_INFO_LOT_COLUMN).setCellValue(lotEntry.getLot());
            lotInfoRow.createCell(LOT_INFO_PRODUCT_KEY_COLUMN).setCellValue(lotEntry.getProduct());
            lotInfoRow.createCell(LOT_INFO_LOTSIZE_COLUMN).setCellValue(lotEntry.getLotSize());
            lotInfoRow.createCell(LOT_INFO_PRODUCTION_LOCATION_COLUMN).setCellValue(lotEntry.getProductionLocation());
            lotInfoRow.createCell(LOT_INFO_PERIOD_COLUMN).setCellValue(lotEntry.getPeriod());
            // TODO: Remove this line when not needed (For checking only):
            // lotInfoRow.createCell(LOT_INFO_NEW_COLUMN).setCellValue(lotEntry.getComparable());
            newRow++;
        }
    }

    /**
     * Edit settings in Input excel file
     * @param workbook Workbook to edit
     */
    private void editSettings(Workbook workbook) {
        // Access Settings sheet
        Sheet settingsSheet = workbook.getSheet(SETTINGS_SHEET_NAME);

        // Edit required setting
        for (Row row : settingsSheet) {
            Cell cell = row.getCell(SETTINGS_PARAMETER_COLUMN);
            String parameter = cell.getStringCellValue().trim();
            switch (parameter) {
                case BATCH_SIZE_PARAMETER:
                    row.getCell(SETTINGS_VALUE_COLUMN).setCellValue(MAX_ALLOWABLE_BATCH_SIZE);
                    break;
                case RESOURCE_SELECT_CRITERIA_PARAMETER:
                    row.getCell(SETTINGS_VALUE_COLUMN).setCellValue(
                            getResourceSelectCriteria(this.resourceSelectCriteria));
                    break;
                case LOT_SELECTION_CRITERIA_PARAMETER:
                    row.getCell(SETTINGS_VALUE_COLUMN).setCellValue(Integer.parseInt(this.lotSelectionCriteria));
                    break;
                case TROLLEY_LOCATION_SELECT_CRITERIA_PARAMETER:
                    row.getCell(SETTINGS_VALUE_COLUMN).setCellValue(
                            Integer.parseInt(this.trolleyLocationSelectCriteria));
                    break;
                case BIB_LOAD_ON_LOT_CRITERIA_PARAMETER:
                    row.getCell(SETTINGS_VALUE_COLUMN).setCellValue(Integer.parseInt(this.bibLoadOnLotCriteria));
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Check if entry in Process Time sheet is of process type "Burn In"
     * @param processTimeRow Given entry
     * @return True or False
     */
    private boolean isBurnInTime(Row processTimeRow) {
        String processTimeProcessType =
                processTimeRow.getCell(PROCESS_TIME_PROCESS_TYPE_COLUMN).getStringCellValue().trim();
        return processTimeProcessType.equals(PROCESS_TIME_PROCESS_TYPE_BURNIN);
    }

    /**
     * Get required input string for Resource Select Criteria from provided index string
     * @param resourceSelectCriteria Index string
     * @return Input string
     */
    private String getResourceSelectCriteria(String resourceSelectCriteria) {
        switch (resourceSelectCriteria) {
        case "2":
            return "ShortestQueue";
        case "3":
            return "ShortestDistance";
        case "4":
            return "ShortestQueue,ShortestDistance";
        default:
            return "";
        }
    }
}
