import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class Connection {
    private String username;
    private String password;
    private String serverAddress;
    private int serverPort;
    private FTPClient ftp;
    public Connection(String username, String password, String serverAddress, String serevrPort) throws Exception {
        if(username.equals("") || password.equals("") || serverAddress.equals("") || serevrPort.equals("")){
            throw new Exception("Please fill in all fields");
        }
        this.username = username.trim();
        this.password = password;
        this.serverAddress = serverAddress.trim();
        try{
            this.serverPort = Integer.parseInt(serevrPort.trim());
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
}
