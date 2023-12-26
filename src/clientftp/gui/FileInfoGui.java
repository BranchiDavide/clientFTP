package clientftp.gui;

import clientftp.exceptions.FTPOperationException;
import clientftp.ftp.FTPManager;

import org.apache.commons.net.ftp.FTPFile;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Classe FileInfoGui che genera una finestra con delle informazioni
 * aggiuntive di un file o di una cartella.
 * Nella GUI viene mostrato il nome del file, il tipo del file,
 * la sua dimensione, l'ultima data di modifica, i permessi, il proprietario e il gruppo.
 *
 * @author Davide Branchi
 * @version 04.06.2023
 */
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

    /**
     * Metodo costruttore della classe che genera la finestra con le
     * informazioni aggiuntive di un file o di una cartella.
     * @param file file o cartella di cui mostrate le informazioni
     * @param ftpManager oggetto FTPManager per andare a recuperare le informazioni dal server
     * @throws FTPOperationException
     */
    public FileInfoGui(FTPFile file, FTPManager ftpManager) throws FTPOperationException, IOException {
        this.file = file;
        this.ftpManager = ftpManager;
        frame = new JFrame(file.getName() + " - information");
        frame.setSize(400,500);
        frame.setResizable(false);
        try{
            frame.setIconImage(ImageIO.read(new File("Assets/icon.png")));
        }catch(IOException ex){
            System.out.println("Unable to load the app icon");
        }
        frame.setLayout(new GridLayout(8,1));
        fileName = new JLabel("File name: " + file.getName());
        fileType = new JLabel("File type: " + (file.isDirectory() ? "Directory" : "File"));
        //ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if(file.isDirectory()){
            icn = new ImageIcon("Assets/dir.png");
            //icn = new ImageIcon(ImageIO.read(classLoader.getResourceAsStream("Assets/dir.png")));
        }else{
            icn = new ImageIcon("Assets/file.png");
            //icn = new ImageIcon(ImageIO.read(classLoader.getResourceAsStream("Assets/file.png")));
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
            /*
            Creazione di un nuovo thread per caricare la dimensione di una
            cartella senza bloccare l'esecuzione di tutto il programma.
             */
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

    /**
     * Metodo utilizzato per caricare la dimensione in byte e il numero di files
     * totali contenuti in una cartella.
     * @throws FTPOperationException
     */
    private void loadDirSize() throws FTPOperationException {
        fileSize.setText("File size: " + ftpManager.formatSize(ftpManager.getDirTotalSize(ftpManager.getFtpPathString()
                + "/" + file.getName())) + " | Total files: " + ftpManager.getDirTotalFiles(ftpManager.getFtpPathString() + "/" + file.getName()));
    }
}
