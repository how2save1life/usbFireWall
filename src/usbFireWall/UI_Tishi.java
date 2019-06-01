package usbFireWall;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashSet;

public class UI_Tishi extends JFrame {
    private JPanel contentPane;
    private JTextField textField;
    private JTextArea textArea;
    USB usb;

    /**
     * Create the frame.
     */
    public UI_Tishi(USB usb) {
        this.usb = usb;

        setTitle("U盘防火墙----发现病毒");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(0, 0, 450, 300);
        setLocationRelativeTo(null);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        //
        JButton btnNewButton = new JButton("txt查看autorun.inf");
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (usb.openAsTXT(usb.infPath)) {
                } else {
                    JOptionPane.showMessageDialog(null, "autorun.inf已删除或不存在。", "U盘防火墙----打开文件失败", JOptionPane.WARNING_MESSAGE);
                }
                System.out.println("txt查看autorun.inf");
            }
        });
        btnNewButton.setBounds(35, 213, 140, 27);
        contentPane.add(btnNewButton);

        //
        JButton btnNewButton_1 = new JButton("删除autorun.inf");
        btnNewButton_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (usb.deleteFile(usb.infPath)) {//删除成功
                    JOptionPane.showMessageDialog(null, "成功删除autorun.inf文件。已阻止病毒自动运行。" + "\n" + "可点击“清除病毒”按钮，完全清除病毒文件", "U盘防火墙----删除成功", JOptionPane.PLAIN_MESSAGE);
                    textField.setText(usb.infPath + " 删除成功");
                } else {
                    JOptionPane.showMessageDialog(null, "autorun.inf已删除或不存在。", "U盘防火墙----删除失败", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        btnNewButton_1.setBounds(183, 213, 130, 27);
        contentPane.add(btnNewButton_1);

        //
        JButton btnNewButton_2 = new JButton("清除病毒");
        btnNewButton_2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                StringBuilder success = new StringBuilder();
                StringBuilder fail = new StringBuilder();
                for (String v : usb.viruses) {
                    if (usb.deleteFile(v)) {
                        usb.viruses.remove(v);//删除成功（剩下的是失败的
                        success.append(v).append("\n");
                    } else {
                        fail.append(v).append("\n");
                    }
                }
                String result;
                result = "成功删除：" + "\n" + success.toString() + "\n" + "删除失败或不存在：" + "\n" + fail.toString();
                textArea.setText(result);
                JOptionPane.showMessageDialog(null, result, "U盘防火墙----删除结果", JOptionPane.PLAIN_MESSAGE);
            }
        });
        btnNewButton_2.setBounds(320, 213, 100, 27);
        contentPane.add(btnNewButton_2);

        //
        JLabel label = new JLabel("提示");
        label.setBounds(35, 25, 72, 18);
        contentPane.add(label);

        //提示文本框
        textField = new JTextField();
        textField.setBounds(85, 22, 333, 24);
        textField.setBackground(Color.white);
        textField.setText("扫描到风险文件" + usb.infPath);
        textField.setEditable(false);
        contentPane.add(textField);
        textField.setColumns(10);

        //
        JLabel lblNewLabel = new JLabel("可疑病毒文件");
        lblNewLabel.setBounds(35, 64, 100, 18);
        contentPane.add(lblNewLabel);

        //病毒列表文本框
        textArea = new JTextArea();
        textArea.setBounds(120, 56, 297, 144);
        if (usb.viruses.isEmpty()) {
            textArea.setText("未识别到可疑病毒文件");
        } else {
            String virus = "";
            for (String v : usb.viruses) {
                virus += (v + "\n");
            }
            textArea.setText(virus);
        }
        textArea.setBackground(Color.white);
        textArea.setEditable(false);
        contentPane.add(textArea);
        textArea.setColumns(10);

        setVisible(true);
    }
}
