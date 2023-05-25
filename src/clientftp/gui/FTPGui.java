package clientftp.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import clientftp.ftp.FTPManager;
import org.apache.commons.net.ftp.FTPClient;

public class FTPGui {
    private JFrame frame;
    private String[][] data;
    private String[] columnNames = { "Type", "Name", "Size", "Last Modified" };
    private JTable fileTable;
    private FTPManager ftpManager;
    private JPopupMenu clickPopup = new JPopupMenu();
    private JMenuItem infoMenu = new JMenuItem("Show more information");
    private JMenuItem downloadMenu = new JMenuItem("Download");
    private int rowClickIndex = 0;
    private DefaultTableModel tableModel;
    public FTPGui(String name, FTPManager ftpManager){
        this.ftpManager = ftpManager;
        frame = new JFrame(name);
        frame.setVisible(true);
        frame.setSize(1000,600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try{
            data = ftpManager.getFiles();
            fileTable = new JTable(data, columnNames);
        }catch(Exception e){
            showError("Loading Error", "Unable to load the table");
        }
        frame.add(new JScrollPane(fileTable));
        fileTable.setFont(new Font("Sans-Serif", Font.PLAIN, 14));
        clickPopup.add(infoMenu);
        clickPopup.add(downloadMenu);
        setTableModel();
        //Row click listener
        fileTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int r = fileTable.rowAtPoint(e.getPoint());
                if (r >= 0 && r < fileTable.getRowCount()) {
                    fileTable.setRowSelectionInterval(r, r);
                } else {
                    fileTable.clearSelection();
                }
                int rowindex = fileTable.getSelectedRow();
                if (rowindex < 0)
                    return;
                if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
                    //Right click on table row
                    clickPopup.show(e.getComponent(), e.getX(), e.getY());
                    rowClickIndex = rowindex;
                }else{
                    rowClickIndex = rowindex;
                    //Left click on table row
                    if(ftpManager.getFile(rowClickIndex).isDirectory()){
                        try{
                            //Change the directory
                            ftpManager.changeDir(ftpManager.getFile(rowClickIndex).getName());
                            //Update the table
                            data = ftpManager.getFiles();
                            tableModel.setRowCount(0);
                            for(Object[] row : data){
                                tableModel.addRow(row);
                            }
                        }catch(IOException ex){
                            showError("Change Directory Error", "Unable to enter the directory");
                        }
                    }
                }
            }
        });
        infoMenu.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                FileInfoGui fig = new FileInfoGui(ftpManager.getFile(rowClickIndex));
            }
        });
    }
    private void setTableModel(){
        //Model for making table cells not editable
        tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        fileTable.setModel(tableModel);
        //Type colum width
        fileTable.getColumnModel().getColumn(0).setMinWidth(150);
        fileTable.getColumnModel().getColumn(0).setMaxWidth(150);
        //Size colum width
        fileTable.getColumnModel().getColumn(2).setMinWidth(150);
        fileTable.getColumnModel().getColumn(2).setMaxWidth(150);
        //Last Modified colum width
        fileTable.getColumnModel().getColumn(3).setMinWidth(200);
        fileTable.getColumnModel().getColumn(3).setMaxWidth(200);
    }
    public void showError(String header, String message){
        JOptionPane.showMessageDialog(frame, message,
                header, JOptionPane.ERROR_MESSAGE);
    }
}
