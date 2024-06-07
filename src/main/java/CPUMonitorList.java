import com.sun.management.OperatingSystemMXBean;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.management.ManagementFactory;

/**
 * @ClassName : CPUMonitorList  //类名
 * @Description : 每个CPU的数据获取和图表绘制  //描述
 * @Author : ZoZou02 //作者
 * @Date: 2024/3/4  15:00
 */
public class CPUMonitorList extends JPanel implements ActionListener {
    private static final int WIDTH = 600;//窗口宽度
    private static final int HEIGHT = 400;//窗口高度
    private static final int MAX_DATA_POINTS = 100;//存储CPU数据数量
    private final Timer timer;//计时器
    private OperatingSystemMXBean bean;
    private SystemInfo systemInfo;    //获取系统信息
    private HardwareAbstractionLayer hardware;    //获取硬件信息
    private CentralProcessor processor ;    //获取CPU
    private int CPU_PHYSICAL_PROCESSOR;    //CPU物理内核
    private int CPU_LOGICAL_PROCESSOR;    //CPU逻辑处理器
    private int[][] xLoadsPoints; // 折线的顶点x坐标
    private int[][] yLoadsPoints; // 折线的顶点y坐标
    private OperatingSystem os;    //获取操作系统信息
    private double[][] cpuLoads; //每个逻辑CPU使用率存储数组
    private double[] cpuTotalData;    //总CPU使用数据
    private int[] xTotalPoints; // 折线的顶点x坐标
    private int[] yTotalPoints; // 折线的顶点y坐标
    private int moveX = 0;//方格移动

    //构造函数，计时器每秒刷新一次，与任务管理器一致
    public CPUMonitorList() {
        bean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        systemInfo = new SystemInfo();    //获取系统信息
        hardware = systemInfo.getHardware();    //获取硬件信息
        processor = hardware.getProcessor();    //获取CPU
        CPU_PHYSICAL_PROCESSOR = processor.getPhysicalProcessorCount();    //CPU物理内核
        CPU_LOGICAL_PROCESSOR = processor.getLogicalProcessorCount();    //CPU逻辑处理器
        xLoadsPoints = new int[CPU_LOGICAL_PROCESSOR][MAX_DATA_POINTS + 2]; // 折线的顶点x坐标
        yLoadsPoints = new int[CPU_LOGICAL_PROCESSOR][MAX_DATA_POINTS + 2]; // 折线的顶点y坐标
        os = systemInfo.getOperatingSystem();    //获取操作系统信息
        xTotalPoints = new int[MAX_DATA_POINTS + 2]; // 折线的顶点x坐标
        yTotalPoints = new int[MAX_DATA_POINTS + 2]; // 折线的顶点y坐标
        cpuTotalData = new double[MAX_DATA_POINTS];
        cpuLoads = new double[CPU_LOGICAL_PROCESSOR][MAX_DATA_POINTS];
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
            int x = marginX + i * ((recWidth) / (MAX_DATA_POINTS - 1));         //i在里面在外面会影响数据大小
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
            g2.drawString("CPU - 总计使用率: " + dataPointStr, 40, 30);
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

        //其他逻辑处理器使用率
        for (int i = 0; i < CPU_LOGICAL_PROCESSOR; i++) {
            //填充黑色矩形
            g2.setColor(Color.black);
            g2.fillRect(marginX, marginY + 175 * (i + 1), recWidth, recHeight);    //i+1  给前面总的CPU使用率挪位置
        }

        // 绘制每个CPU的使用率折线图
        g2.setColor(Color.green);
        for (int i = 0; i < CPU_LOGICAL_PROCESSOR; i++) {
            // 获取坐标信息
            for (int j = 0; j < MAX_DATA_POINTS; j++) {
                int x = marginX + j * (recWidth / (MAX_DATA_POINTS - 1));//i在里面在外面会影响数据大小
                int y = (HEIGHT / 2 - 20 - (int) (cpuLoads[i][j] * HEIGHT / 2)) + 175 * (i + 1);

                //防止图像超出矩形范围
                if (y < marginY + 175 * (i + 1)) {
                    y = marginY + 175 * (i + 1);
                }
                xLoadsPoints[i][j] = x;
                yLoadsPoints[i][j] = y;
                //显示使用率
                String dataPointStr = String.format("%.0f%%", cpuLoads[i][MAX_DATA_POINTS - 1] * 100);
                g2.setColor(Color.black);
                g2.drawString("CPU " + i + " 使用率: " + dataPointStr, 40, 30 + 175 * (i + 1));
            }
        }

        for (int i = 0; i < CPU_LOGICAL_PROCESSOR; i++) {
            xLoadsPoints[i][MAX_DATA_POINTS] = marginX + recWidth;
            yLoadsPoints[i][MAX_DATA_POINTS] = marginY + recHeight + 175 * (i + 1);
            xLoadsPoints[i][MAX_DATA_POINTS + 1] = marginX;
            yLoadsPoints[i][MAX_DATA_POINTS + 1] = marginY + recHeight + 175 * (i + 1);

            //填充
            g2.setColor(new Color(0, 98, 0));
            g2.fillPolygon(xLoadsPoints[i], yLoadsPoints[i], MAX_DATA_POINTS + 2);
            //画方格
            g2.setColor(new Color(5, 160, 8));
            for (int j = 0; j < 10; j++) {
                g2.drawLine(marginX, marginY + 175 * (i + 1) + 14 * j, marginX + recWidth, marginY + 175 * (i + 1) + 14 * j);
            }
            for (int j = 0; j < MAX_DATA_POINTS; j += 5) {
                g2.drawLine(xLoadsPoints[i][j] + 20 - moveX, marginY + 175 * (i + 1), xLoadsPoints[i][j] + 20 - moveX, marginY + 175 * (i + 1) + recHeight);
            }
            //画线
            g2.setColor(Color.green);
            g2.drawPolyline(xLoadsPoints[i], yLoadsPoints[i], MAX_DATA_POINTS + 2);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 3000); // 设置绘图区域的大小
    }

    //每秒新增信息
    public void actionPerformed(ActionEvent e) {
        //获取总CPU使用率
        for (int i = 0; i < MAX_DATA_POINTS - 1; i++) {
            cpuTotalData[i] = cpuTotalData[i + 1];
        }
        cpuTotalData[MAX_DATA_POINTS - 1] = getCpuUsage() * 2;             //乘于一个倍率

        //分别获取每个逻辑处理器使用率
        double[] load = processor.getProcessorCpuLoadBetweenTicks();
        int flag = 0;
        for (double avg : load) {
            // 显示使用率数据值
            for (int j = 0; j < MAX_DATA_POINTS - 1; j++) {
                cpuLoads[flag][j] = cpuLoads[flag][j + 1];   //将之前的数据往前移动
            }
            cpuLoads[flag][MAX_DATA_POINTS - 1] = avg; //新数据放在数组末尾
            flag++;
        }
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
        return bean.getCpuLoad() * 100.0;
    }
}
