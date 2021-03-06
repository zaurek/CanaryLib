package net.canarymod.database.flatfile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import net.canarymod.Logman;
import net.canarymod.database.Database;
import net.canarymod.database.DatabaseRow;
import net.canarymod.database.DatabaseTable;

/**
 * @author Jos Kuijpers
 */
public class DatabaseFlatfile implements Database {

    private HashMap<String, DatabaseTableFlatfile> tables;
    private File dbDirectory;

    /**
     * Sets up the flatfile database
     */
    public DatabaseFlatfile() {

        // Find the database table files
        dbDirectory = new File("db/");
        if (!dbDirectory.exists()) {
            dbDirectory.mkdirs();
        }

        tables = new HashMap<String, DatabaseTableFlatfile>();
        String[] dbFiles = dbDirectory.list();

        // Extract the tablename and verify the extension
        for (String file : dbFiles) {
            if ((new File("db/" + file)).isDirectory()) // TODO inefficient
                continue;
            if (!file.endsWith(".txt")) {
                Logman.logWarning("Invalid file '" + file + "' found in db/");
                continue;
            }

            DatabaseTableFlatfile table;
            try {
                table = new DatabaseTableFlatfile(this, file);
            } catch (IOException e) {
                Logman.logSevere("Failed to load database for " + file + "!\n" + e.getLocalizedMessage());
                continue;
            }

            // Store the table object
            tables.put(table.getName(), table);
        }
    }

    public void reload() {
        // TODO implement database reload
        // look for removed files and remove them in our list
        // reload all tables
    }

    public void save() {

        // Write out the files... just save all tables
        for (DatabaseTableFlatfile t : this.tables.values())
            t.save();
    }

    public boolean prepare() {
        return true;
    }

    public boolean execute() {
        save();
        return true;
    }

    /**
     * Called by DatabaseTableFlatfile to synchronize the database info
     * structure. Do not use!
     * 
     * @param oldName
     * @param newName
     */
    public void tableRenamed(String oldName, String newName) {
        this.tables.put(newName, this.tables.get(oldName));
        this.tables.remove(oldName);
    }

    @Override
    public int getNumTables() {
        return this.tables.size();
    }

    @Override
    public String[] getAllTables() {
        String[] ar = {};
        return this.tables.keySet().toArray(ar);
    }

    @Override
    public DatabaseTable getTable(String name) {
        if (!this.tables.containsKey(name.toLowerCase()))
            return null;
        return this.tables.get(name.toLowerCase());
    }

    @Override
    public DatabaseTable addTable(String table) {
        if (this.tables.containsKey(table.toLowerCase()))
            return null;

        DatabaseTableFlatfile tableObject;
        try {
            tableObject = new DatabaseTableFlatfile(this, table.toLowerCase()
                    + ".txt");
        } catch (IOException e) {
            return null;
        }

        // Just add the table, and save it
        this.tables.put(table.toLowerCase(), tableObject);

        return tableObject;
    }

    @Override
    public void removeTable(String name) {
        if (!this.tables.containsKey(name.toLowerCase()))
            return;
        this.tables.remove(name.toLowerCase());
        File f = new File("db/" + name.toLowerCase() + ".txt");
        f.delete();
    }

    @Override
    public DatabaseRow[] getRelatedRows(String table1, String table2,
            String relation1, String relation2, String searchColumn,
            String searchValue) {

        ArrayList<DatabaseRow> relationRows = new ArrayList<DatabaseRow>();
        ArrayList<DatabaseRow> resultRows = new ArrayList<DatabaseRow>();

        try {
            // The rows in the first table that we need to match items too
            DatabaseRow[] searchRows = this.getTable(table1).getFilteredRows(
                    searchColumn, searchValue);
            if (searchRows == null) {
                return null;
            }

            ArrayList<String> table1Values = new ArrayList<String>();
            for (DatabaseRow row : searchRows) {
                table1Values.add(row.getStringCell(relation1));
            }

            // table1Values contains the values we are relating to

            // Find the records in the relation table
            DatabaseTable relT = this.getTable(table1 + "_" + table2 + "_rel");
            if (relT == null) {
                relT = this.getTable(table2 + "_" + table1 + "_rel");
                if (relT == null) {
                    return null;
                }
            }

            for (String val : table1Values) {
                DatabaseRow[] rs = relT.getFilteredRows(relation1, val);
                if (rs != null) {
                    for (DatabaseRow r : rs) {
                        relationRows.add(r);
                    }
                }
            }

            // Get the second-relation values
            for (DatabaseRow relRow : relationRows) {
                DatabaseRow[] rs = this.getTable(table2).getFilteredRows(
                        relation2, relRow.getStringCell(relation2));
                if (rs == null) {
                    return null;
                }
                for (DatabaseRow r : rs) {
                    if (!resultRows.contains(r)) {
                        resultRows.add(r);
                    }
                }
            }
        } catch (NullPointerException npe) {
            Logman.logStackTrace("Failed to resolve relations in database!",
                    npe);
            return null;
        }

        DatabaseRow[] retForm = {};
        return resultRows.toArray(retForm);
    }

    public boolean setRelated(DatabaseRow row1, DatabaseRow row2) {
        String table1, table2;
        String column1, column2;
        DatabaseTable relTable;
        List<String> table1Columns;
        String[] tableRelColumns;
        DatabaseRow relRow;

        // Get table information
        table1 = row1.getTable().getName();
        table2 = row2.getTable().getName();

        relTable = this.getTable(table1 + "_" + table2 + "_rel");
        if (relTable == null) {
            relTable = this.getTable(table2 + "_" + table1 + "_rel");
            if (relTable == null)
                return false;
        }

        // Get column information
        table1Columns = Arrays.asList(row1.getTable().getAllColumns());
        tableRelColumns = relTable.getAllColumns();
        if (tableRelColumns.length != 2)
            return false; // Invalid format

        if (table1Columns.contains(tableRelColumns[0])) {
            column1 = tableRelColumns[0];
            column2 = tableRelColumns[1];
        } else {
            column1 = tableRelColumns[1];
            column2 = tableRelColumns[0];
        }

        // Now we have all tables and all columns :D Lets link them
        relRow = relTable.addRow();
        relRow.setStringCell(column1, row1.getStringCell(column1));
        relRow.setStringCell(column2, row2.getStringCell(column2));

        return true;
    }

    public boolean unsetRelated(DatabaseRow row1, DatabaseRow row2) {
        // ------- Code Duplication FTW!
        String table1, table2;
        String column1, column2;
        DatabaseTable relTable;
        List<String> table1Columns;
        String[] tableRelColumns;
        DatabaseRow[] datas;

        // Get table information
        table1 = row1.getTable().getName();
        table2 = row2.getTable().getName();

        relTable = this.getTable(table1 + "_" + table2 + "_rel");
        if (relTable == null) {
            relTable = this.getTable(table2 + "_" + table1 + "_rel");
            if (relTable == null)
                return false;
        }

        // Get column information
        table1Columns = Arrays.asList(row1.getTable().getAllColumns());
        tableRelColumns = relTable.getAllColumns();
        if (tableRelColumns.length != 2)
            return false; // Invalid format

        if (table1Columns.contains(tableRelColumns[0])) {
            column1 = tableRelColumns[0];
            column2 = tableRelColumns[1];
        } else {
            column1 = tableRelColumns[1];
            column2 = tableRelColumns[0];
        }
        // -------

        // Now we have all tables and all columns :D Lets link them
        datas = relTable.getFilteredRows(column1, row1.getStringCell(column1));
        if (datas == null || datas.length == 0)
            return false;

        for (DatabaseRow row : datas) {
            if (row.getStringCell(column2).equals(row2.getStringCell(column2))) {
                relTable.removeRow(row);
                return true;
            }
        }

        // Not found
        return false;
    }

    private String[] resolvePath(String[] path) {
        String tableName = path[0].toLowerCase();
        String columnName = path[1].toUpperCase();
        ArrayList<String> data = new ArrayList<String>();
        String[] ret = {};
        DatabaseTableFlatfile table;

        if (!this.tables.containsKey(tableName))
            return null;

        table = this.tables.get(tableName);

        // Find the column index
        int index = table.getColumnPosition(columnName);
        if (index == -1)
            return null;

        for (DatabaseRow row : table.getAllRows()) {
            data.add(row.getStringCell(columnName));
        }

        ret = data.toArray(ret);

        return ret;
    }

    @Override
    public String getStringValue(String path) {
        String[] components = path.split("\\.");
        if (components.length != 3)
            return null;

        String[] values = this.resolvePath(components);
        int index;
        try {
            index = Integer.parseInt(components[2]);
        } catch (NumberFormatException e) {
            Logman.logWarning("A NumberFormatException occurred in Table: '"
                    + components[0] + "' @ Column: " + components[1]);
            return null;
        }

        if (index < 1 || index > values.length)
            return null;

        return values[index - 1];
    }

    @Override
    public String[] getStringValues(String path) {
        String[] components = path.split("\\.");
        if (components.length != 2)
            return null;

        return this.resolvePath(components);
    }

    @Override
    public Integer getIntValue(String path) {
        String sval = this.getStringValue(path);
        if (sval == null)
            return Integer.MAX_VALUE;

        try {
            return Integer.parseInt(sval);
        } catch (NumberFormatException e) {
            Logman.logWarning("A NumberFormatException occurred in database-path: "
                    + path);
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public Integer[] getIntValues(String path) {
        String[] svals = this.getStringValues(path);
        if (svals == null)
            return null;

        Integer[] ret = new Integer[svals.length];
        try {

            for (int i = 0; i < svals.length; i++)
                ret[i] = Integer.parseInt(svals[i]);
        } catch (NumberFormatException e) {
            Logman.logWarning("A NumberFormatException occurred in database-path: "
                    + path);
            return null;
        }

        return ret;
    }

    @Override
    public Float getFloatValue(String path) {
        String sval = this.getStringValue(path);
        if (sval == null)
            return Float.MAX_VALUE;

        try {
            return Float.parseFloat(sval);
        } catch (NumberFormatException e) {
            Logman.logWarning("A NumberFormatException occurred in database-path: "
                    + path);
            return Float.MAX_VALUE;
        }
    }

    @Override
    public Float[] getFloatValues(String path) {
        String[] svals = this.getStringValues(path);
        if (svals == null)
            return null;

        Float[] ret = new Float[svals.length];
        try {

            for (int i = 0; i < svals.length; i++)
                ret[i] = Float.parseFloat(svals[i]);
        } catch (NumberFormatException e) {
            Logman.logWarning("A NumberFormatException occurred in database-path: "
                    + path);
            return null;
        }

        return ret;
    }

    @Override
    public Double getDoubleValue(String path) {
        String sval = this.getStringValue(path);
        if (sval == null)
            return Double.MAX_VALUE;

        try {
            return Double.parseDouble(sval);
        } catch (NumberFormatException e) {
            Logman.logWarning("A NumberFormatException occurred in database-path: "
                    + path);
            return Double.MAX_VALUE;
        }
    }

    @Override
    public Double[] getDoubleValues(String path) {
        String[] svals = this.getStringValues(path);
        if (svals == null)
            return null;

        Double[] ret = new Double[svals.length];
        try {

            for (int i = 0; i < svals.length; i++)
                ret[i] = Double.parseDouble(svals[i]);
        } catch (NumberFormatException e) {
            Logman.logWarning("A NumberFormatException occurred in database-path: "
                    + path);
            return null;
        }

        return ret;
    }

    @Override
    public Boolean getBooleanValue(String path) {
        return this.getBooleanValue(path, false);
    }

    @Override
    public boolean getBooleanValue(String path, boolean defaults) {
        String sval = this.getStringValue(path);
        if (sval == null)
            return defaults;

        try {
            return Boolean.parseBoolean(sval);
        } catch (NumberFormatException e) {
            Logman.logWarning("A NumberFormatException occurred in database-path: "
                    + path);
            return defaults;
        }
    }

    @Override
    public Boolean[] getBooleanValues(String path) {
        String[] svals = this.getStringValues(path);
        if (svals == null)
            return null;

        Boolean[] ret = new Boolean[svals.length];
        for (int i = 0; i < svals.length; i++) {
            if (svals[i].equalsIgnoreCase("true")
                    || svals[i].equalsIgnoreCase("yes") || svals[i].equals("1"))
                ret[i] = true;
            else if (svals[i].equalsIgnoreCase("false")
                    || svals[i].equalsIgnoreCase("no") || svals[i].equals("0"))
                ret[i] = false;
            else {
                Logman.logWarning("A NumberFormatException occurred in database-path: "
                        + path);
                return null;
            }
        }

        return ret;
    }

    @Override
    public Long getLongValue(String path) {
        String sval = this.getStringValue(path);
        if (sval == null)
            return Long.MAX_VALUE;

        try {
            return Long.parseLong(sval);
        } catch (NumberFormatException e) {
            Logman.logWarning("A NumberFormatException occurred in database-path: "
                    + path);
            return Long.MAX_VALUE;
        }
    }

    @Override
    public Long[] getLongValues(String path) {
        String[] svals = this.getStringValues(path);
        if (svals == null)
            return null;

        Long[] ret = new Long[svals.length];
        try {

            for (int i = 0; i < svals.length; i++)
                ret[i] = Long.parseLong(svals[i]);
        } catch (NumberFormatException e) {
            Logman.logWarning("A NumberFormatException occurred in database-path: "
                    + path);
            return null;
        }

        return ret;
    }

    @Override
    public Character getCharacterValue(String path) {
        String sval = this.getStringValue(path);
        if (sval != null && sval.length() > 0) {
            return sval.charAt(0);
        }
        return null;
    }

    @Override
    public Character[] getCharacterValues(String path) {
        String[] svals = this.getStringValues(path);
        if (svals == null)
            return null;

        Character[] ret = new Character[svals.length];

        for (int i = 0; i < svals.length; i++) {
            if (svals[i].length() > 0)
                ret[i] = svals[i].charAt(0);
            else
                return null;
        }

        return ret;
    }

}
