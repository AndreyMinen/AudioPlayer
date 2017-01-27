/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author ADMIN
 */


//фшльтр для fileChooser для выбора mp3
public class MP3FileFilter extends FileFilter {

    private String fileExtension;
    private String fileDescription;

    public MP3FileFilter(String fileExtension, String fileDescription) {
        this.fileExtension = fileExtension;
        this.fileDescription = fileDescription;
    }
    
    
    @Override
    public boolean accept(File file) {
        return file.isDirectory() || file.getAbsolutePath().endsWith(fileExtension); //разрешить папки и файлы
    }

    @Override
    public String getDescription() {
        return fileDescription+" (*."+fileExtension+")";
    }
    
}
