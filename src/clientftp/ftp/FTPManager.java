package clientftp.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class FTPManager {
    private FTPClient ftp;
    private String[][] tableContent;
    private FTPFile[] ftpFiles;
    private ArrayList<FTPFile> contentList;
    private ArrayList<String> ftpPath;
    public FTPManager(FTPClient ftp){
        this.ftp = ftp;
        ftpPath = new ArrayList<>();
        ftpPath.add("/");
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
                directories.add(ftpFiles[i]);
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
}
