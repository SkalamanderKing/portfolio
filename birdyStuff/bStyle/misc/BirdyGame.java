/* 
 * @(#)BirdyGame.java
 * 
 * Gjord för kursen 
 * Internetprogrammering HT 2009/VT 2010
 */ 

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import static javax.swing.JOptionPane.*;

/**
 * Denna klass är huvudklass med main(). Här skapas objekt, hanteras kollisoner, poäng, med mer.
 * Spelet fungerar på följande sätt: Man spelar över nätet,först måste man ange sin port, motståndarens internetadress
 * och port. Därefter måste man accpetera ett spel genom att skicka en förfråga, dvs trycka Play, eller få en förfrågan. 
 * Detta är helt enkelt en variabel (en 1:a) som skickas via UDP. Om man ej får 1:an kan man ej spela, och man
 * måste också skicka. Denna "handskakning" behövs för spelaren ska veta att allt är ok. Man kan
 * även se hur man sköterkontrollen med mer genom att trycka på frågetecken, när man ska välja spel.
 * Man spelar som den gula fågeln ("Birdy"), men hos den andra spelaren är man den rosa fågeln ("Red")
 * Hade först tänkt låta spelarna välja, men detta och mycket annat hann jag helt enkelt ej med
 * Man styr sin fågel med PIL tangenterna och man kan "skjuta" en kula, men bara en i taget.
 * Det finns en del buggar och mycket borde förbättras, musik fattas med mer, men jag hann ej...
 * @version	2010-03-25
 * @author	Fredrick Östlund
 */

class BirdyGame extends JFrame implements Runnable
{
	//div ettiketter
	JLabel valEtikett;
	JLabel playerScore;
	JLabel enemyScore;
 	
	//För att hämta den egna Internetadressen (sker i main(), visas i ramen)
	static InetAddress internetAdressTillDennaDator;
	boolean skickat=false;
	//Räknare för de poäng som ska synas för spelare/motspelare
	int playerHit=0;
	int enemyHit=0;

	//Rektanglar som ska hjälpa till och sköta kulornas kollisonsdetektering
	static Rectangle enemyBullet;
	static Rectangle playerBullet;

	//Motspelarens bild namn
	String enemyPic;

	//Den Point som motspelaren "Red" objekt har dvs. sprite[1]
	public static Point enemyPoint;

	//kontrollerar ev. kollision
	public static boolean krock=false;

	//I denna Run sköts mer än krockar, men det är en viktig uppgift 
	Thread rektangelKrock;

	//Är en tillfällig hjälp variabel när man väljer att spela
	private boolean tlf=true;

	//Om denna är 0 har inte motspelaren skjutit om denna är 1 har motspelaren skjutit och ammo objeket skapas
	private static int enemyFire;

	//Anger höjd och bredd på skärmen
	private int w=600;
	private int h=600;
	
	/*Denna blir true när en kula skjuten av motspelaren ("Red")finns på spelskärmen.Om en kula är för långt
	utanför skärmen blir denna false, och vissa satser ska då göra så man kan skjutan igen*/
	private boolean redBulletIsAlive=false;

	//Denna har ungfär samma uppgift som variabeln ovan, men är till för den gula fågeln "Birdy"
	 private boolean birdyBulletIsAlive=false;
	
	//Hjälpvariabel för när man ska välja att spela mot en annan över nätet
	public boolean accepteratSpel=false;

	//Tar emot 1:an från den andra spelaren, som fungerar som en "handskakning"
	public static int opponentsConfirm;

	//Array deklareras för Superklassen Sprite, men objekt ska vara subklasserna
	private Sprite[] sprite;

	//denna konstant är förval för port om anv ej skriver i annat
	private final static int DEFAULT_PORT=2000;//9494

	//Skall hämta mottagarens Internetadress
 	protected static InetAddress tillAdr;

	//porten som mottagaren ska ta emot
	 protected static int tillPort;

	//vilken animation som skall visas kontrolleras av en int variabel som hämtas
	public static int enemyAni;

	//detta värde skickas till ammoEnemy. Det ska bestämma den rikning kulan skjutits.
	public static int redsSkottRikning;
	
	//Dessa är deafult värden, men är också de där den hämtade info spar dvs. Red's koordinater
	private static int redX=400;
	private static int redY=200;

	//PLAY kanppen
	JButton birdyVal;

	//Initiera ett bakgrundsobjekt
	BackDrop bakgrund;

	//Måste finnas när man ska anv. bilder i jar
	private static ClassLoader cl = ClassLoader.getSystemClassLoader();

		BirdyGame()
		{
		//Titel + Internet adress till den egena datorn
		super("Birdy the Game     Internet Address: "+internetAdressTillDennaDator);

		/*Skapa rektanglar och placera ut på varit håll, så de ej kolliderar*/
		playerBullet=new Rectangle(0, 0, 16,16);
		enemyBullet=new Rectangle(550, 550, 16,16);

		/*Skapa en bakgrund som alla objekt senare skall läggas på*/
		bakgrund = new BackDrop();

		/*en array för att hantera de olika objekt som subklassat denna klass*/
		sprite=new Sprite[5];

		/*På bakrund ska dessa objekt läggas ut, med hjälp av array*/
		bakgrund.add(sprite[0]=new Birdy(100, 200, "birdyIdle.gif"));
		bakgrund.add(sprite[1]=new Red(redX,redY , enemyPic));

		/*Lägg till bakgrunden*/
		add(bakgrund, BorderLayout.CENTER);

		/*"Kul- objekten" skapas men görs osynliga*/
		bakgrund.add(sprite[2]=new Ammo(0,0));
		sprite[2].setVisible(false);
		bakgrund.add(sprite[3]=new AmmoEnemy(0,0));
		sprite[3].setVisible(false);

		//En panel för att lägga diverse komponenter i
		JPanel upp=new JPanel();
		upp.setOpaque(true);
		upp.setBackground(Color.black);
		
		//Komponenter och  etiketter, med div. utseende hanterare
		upp.add(playerScore=new JLabel("SCORE   "+playerHit+"   "));
		playerScore.setOpaque(true);
		playerScore.setBackground(Color.black);
		playerScore.setFont(new Font("SansSerif", Font.BOLD, 14)); 
		playerScore.setForeground(Color.blue);
		upp.add(valEtikett=new JLabel("       New Game>> "));
		valEtikett.setForeground(Color.green);
		valEtikett.setFont(new Font("SansSerif", Font.BOLD, 12)); 

		//Skapa knapp för spel
		birdyVal=new JButton("PLAY");
		birdyVal.setBackground(Color.red);
		birdyVal.setFont(new Font("SansSerif", Font.BOLD, 12)); 
		birdyVal.setForeground(Color.black);

		//tilldela lyssnare till knapparna
		birdyVal.addActionListener(new BirdyLyssnare());

		//Lägg ut knapparna i JPanel
		upp.add(birdyVal);
		upp.add(enemyScore=new JLabel("        ENEMY SCORE   "+enemyHit));
		enemyScore.setOpaque(true);
		enemyScore.setBackground(Color.black);
		enemyScore.setFont(new Font("SansSerif", Font.BOLD, 14)); 
		enemyScore.setForeground(Color.red);

		//Lägg ut JPanel
		add(upp, BorderLayout.NORTH);

		//Skapa och starta tråd
		rektangelKrock=new Thread(this);
		rektangelKrock.start();

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocation(200, 150);
		setSize(h, w);
		setVisible(true);
		
		}//konstruktor

	/*Denna run kollar kollisoner mellan rektanglar, byter animationer, placerar ut med mer*/
	public  void run()
	{
	while(true)
		{
		try
		{
		if(birdyBulletIsAlive)
			{
			//förorsakar ingen heap- allokering (anv. ist. för getX()/getY())
			int tflX=sprite[2].getLocation().x;
			int tflY=sprite[2].getLocation().y;

			//om den svarta kula (dvs. den gula fågelsens) hamnar utanför skärmen sätts variabeln till false
			if(tflX>w+10)
			birdyBulletIsAlive=false;

			if(tflX<w-610)
			birdyBulletIsAlive=false;

			if(tflY>h+10)
			birdyBulletIsAlive=false;
			if(tflY<h-610)
			birdyBulletIsAlive=false;
			
			}//if (birdyBulletIsAlive)slut

	//Om sant kan en kula avfyras, gäller den gula fågeln dvs. spelaren själv
	if(Birdy.spelarenAvfyradeEnKula)
	{
	//kulan "finns" nu
	birdyBulletIsAlive=true;

	//gör ammo spriten synlig
	sprite[2].setVisible(true);

	/*placera ut ammo spriten på de koordinater som hämtas från Birdy klassen. Detta då 
	dessa variabler har koll på var den gula fågeln är, och kulan ska ju utgå från dennna*/
	sprite[2].setLocation(Birdy.birdyX,Birdy.birdyY);
	
	//rita om för säkerhets skull
	bakgrund.repaint();
	
	//lägga ut subkomponenter igen
	bakgrund.validate();
	
	//spriten måste vara i fokus
	sprite[0].requestFocusInWindow();
	
	//måste sättas falsk genast, annars fortsätter utritningen så kulan blir ett långt "streck"	
	Birdy.spelarenAvfyradeEnKula=false;

	}//if(Birdy.spelarenAvfyradeEnKula) slut

		//Null-koll, så inte programmet kraschar. Det måste finnas en kula, avfyras av gula fågeln
		if(sprite[2]!=null)
			{
			
			/*Denna if har hand om kollison mellan motspelaren (röd fågel) och spelarens (gul fågel) kula
			  Den tar även hand om poängen och om kulan hamnar för långt utanför spelytan*/
			if (Red.r2.intersects(playerBullet) || !birdyBulletIsAlive) 
				{
				//För poäng till spelaren Birdy. Är endast aktuellt om det handlar om en rektangelkrock
					if (birdyBulletIsAlive)
					{
					playerHit++;
					playerScore.setText("SCORE "+playerHit);
					}
			
				//förflytta ammo kollisionrektangeln, så den inte påverkar
				playerBullet.setLocation(-5,-5);
		
				//gör ammo spriten osynlig
				sprite[2].setVisible(false);
		
				bakgrund.revalidate();
				bakgrund.repaint();
				
				//kulan är nu inte längre aktiv på spelskärmen 
				birdyBulletIsAlive=false;
				
				//Nu ska det gå att skjuta igen
				Birdy.skjutKlar=true;

				}//if intersect
			}//null-koll

	/*Denna sköter motspelaren skott(dvs den rosa fågeln "Red") om värdet 1 har kommit från den andra användaren*/
	if(enemyFire==1)
	{
		redBulletIsAlive=true;
		
		/*placera ut AmmoEnemy objektet på de koordinater som kommer från motspelaren och gör den synlig
		32 px läggs till för att placera bollen i "mitten" av figuren (för att det ser bättre ut)*/
		sprite[3].setVisible(true);
		sprite[3].setLocation(redX+32,redY+32);
	
		bakgrund.repaint();
		bakgrund.validate();
		sprite[0].requestFocusInWindow();
	
		//måste ändas så att fiendens kula inte kan styras av figuren 
		enemyFire=2;

	}//if(enemyFire==1) slut

	//Motspelarens Red's reaktangelkrock test. Snarlik den ovan
	if(sprite[3]!=null)
	{
		if (Birdy.r1.intersects(enemyBullet) || !redBulletIsAlive) 
		{
				if(redBulletIsAlive)
				{
				enemyHit++;
				enemyScore.setText("        ENEMY SCORE   "+enemyHit);
				}
		
		sprite[3].setVisible(false);

		//"Göm" motspelarens rektangel, så att inga krockar ska ske i onödan
		enemyBullet.setLocation(605,605);

		//måla om
		repaint();
		bakgrund.repaint();
		bakgrund.revalidate();
	
		//återställ för att fiendens kula ska kunna skapas igen
		enemyFire=0;
		redBulletIsAlive=false;
		}//if intersect
	}//null if
	
				//Denna handhar den röda kulan från Red, ifall den hamnar utanför spelytan. Som ovan.
				if(redBulletIsAlive)
				{
					//förorsakar ingen heap- allokering (anv. ist. för getX()/getY())
					int tmpX=sprite[3].getLocation().x;
					int tmpY=sprite[3].getLocation().y;

					if(tmpX>w+10)
					redBulletIsAlive=false;

					if(tmpX<w-610)
					redBulletIsAlive=false;

					if(tmpY>h+10)
					redBulletIsAlive=false;

					if(tmpY<h-610)
					redBulletIsAlive=false;

				}//enemyFire if

	//Ändra texten på den knapp som man startar med för att visa att den andra spelarn är uppkopplad 	
	if(opponentsConfirm==1 || opponentsConfirm==2)
	{
		birdyVal.setText("START!");
	}//if
	
	/*Om en kontakt finns får sprite-figuren focus så denne kan röra sig
	  om inte detta görs kan ju den ena spelaren ev. röra sig (mycket) för den andra
	  (en viss fördröjning finns men den är förhoppningsvis inte så stor att spelet blir orättvist*/
		if(opponentsConfirm==1  && accepteratSpel && tlf)
		{
		sprite[0].requestFocusInWindow();
		tlf=false;
		}//if
	
		//2:an används till för att inte placera ut sprit-figurerna på samma ställe
		else if(opponentsConfirm==2  && accepteratSpel && tlf)
		{
		sprite[0].requestFocusInWindow();
		tlf=false;
		}//if
			
			if(enemyPoint!=null)
			{
			//förflytta motspelarens figur Red
			sprite[1].setLocation(enemyPoint);

			//visa rätt animation på fienden. Denna metod finns i Sprite, och tar emot en int (1 el.0)
			sprite[1].setEnemyBild(enemyAni);

			//placera rektangeln rätt runt motspelarens figur Red
			Red.r2.setLocation(enemyPoint);

			}//if

					/*Om rektangel r1 krockar med r2 blir värdet krock true
		  			detta värde kollas av i Birdy.java och hanteras där*/
					if (Birdy.r1.intersects(Red.r2)) 
					{
					krock=true;

					}//if 
					else
					krock=false;
	
			//sov lite
			rektangelKrock.sleep(20);
			}//try
			catch(InterruptedException e){}


		}//while
	}//run


	/*Denna klass sköter den enda knapp som finns för att starta ett nytt spel. Den har ett antal JOptionPane
  	Detta då tvingar spelaren att göra vissa val, för att förhindra annat.När man valt kommer
  	den spelfigur som startade först placeras på ett ställe, och den andra, dvs. motsspelaren, på ett annat.
  	Rent speldesignmässigt är detta en aning förvirrande att ordna, eftersom ju samma program ska
  	göra olika beronde på vem som valde först. Fungerar ej perfekt, men jag hann ej fixa bättre*/
	class BirdyLyssnare implements ActionListener 	
	{
	public void actionPerformed(ActionEvent e)
		{
		//om inget värde har kommit från motspelaren kör denna där värdet 1 skickas
		if(opponentsConfirm==0 )
		{
		//En modal JOptionPane dvs. en dialogruta som tvingar anv att först göra ett val innan programmet fortsätter
		Object[]gubbar={new ImageIcon(cl.getResource("birdyLeftWalk.gif")), new ImageIcon(cl.getResource("que.gif"))};
		JOptionPane op= new JOptionPane();
		
			int val=op.showOptionDialog
			(null, "Push Button To Start!", "BIRDY vs. RED: You Are Birdy", JOptionPane.DEFAULT_OPTION,
			JOptionPane.PLAIN_MESSAGE, new ImageIcon(cl.getResource("bvsb.gif")), gubbar, gubbar[1]);
		
		if(op.getValue()==null)
		{
		//stäng programmet vid null
		System.exit(0);
		}
	
	//Denna ska visa hur man spelar (När/om man kollat på den skickas ändå en förfrågan till den andra spelaren)
	if(val==1)
	showMessageDialog(null, "", "BIRDY vs. RED", JOptionPane.PLAIN_MESSAGE,new ImageIcon(cl.getResource("keyb.gif")) );

	/*gör om koordinater till en String, med ett mellanrum: 
	  x position, Y position, animation på fienden, antal skott, spelkontroll*/
 
	//används för att om båda spelarna trycker samtidigt så ska inte figurerna hamna på samma ställe
	if(opponentsConfirm==1)
	skickat=true;
	String str = Integer.toString(50) + " " + Integer.toString(50)+ " " +Integer.toString(1)+ 
	" " +Integer.toString(0)+ " " +Integer.toString(1);
	
		try
		{
		Skicka med= new Skicka(BirdyGame.tillAdr,BirdyGame.tillPort,str);
		//anropa metoden för att skicka infon
		med.SkickaMed();
		}//try
		catch(IOException ei){}

	}
			/*om värdet 1 tagits emot körs denna där en 2 skickas. 
			Eftersom kontakt nu är etablerad stängs knappen av för att förhindra mera skickade */
 			if(opponentsConfirm==1 && skickat)
			{
		
			//skicka en bekräftelse
			String str = Integer.toString(50) + " " + Integer.toString(50)+ " " +Integer.toString(1)+ 
			" " +Integer.toString(0)+ " " +Integer.toString(2);
			try
			{
			Skicka med= new Skicka(BirdyGame.tillAdr,BirdyGame.tillPort,str);
			//anropa metoden för att skicka infon
			med.SkickaMed();
			}//try
			catch(IOException ei){System.out.println("Fel!");}

		//Placera ut den gula fågeln
		sprite[0].setLocation(50, 50);
		//placera rektangeln rätt runt gula fågeln
		Birdy.r1.setLocation(50,50);
		accepteratSpel=true;
		birdyVal.setEnabled(false);
	}//else if

		else if(opponentsConfirm==1 && !skickat)
{
//skicka en bekräftelse
			String str = Integer.toString(350) + " " + Integer.toString(400)+ " " +Integer.toString(1)+ 
			" " +Integer.toString(0)+ " " +Integer.toString(2);
			try
			{
			Skicka med= new Skicka(BirdyGame.tillAdr,BirdyGame.tillPort,str);
			//anropa metoden för att skicka infon
			med.SkickaMed();
			}//try
			catch(IOException ei){System.out.println("Fel!");}

		//Placera ut den gula fågeln
		sprite[0].setLocation(350, 400);
		//placera rektangeln rätt runt gula fågeln
		Birdy.r1.setLocation(350,400);
		accepteratSpel=true;
		birdyVal.setEnabled(false);







}
		/*om knappen inte är avstängd och en 2 tags emot körs denna if och knappen stängs av.*/
		if(opponentsConfirm==2 )
			{
			//skicka en bekräftelse
			String str = Integer.toString(50) + " " + Integer.toString(150)+ " " 
			+Integer.toString(1)+ " " +Integer.toString(0)+ " " +Integer.toString(1);
		
				try
				{
				Skicka med= new Skicka(BirdyGame.tillAdr,BirdyGame.tillPort,str);
				//anropa metoden för att skicka infon
				med.SkickaMed();
				}//try
			
				catch(IOException ei){System.out.println("Fel!");}
				sprite[0].setLocation(50, 150);//400, 400
				//placera rektangeln rätt runt gula fågeln
				Birdy.r1.setLocation(50,150);
				accepteratSpel=true;
				birdyVal.setEnabled(false);
				sprite[0].requestFocusInWindow();
			}//if
		}//actionPerformed
	}//BirdyLyssnare

	/*main() har hand om mottagndet av info från motspelaren ang. den rosa fågelns koordinater, skjutande, animationer
 	 och om spelaren accepterat ett spel. Denna kör även en JOptionPane där man matar in sina data om port,
 	 motspelarens internetardess och portnummer.
	*/
	public static void main(String[] args)
	throws InterruptedException,UnknownHostException, SocketException, IOException
		{
		String indata;
		
		//Hämta datorns lokala internet adress
		internetAdressTillDennaDator=InetAddress.getLocalHost();
	
		//den egna datorns portnr
		int datorPort = DEFAULT_PORT;
			try
			{
			//Hämta data med "tvång"
			indata=(String)JOptionPane.showInputDialog(null, "This Computer port number: ", 
			"Birdy The Game",JOptionPane.PLAIN_MESSAGE , new ImageIcon(cl.getResource("birdyIdle.gif")),null,"2000");
			datorPort=Integer.parseInt(indata);
			}
			catch(NumberFormatException e)
			{
			System.err.println("Only integer digits accepted ");
			System.exit(0);
			}

	indata=(String)JOptionPane.showInputDialog(null, "To Internet Address/ IP-adress (remote host): ", 
	"Birdy The Game",JOptionPane.PLAIN_MESSAGE , new ImageIcon(cl.getResource("redLeft.gif")),null,"localhost");
	tillAdr=InetAddress.getByName(indata);
		
		try
		{
		indata=(String)JOptionPane.showInputDialog(null, "To Port number (remote port): ", 
		"Birdy The Game",JOptionPane.PLAIN_MESSAGE , new ImageIcon(cl.getResource("birdRightWalk.gif")),null,"2001");
		tillPort=Integer.parseInt(indata);
		}
		catch(NumberFormatException e)
		{
		System.err.println("Only integer digits accepted ");
		System.exit(0);
		}

			//Skapa en BirdyGame, med de värden som hämtats
			new BirdyGame();

		//skapa ett datagram och tilldela det portnummret
		DatagramSocket socketIn=new DatagramSocket(datorPort);

		//omvandla data till bytes, arrayens storlek måste här anges
		byte[]data=new byte[256];

		//här sköts mottagndet av UDP, en whileloop snurrar hela tiden
		
		while(true)
			{
			//skapa ett datagrampaket för mottagndet
			DatagramPacket pac= new DatagramPacket(data, data.length);

			//använd metoden receive för att ta emot paketet
			socketIn.receive(pac);

			//gör om från bytes till en string
			String med=new String(pac.getData(), 0, pac.getLength());

			//dela string och stoppa in den i en array
			String[]xy=new String[10];
			xy = med.split(" ");
	
			//sätt in koordinaterna i en punkt, i detta fall fiendens 
			enemyPoint = new Point(Integer.parseInt(xy[0]), Integer.parseInt(xy[1]));
			
			//info som ska användas till skott. 
			redX=(Integer.parseInt(xy[0]));
			redY=(Integer.parseInt(xy[1]));
	
			//info om fiendens animation tas emot
			enemyAni=(Integer.parseInt(xy[2]));

			//denna tar emot info om ett skott avfyrats av motspelaren
			if(enemyFire==0 )
				{
				enemyFire=(Integer.parseInt(xy[3]));
				redsSkottRikning=enemyAni;
				}

			/*En ett värde som ska bekräfta att det finns en annan spelare, annars går det ej att spela
			En 1:a visar på att motspelaren skickat en "förfrågan" om spel. Denna 1:a kollas
			av i BirdyLyssnar klassen. Om det är en 2:a så har spelaren redan fått en 1:a.
			vid 0 fortsätter denna att kolllas av och måste sluta kollas av när den fått
 			ett värde så den inte kraschar*/
						if(opponentsConfirm==0)
							{
							opponentsConfirm=(Integer.parseInt(xy[4]));

							}


			}//while
		}//main
}//Birdygame
