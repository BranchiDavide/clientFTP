package clientftp.ftp;

import clientftp.exceptions.FTPConnectionException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;

public class FTPConnection {
    private String username;
    private String password;
    private String serverAddress;
    private int serverPort;
    private FTPClient ftp;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) throws FTPConnectionException {
        if(username.equals("")){
            throw new FTPConnectionException("Please insert the username");
        }else{
            this.username = username;
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) throws FTPConnectionException {
        if(password.equals("")){
            throw new FTPConnectionException("Please insert the password");
        }else{
            this.password = password;
        }
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) throws FTPConnectionException {
        if(serverAddress.equals("")){
            throw new FTPConnectionException("Please insert the server address");
        }else{
            this.serverAddress = serverAddress;
        }
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) throws FTPConnectionException {
        if(serverPort <= 0 || serverPort >= 65535){
            throw new FTPConnectionException("Invalid port number");
        }else{
            this.serverPort = serverPort;
        }
    }

    public FTPClient getFtp() {
        return ftp;
    }

    private void setFtp(FTPClient ftp) {
        this.ftp = ftp;
    }
}
