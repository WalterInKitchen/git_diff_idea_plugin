package top.walterInKitchen.gitdiff.component;

import lombok.Getter;

import javax.swing.*;

/**
 * @Author: walter
 * @Date: 2021/11/25
 **/
@Getter
public class TwoBranchCompare extends JFrame {
    private JPanel mainPanel;
    private JComboBox<Branch> branchBox1;
    private JComboBox<Branch> branchBox2;
    private JLabel diffLabel;
    private JComboBox<Remote> remoteBox2;
    private JComboBox<Remote> remoteBox1;
    private JPanel branchPanel;
    private JPanel changesPanel;
}
