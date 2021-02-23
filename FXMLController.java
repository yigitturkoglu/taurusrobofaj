package robofajv2;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import com.sun.prism.paint.Color;

import javafx.animation.FillTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class FXMLController  {
	
	
	//CascadeClassifier face_cascade = new CascadeClassifier("C:/haarcascade/haarcascade_frontalface_default.xml");
	//CascadeClassifier eye_cascade = new CascadeClassifier("C:/haarcascade/haarcascade_eye.xml");
	//CascadeClassifier mouth_cascade = new CascadeClassifier("C:/haarcascade/haarcascade_mcs_mouth.xml");
	//CascadeClassifier upper_body = new CascadeClassifier("C:/haarcascade/haarcascade_upperbody.xml");
	
	CascadeClassifier face_cascade = new CascadeClassifier("/home/pi/Desktop/robofaj/haarcascade/haarcascade_frontalface_default.xml");
    CascadeClassifier eye_cascade = new CascadeClassifier("/home/pi/Desktop/robofaj/haarcascade/haarcascade_eye.xml");
	CascadeClassifier mouth_cascade = new CascadeClassifier("/home/pi/Desktop/robofaj/haarcascade/haarcascade_mcs_mouth.xml");
	CascadeClassifier upper_body = new CascadeClassifier("/home/pi/Desktop/robofaj/haarcascade/haarcascade_upperbody.xml");

	boolean coklukisi = false;
	boolean maskedurum = true;
	boolean rfiddurum = false;
	boolean temprfid = true;
	boolean welcomebool = true;
	boolean soundtimer = true;
	boolean maskesound = true;
	boolean maskekesin = true;
	
	int temptakili = 0;
	int tempnotakili = 0;
	int tempsestakili = 0;
	int tempsesnotakili = 0;
	int tempbreak = 0;
	int tempsoundbreak = 0;
	
	float tempates = 0;
	
	String rfiddata;
	String tempdata;
	
	int bw_threshold = 80;
	String tempfacex;
	String tempfacey;
	
	int thickness = 1;
	double font_scale = 0.5;
	String weared_mask = "TESEKKURLER!";
	String not_weared_mask = "MASKE TAKIN!";
	
	String ad_soyad_text = "";
	String kart_id_text = "";
	
	LocalDateTime myDateObj = LocalDateTime.now();
	
		int camid = 0;
	
	@FXML
	private ImageView currentFrame;
	
	
	private static final String MEDIA_URL = "file:///home/pi/Desktop/robofaj/maske_takili_video.flv";
	
	@FXML
	private Label tempid;
	
	@FXML
	private Label ad_soyad;
	
	@FXML
	private ImageView sicaklikresim;
	
	@FXML
	private ImageView dezresim;
	
	@FXML
	private AnchorPane anchor;
	
	public List<Rect> listOfFaces;
	public List<Rect> listOfMouths;
	
	private Pane rootElement;
	private Timer timer;
	
	private VideoCapture capture = new VideoCapture(camid);
	
	public void initialize() throws ClassNotFoundException{
		ad_soyad.setText("");
		startCamera();
		startUI();
		startSound();
	}
	
	
	@FXML
	protected void startCamera() throws ClassNotFoundException 
	{
		final ImageView frameView = currentFrame;
		this.capture.open(camid);
		capture.set(Videoio.CAP_PROP_FRAME_WIDTH, 200);
		capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 200);

            //stmt.executeUpdate(sql);
            
		TimerTask frameGrabber = new TimerTask() {
			@Override
			public void run()
			{
				Image tmp = grabFrame();
				Platform.runLater(new Runnable() {
				@Override
				public void run()
				{
					frameView.setImage(tmp);
				}
				});
			}
				};
				
				this.timer = new Timer();
				this.timer.schedule(frameGrabber, 0, 33);
			
	}
	
	protected void startUI()
	{
		TimerTask uicontrol = new TimerTask() {
			@Override
			public void run()
			{
				//hi
				Platform.runLater(new Runnable() {
				@Override
				public void run()
				{
					updateUI();
					if(rfiddurum)
					{
						
					}
				}
				});
		}
			};
			
			this.timer = new Timer();
			this.timer.schedule(uicontrol, 0, 33);
	}
	
	protected void startSound()
	{
		TimerTask soundcontrol = new TimerTask() {
			@Override
			public void run()
			{
				//hi
				Platform.runLater(new Runnable() {
				@Override
				public void run()
				{
					sound();
				}
				});
		}
			};
			
			this.timer = new Timer();
			this.timer.schedule(soundcontrol, 0, 33);
	}
	
	
	private void maskeTespit(int x, int y, int w, int h, boolean maskdurum, Mat frame)
	{
		if(maskdurum)
		{
			Imgproc.rectangle(frame, new Point(x, y), new Point(x + w, y + h), new Scalar(0, 255, 0), 1);
		}
		else
		{
			Imgproc.rectangle(frame, new Point(x, y), new Point(x + w, y + h), new Scalar(0, 0, 255), 1);
		}
	}
	
	
	@FXML
	private Image grabFrame()
	{
		Image imageToShow = null;
		Mat frame = new Mat();
		if(this.capture.isOpened())
		{
			try
			{
				this.capture.read(frame);
				if(!frame.empty())
				{
					Core.flip(frame, frame, 1);
					Mat gray = new Mat();
					Mat black_and_white = new Mat();
					Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
					Imgproc.threshold(gray, black_and_white, bw_threshold, 255,Imgproc.THRESH_BINARY);
					MatOfRect faces = new MatOfRect();
					MatOfRect faces_bw = new MatOfRect();
					MatOfRect mouth_rects = new MatOfRect();
					face_cascade.detectMultiScale(gray, faces, 1.1, 4);
					face_cascade.detectMultiScale(black_and_white, faces_bw, 1.1, 4);
					
					listOfFaces = faces.toList();
					List<Rect> listOfFaces_bw = faces_bw.toList();

					
					
					if(listOfFaces.size() == 0 && listOfFaces_bw.size() == 0)
					{
						//Insan yuzu yok.
					}
					else if(listOfFaces.size() == 0 && listOfFaces_bw.size() == 1)
					{
						//Insan yuzu yok.
					}
					else
					{	
						}
						for (Rect face : listOfFaces)
						{
							//Imgproc.rectangle(frame, new Point(face.x, face.y), new Point(face.x + face.width, face.y + face.height), new Scalar(255, 255, 255), 1);
							
							Rect rectCrop = new Rect(face.x, face.y, face.width, face.height);
							Mat image_roi = new Mat(frame,rectCrop);
							//Imgcodecs.imwrite("C:/haarcascade/image.jpg",image_roi);
							
							Mat tempGray = new Mat();
							
							Imgproc.cvtColor(image_roi, tempGray, Imgproc.COLOR_BGR2GRAY);
							mouth_cascade.detectMultiScale(tempGray, mouth_rects, 1.5, 5);
							listOfMouths = mouth_rects.toList();
							
								
							if(listOfFaces.size() > 1)
							{
								if(listOfMouths.size() == 0)
								{
									maskedurum = true;
								}
								else
								{
									maskedurum = false;
								}
							}
							else
							{
								if(listOfMouths.size() == 0)
								{
									temptakili++;
									tempnotakili = 0;
									if(temptakili == 5)
									{
										temptakili = 0;
										tempnotakili = 0;
										maskedurum = true;
										tempsesnotakili = 0;
										tempsestakili = tempsestakili + 5;
									}
									if(tempsestakili == 30)
									{
										tempsestakili = 0;
										tempsesnotakili = 0;
										maskesound = true;
										if(welcomebool)
										{
				
											
											if(maskesound)
											{
												
												try {
													File myObj = new File("/home/pi/Desktop/robofaj/sound_file.txt");
												      if (myObj.createNewFile()) {
												      } else {
												      }
													System.out.println("yes");
												    FileWriter myWriter = new FileWriter("/home/pi/Desktop/robofaj/sound_file.txt");
												    myWriter.write("0");
												    myWriter.close();
												    } catch (IOException e) {
												    System.out.println("Ses hatasi.");
												    e.printStackTrace();
												    }
												
												welcomebool = false;
												
										}
											
											
										}
									}
								}
								else
								{
									tempnotakili++;
									temptakili = 0;
									if(tempnotakili == 5)
									{
										tempnotakili = 0;
										temptakili = 0;
										maskedurum = false;
										tempsesnotakili = tempsesnotakili + 5;
										tempsestakili = 0;
										
										Imgcodecs.imwrite("/home/pi/Desktop/robofaj/image.jpg",frame);
										
										
									}
									if(tempsesnotakili == 30)
									{
										tempsestakili = 0;
										tempsesnotakili = 0;
										maskesound = false;
										maskekesin = false;
										if(welcomebool)
										{
											try {
												File myObj = new File("/home/pi/Desktop/robofaj/sound_file.txt");
											      if (myObj.createNewFile()) {
											      } else {
											      }
												System.out.println("yes");
											    FileWriter myWriter = new FileWriter("/home/pi/Desktop/robofaj/sound_file.txt");
											    myWriter.write("1");
											    myWriter.close();
											    } catch (IOException e) {
											    System.out.println("Ses hatasi.");
											    e.printStackTrace();
											    }
											maskekesin = false;
											welcomebool = false;
										
										}
									}
								}
								
								
							
							
							if(maskedurum)
							{
								maskeTespit(face.x, face.y, face.width, face.height, true, frame);
							}
							else
							{
								maskeTespit(face.x, face.y, face.width, face.height, false, frame);
							}
							
						}
						
					}
					
					imageToShow = mat2Image(frame);
				}
			}
			catch (Exception e)
			{
				System.err.println("ERROR: " + e.getMessage());
				System.err.println(e.getClass());
			}
			
		}
		return imageToShow;
	}

	private Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:/home/pi/Desktop/robofaj/veritabani.sqlite";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
	
	public void insert(String ad_soyad, String kart_id) {
        String sql = "INSERT INTO veritabani(ad_soyad, kart_id) VALUES(?,?)";

        try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ad_soyad);
            pstmt.setString(2, kart_id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
           
        }
    }
	

	@FXML
	public void sound()
	{
		tempsoundbreak++;
		if(tempsoundbreak == 1000)
		{
			System.out.println(tempsoundbreak);
			//Image image = new Image("maske_takili_video.gif");
			//gif.setImage(image);
			
			welcomebool = true;
			System.out.println("worked");
			tempsoundbreak = 0;
		}
	}
	
	@FXML
	public void updateUI()
	{
		DecimalFormat df = new DecimalFormat("00.0");
		df.setRoundingMode(RoundingMode.DOWN);
		
		int masketakan = listOfFaces.size() - listOfMouths.size();
		int masketakmayan = listOfMouths.size();
		//System.out.println(listOfFaces.size());
		//System.out.println(listOfMouths.size());
		
		try {
	        File myObj = new File("/home/pi/Desktop/robofaj/temp_file.txt");
	        Scanner myReader = new Scanner(myObj);
	        while (myReader.hasNextLine()) {
	          tempdata = myReader.nextLine();
	          //System.out.println(data);
	        }
	        myReader.close();
	      } catch (FileNotFoundException e) {
	        System.out.println("Okuma hatasi.");
	        e.printStackTrace();
	    }
		
		float tempsc = Float.parseFloat(tempdata);
		
		String sicaklik = df.format(tempsc);
		
		tempid.setText(sicaklik);
		
		if(maskedurum)
		{			
			//anchor.setStyle("-fx-background-color: #00FF00");
			anchor.setStyle("-fx-background-color: #FFFFFF");
		}
		else
		{
			anchor.setStyle("-fx-background-color: #FF0000");
		}
		
		if(rfiddurum)
		{
			
		}
		else
		{
			File f = new File("/home/pi/Desktop/robofaj/rfid_file.txt");
			if(f.exists() && !f.isDirectory()) { 
			    
			    try {
			        File myObj = new File("/home/pi/Desktop/robofaj/rfid_file.txt");
			        Scanner myReader = new Scanner(myObj);
			        while (myReader.hasNextLine()) {
			          rfiddata = myReader.nextLine();
			          //System.out.println(data);
			        }
			        myReader.close();
			      } catch (FileNotFoundException e) {
			        System.out.println("Okuma hatasi.");
			        e.printStackTrace();
			    }
			    rfiddurum = true;
			}
		}
		
		if(rfiddurum)
		{
			boolean xmldurum = true;
			tempbreak += 1;
		
			if(temprfid)
			{
				FXMLController controller = new FXMLController();

				//sesli cikis, tesekkur
				String sql = "SELECT ad_soyad, kart_id FROM veritabani";
				String sql1 = "CREATE TABLE IF NOT EXISTS veritabani(ad_soyad, kart_id)";

				
				
		        try (Connection conn = this.connect();
		             Statement stmt  = conn.createStatement();
		             ResultSet rs    = stmt.executeQuery(sql)){
		            
		            // loop through the result set
		            while (rs.next()) {
		                ad_soyad_text = rs.getString("ad_soyad");
		                kart_id_text = rs.getString("kart_id");
		                if(kart_id_text.equalsIgnoreCase(rfiddata))
		                {
		                	break;
		                }
		            }
		        } catch (SQLException e) {
		            System.out.println(e.getMessage());
		        }
		        if(maskedurum)
		        {
		        	ad_soyad.setText("Merhaba " + ad_soyad_text +"! \nMaskenizi taktýðýnýz için teþekkür ederiz!");
		        	//sesli cýkýs
		        	
		        }
		        else
		        {
		        	ad_soyad.setText("Merhaba " + ad_soyad_text +"! \nLütfen saðlýðýnýz için maskenizi takýnýz!");
		        }
		        
		        temprfid = false;
			}
			//loop
			
			System.out.println(tempbreak);
			if(tempsc > tempates)
			{
				tempates = tempsc;
			}
			
			
			if(tempbreak == 880
					)
			{
				rfiddurum = false;
				tempbreak = 0;
				temprfid = true;
			    
			    File myObj = new File("/home/pi/Desktop/robofaj/rfid_file.txt"); 
			    if (myObj.delete()) { 
			      System.out.println("Deleted the file: " + myObj.getName());
			    } else {
			      System.out.println("Failed to delete the file.");
			    } 
			    
				//bitti
			    DateTimeFormatter gunObj = DateTimeFormatter.ofPattern("dd-MM-yyyy");
			    String gun = myDateObj.format(gunObj);
			    
				try {
			         File inputFile = new File("veritabani.xml");
			         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			         Document doc = dBuilder.parse(inputFile);
			         doc.getDocumentElement().normalize();
			         
			         Element kokElement = doc.getDocumentElement();
			         
			         NodeList kisiListesi = kokElement.getElementsByTagName("kisi"); 
			         System.out.println("kisi sayisi "+ kisiListesi.getLength());
			         
			         for(int i = 0; i < kisiListesi.getLength(); i++)
			         {
			        	 Node kisi = kisiListesi.item(i);
			        	 Element kisiElement = (Element) kisi;
			        	 String kisiAdi = kisiElement.getElementsByTagName("ad_soyad").item(0).getTextContent();
			        	 
			        	 if(kisiAdi.equalsIgnoreCase(ad_soyad_text))
			        	 {
			        		 xmldurum = false;
			        		 System.out.println("sseser");
			        	 }
			         }
			         
			         
			         String subtempates = String.valueOf(tempates);
			         
			         if(xmldurum)
			         {
			        	 Element kisiElement = doc.createElement("kisi");
			        	 kokElement.appendChild(kisiElement);
			        	 
			        	 Element isimElement = doc.createElement("ad_soyad");
			        	 isimElement.appendChild(doc.createTextNode(ad_soyad_text));
			        	 kisiElement.appendChild(isimElement);
			        	 
			        	 Element atesElement = doc.createElement("ates_"+gun);
			        	 atesElement.appendChild(doc.createTextNode(subtempates));
			        	 kisiElement.appendChild(atesElement);
			        	 
			        	 Element maskeElement = doc.createElement("maske_durum_"+gun);
			        	 if(!maskekesin)
			        	 {
			        		 maskeElement.appendChild(doc.createTextNode("takili_degil"));
			        		 maskekesin = true;
			        	 }
			        	 else
			        	 {
			        		 maskeElement.appendChild(doc.createTextNode("takili")); 
			        	 }
			        	 
			        	 kisiElement.appendChild(maskeElement);
			         }
			         else
			         {
			        	 for(int i = 0; i < kisiListesi.getLength(); i++)
				         {
				        	 Node kisi = kisiListesi.item(i);
				        	 Element kisiElement = (Element) kisi;
				        	 String kisiAdi = kisiElement.getElementsByTagName("ad_soyad").item(0).getTextContent();
				        	 
				        	 if(kisiAdi.equalsIgnoreCase(ad_soyad_text))
				        	 {
				        		 try
				        		 {
				        			 String tempTarih = kisiElement.getElementsByTagName("ates_"+gun).item(0).getTextContent();
					        		 System.out.println(tempTarih);
				        		 }
				        		 catch(NullPointerException e)
				        		 {
				        			 Element atesElement = doc.createElement("ates_"+gun);
						        	 atesElement.appendChild(doc.createTextNode(subtempates));
						        	 kisiElement.appendChild(atesElement);
				        		 }
				        		 
				        		 try
				        		 {
				        			 String tempMaske = kisiElement.getElementsByTagName("maske_durum_"+gun).item(0).getTextContent();
					        		 System.out.println(tempMaske);
				        		 }
				        		 catch(NullPointerException e)
				        		 {
				        			 Element maskeElement = doc.createElement("maske_durum_"+gun);
				        			 if(maskedurum)
				        			 {
				        				 maskeElement.appendChild(doc.createTextNode("takili"));
				        			 }
				        			 else
				        			 {
				        				 maskeElement.appendChild(doc.createTextNode("takili_degil"));
				        			 }
						        	 kisiElement.appendChild(maskeElement);
				        		 }
				        	 }
				         }
			         }
			         TransformerFactory transformerFactory = TransformerFactory.newInstance();
		        	 Transformer transformer = transformerFactory.newTransformer();
		        	 transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		        	 transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		        	 DOMSource source = new DOMSource(doc);
		        	 StreamResult result = new StreamResult(new File("veritabani.xml"));
		        	 transformer.transform(source, result);
			         
			         ad_soyad.setText("");
		        	 
				} catch (Exception e) {
			         e.printStackTrace();
			      }
				
				
			}
		}
	}
	

	
	private Image mat2Image(Mat frame)
	{
		MatOfByte buffer = new MatOfByte();
		Imgcodecs.imencode(".png", frame, buffer);
		return new Image(new ByteArrayInputStream(buffer.toArray()));
	}
		
	public void setRootElement(Pane root)
	{
		this.rootElement = root;
	}
	
	
}
