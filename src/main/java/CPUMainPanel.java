import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * @ClassName : CPUMainPanel  //类名
 * @Description : 主函数  //描述
 * @Author : ZoZou02 //作者
 * @Date: 2024/3/1  15:02
 */
public class CPUMainPanel extends JPanel {
    public CPUMainPanel() {
        super(new GridLayout(1, 1));
        JTabbedPane tabbedPane = new JTabbedPane();
        //概述容器
        JPanel panel = new JPanel();
        //CPU概述
        JComponent overviewpPanel = new CPUMonitorOverview();
        overviewpPanel.setBorder(BorderFactory.createEtchedBorder());
        //CPU列表
        JComponent cpuListPanel = new CPUMonitorList();
        JScrollPane scrollPane1 = new JScrollPane(
                cpuListPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        //增加滚动速度
        scrollPane1.getVerticalScrollBar().setBlockIncrement(64);
        scrollPane1.getVerticalScrollBar().setUnitIncrement(16);
        //进程概述表格
        JComponent cpuProcessListTable = new CPUProcessListTable();
        cpuProcessListTable.setPreferredSize(new Dimension(580, 200));
        overviewpPanel.setPreferredSize(new Dimension(600, 300));
        panel.add(overviewpPanel);
        panel.add(cpuProcessListTable);
        tabbedPane.addTab("概述", panel);
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
        tabbedPane.addTab("CPU", scrollPane1);
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
        add(tabbedPane);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("CPU实时监测");
        ImageIcon icon=new ImageIcon("icon/icon.png");
        frame.setIconImage(icon.getImage());
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new CPUMainPanel());
        frame.setVisible(true);
    }
}
