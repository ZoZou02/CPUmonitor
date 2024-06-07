import com.sun.management.OperatingSystemMXBean;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.management.ManagementFactory;

/**
 * @ClassName : CPUMonitorOverview  //类名
 * @Description : CPU参数监控概述  //描述
 * @Author : ZoZou02 //作者
 * @Date: 2024/3/2  9:45
 */
public class CPUMonitorOverview extends JPanel implements ActionListener {
    private static final int WIDTH = 600;//窗口宽度
    private static final int HEIGHT = 400;//窗口高度
    private static final int MAX_DATA_POINTS = 100;//存储CPU数据数量
    private final Timer timer;//计时器
    OperatingSystemMXBean bean;     //JMX的操作系统信息
    SystemInfo systemInfo;    //系统信息
    HardwareAbstractionLayer hardware;    //硬件信息
    CentralProcessor processor;    //CPU
    OperatingSystem os;    //获取操作系统信息
    int CPU_PHYSICAL_PROCESSOR;    //CPU物理内核
    int CPU_LOGICAL_PROCESSOR;    //CPU逻辑处理器
    int[][] xLoadsPoints; // 折线的顶点x坐标
    int[][] yLoadsPoints; // 折线的顶点y坐标
    double[] cpuTotalData;    //总CPU使用数据
    int[] xTotalPoints; // 折线的顶点x坐标
    int[] yTotalPoints; // 折线的顶点y坐标
    int moveX = 0;//方格移动
    //构造函数，计时器每秒刷新一次，与任务管理器一致
    public CPUMonitorOverview() {
        bean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean(); //获取操作系统的相关信息
        systemInfo = new SystemInfo();    //获取系统信息
         hardware = systemInfo.getHardware();    //获取硬件信息
        processor = hardware.getProcessor();    //获取CPU
        os = systemInfo.getOperatingSystem();    //获取操作系统信息
        CPU_PHYSICAL_PROCESSOR = processor.getPhysicalProcessorCount();    //CPU物理内核
        CPU_LOGICAL_PROCESSOR = processor.getLogicalProcessorCount();    //CPU逻辑处理器
        xLoadsPoints = new int[CPU_LOGICAL_PROCESSOR][MAX_DATA_POINTS + 2]; // 折线的顶点x坐标
        yLoadsPoints = new int[CPU_LOGICAL_PROCESSOR][MAX_DATA_POINTS + 2]; // 折线的顶点y坐标
        xTotalPoints = new int[MAX_DATA_POINTS + 2]; // 折线的顶点x坐标（+2是为了存储底部两个顶点坐标，绘制的是多边形）
        yTotalPoints = new int[MAX_DATA_POINTS + 2]; // 折线的顶点y坐标
        cpuTotalData = new double[MAX_DATA_POINTS];
        timer = new Timer(1000, this); // 1秒更新一次
        timer.start();
    }
    // 创建绘图面板
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        int marginX = 30;
        int marginY = 40;

        //显示范围
        int recWidth = WIDTH - 105;
        int recHeight = HEIGHT / 2 - 60;
        g2.setFont(new Font("微软雅黑", Font.BOLD, 14));//设置字体
        //CPU总使用率
        //填充矩形
        g2.setColor(Color.black);
        g2.fillRect(marginX, marginY, recWidth, recHeight);

        // 获取坐标信息
        for (int i = 0; i < MAX_DATA_POINTS; i++) {
            int x = marginX + i * ((WIDTH - 100) / (MAX_DATA_POINTS - 1));
            int y = (HEIGHT / 2 - 20 - (int) cpuTotalData[i]);
            //防止图像超出矩形范围
            if (y < marginY) {
                y = marginY;
            }
            xTotalPoints[i] = x;
            yTotalPoints[i] = y;

            g2.setColor(Color.green);
            // 显示使用率数据值
            String dataPointStr = String.format("%.0f%%", cpuTotalData[MAX_DATA_POINTS - 1]);
            g2.setColor(Color.black);
            g2.drawString("CPU使用率: " + dataPointStr, marginX, marginY + 160);
        }
        // 填充
        xTotalPoints[MAX_DATA_POINTS] = marginX + recWidth;
        yTotalPoints[MAX_DATA_POINTS] = marginY + recHeight;
        xTotalPoints[MAX_DATA_POINTS + 1] = marginX;
        yTotalPoints[MAX_DATA_POINTS + 1] = marginY + recHeight;
        g2.setColor(new Color(0, 98, 0));
        g2.fillPolygon(xTotalPoints, yTotalPoints, MAX_DATA_POINTS + 2); // 通过填充部分顶点来填充折线的一边

        //画方格
        g2.setColor(new Color(5, 160, 8));
        for (int i = 0; i < 10; i++) {
            g2.drawLine(marginX, marginY + 14 * i, marginX + recWidth, marginY + 14 * i);
        }
        for (int i = 0; i < MAX_DATA_POINTS; i += 5) {
            g2.drawLine(xTotalPoints[i] + 20 - moveX, marginY, xTotalPoints[i] + 20 - moveX, marginY + recHeight);
        }
        //画线
        g2.setColor(Color.green);
        g2.drawPolyline(xTotalPoints, yTotalPoints, MAX_DATA_POINTS + 2);

        //显示cpu数据
        g2.setColor(Color.black);
        //显示CPU运行时间
        String cpuTime = getCpuTime();
        g2.drawString("正常运行时间：" + cpuTime, marginX, marginY + 180);

        //显示进程数
        String processCount = String.valueOf(os.getProcessCount());
        g2.drawString("进程数：" + processCount, marginX, marginY + 200);

        //显示线程数
        String threadCount = String.valueOf(os.getThreadCount());
        g2.drawString("线程数：" + threadCount, marginX, marginY + 220);

        //操作系统显示
        String osName = "OS：" + os;
        g2.drawString(osName, marginX, 20);

        //CPU型号显示
        String cpuName = "CPU：" + hardware.getProcessor();
        g2.drawString(cpuName, marginX, 35);

        //CPU核数显示
        String cpuPCores = "内核：" + hardware.getProcessor().getPhysicalProcessorCount();
        g2.drawString(cpuPCores, 350, marginY + 160);
        String cpuLCores = "逻辑处理器：" + hardware.getProcessor().getLogicalProcessorCount();
        g2.drawString(cpuLCores, 350, marginY + 180);

        //CPU频率显示（基准速度）
        double freq = processor.getVendorFreq() * 0.000000001; // 将频率转换为GHz
        String formattedFreq = String.format("基准速度：%.2fGHz", freq);
        g2.drawString(formattedFreq, 350, marginY + 200);
    }

    //每秒新增信息
    public void actionPerformed(ActionEvent e) {
        //获取CPU使用率
        for (int i = 0; i < MAX_DATA_POINTS - 1; i++) {
            cpuTotalData[i] = cpuTotalData[i + 1];
        }
        cpuTotalData[MAX_DATA_POINTS - 1] = getCpuUsage();
        //方格移动
        if (moveX != 20) {
            moveX += 5;
        } else {
            moveX = 0;
        }
        repaint();
    }

    //获取CPU使用率
    private double getCpuUsage() {
        return bean.getCpuLoad() * 100.0 * 2;   //乘于倍率
    }

    //获取CPU运行时间
    private String getCpuTime() {
        return FormatUtil.formatElapsedSecs(processor.getSystemUptime());
    }

}
