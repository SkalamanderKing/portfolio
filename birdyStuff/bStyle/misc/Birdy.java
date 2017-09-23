/* 
 * @(#)Birdy.java
 * 
 * Gjord f�r kursen 
 * Internetprogrammering HT 2009/VT 2010
 */ 

import java.awt.*;
import java.io.*;
import java.awt.event.*;

/**
 * Denna klass har hand om hel del; Det �r h�r som spelaren styr sin gula f�gel "Birdy", men 
 * ocks� den rosa "Red". Denna info i skickas via UDP. Koordinater och annat som h�mtas h�r skickas
 * och tas hand om i BirdyGame's main()- metod (kunde ju vara en egen klass...)
 * Andra objekt l�ggs p�/i denna
 * @version	2010-03-24
 * @author	Fredrick �stlund
 */

//Sub-klassar Sprite-klassen
class Birdy extends Sprite
{
//Bilderna som ska visas 
private Image img;

//Namnet p� bild-filen
private String file;

/* Med hj�lpa av Rectangle-klassen objekt kan kollisonsdetektering g�ras. 
   Eftersom de ska anv�ndas i BirdyGame-klassen �r de static*/
public static Rectangle r1;

//denna variabels v�rde skickas via UDP, 0 inneb�r att motspelaren inte avfyrat en kula
int playerHasFiredhisShot=0;

//n�r denna �r true kan spelaren inte skjuta, f�rr�n den �ter blir false. Borde v�l anv metoder ist. f�r static...
public static boolean spelarenAvfyradeEnKula=false;

//Har diverse metoder som anv�nds som "klister" vid exempelvis bildhantering i Java
private Toolkit tk;

//Den string som kommer att anv�ndas f�r att skicka koordinater och annan info via UDP
String str;

//Dessa �r x och y koordniaterna f�r det objekt som anv�nds i BirdyGame klassen f�r att placera den r�tt
public static int birdyX;
public static int birdyY;

//spelaren kan skjuta om denna �r sann mao tryck space och en kula skapas i BirdyGame klassen
public static boolean skjutKlar=true;

//den riktning kulan ska ta efter att spelaren tryckt space
public static int skottRiktning=1;

/*best�mmer den rosa Red's animation, det som ska skickasvia UDP. 1-5, d�r 5 �r stillast�ende*/ 
private int redsAni=1;

//Anv. f�r att kunna anv�nda bilder (gif/jpg) i en Jar -fil
private ClassLoader cl = ClassLoader.getSystemClassLoader();

	/*Tar koordinater och ett namn som parameter*/
	public Birdy(int x, int y, String file)
	{
	//Titta p� Super klassen Sprite koordinater
	super(x, y);
	
	//bild-filens namn
	this.file = file;
	tk = Toolkit.getDefaultToolkit();
	
	//Anv�nda cl.getResource f�r Jar filen ska accptera bilder
	img =tk.getImage(cl.getResource("birdyIdle.gif"));

	//Man m�ste kunna s�tta fokus p� ett objekt av denna klass f�r att kunna styra den med mer
	setFocusable(true);
	
	//En rektangel som ska omge sj�lva gula f�gelns Birdy
	r1 =  new Rectangle(x, y, 64,64);

	//Denna lyssnar p� tangenbordet
	addKeyListener(new PilLyssnare());

	//Denna lyssnar d� tangenbordet "sl�ppts"
	addKeyListener(new IngenLyssnare());
}
			//Denna m�ste implementera och anv�nds f�r att helt enkelt rita ut figuren
			public void spriteObjekt(Graphics g)
			{
			//rita den specificerade bilden
			g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
			}
				//Denna m�ste implementeras, men anv�nds inte...
				public void spriteEnemy(Graphics g, int ngt){}

	/*Denna lyssnar-klass k�nner av om anv�ndaren sl�ppt tangenter. Detta beh�vs f�r
	  ha en s.k. "idle", dvs. f�geln- st�r- och- v�ntar animation. Skickar 
	  naturligvis ocks�, s� det �r samma animation hos motspelaren*/
	class IngenLyssnare extends KeyAdapter
		{
		public  synchronized void keyReleased(KeyEvent e)
			{
		//H�mta x och y
		int x = getX();
		int y = getY();
		
		//Den bild som ska visas vid stilla st�ende (idle)
		img =tk.getImage(cl.getResource("birdyIdle.gif"));
		
		//Idle v�rdet
		redsAni=5;

		//g�r om koordinater till en String, med ett mellanrum
		str = Integer.toString(x) + " " + Integer.toString(y)+ " " +Integer.toString(redsAni)+" "+Integer.toString(0);
		try{
		Skicka med= new Skicka(BirdyGame.tillAdr,BirdyGame.tillPort,str);
			
		//anropa metoden f�r att skicka infon
		med.SkickaMed();
		}
			catch(IOException ei){}
			repaint();
			}
		}

/* Denna lyssnar klass ska styra den gula f�geln p� anv�ndarens sk�rm, och den r�da p� motst�ndaren �ver n�tet.
 * Tar ocks� hand om "skjutande", dvs. Space knappen skickar info till Ammo-klassen och info via UDP, som hos
 * motspelaen tas hand om av EnemyAmmo-klassen (men tas f�rst emot i main() BirdyGame, fast hos motspelarens program)
 */

		class PilLyssnare extends KeyAdapter
		{
			public  synchronized void keyPressed(KeyEvent kev)
			{
	    		//h�mta objketets koordinater
			int x = getX();
			int y = getY();
		
				//En switch f�r de olika tangenttryckningarna
				switch(kev.getKeyCode())
				{
				
				/*V�nstra PIL-tangenten. Mycket ska ske vid en knapptryckning.
				�r snarlik i de andra switch satsterna, f�ruotm sista SPACE*/
				case KeyEvent.VK_LEFT: 
				
				//INTE krock; om det sker ska annat nedanf�r hantera detta.
				if(!BirdyGame.krock)
				{	
				//F�r�ndra x - koordinaten n�r tangenten h�lls nere
				--x; 

				//en metod fr�n standardklassen Rectangle, flyttar rektangel r1
				r1.setLocation(x, y);
				
				//S�tt denna bild
				img =tk.getImage(cl.getResource("birdRightWalk.gif"));

				repaint();
			
			/* 
			 * Kontroll om det g�r att skjuta och kulan ska d� ta denna riktning
			 * Det g�r allts� inte skjuta s� l�nge kulan �r kvar p� plan.
			 * Skottrikningn �r samma som redsAni, f�r att spara in p� det som skickas
			 */
			if(skjutKlar)
			{ 
			skottRiktning=4;//--x
			}
			//Denna best�mmer vilken bild som ska visas p� den rosa f�geln Red
			redsAni=4;

			//g�r om koordinater till en String, med ett mellanrum
			str = Integer.toString(x) + " " + Integer.toString(y)+ " " +Integer.toString(redsAni)+" "+Integer.toString(0);
			
				try
				{
				Skicka med= new Skicka(BirdyGame.tillAdr,BirdyGame.tillPort,str);
				//anropa metoden f�r att skicka infon
				med.SkickaMed();
				}
				catch(IOException ei){}

			}//INTE krock
			
			/*
			 * Hanterar hur Birdy-objektet ska bete sig vid krock, dvs. st�tta bort sig
			 * Krockar kollas av i BirdyGame run
			 */
			if(BirdyGame.krock)
				{
				//F�r�ndra x - koordinaten �t "andra" h�llet; Figuren Studsar bort
				x+=5;
				++x;
				
				//en metod fr�n standardklassen Rectangle, flyttar rektangel r1
				r1.setLocation(x, y);

				img =tk.getImage(cl.getResource("birdRightWalk.gif"));
				repaint();
				//S�tts genast till falsk, det f�r inte p�verka senare
				BirdyGame.krock=false;

				}//slut krock
				
				//Slut p� den v�nstra PIL tangent hanteraren
				break;
	


		case KeyEvent.VK_RIGHT:
		if(!BirdyGame.krock )//inte krock
		{
		++x; 
		r1.setLocation(x, y);
		img =tk.getImage(cl.getResource("birdyLeftWalk.gif"));
		repaint();
			if(skjutKlar)
			{ 
			skottRiktning=3;//++x
			}
			redsAni=3;
			//g�r om koordinater till en String, med ett mellanrum
			str = Integer.toString(x) + " " + Integer.toString(y)+ " " +Integer.toString(redsAni)+" "+Integer.toString(0);
				try
				{
				Skicka med= new Skicka(BirdyGame.tillAdr,BirdyGame.tillPort,str);
				//anropa metoden f�r att skicka infon
				med.SkickaMed();
				}
				catch(IOException ei){}
		}//inte krock
		
			//om rektanglar sk�r varanndras bana utf�rs dessa satser
			if(BirdyGame.krock)//krock
			{			
			x-=5;
			--x;
			//en metod fr�n standardklassen Rectangle, flyttar rektangel r1
			r1.setLocation(x, y);
			img =tk.getImage(cl.getResource("birdyLeftWalk.gif"));
			repaint();
			BirdyGame.krock=false;
			}//krock
	
		break;
	    
		
			case KeyEvent.VK_UP: 
			if(!BirdyGame.krock )//inte krock
			{
			--y; 
			r1.setLocation(x, y);
			img =tk.getImage(cl.getResource("birdyUpWalk.gif"));
			repaint();
			if(skjutKlar)
				{ 
				skottRiktning=2;//--y
				}
			redsAni=2;
			//g�r om koordinater till en String, med ett mellanrum
			str = Integer.toString(x) + " " + Integer.toString(y)+ " " +Integer.toString(redsAni)+" "+Integer.toString(0);
				try
				{
				Skicka med= new Skicka(BirdyGame.tillAdr,BirdyGame.tillPort,str);
				//anropa metoden f�r att skicka infon
				med.SkickaMed();
				}
				catch(IOException ei){}
			}//inte krock
				
					if(BirdyGame.krock)//krock
					{
					y+=5;
					++y;
					//en metod fr�n standardklassen Rectangle, flyttar rektangel r1
					r1.setLocation(x, y);
					img =tk.getImage(cl.getResource("birdyUpWalk.gif"));

					repaint();

					BirdyGame.krock=false;

					}//krock
				break;

		case KeyEvent.VK_DOWN: 
		if(!BirdyGame.krock )//inte krock
			{
			++y; 
			r1.setLocation(x, y);
			img =tk.getImage(cl.getResource("birdFrontWalk.gif"));
			repaint();

			if(skjutKlar)
				{ 
				skottRiktning=1;//++y
				}

			redsAni=1;	
			//g�r om koordinater till en String, med ett mellanrum
			str = Integer.toString(x) + " " + Integer.toString(y)+ " " +Integer.toString(redsAni)+" "+Integer.toString(0);
				try
				{
				Skicka med= new Skicka(BirdyGame.tillAdr,BirdyGame.tillPort,str);
				//anropa metoden f�r att skicka infon
				med.SkickaMed();
				}
				catch(IOException ei){}
			}//inte krock
	
					if(BirdyGame.krock)//krock
					{
					y-=5;
					--y;
					//en metod fr�n standardklassen Rectangle, flyttar rektangel r1
					r1.setLocation(x, y);

					img =tk.getImage(cl.getResource("birdFrontWalk.gif"));

					repaint();

					BirdyGame.krock=false;

					}//krock
				break;
		
		/*Denna handhar "kjutningen". Det inneb�r att en kula skapas +32 x/y-koordinater
		  fr�n spelarens f�glar. Infon skickas via UDP*/
		case KeyEvent.VK_SPACE: 

		if(skjutKlar) 
		{ 
		//N�r denna blir sann ska man inte kunna skjuta, f�rr�n den �ter blir false. 	
		spelarenAvfyradeEnKula=true;
		
		//Koordinater till den gula f�geln i BirdyGame klassen
		birdyX=x+32;
 		birdyY=y+32;

		//H�r �ndras playerHasFiredhisShot till 1, mao har spelaren avyrat en kula(mest f�r tydligheten skull)
		playerHasFiredhisShot=1;
		//Denna info skickas och triggar diverse i BirdyGame klassen hos motspelaren s� en kula skapas (Red's)		
		str = Integer.toString(x) + " " + Integer.toString(y)+ " " +
		Integer.toString(redsAni)+" " +Integer.toString(playerHasFiredhisShot);
		try
		{
		Skicka med= new Skicka(BirdyGame.tillAdr,BirdyGame.tillPort,str);
		//anropa metoden f�r att skicka infon
		med.SkickaMed();
		}
		catch(IOException ei){}
		//St�ng tillf�llet av skjut m�jligheten
		skjutKlar=false;

		}
		break;

			} // switch
		//Denna �ndrar koordinater s� figuren kan r�r sig
		setBounds(x, y, getWidth(), getHeight());
		repaint();


		} // keyPressed
	} // PilLyssnare

}//birdyklass
