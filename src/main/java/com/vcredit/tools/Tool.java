package com.vcredit.tools;

import com.alibaba.excel.EasyExcel;
import com.vcredit.tools.excel.data.FileData;
import com.vcredit.tools.excel.listener.FileImportDataListener;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Set;

/**
 * @Author: tangdandan
 * @Date: 2020/4/17 11:12
 */
@Slf4j
public class Tool extends JFrame {
    public static void main(String[] args) {
        //开启界面
        Tool tool = new Tool();
        tool.block();
    }

    public void block(){
        final JFrame jf = new JFrame("比较数据小工具");

        jf.setSize(300, 500);
        jf.setLocationRelativeTo(null);
        jf.setResizable(false);
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT,20,15);
        JPanel panel = new JPanel(flowLayout);


        //作者
        JLabel author = new JLabel("<html><body><p align=\"left\"><font size=\"3\" " +
                "face=\"arial\" color=\"blue\">author: dandan tang~~~~~<br/>date: 2020/04/20</font" +
                "></p></body></html>");
        panel.add(author);


        // 创建文本框1，写入文件1地址
        final JTextArea fileOneAdd = new JTextArea(2, 25);
        fileOneAdd.setLineWrap(true);
        fileOneAdd.setText("文件1地址：");
        panel.add(fileOneAdd);

        //上传excel文件1
        JButton openBtn1 = new JButton("上传第一个文件");
        openBtn1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("上传第一个文件");
                showFileOpenDialog(jf,fileOneAdd);
            }
        });
        panel.add(openBtn1);



        // 创建文本框2，写入文件2地址
        final JTextArea fileTwoAdd = new JTextArea(2, 25);
        fileTwoAdd.setLineWrap(true);
        fileTwoAdd.setText("文件2地址：");
        panel.add(fileTwoAdd);

        //上传excel文件2
        JButton openBtn2 = new JButton("上传第二个文件");
        openBtn2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("上传第二个文件");
                showFileOpenDialog(jf,fileTwoAdd);
            }
        });
        panel.add(openBtn2);


        // 创建文本区域, 显示结果信息
        final JTextArea resTxt = new JTextArea(10,25);
        resTxt.setLineWrap(true);
        resTxt.setText("显示结果：");
        JScrollPane jsp = new JScrollPane(resTxt);
        jsp.setBounds(13, 10, 350, 340);
        jsp.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(jsp);
       // panel.add(resTxt);

        JLabel hideLabel = new JLabel("                     ");
        hideLabel.setVisible(true);
        panel.add(hideLabel);

        JButton compareBtn = new JButton("比较");
        compareBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("比较数据！");
                if(fileOneAdd.getText().isEmpty()||fileTwoAdd.getText().isEmpty()){
                   warningDialog("请选择要比较的文件");
                }
                compareTool(jf,resTxt,fileOneAdd.getText(),fileTwoAdd.getText());
            }
        });
        panel.add(compareBtn);

        jf.setContentPane(panel);
        jf.setVisible(true);

    }

    /**
     * 比较数据
     * @param jf
     * @param resTxt
     */
    private void compareTool(JFrame jf, JTextArea resTxt,String file1Address,String file2Address) {
        //获取jedis
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        //使用通道，加速处理数据
        Pipeline pipeline = jedis.pipelined();

        ArrayList<String> data = new ArrayList<>();
        ArrayList<String> repeat = new ArrayList<>();
        try {
            String firstLabel = "file1";
            File fileOne= new File(file1Address);
            InputStream inputStreamOne = new FileInputStream(fileOne);
            String secondLabel = "file2";
            File fileTwo= new File(file2Address);
            InputStream inputStreamTwo = new FileInputStream(fileTwo);

            FileImportDataListener dataListener = new FileImportDataListener(firstLabel,repeat);
            EasyExcel.read(inputStreamOne, FileData.class,dataListener).sheet().doRead();
            log.info(fileOne.getName()+"总共解析存储了"+dataListener.getTotal()+"条数据入库~~~~~");
            FileImportDataListener dataListener2 = new FileImportDataListener(secondLabel,repeat);
            EasyExcel.read(inputStreamTwo, FileData.class,dataListener2).sheet().doRead();
            log.info(fileTwo.getName()+"总共解析存储了"+dataListener2.getTotal()+"条数据入库~~~~~");

            Response<Set<String>> sDiff1 = pipeline.sdiff(firstLabel, secondLabel);
            Response<Set<String>> sDiff2 = pipeline.sdiff(secondLabel, firstLabel);

            pipeline.del(firstLabel);
            pipeline.del(secondLabel);
            pipeline.clear();
            pipeline.close();
            sDiff1.get().stream().forEach(s->{
                data.add(s);
            });
            sDiff2.get().stream().forEach(d->{
                data.add(d);
            });
            resTxt.setText("");
            resTxt.setText("两个文件包含的不同数据有："+ "\n");
            resTxt.append(data.toString()+ "\n\n");
            if (!repeat.isEmpty()) {
                resTxt.append("包含的重复数据有："+ "\n" + repeat.toString()+"\n\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * 打开文件
     */
    private static void showFileOpenDialog(Component parent, JTextArea msgTextArea) {
        // 创建一个默认的文件选取器
        JFileChooser fileChooser = new JFileChooser();

        // 设置默认显示的文件夹为当前文件夹
        fileChooser.setCurrentDirectory(new File("."));

        // 设置文件选择的模式（只选文件、只选文件夹、文件和文件均可选）
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        // 设置是否允许多选
        fileChooser.setMultiSelectionEnabled(true);

        // 添加可用的文件过滤器（FileNameExtensionFilter 的第一个参数是描述, 后面是需要过滤的文件扩展名 可变参数）
        FileNameExtensionFilter xlsxFile = new FileNameExtensionFilter("xlsx(*.xlsx)", "xlsx");
        fileChooser.addChoosableFileFilter(xlsxFile);
        fileChooser.setFileFilter(xlsxFile);
        // 打开文件选择框（线程将被阻塞, 直到选择框被关闭）
        int result = fileChooser.showOpenDialog(parent);

        if (result == JFileChooser.APPROVE_OPTION) {
            // 如果点击了"确定", 则获取选择的文件路径
            File file = fileChooser.getSelectedFile();
            msgTextArea.setText("");
            msgTextArea.append(file.getAbsolutePath());
        }
    }

    /**
     * 警告消息框
     * @param mesg
     */
    public static void warningDialog(String mesg)
    {
        JOptionPane
                .showMessageDialog(
                        null,
                        "<html><font color=\"yellow\"  style=\"font-weight:bold;" +
                                "background-color:#666666\" >"
                                + mesg + "</font></html>", "警告",
                        JOptionPane.WARNING_MESSAGE);
    }

}
