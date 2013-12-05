package edu.hm.dako.echo.admin.old;

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

public class AdminClientGui extends JPanel implements ActionListener
{

  private static JFrame f; // Frame fuer Echo-Anwendungs-GUI
  private static JPanel panel;
  private static AdminClient ac;

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

  public AdminClientGui()
  {
    super( new BorderLayout() );
  }

  private void initComponents()
  {

    /**
     * Create GUI
     */
    one = new JTextField();
    two = new JLabel( "-" );
    three = new JLabel( "-" );
    four = new JLabel( "-" );

    // Nachrichtenbereich mit Scrollbar
    messageArea = new JTextArea( "", 5, 100 );

    // messageArea.setLineWrap(true);
    scrollPane = new JScrollPane( messageArea );
    scrollPane
        .setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );

    // Buttons
    countButton = new Button( "CountNow!" );
    delButton = new Button( "DeleteEmAll" );
    beendenButton = new Button( "Exit" );
  }

  /**
   * buildPanel
   * 
   * @return
   */
  public JComponent buildPanel()
  {

    initComponents();

    // Layout definieren

    FormLayout layout = new FormLayout(
        "right:max(50dlu;p), 4dlu, 75dlu, 7dlu, " + "right:p, 4dlu, 75dlu",
        "p, 2dlu, p, 3dlu, p, 3dlu, p, 7dlu, "
            + "p, 2dlu, p, 3dlu, p, 3dlu, p," + "2dlu, p, 3dlu, p, 3dlu, p,"
            + "2dlu, p, 3dlu, " + "2dlu, p, 3dlu, p" );

    panel = new JPanel( layout );
    panel.setBorder( Borders.DIALOG_BORDER );

    /* Panel mit Labels und Komponenten fuellen */

    CellConstraints cc = new CellConstraints();
    panel.add( createSeparator( "Input" ), cc.xyw( 1, 1, 7 ) );
    panel.add( new JLabel( "Client-ID" ), cc.xy( 1, 3 ) );
    panel.add( one, cc.xy( 3, 3 ) );

    panel.add( createSeparator( "Output" ), cc.xyw( 1, 9, 7 ) );
    panel.add( new JLabel( "Count:" ), cc.xy( 1, 11 ) );
    panel.add( two, cc.xy( 3, 11 ) );
    panel.add( new JLabel( "MinCountTime:" ), cc.xy( 5, 11 ) );
    panel.add( three, cc.xy( 7, 11 ) );
    panel.add( new JLabel( "MaxCountTime:" ), cc.xy( 5, 13 ) );
    panel.add( four, cc.xy( 7, 13 ) );

    one.setText( "1" );

    // Meldungsbereich
    panel.add( scrollPane, cc.xyw( 1, 19, 7 ) );

    messageArea.setLineWrap( true );
    messageArea.setWrapStyleWord( true );
    messageArea.setEditable( false );
    messageArea.setCaretPosition( 0 );

    panel.add( createSeparator( "" ), cc.xyw( 1, 21, 7 ) );

    panel.add( countButton, cc.xyw( 2, 23, 2 ) ); // Starten
    panel.add( delButton, cc.xyw( 4, 23, 2 ) ); // Loeschen
    panel.add( beendenButton, cc.xyw( 6, 23, 2 ) ); // Abbrechen

    countButton.addActionListener( this );
    delButton.addActionListener( this );
    beendenButton.addActionListener( this );

    return panel;
  }

  /**
   * actionPerformed
   * 
   */
  public void actionPerformed( ActionEvent e )
  {

    if ( e.getActionCommand().equals( "CountNow!" ) )
    {
      countAction( e );
    }
    else if ( e.getActionCommand().equals( "DeleteEmAll" ) )
    {
      delAction( e );
    }
    else if ( e.getActionCommand().equals( "Exit" ) )
      finishAction( e );
  }

  /**
   * countAction
   * 
   */
  private void countAction( ActionEvent e )
  {

    if ( isInteger( one.getText() ) )
      try
      {
        Count count = ac.getClientCount( Integer.parseInt( one.getText() ) );
        two.setText( count.getCountNr() );
        three.setText( count.getMaxDate() );
        four.setText( count.getMinDate() );
        setMessageLine( new java.util.Date() + ": Counted!" );
      }
      catch ( Exception e1 )
      {
        setMessageLine( e1.getMessage() );
      }
    else
      setMessageLine( "Bitte nur numerische Zeichen als Cliend-ID verwenden!" );
  }

  /**
   * delAction
   * 
   */
  private void delAction( ActionEvent e )
  {

    try
    {
      ac.deleteAllData();
      setMessageLine( new java.util.Date()
          + ": Trace- and Count-Database truncated!" );
    }
    catch ( Exception e1 )
    {
      setMessageLine( e1.getMessage() );
    }
  }

  /**
   * Aktion bei Betaetigung des "Beenden"-Buttons ausfuehren
   * 
   */
  private void finishAction( ActionEvent e )
  {
    setMessageLine( "Shutdoooown..." );

    // Programm beenden
    System.exit( 0 );
  }

  /**
   * Schlieﬂen des Fensters und Beenden des Programms
   * 
   */
  public void windowClosing( WindowEvent e )
  {
    System.exit( 0 );
  }

  public void windowOpened( WindowEvent e )
  {
  }

  public void windowActivated( WindowEvent e )
  {
  }

  public void windowIconified( WindowEvent e )
  {
  }

  public void windowDeiconified( WindowEvent e )
  {
  }

  public void windowDeactivated( WindowEvent e )
  {
  }

  public void windowClosed( WindowEvent e )
  {
  }

  public synchronized void setMessageLine( String message )
  {
    messageArea.append( message + "\n" );
    messageArea.update( messageArea.getGraphics() );
  }

  private Component createSeparator( String text )
  {
    return DefaultComponentFactory.getInstance().createSeparator( text );
  }

  public static boolean isInteger( String s )
  {
    try
    {
      Integer.parseInt( s );
    }
    catch ( NumberFormatException e )
    {
      return false;
    }
    return true;
  }

  public static void main( String[] args ) throws Exception
  {
    PropertyConfigurator
        .configureAndWatch( "log4j.admin.properties", 60 * 1000 );

    ac = new AdminClient();
    f = new JFrame( "Admin Client GUI" );
    f.setTitle( "Administration" );
    f.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
    JComponent panel = new AdminClientGui().buildPanel();
    f.getContentPane().add( panel );
    f.setResizable( false );
    f.pack();
    f.setVisible( true );
  }

}
