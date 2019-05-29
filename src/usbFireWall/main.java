package usbFireWall;

import java.io.File;

import javax.swing.filechooser.FileSystemView;

public class main {

    public static void main(String[] args) {
        System.out.println("LL");

        USB usb = new USB();
        usb.findURootPath();
        usb.start();
/*        while (true){
            String d = usb.usbListener();
            usb.checkDiskDir(d);
        }*/


    }



}
