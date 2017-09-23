/* 
 * @(#)Birdy.java
 * 
 * Gjord för kursen 
 * Internetprogrammering HT 2009/VT 2010
 */ 

import java.awt.*;
import java.io.*;
import java.awt.event.*;

/**
 * Denna klass har hand om hel del; Det är här som spelaren styr sin gula fågel "Birdy", men 
 * också den rosa "Red". Denna info i skickas via UDP. Koordinater och annat som hämtas här skickas
 * och tas hand om i BirdyGame's main()- metod (kunde ju vara en egen klass...)
 * Andra objekt läggs på/i denna
 * @version	2010-03-24
 * @author	Fredrick Östlund
 */

//Sub-klassar Sprite-klassen
class Birdy extends Sprite
{
//Bilderna som ska visas 
private Image img;

//Namnet på bild-filen
private String file;

/* Med hjälpa av Rectangle-klassen objekt kan kollisonsdetektering göras. 
   Eftersom de ska användas i BirdyGame-klassen är de static*/
public static Rectangle r1;

//denna variabels värde skickas via UDP, 0 innebär att motspelaren inte avfyrat en kula
int playerHasFiredhisShot=0;

//när denna är true kan spelaren inte skjuta, förrän den åter blir false. Borde väl anv metoder ist. för static...
public static boolean spelarenAvfyradeEnKula=false;

//Har diverse metoder som används som "klister" vid exempelvis bildhantering i Java
private Toolkit tk;

//Den string som kommer att användas för att skicka koordinater och annan info via UDP
String str;

//Dessa är x och y koordniaterna för det objekt som används i BirdyGame klassen för att placera den rätt
public static int birdyX;
public static int birdyY;

//spelaren kan skjuta om denna är sann mao tryck space och en kula skapas i BirdyGame klassen
public static boolean skjutKlar=true;

//den riktning kulan ska ta efter att spelaren tryckt space
public static int skottRiktning=1;

/*bestämmer den rosa Red's animation, det som ska skickasvia UDP. 1-5, där 5 är stillastående*/ 
private int redsAni=1;

//Anv. för att kunna använda bilder (gif/jpg) i en Jar -fil
private ClassLoader cl = ClassLoader.getSystemClassLoader();

	/*Tar koordinater och ett namn som parameter*/
	public Birdy(int x, int y, String file)
	{
	//Titta på Super klassen Sprite koordinater
	super(x, y);
	
	//bild-filens namn
	this.file = file;
	tk = Toolkit.getDefaultToolkit();
	
	//Använda cl.getResource för Jar filen ska accptera bilder
	img =tk.getImage(cl.getResource("birdyIdle.gif"));

	//Man måste kunna sätta fokus på ett objekt av denna klass för att kunna styra den med mer
	setFocusable(true);
	
	//En rektangel som ska omge själva gula fågelns Birdy
	r1 =  new Rectangle(x, y, 64,64);

	//Denna lyssnar på tangenbordet
	addKeyListener(new PilLyssnare());

	//Denna lyssnar då tangenbordet "släppts"
	addKeyListener(new IngenLyssnare());
}
			//Denna måste implementera och används för att helt enkelt rita ut figuren
			public void spriteObjekt(Graphics g)
			{
			//rita den specificerade bilden
			g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
			}
				//Denna måste implementeras, men används inte...
				public void spriteEnemy(Graphics g, int ngt){}

	/*Denna lyssnar-klass känner av om användaren släppt tangenter. Detta behövs för
	  ha en s.k. "idle", dvs. fågeln- står- och- väntar animation. Skickar 
	  naturligvis också, så det är samma animation hos motspelaren*/
	class IngenLyssnare extends KeyAdapter
		{
		public  synchronized void keyReleased(KeyEvent e)
			{
		//Hämta x och y
		int x = getX();
		int y = getY();
		
		//Den bild som ska visas vid stilla stående (idle)
		img =tk.getImage(cl.getResource("birdyIdle.gif"));
		
		//Idle värdet
		redsAni=5;

		//gör om koordinater till en String, med ett mellanrum
		str = Integer.toString(x) + " " + Integer.toString(y)+ " " +Integer.toString(redsAni)+" "+Integer.toString(0);
		try{
		Skicka med= new Skicka(BirdyGame.tillAdr,BirdyGame.tillPort,str);
			
		//anropa metoden för att skicka infon
		med.SkickaMed();
		}
			catch(IOException ei){}
			repaint();
			}
		}

/* Denna lyssnar klass ska styra den gula fågeln på användarens skärm, och den röda på motståndaren över nätet.
 * Tar också hand om "skjutande", dvs. Space knappen skickar info till Ammo-klassen och info via UDP, som hos
 * motspelaen tas hand om av EnemyAmmo-klassen (men tas först emot i main() BirdyGame, fast hos motspelarens program)
 */

		class PilLyssnare extends KeyAdapter
		{
			public  synchronized void keyPressed(KeyEvent kev)
			{
	    		//hämta objketets koordinater
			int x = getX();
			int y = getY();
		
				//En switch för de olika tangenttryckningarna
				switch(kev.getKeyCode())
				{
				
				/*Vänstra PIL-tangenten. Mycket ska ske vid en knapptryckning.
				Är snarlik i de andra switch satsterna, föruotm sista SPACE*/
				case KeyEvent.VK_LEFT: 
				
				//INTE krock; om det sker ska annat nedanför hantera detta.
				if(!BirdyGame.krock)
				{	
				//Förändra x - koordinaten när tangenten hålls nere
				--x; 

				//en metod från standardklassen Rectangle, flyttar rektangel r1
				r1.setLocation(x, y);
				
				//Sätt denna bild
				img =tk.getImage(cl.getResource("birdRightWalk.gif"));

				repaint();
			
			/* 
			 * Kontroll om det går att skjuta och kulan ska då ta denna riktning
			 * Det går alltså inte skjuta så länge kulan är kvar på plan.
			 * Skottrikningn är samma som redsAni, för att spara in på det som skickas
			 */
			if(skjutKlar)
			{ 
			skottRiktning=4;//--x
			}
			//Denna bestämmer vilken bild som ska visas på den rosa fågeln Red
			redsAni=4;

			//gör om koordinater till en String, med ett mellanrum
			str = Integer.toString(x) + " " + Integer.toString(y)+ " " +Integer.toString(redsAni)+" "+Integer.toString(0);
			
				try
				{
				Skicka med= new Skicka(BirdyGame.tillAdr,BirdyGame.tillPort,str);
				//anropa metoden för att skicka infon
				med.SkickaMed();
				}
				catch(IOException ei){}

			}//INTE krock
			
			/*
			 * Hanterar hur Birdy-objektet ska bete sig vid krock, dvs. stötta bort sig
			 * Krockar kollas av i BirdyGame run
			 */
			if(BirdyGame.krock)
				{
				//Förändra x - koordinaten åt "andra" hållet; Figuren Studsar bort
				x+=5;
				++x;
				
				//en metod från standardklassen Rectangle, flyttar rektangel r1
				r1.setLocation(x, y);

				img =tk.getImage(cl.getResource("birdRightWalk.gif"));
				repaint();
				//Sätts genast till falsk, det får inte påverka senare
				BirdyGame.krock=false;

				}//slut krock
				
				//Slut på den vänstra PIL tangent hanteraren
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
			//gör om koordinater till en String, med ett mellanrum
			str = Integer.toString(x) + " " + Integer.toString(y)+ " " +Integer.toString(redsAni)+" "+Integer.toString(0);
				try
				{
				Skicka med= new Skicka(BirdyGame.tillAdr,BirdyGame.tillPort,str);
				//anropa metoden för att skicka infon
				med.SkickaMed();
				}
				catch(IOException ei){}
		}//inte krock
		
			//om rektanglar skär varanndras bana utförs dessa satser
			if(BirdyGame.krock)//krock
			{			
			x-=5;
			--x;
			//en metod från standardklassen Rectangle, flyttar rektangel r1
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
			//gör om koordinater till en String, med ett mellanrum
			str = Integer.toString(x) + " " + Integer.toString(y)+ " " +Integer.toString(redsAni)+" "+Integer.toString(0);
				try
				{
				Skicka med= new Skicka(BirdyGame.tillAdr,BirdyGame.tillPort,str);
				//anropa metoden för att skicka infon
				med.SkickaMed();
				}
				catch(IOException ei){}
			}//inte krock
				
					if(BirdyGame.krock)//krock
					{
					y+=5;
					++y;
					//en metod från standardklassen Rectangle, flyttar rektangel r1
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
			//gör om koordinater till en String, med ett mellanrum
			str = Integer.toString(x) + " " + Integer.toString(y)+ " " +Integer.toString(redsAni)+" "+Integer.toString(0);
				try
				{
				Skicka med= new Skicka(BirdyGame.tillAdr,BirdyGame.tillPort,str);
				//anropa metoden för att skicka infon
				med.SkickaMed();
				}
				catch(IOException ei){}
			}//inte krock
	
					if(BirdyGame.krock)//krock
					{
					y-=5;
					--y;
					//en metod från standardklassen Rectangle, flyttar rektangel r1
					r1.setLocation(x, y);

					img =tk.getImage(cl.getResource("birdFrontWalk.gif"));

					repaint();

					BirdyGame.krock=false;

					}//krock
				break;
		
		/*Denna handhar "kjutningen". Det innebär att en kula skapas +32 x/y-koordinater
		  från spelarens fåglar. Infon skickas via UDP*/
		case KeyEvent.VK_SPACE: 

		if(skjutKlar) 
		{ 
		//När denna blir sann ska man inte kunna skjuta, förrän den åter blir false. 	
		spelarenAvfyradeEnKula=true;
		
		//Koordinater till den gula fågeln i BirdyGame klassen
		birdyX=x+32;
 		birdyY=y+32;

		//Här ändras playerHasFiredhisShot till 1, mao har spelaren avyrat en kula(mest för tydligheten skull)
		playerHasFiredhisShot=1;
		//Denna info skickas och triggar diverse i BirdyGame klassen hos motspelaren så en kula skapas (Red's)		
		str = Integer.toString(x) + " " + Integer.toString(y)+ " " +
		Integer.toString(redsAni)+" " +Integer.toString(playerHasFiredhisShot);
		try
		{
		Skicka med= new Skicka(BirdyGame.tillAdr,BirdyGame.tillPort,str);
		//anropa metoden för att skicka infon
		med.SkickaMed();
		}
		catch(IOException ei){}
		//Stäng tillfället av skjut möjligheten
		skjutKlar=false;

		}
		break;

			} // switch
		//Denna ändrar koordinater så figuren kan rör sig
		setBounds(x, y, getWidth(), getHeight());
		repaint();


		} // keyPressed
	} // PilLyssnare

}//birdyklass
