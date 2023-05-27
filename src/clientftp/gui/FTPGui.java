package clientftp.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import clientftp.ftp.FTPManager;
import clientftp.Main;

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
    private JButton disconnectMenu = new JButton("Disconnect");
    private JButton uploadMenu = new JButton("Upload");
    private JMenuItem setDwPath = new JMenuItem("Set download path");
    private JProgressBar pb = new JProgressBar();
    private boolean areEventsDisabled = false;
    private JLabel displaiedPath = new JLabel("Remote path: /");
    private JFileChooser fChooser = new JFileChooser();
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
        menuBar.add(new JLabel("       "));
        menuBar.add(uploadMenu);
        menuBar.add(new JLabel("       "));
        menuBar.add(disconnectMenu);
        menuBar.add(new JLabel("       "));
        menuBar.add(displaiedPath);
        fChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fChooser.setDialogTitle("Upload a files or directories");
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
                                updateTable();
                                displaiedPath.setText("Remote path: " + ftpManager.getFtpPathString());
                            }catch(IOException ex){
                                showError("Change Directory Error", "Unable to enter the directory");
                            }
                        }
                    }
                }else{
                    showWarning("Operation in progress", "Please wait until the download/upload finishes");
                }
            }
        });
        infoMenu.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try{
                    FileInfoGui fig = new FileInfoGui(ftpManager.getFile(rowClickIndex), ftpManager);
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
                            ftpManager.downloadFileFromGUI(rowClickIndex, pb);
                            if(ftpManager.getFile(rowClickIndex).isDirectory()){
                                showSuccess("Download successful", "The directory " + ftpManager.getFile(rowClickIndex).getName()
                                        + " has been downloaded successfully!\n" + ftpManager.getDownloadPath() + ftpManager.getFile(rowClickIndex).getName());

                            }else{
                                showSuccess("Download successful", "The file " + ftpManager.getFile(rowClickIndex).getName()
                                        + " has been downloaded successfully!\n" + ftpManager.getDownloadPath() + ftpManager.getFile(rowClickIndex).getName());

                            }
                        } catch (IOException ex) {
                            showError("Download error", "Unable to download the file or the directory");
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
        disconnectMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ftpManager.disconnectFromServer();
                    frame.dispose();
                    new Main().main(new String[0]);
                } catch (IOException ex) {
                    showError("Disconnection error", "Unable to disconnect from the server");
                }
            }
        });
        uploadMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int choice = fChooser.showOpenDialog(null);
                if(choice == JFileChooser.APPROVE_OPTION){
                    File fileSelected = fChooser.getSelectedFile();
                    try{
                        if(fileSelected.isDirectory()){
                            ftpManager.uploadDirectory(ftpManager.getFtpPathString() + fileSelected.getName(),fileSelected);
                            showSuccess("Upload successful", "The directory " + fileSelected.getName()
                                    + " has been uploaded successfully!\n" + ftpManager.getFtpPathString() + fileSelected.getName());

                        }else{
                            ftpManager.uploadFile(ftpManager.getFtpPathString() + fileSelected.getName(), fileSelected);
                            showSuccess("Upload successful", "The file " + fileSelected.getName()
                                    + " has been uploaded successfully!\n" + ftpManager.getFtpPathString() + fileSelected.getName());
                        }
                        updateTable();
                    }catch(IOException ex){
                        showError("Upload error", "Unable to upload the file or the directory");
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
    private void updateTable() throws IOException {
        data = ftpManager.getFiles();
        tableModel.setRowCount(0);
        for(Object[] row : data){
            if(row[0] != null){
                tableModel.addRow(row);
            }
        }
    }
}
