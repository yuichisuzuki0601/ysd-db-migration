package jp.co.ysd.db_migration.properties.ysd;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jp.co.ysd.db_migration.properties.ysd.view.ViewProperty;

@Component
@ConfigurationProperties(prefix = "ysd")
public class YsdDbMigrationProperty {

	private List<String> correctFiles = new ArrayList<>();

	private ViewProperty view = new ViewProperty();

	public List<String> getCorrectFiles() {
		return correctFiles;
	}

	public void setCorrectFiles(List<String> correctFiles) {
		this.correctFiles = correctFiles;
	}

	public ViewProperty getView() {
		return view;
	}

	public void setView(ViewProperty view) {
		this.view = view;
	}

}
