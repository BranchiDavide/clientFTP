package clientftp.gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import clientftp.exceptions.FTPConnectionException;
import clientftp.exceptions.FTPOperationException;
import clientftp.exceptions.FTPSettingsLoadingException;
import clientftp.ftp.FTPConnection;
import clientftp.ftp.FTPManager;

/**
 * Classe ConnectionGui che visualizza la finestra per poter effettuare
 * una nuova connessione a un server FTP. La finestra contiene quattro input che
 * permettono all'utente di specificare l'indirizzo del server, la porta,
 * lo username e la password di accesso.
 *
 * @author Davide Branchi
 * @version 04.06.2023
 */
public class ConnectionGui {
    private JFrame frame = new JFrame("FTP Client - Connection");
    private JPanel mainPanel = new JPanel(new GridLayout(15,1));
    private JLabel serverAddressLabel = new JLabel("Server Address");
    private JLabel serverPortLabel = new JLabel("Server Port (Default: 21)");
    private JTextField serverAddress = new JTextField();
    private JTextField serverPort = new JTextField("21");
    private JLabel usernameLabel = new JLabel("Username");
    private JLabel passwordLabel = new JLabel("Password");
    private JTextField username = new JTextField();
    private JPasswordField password = new JPasswordField();
    private JButton connect = new JButton("Connect");
    private JLabel errorLabel = new JLabel("", SwingConstants.CENTER);

    /**
     * Metodo costruttore della classe che genera la finestra.
     */
    public ConnectionGui(){
        frame.setSize(400,400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setResizable(false);
        JLabel i = new JLabel("New FTP Connection", SwingConstants.CENTER);
        i.setFont(new Font("Sans-Serif", Font.BOLD, 20));
        frame.add(i, BorderLayout.NORTH);
        try{
            frame.setIconImage(ImageIO.read(new File("Assets/icon.png")));
        }catch(IOException ex){
            System.out.println("Unable to load the app icon");
        }
        mainPanel.add(new JLabel());
        mainPanel.add(serverAddressLabel);
        mainPanel.add(serverAddress);
        mainPanel.add(new JLabel());
        mainPanel.add(serverPortLabel);
        mainPanel.add(serverPort);
        mainPanel.add(new JLabel());
        mainPanel.add(usernameLabel);
        mainPanel.add(username);
        mainPanel.add(new JLabel());
        mainPanel.add(passwordLabel);
        mainPanel.add(password);
        mainPanel.add(new JLabel());
        mainPanel.add(errorLabel);
        mainPanel.add(new JLabel());
        frame.add(connect, BorderLayout.SOUTH);
        frame.setLocationRelativeTo(null);
        frame.getRootPane().setDefaultButton(connect);
        serverAddress.requestFocus();
        frame.setVisible(true);
        connect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try{
                    FTPConnection c = new FTPConnection(username.getText(), password.getText(), serverAddress.getText(), serverPort.getText());
                    c.connect();
                    FTPManager ftpManager = new FTPManager(c.getFtp());
                    new FTPGui("FTP Session - Connected to: " + serverAddress.getText() + " logged in as: " + username.getText(), ftpManager);
                    frame.setVisible(false);
                }catch(FTPConnectionException ex){
                    errorLabel.setFont(new Font("Sans-Serif", Font.BOLD, 12));
                    errorLabel.setForeground(Color.RED);
                    errorLabel.setText(ex.getMessage());
                } catch (FTPSettingsLoadingException ex) {
                    errorLabel.setFont(new Font("Sans-Serif", Font.BOLD, 12));
                    errorLabel.setForeground(Color.RED);
                    errorLabel.setText(ex.getMessage());
                } catch (FTPOperationException ex) {
                    errorLabel.setFont(new Font("Sans-Serif", Font.BOLD, 12));
                    errorLabel.setForeground(Color.RED);
                    errorLabel.setText(ex.getMessage());
                }
            }
        });
    }
}
