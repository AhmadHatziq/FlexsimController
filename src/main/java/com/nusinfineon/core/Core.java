package com.nusinfineon.core;

import static com.nusinfineon.util.Directories.INPUT_FOLDER_NAME;
import static com.nusinfineon.util.Directories.OUTPUT_FOLDER_NAME;
import static com.nusinfineon.util.Directories.RAW_OUTPUT_FOLDER_NAME;
import static com.nusinfineon.util.Directories.TABLEAU_WORKBOOK_NAME;
import static com.nusinfineon.util.Directories.TABLEAU_WORKBOOK_SOURCE_DIR;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.nusinfineon.Main;
import com.nusinfineon.exceptions.CustomException;
import com.nusinfineon.util.LotSequencingRule;

public class Core {

    private String flexsimLocation;
    private String modelLocation;
    private String inputLocation;
    private String outputLocation;
    private String runSpeed;
    private String stopTime;
    private HashMap<LotSequencingRule, Boolean> lotSequencingRules;
    private String batchSizeMinString;
    private String batchSizeMaxString;
    private String batchSizeStepString;
    private String resourceSelectCriteria;
    private String lotSelectionCriteria;
    private String trolleyLocationSelectCriteria;
    private String bibLoadOnLotCriteria;
    private boolean isModelShown;
    private ArrayList<File> excelInputFiles;
    private ArrayList<File> excelOutputFiles;

    private static final Logger LOGGER = Logger.getLogger(Core.class.getName());
    private static final Boolean INIT_FCFS = true;
    private static final Boolean INIT_SPT = false;
    private static final Boolean INIT_MJ = false;
    private static final Boolean INIT_RAND = false;
    private static final String INIT_RUN_SPEED = "4";
    private static final String INIT_STOP_TIME = "1140";
    private static final String INIT_MAX_BATCH_SIZE = "24";
    private static final String INIT_MIN_BATCH_SIZE = "20";
    private static final String INIT_STEP_SIZE = "1";
    private static final String INIT_RESOURCE_SELECT_CRITERIA = "4";
    private static final String INIT_LOT_SELECTION_CRITERIA = "3";
    private static final String INIT_TROLLEY_LOCATION_SELECT_CRITERIA = "2";
    private static final String INIT_BIB_LOAD_ON_LOT_CRITERIA = "2";

    /**
     * Main execute function to generate input files. run model and generate output file
     */
    public void execute() throws IOException, CustomException {
        // Initialise InputCore for creation of excel files for runs iteration
        InputCore inputCore = new InputCore(inputLocation, lotSequencingRules, batchSizeMinString,
                batchSizeMaxString, batchSizeStepString, resourceSelectCriteria, lotSelectionCriteria,
                trolleyLocationSelectCriteria, bibLoadOnLotCriteria);

        // Initialise RunCore for running of simulation
        RunCore runCore = new RunCore(flexsimLocation, modelLocation, outputLocation, runSpeed, stopTime, isModelShown);

        // Initialise OutputCore for handling output analysis
        OutputCore outputCore = new OutputCore();

        handleInput(inputCore);

        handleRuns(runCore);

        handleOutput(outputCore);

        // Clear lists of files
        excelInputFiles.clear();
        excelOutputFiles.clear();

        // Close Excel program
        Runtime.getRuntime().exec("cmd /c taskkill /f /im excel.exe");
    }

    /**
     * Used to handle processing of input
     */
    private void handleInput(InputCore inputCore) throws IOException, CustomException {
        try {
            excelInputFiles = inputCore.execute();
        } catch (IOException e) {
            LOGGER.severe("Unable to create files");
            throw new CustomException("Error in creating temp files");
        } catch (CustomException e) {
            LOGGER.severe(e.getMessage());
            throw e;
        }

        // Handle input files
        File inputFile = new File(inputLocation);
        String inputPathName = inputFile.getParent();
        Path inputDir = Paths.get(inputPathName, INPUT_FOLDER_NAME);

        // Clean Input folder if exists, else create
        if (Files.exists(inputDir)) {
            LOGGER.info("Input directory already exists");
            FileUtils.cleanDirectory(new File(inputDir.toString()));
            LOGGER.info("Input directory cleaned");
        } else {
            Files.createDirectory(inputDir);
            LOGGER.info("Input directory created");
        }

        // Move generated input files into Input folder
        for (File file : excelInputFiles) {
            String newFileName = file.getName().substring(0, file.getName().lastIndexOf("_")) + "_input.xlsx";
            FileUtils.copyFile(file, new File(inputDir + "/" + newFileName));
            LOGGER.info(newFileName + " created in input folder");
        }
    }

    /**
     * Used to handle simulation runs
     */
    private void handleRuns(RunCore runCore) {
        excelOutputFiles = runCore.executeRuns(excelInputFiles);
    }

    /**
     * Used to handle processing and analysis of output
     */
    private void handleOutput(OutputCore outputCore) throws IOException, CustomException {
        // Handle output files
        File outputFile = new File(outputLocation);
        String outputPathName = outputFile.getParent();
        Path outputDir = Paths.get(outputPathName, OUTPUT_FOLDER_NAME);
        Path rawOutputDir = Paths.get(outputPathName, OUTPUT_FOLDER_NAME, RAW_OUTPUT_FOLDER_NAME);

        // Create Output folder if not exist
        if (Files.exists(outputDir)) {
            LOGGER.info("Output directory already exists");
        } else {
            Files.createDirectory(outputDir);
            LOGGER.info("Output directory created");
        }

        // Clean Raw Output folder if exists, else create
        if (Files.exists(rawOutputDir)) {
            LOGGER.info("Raw output directory already exists");
            FileUtils.cleanDirectory(new File(rawOutputDir.toString()));
            LOGGER.info("Raw output directory cleaned");
        } else {
            Files.createDirectory(rawOutputDir);
            LOGGER.info("Raw output directory created");
        }

        // Move raw output files into Raw Output folder
        for (File file : excelOutputFiles) {
            String fileName = file.getName();
            Files.move(Paths.get(String.valueOf(file)), Paths.get(String.valueOf(rawOutputDir), fileName),
                    StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info(fileName + " moved into Raw Output folder");
        }

        // Output Analysis
        File folderDirectory = new File(String.valueOf(rawOutputDir));
        File destinationDirectory = new File(String.valueOf(outputDir));

        if (folderDirectory.list().length > 0) {
            // Execute outputCore to generate output summaries and tableau-excel-file
            outputCore.execute(folderDirectory, destinationDirectory);

            // Copy Tableau workbook from resources to Output folder
            try {
                File newFile = new File(destinationDirectory + "/" + TABLEAU_WORKBOOK_NAME);
                URL file = Main.class.getResource(TABLEAU_WORKBOOK_SOURCE_DIR + "/" + TABLEAU_WORKBOOK_NAME);
                FileUtils.copyURLToFile(file, newFile);
                LOGGER.info(TABLEAU_WORKBOOK_NAME + " moved into Output folder");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new CustomException("Raw Output Excel Files folder is empty!");
        }

        // Open Tableau workbook
        try {
            Desktop.getDesktop().open(new File(destinationDirectory + "/" + TABLEAU_WORKBOOK_NAME));
        } catch (IOException e) {
            LOGGER.info("Tableau application not installed or failed to launch! Tableau workbook open aborted.");
        } catch (IllegalArgumentException e) {
            LOGGER.info("Tableau workbook not found! Tableau workbook open aborted.");
        }
    }

    /**
     * Used to store data into core before execute and save (the json parser serializes it)
     */
    public void inputData(String flexsimLocation, String modelLocation, String inputLocation, String outputLocation,
                          String runSpeed, String stopTime, boolean isModelShown,
                          HashMap<LotSequencingRule, Boolean> lotSequencingRules,
                          String batchSizeMinString, String batchSizeMaxString, String batchSizeStepString,
                          String resourceSelectCriteria, String lotSelectionCriteria,
                          String trolleyLocationSelectCriteria, String bibLoadOnLotCriteria) {
        this.flexsimLocation = flexsimLocation;
        this.modelLocation = modelLocation;
        this.inputLocation = inputLocation;
        this.outputLocation = outputLocation;
        this.runSpeed = runSpeed;
        this.stopTime = stopTime;
        this.isModelShown = isModelShown;

        this.lotSequencingRules = lotSequencingRules;

        this.batchSizeMinString = batchSizeMinString;
        this.batchSizeMaxString = batchSizeMaxString;
        this.batchSizeStepString = batchSizeStepString;

        this.resourceSelectCriteria = resourceSelectCriteria;
        this.lotSelectionCriteria = lotSelectionCriteria;
        this.trolleyLocationSelectCriteria = trolleyLocationSelectCriteria;
        this.bibLoadOnLotCriteria = bibLoadOnLotCriteria;
    }

    public String getFlexsimLocation() {
        return flexsimLocation;
    }

    public String getModelLocation() {
        return modelLocation;
    }

    public String getInputLocation() {
        return inputLocation;
    }

    public String getOutputLocation() {
        return outputLocation;
    }

    public String getRunSpeed() {
        if (runSpeed == null) {
            return INIT_RUN_SPEED;
        } else {
            return runSpeed;
        }
    }

    public String getStopTime() {
        if (stopTime == null) {
            return INIT_STOP_TIME;
        } else {
            return stopTime;
        }
    }

    public boolean getIsModelShown() {
        return isModelShown;
    }

    public HashMap<LotSequencingRule, Boolean> getLotSequencingRules() {
        if (lotSequencingRules == null) {
            lotSequencingRules = new HashMap<LotSequencingRule, Boolean>();
            lotSequencingRules.put(LotSequencingRule.FCFS, INIT_FCFS);
            lotSequencingRules.put(LotSequencingRule.SPT, INIT_SPT);
            lotSequencingRules.put(LotSequencingRule.MJ, INIT_MJ);
            lotSequencingRules.put(LotSequencingRule.RAND, INIT_RAND);
        }
        return lotSequencingRules;
    }

    public String getBatchSizeMinString() {
        if (batchSizeMinString == null) {
            return INIT_MIN_BATCH_SIZE;
        } else {
            return batchSizeMinString;
        }
    }

    public String getBatchSizeMaxString() {
        if (batchSizeMaxString == null) {
            return INIT_MAX_BATCH_SIZE;
        } else {
            return batchSizeMaxString;
        }
    }

    public String getBatchSizeStepString() {
        if (batchSizeMinString == null) {
            return INIT_STEP_SIZE;
        } else {
            return batchSizeStepString;
        }
    }

    public String getResourceSelectCriteria() {
        if (resourceSelectCriteria == null) {
            return INIT_RESOURCE_SELECT_CRITERIA;
        } else {
            return resourceSelectCriteria;
        }
    }

    public String getLotSelectionCriteria() {
        if (lotSelectionCriteria == null) {
            return INIT_LOT_SELECTION_CRITERIA;
        } else {
            return lotSelectionCriteria;
        }
    }

    public String getTrolleyLocationSelectCriteria() {
        if (trolleyLocationSelectCriteria == null) {
            return INIT_TROLLEY_LOCATION_SELECT_CRITERIA;
        } else {
            return trolleyLocationSelectCriteria;
        }
    }

    public String getBibLoadOnLotCriteria() {
        if (bibLoadOnLotCriteria == null) {
            return INIT_BIB_LOAD_ON_LOT_CRITERIA;
        } else {
            return bibLoadOnLotCriteria;
        }
    }
}
