package usbFireWall;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class UI extends JFrame implements Runnable {
    private JPanel contentPane;
    private JTextField textField;
    private JComboBox<String> comboBox;
    boolean Start = true;
    String selectedDisk;
    USB usb = new USB();

    @Override
    public void run() {
        //保存扫到的盘符 用于比较是否有变化
        File f[] = usb.files;
        FileSystemView sys = FileSystemView.getFileSystemView();//得到文字命名形式的盘符系统 (C:)、软件 (D:)、公司文档 (E:)、测试U盘 (H:)
        while (true) {
            if (Start) {
                //System.out.println("hello");
                //监听usb
                String d = usb.usbListener();
                //盘符有变 更新下拉
                if (f.length != usb.files.length) {
                    if (usb.theNewDisk != null) {
                        JOptionPane.showMessageDialog(null, "插入 " + sys.getSystemDisplayName(usb.theNewDisk), "新盘加入", JOptionPane.PLAIN_MESSAGE);
                    }else {
                        JOptionPane.showMessageDialog(null, "弹出 " + usb.theOutDisk.getAbsolutePath(), "U盘弹出", JOptionPane.PLAIN_MESSAGE);
                    }
                    comboBox.removeAllItems();
                    for (File item : usb.files) {
                        comboBox.addItem(item.getPath());
                    }
                    f = usb.files;
                }
                //对新增盘扫描
                if (usb.addDisk && usb.checkDiskDir(d) == 1) {

                    UI_Tishi tishi = new UI_Tishi(usb);
                }

            } else {
                //不写语句就死循环，点击按钮关闭再打开后不执行if(Start){}
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    UI() {
        JFrame jf = new JFrame("系统托盘测试");
        jf.setTitle("U盘防火墙");
        jf.setSize(560, 320);
        jf.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE); // 点击关闭按钮时隐藏窗口
        jf.setLocationRelativeTo(null);
        jf.getContentPane().setLayout(null);

        contentPane = new JPanel();
        contentPane.setLayout(null);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setBounds(0, 0, 547, 273);
        jf.getContentPane().add(contentPane);

        //扫描所有盘
        JButton btnNewButton = new JButton("扫描当前盘");
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                System.out.println("扫描当前盘:" + selectedDisk);
                if (usb.checkDiskDir(selectedDisk) == 1) {
                    UI_Tishi tishi = new UI_Tishi(usb);
                } else {
                    JOptionPane.showMessageDialog(null, "没有扫描到风险文件。", "U盘防火墙----U盘安全", JOptionPane.PLAIN_MESSAGE);
                }
            }
        });
        btnNewButton.setBounds(413, 111, 120, 39);
        contentPane.add(btnNewButton);

        //开始监控
        JButton button = new JButton("停止监控");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Start = !Start;
                if (Start) {
                    button.setText("停止监控");
                    textField.setText("----------------------------------U盘插入监控中--------------------------------------");
                } else {
                    button.setText("开始监控");
                    textField.setText("----------------------------------U盘监控停止----------------------------------------");
                }
                System.out.println(Start);
            }
        });
        button.setBounds(279, 111, 120, 39);
        contentPane.add(button);

        //安全打开
        JButton button_1 = new JButton("安全打开");
        button_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("安全打开" + selectedDisk);
                usb.safeOpen(selectedDisk);
            }
        });
        button_1.setBounds(145, 111, 120, 39);
        contentPane.add(button_1);

        //关闭到托盘
        JCheckBox chckbxNewCheckBox = new JCheckBox("关闭到托盘");
        chckbxNewCheckBox.setBounds(28, 173, 90, 30);
        chckbxNewCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (chckbxNewCheckBox.isSelected()) {
                    jf.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
                } else {
                    jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                }
            }
        });
        chckbxNewCheckBox.setSelected(true);
        contentPane.add(chckbxNewCheckBox);

        //退出
        JButton button_2 = new JButton("退出");
        button_2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("退出");
                if (!chckbxNewCheckBox.isSelected()){
                    Object[] options = {"确定 ", "取消 "};
                    int res = JOptionPane.showOptionDialog(jf, "确定退出U盘防火墙？ ", "退出 ", JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                    if (res == JOptionPane.YES_OPTION) {
                        System.out.println("yes");
                        jf.dispatchEvent(new WindowEvent(jf, WindowEvent.WINDOW_CLOSING));
                    } else {
                        System.out.println("no");
                    }
                }else {
                    JOptionPane.showMessageDialog(null, "将最小化到托盘", "提示", JOptionPane.PLAIN_MESSAGE);
                    jf.setVisible(false);
                }

            }
        });
        button_2.setBounds(413, 173, 120, 39);
        contentPane.add(button_2);


        //开机启动
        JCheckBox NewCheckBox = new JCheckBox("开机自启动(需要管理员权限)");
        NewCheckBox.setBounds(140, 173, 190, 30);
        NewCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (NewCheckBox.isSelected()) {
                    try {
                        usb.changeStart(true);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    try {
                        usb.changeStart(false);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        NewCheckBox.setSelected(true);
        contentPane.add(NewCheckBox);


        //下拉选框
        comboBox = new JComboBox(this.usb.files);
        // 添加条目选中状态改变的监听器
        comboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                // 只处理选中的状态
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    selectedDisk = comboBox.getSelectedItem().toString();
                    System.out.println("选中: " + comboBox.getSelectedIndex() + " = " + selectedDisk);
                }
            }
        });
        comboBox.setSelectedIndex(0);
        selectedDisk = comboBox.getSelectedItem().toString();
        comboBox.setBounds(28, 111, 84, 39);
        contentPane.add(comboBox);

        //程序状态
        JLabel lblNewLabel = new JLabel("程序状态");
        lblNewLabel.setBounds(28, 36, 84, 39);
        contentPane.add(lblNewLabel);

        //程序状态文本
        textField = new JTextField();
        textField.setText("----------------------------------U盘插入监控中--------------------------------------");
        textField.setBounds(126, 36, 369, 35);
        textField.setEditable(false);
        contentPane.add(textField);
        textField.setColumns(10);

        //
        JLabel lblNewLabel_1 = new JLabel("161310225  张## && 161310221  张##");
        lblNewLabel_1.setBounds(28, 225, 495, 35);
        contentPane.add(lblNewLabel_1);

        /*
         * 添加系统托盘
         */
        if (SystemTray.isSupported()) {
            // 获取当前平台的系统托盘
            SystemTray tray = SystemTray.getSystemTray();
            // 加载一个图片用于托盘图标的显示
            Image image = Toolkit.getDefaultToolkit().getImage(firewall.class.getResource("/image/111.jpg"));
            // 创建点击图标时的弹出菜单
            PopupMenu popupMenu = new PopupMenu();

            MenuItem openItem = new MenuItem("打开");
            MenuItem exitItem = new MenuItem("退出");

            openItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // 点击打开菜单时显示窗口
                    if (!jf.isShowing()) {
                        jf.setVisible(true);
                    }
                }
            });
            exitItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 点击退出菜单时退出程序
                    System.exit(0);
                }
            });

            popupMenu.add(openItem);
            popupMenu.add(exitItem);

            // 创建一个托盘图标
            TrayIcon trayIcon = new TrayIcon(image, "U盘防火墙", popupMenu);

            // 托盘图标自适应尺寸
            trayIcon.setImageAutoSize(true);

            trayIcon.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("托盘图标被左键双击");
                    if (!jf.isShowing()) {
                        jf.setVisible(true);
                    }
                }
            });
            trayIcon.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    switch (e.getButton()) {
                        case MouseEvent.BUTTON1: {
                            //System.out.println("托盘图标被鼠标左键被点击");
                            break;
                        }
                        case MouseEvent.BUTTON2: {
                            //System.out.println("托盘图标被鼠标中键被点击");
                            break;
                        }
                        case MouseEvent.BUTTON3: {
                            // System.out.println("托盘图标被鼠标右键被点击");
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                }
            });

            // 添加托盘图标到系统托盘
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                e.printStackTrace();
            }

        } else {
            System.out.println("当前系统不支持系统托盘");
        }

        jf.setVisible(true);
    }


}
