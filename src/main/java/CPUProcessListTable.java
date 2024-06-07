import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;

/**
 * @ClassName : CPUProcessListTable  //类名
 * @Description : 实时进程表格  //描述
 * @Author : ZoZou02 //作者
 * @Date: 2024/3/3  15:02
 */
public class CPUProcessListTable extends JPanel implements ActionListener {
    private static final int MAX_DATA_POINTS = 400;//存储进程数量
    private final Timer timer;//计时器
    String[][] processList;//存储进程列表
    String[][] servicesList;//存储服务列表
    String[][] consoleList;//存储应用列表
    String[] columns;//表头
    private JTable table;
    private DefaultTableModel model;
    private JTable serviceTable;
    private DefaultTableModel servicemodel;
    private JTable consoleTable;
    private DefaultTableModel consolemodel;
    JPopupMenu m_popupMenu;   //右键菜单
    int selectedRow;        //选中的行数
    String selectedPid;       //选中进程的pid

    //构造函数，计时器每秒刷新一次，与任务管理器一致
    public CPUProcessListTable() {
        setLayout(new BorderLayout());
        processList = new String[MAX_DATA_POINTS][5];
        servicesList = new String[MAX_DATA_POINTS][5];
        consoleList = new String[MAX_DATA_POINTS][5];

        columns = new String[]{"名称", "PID", "会话名", "CPU", "内存使用"};
        model = new DefaultTableModel(processList, columns);
        table = new JTable(model);

        servicemodel = new DefaultTableModel(servicesList, columns);
        serviceTable = new JTable(servicemodel);
        consolemodel = new DefaultTableModel(consoleList, columns);
        consoleTable = new JTable(consolemodel);

        createPopupMenu();
        Comparator<String> customComparator = createCustomComparator();

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
        TableRowSorter<TableModel> serviceSorter = new TableRowSorter<>(servicemodel);
        TableRowSorter<TableModel> consoleSorter = new TableRowSorter<>(consolemodel);

        sorter.setComparator(1,customComparator);
        sorter.setComparator(4,customComparator);
        serviceSorter.setComparator(1,customComparator);
        serviceSorter.setComparator(4,customComparator);
        consoleSorter.setComparator(1,customComparator);
        consoleSorter.setComparator(4,customComparator);

        table.setRowSorter(sorter);
        serviceTable.setRowSorter(serviceSorter);
        consoleTable.setRowSorter(consoleSorter);
        //添加点击事件
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                MouseClicked(evt);
            }
        });

        //添加表格组件
        JScrollPane pane = new JScrollPane(table);
        pane.getVerticalScrollBar().setBlockIncrement(64);
        pane.getVerticalScrollBar().setUnitIncrement(16);

        JScrollPane servicePane = new JScrollPane(serviceTable);
        servicePane.getVerticalScrollBar().setBlockIncrement(64);
        servicePane.getVerticalScrollBar().setUnitIncrement(16);

        JScrollPane consolePane = new JScrollPane(consoleTable);
        consolePane.getVerticalScrollBar().setBlockIncrement(64);
        consolePane.getVerticalScrollBar().setUnitIncrement(16);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("总进程", pane);
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
        tabbedPane.addTab("服务进程", servicePane);
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
        tabbedPane.addTab("应用进程", consolePane);
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_3);
        add(tabbedPane, BorderLayout.CENTER);

        timer = new Timer(1000, this); // 1秒更新一次
        timer.start();
    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        super.setPreferredSize(preferredSize);
    }

    //每秒新增信息
    public void actionPerformed(ActionEvent e) {
        selectedRow = table.getSelectedRow();//选中的行
        if (selectedRow != -1) {
            Object o = table.getModel().getValueAt(selectedRow,1);
            selectedPid = o.toString().replaceAll("\\s", "");
        }

        //获取进程信息
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("tasklist");
            Process process = processBuilder.start();

            // 读取命令输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            //清空列表
            model.setRowCount(0);
            servicemodel.setRowCount(0);
            consolemodel.setRowCount(0);

            for (int i = 0; i < MAX_DATA_POINTS; i++) {
                line = reader.readLine();
                if (line != null && i >= 3) {                    //前三行没有实际数据
                    processList[i - 3][0] = line.substring(0, 24);       //名称
                    processList[i - 3][1] = line.substring(26, 34);      //PID
                    processList[i - 3][2] = line.substring(35, 43);      //会话
                    processList[i - 3][3] = line.substring(62, 63);      //CPU
                    processList[i - 3][4] = line.substring(63, 76);       //内存占用
                    model.addRow(processList[i - 3]);                    //将读取到的添加进table

                    if (line.substring(35, 43).replaceAll("\\s", "").equals("Console")) {
                        consolemodel.addRow(processList[i - 3]);
                    } else {
                        servicemodel.addRow(processList[i - 3]);
                    }
                }
            }
            process.destroy();                     //结束命令行进程
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        updatePanelSize();
        // 重新绘制表格
        revalidate();
        repaint();
    }
    //点击事件
    private void MouseClicked(java.awt.event.MouseEvent evt) {
        mouseRightButtonClick(evt);
    }

    //鼠标右键点击事件
    private void mouseRightButtonClick(java.awt.event.MouseEvent evt) {
        //判断是否为鼠标的BUTTON3按钮，BUTTON3为鼠标右键
        if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
            //通过点击位置找到点击为表格中的行
            int focusedRowIndex = table.rowAtPoint(evt.getPoint());
            if (focusedRowIndex == -1) {
                return;
            }
            //将表格所选项设为当前右键点击的行
            table.setRowSelectionInterval(focusedRowIndex, focusedRowIndex);
            //弹出菜单
            m_popupMenu.show(table, evt.getX(), evt.getY());
        }

    }

    //排序比较器
    private static Comparator<String> createCustomComparator() {
        return new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                try {
                    Double d1 = Double.parseDouble(o1.replaceAll("[,\\sK]", ""));
                    Double d2 = Double.parseDouble(o2.replaceAll("[,\\sK]", ""));
                    return d1.compareTo(d2);
                } catch (NumberFormatException e) {
                    return 0; // 如果转换出错则返回0
                }
            }
        };
    }

    //创建一个JPopupMenu()
    private void createPopupMenu() {
        m_popupMenu = new JPopupMenu();
        JMenuItem delMenItem = new JMenuItem();
        delMenItem.setText("  结束进程  ");
        delMenItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //该操作需要做的事
                try {
                    killProcessByPid(selectedPid);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        m_popupMenu.add(delMenItem);
    }

    // 根据Pid将进程干掉
    public static void killProcessByPid(String pid) throws Exception {
        Runtime.getRuntime().exec("taskkill /F /PID " + pid);
    }

    //更新Panel大小(无)
    private void updatePanelSize() {
        setPreferredSize(new Dimension(580, 200));
        revalidate();
    }
}
