package usbFireWall;

import java.awt.*;
import java.io.File;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

public class firewall {

    public static void main(String[] args) {
        System.out.println("LL");
        UI frame = new UI();
        Thread uiThread = new Thread(frame);
        uiThread.start();
    }


}
