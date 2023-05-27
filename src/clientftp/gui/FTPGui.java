package clientftp.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import clientftp.ftp.FTPManager;

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
    private JMenuBar menuBar = new JMenuBar();
    private JMenu settingsMenu = new JMenu("Settings");
    private JMenuItem setDwPath = new JMenuItem("Set download path");
    private JProgressBar pb = new JProgressBar();
    private boolean areEventsDisabled = false;
    public FTPGui(String name, FTPManager ftpManager){
        this.ftpManager = ftpManager;
        frame = new JFrame(name);
        frame.setVisible(true);
        frame.setLayout(new BorderLayout());
        settingsMenu.add(setDwPath);
        menuBar.add(settingsMenu);
        frame.add(menuBar, BorderLayout.NORTH);
        frame.add(pb, BorderLayout.SOUTH);
        pb.setSize(1000,100);
        pb.setVisible(false);
        frame.setSize(1000,600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try{
            data = ftpManager.getFiles();
            fileTable = new JTable(data, columnNames);
        }catch(Exception e){
            showError("Loading Error", "Unable to load the table");
        }
        frame.add(new JScrollPane(fileTable), BorderLayout.CENTER);
        fileTable.setFont(new Font("Sans-Serif", Font.PLAIN, 14));
        clickPopup.add(infoMenu);
        clickPopup.add(downloadMenu);
        setTableModel();
        //Row click listener
        fileTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if(!areEventsDisabled){
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
                        if(!ftpManager.getFile(rowindex).getName().equals("..")){
                            clickPopup.show(e.getComponent(), e.getX(), e.getY());
                        }
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
                                    if(row[0] != null){
                                        tableModel.addRow(row);
                                    }
                                }
                            }catch(IOException ex){
                                showError("Change Directory Error", "Unable to enter the directory");
                            }
                        }
                    }
                }else{
                    showWarning("Download in progress", "Please wait until the download finishes");
                }
            }
        });
        infoMenu.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try{
                    FileInfoGui fig = new FileInfoGui(ftpManager.getFile(rowClickIndex));
                }catch(IOException ex){
                    showError("Information error", "Unable to load some or all the information");
                }
            }
        });
        downloadMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Thread for the file and directory download
                Thread trd = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            areEventsDisabled = true;
                            ftpManager.downloadFile(rowClickIndex, pb);
                            if(ftpManager.getFile(rowClickIndex).isDirectory()){
                                showSuccess("Download successful", "The directory " + ftpManager.getFile(rowClickIndex).getName()
                                        + " has been downloaded successfully!\n" + ftpManager.getDownloadPath() + ftpManager.getFile(rowClickIndex).getName());

                            }else{
                                showSuccess("Download successful", "The file " + ftpManager.getFile(rowClickIndex).getName()
                                        + " has been downloaded successfully!\n" + ftpManager.getDownloadPath() + ftpManager.getFile(rowClickIndex).getName());

                            }
                        } catch (IOException ex) {
                            showError("Download error", "Unable to download the file");
                        }finally{
                            areEventsDisabled = false;
                            pb.setVisible(false);
                        }
                    }
                });
                trd.start();
            }
        });
        setDwPath.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UIManager.put("OptionPane.minimumSize",new Dimension(600,100));
                String path = JOptionPane.showInputDialog(null,"Current download path: " + ftpManager.getDownloadPath() + "\nSet download directory path", "Settings", JOptionPane.QUESTION_MESSAGE);
                if(path != null){
                    if(Files.exists(Paths.get(path)) && path.length() > 0){
                        try{
                            ftpManager.setDownloadPath(path);
                        }catch(IOException ex){
                            showError("Settings error", "Unable to write settings file");
                        }
                    }else{
                        showError("Settings error", "The path doesn't exists!");
                    }
                }
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
        UIManager.put("OptionPane.minimumSize",new Dimension(100,100));
        JOptionPane.showMessageDialog(frame, message,
                header, JOptionPane.ERROR_MESSAGE);
    }
    public void showSuccess(String header, String message){
        UIManager.put("OptionPane.minimumSize",new Dimension(100,100));
        JOptionPane.showMessageDialog(frame, message,
                header, JOptionPane.INFORMATION_MESSAGE);
    }
    public void showWarning(String header, String message){
        UIManager.put("OptionPane.minimumSize",new Dimension(100,100));
        JOptionPane.showMessageDialog(frame, message,
                header, JOptionPane.WARNING_MESSAGE);
    }
}
