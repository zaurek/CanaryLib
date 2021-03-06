package net.canarymod.database.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;

import net.canarymod.Logman;
import net.canarymod.database.DatabaseRow;
import net.canarymod.database.DatabaseTable;

public class DatabaseTableMySql implements DatabaseTable {

    private String tableName;

    public DatabaseTableMySql(String tableName) {
        this.tableName = tableName.toLowerCase();
    }

    @Override
    public String getName() {
        return tableName;
    }

    @Override
    public void setName(String name) {
        if (name == null || name.isEmpty()) {
            return;
        }
        
        name = name.toLowerCase();
        try {
            PreparedStatement ps = DatabaseMySql.getStatement("ALERT TABLE ? RENAME ?");
            ps.setString(1, tableName);
            ps.setString(2, name);

            if (ps.execute()) {
                tableName = name;
            }
        } catch (SQLException e) {
            Logman.logStackTrace("Exception while renaming table "+tableName, e);
        }
    }

    @Override
    public String getDescription() {
        try {
            PreparedStatement ps = DatabaseMySql
                    .getStatement("SHOW TABLE STATUS FROM ?");
            ps.setString(1, tableName);

            ResultSet rs = ps.executeQuery();
            if (rs.first())
                return rs.getString("COMMENT");
        } catch (SQLException e) {
            Logman.logStackTrace("Exception while getting description for table "+tableName, e);
        }
        return null;
    }

    @Override
    public void setDescription(String description) {
        if (description == null) {
            description = "";
        }

        try {
            PreparedStatement ps = DatabaseMySql
                    .getStatement("ALTER TABLE ? COMMENT = ?");
            ps.setString(1, tableName);
            ps.setString(2, description);

            ps.execute();
        } catch (SQLException e) {
            Logman.logStackTrace("Exception while setting description for table "+tableName, e);
        }
    }

    @Override
    public int getNumRows() {
        try {
            PreparedStatement ps = DatabaseMySql
                    .getStatement("SELECT count(*) as numRows FROM ?");
            ps.setString(1, tableName);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getInt("numRows");
            }
        } catch (SQLException e) {
            Logman.logStackTrace("Exception while getting number of rows from "+tableName, e);
        }
        return -1;
    }

    @Override
    public DatabaseRow getRow(int rowID) {
        if (rowID <= 0) {
            return null;
        }

        try {
            PreparedStatement ps = DatabaseMySql
                    .getStatement("SELECT * FROM ? WHERE RID = ?");
            ps.setString(1, tableName);
            ps.setInt(2, rowID);

            ResultSet rs = ps.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            HashMap<String, Object> columnValues = new HashMap<String, Object>();

            for (int i = 0; i < rsmd.getColumnCount(); ++i) {
                columnValues.put(rsmd.getColumnName(i).toUpperCase(),
                        rs.getObject(i));
            }

            return new DatabaseRowMySql(this, rs.getInt("RID"), columnValues);
        } catch (SQLException e) {
            Logman.logStackTrace("Exception while getting DatabaseRow from "+tableName, e);
            return null;
        }
    }

    @Override
    public DatabaseRow[] getAllRows() {
        try {
            PreparedStatement ps = DatabaseMySql
                    .getStatement("SELECT * FROM ?");
            ps.setString(1, tableName);

            ResultSet rs = ps.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            int numColumns = rsmd.getColumnCount();

            // count Rows
            rs.last();
            int numRows = rs.getRow();
            
            if (numRows <= 0)
                return null;

            rs.beforeFirst();
            DatabaseRow[] result = new DatabaseRow[numRows];
            HashMap<String, Object> columnValues;
            while (rs.next()) {
                columnValues = new HashMap<String, Object>();
                for (int i = 0; i < numColumns; ++i) {
                    columnValues.put(rsmd.getColumnName(i).toUpperCase(),
                            rs.getObject(i));
                }
                result[rs.getRow() - 1] = new DatabaseRowMySql(this, rs.getInt("ID"), columnValues);
            }

            return result;
        } catch (SQLException e) {
            Logman.logStackTrace("Exception while getting DatabaseRows from "+tableName, e);
            return null;
        }
    }

    @Override
    public DatabaseRow[] getFilteredRows(String column, String value) {
        if (column == null || column.isEmpty() || value == null
                || value.isEmpty()) {
            return null;
        }

        column = column.toUpperCase();
        try {
            PreparedStatement ps = DatabaseMySql
                    .getStatement("SELECT * FROM ? WHERE ? = ?");
            ps.setString(1, tableName);
            ps.setString(2, column);

            try {
                ps.setInt(2, Integer.parseInt(value));
            } catch (Exception ex) {
                try {
                    ps.setDouble(2, Double.parseDouble(value));
                } catch (Exception iex) {
                    ps.setString(2, value);
                }
            }

            ResultSet rs = ps.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            int numColumns = rsmd.getColumnCount();

            // count Rows
            rs.last();
            int numRows = rs.getRow();
            
            if (numRows <= 0)
                return null;

            rs.beforeFirst();
            DatabaseRow[] result = new DatabaseRow[numRows];
            HashMap<String, Object> columnValues;
            while (rs.next()) {
                columnValues = new HashMap<String, Object>();
                for (int i = 0; i < numColumns; ++i) {
                    columnValues.put(rsmd.getColumnName(i).toUpperCase(),
                            rs.getObject(i));
                }
                result[rs.getRow() - 1] = new DatabaseRowMySql(this, rs.getInt("RID"), columnValues);
            }

            return result;
        } catch (SQLException e) {
            Logman.logStackTrace("Exception while getting filtered DatabaseRows from "+tableName, e);
            return null;
        }
    }

    @Override
    public boolean rowExists(String column, String value) {
        if (column == null || column.isEmpty() || value == null
                || value.isEmpty()) {
            return false;
        }

        column = column.toUpperCase();
        try {
            PreparedStatement ps = DatabaseMySql
                    .getStatement("SELECT * FROM ? WHERE ? = ? LIMIT 1");
            ps.setString(1, tableName);
            ps.setString(2, column);

            try {
                ps.setInt(2, Integer.parseInt(value));
            } catch (Exception ex) {
                try {
                    ps.setDouble(2, Double.parseDouble(value));
                } catch (Exception iex) {
                    ps.setString(2, value);
                }
            }

            ResultSet rs = ps.executeQuery();
            return rs.first();
        } catch (SQLException ex) {
            Logman.logStackTrace("Exception while checking if rows exsists in "+tableName+". It probably doesn't then ...", ex);
            return false;
        }
    }

    @Override
    public DatabaseRow addRow() {
        try {
            String[] availableColumns = getAllColumns();
            if (availableColumns == null || availableColumns.length  <= 1)
                return null;
            
            PreparedStatement ps = DatabaseMySql.getStatement("INSERT INTO ?(?) VALUES (NULL)");
            ps.setString(1, tableName);
            ps.setString(2, availableColumns[1]);

            ResultSet rs = ps.executeQuery();
            if (!rs.first())
                return null;

            ResultSetMetaData rsmd = rs.getMetaData();
            HashMap<String, Object> columnValues = new HashMap<String, Object>();

            for (int i = 0; i < rsmd.getColumnCount(); ++i) {
                columnValues.put(rsmd.getColumnName(i).toUpperCase(),rs.getObject(i));
            }

            return new DatabaseRowMySql(this, rs.getInt("RID"), columnValues);
        } catch (SQLException ex) {
            Logman.logStackTrace("Exception while creating new DatabaseRow in "+tableName, ex);
            return null;
        }
    }

    @Override
    public void removeRow(DatabaseRow row) {
        removeRow(row.getRowID());
    }

    @Override
    public void removeRow(int rowID) {
        if (rowID <= 0) {
            return;
        }

        try {
            PreparedStatement ps = DatabaseMySql.getStatement("DELETE FROM ? WHERE RID = ?");
            ps.setString(1, tableName);
            ps.setInt(2, rowID);

            ps.executeQuery();
        } catch (SQLException ex) {
            Logman.logStackTrace("Exception while removing row after index in "+tableName, ex);
        }
    }

    @Override
    public int getNumColumns() {
        try {
            PreparedStatement ps = DatabaseMySql
                    .getStatement("SELECT * FROM ? LIMIT 1");
            ps.setString(1, tableName);

            ResultSet rs = ps.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();

            return rsmd.getColumnCount();
        } catch (SQLException ex) {
            Logman.logStackTrace("Exception while getNumColumns in "+tableName, ex);
            return -1;
        }
    }

    @Override
    public String[] getAllColumns() {
        try {
            PreparedStatement ps = DatabaseMySql
                    .getStatement("SELECT * FROM ? LIMIT 1");
            ps.setString(1, tableName);

            ResultSet rs = ps.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            int numColumns = rsmd.getColumnCount();
            
            if (numColumns <= 0)
                return new String[0];

            String[] result = new String[numColumns];
            for (int i = 0; i < numColumns; ++i) {
                result[i] = rsmd.getColumnName(i).toUpperCase();
            }

            return result;
        } catch (SQLException ex) {
            Logman.logStackTrace("Exception while getting column names in "+tableName, ex);
            return new String[0];
        }
    }

    @Override
    public void appendColumn(String name, ColumnType type) {
        if (name == null || name.isEmpty())
            return;
        
        name = name.toUpperCase();
        try {
            String strType = "";

            if (type == ColumnType.BOOLEAN)
                strType = "TINYINT";
            else if (type == ColumnType.INTEGER)
                strType = "INTEGER";
            else if (type == ColumnType.LONG)
                strType = "BIGINT";
            else if (type == ColumnType.FLOAT)
                strType = "FLOAT";
            else if (type == ColumnType.DOUBLE)
                strType = "DOUBLE";
            else if (type == ColumnType.CHARACTER)
                strType = "CHAR";
            else if (type == ColumnType.STRING)
                strType = "VARCHAR(max)";

            PreparedStatement ps = DatabaseMySql
                    .getStatement("ALTER TABLE ? ADD COLUMN ? " + strType
                            + " NULL");
            ps.setString(1, tableName);
            ps.setString(2, name);

            ps.executeQuery();
        } catch (SQLException ex) {
            Logman.logStackTrace("Exception while appending column to "+tableName, ex);
        }
    }

    @Override
    public void removeColumn(String name) {
        if (name == null || name.isEmpty() || name.toUpperCase() == "RID")
            return;
        
        name = name.toUpperCase();
        try {
            PreparedStatement ps = DatabaseMySql
                    .getStatement("ALTER TABLE ? DROP COLUMN ?");
            ps.setString(1, tableName);
            ps.setString(2, name);

            ps.executeQuery();
        } catch (SQLException ex) {
            Logman.logStackTrace("Exception while dropping column to "+tableName, ex);
        }
    }

    @Override
    public void insertColumn(String name, ColumnType type, String after) {
        if (name == null || name.isEmpty() || after == null || after.isEmpty())
            return;
        
        name = name.toUpperCase();
        after = after.toUpperCase();
        try {
            String strType = "";

            if (type == ColumnType.BOOLEAN)
                strType = "TINYINT";
            else if (type == ColumnType.INTEGER)
                strType = "INTEGER";
            else if (type == ColumnType.LONG)
                strType = "BIGINT";
            else if (type == ColumnType.FLOAT)
                strType = "FLOAT";
            else if (type == ColumnType.DOUBLE)
                strType = "DOUBLE";
            else if (type == ColumnType.CHARACTER)
                strType = "CHAR";
            else if (type == ColumnType.STRING)
                strType = "VARCHAR(max)";

            PreparedStatement ps = DatabaseMySql
                    .getStatement("ALTER TABLE ? ADD COLUMN ? " + strType
                            + " NULL AFTER ?");
            ps.setString(1, tableName);
            ps.setString(2, name);
            ps.setString(3, after);

            ps.executeQuery();
        } catch (SQLException ex) {
            Logman.logStackTrace("Exception while inserting column to "+tableName, ex);
        }
    }

    @Override
    public void truncateTable() {
        try {
            PreparedStatement ps = DatabaseMySql
                    .getStatement("TRUNCATE TABLE ?");
            ps.setString(1, tableName);

            ps.executeQuery();
        } catch (SQLException ex) {
            Logman.logStackTrace("Exception while truncating "+tableName, ex);
        }
    }
}
