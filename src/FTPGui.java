import org.apache.commons.net.ftp.FTPClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FTPGui {
    private JFrame frame;
    private String[][] data;
    private String[] columnNames = { "Type", "Name", "Size", "Last Modified" };
    private JTable fileTable;
    private FTPManager ftpManager;
    private JPopupMenu clickPopup = new JPopupMenu();
    private JMenuItem infoMenu = new JMenuItem("Show more information");
    private JMenuItem downloadMenu = new JMenuItem("Download");
    public FTPGui(String name, FTPClient ftp){
        ftpManager = new FTPManager(ftp);
        frame = new JFrame(name);
        frame.setVisible(true);
        frame.setSize(1000,600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try{
            data = ftpManager.getFiles();
            fileTable = new JTable(data, columnNames);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        frame.add(new JScrollPane(fileTable));
        fileTable.setFont(new Font("Sans-Serif", Font.PLAIN, 14));
        clickPopup.add(infoMenu);
        clickPopup.add(downloadMenu);
        setTableModel();
        //Row click listener
        fileTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int r = fileTable.rowAtPoint(e.getPoint());
                if (r >= 0 && r < fileTable.getRowCount()) {
                    fileTable.setRowSelectionInterval(r, r);
                } else {
                    fileTable.clearSelection();
                }
                int rowindex = fileTable.getSelectedRow();
                if (rowindex < 0)
                    return;
                if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
                    //Right click on table row
                    clickPopup.show(e.getComponent(), e.getX(), e.getY());
                }else{
                    //Left click on table row
                }
            }
        });
    }
    private void setTableModel(){
        //Model for making table cells not editable
        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        fileTable.setModel(tableModel);
    }
}
