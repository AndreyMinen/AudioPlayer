/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objects;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

/**
 *
 * @author ADMIN
 */
public class MP3Player extends BasicPlayer {
    
    private BasicPlayer player=new BasicPlayer();
    private String currentFile;//текущая песня
    private double currentVolume;//текущая громкость
    
    public MP3Player(BasicPlayerListener listener){
        player.addBasicPlayerListener(listener);
    }
    
    public void play(String fileName){
        //продолжить воспроизведение
        try{
            if(currentFile!=null && currentFile.equals(fileName) && player.getStatus()==player.PAUSED){
                player.resume();
                return;
            }
            
            File mp3File=new File(fileName);
            currentFile=fileName;
            player.open(mp3File);
            player.play();
            player.setGain(currentVolume);//устанавливаем уровень звука
            
        }catch(BasicPlayerException ex) {
            Logger.getLogger(MP3Player.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void stop(){
        try {
            player.stop();
        } catch (BasicPlayerException ex) {
            Logger.getLogger(MP3Player.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void pause(){
        try {
            player.pause();
        } catch (BasicPlayerException ex) {
            Logger.getLogger(MP3Player.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //регулировать звук
    public void setVolume(int currentVolume, int maxVolume){
        this.currentVolume=currentVolume;
        try {
               if(currentVolume==0){
                   player.setGain(0);
               } else{
                   player.setGain(calcVolume(currentVolume, maxVolume));
               }
            
        } catch (BasicPlayerException ex) {
                Logger.getLogger(MP3Player.class.getName()).log(Level.SEVERE, null, ex);
            
        }
    }
    
    //подсчитать уровегь громкости
    private double calcVolume(int currentVolume,int maxVolume){
        this.currentVolume=(double) currentVolume / (double) maxVolume;
        return this.currentVolume;
    }
    
   public void jump(long bytes) {
        try {
            player.seek(bytes);
            player.setGain(currentVolume);// устанавливаем уровень звука
        } catch (BasicPlayerException ex) {
            Logger.getLogger(MP3Player.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
