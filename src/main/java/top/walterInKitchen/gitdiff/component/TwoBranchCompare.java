package top.walterInKitchen.gitdiff.component;

import javax.swing.*;

/**
 * @Author: walter
 * @Date: 2021/11/25
 **/
public class TwoBranchCompare extends JFrame {
    private JPanel mainPanel;
    private JComboBox<String> comboBox1;
    private JComboBox<String> comboBox2;
    private JLabel diffLabel;

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JComboBox<String> getComboBox1() {
        return comboBox1;
    }

    public JComboBox<String> getComboBox2() {
        return comboBox2;
    }

    public JLabel getDiffLabel() {
        return diffLabel;
    }
}
