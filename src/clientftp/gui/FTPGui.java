package clientftp.gui;

import javax.imageio.ImageIO;
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

import clientftp.exceptions.FTPOperationException;
import clientftp.ftp.FTPManager;
import clientftp.Main;

/**
 * Classe FTPGui che genera la finestra dove l'utente può interagire con il server FTP.
 * In questa classe vengono anche ascoltati tutti gli eventi dell'utente ad esempio nel caso
 * l'utente volesse effettuare il download/upload/eliminazione di un file o di una cartella.
 *
 * @author Davide Branchi
 * @version 04.06.2023
 */
public class FTPGui {
    private JFrame frame;
    private String[][] data;
    private String[] columnNames = { "Type", "Name", "Size", "Last Modified" };
    private JTable fileTable;
    private FTPManager ftpManager;
    private JPopupMenu clickPopup = new JPopupMenu();
    private JMenuItem infoMenu = new JMenuItem("Show more information");
    private JMenuItem downloadMenu = new JMenuItem("Download");
    private JMenuItem deleteMenu = new JMenuItem("Delete");
    private int rowClickIndex = 0;
    private DefaultTableModel tableModel;
    private JMenuBar menuBar = new JMenuBar();
    private JMenu settingsMenu = new JMenu("Settings");
    private JButton disconnectMenu = new JButton("Disconnect");
    private JButton uploadMenu = new JButton("Upload");
    private JButton refreshMenu = new JButton(new ImageIcon(new ImageIcon("Assets/refresh.png").getImage().getScaledInstance(15, 15,  java.awt.Image.SCALE_SMOOTH)));
    //private JButton refreshMenu = new JButton(new ImageIcon(new ImageIcon(ImageIO.read(Thread.currentThread().getContextClassLoader().getResourceAsStream("Assets/refresh.png"))).getImage().getScaledInstance(15, 15,  java.awt.Image.SCALE_SMOOTH)));
    private JMenuItem setDwPath = new JMenuItem("Set download path");
    private JProgressBar pb = new JProgressBar();
    private boolean areEventsDisabled = false;
    private JLabel displaiedPath = new JLabel("Remote path: /");
    private JFileChooser fChooser = new JFileChooser();

    /**
     * Metodo costruttore della classe in cui viene generata la GUI
     * e dove vengono ascoltate tutte le interazioni dell'utente
     * @param name nome della finestra
     * @param ftpManager oggetto FTPManager che viene utilizzato per effettuare tutte le operazioni sul server FTP
     */
    public FTPGui(String name, FTPManager ftpManager) throws IOException {
        this.ftpManager = ftpManager;
        ftpManager.setPb(pb);;
        frame = new JFrame(name);
        frame.setVisible(true);
        frame.setLayout(new BorderLayout());
        try{
            frame.setIconImage(ImageIO.read(new File("Assets/icon.png")));
            //frame.setIconImage(ImageIO.read(Thread.currentThread().getContextClassLoader().getResourceAsStream("Assets/icon.png")));
        }catch(IOException ex){
            System.out.println("Unable to load the app icon");
        }
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
        menuBar.add(refreshMenu);
        menuBar.add(new JLabel("       "));
        menuBar.add(displaiedPath);
        fChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fChooser.setDialogTitle("Upload a files or directories");
        frame.setSize(1000,600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try{
            data = ftpManager.getFiles();
            fileTable = new JTable(data, columnNames);
        }catch(FTPOperationException e){
            showError("Loading Error", e.getMessage());
        }
        frame.add(new JScrollPane(fileTable), BorderLayout.CENTER);
        fileTable.setFont(new Font("Sans-Serif", Font.PLAIN, 14));
        clickPopup.add(infoMenu);
        clickPopup.add(downloadMenu);
        clickPopup.add(deleteMenu);
        setTableModel();
        /*
        Action listener per il click dell'utente su una riga della tabella
         */
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
                    int rowIndex = fileTable.getSelectedRow();
                    if (rowIndex < 0)
                        return;
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        //Right click on table row
                        if(!ftpManager.getFile(rowIndex).getName().equals("..")){
                            clickPopup.show(e.getComponent(), e.getX(), e.getY());
                        }
                        rowClickIndex = rowIndex;
                    }else{
                        rowClickIndex = rowIndex;
                        //Left click on table row
                        if(ftpManager.getFile(rowClickIndex).isDirectory()){
                            try{
                                //Change the directory
                                ftpManager.changeDir(ftpManager.getFile(rowClickIndex).getName());
                                //Update the table
                                updateTable();
                                displaiedPath.setText("Remote path: " + ftpManager.getFtpPathString());
                            }catch(FTPOperationException ex){
                                showError("Error", ex.getMessage());
                            }
                        }
                    }
                }else{
                    showWarning("Operation in progress", "Please wait until the download/upload/deletion finishes");
                }
            }
        });
        /*
        Action listener per il click dell'utente sul menu per visualizzare
        informazioni aggiuntive su un file o una cartella
         */
        infoMenu.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try{
                    FileInfoGui fig = new FileInfoGui(ftpManager.getFile(rowClickIndex), ftpManager);
                }catch(FTPOperationException ex){
                    showError("Information error", "Unable to load some or all the information");
                } catch (IOException ex) {
                    showError("Information error", "Unable to load icon");
                }
            }
        });
        /*
        Action listener per il click dell'utente sul menu di download
         */
        downloadMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                /*
                Thread per non bloccare tutto il programma durante il download di un file o di una cartella
                 */
                Thread trd = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            areEventsDisabled = true;
                            ftpManager.downloadFileFromGUI(rowClickIndex);
                            if(ftpManager.getFile(rowClickIndex).isDirectory()){
                                showSuccess("Download successful", "The directory " + ftpManager.getFile(rowClickIndex).getName()
                                        + " has been downloaded successfully!\n" + ftpManager.getDownloadPath() + ftpManager.getFile(rowClickIndex).getName());

                            }else{
                                showSuccess("Download successful", "The file " + ftpManager.getFile(rowClickIndex).getName()
                                        + " has been downloaded successfully!\n" + ftpManager.getDownloadPath() + ftpManager.getFile(rowClickIndex).getName());

                            }
                        } catch (FTPOperationException ex) {
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
        /*
        Action listener per il click dell'utente sul menu per impostare il percorso locale di download
         */
        setDwPath.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UIManager.put("OptionPane.minimumSize",new Dimension(600,100));
                String path = JOptionPane.showInputDialog(null,"Current download path: " + ftpManager.getDownloadPath() + "\nSet download directory path", "Settings", JOptionPane.QUESTION_MESSAGE);
                if(path != null){
                    if(Files.exists(Paths.get(path)) && path.length() > 0){
                        try{
                            ftpManager.setDownloadPath(path);
                        }catch(FTPOperationException ex){
                            showError("Settings error", "Unable to write settings file");
                        }
                    }else{
                        showError("Settings error", "The path doesn't exists!");
                    }
                }
            }
        });
        /*
        Action listener per il click dell'utente sul menu per disconnettersi dal server
         */
        disconnectMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ftpManager.disconnectFromServer();
                    frame.dispose();
                    new Main().main(new String[0]);
                } catch (FTPOperationException ex) {
                    showError("Disconnection error", "Unable to disconnect from the server");
                }
            }
        });
        /*
        Action listener per il click dell'utente sul menu per caricare un file o una cartella
         */
        uploadMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                /*
                Thread per non bloccare tutto il programma durante l'upload di un file o di una cartella
                 */
                Thread trd = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int choice = fChooser.showOpenDialog(null);
                        if(choice == JFileChooser.APPROVE_OPTION){
                            File fileSelected = fChooser.getSelectedFile();
                            try{
                                if(fileSelected != null){
                                    areEventsDisabled = true;
                                    ftpManager.setTmpProgress(0);
                                    if(fileSelected.isDirectory()){
                                        pb.setForeground(Color.BLUE);
                                        pb.setValue(0);
                                        pb.setVisible(true);
                                        pb.setMaximum(ftpManager.getLocalDirTotalFiles(fileSelected.getAbsolutePath()));
                                        ftpManager.uploadDirectory(ftpManager.getFtpPathString() + fileSelected.getName(),fileSelected);
                                        showSuccess("Upload successful", "The directory " + fileSelected.getName()
                                                + " has been uploaded successfully!\n" + ftpManager.getFtpPathString() + fileSelected.getName());

                                    }else{
                                        ftpManager.uploadFile(ftpManager.getFtpPathString() + fileSelected.getName(), fileSelected);
                                        showSuccess("Upload successful", "The file " + fileSelected.getName()
                                                + " has been uploaded successfully!\n" + ftpManager.getFtpPathString() + fileSelected.getName());
                                    }
                                    updateTable();
                                }
                            }catch(FTPOperationException ex){
                                showError("Upload error", "Unable to upload the file or the directory");
                            }finally{
                                areEventsDisabled = false;
                                pb.setVisible(false);
                            }
                        }
                    }
                });
                trd.start();
            }
        });
        /*
        Action listener per il click dell'utente sull'icona per fare il refresh della
        tabella in cui vengono visualizzati i file e le cartelle
         */
        refreshMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    updateTable();
                } catch (FTPOperationException ex) {
                    showError("Update error", "Unable to update the file table");
                }
            }
        });
        /*
        Action listener per il click dell'utente sul menu per eliminare un file o una cartella
         */
        deleteMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int choice = showQuestion("Delete confirm", "Are you sure you want to delete " + ftpManager.getFile(rowClickIndex).getName() + " ?");
                if(choice == JOptionPane.YES_OPTION){
                    /*
                    Thread per non bloccare tutto il programma durante l'eliminazione di un file o di una cartella
                     */
                    Thread trd = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                if(ftpManager.getFile(rowClickIndex).isDirectory()){
                                    areEventsDisabled = true;
                                    ftpManager.setTmpProgress(0);
                                    pb.setForeground(Color.RED);
                                    pb.setValue(0);
                                    pb.setVisible(true);
                                    pb.setMaximum(ftpManager.getDirTotalFiles(ftpManager.getFtpPathString() + "/" + ftpManager.getFile(rowClickIndex).getName()));
                                    ftpManager.deleteDirectory(ftpManager.getFtpPathString() + "/" + ftpManager.getFile(rowClickIndex).getName());
                                    showSuccess("Deletion successful", "The directory " + ftpManager.getFile(rowClickIndex).getName() + " has been deleted successfully!");
                                }else{
                                    ftpManager.deleteFile(ftpManager.getFtpPathString() + "/" + ftpManager.getFile(rowClickIndex).getName());
                                    showSuccess("Deletion successful", "The file " + ftpManager.getFile(rowClickIndex).getName() + " has been deleted successfully!");
                                }
                                updateTable();
                            }catch(FTPOperationException ex){
                                ex.printStackTrace();
                            }finally{
                                areEventsDisabled = false;
                                pb.setVisible(false);
                            }
                        }
                    });
                    trd.start();
                }
            }
        });
    }

    /**
     * Metodo invocato nel costruttore che imposta il modello della tabella
     */
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

    /**
     * Metodo che mostra una finestra con un messaggio di errore
     * @param header titolo della finestra
     * @param message messaggio di errore
     */
    public void showError(String header, String message){
        UIManager.put("OptionPane.minimumSize",new Dimension(100,100));
        JOptionPane.showMessageDialog(frame, message,
                header, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Metodo che mostra una finestra con un messaggio di successo
     * @param header titolo della finestra
     * @param message messaggio di successo
     */
    public void showSuccess(String header, String message){
        UIManager.put("OptionPane.minimumSize",new Dimension(100,100));
        JOptionPane.showMessageDialog(frame, message,
                header, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Metodo che mostra una finestra con un avviso
     * @param header titolo della finestra
     * @param message messaggio di avviso
     */
    public void showWarning(String header, String message){
        UIManager.put("OptionPane.minimumSize",new Dimension(100,100));
        JOptionPane.showMessageDialog(frame, message,
                header, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Metodo che mostra una finestra di dialogo con l'utente dove può selezionare un'opzione fra "Yes" e "Cancel"
     * @param header titolo della finestra
     * @param message messaggio da visualizzare
     * @return int rappresentante la scelta dell'utente
     */
    public int showQuestion(String header, String message){
        UIManager.put("OptionPane.minimumSize",new Dimension(100,100));
        return JOptionPane.showConfirmDialog(null, message, header, JOptionPane.YES_NO_OPTION);
    }

    /**
     * Metodo che aggiorna la tabella in cui vengono visualizzati i files e le cartelle
     * @throws FTPOperationException
     */
    private void updateTable() throws FTPOperationException {
        data = ftpManager.getFiles();
        tableModel.setRowCount(0);
        for(Object[] row : data){
            if(row[0] != null){
                tableModel.addRow(row);
            }
        }
    }
}
