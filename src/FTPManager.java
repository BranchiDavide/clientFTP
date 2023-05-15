import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class FTPManager {
    private FTPClient ftp;
    private String[][] tableContent;
    private FTPFile[] ftpFiles;
    public FTPManager(FTPClient ftp){
        this.ftp = ftp;
    }
    public String[][] getFiles() throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy - HH:mm");
        ftpFiles = ftp.listFiles();
        tableContent = new String[ftpFiles.length][4];
        ArrayList<FTPFile> files = new ArrayList();
        ArrayList<FTPFile> directories = new ArrayList();
        for(int i = 0; i < ftpFiles.length; i++){
            if(ftpFiles[i].isDirectory()){
                directories.add(ftpFiles[i]);
            }else{
                files.add(ftpFiles[i]);
            }
        }
        for(int i = 0; i < directories.size(); i++){
            tableContent[i][0] = "Directory";
            tableContent[i][1] = directories.get(i).getName();
            tableContent[i][2] = "";
            tableContent[i][3] = dateFormat.format(directories.get(i).getTimestamp().getTime());
        }
        for(int i = 0; i < files.size(); i++){
            tableContent[tableContent.length - 1 + i][0] = "File";
            tableContent[tableContent.length - 1 + i][1] = files.get(i).getName();
            tableContent[tableContent.length - 1 + i][2] = formatSize(files.get(i).getSize());
            tableContent[tableContent.length - 1 + i][3] = dateFormat.format(files.get(i).getTimestamp().getTime());
        }
        return tableContent;
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
