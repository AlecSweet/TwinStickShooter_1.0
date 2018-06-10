package com.example.sweet.game20;

import com.example.sweet.game20.Objects.Asteroid;
import com.example.sweet.game20.Objects.Enemy;
import com.example.sweet.game20.Objects.Player;
import com.example.sweet.game20.util.CollisionThread;
import com.example.sweet.game20.util.Constants;
import com.example.sweet.game20.util.EnemyFactory;

import static com.example.sweet.game20.util.Constants.EnemyType.*;

import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Sweet on 6/8/2018.
 */

public class LevelControllerThread implements Runnable
{
    public Player player1;

    //public Enemy[][] entityLists;

    //private AIThread aiRunnable;

    //private CollisionThread collisionRunnable;

    //private Enemy[] uiEntities;

    public ConcurrentLinkedQueue<Enemy> enemiesToAdd = new ConcurrentLinkedQueue<>();
    private EnemyFactory enemyFactory;

    private Stack<Integer> openEntityIndices = new Stack<>();

    public volatile boolean running = true;

    private double
            lastSpawn,
            spawnDelay = 2000;

    //public LevelControllerThread(Enemy[][] e, EnemyFactory eF)
    public LevelControllerThread(EnemyFactory eF)
    {
        //entityLists = e;
        //aiRunnable = a;
        //collisionRunnable = c;
        //uiEntities = e;

        enemyFactory = eF;
        for(int i = 0; i < Constants.ENTITIES_LENGTH; i++)
        {
            openEntityIndices.push(i);
        }
    }

    public void run()
    {
        while(running)
        {
            levelController();
        }
    }

    public void levelController()
    {
        /*if(false == true)
        {
            for(int i = 0; i < difficulty; i++)
            {
                Simple s = new Simple(
                        simple.clone(),
                        new BasicGun(whiteTexture, shaderProgram, particleShaderProgram, context, enemyParticles),
                        enemyParticles,
                        0);
                s.setLoc((float) (Math.random() * 2 - 1), (float) (Math.random() * 2 - 1));
                Simple s2 = new Simple(
                        simple.clone(),
                        new BasicGun(whiteTexture, shaderProgram, particleShaderProgram, context, enemyParticles),
                        enemyParticles,
                        0);
                s2.setLoc((float) (Math.random() * 2 - 1), (float) (Math.random() * 2 - 1));
            }
        }*/

        if(System.currentTimeMillis() - spawnDelay > lastSpawn)
        {
            lastSpawn = System.currentTimeMillis();
            System.out.println("Spawned");
            Asteroid a = null;

            switch((int)(Math.random()*5.9))
            {
                case 0: a = (Asteroid)enemyFactory.getNewEnemy(ASTEROID_GREY_MEDIUM); break;
                case 1: a = (Asteroid)enemyFactory.getNewEnemy(ASTEROID_GREY_SMALL); break;
                case 2: a = (Asteroid)enemyFactory.getNewEnemy(ASTEROID_GREY_TINY); break;
                case 3: a = (Asteroid)enemyFactory.getNewEnemy(ASTEROID_RED_SMALL); break;
                case 4: a = (Asteroid)enemyFactory.getNewEnemy(ASTEROID_RED_SMALL); break;
                case 5: a = (Asteroid)enemyFactory.getNewEnemy(ASTEROID_RED_MEDIUM); break;
            }
            //distributeEnemy(a);
            //int index = openEntityIndices.pop();
            //aiRunnable.entities[index] = a;
            //collisionRunnable.entities[index] = a;
            enemiesToAdd.add(a);
            //uiEntities[index] = a;
        }
    }

    /*public void distributeEnemy(Enemy e)
    {
        if(!openEntityIndices.isEmpty())
        {
            int index = openEntityIndices.pop();
            for (int r = 0; r < entityLists.length; r++)
            {
                entityLists[r][index] = e;
            }
        }
        else
        {
            enemyOverflow.add(e);
        }
    }*/

    public void setPlayer(Player p)
    {
        player1 = p;
    }
}