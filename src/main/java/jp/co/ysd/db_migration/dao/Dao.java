package jp.co.ysd.db_migration.dao;

import static jp.co.ysd.ysd_util.stream.StreamWrapperFactory.*;
import static jp.co.ysd.ysd_util.string.YsdStringUtil.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import jp.co.ysd.db_migration.ExecMode;
import jp.co.ysd.db_migration.dao.sql.CreateTableSql;
import jp.co.ysd.db_migration.replacer.DataReplacer;
import jp.co.ysd.db_migration.util.FileAccessor;
import jp.co.ysd.ysd_util.tuple.Tuple2;

/**
 *
 * @author yuichi
 *
 */
public abstract class Dao {

	private static final String SQL_DROP_TABLE = "DROP TABLE `%s`;";
	private static final String SQL_DROP_INDEX = "DROP INDEX `%s` ON %s;";
	private static final String SQL_DROP_VIEW = "DROP VIEW `%s`;";
	private static final String SQL_CREATE_INDEX = "CREATE INDEX `%s` ON `%s` (%s);";
	private static final String SQL_CREATE_FOREIGN_KEY = "ALTER TABLE `%s` ADD FOREIGN KEY (`%s`) REFERENCES `%s` (`%s`) %s;";
	private static final String SQL_CREATE_VIEW = "CREATE OR REPLACE VIEW `%s` AS %s;";
	private static final String SQL_SELECT_BY_ID = "SELECT `%s` FROM `%s` WHERE id = %s;";
	private static final String SQL_SELECT_ORDER_BY_ID = "SELECT `%s` FROM `%s` ORDER BY id;";
	private static final String SQL_INSERT = "INSERT INTO `%s` %s VALUES %s;";

	protected Logger l = LoggerFactory.getLogger(getClass());

	@Autowired
	protected JdbcTemplate j;

	@Autowired
	protected List<DataReplacer> replacers;

	protected abstract boolean existTableAndView(String tableName);

	protected abstract String getSelectAllTableAndViewSql();

	protected abstract String getSelectAllForeignKeySql();

	protected abstract String getDropForeignKeySql();

	protected abstract String getSelectAllIndexFromTableSql(String tableName);

	private void dropTable(String tableName) {
		String sql = String.format(SQL_DROP_TABLE, tableName);
		l.info(sql);
		j.execute(sql);
	}

	private void dropView(String viewName) {
		String sql = String.format(SQL_DROP_VIEW, viewName);
		l.info(sql);
		j.execute(sql);
	}

	private List<Tuple2<String, String>> selectAllTableAndView() {
		return j.query(getSelectAllTableAndViewSql(), (rs, rowNum) -> {
			return new Tuple2<>(rs.getString("name"), rs.getString("type"));
		});
	}

	public void dropAllTableAndView() {
		selectAllTableAndView().forEach(tableInfo -> {
			String name = tableInfo.one();
			String type = tableInfo.two();
			if ("VIEW".equals(type)) {
				dropView(name);
			} else {
				dropTable(name);
			}
		});
	}

	public boolean createTable(ExecMode mode, String tableName, List<Map<String, String>> cols, String pk, Object uq) {
		var result = false;
		var condCreate = mode.is(ExecMode.REBUILD) || !existTableAndView(tableName);
		if (condCreate) {
			var sql = CreateTableSql.get(tableName, cols, pk, uq);
			l.info(sql);
			j.execute(sql);
			result = true;
		}
		l.info("table:" + tableName + (condCreate ? " was created." : " is already exists."));
		return result;
	}

	private List<String> selectAllIndexFromTable(String tableName) {
		return j.query(getSelectAllIndexFromTableSql(tableName), (rs, rowNum) -> {
			return rs.getString("Key_name");
		});
	}

	public void dropIndexFromTable(String tableName) {
		List<String> indexNames = selectAllIndexFromTable(tableName);
		for (String indexName : indexNames) {
			String sql = String.format(SQL_DROP_INDEX, indexName, tableName);
			l.info(sql);
			j.execute(sql);
		}
	}

	public void createIndex(String tableName, List<Map<String, Object>> cols) {
		execute(getCreateIndexSql(tableName, cols));
	}

	public String getCreateIndexSql(String tableName, List<Map<String, Object>> cols) {
		StringBuilder sql = new StringBuilder();
		for (Map<String, Object> col : cols) {
			sql.append(String.format(SQL_CREATE_INDEX, col.get("index_name"), tableName, col.get("col")));
		}
		return sql.toString();
	}

	private List<Map<String, Object>> selectAllForeignKey() {
		return j.query(getSelectAllForeignKeySql(), (rs, rowNum) -> {
			Map<String, Object> map = new HashMap<>();
			ResultSetMetaData meta = rs.getMetaData();
			for (int i = 1; i <= meta.getColumnCount(); ++i) {
				map.put(meta.getColumnLabel(i), rs.getObject(i));
			}
			return map;
		});
	}

	public void dropAllForeignKey() {
		String sql = getDropAllForeignKeySql();
		if (StringUtils.hasText(sql)) {
			execute(sql);
		}
	}

	public String getDropAllForeignKeySql() {
		StringBuilder sql = new StringBuilder();
		for (Map<String, Object> fk : selectAllForeignKey()) {
			sql.append(String.format(getDropForeignKeySql(), fk.get("table_name"), fk.get("foreign_key_name")))
					.append(";");
		}
		return sql.toString();
	}

	public void createForeignKey(String tableName, List<Map<String, Object>> cols) {
		execute(getCreateForeignKeySql(tableName, cols));
	}

	@SuppressWarnings("unchecked")
	public String getCreateForeignKeySql(String tableName, List<Map<String, Object>> cols) {
		StringBuilder sql = new StringBuilder();
		for (Map<String, Object> col : cols) {
			Map<String, String> ref = (Map<String, String>) col.get("references");
			sql.append(String.format(SQL_CREATE_FOREIGN_KEY, tableName, col.get("name"), ref.get("table"),
					ref.get("col"), col.get("option")));
		}
		return sql.toString();
	}

	@SuppressWarnings("unchecked")
	public void createView(boolean force, String viewName, Map<String, Object> viewSetting) throws IOException {
		boolean condCreate = force || !existTableAndView(viewName);
		if (condCreate) {
			Path templatePath = FileAccessor.getViewTemplateFile((String) viewSetting.get("template")).toPath();
			Map<String, Object> parameters = (Map<String, Object>) viewSetting.get("parameters");
			execute(getCreateViewSql(viewName, templatePath, parameters));
		}
		l.info("view:" + viewName + (condCreate ? " was created." : " is already exists."));
	}

	public String getCreateViewSql(String viewName, Path templatePath, Map<String, Object> parameters)
			throws IOException {
		String targetSql = strip(stream(Files.readAllLines(templatePath)).reduce((l, r) -> l + " " + r));
		for (Entry<String, Object> parameter : parameters.entrySet()) {
			targetSql = targetSql.replaceAll("\\$\\{" + parameter.getKey() + "\\}", parameter.getValue().toString());
		}
		return String.format(SQL_CREATE_VIEW, viewName, targetSql);
	}

	public Object selectDataById(String tableName, String colmuName, String id) {
		return j.queryForObject(String.format(SQL_SELECT_BY_ID, colmuName, tableName, id), (rs, rowNum) -> {
			return rs.getObject(1);
		});
	}

	public List<Object> selectDataOrderById(String tableName, String colmuName) {
		return j.query(String.format(SQL_SELECT_ORDER_BY_ID, colmuName, tableName), (rs, rowNum) -> {
			return rs.getObject(1);
		});
	}

	public int insert(String tableName, Map<String, Object> data) {
		return insert(tableName, Arrays.asList(data));
	}

	public int insert(String tableName, List<Map<String, Object>> data) {
		int count = 0;
		for (Map<String, Object> datum : data) {
			StringJoiner colsPart = new StringJoiner(",", "(", ")");
			StringJoiner valsPart = new StringJoiner(",", "(", ")");
			List<Object> vals = new ArrayList<>();
			datum.entrySet().stream().forEach(e -> {
				colsPart.add("`" + e.getKey() + "`");
				Object value = e.getValue();
				if (value != null) {
					valsPart.add("?");
					for (DataReplacer replacer : replacers) {
						value = replacer.replace(value);
					}
					vals.add(value);
				} else {
					valsPart.add("NULL");
				}
			});
			String sql = String.format(SQL_INSERT, tableName, colsPart.toString(), valsPart.toString());
			l.info(sql);
			l.info(vals.toString());
			try {
				count += j.update(sql, vals.toArray());
			} catch (DuplicateKeyException e) {
				l.warn("!!!duplicated!!!");
				count += 0;
			}
		}
		return count;
	}

	public int bulkInsert(String tableName, List<Map<String, Object>> data) {
		if (data.isEmpty()) {
			return 0;
		}
		Map<String, Object> datum0 = data.get(0);
		StringJoiner colsPart = new StringJoiner(",", "(", ")");
		StringJoiner valsPart = new StringJoiner(",", "(", ")");
		datum0.entrySet().stream().forEach(e -> {
			colsPart.add("`" + e.getKey() + "`");
			valsPart.add("?");
		});
		String sql = String.format(SQL_INSERT, tableName, colsPart.toString(), valsPart.toString());
		l.info(sql);
		int[] result = null;
		try {
			result = j.batchUpdate(sql, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					Map<String, Object> datum = data.get(i);
					List<Object> vals = new ArrayList<>();
					int parameterIndex = 1;
					for (Entry<String, Object> e : datum.entrySet()) {
						Object value = e.getValue();
						for (DataReplacer replacer : replacers) {
							value = replacer.replace(value);
						}
						vals.add(value);
						ps.setObject(parameterIndex++, value);
					}
					l.info(vals.toString());
				}

				@Override
				public int getBatchSize() {
					return data.size();
				}
			});
		} catch (DuplicateKeyException e) {
			l.warn("!!!duplicated!!!");
			result = new int[] { 0 };
		}
		return Arrays.stream(result).reduce((left, right) -> left + right).getAsInt();
	}

	public void execute(String sqls) {
		for (String shot : sqls.split(";")) {
			String sql = shot.trim();
			l.info(sql);
			j.execute(sql);
		}
	}

}
