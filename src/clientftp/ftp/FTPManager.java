package clientftp.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Properties;

public class FTPManager {
    private FTPClient ftp;
    private String[][] tableContent;
    private FTPFile[] ftpFiles;
    private ArrayList<FTPFile> contentList;
    private ArrayList<String> ftpPath;
    private String downloadPath;

    public FTPManager(FTPClient ftp) throws IOException {
        this.ftp = ftp;
        ftpPath = new ArrayList<>();
        ftpPath.add("/");
        //Load settings from settings.xml file
        Properties loadProps = new Properties();
        loadProps.loadFromXML(new FileInputStream("settings.xml"));
        String settingsPath = loadProps.getProperty("downloadPath");
        if(settingsPath.equals("")){
            Properties saveProps = new Properties();
            saveProps.setProperty("downloadPath", System.getProperty("user.home") + "\\");
            saveProps.storeToXML(new FileOutputStream("settings.xml"), "");
            downloadPath = System.getProperty("user.home") + "\\";
        }else{
            downloadPath = settingsPath;
        }
    }
    public String[][] getFiles() throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy - HH:mm");
        ftpFiles = ftp.listFiles();
        tableContent = new String[ftpFiles.length][4];
        contentList = new ArrayList();
        ArrayList<FTPFile> files = new ArrayList();
        ArrayList<FTPFile> directories = new ArrayList();
        for(int i = 0; i < ftpFiles.length; i++){
            if(ftpFiles[i].isDirectory()){
                if(!ftpFiles[i].getName().equals(".") && !ftpFiles[i].getName().equals("..")){
                    directories.add(ftpFiles[i]);
                }
            }else{
                files.add(ftpFiles[i]);
            }
        }
        int posEndDir = 0;
        if(ftpPath.size() != 1){
            //The user is not in the root directory
            FTPFile parentDir = new FTPFile();
            parentDir.setName("..");
            parentDir.setType(FTPFile.DIRECTORY_TYPE);
            tableContent = new String[ftpFiles.length+1][4];
            directories.add(0, parentDir);
        }
        for(int i = 0; i < directories.size(); i++){
            tableContent[i][0] = "Directory";
            tableContent[i][1] = directories.get(i).getName();
            tableContent[i][2] = "";
            try{
                tableContent[i][3] = dateFormat.format(directories.get(i).getTimestamp().getTime());
            }catch(Exception e){
                tableContent[i][3] = "";
            }
            contentList.add(directories.get(i));
            posEndDir++;
        }
        for(int i = 0; i < files.size(); i++){
            tableContent[posEndDir + i][0] = "File";
            tableContent[posEndDir + i][1] = files.get(i).getName();
            tableContent[posEndDir + i][2] = formatSize(files.get(i).getSize());
            tableContent[posEndDir + i][3] = dateFormat.format(files.get(i).getTimestamp().getTime());
            contentList.add(files.get(i));
        }
        return tableContent;
    }

    public static String formatSize(long size) {
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double formattedSize = size;
        while (formattedSize >= 1024 && unitIndex < units.length - 1) {
            formattedSize /= 1024;
            unitIndex++;
        }
        return String.format("%.2f %s", formattedSize, units[unitIndex]);
    }

    public FTPFile getFile(int index){
        return contentList.get(index);
    }

    public void changeDir(String path) throws IOException{
        if(path.equals("..")){
            ftp.changeToParentDirectory();
            ftpPath.remove(ftpPath.size() - 1);
            ftpPath.remove(ftpPath.size() - 1);
        }else{
            ftp.changeWorkingDirectory(path);
            ftpPath.add(path);
            ftpPath.add("/");
        }
    }

    public void downloadFile(int index) throws IOException {
        if(contentList.get(index).isDirectory()){
            downloadDir(getFtpPathString() + contentList.get(index).getName(), downloadPath + contentList.get(index).getName());
        }else{
            FileOutputStream fos = new FileOutputStream(downloadPath + contentList.get(index).getName());
            ftp.retrieveFile(getFtpPathString() + contentList.get(index).getName(), fos);
            fos.close();
        }
    }

    private void downloadDir(String rPath, String lPath) throws IOException {
        FTPFile[] dirFiles = ftp.listFiles(rPath);
        for(FTPFile f : dirFiles){
            if(f.isDirectory()){
                if(!f.getName().equals("..") && !f.getName().equals(".")){
                    if(!Files.exists(Paths.get(lPath + "/" + f.getName()))){
                        Files.createDirectories(Paths.get(lPath + "/" + f.getName()));
                    }
                    downloadDir(rPath + "/" + f.getName(), lPath + "/" + f.getName());
                }
            }else{
                if(!Files.exists(Paths.get(lPath))){
                    Files.createDirectories(Paths.get(lPath));
                }
                downloadDirFile(rPath + "/" + f.getName(), lPath + "/" + f.getName());
            }
        }
    }

    private void downloadDirFile(String rPath, String lPath) throws IOException {
        FileOutputStream fos = new FileOutputStream(lPath);
        ftp.retrieveFile(rPath, fos);
        fos.close();
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public void setDownloadPath(String downloadPath) throws IOException {
        if(!Character.toString(downloadPath.charAt(downloadPath.length()-1)).equals("\\")){
            downloadPath += "\\";
        }
        Properties saveProps = new Properties();
        saveProps.setProperty("downloadPath", downloadPath);
        saveProps.storeToXML(new FileOutputStream("settings.xml"), "");
        this.downloadPath = downloadPath;
    }
    public String getFtpPathString(){
        String out = "";
        for(String element : ftpPath){
            out += element;
        }
        return out;
    }
}
