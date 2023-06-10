package clientftp.ftp;

import clientftp.exceptions.FTPOperationException;
import clientftp.exceptions.FTPSettingsLoadingException;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Classe FTPManager che contiene tutti i metodi utili per effettuare
 * operazioni sul server FTP.
 * Alcuni dei metodi presenti nella classe permettono ad esempio di effettuare
 * il download/upload di files e cartelle.
 *
 * @author Davide Branchi
 * @version 04.06.2023
 */
public class FTPManager {
    private FTPClient ftp;
    private String[][] tableContent;
    private FTPFile[] ftpFiles;
    private ArrayList<FTPFile> contentList;
    private ArrayList<String> ftpPath;
    private String downloadPath;
    private int tmpProgress = 0;
    private JProgressBar pb;

    /**
     * Metodo costruttore della classe a cui deve essere passato come parametro
     * l'oggetto FTPClient per poter eseguire delle operazioni sul server FTP.
     * Nel metodo costruttore vengono anche caricare le impostazioni dell'utente
     * salvate nel file settings.xml
     * @param ftp oggetto FTPClient
     * @throws FTPSettingsLoadingException
     * @throws FTPOperationException
     */
    public FTPManager(FTPClient ftp) throws FTPSettingsLoadingException, FTPOperationException {
        this.ftp = ftp;
        this.pb = pb;
        ftpPath = new ArrayList<>();
        ftpPath.add("/");
        //Load settings from settings.xml file
        Properties loadProps = new Properties();
        try{
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
        }catch(IOException ex){
            throw new FTPSettingsLoadingException("Unable to load settings from file settings.xml");
        }
        try{
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
        }catch(IOException ex){
            throw new FTPOperationException("Unable to set file type");
        }
    }

    /**
     * Metodo che va a leggere il contenuto della cartella in cui si trova l'FTPClient
     * e ritorna un array 2D di stringhe contenente tutte le informazioni sui files e cartelle.
     * @return array di stringhe contenente le informazioni su files e cartelle
     * @throws FTPOperationException
     */
    public String[][] getFiles() throws FTPOperationException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy - HH:mm");
        try{
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
        }catch(IOException ex){
            throw new FTPOperationException("Unable to list the files on this directory");
        }
    }

    /**
     * Metodo che permette di formattare un numero long in una grandezza
     * di memoria (da Byte a Terabyte).
     * @param size numero long che rappresenta l'occupazione di memoria di un file o di una cartella.
     * @return stringa formattata con unità di misura.
     */
    public String formatSize(long size) {
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double formattedSize = size;
        while (formattedSize >= 1024 && unitIndex < units.length - 1) {
            formattedSize /= 1024;
            unitIndex++;
        }
        return String.format("%.2f %s", formattedSize, units[unitIndex]);
    }

    /**
     * Metodo che ritorna il file posizionato alla posizione index nella
     * tabella visualizzata dall'utente.
     * @param index posizione del file da ritornare
     * @return file alla posizione index nella tabella
     */
    public FTPFile getFile(int index){
        return contentList.get(index);
    }

    /**
     * Metodo che cambia la cartella in cui si trova l'FTPClient
     * @param path percorso della cartella in cui entrare
     * @throws FTPOperationException
     */
    public void changeDir(String path) throws FTPOperationException{
        try{
            if(path.equals("..")){
                ftp.changeToParentDirectory();
                ftpPath.remove(ftpPath.size() - 1);
                ftpPath.remove(ftpPath.size() - 1);
            }else{
                ftp.changeWorkingDirectory(path);
                ftpPath.add(path);
                ftpPath.add("/");
            }
        }catch(IOException ex){
            throw new FTPOperationException("Unable to change directory");
        }
    }

    /**
     * Metodo che permette di effettuare il download di un file o di una cartella dal GUI.
     * Questo metodo viene invocato quando l'utente clicca sull'opzione download
     * nel menu visualizzato quando si clicca con il tasto destro su un file nella tabella.
     * @param index posizione del file o della cartella nella tabella
     * @throws FTPOperationException
     */
    public void downloadFileFromGUI(int index) throws FTPOperationException {
        tmpProgress = 0;
        if(contentList.get(index).isDirectory()){
            pb.setVisible(true);
            pb.setValue(0);
            pb.setForeground(Color.GREEN);
            pb.setMaximum(getDirTotalFiles(getFtpPathString() + contentList.get(index).getName()));
            downloadDir(getFtpPathString() + contentList.get(index).getName(), downloadPath + contentList.get(index).getName());
        }else{
            downloadFile(getFtpPathString() + contentList.get(index).getName(),downloadPath + contentList.get(index).getName());
        }
        pb.setVisible(false);
    }

    /**
     * Metodo ricorsivo che scarica la cartella dal server FTP.
     * @param rPath percorso remoto della cartella da scaricare sul server FTP
     * @param lPath percorso locale in cui salvare la cartella
     * @throws FTPOperationException
     */
    private void downloadDir(String rPath, String lPath) throws FTPOperationException {
        try{
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
                    downloadFile(rPath + "/" + f.getName(), lPath + "/" + f.getName());
                }
            }
        }catch(IOException ex){
            throw new FTPOperationException("Unable to download the directory");
        }
    }

    /**
     * Metodo che scarica il file dal server FTP.
     * @param rPath percorso remoto del file sul server FTP
     * @param lPath percorso locale in cui salvare il file
     * @throws FTPOperationException
     */
    private void downloadFile(String rPath, String lPath) throws FTPOperationException {
        try{
            FileOutputStream fos = new FileOutputStream(lPath);
            ftp.retrieveFile(rPath, fos);
            fos.close();
            tmpProgress++;
            pb.setValue(tmpProgress);
        }catch(IOException ex){
            throw new FTPOperationException("Unable to download the file");
        }
    }

    /**
     * Metodo che ritorna il percorso locale di download.
     * @return percorso locale dove vengono scaricati i files e le cartelle
     */
    public String getDownloadPath() {
        return downloadPath;
    }

    /**
     * Metodo che permette d'impostare il percorso di download locale
     * @param downloadPath percorso dove salvare i files e le cartelle scaricate
     * @throws FTPOperationException
     */
    public void setDownloadPath(String downloadPath) throws FTPOperationException {
        try{
            if(!Character.toString(downloadPath.charAt(downloadPath.length()-1)).equals("\\")){
                downloadPath += "\\";
            }
            Properties saveProps = new Properties();
            saveProps.setProperty("downloadPath", downloadPath);
            saveProps.storeToXML(new FileOutputStream("settings.xml"), "");
            this.downloadPath = downloadPath;
        }catch(IOException ex){
            throw new FTPOperationException("Unable to set the download path");
        }
    }

    /**
     * Metodo che ritorna il percorso remoto in cui si trova l'FTPClient
     * @return percorso remoto in cu si trova l'FTPClient
     */
    public String getFtpPathString(){
        String out = "";
        for(String element : ftpPath){
            out += element;
        }
        return out;
    }

    /**
     * Metodo che ritorna la dimensione in byte di una cartella sul server FTP
     * @param rPath percorso della cartella sul server FTP
     * @return dimensione della cartella in byte
     * @throws FTPOperationException
     */
    public long getDirTotalSize(String rPath) throws FTPOperationException {
        try{
            FTPFile[] files = ftp.listFiles(rPath);
            long totalSize = 0;
            if (files != null && files.length > 0) {
                for (FTPFile file : files) {
                    if(!file.getName().equals("..") && !file.getName().equals(".")){
                        String remoteFilePath = rPath + "/" + file.getName();
                        if (file.isFile()) {
                            totalSize += file.getSize();
                        } else if (file.isDirectory()) {
                            totalSize += getDirTotalSize(remoteFilePath);
                        }
                    }
                }
            }
            return totalSize;
        }catch(IOException ex){
            throw new FTPOperationException("Unable to get the directory total size");
        }
    }

    /**
     * Metodo che ritorna il numero totale di files contenuti in una cartella sul server FTP
     * @param rPath percorso della cartella sul server FTP
     * @return numero totale di files contenuti nella cartella
     * @throws FTPOperationException
     */
    public int getDirTotalFiles(String rPath) throws FTPOperationException {
        try{
            FTPFile[] files = ftp.listFiles(rPath);
            int totalFiles = 0;
            if (files != null && files.length > 0) {
                for (FTPFile file : files) {
                    if(!file.getName().equals("..") && !file.getName().equals(".")){
                        String remoteFilePath = rPath + "/" + file.getName();
                        if (file.isFile()) {
                            totalFiles++;
                        } else if (file.isDirectory()) {
                            totalFiles += getDirTotalFiles(remoteFilePath);
                        }
                    }
                }
            }
            return totalFiles;
        }catch(IOException ex){
            throw new FTPOperationException("Unable to get the total number of files in the directory");
        }
    }

    /**
     * Metodo che disconnette l'FTPClient dal server FTP
     * @throws FTPOperationException
     */
    public void disconnectFromServer() throws FTPOperationException {
        try{
            ftp.disconnect();
        }catch(IOException ex){
            throw new FTPOperationException("Unable to disconnect from the server");
        }
    }

    /**
     * Metodo che permette di caricare un file sul server FTP
     * @param rPath percorso remoto in cui caricare il file
     * @param file oggetto file contenente il file locale da caricare sul server FTP
     * @throws FTPOperationException
     */
    public void uploadFile(String rPath, File file) throws FTPOperationException {
        try{
            InputStream is = new FileInputStream(file);
            ftp.storeFile(rPath, is);
            is.close();
            tmpProgress++;
            pb.setValue(tmpProgress);
        }catch(IOException ex){
            throw new FTPOperationException("Unable to upload the file");
        }
    }

    /**
     * Metodo ricorsivo che permette di caricare una cartella sul server FTP
     * @param rPath percorso remoto in cui caricare la cartella sul server FTP
     * @param directory oggetto file contenente la cartella locale da caricare sul server FTP
     * @throws FTPOperationException
     */
    public void uploadDirectory(String rPath, File directory) throws FTPOperationException {
        try{
            File[] files = directory.listFiles();
            ftp.makeDirectory(rPath);
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.isFile()) {
                        uploadFile(rPath + "/" + file.getName(), file);
                    } else if (file.isDirectory()) {
                        String subDirectoryPath = rPath + "/" + file.getName();
                        ftp.makeDirectory(subDirectoryPath);
                        uploadDirectory(subDirectoryPath, file);
                    }
                }
            }
        }catch(IOException ex){
            throw new FTPOperationException("Unable to upload the directory");
        }
    }

    /**
     * Metodo che permette d'impostare la ProgressBar che viene visualizzata dall'utente
     * quando carica/scarica/elimina un file o una cartella
     * @param pb oggetto JProgressBar che rappresenta la ProgressBar visualizzata sulla GUI
     */
    public void setPb(JProgressBar pb) {
        this.pb = pb;
    }

    /**
     * Metodo che permette d'impostare il progresso temporaneo durante il
     * caricamento/scaricamento/eliminazione di un file o di una cartella
     * @param tmpProgress
     */
    public void setTmpProgress(int tmpProgress) {
        this.tmpProgress = tmpProgress;
    }

    /**
     * Metodo che ritorna il numero totale di files contenuti in una cartella locale
     * @param directoryPath percorso locale della cartella
     * @return
     */
    public int getLocalDirTotalFiles(String directoryPath) {
        File directory = new File(directoryPath);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                int count = 0;
                for (File file : files) {
                    if (file.isFile()) {
                        count++;
                    } else if (file.isDirectory()) {
                        count += getLocalDirTotalFiles(file.getAbsolutePath());
                    }
                }
                return count;
            }
        }
        return 0;
    }

    /**
     * Metodo che elimina un file dal server FTP
     * @param rPath percorso remoto del file da eliminare
     * @throws FTPOperationException
     */
    public void deleteFile(String rPath) throws FTPOperationException {
        try{
            ftp.deleteFile(rPath);
            tmpProgress++;
            pb.setValue(tmpProgress);
        }catch(IOException ex){
            throw new FTPOperationException("Unable to delete the file");
        }
    }

    /**
     * Metodo ricorsivo che elimina una cartella dal server FTP
     * @param rPath percorso remoto della cartella da eliminare
     * @throws FTPOperationException
     */
    public void deleteDirectory(String rPath) throws FTPOperationException {
        try{
            FTPFile[] files = ftp.listFiles(rPath);
            if (files != null) {
                for (FTPFile file : files) {
                    if(!file.getName().equals("..") && !file.getName().equals(".")){
                        String filePath = rPath + "/" + file.getName();
                        if (file.isDirectory()) {
                            deleteDirectory(filePath);
                        } else {
                            deleteFile(filePath);
                        }
                    }
                }
            }
            ftp.removeDirectory(rPath);
        }catch(IOException ex){
            throw new FTPOperationException("Unable to delete the directory");
        }
    }
}
