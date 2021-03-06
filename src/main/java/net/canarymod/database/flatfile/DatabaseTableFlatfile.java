package net.canarymod.database.flatfile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import net.canarymod.Logman;
import net.canarymod.database.DatabaseRow;
import net.canarymod.database.DatabaseTable;

/**
 * @author Jos Kuijpers
 */
public class DatabaseTableFlatfile implements DatabaseTable {

    private File tableFile;
    private String name;
    private String description;
    public ArrayList<String> columnNames; // Public for access by Row
    public ArrayList<String> columnTypes; // Public for access by Row
    private ArrayList<DatabaseRowFlatfile> rows;
    private DatabaseFlatfile database;

    public DatabaseTableFlatfile(DatabaseFlatfile database, String file)
            throws IOException {

        this.database = database;
        this.tableFile = new File("db/" + file);
        this.name = file.substring(0, file.indexOf(".")).toLowerCase();
        this.description = "";
        this.columnNames = new ArrayList<String>();
        this.columnTypes = new ArrayList<String>();
        this.rows = new ArrayList<DatabaseRowFlatfile>();

        if(!this.tableFile.exists()) {
            this.tableFile.createNewFile();
            return;
        }
        
        this.load();
    }

    @SuppressWarnings("resource")
    private void load() throws IOException {
        BufferedReader in = null;
        int rowId = 0;
        
        try {
            in = new BufferedReader(new FileReader(this.tableFile));

            String inLine;
            int parseState = 0; // 0 = desc, 1 = columnname, 2 = column types, 3
                                // = content

            while ((inLine = in.readLine()) != null) {
                if (inLine.startsWith("##")) {
                    // Description is optional
                    if (parseState == 0)
                        parseState = 1;

                    if (parseState == 1) { // column names
                        // Remove ##
                        inLine = inLine.substring(2);

                        // Split at column-separator
                        String[] items = inLine.split(":",-1);

                        // Handle all items
                        for (String i : items) {
                            if (this.columnNames.contains(i.toUpperCase())) {
                                throw new IOException(
                                        "Duplicate column names in "
                                                + this.tableFile.getPath());
                            }
                            this.columnNames.add(i.toUpperCase());
                        }
                        parseState = 2;
                    } else if (parseState == 2) { // column types
                        // Remove ##
                        inLine = inLine.substring(2);

                        // Split at column-separator
                        String[] items = inLine.split(":",-1);

                        if (items.length != this.columnNames.size())
                            throw new IOException(
                                    "Column type definitions do not cover all columns");

                        // Handle all items
                        for (int i = 0; i < items.length; i++) {
                            this.columnTypes.add(items[i].toUpperCase());
                        }

                        parseState = 3;
                    }

                    continue;
                }
                if (inLine.startsWith("#")) {
                    if (parseState != 0)
                        throw new IOException("Invalid format");

                    String appDesc = inLine.substring(1).trim();
                    this.description += ((this.description == "") ? "" : " ")
                            + appDesc;

                    continue;
                }
                if(inLine != null && !inLine.isEmpty()) {
                 // Get the cells
                    String[] cells = inLine.split(":",-1);

                    // Verify number of cells
                    if (cells.length != this.columnNames.size())
                        throw new IOException(
                                "Numbers of cells does not match number of columns("+cells.length+"/"+columnNames.size()+")");

                    DatabaseRowFlatfile row = new DatabaseRowFlatfile(this, cells, rowId);
                    this.rows.add(row);
                }
                rowId++;
            }
        } catch (IOException e) {
            if (in != null) {
                in.close();
            }
            throw e;
        }
        if (in != null) {
            in.close();
        }
    }

    public void reload() {
        // TODO implement
    }

    public void saveRow(DatabaseRowFlatfile row) {
        this.save();
    }

    public void save() {

        try {
            // Trash the files
            this.tableFile.delete();
            this.tableFile = new File("db/" + this.name + ".txt");

            BufferedWriter out = new BufferedWriter(new FileWriter(
                    this.tableFile, true));

            // Write the description
            out.write("# " + this.description);
            out.newLine();

            // Write table column names
            out.write("##");
            for (int i = 0; i < this.columnNames.size(); i++) {
                String cn = this.columnNames.get(i);
                out.write(((i == 0) ? "" : ":") + cn);
            }
            out.newLine();

            // Write table column types
            out.write("##");
            for (int i = 0; i < this.columnTypes.size(); i++) {
                String cn = this.columnTypes.get(i);
                out.write(((i == 0) ? "" : ":") + cn);
            }
            out.newLine();

            // Write rows
            for (DatabaseRowFlatfile r : this.rows) {
                for (int i = 0; i < r.cells.size(); i++) {
                    String cn = r.cells.get(i);
                    out.write(((i == 0) ? "" : ":") + cn);
                }
                out.newLine();
            }

            out.close();
        } catch (IOException e) {
            Logman.logWarning("An IOException occurred in table-file: '"
                    + this.tableFile.getPath() + "'");
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.tableFile.renameTo(new File("db/" + name + ".txt"));
        this.database.tableRenamed(this.name, name);
        this.name = name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int getNumRows() {
        return this.rows.size();
    }

    @Override
    public DatabaseRow getRow(int row) {
        return this.rows.get(row - 1);
    }

    @Override
    public DatabaseRow[] getAllRows() {
        DatabaseRow[] t = {};
        return this.rows.toArray(t);
    }

    @Override
    public DatabaseRow[] getFilteredRows(String column, String value) {
        // Find the column to check
        int position = this.getColumnPosition(column);
        if (position == -1) {
            return null;
        }

        // Find and store the rows
        ArrayList<DatabaseRow> ret = new ArrayList<DatabaseRow>();
        DatabaseRow[] retType = {};

        for (DatabaseRowFlatfile row : this.rows) {
            if (row.cells.get(position).equals(value)) {
                ret.add(row);
            }
        }

        // With no items, return no empty array but return null
        if (ret.size() == 0) {
            return null;
        }

        return ret.toArray(retType);
    }

    public boolean rowExists(String column, String value) {
        int position = this.getColumnPosition(column);
        if (position == -1) {
            return false;
        }
        
        for (DatabaseRowFlatfile row : this.rows) {
            if (row.cells.get(position).equals(value)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Find highest ID
     * @param dbr
     * @return
     */
    private DatabaseRowFlatfile verifyRowId(DatabaseRowFlatfile dbr) {
        for(DatabaseRowFlatfile row : rows) {
            if(row.getRowID() > dbr.getRowID()) {
                dbr.setRowId(row.getRowID()+1);
            }
        }
        return dbr;
    }
    
    @Override
    public DatabaseRow addRow() {
        int rowId = 1;
        if(rows.size() > 0) {
            rowId = rows.get(rows.size()-1).getRowID()+1;
        }
        DatabaseRowFlatfile newRow = new DatabaseRowFlatfile(this, null, rowId);
        newRow = verifyRowId(newRow);
        this.rows.add(newRow);
        return newRow;
    }

    @Override
    public void removeRow(DatabaseRow row) {
        this.rows.remove(row);
    }

    @Override
    public void removeRow(int row) {
        if (row < 1 || row > this.rows.size())
            return;
        this.rows.remove(row - 1);
    }

    public int getColumnPosition(String column) {
        return this.columnNames.indexOf(column.toUpperCase());
    }

    @Override
    public int getNumColumns() {
        return this.columnNames.size();
    }

    @Override
    public void appendColumn(String name, ColumnType type) { // TODO: throw
                                                             // invalid argument
                                                             // instead of
                                                             // return? or
                                                             // return bool?
        if (this.columnNames.contains(name.toUpperCase()))
            return;

        // Add headers
        this.columnNames.add(name.toUpperCase());
        this.columnTypes.add(type.name());

        // Add to all rows
        for (DatabaseRowFlatfile r : this.rows)
            r.cells.add("");
    }

    @Override
    public void removeColumn(String name) {
        int position = this.getColumnPosition(name);
        if (position == -1)
            return;

        // Remove the column for all rows
        for (DatabaseRowFlatfile r : this.rows)
            r.cells.remove(position);

        // Remove from headers
        this.columnNames.remove(position);
        this.columnTypes.remove(position);
    }

    @Override
    public void insertColumn(String name, ColumnType type, String after) {
        if (this.columnNames.contains(name.toUpperCase()))
            return;

        int position = this.getColumnPosition(after);
        // position is -1 or position of the column. We add it after
        position++;
        // Now position is 0 or the one after the given column :D

        // Quicker to append
        if (position == this.columnNames.size()) {
            this.appendColumn(name, type);
            return;
        }

        // Insert headers
        this.columnNames.add(position, name.toUpperCase());
        this.columnTypes.add(position, type.name());

        // Add to all rows
        for (DatabaseRowFlatfile r : this.rows)
            r.cells.add(position, "");
    }

    @Override
    public String[] getAllColumns() {
        String[] ar = {};
        return this.columnNames.toArray(ar);
    }

    @Override
    public void truncateTable() {
        this.rows.clear();
    }

}
