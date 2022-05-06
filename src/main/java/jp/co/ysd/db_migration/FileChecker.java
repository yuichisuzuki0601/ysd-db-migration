package jp.co.ysd.db_migration;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.ysd.db_migration.properties.ysd.YsdDbMigrationProperty;
import jp.co.ysd.db_migration.util.FileAccessor;

@Service
public class FileChecker {

	@Autowired
	private YsdDbMigrationProperty property;

	public void checkAllFiles() throws Exception {
		List<String> tableNames = new ArrayList<>();
		for (File defineFile : FileAccessor.getDefineFiles()) {
			tableNames.add(FilenameUtils.removeExtension(defineFile.getName()));
		}
		List<String> incorrectFiles = new ArrayList<>();
		for (File file : FileAccessor.getIndexFiles()) {
			String fileName = file.getName();
			if (!tableNames.contains(FilenameUtils.removeExtension(fileName.replaceAll("-index", "")))) {
				incorrectFiles.add(file.getCanonicalPath());
			}
		}
		for (File file : FileAccessor.getConstraintFiles()) {
			String fileName = file.getName();
			if (!tableNames.contains(FilenameUtils.removeExtension(fileName.replaceAll("-constraint", "")))) {
				incorrectFiles.add(file.getCanonicalPath());
			}
		}
		for (File file : FileAccessor.getViewFiles()) {
			String fileName = file.getName();
			String viewName = FilenameUtils.removeExtension(fileName.replaceAll("-view", ""));
			if (!tableNames.contains(viewName.replaceFirst(property.getView().getPrefix(), ""))) {
				incorrectFiles.add(file.getCanonicalPath());
			}
		}
		for (File file : FileAccessor.getDataFiles()) {
			String fileName = file.getName();
			if (!tableNames.contains(FilenameUtils.removeExtension(fileName.replaceAll("-data", "")))) {
				incorrectFiles.add(file.getCanonicalPath());
			}
		}

		for (Iterator<String> it = incorrectFiles.iterator(); it.hasNext();) {
			String incorrectFile = it.next();
			for (String setting : property.getCorrectFiles()) {
				if (incorrectFile.contains(setting)) {
					it.remove();
				}
			}
		}

		if (!incorrectFiles.isEmpty()) {
			throw new RuntimeException("Incorrect files are mixed. " + incorrectFiles.toString());
		}
	}

}
