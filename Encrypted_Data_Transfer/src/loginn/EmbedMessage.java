package loginn;

//EmbedMessage.java

import java.io.File;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
 import java.awt.image.*;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
 import javax.imageio.*;


import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import javax.swing.table.DefaultTableModel;
 
 public class EmbedMessage extends JFrame implements ActionListener
 {
 JButton open = new JButton("Open"), embed = new JButton("Embed"),
    save = new JButton("Save into new file"), reset = new JButton("Reset"), 
         mail = new JButton("Mail");
 JTextArea message = new JTextArea(5,3);
 BufferedImage sourceImage = null, embeddedImage = null;
 JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
 JScrollPane originalPane = new JScrollPane(),
    embeddedPane = new JScrollPane();
 
 public String userName,inputString,mailId,password;
 
 
 public EmbedMessage() {
    super("Embed stegonographic message in image");
    assembleInterface();
    
//    this.setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().
//       getMaximumWindowBounds());
    this.setSize(500, 500);
    this.setLocationRelativeTo(null);
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);   
    this.setVisible(true);
    sp.setDividerLocation(0.5);
    this.validate();
    }
 
 private void assembleInterface() {
    JPanel p = new JPanel(new FlowLayout());
    p.add(open);
    p.add(embed);
    p.add(save);   
    p.add(reset);
    p.add(mail);
    this.getContentPane().add(p, BorderLayout.SOUTH);
    open.addActionListener(this);
    embed.addActionListener(this);
    save.addActionListener(this);   
    reset.addActionListener(this);
    mail.addActionListener(this);
    open.setMnemonic('O');
    embed.setMnemonic('E');
    save.setMnemonic('S');
    reset.setMnemonic('R');
    mail.setMnemonic('O');
    
    p = new JPanel(new GridLayout(1,1));
    p.add(new JScrollPane(message));
    message.setFont(new Font("Arial",Font.BOLD,20));
    p.setBorder(BorderFactory.createTitledBorder("Message to be embedded"));
    this.getContentPane().add(p, BorderLayout.NORTH);
    
    sp.setLeftComponent(originalPane);
    sp.setRightComponent(embeddedPane);
    originalPane.setBorder(BorderFactory.createTitledBorder("Original Image"));
    embeddedPane.setBorder(BorderFactory.createTitledBorder("Steganographed Image"));
    this.getContentPane().add(sp, BorderLayout.CENTER);
    }
 
 public void actionPerformed(ActionEvent ae) {
    Object o = ae.getSource();
    if(o == open)
       openImage();
    else if(o == embed)
       embedMessage();
    else if(o == save) 
       saveImage();
    else if(o == reset) 
       resetInterface();
    
    else if(o == mail) 
    {
       mailInterface();    
    
     }
    }
  
 
 private java.io.File showFileDialog(final boolean open) {
    JFileChooser fc = new JFileChooser("Open an image");
    javax.swing.filechooser.FileFilter ff = new javax.swing.filechooser.FileFilter() {
       public boolean accept(java.io.File f) {
          String name = f.getName().toLowerCase();
          if(open)
             return f.isDirectory() || name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                name.endsWith(".png") || name.endsWith(".gif") || name.endsWith(".tiff") ||
                name.endsWith(".bmp") || name.endsWith(".dib");
          return f.isDirectory() || name.endsWith(".png") ||    name.endsWith(".bmp");
          }
       public String getDescription() {
          if(open)
             return "Image (*.jpg, *.jpeg, *.png, *.gif, *.tiff, *.bmp, *.dib)";
          return "Image (*.png, *.bmp)";
          }
       };
    fc.setAcceptAllFileFilterUsed(false);
    fc.addChoosableFileFilter(ff);
 
    java.io.File f = null;
   if(open && fc.showOpenDialog(this) == fc.APPROVE_OPTION)
       f = fc.getSelectedFile();
    else if(!open && fc.showSaveDialog(this) == fc.APPROVE_OPTION)
       f = fc.getSelectedFile();
    return f;
    }
 
 private void openImage() {
    java.io.File f = showFileDialog(true);
    try {   
       sourceImage = ImageIO.read(f);
       JLabel l = new JLabel(new ImageIcon(sourceImage));
       originalPane.getViewport().add(l);
       this.validate();
       } catch(Exception ex) { ex.printStackTrace(); }
    }
 
 private void embedMessage() {
    String mess = message.getText();
    embeddedImage = sourceImage.getSubimage(0,0,
       sourceImage.getWidth(),sourceImage.getHeight());
    embedMessage(embeddedImage, mess);
    JLabel l = new JLabel(new ImageIcon(embeddedImage));
    embeddedPane.getViewport().add(l);
    this.validate();
    }
 
 private void embedMessage(BufferedImage img, String mess) {
    int messageLength = mess.length();
    
    int imageWidth = img.getWidth(), imageHeight = img.getHeight(),
       imageSize = imageWidth * imageHeight;
    if(messageLength * 8 + 32 > imageSize) {
       JOptionPane.showMessageDialog(this, "Message is too long for the chosen image",
          "Message too long!", JOptionPane.ERROR_MESSAGE);
       return;
       }
    embedInteger(img, messageLength, 0, 0);
 
    byte b[] = mess.getBytes();
    for(int i=0; i<b.length; i++)
       embedByte(img, b[i], i*8+32, 0);
    }
 
 private void embedInteger(BufferedImage img, int n, int start, int storageBit) {
    int maxX = img.getWidth(), maxY = img.getHeight(), 
       startX = start/maxY, startY = start - startX*maxY, count=0;
    for(int i=startX; i<maxX && count<32; i++) {
       for(int j=startY; j<maxY && count<32; j++) {
          int rgb = img.getRGB(i, j), bit = getBitValue(n, count);
          rgb = setBitValue(rgb, storageBit, bit);
          img.setRGB(i, j, rgb);
          count++;
          }
       }
    }
 
 private void embedByte(BufferedImage img, byte b, int start, int storageBit) {
    int maxX = img.getWidth(), maxY = img.getHeight(), 
       startX = start/maxY, startY = start - startX*maxY, count=0;
    for(int i=startX; i<maxX && count<8; i++) {
       for(int j=startY; j<maxY && count<8; j++) {
          int rgb = img.getRGB(i, j), bit = getBitValue(b, count);
          rgb = setBitValue(rgb, storageBit, bit);
          img.setRGB(i, j, rgb);
          count++;
          }
       }
    }
 
 private void saveImage() {
    if(embeddedImage == null) {
       JOptionPane.showMessageDialog(this, "No message has been embedded!", 
         "Nothing to save", JOptionPane.ERROR_MESSAGE);
       return;
      }
   java.io.File f = showFileDialog(false);
    String name = f.getName();
    String ext = name.substring(name.lastIndexOf(".")+1).toLowerCase();
    if(!ext.equals("png") && !ext.equals("bmp") &&   !ext.equals("dib")) {
          ext = "png";
          f = new java.io.File(f.getAbsolutePath()+".png");
          }
    try {
       if(f.exists()) f.delete();
       ImageIO.write(embeddedImage, ext.toUpperCase(), f);
       } catch(Exception ex) { ex.printStackTrace(); }
    }

 private void resetInterface() {
    message.setText("");
    originalPane.getViewport().removeAll();
    embeddedPane.getViewport().removeAll();
    sourceImage = null;
    embeddedImage = null;
    sp.setDividerLocation(0.5);
    this.validate();
    }
 
   
    
    
 
 public void mailInterface() {
       
 
     if(embeddedImage==null){
     
     JOptionPane.showMessageDialog(this, "No message has been embedded!", 
         "Nothing to mail", JOptionPane.ERROR_MESSAGE);
       return;
     }
     
     
     
        
        JFrame f = new JFrame(); 
        f.setLayout(new BorderLayout());
        f.setTitle("JTable Example");   
        f.setSize(400, 300);
        
        DefaultTableModel model = new DefaultTableModel();
        JTable  table = new JTable(model){

         private static final long serialVersionUID = 1L;

            @Override
            public Class getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return String.class;
          
                    case 1:
                         return String.class;
                         
                    default:
                       return Boolean.class;
                }
            }
        };
         Object[] columnNames = {"Username","Email","Select"}; 
         model.setColumnIdentifiers(columnNames);
         table.setModel(model);
         table.setBounds(30, 40, 200, 300);
        
        
        
        
         JButton mail = new JButton("mail");
         JPanel btnPnl = new JPanel(new BorderLayout()); 
         JPanel topBtnPnl = new JPanel(new FlowLayout(FlowLayout.TRAILING));
         topBtnPnl.add(mail);
        // mail.addActionListener(this);
         btnPnl.add(topBtnPnl, BorderLayout.CENTER);
         f.add(table.getTableHeader(), BorderLayout.NORTH);
         f.add(table, BorderLayout.CENTER);
         f.add(btnPnl, BorderLayout.SOUTH);
         f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // f.pack();
         f.setVisible(true);
         JScrollPane sp = new JScrollPane(table);
         f.add(sp); 
        
       // String currentuser = 
          LoginFrame login = new LoginFrame();
          userName=login.userName;        
         //System.out.print(login.userName);
             
        try{
                     Class.forName("com.mysql.jdbc.Driver");
                     Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/project","root","");
                    
                   
                    String mail_sql="SELECT Email FROM login WHERE Username='" + userName + "'";
                   
                    String sql="SELECT Username,Email FROM login";
                    PreparedStatement pst1 = con.prepareStatement(sql);
                    ResultSet rs = pst1.executeQuery();
                       while(rs.next()){
                       
                        inputString=(String)rs.getString("Username");
                       
                        if(!(inputString.equals(userName))){
                       
                            
                        model.addRow(new Object[]{rs.getString("Username"),rs.getString("Email"),false});
                     
                        }
                       
                       }
                       
                       pst1=con.prepareStatement(mail_sql);
                       rs = pst1.executeQuery();
                       while(rs.next()){
                       
                       mailId=rs.getString("Email");
                       
                       }
         }
           catch (Exception ex){
                    JOptionPane.showMessageDialog(null, ex);
                }
        
         
           
            
       
          
            
            mail.addActionListener(new ActionListener(){
         
          
      
                 
       public void actionPerformed(ActionEvent e){  
           
            int rows = table.getRowCount(),j=0,test=0;           
            boolean[] b = new boolean[rows];   
            String[] s={""};
            
            for(int i=0;i<rows;i++){
               
             b[i]=(boolean)table.getModel().getValueAt(i, 2);
            
            }
            
            for(int i=0;i<rows;i++){
            
            if(b[i]==true){
            
            s[j++]=(String)table.getModel().getValueAt(i, 1);
            } 
            }
       
            /*mail transfer*/
     try{
            String host ="smtp.gmail.com" ;
            String[] to = s;
            int l = j;
            String from = mailId;
            //System.out.print(userName);
            String pass = "147147@raj";
            String subject = "Image";
            boolean sessionDebug = false;

            Properties props = System.getProperties();

            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.required", "true");

            //java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
            Session mailSession = Session.getDefaultInstance(props, null);
            mailSession.setDebug(sessionDebug);
            Message msg = new MimeMessage(mailSession);
            msg.setFrom(new InternetAddress(from));
            
            
            InternetAddress[] address = new InternetAddress[l];
         
            for( int i = 0; i < l; i++ ) {
                address[i] = new InternetAddress(to[i]);
            }

            for( int i = 0; i < l; i++) {
                msg.addRecipient(Message.RecipientType.TO, address[i]);
            }
            
            
            msg.setSubject(subject); 
            msg.setSentDate(new Date());
          
            BodyPart messageBodyPart = new MimeBodyPart();
 
       
            messageBodyPart.setText("This is message body");
         
         
            Multipart multipart = new MimeMultipart();

         
            multipart.addBodyPart(messageBodyPart);

       
            messageBodyPart = new MimeBodyPart();
         
         
            /* Image to file conversion*/
            File file = new File("image.jpg");
            ImageIO.write(embeddedImage, "jpg", file);
            String filename = "Hello";
            DataSource source = new FileDataSource(file);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(filename);
            multipart.addBodyPart(messageBodyPart);
            msg.setContent(multipart );
            
      

           Transport transport=mailSession.getTransport("smtp");
           transport.connect(host, from, pass);
           transport.sendMessage(msg, msg.getAllRecipients());
           transport.close();
           JOptionPane.showMessageDialog(null, "Sent Successfully", "Mail",JOptionPane.INFORMATION_MESSAGE);
           
        }catch(Exception ex)
        {
            System.out.println(ex);
        }
     
            
            
            
    }  
    });
      
    
 
 }
 private int getBitValue(int n, int location) {
    int v = n & (int) Math.round(Math.pow(2, location));
    return v==0?0:1;
    }
 
 private int setBitValue(int n, int location, int bit) {
    int toggle = (int) Math.pow(2, location), bv = getBitValue(n, location);
    if(bv == bit)
       return n;
    if(bv == 0 && bit == 1)
       n |= toggle;
    else if(bv == 1 && bit == 0)
       n ^= toggle;
    return n;
    }
 
 public static void main(String arg[]) {
    new EmbedMessage();
    
    }
}