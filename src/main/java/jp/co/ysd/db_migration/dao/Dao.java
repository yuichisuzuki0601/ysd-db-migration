package jp.co.ysd.db_migration.dao;

import static jp.co.ysd.db_migration.util.SqlUtil.*;
import static jp.co.ysd.ysd_util.stream.StreamWrapperFactory.*;
import static jp.co.ysd.ysd_util.string.YsdStringUtil.*;

import java.io.IOException;
import java.nio.file.Files;
import java.sql.PreparedStatement;
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
import jp.co.ysd.db_migration.dao.sql.CreateForeignKeySql;
import jp.co.ysd.db_migration.dao.sql.CreateIndexSql;
import jp.co.ysd.db_migration.dao.sql.CreateTableSql;
import jp.co.ysd.db_migration.dao.sql.CreateViewSql;
import jp.co.ysd.db_migration.dao.sql.DropIndexSql;
import jp.co.ysd.db_migration.dao.sql.DropTableSql;
import jp.co.ysd.db_migration.dao.sql.DropViewSql;
import jp.co.ysd.db_migration.dao.sql.SelectDataByIdSql;
import jp.co.ysd.db_migration.dao.sql.SelectDataOrderByIdSql;
import jp.co.ysd.db_migration.datasource.DataSourceManager.CurrentDataSource;
import jp.co.ysd.db_migration.datasource.DataSourceWrapper;
import jp.co.ysd.db_migration.replacer.DataReplacer;
import jp.co.ysd.db_migration.util.FileAccessor;
import jp.co.ysd.ysd_util.tuple.Tuple2;

/**
 *
 * @author yuichi
 *
 */
public abstract class Dao {

	private static final String SQL_INSERT = "INSERT INTO `%s` %s VALUES %s;";

	protected Logger l = LoggerFactory.getLogger(getClass());

	@Autowired
	protected DataSourceWrapper dataSourceWrapper;

	@Autowired
	protected JdbcTemplate j;

	@Autowired
	protected List<DataReplacer> replacers;

	protected abstract boolean existTableAndView(String tableName);

	protected abstract String getDropSchemaIfExistsSql();

	protected abstract String getCreateSchemaIfNotExistsSql();

	protected abstract String getSelectAllTableAndViewSql();

	protected abstract String getSelectAllForeignKeySql();

	protected abstract String getDropForeignKeySql(String tableName, String foreignKey);

	protected abstract String getSelectAllIndexFromTableSql(String tableName);

	public void execute(String sqls) {
		for (var shot : sqls.split(";")) {
			var sql = shot.trim();
			l.info(sql);
			j.execute(sql);
		}
	}

	public void dropSchemaIfExists() {
		var currentDataSource = CurrentDataSource.getInstance();
		currentDataSource.toRoot();
		execute(getDropSchemaIfExistsSql());
		currentDataSource.toTarget();
	};

	public void createSchemaIfNotExists() {
		var currentDataSource = CurrentDataSource.getInstance();
		currentDataSource.toRoot();
		execute(getCreateSchemaIfNotExistsSql());
		currentDataSource.toTarget();
	};

	public void dropAllTableAndView() {
		var tableInfos = j.query(getSelectAllTableAndViewSql(),
				(rs, rowNum) -> new Tuple2<>(rs.getString("name"), rs.getString("type")));
		for (var tableInfo : tableInfos) {
			var name = tableInfo.one();
			var type = tableInfo.two();
			execute("VIEW".equals(type) ? DropViewSql.get(name) : DropTableSql.get(name));
		}
	}

	public boolean createTable(ExecMode mode, String tableName, List<Map<String, String>> cols, String pk, Object uq) {
		var result = false;
		var condCreate = mode.is(ExecMode.REBUILD) || !existTableAndView(tableName);
		if (condCreate) {
			execute(CreateTableSql.get(tableName, cols, pk, uq));
			result = true;
		}
		l.info("table:" + tableName + (condCreate ? " was created." : " is already exists."));
		return result;
	}

	@SuppressWarnings("unchecked")
	public void createView(boolean force, String viewName, Map<String, Object> viewSetting) throws IOException {
		var condCreate = force || !existTableAndView(viewName);
		if (condCreate) {
			var templatePath = FileAccessor.getViewTemplateFile((String) viewSetting.get("template")).toPath();
			var parameters = (Map<String, Object>) viewSetting.get("parameters");
			var targetSql = strip(stream(Files.readAllLines(templatePath)).reduce((l, r) -> l + " " + r));
			for (var parameter : parameters.entrySet()) {
				targetSql = targetSql.replaceAll("\\$\\{" + parameter.getKey() + "\\}",
						parameter.getValue().toString());
			}
			execute(CreateViewSql.get(viewName, targetSql));
		}
		l.info("view:" + viewName + (condCreate ? " was created." : " is already exists."));
	}

	public void dropIndexFromTable(String tableName) {
		var indexNames = j.query(getSelectAllIndexFromTableSql(tableName), (rs, rowNum) -> rs.getString("Key_name"));
		for (var indexName : indexNames) {
			execute(DropIndexSql.get(indexName, tableName));
		}
	}

	public String getCreateIndexSql(String tableName, List<Map<String, String>> cols) {
		var sql = new StringBuilder();
		for (var col : cols) {
			sql.append(CreateIndexSql.get(col.get("index_name"), tableName, col.get("col")));
		}
		return sql.toString();
	}

	public void createIndex(String tableName, List<Map<String, String>> cols) {
		execute(getCreateIndexSql(tableName, cols));
	}

	public String getDropAllForeignKeySql() {
		var allForeignKey = j.query(getSelectAllForeignKeySql(), (rs, rowNum) -> {
			var map = new HashMap<String, String>();
			var meta = rs.getMetaData();
			for (var i = 1; i <= meta.getColumnCount(); ++i) {
				map.put(meta.getColumnLabel(i), rs.getString(i));
			}
			return map;
		});
		var sql = new StringBuilder();
		for (var fk : allForeignKey) {
			sql.append(getDropForeignKeySql(fk.get("table_name"), fk.get("foreign_key_name")));
		}
		return sql.toString();
	}

	public void dropAllForeignKey() {
		var sql = getDropAllForeignKeySql();
		if (StringUtils.hasText(sql)) {
			execute(sql);
		}
	}

	@SuppressWarnings("unchecked")
	public String getCreateForeignKeySql(String tableName, List<Map<String, Object>> cols) {
		var sql = new StringBuilder();
		for (var col : cols) {
			var ref = (Map<String, String>) col.get("references");
			sql.append(CreateForeignKeySql.get(tableName, (String) col.get("name"), ref.get("schema"), ref.get("table"),
					ref.get("col"), (String) col.get("option")));
		}
		return sql.toString();
	}

	public void createForeignKey(String tableName, List<Map<String, Object>> cols) {
		execute(getCreateForeignKeySql(tableName, cols));
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
				colsPart.add(bq(e.getKey()));
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
			} catch (Exception e) {
				l.error("error occurred when insert data: insert target table={} ", tableName);
				throw e;
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
			colsPart.add(bq(e.getKey()));
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
		} catch (Exception e) {
			l.error("error occurred when insert data: insert target table={} ", tableName);
			throw e;
		}
		return Arrays.stream(result).reduce((left, right) -> left + right).getAsInt();
	}

	public Object selectDataById(String tableName, String column, String id) {
		return j.queryForObject(SelectDataByIdSql.get(column, tableName, id), (rs, rowNum) -> rs.getObject(1));
	}

	public List<Object> selectDataOrderById(String schema, String tableName, String column) {
		return j.query(SelectDataOrderByIdSql.get(column, schema, tableName), (rs, rowNum) -> rs.getObject(1));
	}

}
