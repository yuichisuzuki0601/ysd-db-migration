package jp.co.ysd.db_migration.dao;

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
import jp.co.ysd.db_migration.replacer.DataReplacer;

/**
 * 
 * @author yuichi
 * 
 */
public abstract class Dao {

	private static final String SQL_DROP_TABLE = "DROP TABLE %s;";
	private static final String SQL_CREATE_TABLE = "CREATE TABLE %s (%s);";
	private static final String SQL_CREATE_INDEX = "CREATE INDEX %s ON %s (%s);";
	private static final String SQL_CREATE_FOREIGN_KEY = "ALTER TABLE %s ADD FOREIGN KEY (%s) REFERENCES %s (%s) %s;";
	private static final String SQL_INSERT = "INSERT INTO %s %s VALUES %s;";

	protected Logger l = LoggerFactory.getLogger(getClass());

	@Autowired
	protected JdbcTemplate j;

	@Autowired
	protected List<DataReplacer> replacers;

	protected abstract boolean existTable(String tableName);

	protected abstract String getSelectAllTableSql();

	protected abstract String getSelectAllForeignKeySql();

	protected abstract String getDropForeignKeySql();

	protected List<String> selectAllTable() {
		return j.query(getSelectAllTableSql(), (rs, rowNum) -> rs.getString("name"));
	}

	private void dropTable(String tableName) {
		String sql = String.format(SQL_DROP_TABLE, tableName);
		l.info(sql);
		j.execute(sql);
	}

	public void dropAllTable() {
		selectAllTable().forEach(tableName -> dropTable(tableName));
	}

	public boolean createTable(String tableName, List<Map<String, String>> cols, String pk, String uq) {
		return createTable(ExecMode.NORMAL, tableName, cols, pk, uq);
	}

	public boolean createTable(ExecMode mode, String tableName, List<Map<String, String>> cols, String pk, Object uq) {
		boolean result = false;
		boolean condCreate = mode == ExecMode.REBUILD || !existTable(tableName);
		if (condCreate) {
			String sql = getCreateTableSql(tableName, cols, pk, uq);
			l.info(sql);
			j.execute(sql);
			result = true;
		}
		l.info("table:" + tableName + (condCreate ? " was created." : " is already exists."));
		return result;
	}

	@SuppressWarnings("unchecked")
	public String getCreateTableSql(String tableName, List<Map<String, String>> cols, String pk, Object uq) {
		String colDefine = cols.stream().map(col -> col.get("name") + " " + col.get("type"))
				.reduce((l, r) -> l + "," + r).get();
		colDefine += pk != null ? ",PRIMARY KEY(" + pk + ")" : "";
		List<String> uqList = new ArrayList<>();
		if (uq instanceof List) {
			uqList = (List<String>) uq;
		} else if (uq instanceof String) {
			uqList.add((String) uq);
		}
		for (String line : uqList) {
			colDefine += ",UNIQUE(" + line + ")";
		}
		return String.format(SQL_CREATE_TABLE, tableName, colDefine);
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
		if (!StringUtils.isEmpty(sql)) {
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

	public int insert(String tableName, Map<String, Object> data) {
		return insert(tableName, Arrays.asList(data));
	}

	public int insert(String tableName, List<Map<String, Object>> data) {
		return (int) data.stream().map(datum -> {
			StringJoiner colsPart = new StringJoiner(",", "(", ")");
			StringJoiner valsPart = new StringJoiner(",", "(", ")");
			List<Object> vals = new ArrayList<>();
			datum.entrySet().stream().forEach(e -> {
				colsPart.add(e.getKey());
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
				return j.update(sql, vals.toArray());
			} catch (DuplicateKeyException e) {
				l.warn("!!!duplicated!!!");
				return 0;
			}
		}).count();
	}

	public int bulkInsert(String tableName, List<Map<String, Object>> data) {
		if (data.isEmpty()) {
			return 0;
		}
		Map<String, Object> datum0 = data.get(0);
		StringJoiner colsPart = new StringJoiner(",", "(", ")");
		StringJoiner valsPart = new StringJoiner(",", "(", ")");
		datum0.entrySet().stream().forEach(e -> {
			colsPart.add(e.getKey());
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
