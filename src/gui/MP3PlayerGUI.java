/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import com.jtattoo.plaf.acryl.AcrylLookAndFeel;
import com.jtattoo.plaf.hifi.HiFiLookAndFeel;
import com.jtattoo.plaf.mcwin.McWinLookAndFeel;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerListener;
import objects.MP3;
import objects.MP3Player;
import utils.FileUtils;
import utils.MP3FileFilter;
import utils.SkinUtils;

/**
 *
 * @author ADMIN
 */
public class MP3PlayerGUI extends javax.swing.JFrame implements BasicPlayerListener{

    //константы
    private static final String MP3_FILE_EXTENSION="mp3";
    private static final String MP3_FILE_DESCRIPTION="файлы mp3";
    private static final String PLAYLIST_FILE_EXTENSION="pls";
    private static final String PLAYLIST_FILE_DESCRIPTION="файлы плейлиста";
    private static final String EMPTY_STRING="";
    private static final String INPUT_NAME_TRACK="Введите имя трека";
    
    private DefaultListModel mp3ListModel=new DefaultListModel();
    private FileFilter mp3FileFilter=new MP3FileFilter(MP3_FILE_EXTENSION,MP3_FILE_DESCRIPTION);
    private FileFilter playListFileFilter=new MP3FileFilter(PLAYLIST_FILE_EXTENSION,PLAYLIST_FILE_DESCRIPTION);
    private MP3Player player=new MP3Player(this);
    
    private int currentVolume;
    private boolean selectTrack=false;
    
    //переменные для прокрутки песни
    private long secondAmount;//секунд прошло с начала проигрывания
    private long duration;//длительность песни в секундах
    private int byteLen;//размер песни в байтах
    private double posValue=0.0;//позиция для прокрутки
    
    //передвижение ползунка от перетаскивания или от проигрывания (нужно для перемотки)
    private boolean movingFromJump=false;
    private boolean movingAvtomatic=false;//автоматическое передвижение ползунка
    
    
    
    @Override
    public void opened(Object o, Map map) {
        
        //определить размер песни и длину
        duration=(long)Math.round((((Long) map.get("duration")).longValue())/1000000);
        byteLen=(int)Math.round(((Integer)map.get("mp3.length.bytes")).intValue());
        
        //берем имя из тега если его нет то из названия файла
        String songName=map.get("title")!=null ? map.get("title").toString() : FileUtils.getFileNameWithoutExtension(new File(o.toString()).getName());
        
        //если длинное название укоротить его
        if(songName.length()>30){
            songName=songName.substring(0,30)+"...";
        }
        
        trackName.setText(songName);
        
    }

    @Override
    public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties) {
       float progress=-1.0f;
       
       if((bytesread>0) && (duration>0)){
           progress=bytesread*1.0f / byteLen*1.0f;
       }
       
       //определить сколько секунд прошло
       secondAmount=(long) (duration*progress);
       
       if(duration!=0){
           if(movingFromJump==false){
               progressTrack.setValue(((int)Math.round(secondAmount*1000/duration)));
           }
       }
       
    }

    @Override
    public void stateUpdated(BasicPlayerEvent bpe) {
       int state=bpe.getCode();
       
       if(state==BasicPlayerEvent.PLAYING){
           movingFromJump=false;
       } else if(state==BasicPlayerEvent.SEEKING){
           movingFromJump=true;
       }else if(state==BasicPlayerEvent.EOM){
           if(selectNextTrack()){
               playFile();
           }
       }
    }

    @Override
    public void setController(BasicController bc) {
    }
    
    
    /**
     * Creates new form MP3PlayerGUI
     */
    public MP3PlayerGUI() {
        initComponents();
    }
    
    
    private void playFile(){
        int[] indexes=playList.getSelectedIndices();//получаем индексы выбранных песен
        if(indexes.length>0){//выбрали хотя бы одну песню
            selectTrack=true;
            MP3 mp3=(MP3) mp3ListModel.getElementAt(indexes[0]);//выбираем первую песню
            player.play(mp3.getPath());
            player.setVolume(sliderVolume.getValue(), sliderVolume.getMaximum());
        }
    }
    
    private boolean selectPrevTrack(){
        int prevIndex=playList.getSelectedIndex()-1;
        if(prevIndex>=0){//если не вышли за пределы
            playList.setSelectedIndex(prevIndex);
            return true;
        }
        return false;
    }
    
    private boolean selectNextTrack(){
        int nextIndex=playList.getSelectedIndex()+1;
        if(nextIndex<=playList.getModel().getSize()-1){//если не вышли за пределы
            playList.setSelectedIndex(nextIndex);
            return true;
        }else{
            playList.setSelectedIndex(0);
            return true;
        }
        
    }
    
    private void delTrack(){
        int[] indexesPLS=playList.getSelectedIndices();
        if(indexesPLS.length>0){//выбран хотя бы один элемент
            ArrayList<MP3> mp3ForDel=new ArrayList<MP3>();//сохраним в отдельную коллекцию
            for(int i=0;i<indexesPLS.length;i++){
                MP3 mp3=(MP3)mp3ListModel.getElementAt(indexesPLS[i]);
                mp3ForDel.add(mp3);
            }
            
            //удаляем из плейлиста
            for(MP3 mp3:mp3ForDel){
                mp3ListModel.removeElement(mp3);
            }
        }    
    }
    
    private void processSeek(double bytes){
        try{
            long skipBytes=(long) Math.round(((Integer)byteLen).intValue()*bytes);
            player.jump(skipBytes);
        }catch(Exception ex){
            ex.printStackTrace();
            movingFromJump=false;
        }
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        contMenuAddTrack = new javax.swing.JMenuItem();
        contMenuDelTrack = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        contMenuOpenPLS = new javax.swing.JMenuItem();
        contMenuSavePLS = new javax.swing.JMenuItem();
        contMenuClearPLS = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        contMenuPlay = new javax.swing.JMenuItem();
        contMenuPause = new javax.swing.JMenuItem();
        contMenuStop = new javax.swing.JMenuItem();
        fileChooser = new javax.swing.JFileChooser();
        jPanel1 = new javax.swing.JPanel();
        trackName = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        playList = new javax.swing.JList<>();
        jPanel3 = new javax.swing.JPanel();
        sliderVolume = new javax.swing.JSlider();
        jPanel4 = new javax.swing.JPanel();
        progressTrack = new javax.swing.JSlider();
        btnVolume = new javax.swing.JToggleButton();
        btnStop = new javax.swing.JButton();
        btnPrevTrack = new javax.swing.JButton();
        btnPlayPause = new javax.swing.JToggleButton();
        btnNextTrack = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        openPLS = new javax.swing.JMenuItem();
        savePLS = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        menuExit = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenu3 = new javax.swing.JMenu();
        skinAcryl = new javax.swing.JMenuItem();
        skinHiFi = new javax.swing.JMenuItem();
        skinMcWin = new javax.swing.JMenuItem();

        contMenuAddTrack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/plus_16.png"))); // NOI18N
        contMenuAddTrack.setText("Добавить трек");
        contMenuAddTrack.setToolTipText("Добавить трек");
        contMenuAddTrack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contMenuAddTrackActionPerformed(evt);
            }
        });
        jPopupMenu1.add(contMenuAddTrack);

        contMenuDelTrack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/remove_icon.png"))); // NOI18N
        contMenuDelTrack.setText("Удалить трек");
        contMenuDelTrack.setToolTipText("Удалить трек");
        contMenuDelTrack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contMenuDelTrackActionPerformed(evt);
            }
        });
        jPopupMenu1.add(contMenuDelTrack);
        jPopupMenu1.add(jSeparator2);

        contMenuOpenPLS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/open-icon.png"))); // NOI18N
        contMenuOpenPLS.setText("Открыть плейлист");
        contMenuOpenPLS.setToolTipText("Открыть плейлист");
        contMenuOpenPLS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contMenuOpenPLSActionPerformed(evt);
            }
        });
        jPopupMenu1.add(contMenuOpenPLS);

        contMenuSavePLS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/save_16.png"))); // NOI18N
        contMenuSavePLS.setText("Сохранить плейлист");
        contMenuSavePLS.setToolTipText("Сохранить плейлист");
        contMenuSavePLS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contMenuSavePLSActionPerformed(evt);
            }
        });
        jPopupMenu1.add(contMenuSavePLS);

        contMenuClearPLS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/clear.png"))); // NOI18N
        contMenuClearPLS.setText("Очистить плейлист");
        contMenuClearPLS.setToolTipText("Очистить плейлист");
        contMenuClearPLS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contMenuClearPLSActionPerformed(evt);
            }
        });
        jPopupMenu1.add(contMenuClearPLS);
        jPopupMenu1.add(jSeparator3);

        contMenuPlay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Play.png"))); // NOI18N
        contMenuPlay.setText("Воспроизвести");
        contMenuPlay.setToolTipText("Воспроизвести");
        contMenuPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contMenuPlayActionPerformed(evt);
            }
        });
        jPopupMenu1.add(contMenuPlay);

        contMenuPause.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Pause-icon.png"))); // NOI18N
        contMenuPause.setText("Пауза");
        contMenuPause.setToolTipText("Пауза");
        contMenuPause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contMenuPauseActionPerformed(evt);
            }
        });
        jPopupMenu1.add(contMenuPause);

        contMenuStop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/stop-red-icon.png"))); // NOI18N
        contMenuStop.setText("Стоп");
        contMenuStop.setToolTipText("Остановить");
        contMenuStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contMenuStopActionPerformed(evt);
            }
        });
        jPopupMenu1.add(contMenuStop);

        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setDialogTitle("Выбрать файл");
        fileChooser.setMultiSelectionEnabled(true);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Плеер");
        setResizable(false);

        jPanel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        trackName.setFont(new java.awt.Font("Tahoma", 2, 12)); // NOI18N
        trackName.setText("...");
        trackName.setToolTipText("Название трека");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(trackName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(trackName))
        );

        jPanel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        playList.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        playList.setModel(mp3ListModel);
        playList.setToolTipText("Плейлист");
        playList.setComponentPopupMenu(jPopupMenu1);
        playList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                playListMouseClicked(evt);
            }
        });
        playList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                playListKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(playList);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel3.setPreferredSize(new java.awt.Dimension(20, 20));

        sliderVolume.setMaximum(200);
        sliderVolume.setMinorTickSpacing(5);
        sliderVolume.setOrientation(javax.swing.JSlider.VERTICAL);
        sliderVolume.setSnapToTicks(true);
        sliderVolume.setToolTipText("Громкость");
        sliderVolume.setValue(200);
        sliderVolume.setFocusable(false);
        sliderVolume.setPreferredSize(new java.awt.Dimension(10, 100));
        sliderVolume.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderVolumeStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sliderVolume, javax.swing.GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(sliderVolume, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanel4.setPreferredSize(new java.awt.Dimension(20, 20));

        progressTrack.setMaximum(1000);
        progressTrack.setMinorTickSpacing(1);
        progressTrack.setSnapToTicks(true);
        progressTrack.setToolTipText("Перемотка");
        progressTrack.setValue(0);
        progressTrack.setFocusable(false);
        progressTrack.setPreferredSize(new java.awt.Dimension(20, 26));
        progressTrack.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                progressTrackStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(progressTrack, javax.swing.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(progressTrack, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        btnVolume.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/speaker.png"))); // NOI18N
        btnVolume.setToolTipText("Вкл/Выкл звук");
        btnVolume.setFocusable(false);
        btnVolume.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/mute.png"))); // NOI18N
        btnVolume.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVolumeActionPerformed(evt);
            }
        });

        btnStop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/stop-red-icon.png"))); // NOI18N
        btnStop.setToolTipText("Остановить");
        btnStop.setFocusable(false);
        btnStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStopActionPerformed(evt);
            }
        });

        btnPrevTrack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rewind.png"))); // NOI18N
        btnPrevTrack.setToolTipText("Предыдущий трек");
        btnPrevTrack.setFocusable(false);
        btnPrevTrack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrevTrackActionPerformed(evt);
            }
        });

        btnPlayPause.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Play.png"))); // NOI18N
        btnPlayPause.setToolTipText("Воспроизвести");
        btnPlayPause.setFocusable(false);
        btnPlayPause.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Pause-icon.png"))); // NOI18N
        btnPlayPause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPlayPauseActionPerformed(evt);
            }
        });

        btnNextTrack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/forward.png"))); // NOI18N
        btnNextTrack.setToolTipText("Следующий трек");
        btnNextTrack.setFocusable(false);
        btnNextTrack.setPreferredSize(new java.awt.Dimension(30, 15));
        btnNextTrack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextTrackActionPerformed(evt);
            }
        });

        jMenu1.setText("Файл");

        openPLS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/open-icon.png"))); // NOI18N
        openPLS.setText("Откртыть плейлист");
        openPLS.setToolTipText("Откртыть плейлист");
        openPLS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openPLSActionPerformed(evt);
            }
        });
        jMenu1.add(openPLS);

        savePLS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/save_16.png"))); // NOI18N
        savePLS.setText("Сохранить плейлист");
        savePLS.setToolTipText("Сохранить плейлист");
        savePLS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                savePLSActionPerformed(evt);
            }
        });
        jMenu1.add(savePLS);
        jMenu1.add(jSeparator1);

        menuExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/exit.png"))); // NOI18N
        menuExit.setText("Выход");
        menuExit.setToolTipText("Выход");
        menuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuExitActionPerformed(evt);
            }
        });
        jMenu1.add(menuExit);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Сервис");

        jMenu3.setText("Сменить скин");
        jMenu3.setToolTipText("Смена скина");

        skinAcryl.setText("Acryl");
        skinAcryl.setToolTipText("Сменить скин на Acryl");
        skinAcryl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                skinAcrylActionPerformed(evt);
            }
        });
        jMenu3.add(skinAcryl);

        skinHiFi.setText("HiFi");
        skinHiFi.setToolTipText("Сменить скин на HiFi");
        skinHiFi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                skinHiFiActionPerformed(evt);
            }
        });
        jMenu3.add(skinHiFi);

        skinMcWin.setText("McWin");
        skinMcWin.setToolTipText("Сменить скин на McWin");
        skinMcWin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                skinMcWinActionPerformed(evt);
            }
        });
        jMenu3.add(skinMcWin);

        jMenu2.add(jMenu3);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnStop, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(26, 26, 26)
                                .addComponent(btnPrevTrack, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnPlayPause, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnNextTrack, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnVolume, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnVolume, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(14, 14, 14)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(btnPrevTrack)
                                    .addComponent(btnNextTrack, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(10, 10, 10))
                            .addComponent(btnPlayPause, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 3, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(btnStop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void openPLSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openPLSActionPerformed
        FileUtils.addFileFilter(fileChooser, playListFileFilter);
        int result=fileChooser.showOpenDialog(this);//хранит результат выбран ли файл
        
        if(result==JFileChooser.APPROVE_OPTION){//нажата кнопка ОК
            File selectFile=fileChooser.getSelectedFile();
            DefaultListModel mp3ListModel=(DefaultListModel) FileUtils.deserialize(selectFile.getPath());
            this.mp3ListModel=mp3ListModel;
            playList.setModel(mp3ListModel);
        }
    }//GEN-LAST:event_openPLSActionPerformed

    private void savePLSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_savePLSActionPerformed
        FileUtils.addFileFilter(fileChooser, playListFileFilter);
        int result=fileChooser.showSaveDialog(this);
        if(result==JFileChooser.APPROVE_OPTION){
            File selectFile=fileChooser.getSelectedFile();
            if(selectFile.exists()){//существует ли такой файл
                
                int resultOverride=JOptionPane.showConfirmDialog(this, "Файл существует", "Перезаписать?", JOptionPane.YES_NO_CANCEL_OPTION);
                switch(resultOverride){
                    case JOptionPane.NO_OPTION:
                        savePLSActionPerformed(evt);
                        return;
                    case JOptionPane.CANCEL_OPTION:
                        fileChooser.cancelSelection();
                        return;
                }
                
                fileChooser.approveSelection();
            }
            
            String fileExtension=FileUtils.getFileExtension(selectFile);
            
            //нужно ли расширение добавлять к имени файла
            String fileNameForSave=(fileExtension!=null && fileExtension.equals(PLAYLIST_FILE_EXTENSION)) ? selectFile.getPath() : selectFile.getPath()+"."+PLAYLIST_FILE_EXTENSION;
            
            FileUtils.serialize(mp3ListModel, fileNameForSave);
        }
    }//GEN-LAST:event_savePLSActionPerformed

    private void menuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuExitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_menuExitActionPerformed

    private void skinAcrylActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_skinAcrylActionPerformed
        SkinUtils.changeSkin(this, new AcrylLookAndFeel());
        fileChooser.updateUI();
    }//GEN-LAST:event_skinAcrylActionPerformed

    private void skinHiFiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_skinHiFiActionPerformed
        SkinUtils.changeSkin(this, new HiFiLookAndFeel());
        fileChooser.updateUI();
    }//GEN-LAST:event_skinHiFiActionPerformed

    private void skinMcWinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_skinMcWinActionPerformed
        SkinUtils.changeSkin(this, new McWinLookAndFeel());
        fileChooser.updateUI();
    }//GEN-LAST:event_skinMcWinActionPerformed

    private void playListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playListMouseClicked
        //если нажали левую кнопку 2 раза
        if(evt.getModifiers()==InputEvent.BUTTON1_MASK && evt.getClickCount()==2){
            playFile();
            btnPlayPause.setSelected(true);
        }
    }//GEN-LAST:event_playListMouseClicked

    private void btnVolumeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVolumeActionPerformed
        if(btnVolume.isSelected()){
            currentVolume=sliderVolume.getValue();
            sliderVolume.setValue(0);
        }else{
            sliderVolume.setValue(currentVolume);
        }
    }//GEN-LAST:event_btnVolumeActionPerformed

    private void progressTrackStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_progressTrackStateChanged
        if(progressTrack.getValueIsAdjusting()==false){
            if(movingAvtomatic==true){
                movingAvtomatic=false;
                posValue=progressTrack.getValue()*1.0/1000;
                processSeek(posValue);
            }
        }else{
            movingAvtomatic=true;
            movingFromJump=true;
        }

    }//GEN-LAST:event_progressTrackStateChanged

    private void btnNextTrackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextTrackActionPerformed
        if(selectNextTrack()){
            playFile();
        }
    }//GEN-LAST:event_btnNextTrackActionPerformed

    private void btnPlayPauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPlayPauseActionPerformed
        int size=playList.getModel().getSize();
        if(size>0){
            if(btnPlayPause.isSelected()){
                playFile();
            } else{
                player.pause();
            }
        }else{
            btnPlayPause.setSelected(false);
        }
        
    }//GEN-LAST:event_btnPlayPauseActionPerformed

    private void btnPrevTrackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevTrackActionPerformed
        if(selectPrevTrack()){
            playFile();
        }
    }//GEN-LAST:event_btnPrevTrackActionPerformed

    private void btnStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStopActionPerformed
        player.stop();
        btnPlayPause.setSelected(false);
    }//GEN-LAST:event_btnStopActionPerformed

    private void contMenuAddTrackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contMenuAddTrackActionPerformed
        FileUtils.addFileFilter(fileChooser, mp3FileFilter);
        int result=fileChooser.showOpenDialog(this);
        
        if(result==JFileChooser.APPROVE_OPTION){
            File[] files=fileChooser.getSelectedFiles();
            //перебираем для добавления в плейлист
            for(File file:files){
                MP3 mp3=new MP3(file.getName(), file.getPath());
                
                if(!mp3ListModel.contains(mp3)){
                    mp3ListModel.addElement(mp3);
                    playList.setSelectedIndex(0);
                }
            }
        }
    }//GEN-LAST:event_contMenuAddTrackActionPerformed

    private void sliderVolumeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderVolumeStateChanged
        player.setVolume(sliderVolume.getValue(), sliderVolume.getMaximum());
        
        
        if (sliderVolume.getValue() == 0) {
            btnVolume.setSelected(true);
        } else {
            btnVolume.setSelected(false);
        }
    }//GEN-LAST:event_sliderVolumeStateChanged

    private void contMenuDelTrackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contMenuDelTrackActionPerformed
        delTrack();
    }//GEN-LAST:event_contMenuDelTrackActionPerformed

    private void playListKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_playListKeyPressed
        int key=evt.getKeyCode();
        if(key==KeyEvent.VK_DELETE){
            delTrack();
        }
    }//GEN-LAST:event_playListKeyPressed

    private void contMenuSavePLSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contMenuSavePLSActionPerformed
        savePLSActionPerformed(evt);
    }//GEN-LAST:event_contMenuSavePLSActionPerformed

    private void contMenuOpenPLSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contMenuOpenPLSActionPerformed
        openPLSActionPerformed(evt);
    }//GEN-LAST:event_contMenuOpenPLSActionPerformed

    private void contMenuClearPLSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contMenuClearPLSActionPerformed
        mp3ListModel.clear();
    }//GEN-LAST:event_contMenuClearPLSActionPerformed

    private void contMenuPlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contMenuPlayActionPerformed
        btnPlayPause.setSelected(true);
        btnPlayPauseActionPerformed(evt);
    }//GEN-LAST:event_contMenuPlayActionPerformed

    private void contMenuPauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contMenuPauseActionPerformed
        btnPlayPause.setSelected(false);
        btnPlayPauseActionPerformed(evt);
    }//GEN-LAST:event_contMenuPauseActionPerformed

    private void contMenuStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contMenuStopActionPerformed
        btnStopActionPerformed(evt);
    }//GEN-LAST:event_contMenuStopActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            /* Set the Nimbus look and feel */
            //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
            /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
            * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
            */
            javax.swing.UIManager.setLookAndFeel(new HiFiLookAndFeel());
            //</editor-fold>
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(MP3PlayerGUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MP3PlayerGUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnNextTrack;
    private javax.swing.JToggleButton btnPlayPause;
    private javax.swing.JButton btnPrevTrack;
    private javax.swing.JButton btnStop;
    private javax.swing.JToggleButton btnVolume;
    private javax.swing.JMenuItem contMenuAddTrack;
    private javax.swing.JMenuItem contMenuClearPLS;
    private javax.swing.JMenuItem contMenuDelTrack;
    private javax.swing.JMenuItem contMenuOpenPLS;
    private javax.swing.JMenuItem contMenuPause;
    private javax.swing.JMenuItem contMenuPlay;
    private javax.swing.JMenuItem contMenuSavePLS;
    private javax.swing.JMenuItem contMenuStop;
    private javax.swing.JFileChooser fileChooser;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JMenuItem menuExit;
    private javax.swing.JMenuItem openPLS;
    private javax.swing.JList<String> playList;
    private javax.swing.JSlider progressTrack;
    private javax.swing.JMenuItem savePLS;
    private javax.swing.JMenuItem skinAcryl;
    private javax.swing.JMenuItem skinHiFi;
    private javax.swing.JMenuItem skinMcWin;
    private javax.swing.JSlider sliderVolume;
    private javax.swing.JLabel trackName;
    // End of variables declaration//GEN-END:variables

    
}
