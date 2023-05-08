import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class FTPManager {
    private FTPClient ftp;
    private String[][] files;
    private FTPFile[] ftpFiles;
    public FTPManager(FTPClient ftp){
        this.ftp = ftp;
    }
    public String[][] getFiles() throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy - HH:mm");
        ftpFiles = ftp.listFiles();
        files = new String[ftpFiles.length][4];
        for(int i = 0; i < ftpFiles.length; i++){
            if(ftpFiles[i].isDirectory()){
                files[i][0] = "Directory";
                files[i][1] = ftpFiles[i].getName();
                files[i][2] = "";
                files[i][3] = dateFormat.format(ftpFiles[i].getTimestamp().getTime());
            }else{
                files[i][0] = "File";
                files[i][1] = ftpFiles[i].getName();
                files[i][2] = formatSize(ftpFiles[i].getSize());
                files[i][3] = dateFormat.format(ftpFiles[i].getTimestamp().getTime());
            }
        }
        return files;
    }

    private static String formatSize(long size) {
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double formattedSize = size;

        while (formattedSize >= 1024 && unitIndex < units.length - 1) {
            formattedSize /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", formattedSize, units[unitIndex]);
    }
}
