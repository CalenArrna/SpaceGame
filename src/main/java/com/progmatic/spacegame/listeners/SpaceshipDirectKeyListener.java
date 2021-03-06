/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.progmatic.spacegame.listeners;

import com.progmatic.spacegame.MainGameFrame;
import com.progmatic.spacegame.spaceobjects.Spaceship;
import com.progmatic.spacegame.spaceobjects.projectile.Projectile;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Timer;

/**
 *
 * @author peti
 */
public class SpaceshipDirectKeyListener implements KeyListener {

    

    private enum ListenerState {
        in_game, to_nextlevel, game_over, game_complete;
    }

    private final Spaceship spaceship;
    private int move = 20;
    private final Set<Integer> pressedNavKeys = new HashSet<>();
    private static final Set<Integer> arrowKeys = new HashSet<>();
    private Dimension mainFrameDimensions;
    private final MainGameFrame gameFrame;
    private ListenerState myState = ListenerState.in_game;

    private boolean goAround = false;

    Timer spMoveTimer;

    static {
        arrowKeys.add(KeyEvent.VK_UP);
        arrowKeys.add(KeyEvent.VK_DOWN);
        arrowKeys.add(KeyEvent.VK_LEFT);
        arrowKeys.add(KeyEvent.VK_RIGHT);
    }

    public SpaceshipDirectKeyListener(Spaceship spaceship, Dimension mDimension, MainGameFrame mainGameFrame) {
        this.spaceship = spaceship;
        this.mainFrameDimensions = mDimension;
        spMoveTimer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Integer pressedKey : pressedNavKeys) {
                    moveByKey(pressedKey);
                }
            }
        });
        this.gameFrame = mainGameFrame;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    private void moveByKey(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP:
                move(0, -1 * move);
                break;
            case KeyEvent.VK_DOWN:
                move(0, move);
                break;
            case KeyEvent.VK_LEFT:
                move(-1 * move, 0);
                break;
            case KeyEvent.VK_RIGHT:
                move(move, 0);
                break;
            default:
                break;
        }
    }

    private void move(int x, int y) {
        Rectangle speaceShipBounds = spaceship.getBounds();
        Point p;
        if (goAround) {
            p = getPositionWhenGoesAround(speaceShipBounds, x, y);
        } else {
            p = getPosition(speaceShipBounds, x, y);
        }
        spaceship.setBounds(p.x, p.y, speaceShipBounds.width, speaceShipBounds.height);
    }

    private Point getPositionWhenGoesAround(Rectangle speaceShipBounds, int x, int y) {
        int newX = speaceShipBounds.x + x;
        if (newX > mainFrameDimensions.width) {
            newX = 0 - speaceShipBounds.width;
        } else if (newX < 0 - speaceShipBounds.width) {
            newX = mainFrameDimensions.width;
        }
        int newY = speaceShipBounds.y + y;
        if (newY > mainFrameDimensions.height) {
            newY = 0 - speaceShipBounds.height;
        } else if (newY < 0 - speaceShipBounds.height) {
            newY = mainFrameDimensions.height;
        }
        return new Point(newX, newY);
    }

    private Point getPosition(Rectangle speaceShipBounds, int x, int y) {
        int newX = speaceShipBounds.x + x;
        newX = Math.min(newX, mainFrameDimensions.width - speaceShipBounds.width);
        newX = Math.max(0, newX);
        int newY = speaceShipBounds.y + y;
        newY = Math.min(newY, mainFrameDimensions.height - speaceShipBounds.height);
        newY = Math.max(newY, 0);
        return new Point(newX, newY);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (myState == ListenerState.to_nextlevel) {
            if (e.getKeyCode() == ENTER) {
                gameFrame.nextLevel();
                myState = ListenerState.in_game;
            }
        }
        else if(myState == ListenerState.game_over){
            switch (e.getKeyCode()) {
                case ENTER:
                    gameFrame.restart(false);
                    myState = ListenerState.in_game;
                    break;
                case SPACE:
                    gameFrame.restart(true);
                    myState = ListenerState.in_game;
                    break;
                default:
                    ;
            }
        }
        else if(myState == ListenerState.game_complete){
            switch (e.getKeyCode()) {
                case ENTER:
                    gameFrame.restart(true);
                    myState = ListenerState.in_game;
                    break;
                case ESCAPE:
                    gameFrame.dispatchEvent(new WindowEvent(gameFrame, WindowEvent.WINDOW_CLOSING));
                    break;
                default:
                    ;
            }
        }
        else{
            gameFrame.initializeIfNeeded();
            int keyCode = e.getKeyCode();
            if (e.isActionKey()) {

                if (arrowKeys.contains(keyCode)) {
                    pressedNavKeys.add(keyCode);
                    if (!spMoveTimer.isRunning()) {
                        spMoveTimer.start();
                    }
                }

            } else if (keyCode == SPACE || e.getKeyChar() == 's') {
                Projectile bullet = spaceship.fireBullet();
                gameFrame.addBullet(bullet);
            } else if (e.getKeyChar() == 'a') {
                Projectile missile = spaceship.fireMissile();
                if (missile != null) {
                    gameFrame.addBullet(missile);
                }
            }
        }



    }
    private static final int SPACE = 32;
    private static final int ENTER = 10;
    private static final int ESCAPE = 27;

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.isActionKey()) {
            int keyCode = e.getKeyCode();
            pressedNavKeys.remove(keyCode);
        }
        if (pressedNavKeys.isEmpty()) {
            spMoveTimer.stop();
        }
    }

    public Dimension getMainFrameDimensions() {
        return mainFrameDimensions;
    }

    public void setMainFrameDimensions(Dimension mainFrameDimensions) {
        this.mainFrameDimensions = mainFrameDimensions;
    }

    public void setInNextLevelMenu() {
        this.myState = ListenerState.to_nextlevel;
    }

    public void setGameCompleted(){
        this.myState = ListenerState.game_complete;
    }
    
    public void setInGameOverMenu() {
        this.myState = ListenerState.game_over;
    }

}
