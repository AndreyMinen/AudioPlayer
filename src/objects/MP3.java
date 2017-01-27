/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objects;

import java.io.Serializable;
import utils.FileUtils;

/**
 *
 * @author ADMIN
 */
public class MP3 implements Serializable{
    
    private String name;
    private String path;

    public MP3(String name, String path) {
        this.name = name;
        this.path = path;
    }
    
    
    //для корректного отображения

    @Override
    public String toString() {
        return FileUtils.getFileNameWithoutExtension(name);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof MP3)) return false;
        
        MP3 mp3= (MP3) obj;
        return path.equals(mp3.getPath());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    
    
    
    
}
