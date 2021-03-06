package org.openforis.collect.io.metadata.samplingdesign;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.io.exception.ParsingException;
import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.collect.io.metadata.parsing.ParsingError.ErrorType;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.process.AbstractProcess;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.ReferenceDataSchema;
import org.openforis.idm.metamodel.ReferenceDataSchema.ReferenceDataDefinition;
import org.openforis.idm.metamodel.ReferenceDataSchema.SamplingPointDefinition;

/**
 * 
 * @author S. Ricci
 * 
 */
public class SamplingDesignImportProcess extends AbstractProcess<Void, SamplingDesignImportStatus> {

	private static final String SURVEY_NOT_FOUND_ERROR_MESSAGE_KEY = "samplingDesignImport.error.surveyNotFound";
	private static final String IMPORTING_FILE_ERROR_MESSAGE_KEY = "samplingDesignImport.error.internalErrorImportingFile";
	
	private static Log LOG = LogFactory.getLog(SamplingDesignImportProcess.class);
	
	private static final String CSV = "csv";

	private SamplingDesignManager samplingDesignManager;
	private SurveyManager surveyManager;
	private File file;
	private boolean overwriteAll;
	
	private SamplingDesignCSVReader reader;
	private String errorMessage;
	private List<SamplingDesignLine> lines;

	private CollectSurvey survey;
	
	public SamplingDesignImportProcess(SamplingDesignManager samplingDesignManager, SurveyManager surveyManager, 
			CollectSurvey survey, File file, boolean overwriteAll) {
		super();
		this.samplingDesignManager = samplingDesignManager;
		this.surveyManager = surveyManager;
		this.survey = survey;
		this.file = file;
		this.overwriteAll = overwriteAll;
	}
	
	@Override
	public void init() {
		super.init();
		lines = new ArrayList<SamplingDesignLine>();
		validateParameters();
	}

	protected void validateParameters() {
		if ( ! file.exists() && ! file.canRead() ) {
			status.error();
			status.setErrorMessage(IMPORTING_FILE_ERROR_MESSAGE_KEY);
		} else if ( survey == null ) {
			status.error();
			status.setErrorMessage(SURVEY_NOT_FOUND_ERROR_MESSAGE_KEY);
		}
	}
	
	@Override
	protected void initStatus() {
		status = new SamplingDesignImportStatus();
	}

	@Override
	public void startProcessing() throws Exception {
		super.startProcessing();
		String fileName = file.getName();
		String extension = FilenameUtils.getExtension(fileName);
		if ( CSV.equalsIgnoreCase(extension) ) {
			parseCSVLines(file);
//		} else if (ZIP.equals(extension) ) {
//			processPackagedFile();
//			status.complete();
		} else {
			errorMessage = "File type not supported" + extension;
			status.setErrorMessage(errorMessage);
			status.error();
			LOG.error("Species import: " + errorMessage);
		}
		if ( status.isRunning() ) {
			processLines();
		}
		if ( status.isRunning() && ! status.hasErrors() ) {
			persistSamplingDesign();
		} else {
			status.error();
		}
		if ( status.isRunning() ) {
			status.complete();
		}
	}

	protected void parseCSVLines(File file) {
		long currentRowNumber = 0;
		try {
			reader = new SamplingDesignCSVReader(file);
			reader.init();
			status.addProcessedRow(1);
			status.setTotal(reader.size());
			currentRowNumber = 2;
			while ( status.isRunning() ) {
				try {
					SamplingDesignLine line = reader.readNextLine();
					if ( line != null ) {
						lines.add(line);
					}
					if ( ! reader.isReady() ) {
						break;
					}
				} catch (ParsingException e) {
					status.addParsingError(currentRowNumber, e.getError());
				} finally {
					currentRowNumber ++;
				}
			}
			status.setTotal(reader.getLinesRead() + 1);
		} catch (ParsingException e) {
			status.error();
			status.addParsingError(1, e.getError());
		} catch (Exception e) {
			status.error();
			status.addParsingError(currentRowNumber, new ParsingError(ErrorType.IOERROR, e.getMessage()));
			LOG.error("Error importing sampling design CSV file", e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}
	
	protected void processLines() {
		for (SamplingDesignLine line : lines) {
			long lineNumber = line.getLineNumber();
			if ( status.isRunning() && ! status.isRowProcessed(lineNumber) && ! status.isRowInError(lineNumber) ) {
				try {
					boolean processed = processLine(line);
					if (processed ) {
						status.addProcessedRow(lineNumber);
					}
				} catch (ParsingException e) {
					status.addParsingError(lineNumber, e.getError());
				}
			}
		}
	}
	
	protected boolean processLine(SamplingDesignLine line) throws ParsingException {
		SamplingDesignLineValidator validator = SamplingDesignLineValidator.createInstance(survey);
		validator.validate(line);
		List<ParsingError> errors = validator.getErrors();
		for (ParsingError error : errors) {
			status.addParsingError(error);
		}
		checkDuplicateLine(line);
		return true;
	}

	protected void checkDuplicateLine(SamplingDesignLine line) throws ParsingException {
		for (SamplingDesignLine currentLine : lines) {
			if ( currentLine.getLineNumber() != line.getLineNumber() ) {
				if ( isDuplicateLocation(line, currentLine) ) {
					throwDuplicateLineException(line, currentLine, SamplingDesignFileColumn.LOCATION_COLUMNS);
				} else if ( line.getLevelCodes().equals(currentLine.getLevelCodes()) ) {
					SamplingDesignFileColumn lastLevelCol = SamplingDesignFileColumn.LEVEL_COLUMNS[line.getLevelCodes().size() - 1];
					throwDuplicateLineException(line, currentLine, new SamplingDesignFileColumn[]{lastLevelCol});
				}
			}
		}
	}
	
	protected boolean isDuplicateLocation(SamplingDesignLine line1, SamplingDesignLine line2) throws ParsingException {
		List<String> line1LevelCodes = line1.getLevelCodes();
		List<String> line2LevelCodes = line2.getLevelCodes();
		if ( line1.hasEqualLocation(line2) ) {
			if ( line2LevelCodes.size() == line1LevelCodes.size()) {
				return true;
			} else {
				int minLevelPosition = Math.min(line1LevelCodes.size(), line2LevelCodes.size());
				List<String> firstLevelCodes1 = line1LevelCodes.subList(0, minLevelPosition);
				List<String> firstLevelCodes2 = line2LevelCodes.subList(0, minLevelPosition);
				if ( ! firstLevelCodes1.equals(firstLevelCodes2) ) {
					return true;
				}
			}
		}
		return false;
	}

	protected void throwDuplicateLineException(SamplingDesignLine line, SamplingDesignLine duplicateLine, 
			SamplingDesignFileColumn[] columns) throws ParsingException {
		String[] colNames = new String[columns.length];
		for (int i = 0; i < columns.length; i++) {
			SamplingDesignFileColumn column = columns[i];
			colNames[i] = column.getColumnName();
		}
		ParsingError error = new ParsingError(
			ErrorType.DUPLICATE_VALUE, 
			line.getLineNumber(), 
			colNames);
		String duplicateLineNumber = Long.toString(duplicateLine.getLineNumber());
		error.setMessageArgs(new String[] {duplicateLineNumber});
		throw new ParsingException(error);
	}

	protected void persistSamplingDesign() throws SurveyImportException {
		List<String> infoColumnNames = reader.getInfoColumnNames();
		List<ReferenceDataDefinition.Attribute> attributes = ReferenceDataDefinition.Attribute.fromNames(infoColumnNames);
		SamplingPointDefinition samplingPoint;
		if ( attributes.isEmpty() ) {
			samplingPoint = null;
		} else {
			samplingPoint = new SamplingPointDefinition();
			samplingPoint.setAttributes(attributes);
		}
		ReferenceDataSchema referenceDataSchema = survey.getReferenceDataSchema();
		if ( referenceDataSchema == null ) {
			referenceDataSchema = new ReferenceDataSchema();
			survey.setReferenceDataSchema(referenceDataSchema);
		}
		referenceDataSchema.setSamplingPointDefinition(samplingPoint);
		saveSurvey();

		List<SamplingDesignItem> items = createItemsFromLines();
		samplingDesignManager.insert(survey, items, overwriteAll);
	}

	private void saveSurvey() throws SurveyImportException {
		if ( survey.isWork() ) {
			surveyManager.saveSurveyWork(survey);
		} else {
			surveyManager.updateModel(survey);
		}
	}

	protected List<SamplingDesignItem> createItemsFromLines() {
		List<SamplingDesignItem> items = new ArrayList<SamplingDesignItem>();
		for (SamplingDesignLine line : lines) {
			SamplingDesignItem item = line.toSamplingDesignItem(survey, reader.getInfoColumnNames());
			items.add(item);
		}
		return items;
	}
	
	public List<String> getInfoColumnNames() {
		return reader.getInfoColumnNames();
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}
}
