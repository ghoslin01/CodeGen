package me.hehaiyang.codegen.setting;

import com.google.common.collect.Maps;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBDimension;
import lombok.Data;
import me.hehaiyang.codegen.model.CodeTemplate;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Map;

/**
 * Desc: 配置面板
 * Mail: hehaiyangwork@qq.com
 * Date: 2017/3/17
 */
@Data
public class FormatForm {

    /**
     * 主面板
     */
    private JPanel mainPanel;

    /**
     * 主页tab
     */
    private JTabbedPane tabbedPanel;

    /**
     * 自定义参数面板
     */
    private JPanel paramPanel;

    private DefaultTableModel tableModel;   //表格模型对象

    private JBTable table;

    /**
     *
     */
    private JTabbedPane templateTabbedPane;

    private JPanel extraPanel;
    private JButton help;

    private Map<String, TemplateEditPane> editPaneMap;

    private void createUIComponents() {
        // TODO: place custom component creation code here

        paramPanel = new JPanel();
        paramPanel.setLayout(new BorderLayout());
        paramPanel.setSize(500, 400);

        table = new JBTable();
        table.setSize(300,200);
        // 单选
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 支持滚动
        JScrollPane scrollPane = new JBScrollPane(table);
        scrollPane.setViewportView(table);
        paramPanel.add(scrollPane, BorderLayout.CENTER);

        help = new JButton("help");
        help.setIcon(AllIcons.General.Help);
        help.setBounds(0, 0, 85, 82);
        help.setContentAreaFilled(false);
        help.addActionListener(helpbroker());
    }

    public FormatForm(FormatSetting settings) {

        // 模版配置面板
        templateTabbedPane = new JBTabbedPane();
        editPaneMap = Maps.newHashMap();
        resetTabPane(settings);
        tabbedPanel.add("模版配置", templateTabbedPane);

        // 自定义参数 操作面板
        resetParams(settings);

        paramPanel.add(actionPanel(),BorderLayout.SOUTH);


    }

    /**
     * 模版配置Tab
     * @param settings
     */
    private void resetTabPane(FormatSetting settings) {
        settings.getCodeTemplates().forEach((key, value) -> {
            TemplateEditPane editPane = new TemplateEditPane(settings, key);
            templateTabbedPane.addTab(key, editPane.getTemplatePanel());
            editPaneMap.put(key, editPane);
        });
    }

    private void resetParams(FormatSetting settings){
        // 列名
        String[] columnNames = {"参数名","参数值"};

        // 默认数据
        Map<String, String> map = settings.getParams();
        Object[][] tableVales = new String[map.size()][2];
        Object[] keys = map.keySet().toArray();
        Object[] values = map.values().toArray();
        for (int row = 0; row < tableVales.length; row++) {
            tableVales[row][0] = keys[row];
            tableVales[row][1] = values[row];
        }

        tableModel = new DefaultTableModel(tableVales,columnNames);
        table.setModel(tableModel);
    }

    private JPanel actionPanel(){
        JPanel actionPanel = new JPanel();
        // 添加参数
        final JButton addButton = new JButton("添加");
        addButton.addActionListener(e -> {
            String []rowValues = {"NULL", "NULL"};
            tableModel.addRow(rowValues);
        });
        actionPanel.add(addButton);

        // 删除参数
        final JButton delButton = new JButton("删除");
        delButton.addActionListener( e ->{
            int selectedRow = table.getSelectedRow();
            if(selectedRow!=-1){
                tableModel.removeRow(selectedRow);
            }else{
                Messages.showMessageDialog("请选择一行参数", "警告", Messages.getWarningIcon());
            }
        });
        actionPanel.add(delButton);
        return actionPanel;
    }


    private ActionListener helpbroker(){
        HelpSet helpset;
        ClassLoader loader = this.getClass().getClassLoader();
        URL url = HelpSet.findHelpSet(loader, "javahelp/hello.hs");
        try {
            helpset = new HelpSet(loader, url);
        } catch (HelpSetException e) {
            System.err.println("Error loading HelpSet");
            return null;
        }

        HelpBroker helpbroker = helpset.createHelpBroker();
        helpbroker.setViewDisplayed(true);
        helpbroker.setSize(new JBDimension(800, 600));
        helpbroker.setLocation(getCenterPoint(800,600));
//        helpbroker.setFont(new Font("宋体",Font.BOLD,14));
        return new CSH.DisplayHelpFromSource(helpbroker);
    }

    /**
     * 文档居中处理
     * @param frameW 文档宽度
     * @param frameH 文档高度
     * @return
     */
    public Point getCenterPoint(int frameW,int frameH){

        Toolkit toolkit=Toolkit.getDefaultToolkit();
        Dimension dimension=toolkit.getScreenSize();
        Integer screenW = dimension.width;
        Integer screenH = dimension.height;

        Integer centerX = screenW/2-frameW/2;
        Integer centerY = screenH/2-frameH/2;

        return new Point(centerX,centerY);
    }

    public Map<String, CodeTemplate> getTabTemplates() {
        Map<String, CodeTemplate> map = Maps.newHashMap();
        editPaneMap.forEach((key, value) -> {
            CodeTemplate codeTemplate = new CodeTemplate(value.getTemplateName().getText(),
                    value.getTemplateType().getText(), value.getFileName().getText(),
                    value.getEditor().getDocument().getText());
            map.put(codeTemplate.getName(), codeTemplate);
        });
        return map;
    }

    public void refresh(FormatSetting settings) {
        templateTabbedPane.removeAll();
        editPaneMap.clear();
        resetTabPane(settings);
        resetParams(settings);
    }

}
