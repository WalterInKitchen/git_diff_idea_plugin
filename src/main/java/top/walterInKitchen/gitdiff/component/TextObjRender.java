package top.walterInKitchen.gitdiff.component;

import javax.swing.*;
import java.awt.*;

public class TextObjRender extends JLabel implements ListCellRenderer<Object> {
    public static TextObjRender getInstance() {
        return new TextObjRender();
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof TextObject) {
            setText(((TextObject) value).getText());
        } else {
            setText(value.toString());
        }
        return this;
    }
}
