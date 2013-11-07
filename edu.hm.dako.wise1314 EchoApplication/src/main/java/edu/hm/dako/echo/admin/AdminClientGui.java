package edu.hm.dako.echo.admin;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import org.apache.log4j.PropertyConfigurator;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

/**
 * @author Christoph Friegel
 * @version 1.0
 */

public class AdminClientGui extends JPanel implements ActionListener {
	
    private static JFrame f; // Frame fuer Echo-Anwendungs-GUI 
    private static JPanel panel;

    /**
     * GUI-Komponenten
     */
    private JTextField one;
    private JLabel two, three, four;

    private JTextArea messageArea;
    private JScrollPane scrollPane;

    private Button countButton;
    private Button delButton;
    private Button beendenButton;

    private static final long serialVersionUID = 1000010000L;

    public AdminClientGui() {
        super(new BorderLayout());
    }

    private void initComponents() {

        /**
         * Create GUI
         */
        one = new JTextField();
        two = new JLabel("-");
        three = new JLabel("-");
        four = new JLabel("-");

        // Nachrichtenbereich mit Scrollbar
        messageArea = new JTextArea("", 5, 100);

        //messageArea.setLineWrap(true);
        scrollPane = new JScrollPane(messageArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Buttons
        countButton = new Button("Zählen");
        delButton = new Button("Löschen");
        beendenButton = new Button("Beenden");
    }


/**
 * buildPanel
 *
 * @return
 */
public JComponent buildPanel() {

    initComponents();

    // Layout definieren
    FormLayout layout = new FormLayout(
            "right:max(40dlu;pref), 3dlu, 70dlu, 7dlu, "
                    + "right:max(40dlu;pref), 3dlu, 70dlu",
            "p, 3dlu, p, 3dlu, p, 3dlu, p, 9dlu, " +
                    "p, 3dlu, p, 3dlu, p, 3dlu, p, 9dlu, " +
                    "p, 3dlu, p, 3dlu, p, 3dlu, p, 9dlu, " +
                    "p, 3dlu, p, 3dlu, p, 3dlu, p, 9dlu, " +
                    "p, 3dlu, p, 3dlu, " +
                    "p, 3dlu, p, 3dlu, p");

    panel = new JPanel(layout);
    panel.setBorder(Borders.DIALOG_BORDER);

    /*
           *  Panel mit Labels und Komponenten fuellen
           */

    CellConstraints cc = new CellConstraints();
    panel.add(createSeparator("Eingabe"), cc.xyw(1, 1, 7));
    panel.add(new JLabel("Client-ID"), cc.xy(1, 3));
    panel.add(one, cc.xy(7, 3));
    
    panel.add(createSeparator("Ausgabe"), cc.xyw(1, 15, 7));
    panel.add(new JLabel("Count:"), cc.xy(5, 17));
    panel.add(two, cc.xy(7, 17));
    panel.add(new JLabel("MinCountTime:"), cc.xy(1, 17));
    panel.add(three, cc.xy(3, 17));
    panel.add(new JLabel("MaxCountTime:"), cc.xy(1, 19));
    panel.add(four, cc.xy(3, 19));

    one.setText("1");

    
    // Meldungsbereich
    panel.add(scrollPane, cc.xyw(1, 33, 7));

    messageArea.setLineWrap(true);
    messageArea.setWrapStyleWord(true);
    messageArea.setEditable(false);
    messageArea.setCaretPosition(0);

    panel.add(createSeparator(""), cc.xyw(1, 35, 7));

    panel.add(countButton, cc.xyw(2, 37, 2));   //Starten
    panel.add(delButton, cc.xyw(4, 37, 2));       //Loeschen
    panel.add(beendenButton, cc.xyw(6, 37, 2)); //Abbrechen

    countButton.addActionListener(this);
    delButton.addActionListener(this);
    beendenButton.addActionListener(this);
    return panel;
}

/**
 * actionPerformed
 *
 */
public void actionPerformed(ActionEvent e) {

    if (e.getActionCommand().equals("Zählen")) {
        countAction(e);
    } else if (e.getActionCommand().equals("Löschen")) {
        delAction(e);
    } else if (e.getActionCommand().equals("Beenden"))
        finishAction(e);
}

/**
 * countAction
 *
 */
private void countAction(ActionEvent e) {
    AdminClient ac = new AdminClient();
    
    if(isInteger(one.getText()))
		try {
			Count count = ac.getClientCount( Integer.parseInt(one.getText()) );
			two.setText( count.getCountNr() );
			three.setText( count.getMaxDate() );
			four.setText( count.getMinDate() );
			setMessageLine(new java.util.Date() + ": Count gezählt!");
		} catch (Exception e1) {
			setMessageLine(e1.getMessage());
		}
	else
    	setMessageLine("Bitte nur numerische Zeichen als Cliend-ID verwenden!");
}

/**
 * delAction
 *
 */
private void delAction(ActionEvent e) {
    AdminClient ac = new AdminClient();
    
    try {
		ac.deleteAllData();
		setMessageLine(new java.util.Date() + ": Trace- und Count-Datenbank geleert!");
	} catch (Exception e1) {
		setMessageLine(e1.getMessage());
	}
}

/**
 * Aktion bei Betaetigung des "Beenden"-Buttons ausfuehren
 * 
 */
private void finishAction(ActionEvent e) {
    setMessageLine("Programm wird beendet...");

    // Programm beenden
    System.exit(0);
}

/**
 * Schlieï¿½en des Fensters und Beenden des Programms
 *
 * @param e
 */
public void windowClosing(WindowEvent e) {
    System.exit(0);
}

public void windowOpened(WindowEvent e) {
}

public void windowActivated(WindowEvent e) {
}

public void windowIconified(WindowEvent e) {
}

public void windowDeiconified(WindowEvent e) {
}

public void windowDeactivated(WindowEvent e) {
}

public void windowClosed(WindowEvent e) {
}

public synchronized void setMessageLine(String message) {
    messageArea.append(message + "\n");
    messageArea.update(messageArea.getGraphics());
}

private Component createSeparator(String text) {
    return DefaultComponentFactory.getInstance().createSeparator(text);
}

public static boolean isInteger(String s) {
    try { 
        Integer.parseInt(s); 
    } catch(NumberFormatException e) { 
        return false; 
    }
    return true;
}

	  public static void main(String[] args) throws Exception {
		    PropertyConfigurator.configureAndWatch("log4j.admin.properties", 60 * 1000);

		    f = new JFrame("Admin Client GUI");
		    f.setTitle("Administration");
		    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		    JComponent panel = new AdminClientGui().buildPanel();
		    f.getContentPane().add(panel);
		    f.pack();
		    f.setVisible(true);
	  }

}
