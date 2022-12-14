package jp.co.ysd.db_migration.util;

import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.ysd.db_migration.properties.ysd.YsdDbMigrationProperty;

@Service
public class FileChecker {

	@Autowired
	private YsdDbMigrationProperty property;

	public void checkAllFiles() throws Exception {
		var tableNames = new ArrayList<String>();
		for (var defineFile : FileAccessor.getDefineFiles()) {
			tableNames.add(FilenameUtils.removeExtension(defineFile.getName()));
		}
		var incorrectFiles = new ArrayList<String>();
		for (var file : FileAccessor.getIndexFiles()) {
			var fileName = file.getName();
			if (!tableNames.contains(FilenameUtils.removeExtension(fileName.replaceAll("-index", "")))) {
				incorrectFiles.add(file.getCanonicalPath());
			}
		}
		for (var file : FileAccessor.getConstraintFiles()) {
			var fileName = file.getName();
			if (!tableNames.contains(FilenameUtils.removeExtension(fileName.replaceAll("-constraint", "")))) {
				incorrectFiles.add(file.getCanonicalPath());
			}
		}
		for (var file : FileAccessor.getViewFiles()) {
			var fileName = file.getName();
			var viewName = FilenameUtils.removeExtension(fileName.replaceAll("-view", ""));
			if (!tableNames.contains(viewName.replaceFirst(property.getView().getPrefix(), ""))) {
				incorrectFiles.add(file.getCanonicalPath());
			}
		}
		for (var file : FileAccessor.getDataFiles()) {
			var fileName = file.getName();
			if (!tableNames.contains(FilenameUtils.removeExtension(fileName.replaceAll("-data", "")))) {
				incorrectFiles.add(file.getCanonicalPath());
			}
		}

		for (var it = incorrectFiles.iterator(); it.hasNext();) {
			var incorrectFile = it.next();
			for (var setting : property.getCorrectFiles()) {
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
