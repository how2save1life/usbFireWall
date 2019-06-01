package usbFireWall;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class USB extends Thread {
    //保存所有盘符
    File[] files = File.listRoots();
    boolean addDisk = false;//是否有新盘
    File theNewDisk = null;
    File theOutDisk = null;
    String infPath;
    HashSet<String> viruses = new HashSet<String>();

    public void run() {
        while (true) {
            String d = usbListener();
            checkDiskDir(d);
        }

    }

    void findURootPath() {
        //文件目录
        FileSystemView sys = FileSystemView.getFileSystemView();

        for (File file : files) {
            //得到系统中存在的C:\,D:\,E:\,F:\,H:\
            System.out.println("系统中存在的" + file.getPath());
        }

        for (File file : files) {
            //得到文字命名形式的盘符系统 (C:)、软件 (D:)、公司文档 (E:)、测试U盘 (H:)
            System.out.println("文字命名：" + sys.getSystemDisplayName(file));
        }
    }

    String usbListener() {
        File[] currentFiles = File.listRoots();
        FileSystemView sys = FileSystemView.getFileSystemView();//得到文字命名形式的盘符系统 (C:)、软件 (D:)、公司文档 (E:)、测试U盘 (H:)
        File theFile = null;//变化的那个盘

        //有新加入的盘
        if (files.length < currentFiles.length) {
            theFile = getTheFile(currentFiles, files);
            theNewDisk=theFile;
            files = currentFiles;
            addDisk = true;
            safeOpen(theFile.getAbsolutePath());//安全打开
            System.out.println("新加盘：" + theFile.getAbsolutePath() + sys.getSystemDisplayName(theFile));
        }
        //减少盘
        else if (files.length > currentFiles.length) {
            theFile = getTheFile(files, currentFiles);
            theNewDisk=null;
            theOutDisk=theFile;
            files = currentFiles;
            addDisk = false;
            System.out.println("删除盘：" + theFile.getAbsolutePath());
        }

        return theFile == null ? "" : theFile.getPath();
    }


    //某盘全扫描
    void checkOneDisk(String disk) {
        File dir = new File(disk);
        File[] files = dir.listFiles();
        if (files == null)
            return;
        for (File file : files) {
            if (file.isDirectory()) {
                checkOneDisk(file.getAbsolutePath());
            } else {
                String strFileName = file.getAbsolutePath();
                System.out.println("---" + strFileName);
            }
        }
    }

    //某盘根目录扫描
    int checkDiskDir(String disk) {
        File dir = new File(disk);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory()) {
                    String strFileName = file.getAbsolutePath();
                    System.out.println("---" + strFileName + "?");
                    if (file.getName().toLowerCase().equals("autorun.inf")) {
                        System.out.println(file.getAbsolutePath());
                        infPath = file.getAbsolutePath();
                        //openAsTXT(file.getAbsolutePath());
                        //autorun = file;
                        return scanAutoRun(file.getAbsolutePath());
                    }
                }
            }
        }

        return 0;
    }


    /*************************************************************************************************************/
    //找出缺少or多的那一个盘
    private File getTheFile(File[] longerFiles, File[] shorterFiles) {
        File theFile = null;//变化的那个盘
        boolean flag = false;
        //当前usb设备列表  和 之前的usb设备列表对比
        for (int i = longerFiles.length - 1; i >= 0; i--) {
            flag = false;
            for (int j = shorterFiles.length - 1; j >= 0; j--) {//倒着找，变化的肯定在后面
                if (longerFiles[i].equals(shorterFiles[j])) {//要用equals，==没用
                    flag = true;//在过去的列表中找到当前的
                    break;
                }
            }
            //没找到 则longerFiles[i]为变化盘
            if (!flag) {
                theFile = longerFiles[i];//当前盘为新加盘
                break;
            }
        }
        return theFile;
    }

    //安全打开
    void safeOpen(String path) {
        try {
            Runtime.getRuntime().exec("cmd /c start " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //以txt打开
    boolean openAsTXT(String path) {
        File f=new File(path);
        if(!f.exists()){
            return false;
        }
        try {
            Runtime.getRuntime().exec("cmd.exe  /c notepad " + path);
            return true;
        } catch (Exception e) {
            System.out.println("打开" + path + "失败！");
            e.printStackTrace();
        }
        return false;
    }

    //扫描文件
    int scanAutoRun(String path) {
        viruses.clear();//清空上一次存入的
        String diskpath=path.substring(0,3).toLowerCase();
        File file = new File(path);
        //保存一个一个字节读
        byte[] infbytes = new byte[1];
        //字符串保存结果
        String inf = "";
        try {
            //输入流
            FileInputStream reader = new FileInputStream(file);

            //读 转换为string
            while ((reader.read(infbytes, 0, 1)) != -1) {
                inf += new String(infbytes);
            }

            //去空格
            inf = inf.replaceAll(" ", "");
            //转小写
            inf = inf.toLowerCase();

            //是否包含 "[autorun]"
            if (!inf.contains("[autorun]")) {
                return -1;
            }

            int index = 0;
            String keyword;
            String virus = "";
            while ((index = inf.indexOf("=", index)) != -1) {
                //open
                keyword = inf.substring(index - "open".length(), index);
                if (keyword.equals("open")) {
                    for (int i = 1;
                         index + i < inf.length() && inf.charAt(index + i) != '\r' && inf.charAt(index + i) != '\n';
                         i++) {
                        virus += inf.charAt(index + i);
                    }
                    if (virus.contains(".bat") || virus.contains(".exe")
                            || virus.contains(".com") || virus.contains(".vbs")) {
                        if (!virus.contains(":\\")) {
                            virus = diskpath + virus;
                        }
                        viruses.add(virus);
                    }

                    //System.out.println(virus);
                    virus = "";
                }

                //shellexecute
                keyword = inf.substring(index - "shellexecute".length(), index);
                if (keyword.equals("shellexecute")) {
                    for (int i = 1; index + i < inf.length() && inf.charAt(index + i) != '\r' && inf.charAt(index + i) != '\n'; i++) {
                        virus += inf.charAt(index + i);
                    }
                    if (virus.contains(".bat") || virus.contains(".exe") || virus.contains(".com") || virus.contains(".vbs")) {
                        if (!virus.contains(":\\")) {
                            virus = diskpath + virus;
                        }
                        viruses.add(virus);
                    }
                    //System.out.println("shell" + virus);
                    virus = "";
                }

                //command
                keyword = inf.substring(index - "command".length(), index);
                if (keyword.equals("command")) {
                    for (int i = 1; index + i < inf.length() && inf.charAt(index + i) != '\r' && inf.charAt(index + i) != '\n'; i++) {
                        virus += inf.charAt(index + i);
                    }
                    if (virus.contains(".bat") || virus.contains(".exe") || virus.contains(".com") || virus.contains(".vbs")) {
                        if (!virus.contains(":\\")) {
                            virus = diskpath + virus;
                        }
                        viruses.add(virus);
                    }
                    //System.out.println("command" + virus);
                    virus = "";

                }
                index++;
            }
            System.out.println(inf);
            //有风险
            if (!viruses.isEmpty()) {
                Toolkit.getDefaultToolkit().beep();
                return 1;
            }


        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("file read fail" + e);
        }
        //失败
        return 0;
    }

    //删除文件
    boolean deleteFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("删除文件失败:" + file.getAbsolutePath() + "不存在！");
            return false;
        } else {
            if (file.isFile())
                return file.delete();
        }
        return false;
    }

    public void changeStart(boolean isStartAtLogon) throws IOException{
        String regKey = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run";
        String myAppName = "usbFireWall";
        String exePath = System.getProperty("user.dir")+"\\usbFireWall.exe";
        Runtime.getRuntime().exec("reg "+(isStartAtLogon?"add ":"delete ")+regKey+" /v "+myAppName+(isStartAtLogon?" /t reg_sz /d "+exePath:" /f"));
    }

}
