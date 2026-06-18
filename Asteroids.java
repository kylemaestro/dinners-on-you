/**********************************************************************************************************************************
 _____     __     __   __     __   __     ______     ______     ______        ______     __   __        __  __     ______     __  __
/\  __-.  /\ \   /\ "-.\ \   /\ "-.\ \   /\  ___\   /\  == \   /\  ___\      /\  __ \   /\ "-.\ \      /\ \_\ \   /\  __ \   /\ \/\ \
\ \ \/\ \ \ \ \  \ \ \-.  \  \ \ \-.  \  \ \  __\   \ \  __<   \ \___  \     \ \ \/\ \  \ \ \-.  \     \ \____ \  \ \ \/\ \  \ \ \_\ \
 \ \____-  \ \_\  \ \_\\"\_\  \ \_\\"\_\  \ \_____\  \ \_\ \_\  \/\_____\     \ \_____\  \ \_\\"\_\     \/\_____\  \ \_____\  \ \_____\
  \/____/   \/_/   \/_/ \/_/   \/_/ \/_/   \/_____/   \/_/ /_/   \/_____/      \/_____/   \/_/ \/_/      \/_____/   \/_____/   \/_____/

  Version 1.1
***********************************************************************************************************************************/
//k.andrus19@gmail.com
//zachary.istas.378@k12.friscoisd.org
//cameron.ford.311@k12.friscoisd.org

//Import statements
import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.DecimalFormat;
import javax.imageio.*;
import java.io.*;
import java.awt.image.*;


public class Asteroids extends Applet implements Runnable, KeyListener
{
	String version = "1.1";

	DecimalFormat money = new DecimalFormat("$#,###,###.##");

	//Class variables
	long startTime, endTime, framePeriod;
	long currentTime, desiredRegenTimeOne, desiredRegenTimeTwo, desiredBaseRegenOne, desiredBaseRegenTwo, detTimeOne, detTimeTwo;
	long starMoveDelay, accDeclineOne, accDeclineTwo, respawnTimeOne, respawnTimeTwo;
	long minesOneDetTime, minesTwoDetTime, minesOneFlash, minesTwoFlash, minesOneCooldown=0, minesTwoCooldown=0;
	long destroyerOneRespawn = 0, destroyerTwoRespawn = 0;;
	int numshotsOne, numshotsTwo, prevX=0, prevY=0, xCheck=-150, yCheck=-150, starsX=0, starsY=0, p1Deaths=0, p2Deaths=0, d1Deaths=0, d2Deaths=0, t1Deaths=0, t2Deaths=0;
	boolean playerOneShooting, playerTwoShooting, sentryOneShooting, sentryTwoShooting, paused, explosionDrawn = false;
	boolean beginMinesTwoRegen = false, beginMinesOneRegen = false;
	boolean gameOver = false;



	//Activates developer mode
	boolean devMode = false;

	//Color declaration (BLUE)
	Color blue = new Color(0, 153, 255);

	//Random number generator
	Random rand = new Random();

	//Generates buffered image
	Dimension dim; //Stores size of back buffer
	Image img;     //Back buffer object
	Graphics g;    //Used to draw on the back buffer

	//Constructs objects
	Ship playerOne, playerTwo;
	Waypoint wayOne, wayTwo;
	Base baseOne, baseTwo;
	Destroyer destroyerOne, destroyerTwo;
	Sentry sentryOne;
	Sentry sentryTwo;
	Shot[] shotsOne;
	Shot[] shotsTwo;
	Mine minesOne, minesTwo;
	Explosion[] explosions = new Explosion[15];
	Star[] stars = new Star[150];
	News news;
	ArrayList<News> newsQueue = new ArrayList<News>();
	Bill bill;

	Turret[] turretsRed;
	boolean[] turretsRedShooting;

	Turret[] turretsBlue;
	boolean [] turretsBlueShooting;

	//Creates main game thread
	Thread thread;


	//Initialization method
	public void init()
	{
		//Applet resolution
		resize(1000, 600);

		//Construts ship objects

			//Constructs ships: x, y, angle, acceleration, velocityDecay, rotationalspeed, shotDelay, playerID, health, ammo, stun
			playerOne = new Ship(15,  300, 0,       2, .98, .1, 17, 1, 5, 50, false);
			playerTwo = new Ship(985, 300, Math.PI, 2, .98, .1, 17, 2, 5, 50, false);

			//Constructs waypoints: playerID
			wayOne = new Waypoint(1);
			wayTwo = new Waypoint(2);

			//Constructs destroyer objects: x, y, angle, acceleration, velocityDecay, rotationalSpeed, playerID, health, alive)
			destroyerOne = new Destroyer(-50, 300, 0,        .3, .98, .007, 1, 75, true);
			destroyerTwo = new Destroyer(1050, 300, Math.PI, .3, .98, .007, 2, 75, true);

			//Constructs sentry objects: x, y, angle, drawAngle, rotationalSpeed, shotDelay, Health, playerID
			sentryOne = new Sentry(50, 50, 0, 0, .1, 50, 2, 1);
			sentryTwo = new Sentry(50, 50, 0, 0, .1, 50, 2, 2);

			//Constructs bases: health, teamID, regenDelay
			baseOne = new Base(20, 1, 15);
			baseTwo = new Base(20, 2, 15);

			//Constructs mines: x, y, detRadius, expRadius, playerID, color, activated
			minesOne = new Mine(50, 50, 80, 80, 1, "red", false, true);
			minesTwo = new Mine(50, 50, 80, 80, 2, "red", false, true);

			//constructs the bill
			bill = new Bill(3);;

			//Constucts explosion array
			for(int c=0; c<explosions.length; c++)
			{
				explosions[c] = new Explosion(50, 50, 10, 1);
			}

			//Constructs stars array
			for(int f=0; f<stars.length; f++)
			{
				stars[f] = new Star(50, 50);
			}

			//Calls method to construct all the stars before the game begins
			starsCalc();

			//Constructs turret: x, y, angle, drawAngle, shotDelay, inHealth, inPlayerID, range, deployable, alive
			turretsRed = new Turret[5];
			turretsBlue = new Turret[5];

			//Constructs destroyer turrets: x, y, angle, drawAngle, shotDelay, inHealth, inPlayerID, range, deployable, alive, accuracy(0 is flawless accuracy), shotLife
			turretsRed[0] = new Turret(destroyerOne.getX(), destroyerOne.getY(), 0, 0, 50, 1, 1, 150, false, true, 10, 40);
			turretsRed[1] = new Turret(destroyerOne.getX(), destroyerOne.getY(), 0, 0, 50, 1, 1, 150, false, true, 10, 40);
			turretsBlue[0] = new Turret(destroyerTwo.getX(), destroyerTwo.getY(), Math.PI, Math.PI, 50, 1, 2, 150, false, true, 10, 40);
			turretsBlue[1] = new Turret(destroyerTwo.getX(), destroyerTwo.getY(), Math.PI, Math.PI, 50, 1, 2, 150, false, true, 10, 40);

			//Base turrets
			turretsRed[2] = new Turret(20, 250, 0, 0, 50, 1, 1, 175, false, true, 10, 40);
			turretsRed[3] = new Turret(20, 350, 0, 0, 50, 1, 1, 175, false, true, 10, 40);
			turretsBlue[2] = new Turret(980, 250, Math.PI, Math.PI, 50, 1, 2, 175, false, true, 10, 40);
			turretsBlue[3] = new Turret(980, 350, Math.PI, Math.PI, 50, 1, 2, 175, false, true, 10, 40);

			//Deployable turrets
			turretsRed[4] = new Turret(500, 300, 0, 0, 75, 3, 1, 300, true, false, 3, 75);
			turretsBlue[4] = new Turret(500, 300, 0, 0, 75, 3, 2, 300, true, false, 3, 75);

			//Creates player shot arrays
			shotsOne = new Shot[50]; //1 shot per frame, 40 fps for game. No need for higher var
			shotsTwo = new Shot[50];

		numshotsOne = 0;
		numshotsTwo = 0;

		playerOneShooting = false;
		playerTwoShooting = false;

		turretsRedShooting = new boolean[turretsRed.length];
		turretsBlueShooting = new boolean[turretsBlue.length];

		for(int y=0; y<turretsRed.length; y++)
		{
			turretsRedShooting[y] = false;
		}
		for(int y=0; y<turretsBlue.length; y++)
		{
			turretsBlueShooting[y] = false;
		}

		//Begins the game paused
		paused=false;
		addKeyListener(this);

		//Starts timer
		startTime = 0;
		endTime = 0;
		framePeriod = 25; //25 ms ~ 40 fps

		//Double Buffering
		dim = getSize();                          //Set dim equal to size of applet
		img = createImage(dim.width, dim.height); //Creates the back buffer
		g = img.getGraphics();                    //retrives Graphics object for back buffer

		//Auto-starts the game as soon as the applet finishes loading
		//(no need to press ENTER to begin; ENTER still pauses/unpauses)
		playerOne.setActive(true);
		playerTwo.setActive(true);
		sentryOne.setActive(true);
		sentryTwo.setActive(true);
		baseOne.setActive(true);
		baseTwo.setActive(true);
		destroyerOne.setActive(true);
		destroyerTwo.setActive(true);
		wayOne.setActive(true);
		wayTwo.setActive(true);
		minesOne.setActive(true);
		minesTwo.setActive(true);

		for(int y=0; y<turretsRed.length; y++)
		{
			turretsRed[y].setActive(true);
		}
		for(int y=0; y<turretsBlue.length; y++)
		{
			turretsBlue[y].setActive(true);
		}

		//Creates new thread and starts it
		thread = new Thread(this);
		thread.start();
	}


	public void paint(Graphics gfx)
	{
		//Sets black background
		g.setColor(Color.black);
		g.fillRect(0, 0, 1000, 600);

		//Draws stars in background
		for(int k=0; k<stars.length; k++)
		{
			if(stars[k].getAlive()==true)
			{
				stars[k].draw(g);
			}
		}

		//Updates player ships/sentrys
		baseOne.draw(g);
		baseTwo.draw(g);
		sentryOne.draw(g);
		sentryTwo.draw(g);
		wayOne.draw(g);
		wayTwo.draw(g);
		if(destroyerOne.getAlive()==true)
			destroyerOne.draw(g);
		if(destroyerTwo.getAlive()==true)
			destroyerTwo.draw(g);
		playerOne.draw(g);
		playerTwo.draw(g);
		minesOne.draw(g);
		minesTwo.draw(g);
		if(newsQueue.size()!=0 && newsQueue.get(0).getAlive()==true)
			newsQueue.get(0).draw(g);

		for(int k=0; k<explosions.length; k++)
		{
			if(explosions[k].getAlive()==true)
			{
				explosions[k].draw(g);
			}
		}

		//an IF statement to differentiate the base turrets from the destroyer turrets will be required
		for(int y=0; y<turretsRed.length; y++)
		{
			if(turretsRed[y].getAlive()==true)
			{
				if(y!=0&&y!=1)
				{
					if(baseOne.getAlive()==true)
						turretsRed[y].draw(g);
				}
			}
		}
		for(int y=0; y<turretsBlue.length; y++)
		{
			if(turretsBlue[y].getAlive()==true)
			{
				if(y!=0&&y!=1)
				{
					if(baseTwo.getAlive()==true)
						turretsBlue[y].draw(g);
				}
			}
		}

		turretsRed[4].draw(g);
		turretsBlue[4].draw(g);

		//Checks if a player has won
		checkVictory();

		//Controls and health displays
		g.setColor(Color.red);
		if(playerOne.getHealth()<=0)
			g.drawString("Health: 0", 25, 25);
		else
			g.drawString("Health: " +playerOne.getHealth()+"", 25, 25);

		g.drawString("Ammo: " +playerOne.getAmmo()+"", 25, 50);
		g.drawString("Base Health: " +baseOne.getHealth()+"", 25, 75);

		g.setColor(blue);
		if(playerTwo.getHealth()<=0)
			g.drawString("Health: 0", 875, 25);
		else
			g.drawString("Health: " +playerTwo.getHealth()+"", 875, 25);

		g.drawString("Ammo: " +playerTwo.getAmmo()+"", 875, 50);
		g.drawString("Base Health: " +baseTwo.getHealth()+"", 875, 75);

		g.setColor(Color.white);

		if(paused == true)
		{
			g.setColor(Color.white);
			g.drawString("ENTER toggles pause mode", 450, 300);
		}

		//Prints version number if dev mode is on
		if(devMode==true)
		{
			g.setColor(Color.white);
			g.drawString("Asteroids version "+version+"", 15, 580);
		}

		/*g.drawString("destroyerTwoRespawn:" +destroyerTwoRespawn+"", 500, 300);
		g.drawString("currentTime:" +currentTime+"", 500, 350);*/

		if(gameOver == true)
		{
			bill.draw(g);
			paused = true;
		}

		//Double buffer for shots
		for(int i=0; i<numshotsOne; i++)
			shotsOne[i].draw(g);

		gfx.drawImage(img, 0, 0, this); //Copies back buffer to the screen

		for(int i=0; i<numshotsTwo; i++)
			shotsTwo[i].draw(g);

		gfx.drawImage(img, 0, 0, this); //Copies back buffer to the screen



	} //Closes paint()


	//Calls paint without flickering
	public void update(Graphics gfx)
	{
		paint(gfx);
	}


	/********************************************************
	*  __  __       _         _____ _                        _
	* |  \/  | __ _(_)_ __   |_   _| |__  _ __ ___  __ _  __| |
	* | |\/| |/ _` | | '_ \    | | | '_ \| '__/ _ \/ _` |/ _` |
	* | |  | | (_| | | | | |   | | | | | | | |  __/ (_| | (_| |
	* |_|  |_|\__,_|_|_| |_|   |_| |_| |_|_|  \___|\__,_|\__,_|
    *
    ********************************************************/


	//Main game method
	public void run()
	{
		if(gameOver == false)
		{
			for(;;)
			{
				//Keeps track of current system time
				currentTime = System.currentTimeMillis();

				//Always printing news (update method called as needed)
				printNews();

				//Adds dollars to fuel doubles in Bill.java if a ship/destroyer is moving
				if(playerOne.getAccelerating()==true)
					bill.addRedShipFuel();
				if(playerTwo.getAccelerating()==true)
					bill.addBlueShipFuel();
				if(destroyerOne.getAccelerating()==true)
					bill.addBlueDestroyerFuel();
				if(destroyerTwo.getAccelerating()==true)
					bill.addRedDestroyerFuel();

				//Keeps sentrys shooting while deployed
				if(sentryOne.getAlive()==true && playerOne.isActive()==true)
				sentryOneShooting = true; //sentry should always be shooting

				if(sentryTwo.getAlive()==true && playerOne.isActive()==true)
				sentryTwoShooting = true; //sentry should always be shooting

				for(int y=0; y<turretsRed.length; y++)
				{
					if(turretsRed[y].getAlive()==true && playerOne.isActive()==true)
					turretsRedShooting[y] = true; //Turrets should always be shooting
				}
				for(int y=0; y<turretsBlue.length; y++)
				{
					if(turretsBlue[y].getAlive()==true && playerOne.isActive()==true)
					turretsBlueShooting[y] = true; //Turrets should always be shooting
				}

				//Respawn when players die
				if(playerOne.getHealth()<=0)
				{
					respawnOne();
				}
				if(playerTwo.getHealth()<=0)
				{
					respawnTwo();
				}
				if(destroyerOne.getHealth()<=0)
				{
					destroyerOneRespawn();
				}
				if(destroyerTwo.getHealth()<=0)
				{
					destroyerTwoRespawn();
				}

				//Checks for collosions
				checkCollisions();

				//Rotate sentrys
				Target(1);
				Target(2);

				//Tracks targets for the turrets
				turretTargetRed();
				turretTargetBlue();

				//Handles the mine calculations and damage
				minesActivation();

				//Changes mine indicator light to show armed status; beeping for activated; etc
				mineLight();

				//Begins regenerative methods for mines if needed
				if(beginMinesOneRegen == true)
					minesOneRegen();
				if(beginMinesTwoRegen == true)
					minesTwoRegen();

				//Moves destroyers
				if(destroyerOne.getAlive()==true)
					destroyerOneMovement();
				if(destroyerTwo.getAlive()==true)
					destroyerTwoMovement();

				//Moves stars every 1000 ms
				if(starMoveDelay == 0)
					starMoveDelay = currentTime + 250;
				if(currentTime > starMoveDelay)
				{
					for(int h=0; h<stars.length; h++)
					{
						stars[h].move();
					}

					starMoveDelay = 0;
				}


					//Marks start time for thread
					startTime = System.currentTimeMillis();

					if(!paused)
					{
						//Moves each shot and deletes 'dead' ones
						playerOne.move(dim.width, dim.height);
						playerTwo.move(dim.width, dim.height);

						//Rotates sentrys according to angle
						sentryOne.move();
						sentryTwo.move();

						//Updates mines status (Note: Important)
						if(detTimeTwo != 0 && currentTime>detTimeTwo)
						{
							minesTwo.setArmed(true);
							detTimeTwo = 0;
						}
						if(detTimeOne != 0 && currentTime>detTimeOne)
						{
							minesOne.setArmed(true);
							detTimeOne = 0;
						}

						//Binds destroyer turrets to the destroyers
						//THIS IS WHERE THE CODE NEEDS TO BE CHANGED TO SET THE TURRETS TO TWO DIFFERENT POINTS ON THE DESTROYERS###########################################################
						for(int y=0; y<turretsRed.length; y++)
						{
							turretsRed[y].move();
							if(y == 0 || y == 1)
							{
								turretsRed[y].setX(destroyerOne.getX());
								turretsRed[y].setY(destroyerOne.getY());
							}					}
						for(int y=0; y<turretsBlue.length; y++)
						{
							turretsBlue[y].move();
							if(y == 0 || y == 1)
							{
								turretsBlue[y].setX(destroyerTwo.getX());
								turretsBlue[y].setY(destroyerTwo.getY());
							}
						}

						//runs move() for destroyerOne
						if(destroyerOne.getAlive()==true)
							destroyerOne.move();
						if(destroyerTwo.getAlive()==true)
							destroyerTwo.move();

						//Processes playerOneShot array
						for(int i =0; i<numshotsOne; i++)
						{
							//Deletes shot if it doesn't hit anything too long
							shotsOne[i].move(dim.width, dim.height);

							if(shotsOne[i].getLifeLeft()<=0)
							{
								deleteplayerOneShot(i);
								i--;
							}
						}

							if(playerOneShooting && playerOne.canShoot())
							{
								//Adds a shot if the playerOne is playerOneShooting
								shotsOne[numshotsOne] = playerOne.shootPlayerOne();
								numshotsOne++;
								bill.setRedShotExpenses(bill.getRedShotExpenses()+bill.getShotCost());
							}

							if(sentryOneShooting && sentryOne.canShoot())
							{
								//Adds a shot if the sentryOne is sentryOneShooting
								shotsOne[numshotsOne] = sentryOne.shootPlayerOneSentry();
								numshotsOne++;
								bill.setRedShotExpenses(bill.getRedShotExpenses()+bill.getShotCost());
							}

							for(int y=0; y<turretsRed.length; y++)
							{
								if(turretsRedShooting[y] && turretsRed[y].canShoot())
								{
									//Adds a shot if the red turret is shooting
									shotsOne[numshotsOne] = turretsRed[y].shootPlayerOneTurret();
									numshotsOne++;
									bill.setRedShotExpenses(bill.getRedShotExpenses()+bill.getShotCost());
								}
							}
							for(int y=0; y<turretsBlue.length; y++)
							{
								if(turretsBlueShooting[y] && turretsBlue[y].canShoot())
								{
									//adds a shot if the blue turret is shooting
									shotsTwo[numshotsTwo] = turretsBlue[y].shootPlayerTwoTurret();
									numshotsTwo++;
									bill.setBlueShotExpenses(bill.getBlueShotExpenses()+bill.getShotCost());
								}
							}

						//Runs through playerTwoShot array
						for(int i =0; i<numshotsTwo; i++)
						{
							//Deletes shot if it doesn't hit anything too long
							shotsTwo[i].move(dim.width, dim.height);

							if(shotsTwo[i].getLifeLeft()<=0)
							{
								deleteplayerTwoShot(i);
								i--;
							}
						}

							if(playerTwoShooting && playerTwo.canShoot())
							{
								//Adds a shot if the playerTwo is playerTwoShooting
								shotsTwo[numshotsTwo] = playerTwo.shootPlayerTwo();
								numshotsTwo++;
								bill.setBlueShotExpenses(bill.getBlueShotExpenses()+bill.getShotCost());
							}

							if(sentryTwoShooting && sentryTwo.canShoot())
							{
								//adds a shot if the sentryTwo is sentryTwoShooting
								shotsTwo[numshotsTwo] = sentryTwo.shootPlayerTwoSentry();
								numshotsTwo++;
								bill.setBlueShotExpenses(bill.getBlueShotExpenses()+bill.getShotCost());
							}

							//Regens baseOne health over time
							if(desiredBaseRegenOne == 0)
								desiredBaseRegenOne = currentTime + 500;

							if(baseOne.canRegen() == true && desiredBaseRegenOne < currentTime)
							{
								baseOne.addHealth(1);
								bill.setRedBaseExpenses(bill.getRedBaseExpenses()+bill.getBaseCost());
								desiredBaseRegenOne = 0;
							}

							//Regens baseTwo health over time
							if(desiredBaseRegenTwo == 0)
								desiredBaseRegenTwo = currentTime + 500;

							if(baseTwo.canRegen() == true && desiredBaseRegenTwo < currentTime)
							{
								baseTwo.addHealth(1);
								bill.setBlueBaseExpenses(bill.getBlueBaseExpenses()+bill.getBaseCost());
								desiredBaseRegenTwo = 0;
							}

						} //Closes !paused

					//Refreshes the screen according the the framerate
					repaint();

					//Resets the detonation light indicators for mines
					if(minesOne.getAlive()==false)
						minesOne.setArmed(false);
					if(minesTwo.getAlive()==false)
						minesTwo.setArmed(false);

					try{
						//Mark end time
						endTime = System.currentTimeMillis();

						//Dont sleep for negative amount of time :s
						if(framePeriod - (endTime - startTime)>0)
							Thread.sleep(framePeriod -
								(endTime-startTime));
						}catch(InterruptedException e){
						}

			} //Closes for(;;)

		}

	} //Closes run()


	private void deleteplayerOneShot(int index)
	{
		numshotsOne--;
		for(int i = index; i<numshotsOne; i++)
			shotsOne[i] = shotsOne[i+1];
		shotsOne[numshotsOne] = null;
	}


	private void deleteplayerTwoShot(int index)
	{
		numshotsTwo--;
		for(int i = index; i<numshotsTwo; i++)
			shotsTwo[i] = shotsTwo[i+1];
		shotsTwo[numshotsTwo] = null;
	}


	/************************************************************
	 *   _  __            ____  _           _ _
     *  | |/ /___ _   _  | __ )(_)_ __   __| (_)_ __   __ _ ___
     *  | ' // _ \ | | | |  _ \| | '_ \ / _` | | '_ \ / _` / __|
     *  | . \  __/ |_| | | |_) | | | | | (_| | | | | | (_| \__ \
     *  |_|\_\___|\__, | |____/|_|_| |_|\__,_|_|_| |_|\__, |___/
     *            |___/                               |___/
	 ***********************************************************/

	 //Controls\\

	 	//Player One:
	 	//W - Forward Thrust
	 	//A/D - Turn Left/Right
	 	//S - Mine Placement/Detonation
	 	//Q - Destroyer Waypoint
	 	//E - Sentry
	 	//Space - Shoot
	 	//U - Instant Victory

	 	//Player Two: (numpad)
	 	//8 - Forward Thrust
	 	//4/6 - Turn Left/Right
	 	//5 - Mine Placement/Detonation
	 	//7 - Destroyer Waypoint
	 	//9 - Sentry
	 	//0 - Shoot
	 	//I - Instant Victory

	//Manages key inputs for pressedKeys
	public void keyPressed(KeyEvent e)
	{
		if(gameOver==false)
		{
			if(e.getKeyCode()==KeyEvent.VK_ENTER)
			{
				if(!playerOne.isActive() && !paused)
				{
					playerOne.setActive(true);
					playerTwo.setActive(true);
					sentryOne.setActive(true);
					sentryTwo.setActive(true);
					baseOne.setActive(true);
					baseTwo.setActive(true);
					destroyerOne.setActive(true);
					destroyerTwo.setActive(true);
					wayOne.setActive(true);
					wayTwo.setActive(true);
					minesOne.setActive(true);
					minesTwo.setActive(true);

					for(int y=0; y<turretsRed.length; y++)
					{
						turretsRed[y].setActive(true);
					}
					for(int y=0; y<turretsBlue.length; y++)
					{
						turretsBlue[y].setActive(true);
					}
				}
				else{
					paused=!paused;
					if(paused)
					{
						playerOne.setActive(false);
						playerTwo.setActive(false);
						sentryOne.setActive(false);
						sentryTwo.setActive(false);
						baseOne.setActive(false);
						baseTwo.setActive(false);
						destroyerOne.setActive(false);
						destroyerTwo.setActive(false);
						wayOne.setActive(false);
						wayTwo.setActive(false);
						minesOne.setActive(false);
						minesTwo.setActive(false);

						for(int y=0; y<turretsRed.length; y++)
						{
							turretsRed[y].setActive(false);
						}
						for(int y=0; y<turretsBlue.length; y++)
						{
							turretsBlue[y].setActive(false);
						}
					}
					else
					{
						playerOne.setActive(true);
						playerTwo.setActive(true);
						sentryOne.setActive(true);
						sentryTwo.setActive(true);
						baseOne.setActive(true);
						baseTwo.setActive(true);
						destroyerOne.setActive(true);
						destroyerTwo.setActive(true);
						wayOne.setActive(true);
						wayTwo.setActive(true);
						minesOne.setActive(true);
						minesTwo.setActive(true);
						for(int y=0; y<turretsRed.length; y++)
						{
							turretsRed[y].setActive(true);
						}
						for(int y=0; y<turretsBlue.length; y++)
						{
							turretsBlue[y].setActive(true);
						}
					}
				}
			}else if(paused || !playerOne.isActive())

			return;


			//A good place to get keycodes: http://www.cambiaresearch.com/articles/15/javascript-char-codes-key-codes


			/*Player Two Keycodes*/

			if(playerTwo.getStun() == false)
			{

				if(e.getKeyCode()== 104)
					playerTwo.setAccelerating(true);
				else if(e.getKeyCode()== 100)
					playerTwo.setTurningLeft(true);
				else if(e.getKeyCode()== 102)
					playerTwo.setTurningRight(true);
				else if(e.getKeyCode()== 96)
					playerTwoShooting = true;
				else if(e.getKeyCode()==73){
					gameOver = true; bill.setWinnerID(2);
				}
				else if(e.getKeyCode()== 103)
				{
					if(wayTwo.getAlive()==false && destroyerTwo.getAlive()==true && wayTwo.getWayAllowed() == true)
					{
						wayTwo.setAlive(true);
						wayTwo.setX(playerTwo.getX());
						wayTwo.setY(playerTwo.getY());
					}else
						wayTwo.setAlive(false);
				}
				else if(e.getKeyCode()== 105)
				{
					for(int y=0; y<turretsBlue.length; y++)
					{
						if(turretsBlue[y].getDeployable()==true)
						{
							if(turretsBlue[y].getAlive()==false&&playerTwo.getSentryAllowed()==true)
							{
								turretsBlue[y].setDrawAngle(playerTwo.getAngle());
								turretsBlue[y].setAlive(true);
								turretsBlue[y].setX(playerTwo.getX());
								turretsBlue[y].setY(playerTwo.getY());
								turretsBlue[y].setHealth(turretsBlue[y].getDefaultHealth());

							}
						}
					}
				}
				else if(e.getKeyCode()==101)
				{
					if((minesTwo.getAlive()==false && minesTwo.getAllowed()==true) && minesTwo.getActive()==true)
					{
						minesTwo.setAlive(true);
						minesTwo.setArmed(false);

						//Casts the double x/y locations for ships into usable ints
						int p1X = (int)playerTwo.getX();
						int p1Y = (int)playerTwo.getY();

						minesTwo.setX(p1X);
						minesTwo.setY(p1Y);

						//Begins detonation allowance timer
						if(detTimeTwo == 0)
							detTimeTwo = currentTime + 1000;

						minesTwo.setAllowed(false);
					}
					if(minesTwo.getAlive()==true)
					{
						if(currentTime > detTimeTwo)
						{
							minesTwo.setAlive(false);
							minesTwo.setAllowed(false);
							beginMinesTwoRegen=true;
						}
					}
				}

			} //Closes if(!stunned)


			/*Player One Keycodes*/

			if(playerOne.getStun() == false)
			{

				if(e.getKeyCode()==KeyEvent.VK_W)
					playerOne.setAccelerating(true);
				else if(e.getKeyCode()==KeyEvent.VK_A)
					playerOne.setTurningLeft(true);
				else if(e.getKeyCode()==KeyEvent.VK_D)
					playerOne.setTurningRight(true);
				else if(e.getKeyCode()== 32)
					playerOneShooting = true;
				else if(e.getKeyCode()==85){
					gameOver = true; bill.setWinnerID(1);
				}
				else if(e.getKeyCode()==KeyEvent.VK_Q)
				{
					if(wayOne.getAlive()==false && destroyerOne.getAlive()==true && wayOne.getWayAllowed() == true)
					{
						wayOne.setAlive(true);
						wayOne.setX(playerOne.getX());
						wayOne.setY(playerOne.getY());
					}else
						wayOne.setAlive(false);
				}
				else if(e.getKeyCode()==69)
				{
					for(int y=0; y<turretsRed.length; y++)
					{
						if(turretsRed[y].getDeployable()==true)
						{
							if(turretsRed[y].getAlive()==false&&playerOne.getSentryAllowed()==true)
							{
								turretsRed[y].setDrawAngle(playerOne.getAngle());
								turretsRed[y].setAlive(true);
								turretsRed[y].setX(playerOne.getX());
								turretsRed[y].setY(playerOne.getY());
								turretsRed[y].setHealth(turretsRed[y].getDefaultHealth());

							}
						}
					}
				}
				else if(e.getKeyCode()==KeyEvent.VK_S)
				{
					if((minesOne.getAlive()==false && minesOne.getAllowed()==true) && minesOne.getActive()==true)
					{
						minesOne.setAlive(true);
						minesOne.setAllowed(false);

						//Casts the double x/y locations for ships into usable ints
						int p1X = (int)playerOne.getX();
						int p1Y = (int)playerOne.getY();

						minesOne.setX(p1X);
						minesOne.setY(p1Y);

						//Begins detonation allowance timer
						if(detTimeOne == 0)
							detTimeOne = currentTime + 1000;
						if(currentTime>detTimeOne)
							minesOne.setArmed(true);

						minesOne.setAllowed(false);

					}
					if(minesOne.getAlive()==true)
					{
						if(currentTime > detTimeOne)
						{
							minesOne.setAlive(false);
							minesOne.setAllowed(false);
							beginMinesOneRegen=true;
						}
					}
				}

			} //Closes if(!stunned)

		} //Closes if(!gameover)

	} //Closes keyPressed()


	//Manages key input for KeyReleaseds
	public void keyReleased(KeyEvent e)
	{
		/*Player Two Keycodes*/

		if(playerTwo.getStun() == false)
		{

			if(e.getKeyCode()== 104)
				playerTwo.setAccelerating(false);
			else if(e.getKeyCode()== 100)
				playerTwo.setTurningLeft(false);
			else if(e.getKeyCode()== 102)
				playerTwo.setTurningRight(false);
			else if(e.getKeyCode()== 96 || gameOver == true)
				playerTwoShooting = false;

		}

		/*Player One Keycodes*/

		if(playerOne.getStun() == false)
		{

			if(e.getKeyCode()==KeyEvent.VK_W)
				playerOne.setAccelerating(false);
			else if(e.getKeyCode()==KeyEvent.VK_A)
				playerOne.setTurningLeft(false);
			else if(e.getKeyCode()==KeyEvent.VK_D)
				playerOne.setTurningRight(false);
			else if(e.getKeyCode()==KeyEvent.VK_SPACE || gameOver == true)
				playerOneShooting = false;

		}

	} //Closes keyReleased()


	public void keyTyped(KeyEvent e){} //Needed, but unused


	//Checks for player victories
	public void checkVictory()
	{

		if(baseOne.getHealth()<=0)
		{
			g.setColor(Color.cyan);
			g.drawString("Blue wins!", 600, 360);
			playerOne.setActive(false);
			sentryOne.setActive(false);
			sentryOneShooting = false;
			sentryTwo.setActive(false);
			sentryTwoShooting = false;
			destroyerOne.setActive(false);
			destroyerTwo.setActive(false);
			for(int y=0; y<turretsRed.length; y++)
			{
				turretsRed[y].setActive(false);
				turretsRedShooting[y] = false;
				turretsBlueShooting[y] = false;
			}
		}

		if(baseTwo.getHealth()<=0)
		{
			g.setColor(Color.red);
			g.drawString("Red wins!", 600, 360);
			playerTwo.setActive(false);
			sentryOne.setActive(false);
			sentryOneShooting = false;
			sentryTwo.setActive(false);
			sentryTwoShooting = false;
			for(int y=0; y<turretsBlue.length; y++)
			{
				turretsBlue[y].setActive(false);
				turretsRedShooting[y] = false;
				turretsBlueShooting[y] = false;
			}

		}

	} //Closes checkVictory()


	/***********************************************************************************
	 *     ____      _ _ _     _               ____       _            _   _
	 *    / ___|___ | | (_)___(_) ___  _ __   |  _ \  ___| |_ ___  ___| |_(_) ___  _ __
	 *   | |   / _ \| | | / __| |/ _ \| '_ \  | | | |/ _ \ __/ _ \/ __| __| |/ _ \| '_ \
	 *	 | |__| (_) | | | \__ \ | (_) | | | | | |_| |  __/ ||  __/ (__| |_| | (_) | | | |
	 *	  \____\___/|_|_|_|___/_|\___/|_| |_| |____/ \___|\__\___|\___|\__|_|\___/|_| |_|
	 *
	 **********************************************************************************/


	//Checks for collisions
	public void checkCollisions()
	{
		//Detects collisions between playerTwo's shots and playerOne's ship
		for(int m = 0; m<numshotsTwo; m++)
		{
			//Checks if it is in use
			if(shotsTwo[m].getLifeLeft()>0)
			{
				if(playerOne.getBounds().contains(shotsTwo[m].getX(),shotsTwo[m].getY()) && playerOne.getHealth() > 0)
				{
					shotsTwo[m].setLifeLeft(shotsTwo[m].getLifeLeft()-shotsTwo[m].getLifeLeft());
					playerOne.setHealth(playerOne.getHealth()-1);
					if(playerOne.getHealth()<=0)
					{
						updateNews("Player One was shot down!");
						bill.setRedShipExpenses(bill.getRedShipExpenses()+bill.getShipCost());
						p1Deaths++;
					}

					//Draws explosion [casts double x/y of shots into usable ints]
					explosionsCalc((int)shotsTwo[m].getX(), (int)shotsTwo[m].getY(), 10, 2, false);

					continue;
				}
			}
		}

		//Detects collisions between playerOne's shots and playerTwo's ship
		for(int m = 0; m<numshotsOne; m++)
		{
			if(shotsOne[m].getLifeLeft()>0)
			{
				if(playerTwo.getBounds().contains(shotsOne[m].getX(),shotsOne[m].getY()) && playerTwo.getHealth() > 0)
				{
					shotsOne[m].setLifeLeft(shotsOne[m].getLifeLeft()-shotsOne[m].getLifeLeft());
					playerTwo.setHealth(playerTwo.getHealth()-1);
					if(playerTwo.getHealth()<=0)
					{
						System.out.println("Player 2 was shot down");
						bill.setBlueShipExpenses(bill.getBlueShipExpenses()+bill.getShipCost());
						p2Deaths++;
					}

					//Draws explosion [casts double x/y of shots into usable ints]
					explosionsCalc((int)shotsOne[m].getX(), (int)shotsOne[m].getY(), 10, 1, false);

					continue;
				}
			}
		}

		//Checks if sentryOne is shot by playerTwoShot
		if(sentryOne.getAlive()==true)
		{
			for(int m = 0; m<numshotsTwo; m++)
			{
				//checks if it is in use
				if(shotsTwo[m].getLifeLeft()>0)
				{
					if(sentryOne.getBounds().contains(shotsTwo[m].getX(),shotsTwo[m].getY()))
					{
						shotsTwo[m].setLifeLeft(shotsTwo[m].getLifeLeft()-shotsTwo[m].getLifeLeft());
						sentryOne.setHealth(sentryOne.getHealth()-1);
						if(sentryOne.getHealth()==0)
						{
							sentryOne.setAlive(false);
							System.out.println("Red sentry was shot down");
							bill.setRedTurretExpenses(bill.getRedTurretExpenses()+bill.getTurretCost());
						}

							//Draws explosion [casts double x/y of shots into usable ints]
							explosionsCalc((int)shotsTwo[m].getX(), (int)shotsTwo[m].getY(), 10, 2, false);

						continue;
					}
				}
			}
		}

		//checks if sentryTwo is shot by playerOneShot
		if(sentryTwo.getAlive()==true)
		{
			for(int m = 0; m<numshotsOne; m++)
			{
				//Checks if it is in use
				if(shotsOne[m].getLifeLeft()>0)
				{
					if(sentryTwo.getBounds().contains(shotsOne[m].getX(),shotsOne[m].getY()))
					{
						shotsOne[m].setLifeLeft(shotsOne[m].getLifeLeft()-shotsOne[m].getLifeLeft());
						sentryTwo.setHealth(sentryTwo.getHealth()-1);
						if(sentryTwo.getHealth()==0)
						{
							sentryTwo.setAlive(false);
							System.out.println("Blue sentry was shot down");
							bill.setBlueTurretExpenses(bill.getBlueTurretExpenses()+bill.getTurretCost());
						}

							//Draws explosion [casts double x/y of shots into usable ints]
							explosionsCalc((int)shotsOne[m].getX(), (int)shotsOne[m].getY(), 10, 1, false);

						continue;
					}
				}
			}
		}

		//Checks if baseOne is shot by playerTwoShot
		if(baseOne.getAlive()==true)
		{
			for(int m = 0; m<numshotsTwo; m++)
			{
				//checks if it is in use
				if(shotsTwo[m].getLifeLeft()>0)
				{
					if(baseOne.getBoundsOuter().contains(shotsTwo[m].getX(),shotsTwo[m].getY()))
					{
						shotsTwo[m].setLifeLeft(shotsTwo[m].getLifeLeft()-shotsTwo[m].getLifeLeft());
						baseOne.setHealth(baseOne.getHealth()-1);
						if(baseOne.getHealth()==0)
						{
							gameOver = true;
							baseOne.setAlive(false);
							System.out.println("Red base was destroyed!");
							System.out.println();
							System.out.println("-----|Round Statistics|-----");
							System.out.println("Winner: Player 2");
							System.out.println("Player One deaths: "+p1Deaths+"");
							System.out.println("Player Two deaths: "+p2Deaths+"");
							System.out.println("Red destroyers killed: "+d1Deaths+"");
							System.out.println("Blue destroyers killed: "+d1Deaths+"");
							System.out.println("Red turrets killed: "+t1Deaths+"");
							System.out.println("Blue turrets killed: "+t2Deaths+"");
							System.out.println("----------------------------");
							bill.setWinnerID(2);

							//i will also include hit percentage, shots fired, misses, etc.
						}

							//Draws explosion [casts double x/y of shots into usable ints]
							explosionsCalc((int)shotsTwo[m].getX(), (int)shotsTwo[m].getY(), 10, 2, false);
						continue;
					}
				}
			}
		}

		//checks if baseTwo is shot by playerOneShot
		if(baseTwo.getAlive()==true)
		{
			for(int m = 0; m<numshotsOne; m++)
			{
				//Checks if it is in use
				if(shotsOne[m].getLifeLeft()>0)
				{
					if(baseTwo.getBoundsOuter().contains(shotsOne[m].getX(),shotsOne[m].getY()))
					{
						shotsOne[m].setLifeLeft(shotsOne[m].getLifeLeft()-shotsOne[m].getLifeLeft());
						baseTwo.setHealth(baseTwo.getHealth()-1);
						if(baseTwo.getHealth()==0)
						{
							gameOver = true;
							baseTwo.setAlive(false);
							System.out.println("Blue base was destroyed!");
							System.out.println();
							System.out.println("-----|Round Statistics|-----");
							System.out.println("Winner: Player 1");
							System.out.println("Player One deaths: "+p1Deaths+"");
							System.out.println("Player Two deaths: "+p2Deaths+"");
							System.out.println("Red destroyers killed: "+d1Deaths+"");
							System.out.println("Blue destroyers killed: "+d1Deaths+"");
							System.out.println("Red turrets killed: "+t1Deaths+"");
							System.out.println("Blue turrets killed: "+t2Deaths+"");
							System.out.println("----------------------------");
							bill.setWinnerID(1);
						}

							//Draws explosion [casts double x/y of shots into usable ints]
							explosionsCalc((int)shotsOne[m].getX(), (int)shotsOne[m].getY(), 10, 1, false);

						continue;
					}
				}
			}
		}

		//checks if playerOne returns to their base for recovery
		if(baseOne.getAlive()==true)
		{
			if(baseOne.getBoundsInner().contains(playerOne.getX(), playerOne.getY())||baseOne.getBoundsOuter().contains(playerOne.getX(), playerOne.getY()))
			{
				playerOne.setShotsAllowed(false);
				playerOne.setSentryAllowed(false);
				wayOne.setWayAllowed(false);

				//Sets intervals between health/ammo replenishes
				if(desiredRegenTimeOne == 0)
					desiredRegenTimeOne = currentTime + 300;

				//Adds health every 200 ms
				if(currentTime > desiredRegenTimeOne)
				{
					if(playerOne.getHealth() < playerOne.getDefaultHealth())
						playerOne.addHealth(1);
					desiredRegenTimeOne = 0;
				}
				playerOne.setAmmo(playerOne.getDefaultAmmo());

			}else{
				playerOne.setShotsAllowed(true);
				playerOne.setSentryAllowed(true);
				wayOne.setWayAllowed(true);
			}
		}


		//checks if playerTwo returns to their base for recovery
		if(baseTwo.getAlive()==true)
		{
			if(baseTwo.getBoundsInner().contains(playerTwo.getX(), playerTwo.getY())||baseTwo.getBoundsOuter().contains(playerTwo.getX(), playerTwo.getY()))
			{
				playerTwo.setShotsAllowed(false);
				playerTwo.setSentryAllowed(false);
				wayTwo.setWayAllowed(false);

				//Sets intervals between health/ammo replenishes
				if(desiredRegenTimeTwo == 0)
					desiredRegenTimeTwo = currentTime + 300;

				//Adds health every 200 ms
				if(currentTime > desiredRegenTimeTwo)
				{
					if(playerTwo.getHealth() < playerTwo.getDefaultHealth())
						playerTwo.addHealth(1);
					desiredRegenTimeTwo = 0;
				}
				playerTwo.setAmmo(playerTwo.getDefaultAmmo());

			}else
			{
				playerTwo.setShotsAllowed(true);
				playerTwo.setSentryAllowed(true);
				wayTwo.setWayAllowed(true);
			}
		}

		//Detects collisions between playerTwo's shots and destroyerOne
		if(destroyerOne.getAlive()==true)
		{
			for(int m = 0; m<numshotsTwo; m++)
			{
				//Checks if it is in use
				if(shotsTwo[m].getLifeLeft()>0)
				{
					if(destroyerOne.getBounds().contains(shotsTwo[m].getX(),shotsTwo[m].getY()))
					{
						shotsTwo[m].setLifeLeft(shotsTwo[m].getLifeLeft()-shotsTwo[m].getLifeLeft());
						destroyerOne.setHealth(destroyerOne.getHealth()-1);

						//Draws explosion [casts double x/y of shots into usable ints]
						explosionsCalc((int)shotsTwo[m].getX(), (int)shotsTwo[m].getY(), 10, 2, false);

						continue;
					}
				}
			}
		}

		//Detects collisions between playerOne's shots and destroyerTwo
		if(destroyerTwo.getAlive()==true)
		{
			for(int m = 0; m<numshotsOne; m++)
			{
				if(shotsOne[m].getLifeLeft()>0)
				{
					if(destroyerTwo.getBounds().contains(shotsOne[m].getX(),shotsOne[m].getY()))
					{
						shotsOne[m].setLifeLeft(shotsOne[m].getLifeLeft()-shotsOne[m].getLifeLeft());
						destroyerTwo.setHealth(destroyerTwo.getHealth()-1);

						//Draws explosion [casts double x/y of shots into usable ints]
						explosionsCalc((int)shotsOne[m].getX(), (int)shotsOne[m].getY(), 10, 1, false);

						continue;
					}
				}
			}
		}

		//detects collisions between deployable RED turrets and playerTwo shots
		for(int m = 0; m<numshotsTwo; m++)
		{
			if(shotsTwo[m].getLifeLeft()>0)
			{
				for(int y=0; y<turretsRed.length; y++)
				{
					if(turretsRed[y].getDeployable()==true)
					{
						if(turretsRed[y].getBounds().contains(shotsTwo[m].getX(),shotsTwo[m].getY()))
						{
							if(turretsRed[y].getAlive()==true)
							{
								shotsTwo[m].setLifeLeft(shotsTwo[m].getLifeLeft()-shotsTwo[m].getLifeLeft());
								turretsRed[y].setHealth(turretsRed[y].getHealth()-1);
								if(turretsRed[y].getHealth()<=0)
								{
									turretsRed[y].setAlive(false);
									System.out.println("Red turret was shot down");
									bill.setRedTurretExpenses(bill.getRedTurretExpenses()+bill.getTurretCost());
									t1Deaths++;
								}

								//Draws explosion [casts double x/y of shots into usable ints]
								explosionsCalc((int)shotsTwo[m].getX(), (int)shotsTwo[m].getY(), 10, 2, false);

								continue;
							}
						}
					}
				}
			}
		}

		//detects collisions between deployable BLUE turrets and playerOne shots
		for(int m = 0; m<numshotsOne; m++)
		{
			if(shotsOne[m].getLifeLeft()>0)
			{
				for(int y=0; y<turretsBlue.length; y++)
				{
					if(turretsBlue[y].getDeployable()==true)
					{
						if(turretsBlue[y].getBounds().contains(shotsOne[m].getX(),shotsOne[m].getY()))
						{
							if(turretsBlue[y].getAlive()==true)
							{
								shotsOne[m].setLifeLeft(shotsOne[m].getLifeLeft()-shotsOne[m].getLifeLeft());
								turretsBlue[y].setHealth(turretsBlue[y].getHealth()-1);
								if(turretsBlue[y].getHealth()<=0)
								{
									turretsBlue[y].setAlive(false);
									System.out.println("Blue turret was shot down");
									bill.setBlueTurretExpenses(bill.getBlueTurretExpenses()+bill.getTurretCost());
									t2Deaths++;
								}


								//Draws explosion [casts double x/y of shots into usable ints]
								explosionsCalc((int)shotsOne[m].getX(), (int)shotsOne[m].getY(), 10, 1, false);

								continue;
							}
						}
					}
				}
			}
		}




	}//checkCollisions()


	//Aims sentryOne at playerTwo
	public void Target(int target)
	{
		if(target==2)
		{
			//check if playerTwo is above the sentry
			double delY, delX, targetAngle;
			targetAngle = 0;
			delX = playerTwo.getX()-sentryOne.getX();
			delY = playerTwo.getY()-sentryOne.getY();

			if(delX<0)
			targetAngle = (Math.atan(delY/delX))+Math.PI;
			else
			targetAngle =Math.atan(delY/delX);
			sentryOne.setDrawAngle(targetAngle);


			//THIS is random accuracy
			int lr = 3;
			//either 0 or 1;
			lr = rand.nextInt(2);
			if(lr == 0)
				targetAngle = targetAngle - (((rand.nextInt(5))*Math.PI)/180);
			else
				targetAngle = targetAngle + (((rand.nextInt(5))*Math.PI)/180);
				sentryOne.setAngle(targetAngle);

		}

		if(target==1)
		{
			//check if playerOne is above the sentry
			double delY, delX, targetAngle;
			targetAngle = 0;
			delX = playerOne.getX()-sentryTwo.getX();
			delY = playerOne.getY()-sentryTwo.getY();

			if(delX<0)
			targetAngle = (Math.atan(delY/delX))+Math.PI;
			else
			targetAngle =Math.atan(delY/delX);
			sentryTwo.setDrawAngle(targetAngle);


			//THIS is random accuracy
			int lr = 3;
			//either 0 or 1;
			lr = rand.nextInt(2);
			if(lr == 0)
				targetAngle = targetAngle - (((rand.nextInt(5))*Math.PI)/180);
			else
				targetAngle = targetAngle + (((rand.nextInt(5))*Math.PI)/180);
			sentryTwo.setAngle(targetAngle);

		}

	} //Closes target()


	public void destroyerOneMovement()
	{
		//Method Variables
		double targetAngle = 0;
		double destroyerAngle;
		double tarAng = 0;
		double maxRight=5000, maxLeft=5000;
		boolean turnLeft = false; //If true, it will turn left, and if false: right
		boolean going = false; //Detects if it's in the process of going twoards the wayOne
		double targetDistance=0; //The distance between destroyerOne and wayOne

		if(destroyerOne.getAccelerating()==true)
			going=true;

		//Checks if the wayOne is alive
		if(wayOne.getAlive()==true)
		{
			//Uses same targeting mechanism as the sentries
			double delY, delX;
			delX = wayOne.getX()-destroyerOne.getX();
			delY = wayOne.getY()-destroyerOne.getY();

			//Calculates the targetAngle; used to face the waypoint
			if(delX<0)
				targetAngle = (Math.atan(delY/delX))+Math.PI;
			else
				targetAngle =Math.atan(delY/delX);

			//Fixes the top right quadrant issue AKA: The magical wonderful code that saved the world
			if(delX>0&&delY<0)
				targetAngle = targetAngle+Math.PI*2;

			//Rounds the angles and does some hocus pocus stuff. (Cameron wrote this :s)
			tarAng = Math.round(targetAngle*10);
			destroyerAngle = Math.round(destroyerOne.getAngle()*10);

			//Calculates if turning left or right is most optimal
			if(maxRight == 5000)
			{
				maxRight = destroyerAngle + 31.5;
				maxLeft = destroyerAngle - 31.5;

				if(tarAng<maxRight&&tarAng>destroyerAngle)
					turnLeft = false;

				if(tarAng>maxLeft&&tarAng<destroyerAngle)
					turnLeft = true;

				if(maxRight>63)
				{
					maxRight = maxRight - 63;
					if(tarAng<maxRight)
						turnLeft = false;
				}

				if(maxLeft<0)
				{
					maxLeft = maxLeft + 63;
					if(tarAng>maxLeft)
						turnLeft = true;
				}

			} //Closes if(maxRight == 5000)

			/*Calculates the target distance using the Goddamned Pythagorean Theorem*/

			//Checks if waypoint is above the destroyer
			if(wayOne.getY()>destroyerOne.getY())
			{
				//Destroyer is to the right of waypoint
				if(destroyerOne.getX()>wayOne.getX())
					targetDistance=Math.sqrt( ((wayOne.getY()-destroyerOne.getY())*(wayOne.getY()-destroyerOne.getY())) + ((destroyerOne.getX()-wayOne.getX())*(destroyerOne.getX()-wayOne.getX())) );

				//Destroyer is to the left of waypoint
				else
					targetDistance=Math.sqrt( ((wayOne.getY()-destroyerOne.getY())*(wayOne.getY()-destroyerOne.getY())) + ((wayOne.getX()-destroyerOne.getX())*(wayOne.getX()-destroyerOne.getX())) );

			//Otherwise; the waypoint is below the destroyer
			}else{

				//Destroyer is to the right of waypoint
				if(destroyerOne.getX()>wayOne.getX())
					targetDistance=Math.sqrt( ((destroyerOne.getY()-wayOne.getY())*(destroyerOne.getY()-wayOne.getY())) + ((destroyerOne.getX()-wayOne.getX())*(destroyerOne.getX()-wayOne.getX())) );
				//Destroyer is to the left of waypoint
				else
					targetDistance=Math.sqrt( ((destroyerOne.getY()-wayOne.getY())*(destroyerOne.getY()-wayOne.getY())) + ((wayOne.getX()-destroyerOne.getX())*(wayOne.getX()-destroyerOne.getX())) );

			}

			//Detects if the destroyer is not facing the target, in which it needs to turn in place until it is
			if((destroyerOne.getAngle()!=targetAngle) && (going==false))
			{
				destroyerOne.setAccelerating(false);

				if(turnLeft == true)
					destroyerOne.setTurningLeft(true);
				else
					destroyerOne.setTurningRight(true);
			}

			//Detects if it is roughly facing the waypoint
			if(Math.round(destroyerOne.getAngle()*10)==Math.round(targetAngle*10))
			{
				//Snaps the destroyer to face the waypoint when close to avoid bugs
				destroyerOne.setAngle(targetAngle);

				//Stops the turning
				destroyerOne.setTurningLeft(false);
				destroyerOne.setTurningRight(false);

				//"At 5 pixels, it has gotten close enough...whatever...it will pass as acceptable" -Cameron 'Cheater-Pants' Ford
				if(targetDistance>5)
					going=true;
				if(targetDistance<5)
					going=false;

				if(going == true)
					destroyerOne.setAccelerating(true);
				if(going == false)
				{
					destroyerOne.setAccelerating(false);
					wayOne.setAlive(false);
				}

			}

		}else{ //Stops the destroyer if there exists no waypoint
			destroyerOne.setAccelerating(false);
			destroyerOne.setTurningLeft(false);
			destroyerOne.setTurningRight(false);
		}

	} //Closes destroyerOneMovement()

	public void destroyerTwoMovement()
	{
		//Method Variables
		double targetAngle = 0;
		double destroyerAngle;
		double tarAng = 0;
		double maxRight=5000, maxLeft=5000;
		boolean turnLeft = false; //If true, it will turn left, and if false: right
		boolean going = false; //Detects if it's in the process of going twoards the wayTwo
		double targetDistance=0; //The distance between destroyerTwo and wayTwo

		if(destroyerTwo.getAccelerating()==true)
			going=true;

		//Checks if the wayTwo is alive
		if(wayTwo.getAlive()==true)
		{
			//Uses same targeting mechanism as the sentries
			double delY, delX;
			delX = wayTwo.getX()-destroyerTwo.getX();
			delY = wayTwo.getY()-destroyerTwo.getY();

			//Calculates the targetAngle; used to face the waypoint
			if(delX<0)
				targetAngle = (Math.atan(delY/delX))+Math.PI;
			else
				targetAngle =Math.atan(delY/delX);

			//Fixes the top right quadrant issue AKA: The magical wonderful code that saved the world
			if(delX>0&&delY<0)
				targetAngle = targetAngle+Math.PI*2;

			//Rounds the angles and does some hocus pocus stuff. (Cameron wrote this :s)
			tarAng = Math.round(targetAngle*10);
			destroyerAngle = Math.round(destroyerTwo.getAngle()*10);

			//Calculates if turning left or right is most optimal
			if(maxRight == 5000)
			{
				maxRight = destroyerAngle + 31.5;
				maxLeft = destroyerAngle - 31.5;

				if(tarAng<maxRight&&tarAng>destroyerAngle)
					turnLeft = false;

				if(tarAng>maxLeft&&tarAng<destroyerAngle)
					turnLeft = true;

				if(maxRight>63)
				{
					maxRight = maxRight - 63;
					if(tarAng<maxRight)
						turnLeft = false;
				}

				if(maxLeft<0)
				{
					maxLeft = maxLeft + 63;
					if(tarAng>maxLeft)
						turnLeft = true;
				}

			} //Closes if(maxRight == 5000)

			/*Calculates the target distance using the Goddamned Pythagorean Theorem*/

			//Checks if waypoint is above the destroyer
			if(wayTwo.getY()>destroyerTwo.getY())
			{
				//Destroyer is to the right of waypoint
				if(destroyerTwo.getX()>wayTwo.getX()){
					targetDistance=Math.sqrt( ((wayTwo.getY()-destroyerTwo.getY())*(wayTwo.getY()-destroyerTwo.getY())) + ((destroyerTwo.getX()-wayTwo.getX())*(destroyerTwo.getX()-wayTwo.getX())) );
				//Destroyer is to the left of waypoint
				}else{
					targetDistance=Math.sqrt( ((wayTwo.getY()-destroyerTwo.getY())*(wayTwo.getY()-destroyerTwo.getY())) + ((wayTwo.getX()-destroyerTwo.getX())*(wayTwo.getX()-destroyerTwo.getX())) );
				}
			//Otherwise; the waypoint is below the destroyer
			}else{

				//Destroyer is to the right of waypoint
				if(destroyerTwo.getX()>wayTwo.getX()){
					targetDistance=Math.sqrt( ((destroyerTwo.getY()-wayTwo.getY())*(destroyerTwo.getY()-wayTwo.getY())) + ((destroyerTwo.getX()-wayTwo.getX())*(destroyerTwo.getX()-wayTwo.getX())) );
				//Destroyer is to the left of waypoint
				}else{
					targetDistance=Math.sqrt( ((destroyerTwo.getY()-wayTwo.getY())*(destroyerTwo.getY()-wayTwo.getY())) + ((wayTwo.getX()-destroyerTwo.getX())*(wayTwo.getX()-destroyerTwo.getX())) );
				}
			}

			//Detects if the destroyer is not facing the target, in which it needs to turn in place until it is
			if((destroyerTwo.getAngle()!=targetAngle) && (going==false))
			{
				destroyerTwo.setAccelerating(false);

				if(turnLeft == true)
					destroyerTwo.setTurningLeft(true);
				else
					destroyerTwo.setTurningRight(true);
			}

			//Detects if it is roughly facing the waypoint
			if(Math.round(destroyerTwo.getAngle()*10)==Math.round(targetAngle*10))
			{
				//Snaps the destroyer to face the waypoint when close to avoid bugs
				destroyerTwo.setAngle(targetAngle);

				//Stops the turning
				destroyerTwo.setTurningLeft(false);
				destroyerTwo.setTurningRight(false);

				//"At 5 pixels, it has gotten close enough...whatever...it will pass as acceptable" -Cameron 'Cheater-Pants' Ford
				if(targetDistance>5)
					going=true;
				if(targetDistance<5)
					going=false;

				if(going == true)
					destroyerTwo.setAccelerating(true);
				if(going == false)
				{
					destroyerTwo.setAccelerating(false);
					wayTwo.setAlive(false);
				}

			}

		}else{ //Stops the destroyer if there exists no waypoint
			destroyerTwo.setAccelerating(false);
			destroyerTwo.setTurningLeft(false);
			destroyerTwo.setTurningRight(false);
		}

	} //Closes destroyerTwoMovement()


	public void turretTargetRed()
	{
		if(baseOne.getAlive()==true && baseTwo.getAlive()==true)
		{
			//Traverses the turret array
			for(int y=0; y<turretsRed.length; y++)
			{

				int currentTarget = 0;//This will be used to differentuate between what the turret is actually shooting at, since it can only shoot at one thing at a time

						/*
						 *---Target Codes---
						 *0 no current target
						 *1 blue player ship
						 *2 blue base
						 *3 blue destroyer
						 *4 turretsBlue[4]
						 */

						 //CURRENT BUG
						 //it has priorities: player is top priority, then base, then destroyer

				if(turretsRed[y].getAlive()==true && turretsRed[y].getActive()==true)
				{
					boolean youMayFireWhenReady = true; //Whether something is within the range or not

					if(turretsRed[y].getRangeBounds().contains(playerTwo.getX(),playerTwo.getY()))
						youMayFireWhenReady = true;
					if(turretsRed[y].getRangeBounds().intersects(baseTwo.getBoundsOuter()))
						youMayFireWhenReady = true;
					if(turretsRed[y].getRangeBounds().contains(destroyerTwo.getX(), destroyerTwo.getY()))
						youMayFireWhenReady = true;
					if(turretsRed[y].getRangeBounds().intersects(turretsBlue[4].getBounds()))
						youMayFireWhenReady = true;
					else
					{
						youMayFireWhenReady = false;
						currentTarget = 0;
					}

					if(currentTarget == 0)
					{
						if(turretsRed[y].getRangeBounds().contains(playerTwo.getX(),playerTwo.getY()) && playerTwo.getStun()==false)
							currentTarget = 1;
						else if(turretsRed[y].getRangeBounds().intersects(baseTwo.getBoundsOuter())&&currentTarget==0)
							currentTarget = 2;
						else if(turretsRed[y].getRangeBounds().contains(destroyerTwo.getX(), destroyerTwo.getY())&&currentTarget==0)
							currentTarget = 3;
						else if(turretsRed[y].getRangeBounds().intersects(turretsBlue[4].getBounds())&&currentTarget==0)
							currentTarget = 4;
						else
							currentTarget = 0;
					}

					switch(currentTarget)
					{
						case 0:
						{
							turretsRed[y].setActivelyShooting(false);
						}
						break;

						case 1:
						{
							double delY, delX, targetAngle;
							targetAngle = 0;

							turretsRed[y].setActivelyShooting(true);

							//Relativity of playerTwo to the turret
							delX = playerTwo.getX()-turretsRed[y].getX();
							delY = playerTwo.getY()-turretsRed[y].getY();

							if(delX<0)
								targetAngle = (Math.atan(delY/delX))+Math.PI;
							else
								targetAngle =Math.atan(delY/delX);
								turretsRed[y].setDrawAngle(targetAngle);

							//Assigns lr (left / right) one of two values to use in random accuracy
							int lr = 3;
							lr = rand.nextInt(2);
							if(lr == 0)
								targetAngle = targetAngle - (((rand.nextInt(turretsRed[y].getAccuracy()))*Math.PI)/180);
							else
								targetAngle = targetAngle + (((rand.nextInt(turretsRed[y].getAccuracy()))*Math.PI)/180);
								turretsRed[y].setAngle(targetAngle);
						}
						break;

						case 2:
						{
							double delY, delX, targetAngle;
							targetAngle = 0;

							turretsRed[y].setActivelyShooting(true);

							//Hard coded base targeting locations
							delX = 975 - turretsRed[y].getX();
							delY = 300 - turretsRed[y].getY();

							if(delX<0)
								targetAngle = (Math.atan(delY/delX))+Math.PI;
							else
								targetAngle =Math.atan(delY/delX);
								turretsRed[y].setDrawAngle(targetAngle);

							//Assigns variables lr (left / right) one of two values to use for random accuracy
							int lr = 3;
							lr = rand.nextInt(2);

							if(lr == 0)
								targetAngle = targetAngle - (((rand.nextInt(turretsRed[y].getAccuracy()))*Math.PI)/180);
							else
								targetAngle = targetAngle + (((rand.nextInt(turretsRed[y].getAccuracy()))*Math.PI)/180);
								turretsRed[y].setAngle(targetAngle);
						}
						break;

						case 3:
						{
							double delY, delX, targetAngle;
							targetAngle = 0;

							turretsRed[y].setActivelyShooting(true);

							//Relativity of playerTwo to the turret
							delX = destroyerTwo.getX()-turretsRed[y].getX();
							delY = destroyerTwo.getY()-turretsRed[y].getY();

							if(delX<0)
								targetAngle = (Math.atan(delY/delX))+Math.PI;
							else
								targetAngle =Math.atan(delY/delX);
								turretsRed[y].setDrawAngle(targetAngle);

							//Assigns integer lr (left / right) one of two values used for random accuracy
							int lr = 3;
							lr = rand.nextInt(2);

							if(lr == 0)
								targetAngle = targetAngle - (((rand.nextInt(turretsRed[y].getAccuracy()))*Math.PI)/180);
							else
								targetAngle = targetAngle + (((rand.nextInt(turretsRed[y].getAccuracy()))*Math.PI)/180);
								turretsRed[y].setAngle(targetAngle);
						}
						break;

						case 4:
							{
								if(turretsBlue[4].getAlive()==true)
								{
									double delY, delX, targetAngle;
									targetAngle = 0;

									turretsRed[y].setActivelyShooting(true);

									//Relativity of playerTwo to the turret
									delX = turretsBlue[4].getX()-turretsRed[y].getX();
									delY = turretsBlue[4].getY()-turretsRed[y].getY();

									if(delX<0)
										targetAngle = (Math.atan(delY/delX))+Math.PI;
									else
										targetAngle =Math.atan(delY/delX);
										turretsRed[y].setDrawAngle(targetAngle);

									//Assigns integer lr (left / right) one of two values used for random accuracy
									int lr = 3;
									lr = rand.nextInt(2);

									if(lr == 0)
										targetAngle = targetAngle - (((rand.nextInt(turretsRed[y].getAccuracy()))*Math.PI)/180);
									else
										targetAngle = targetAngle + (((rand.nextInt(turretsRed[y].getAccuracy()))*Math.PI)/180);
										turretsRed[y].setAngle(targetAngle);
								}else
									turretsRed[y].setActivelyShooting(false);
								break;
							}

					} //Closes switch statement

				} //Closes if statement for if the turret is alive and active

			} //Closes for loop for array traversing

		}

	} //Closes turretTargetRed


	public void turretTargetBlue()
	{
		if(baseOne.getAlive()==true && baseTwo.getAlive()==true)
		{
			//Traverses the turret array
			for(int y=0; y<turretsBlue.length; y++)
			{

			int currentTarget = 0;

						/*
						 *---Target Codes---
						 *0 no current target
						 *1 red player ship
						 *2 red base
						 *3 red destroyer
						 *4 turretsRed[4]
						 */

				if(turretsBlue[y].getAlive()==true&&turretsBlue[y].getActive()==true)
				{
					boolean youMayFireWhenReady = true; //Unused boolean just to make the IF statements happy

					if(turretsBlue[y].getRangeBounds().contains(playerOne.getX(),playerOne.getY()))
						youMayFireWhenReady = true;
					if(turretsBlue[y].getRangeBounds().intersects(baseOne.getBoundsOuter()))
						youMayFireWhenReady = true;
					if(turretsBlue[y].getRangeBounds().contains(destroyerOne.getX(), destroyerOne.getY()))
						youMayFireWhenReady = true;
					if(turretsBlue[y].getRangeBounds().intersects(turretsRed[4].getBounds()))
						youMayFireWhenReady = true;
					else
					{
						youMayFireWhenReady = false;
						currentTarget = 0;
					}

					if(currentTarget == 0)
					{
						if(turretsBlue[y].getRangeBounds().contains(playerOne.getX(),playerOne.getY()) && playerOne.getStun()==false)
							currentTarget = 1;
						else if(turretsBlue[y].getRangeBounds().intersects(baseOne.getBoundsOuter())&&currentTarget==0)
							currentTarget = 2;
						else if(turretsBlue[y].getRangeBounds().contains(destroyerOne.getX(), destroyerOne.getY())&&currentTarget==0)
							currentTarget = 3;
						else if(turretsBlue[y].getRangeBounds().intersects(turretsRed[4].getBounds()))
							currentTarget = 4;
						else
							currentTarget = 0;
					}

					switch(currentTarget)
					{
						//No target
						case 0:
						{
							turretsBlue[y].setActivelyShooting(false);
						}
						break;

						//Red player ship
						case 1:
						{
							double delY, delX, targetAngle;
							targetAngle = 0;

							turretsBlue[y].setActivelyShooting(true);

							//Relativity of playerOne to the turret
							delX = playerOne.getX()-turretsBlue[y].getX();
							delY = playerOne.getY()-turretsBlue[y].getY();

							if(delX<0)
								targetAngle = (Math.atan(delY/delX))+Math.PI;
							else
								targetAngle =Math.atan(delY/delX);
								turretsBlue[y].setDrawAngle(targetAngle);

							//Assigns lr (left / right) Two of two values to use in random accuracy
							int lr = 3;
							lr = rand.nextInt(2);
							if(lr == 0)
								targetAngle = targetAngle - (((rand.nextInt(turretsBlue[y].getAccuracy()))*Math.PI)/180);
							else
								targetAngle = targetAngle + (((rand.nextInt(turretsBlue[y].getAccuracy()))*Math.PI)/180);
								turretsBlue[y].setAngle(targetAngle);
						}
						break;

						case 2:
						{
							double delY, delX, targetAngle;
							targetAngle = 0;

							turretsBlue[y].setActivelyShooting(true);

							//Hard coded base targeting locations
							delX = 25 - turretsBlue[y].getX();
							delY = 300 - turretsBlue[y].getY();

							if(delX<0)
								targetAngle = (Math.atan(delY/delX))+Math.PI;
							else
								targetAngle =Math.atan(delY/delX);
								turretsBlue[y].setDrawAngle(targetAngle);

							//Assigns variables lr (left / right) Two of two values to use for random accuracy
							int lr = 3;
							lr = rand.nextInt(2);

							if(lr == 0)
								targetAngle = targetAngle - (((rand.nextInt(turretsBlue[y].getAccuracy()))*Math.PI)/180);
							else
								targetAngle = targetAngle + (((rand.nextInt(turretsBlue[y].getAccuracy()))*Math.PI)/180);
								turretsBlue[y].setAngle(targetAngle);
						}
						break;

						case 3:
						{
							double delY, delX, targetAngle;
							targetAngle = 0;

							turretsBlue[y].setActivelyShooting(true);

							//Relativity of playerOne to the turret
							delX = destroyerOne.getX()-turretsBlue[y].getX();
							delY = destroyerOne.getY()-turretsBlue[y].getY();

							if(delX<0)
								targetAngle = (Math.atan(delY/delX))+Math.PI;
							else
								targetAngle =Math.atan(delY/delX);
								turretsBlue[y].setDrawAngle(targetAngle);

							//Assigns integer lr (left / right) Two of two values used for random accuracy
							int lr = 3;
							lr = rand.nextInt(2);

							if(lr == 0)
								targetAngle = targetAngle - (((rand.nextInt(turretsBlue[y].getAccuracy()))*Math.PI)/180);
							else
								targetAngle = targetAngle + (((rand.nextInt(turretsBlue[y].getAccuracy()))*Math.PI)/180);
								turretsBlue[y].setAngle(targetAngle);
						}
						break;

						case 4:
						{
							if(turretsRed[4].getAlive()==true)
							{
								double delY, delX, targetAngle;
								targetAngle = 0;

								turretsBlue[y].setActivelyShooting(true);

								//Relativity of playerOne to the turret
								delX = turretsRed[4].getX()-turretsBlue[y].getX();
								delY = turretsRed[4].getY()-turretsBlue[y].getY();

								if(delX<0)
									targetAngle = (Math.atan(delY/delX))+Math.PI;
								else
									targetAngle =Math.atan(delY/delX);
									turretsBlue[y].setDrawAngle(targetAngle);

								//Assigns integer lr (left / right) Two of two values used for random accuracy
								int lr = 3;
								lr = rand.nextInt(2);

								if(lr == 0)
									targetAngle = targetAngle - (((rand.nextInt(turretsBlue[y].getAccuracy()))*Math.PI)/180);
								else
									targetAngle = targetAngle + (((rand.nextInt(turretsBlue[y].getAccuracy()))*Math.PI)/180);
									turretsBlue[y].setAngle(targetAngle);
							}else
								turretsBlue[y].setActivelyShooting(false);
						}

						break;

					} //Closes switch statement

				} //Closes if statement for if the turret is alive and active

			} //Closes for loop for array traversing

		}

	} //Closes turretTargetBlue


	//Handles explosion calculations for explosions[]
	public void explosionsCalc(int x, int y, int radius, int playerID, boolean multi)
	{
		//false is a standard, single explosion
		if(multi == false)
		{
			//Only activates one explosion in the array
			if(explosionDrawn == false)
			{
				for(int z=0; z<explosions.length; z++)
				{
					if(explosions[z].getAlive()==false)
					{
						explosions[z] = new Explosion(x, y, radius, playerID);
						explosions[z].setAlive(true);
						explosionDrawn = true;
					}
				}
			}
			//Resets explosionDrawn to prepare for next method call
			explosionDrawn = false;

		//true is practically a firework show
		}else if(multi == true)
		{
			long g = 0;
			g = currentTime + 100;

			while(currentTime>g)
			{
				//Only activates one explosion in the array at a time
				if(explosionDrawn == false)
				{
					for(int z=0; z<explosions.length; z++)
					{
						if(explosions[z].getAlive()==false)
						{
							int w = rand.nextInt(radius);
							int j = rand.nextInt(2);
							if(j==0)
							{
								if(w%2==0)
									explosions[z] = new Explosion(x+w, y+w, 10, playerID);
								else if(w%2!=0)
									explosions[z] = new Explosion(x-w, y-w, 10, playerID);
							}else if(j==1)
							{
								if(w%2==0)
									explosions[z] = new Explosion(x-w, y+w, 10, playerID);
								else if(w%2!=0)
									explosions[z] = new Explosion(x-w, y+w, 10, playerID);
							}

							explosions[z].setAlive(true);
							explosionDrawn = true;
						}
					}
				}
			}
		}
	}


	//Handles star calculations for stars[]
	public void starsCalc()
	{
		for(int r=0; r<stars.length; r++)
		{


			//If the 50x50 rectangle does not contain a star, then one is randomly placed inside the rectangle
			Random plusOrMinusGenX = new Random();
			int plusOrMinusX = plusOrMinusGenX.nextInt(2);

			Random plusOrMinusGenY = new Random();
			int plusOrMinusY = plusOrMinusGenY.nextInt(2);

			Random xChangeGen = new Random();
			int xChange = xChangeGen.nextInt(201);

			Random yChangeGen = new Random();
			int yChange = yChangeGen.nextInt(201);

			if(plusOrMinusX == 1)
			{
				xChange = xChange - (xChange * 2); //Turns into negative integer
				xChange -= 201;
			}else{
				xChange += 201;
			}

			if(plusOrMinusY ==1)
			{
				yChange = yChange - (yChange * 2); //Turns into negative integer
				yChange -= 201;
			}else{
				yChange += 201;
			}

			starsX = (xCheck + xChange);
			starsY = (yCheck + yChange);

			stars[r] = new Star(starsX, starsY);
			stars[r].setAlive(true);

			xCheck += 150; //Lower this number for denser stars!

			if(xCheck > 1150) //Hardcoded screen width
			{
				xCheck = -150; //Lower this number for denser stars!
				yCheck += 150; //Lower this number for denser stars!
			}

		}

	} //Closes starsCalc()


	public void respawnOne()
	{

		//Initiates over all timer for respawn time
		if(respawnTimeOne == 0)
			respawnTimeOne = currentTime + 5000;

		//If there is still time before respawning
		if(currentTime < respawnTimeOne)
		{

			playerOne.setStun(true);
			playerOne.setAngle(playerOne.getAngle());

			//Initiates timer to reduce speed over brief time period
			if(accDeclineOne == 0)
				accDeclineOne = currentTime + 250;

			if(currentTime > accDeclineOne && playerOne.getAcceleration() > .2)
			{
				playerOne.reduceAccleration(.2);
				accDeclineOne = 0;
				playerOne.setAmmo(0);
			}

		}

		if(baseOne.getAlive()==true)
		{
			//When the respawn delay has passed
			if(currentTime > respawnTimeOne)
			{
				explosionsCalc((int)playerOne.getX(), (int)playerOne.getY(), 10, 1, false);
				playerOne.setHealth(playerOne.getDefaultHealth());
				playerOne.setX(15);
				playerOne.setY(300);
				playerOne.setAngle(0);
				playerOne.setAcceleration(playerOne.getDefaultAcceleration());
				playerOne.setAccelerating(false);
				playerOne.setStun(false);
				playerOne.setTurningRight(false);
				playerOne.setTurningLeft(false);
				playerOneShooting = false;
				respawnTimeOne = 0;

			}
	}

	} //Closes respawnOne()


	public void respawnTwo()
	{

		//Initiates over all timer for respawn time
		if(respawnTimeTwo == 0)
			respawnTimeTwo = currentTime + 5000;


		//If there is still time before respawning
		if(currentTime < respawnTimeTwo)
		{

			playerTwo.setStun(true);
			playerTwo.setAngle(playerTwo.getAngle());
			playerTwo.setAmmo(0);

			//Initiates timer to reduce speed over brief time period
			if(accDeclineTwo == 0)
				accDeclineTwo = currentTime + 250;

			if(currentTime > accDeclineTwo && playerTwo.getAcceleration() > .2)
			{
				playerTwo.reduceAccleration(.2);
				accDeclineTwo = 0;
			}

		}
		if(baseTwo.getAlive()==true)
		{
			//When the respawn delay has passed
			if(currentTime > respawnTimeTwo)
			{
				explosionsCalc((int)playerTwo.getX(), (int)playerTwo.getY(), 10, 2, false);
				playerTwo.setHealth(playerTwo.getDefaultHealth());
				playerTwo.setX(985);
				playerTwo.setY(300);
				playerTwo.setAngle(3.1459);
				playerTwo.setAcceleration(playerTwo.getDefaultAcceleration());
				playerTwo.setAccelerating(false);
				playerTwo.setStun(false);
				playerTwo.setTurningRight(false);
				playerTwo.setTurningLeft(false);
				playerTwoShooting = false;
				respawnTimeTwo = 0;
			}
		}

	} //Closes respawnTwo()

	public void destroyerOneRespawn()
	{
		//Timer for respawn
		if(destroyerOneRespawn == 0)
		{
			destroyerOneRespawn = currentTime + 25000;

			explosionsCalc((int)destroyerOne.getX(), (int)destroyerOne.getY(), 80, 1, false);
			System.out.println("Red destroyer has been terminated");
			bill.setRedDestroyerExpenses(bill.getRedDestroyerExpenses()+bill.getDestroyerCost());
			d1Deaths++;
			destroyerOne.setX(-50);
			destroyerOne.setY(300);
			destroyerOne.setAlive(false);
			turretsRed[0].setActive(false);
			turretsRed[1].setActive(false);
			wayOne.setAlive(false);
		}

		if(currentTime > destroyerOneRespawn && destroyerOneRespawn != 0)
		{
			destroyerOne.setAlive(true);
			destroyerTwo.setActive(true);
			destroyerOne.setHealth(destroyerOne.getDefaultHealth());
			destroyerOne.setX(-50);
			destroyerOne.setY(300);
			destroyerOne.setAngle(0);
			wayOne.setAlive(false);
			turretsRed[0].setActive(true);
			turretsRed[1].setActive(true);
			destroyerOneRespawn = 0;
			System.out.println("Red destroyer is ready");
		}

	} //Closes destroyerOneRespawn()


	public void destroyerTwoRespawn()
	{
		//Timer for respawn
		if(destroyerTwoRespawn == 0)
		{
			destroyerTwoRespawn = currentTime + 25000;

			explosionsCalc((int)destroyerTwo.getX(), (int)destroyerTwo.getY(), 80, 2, false);
			System.out.println("Blue destroyer has been terminated");
			bill.setBlueDestroyerExpenses(bill.getBlueDestroyerExpenses()+bill.getDestroyerCost());
			d2Deaths++;
			destroyerTwo.setX(1050);
			destroyerTwo.setY(300);
			destroyerTwo.setAlive(false);
			turretsBlue[0].setActive(false);
			turretsBlue[1].setActive(false);
			wayTwo.setAlive(false);
		}

		if(currentTime > destroyerTwoRespawn)
		{
			destroyerTwo.setAlive(true);
			destroyerTwo.setActive(true);
			destroyerTwo.setHealth(destroyerTwo.getDefaultHealth());
			destroyerTwo.setX(1050);
			destroyerTwo.setY(300);
			destroyerTwo.setAngle(Math.PI);
			wayTwo.setAlive(false);
			turretsBlue[0].setActive(true);
			turretsBlue[1].setActive(true);
			destroyerTwoRespawn = 0;
			System.out.println("Blue destroyer is ready");
		}
	} //Closes destroyerTwoRespawn()


	//Detects when a player is within the proximity of a mine
	public void minesActivation()
	{
		//Checks P1's mines first
		if(minesOne.getArmed()==true)
		{
			//Checks if player two is within the activation range
			if(distance(minesOne.getX(), minesOne.getY(), (int)playerTwo.getX(), (int)playerTwo.getY()) < minesOne.getDetRadius())
			{
				minesOne.setActivated(true);

				//Begins 1 second timer; following which is detonation!
				if(minesOneDetTime == 0)
					minesOneDetTime = currentTime + 1000;

				if(minesOneDetTime < currentTime)
				{
					minesDetonation(1);
					minesOne.setAlive(false);
					minesOne.setActivated(false);
					minesOneDetTime = 0;
				}
			}else{
				//Resets det timer if ship moves out of det range
				minesOneDetTime = 0;
				minesOne.setActivated(false);
			}
		}

		//Checks P2's mines second
		if(minesTwo.getArmed()==true)
		{
			//Checks if player two is within the activation range
			if(distance(minesTwo.getX(), minesTwo.getY(), (int)playerOne.getX(), (int)playerOne.getY()) < minesTwo.getDetRadius())
			{
				minesTwo.setActivated(true);

				//Begins 1 second timer; following which is detonation!
				if(minesTwoDetTime == 0)
					minesTwoDetTime = currentTime + 1000;

				if(minesTwoDetTime < currentTime)
				{
					minesDetonation(2);
					minesTwo.setAlive(false);
					minesTwo.setActivated(false);
					minesTwoDetTime = 0;
				}
			}else{
				//Resets det timer if ship moves out of det range
				minesTwoDetTime = 0;
				minesTwo.setActivated(false);
			}
		}


	} //Closes minesActivation()


	//Calculates damage from a mine detonation
	public void minesDetonation(int playerID)
	{
		//If called for a P1 mine explosion...
		if(playerID == 1)
		{
			//If P2 is inside the expRadius (AKA: The damage zone)
			if(distance(minesOne.getX(), minesOne.getY(), (int)playerTwo.getX(), (int)playerTwo.getY()) < minesOne.getExpRadius())
			{
				playerTwo.addHealth(-3);
				bill.setBlueMineExpenses(bill.getBlueMineExpenses()+bill.getMineCost());
				explosionsCalc(minesOne.getX(), minesOne.getY(), minesOne.getExpRadius(), playerID, false);
				minesOne.setAllowed(true);

				if(playerTwo.getHealth()<=0)
				{
					System.out.println("Player 2 was blown up");
					bill.setBlueShipExpenses(bill.getBlueShipExpenses()+bill.getShipCost());
					p2Deaths++;
				}
			}
		}

		//If called for a P2 mine explosion...
		if(playerID == 2)
		{
			//If P1 is inside the expRadius (AKA: The damage zone)
			if(distance(minesTwo.getX(), minesTwo.getY(), (int)playerOne.getX(), (int)playerOne.getY()) < minesTwo.getExpRadius())
			{
				playerOne.addHealth(-3);
				bill.setRedMineExpenses(bill.getRedMineExpenses()+bill.getMineCost());
				explosionsCalc(minesTwo.getX(), minesTwo.getY(), minesTwo.getExpRadius(), playerID, false);
				minesTwo.setAllowed(true);

				if(playerOne.getHealth()<=0)
				{
					System.out.println("Player 1 was blown up");
					bill.setRedShipExpenses(bill.getRedShipExpenses()+bill.getShipCost());
					p1Deaths++;
				}
			}
		}

	} //Closes minesDetonation


	//Sets colors of mines indicator lights and controls blinking when activated
	public void mineLight()
	{
		//Sets the light color to green if mine is in armed state
		if(minesOne.getArmed()==true && minesOne.getActivated()==false)
			minesOne.setColor("green");
		if(minesTwo.getArmed()==true && minesTwo.getActivated()==false)
			minesTwo.setColor("green");

		//Sets the light color to red if they are not armed
		if(minesOne.getArmed()==false && minesOne.getActivated()==false)
			minesOne.setColor("red");
		if(minesTwo.getArmed()==false && minesTwo.getActivated()==false)
			minesTwo.setColor("red");

		//Flashes indicator lights if the mines are activated (Player One)
		if(minesOne.getActivated()==true)
		{
			//Begins timer to flash light
			if(minesOneFlash==0)
				minesOneFlash = currentTime + 100;

			//Alternates color
			if(minesOneFlash < currentTime)
			{
				minesOne.setColor("green");
			}

			if(minesOneFlash+100 < currentTime)
			{
				minesOne.setColor("gray");
				minesOneFlash = 0;
			}
		}

		//Flashes indicator lights if the mines are activated (Player Two)
		if(minesTwo.getActivated()==true)
		{
			//Begins timer to flash light
			if(minesTwoFlash==0)
				minesTwoFlash = currentTime + 100;

			//Alternates color
			if(minesTwoFlash < currentTime)
			{
				minesTwo.setColor("green");
			}

			if(minesTwoFlash+100 < currentTime)
			{
				minesTwo.setColor("gray");
				minesTwoFlash = 0;
			}
		}

	} //Closes mineLight()

	public void minesOneRegen()
	{
		//Begins timer to restrict mine location spamming
		if(minesOneCooldown == 0)
			minesOneCooldown = currentTime + 5000;

		/*if(minesOneCooldown < currentTime)*/
		if(currentTime > minesOneCooldown)
		{
			minesOne.setAllowed(true);
			minesOneCooldown = 0;
			beginMinesOneRegen=false;
		}
	}


	public void minesTwoRegen()
	{
		//Begins timer to restrict mine location spamming
		if(minesTwoCooldown == 0)
			minesTwoCooldown = currentTime + 5000;

		/*if(minesTwoCooldown < currentTime)*/
		if(currentTime > minesTwoCooldown)
		{
			minesTwo.setAllowed(true);
			minesTwoCooldown = 0;
			beginMinesTwoRegen=false;
		}
	}


	//Distance formula method (rounds to integer)
	public double distance(int x1, int y1, int x2, int y2)
	{
		double doubleDistance = Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
		int intDistance = (int)doubleDistance;
		return intDistance;
	}


	//Manages newsQueue arraylist
	public void updateNews(String context)
	{
		newsQueue.add(new News(0, 0, false, context));
	}


	//Manages the printing ('ticking') of news across the notification bar
	public void printNews()
	{
		if(newsQueue.size()!=0 && newsQueue.get(0).getAlive()==false)
		{
			newsQueue.get(0).setX(300);
			newsQueue.get(0).setY(500);
			newsQueue.get(0).setAlive(true);
		}
		if(newsQueue.size()!=0 && newsQueue.get(0).getAlive()==true)
		{
			newsQueue.get(0).move();

			if(newsQueue.get(0).getX() > 1010)
			{
				newsQueue.remove(0);
			}
		}

	} //Closes printNews()


} //Closes Asteroids.java