import org.apache.commons.net.ftp.FTPClient;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class FTPGui {
    private JFrame frame;
    private String[][] data;

    private String[] columnNames = { "Type", "Name", "Size", "Last Modified" };
    private JTable fileTable;
    private FTPManager ftpManager;
    public FTPGui(String name, FTPClient ftp){
        ftpManager = new FTPManager(ftp);
        frame = new JFrame(name);
        frame.setVisible(true);
        frame.setSize(1000,600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try{
            data = ftpManager.getFiles();
            fileTable = new JTable(data, columnNames);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        frame.add(new JScrollPane(fileTable));
        fileTable.setFont(new Font("Sans-Serif", Font.PLAIN, 14));
        fileTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event) {
                if(!event.getValueIsAdjusting()){
                    rowClick(fileTable.getSelectedRow());
                }
            }
        });
        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        fileTable.setModel(tableModel);
    }
    public void rowClick(int row){
        System.out.println(row);
    }
}
