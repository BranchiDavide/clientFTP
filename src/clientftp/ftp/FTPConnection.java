package clientftp.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class FTPConnection {
    private String username;
    private String password;
    private String serverAddress;
    private int serverPort;
    private FTPClient ftp;
    public FTPConnection(String username, String password, String serverAddress, String serevrPort) throws Exception {
        setUsername(username.trim());
        setPassword(password);
        setServerAddress(serverAddress.trim());
        try{
            setServerPort(Integer.parseInt(serevrPort.trim()));
        }catch(NumberFormatException e){
            throw new Exception("Invalid port number");
        }
        ftp = new FTPClient();
    }
    public void connect() throws Exception {
        try{
            ftp.connect(serverAddress, serverPort);
            int replyCode = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                throw new Exception("Unable to connect to the server, please check address and port");
            }
        }catch (Exception e){
            throw new Exception("Unable to connect to the server, please check address and port");
        }
        try{
            ftp.login(username, password);
            int replyCode = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                throw new Exception("Unable to login to the server, please check username and password");
            }
        }catch(Exception e){
            throw new Exception("Unable to login to the server, please check username and password");
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) throws Exception {
        if(username.equals("")){
            throw new Exception("Please insert the username");
        }else{
            this.username = username;
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) throws Exception {
        if(password.equals("")){
            throw new Exception("Please insert the password");
        }else{
            this.password = password;
        }
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) throws Exception {
        if(serverAddress.equals("")){
            throw new Exception("Please insert the server address");
        }else{
            this.serverAddress = serverAddress;
        }
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) throws Exception {
        if(serverPort <= 0 || serverPort >= 65535){
            throw new Exception("Invalid port number");
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
