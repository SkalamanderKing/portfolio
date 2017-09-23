/* 
 * @(#)BirdyGame.java
 * 
 * Gjord f�r kursen 
 * Internetprogrammering HT 2009/VT 2010
 */ 

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import static javax.swing.JOptionPane.*;

/**
 * Denna klass �r huvudklass med main(). H�r skapas objekt, hanteras kollisoner, po�ng, med mer.
 * Spelet fungerar p� f�ljande s�tt: Man spelar �ver n�tet,f�rst m�ste man ange sin port, motst�ndarens internetadress
 * och port. D�refter m�ste man accpetera ett spel genom att skicka en f�rfr�ga, dvs trycka Play, eller f� en f�rfr�gan. 
 * Detta �r helt enkelt en variabel (en 1:a) som skickas via UDP. Om man ej f�r 1:an kan man ej spela, och man
 * m�ste ocks� skicka. Denna "handskakning" beh�vs f�r spelaren ska veta att allt �r ok. Man kan
 * �ven se hur man sk�terkontrollen med mer genom att trycka p� fr�getecken, n�r man ska v�lja spel.
 * Man spelar som den gula f�geln ("Birdy"), men hos den andra spelaren �r man den rosa f�geln ("Red")
 * Hade f�rst t�nkt l�ta spelarna v�lja, men detta och mycket annat hann jag helt enkelt ej med
 * Man styr sin f�gel med PIL tangenterna och man kan "skjuta" en kula, men bara en i taget.
 * Det finns en del buggar och mycket borde f�rb�ttras, musik fattas med mer, men jag hann ej...
 * @version	2010-03-25
 * @author	Fredrick �stlund
 */

class BirdyGame extends JFrame implements Runnable
{
	//div ettiketter
	JLabel valEtikett;
	JLabel playerScore;
	JLabel enemyScore;
 	
	//F�r att h�mta den egna Internetadressen (sker i main(), visas i ramen)
	static InetAddress internetAdressTillDennaDator;
	boolean skickat=false;
	//R�knare f�r de po�ng som ska synas f�r spelare/motspelare
	int playerHit=0;
	int enemyHit=0;

	//Rektanglar som ska hj�lpa till och sk�ta kulornas kollisonsdetektering
	static Rectangle enemyBullet;
	static Rectangle playerBullet;

	//Motspelarens bild namn
	String enemyPic;

	//Den Point som motspelaren "Red" objekt har dvs. sprite[1]
	public static Point enemyPoint;

	//kontrollerar ev. kollision
	public static boolean krock=false;

	//I denna Run sk�ts mer �n krockar, men det �r en viktig uppgift 
	Thread rektangelKrock;

	//�r en tillf�llig hj�lp variabel n�r man v�ljer att spela
	private boolean tlf=true;

	//Om denna �r 0 har inte motspelaren skjutit om denna �r 1 har motspelaren skjutit och ammo objeket skapas
	private static int enemyFire;

	//Anger h�jd och bredd p� sk�rmen
	private int w=600;
	private int h=600;
	
	/*Denna blir true n�r en kula skjuten av motspelaren ("Red")finns p� spelsk�rmen.Om en kula �r f�r l�ngt
	utanf�r sk�rmen blir denna false, och vissa satser ska d� g�ra s� man kan skjutan igen*/
	private boolean redBulletIsAlive=false;

	//Denna har ungf�r samma uppgift som variabeln ovan, men �r till f�r den gula f�geln "Birdy"
	 private boolean birdyBulletIsAlive=false;
	
	//Hj�lpvariabel f�r n�r man ska v�lja att spela mot en annan �ver n�tet
	public boolean accepteratSpel=false;

	//Tar emot 1:an fr�n den andra spelaren, som fungerar som en "handskakning"
	public static int opponentsConfirm;

	//Array deklareras f�r Superklassen Sprite, men objekt ska vara subklasserna
	private Sprite[] sprite;

	//denna konstant �r f�rval f�r port om anv ej skriver i annat
	private final static int DEFAULT_PORT=2000;//9494

	//Skall h�mta mottagarens Internetadress
 	protected static InetAddress tillAdr;

	//porten som mottagaren ska ta emot
	 protected static int tillPort;

	//vilken animation som skall visas kontrolleras av en int variabel som h�mtas
	public static int enemyAni;

	//detta v�rde skickas till ammoEnemy. Det ska best�mma den rikning kulan skjutits.
	public static int redsSkottRikning;
	
	//Dessa �r deafult v�rden, men �r ocks� de d�r den h�mtade info spar dvs. Red's koordinater
	private static int redX=400;
	private static int redY=200;

	//PLAY kanppen
	JButton birdyVal;

	//Initiera ett bakgrundsobjekt
	BackDrop bakgrund;

	//M�ste finnas n�r man ska anv. bilder i jar
	private static ClassLoader cl = ClassLoader.getSystemClassLoader();

		BirdyGame()
		{
		//Titel + Internet adress till den egena datorn
		super("Birdy the Game     Internet Address: "+internetAdressTillDennaDator);

		/*Skapa rektanglar och placera ut p� varit h�ll, s� de ej kolliderar*/
		playerBullet=new Rectangle(0, 0, 16,16);
		enemyBullet=new Rectangle(550, 550, 16,16);

		/*Skapa en bakgrund som alla objekt senare skall l�ggas p�*/
		bakgrund = new BackDrop();

		/*en array f�r att hantera de olika objekt som subklassat denna klass*/
		sprite=new Sprite[5];

		/*P� bakrund ska dessa objekt l�ggas ut, med hj�lp av array*/
		bakgrund.add(sprite[0]=new Birdy(100, 200, "birdyIdle.gif"));
		bakgrund.add(sprite[1]=new Red(redX,redY , enemyPic));

		/*L�gg till bakgrunden*/
		add(bakgrund, BorderLayout.CENTER);

		/*"Kul- objekten" skapas men g�rs osynliga*/
		bakgrund.add(sprite[2]=new Ammo(0,0));
		sprite[2].setVisible(false);
		bakgrund.add(sprite[3]=new AmmoEnemy(0,0));
		sprite[3].setVisible(false);

		//En panel f�r att l�gga diverse komponenter i
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

		//Skapa knapp f�r spel
		birdyVal=new JButton("PLAY");
		birdyVal.setBackground(Color.red);
		birdyVal.setFont(new Font("SansSerif", Font.BOLD, 12)); 
		birdyVal.setForeground(Color.black);

		//tilldela lyssnare till knapparna
		birdyVal.addActionListener(new BirdyLyssnare());

		//L�gg ut knapparna i JPanel
		upp.add(birdyVal);
		upp.add(enemyScore=new JLabel("        ENEMY SCORE   "+enemyHit));
		enemyScore.setOpaque(true);
		enemyScore.setBackground(Color.black);
		enemyScore.setFont(new Font("SansSerif", Font.BOLD, 14)); 
		enemyScore.setForeground(Color.red);

		//L�gg ut JPanel
		add(upp, BorderLayout.NORTH);

		//Skapa och starta tr�d
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
			//f�rorsakar ingen heap- allokering (anv. ist. f�r getX()/getY())
			int tflX=sprite[2].getLocation().x;
			int tflY=sprite[2].getLocation().y;

			//om den svarta kula (dvs. den gula f�gelsens) hamnar utanf�r sk�rmen s�tts variabeln till false
			if(tflX>w+10)
			birdyBulletIsAlive=false;

			if(tflX<w-610)
			birdyBulletIsAlive=false;

			if(tflY>h+10)
			birdyBulletIsAlive=false;
			if(tflY<h-610)
			birdyBulletIsAlive=false;
			
			}//if (birdyBulletIsAlive)slut

	//Om sant kan en kula avfyras, g�ller den gula f�geln dvs. spelaren sj�lv
	if(Birdy.spelarenAvfyradeEnKula)
	{
	//kulan "finns" nu
	birdyBulletIsAlive=true;

	//g�r ammo spriten synlig
	sprite[2].setVisible(true);

	/*placera ut ammo spriten p� de koordinater som h�mtas fr�n Birdy klassen. Detta d� 
	dessa variabler har koll p� var den gula f�geln �r, och kulan ska ju utg� fr�n dennna*/
	sprite[2].setLocation(Birdy.birdyX,Birdy.birdyY);
	
	//rita om f�r s�kerhets skull
	bakgrund.repaint();
	
	//l�gga ut subkomponenter igen
	bakgrund.validate();
	
	//spriten m�ste vara i fokus
	sprite[0].requestFocusInWindow();
	
	//m�ste s�ttas falsk genast, annars forts�tter utritningen s� kulan blir ett l�ngt "streck"	
	Birdy.spelarenAvfyradeEnKula=false;

	}//if(Birdy.spelarenAvfyradeEnKula) slut

		//Null-koll, s� inte programmet kraschar. Det m�ste finnas en kula, avfyras av gula f�geln
		if(sprite[2]!=null)
			{
			
			/*Denna if har hand om kollison mellan motspelaren (r�d f�gel) och spelarens (gul f�gel) kula
			  Den tar �ven hand om po�ngen och om kulan hamnar f�r l�ngt utanf�r spelytan*/
			if (Red.r2.intersects(playerBullet) || !birdyBulletIsAlive) 
				{
				//F�r po�ng till spelaren Birdy. �r endast aktuellt om det handlar om en rektangelkrock
					if (birdyBulletIsAlive)
					{
					playerHit++;
					playerScore.setText("SCORE "+playerHit);
					}
			
				//f�rflytta ammo kollisionrektangeln, s� den inte p�verkar
				playerBullet.setLocation(-5,-5);
		
				//g�r ammo spriten osynlig
				sprite[2].setVisible(false);
		
				bakgrund.revalidate();
				bakgrund.repaint();
				
				//kulan �r nu inte l�ngre aktiv p� spelsk�rmen 
				birdyBulletIsAlive=false;
				
				//Nu ska det g� att skjuta igen
				Birdy.skjutKlar=true;

				}//if intersect
			}//null-koll

	/*Denna sk�ter motspelaren skott(dvs den rosa f�geln "Red") om v�rdet 1 har kommit fr�n den andra anv�ndaren*/
	if(enemyFire==1)
	{
		redBulletIsAlive=true;
		
		/*placera ut AmmoEnemy objektet p� de koordinater som kommer fr�n motspelaren och g�r den synlig
		32 px l�ggs till f�r att placera bollen i "mitten" av figuren (f�r att det ser b�ttre ut)*/
		sprite[3].setVisible(true);
		sprite[3].setLocation(redX+32,redY+32);
	
		bakgrund.repaint();
		bakgrund.validate();
		sprite[0].requestFocusInWindow();
	
		//m�ste �ndas s� att fiendens kula inte kan styras av figuren 
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

		//"G�m" motspelarens rektangel, s� att inga krockar ska ske i on�dan
		enemyBullet.setLocation(605,605);

		//m�la om
		repaint();
		bakgrund.repaint();
		bakgrund.revalidate();
	
		//�terst�ll f�r att fiendens kula ska kunna skapas igen
		enemyFire=0;
		redBulletIsAlive=false;
		}//if intersect
	}//null if
	
				//Denna handhar den r�da kulan fr�n Red, ifall den hamnar utanf�r spelytan. Som ovan.
				if(redBulletIsAlive)
				{
					//f�rorsakar ingen heap- allokering (anv. ist. f�r getX()/getY())
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

	//�ndra texten p� den knapp som man startar med f�r att visa att den andra spelarn �r uppkopplad 	
	if(opponentsConfirm==1 || opponentsConfirm==2)
	{
		birdyVal.setText("START!");
	}//if
	
	/*Om en kontakt finns f�r sprite-figuren focus s� denne kan r�ra sig
	  om inte detta g�rs kan ju den ena spelaren ev. r�ra sig (mycket) f�r den andra
	  (en viss f�rdr�jning finns men den �r f�rhoppningsvis inte s� stor att spelet blir or�ttvist*/
		if(opponentsConfirm==1  && accepteratSpel && tlf)
		{
		sprite[0].requestFocusInWindow();
		tlf=false;
		}//if
	
		//2:an anv�nds till f�r att inte placera ut sprit-figurerna p� samma st�lle
		else if(opponentsConfirm==2  && accepteratSpel && tlf)
		{
		sprite[0].requestFocusInWindow();
		tlf=false;
		}//if
			
			if(enemyPoint!=null)
			{
			//f�rflytta motspelarens figur Red
			sprite[1].setLocation(enemyPoint);

			//visa r�tt animation p� fienden. Denna metod finns i Sprite, och tar emot en int (1 el.0)
			sprite[1].setEnemyBild(enemyAni);

			//placera rektangeln r�tt runt motspelarens figur Red
			Red.r2.setLocation(enemyPoint);

			}//if

					/*Om rektangel r1 krockar med r2 blir v�rdet krock true
		  			detta v�rde kollas av i Birdy.java och hanteras d�r*/
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


	/*Denna klass sk�ter den enda knapp som finns f�r att starta ett nytt spel. Den har ett antal JOptionPane
  	Detta d� tvingar spelaren att g�ra vissa val, f�r att f�rhindra annat.N�r man valt kommer
  	den spelfigur som startade f�rst placeras p� ett st�lle, och den andra, dvs. motsspelaren, p� ett annat.
  	Rent speldesignm�ssigt �r detta en aning f�rvirrande att ordna, eftersom ju samma program ska
  	g�ra olika beronde p� vem som valde f�rst. Fungerar ej perfekt, men jag hann ej fixa b�ttre*/
	class BirdyLyssnare implements ActionListener 	
	{
	public void actionPerformed(ActionEvent e)
		{
		//om inget v�rde har kommit fr�n motspelaren k�r denna d�r v�rdet 1 skickas
		if(opponentsConfirm==0 )
		{
		//En modal JOptionPane dvs. en dialogruta som tvingar anv att f�rst g�ra ett val innan programmet forts�tter
		Object[]gubbar={new ImageIcon(cl.getResource("birdyLeftWalk.gif")), new ImageIcon(cl.getResource("que.gif"))};
		JOptionPane op= new JOptionPane();
		
			int val=op.showOptionDialog
			(null, "Push Button To Start!", "BIRDY vs. RED: You Are Birdy", JOptionPane.DEFAULT_OPTION,
			JOptionPane.PLAIN_MESSAGE, new ImageIcon(cl.getResource("bvsb.gif")), gubbar, gubbar[1]);
		
		if(op.getValue()==null)
		{
		//st�ng programmet vid null
		System.exit(0);
		}
	
	//Denna ska visa hur man spelar (N�r/om man kollat p� den skickas �nd� en f�rfr�gan till den andra spelaren)
	if(val==1)
	showMessageDialog(null, "", "BIRDY vs. RED", JOptionPane.PLAIN_MESSAGE,new ImageIcon(cl.getResource("keyb.gif")) );

	/*g�r om koordinater till en String, med ett mellanrum: 
	  x position, Y position, animation p� fienden, antal skott, spelkontroll*/
 
	//anv�nds f�r att om b�da spelarna trycker samtidigt s� ska inte figurerna hamna p� samma st�lle
	if(opponentsConfirm==1)
	skickat=true;
	String str = Integer.toString(50) + " " + Integer.toString(50)+ " " +Integer.toString(1)+ 
	" " +Integer.toString(0)+ " " +Integer.toString(1);
	
		try
		{
		Skicka med= new Skicka(BirdyGame.tillAdr,BirdyGame.tillPort,str);
		//anropa metoden f�r att skicka infon
		med.SkickaMed();
		}//try
		catch(IOException ei){}

	}
			/*om v�rdet 1 tagits emot k�rs denna d�r en 2 skickas. 
			Eftersom kontakt nu �r etablerad st�ngs knappen av f�r att f�rhindra mera skickade */
 			if(opponentsConfirm==1 && skickat)
			{
		
			//skicka en bekr�ftelse
			String str = Integer.toString(50) + " " + Integer.toString(50)+ " " +Integer.toString(1)+ 
			" " +Integer.toString(0)+ " " +Integer.toString(2);
			try
			{
			Skicka med= new Skicka(BirdyGame.tillAdr,BirdyGame.tillPort,str);
			//anropa metoden f�r att skicka infon
			med.SkickaMed();
			}//try
			catch(IOException ei){System.out.println("Fel!");}

		//Placera ut den gula f�geln
		sprite[0].setLocation(50, 50);
		//placera rektangeln r�tt runt gula f�geln
		Birdy.r1.setLocation(50,50);
		accepteratSpel=true;
		birdyVal.setEnabled(false);
	}//else if

		else if(opponentsConfirm==1 && !skickat)
{
//skicka en bekr�ftelse
			String str = Integer.toString(350) + " " + Integer.toString(400)+ " " +Integer.toString(1)+ 
			" " +Integer.toString(0)+ " " +Integer.toString(2);
			try
			{
			Skicka med= new Skicka(BirdyGame.tillAdr,BirdyGame.tillPort,str);
			//anropa metoden f�r att skicka infon
			med.SkickaMed();
			}//try
			catch(IOException ei){System.out.println("Fel!");}

		//Placera ut den gula f�geln
		sprite[0].setLocation(350, 400);
		//placera rektangeln r�tt runt gula f�geln
		Birdy.r1.setLocation(350,400);
		accepteratSpel=true;
		birdyVal.setEnabled(false);







}
		/*om knappen inte �r avst�ngd och en 2 tags emot k�rs denna if och knappen st�ngs av.*/
		if(opponentsConfirm==2 )
			{
			//skicka en bekr�ftelse
			String str = Integer.toString(50) + " " + Integer.toString(150)+ " " 
			+Integer.toString(1)+ " " +Integer.toString(0)+ " " +Integer.toString(1);
		
				try
				{
				Skicka med= new Skicka(BirdyGame.tillAdr,BirdyGame.tillPort,str);
				//anropa metoden f�r att skicka infon
				med.SkickaMed();
				}//try
			
				catch(IOException ei){System.out.println("Fel!");}
				sprite[0].setLocation(50, 150);//400, 400
				//placera rektangeln r�tt runt gula f�geln
				Birdy.r1.setLocation(50,150);
				accepteratSpel=true;
				birdyVal.setEnabled(false);
				sprite[0].requestFocusInWindow();
			}//if
		}//actionPerformed
	}//BirdyLyssnare

	/*main() har hand om mottagndet av info fr�n motspelaren ang. den rosa f�gelns koordinater, skjutande, animationer
 	 och om spelaren accepterat ett spel. Denna k�r �ven en JOptionPane d�r man matar in sina data om port,
 	 motspelarens internetardess och portnummer.
	*/
	public static void main(String[] args)
	throws InterruptedException,UnknownHostException, SocketException, IOException
		{
		String indata;
		
		//H�mta datorns lokala internet adress
		internetAdressTillDennaDator=InetAddress.getLocalHost();
	
		//den egna datorns portnr
		int datorPort = DEFAULT_PORT;
			try
			{
			//H�mta data med "tv�ng"
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

			//Skapa en BirdyGame, med de v�rden som h�mtats
			new BirdyGame();

		//skapa ett datagram och tilldela det portnummret
		DatagramSocket socketIn=new DatagramSocket(datorPort);

		//omvandla data till bytes, arrayens storlek m�ste h�r anges
		byte[]data=new byte[256];

		//h�r sk�ts mottagndet av UDP, en whileloop snurrar hela tiden
		
		while(true)
			{
			//skapa ett datagrampaket f�r mottagndet
			DatagramPacket pac= new DatagramPacket(data, data.length);

			//anv�nd metoden receive f�r att ta emot paketet
			socketIn.receive(pac);

			//g�r om fr�n bytes till en string
			String med=new String(pac.getData(), 0, pac.getLength());

			//dela string och stoppa in den i en array
			String[]xy=new String[10];
			xy = med.split(" ");
	
			//s�tt in koordinaterna i en punkt, i detta fall fiendens 
			enemyPoint = new Point(Integer.parseInt(xy[0]), Integer.parseInt(xy[1]));
			
			//info som ska anv�ndas till skott. 
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

			/*En ett v�rde som ska bekr�fta att det finns en annan spelare, annars g�r det ej att spela
			En 1:a visar p� att motspelaren skickat en "f�rfr�gan" om spel. Denna 1:a kollas
			av i BirdyLyssnar klassen. Om det �r en 2:a s� har spelaren redan f�tt en 1:a.
			vid 0 forts�tter denna att kolllas av och m�ste sluta kollas av n�r den f�tt
 			ett v�rde s� den inte kraschar*/
						if(opponentsConfirm==0)
							{
							opponentsConfirm=(Integer.parseInt(xy[4]));

							}


			}//while
		}//main
}//Birdygame
