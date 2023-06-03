package clientftp.ftp;

import clientftp.exceptions.FTPConnectionException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;

/**
 * Classe FTPConnection che permette di connettersi a un server FTP.
 *
 * @author Davide Branchi
 * @version 04.06.2023
 */
public class FTPConnection {
    private String username;
    private String password;
    private String serverAddress;
    private int serverPort;
    private FTPClient ftp;

    /**
     * Metodo costruttore della classe che prende come parametri lo username, password, indirizzo
     * del server FTP e porta per effettuare la connessione.
     * @param username nome utente da utilizzare per effettuare il login nel server FTP
     * @param password password dell'utente da utilizzare per effettuare il login nel server FTP
     * @param serverAddress indirizzo del server FTP
     * @param serevrPort porta del server FTP
     * @throws FTPConnectionException
     */
    public FTPConnection(String username, String password, String serverAddress, String serevrPort) throws FTPConnectionException {
        setServerAddress(serverAddress.trim());
        try{
            setServerPort(Integer.parseInt(serevrPort.trim()));
        }catch(NumberFormatException e){
            throw new FTPConnectionException("Invalid port number");
        }
        setUsername(username.trim());
        setPassword(password);
        ftp = new FTPClient();
    }

    /**
     * Metodo che effettua la connessione e il login nel server FTP.
     * @throws FTPConnectionException
     */
    public void connect() throws FTPConnectionException {
        try {
            ftp.connect(serverAddress, serverPort);
            int replyCode = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                throw new FTPConnectionException("Unable to connect to the server, please check address and port");
            }
        }catch(IOException ex){
            throw new FTPConnectionException("Unable to connect to the server, please check address and port");
        }

        try{
            ftp.login(username, password);
            int replyCode = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                throw new FTPConnectionException("Unable to login to the server, please check username and password");
            }
        }catch(IOException ex){
            throw new FTPConnectionException("Unable to login to the server, please check username and password");
        }
    }

    /**
     * Metodo che ritorna il nome utente utilizzato per il login nel server FTP.
     * @return nome utente
     */
    public String getUsername() {
        return username;
    }

    /**
     * Metodo per impostare il nome utente da utilizzare per il login nel server FTP.
     * @param username nome utente
     * @throws FTPConnectionException
     */
    public void setUsername(String username) throws FTPConnectionException {
        if(username.equals("")){
            throw new FTPConnectionException("Please insert the username");
        }else{
            this.username = username;
        }
    }

    /**
     * Metodo che ritorna la password utilizzata per effettuare il login nel server FTP.
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Metodo per impostare la password da utilizzare per il login nel server FTP.
     * @param password
     * @throws FTPConnectionException
     */
    public void setPassword(String password) throws FTPConnectionException {
        if(password.equals("")){
            throw new FTPConnectionException("Please insert the password");
        }else{
            this.password = password;
        }
    }

    /**
     * Metodo che ritorna l'indirizzo del server FTP.
     * @return indirizzo del server FTP
     */
    public String getServerAddress() {
        return serverAddress;
    }

    /**
     * Metodo per impostare l'indirizzo del server FTP a cui connettersi.
     * @param serverAddress indirizzo del server FTP
     * @throws FTPConnectionException
     */
    public void setServerAddress(String serverAddress) throws FTPConnectionException {
        if(serverAddress.equals("")){
            throw new FTPConnectionException("Please insert the server address");
        }else{
            this.serverAddress = serverAddress;
        }
    }

    /**
     * Metodo che ritorna la porta del server FTP.
     * @return porta del server FTP
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * Metodo per impostare la porta del server FTP a cui connettersi.
     * @param serverPort porta del server FTP
     * @throws FTPConnectionException
     */
    public void setServerPort(int serverPort) throws FTPConnectionException {
        if(serverPort <= 0 || serverPort >= 65535){
            throw new FTPConnectionException("Invalid port number");
        }else{
            this.serverPort = serverPort;
        }
    }

    /**
     * Metodo che ritorna l'oggetto FTPClient utilizzato per
     * effettuare la connessione e il login nel server FTP.
     * @return oggetto FTPClient
     */
    public FTPClient getFtp() {
        return ftp;
    }

    /**
     * Metodo per impostare l'oggetto FTPClient da utilizzare per
     * effettuare la connessione e il login nel server FTP.
     * @param ftp oggetto FTPClient
     */
    private void setFtp(FTPClient ftp) {
        this.ftp = ftp;
    }
}
