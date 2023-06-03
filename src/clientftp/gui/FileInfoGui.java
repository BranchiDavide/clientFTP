package clientftp.gui;

import clientftp.exceptions.FTPOperationException;
import org.apache.commons.net.ftp.FTPFile;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;

import clientftp.ftp.FTPManager;

public class FileInfoGui {
    private JFrame frame;
    private FTPFile file;
    private FTPManager ftpManager;
    private JLabel fileName;
    private JLabel fileType;
    private JLabel fileSize;
    private JLabel lastModified;
    private JLabel permission;
    private JLabel owner;
    private JLabel group;
    private ImageIcon icn;
    private Image scaleImage;
    public FileInfoGui(FTPFile file, FTPManager ftpManager) throws IOException {
        this.file = file;
        this.ftpManager = ftpManager;
        frame = new JFrame(file.getName() + " - information");
        frame.setSize(400,500);
        frame.setResizable(false);
        frame.setLayout(new GridLayout(8,1));
        fileName = new JLabel("File name: " + file.getName());
        fileType = new JLabel("File type: " + (file.isDirectory() ? "Directory" : "File"));
        if(file.isDirectory()){
            icn = new ImageIcon("Assets/dir.png");
        }else{
            icn = new ImageIcon("Assets/file.png");
        }
        scaleImage = icn.getImage().getScaledInstance(70, 70,Image.SCALE_DEFAULT);
        icn = new ImageIcon(scaleImage);
        fileSize = new JLabel("File size: " + (file.isDirectory() ? "Loading..." : ftpManager.formatSize(file.getSize())));
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy - HH:mm");
        lastModified = new JLabel("Last modified: " + dateFormat.format(file.getTimestamp().getTime()));
        permission = new JLabel("Permission: " +  file.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION));
        owner = new JLabel("Owner: " + file.getUser());
        group = new JLabel("Group: " + file.getGroup());
        frame.add(new JLabel(icn));
        frame.add(fileName);
        frame.add(fileType);
        frame.add(fileSize);
        frame.add(lastModified);
        frame.add(permission);
        frame.add(owner);
        frame.add(group);
        frame.setVisible(true);
        if(file.isDirectory()){
            //New thread for loading the directory size without blocking the GUI loading
            Thread trd = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        loadDirSize();
                    } catch (FTPOperationException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            trd.start();
        }
    }
    private void loadDirSize() throws FTPOperationException {
        fileSize.setText("File size: " + ftpManager.formatSize(ftpManager.getDirTotalSize(ftpManager.getFtpPathString()
                + "/" + file.getName())) + " | Total files: " + ftpManager.getDirTotalFiles(ftpManager.getFtpPathString() + "/" + file.getName()));
    }
}
