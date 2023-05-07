import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConnectionGui {
    JFrame frame = new JFrame("FTP Client - Connection");
    JPanel mainPanel = new JPanel(new GridLayout(15,1));
    JLabel serverAddressLabel = new JLabel("Server Address");
    JLabel serverPortLabel = new JLabel("Server Port (Default: 21)");
    JTextField serverAddress = new JTextField();
    JTextField serverPort = new JTextField("21");
    JLabel usernameLabel = new JLabel("Username");
    JLabel passwordLabel = new JLabel("Password");
    JTextField username = new JTextField();
    JPasswordField password = new JPasswordField();
    JButton connect = new JButton("Connect");
    JLabel errorLabel = new JLabel("", SwingConstants.CENTER);
    public ConnectionGui(){
        frame.setVisible(true);
        frame.setSize(400,400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setResizable(false);
        JLabel i = new JLabel("New FTP Connection", SwingConstants.CENTER);
        i.setFont(new Font("Sans-Serif", Font.BOLD, 20));
        frame.add(i, BorderLayout.NORTH);
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
        connect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try{
                    Connection c = new Connection(username.getText(), password.getText(), serverAddress.getText(), serverPort.getText());
                    c.connect();
                }catch(Exception ex){
                    errorLabel.setFont(new Font("Sans-Serif", Font.BOLD, 12));
                    errorLabel.setForeground(Color.RED);
                    errorLabel.setText(ex.getMessage());
                }
            }
        });
    }
}
