/*
 * KloC - Java Companion App
 * Copyright (C) 2017  TheSilkMiner
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact information:
 * E-mail: thesilkminer <at> outlook <dot> com
 */
package net.thesilkminer.arduino.kloc.crash;

import com.google.common.base.Preconditions;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

/**
 * The frame used to display crash information.
 *
 * @author TheSilkMiner
 * @since 1.0
 */
public final class CrashFrame extends JFrame {

    /*
     * We need to use Java Swing because we don't know when exactly
     * the crash may be thrown. It is *not* safe to assume that the
     * crash is thrown during or after initialization, when the JavaFX
     * context is ready to be used safely. Crashes may in fact happen
     * at any moment.
     *
     * One may argue that in that case a Swing dialog may be sufficient,
     * whereas after initialization a fully-fledged JavaFX window should
     * be used. According to the DRY principle, though, I think this
     * is a bad idea when one can develop a system that works for every
     * possible case. Also, a dialog is not enough, because we need buttons
     * for ease of reporting and file locating.
     *
     * Also, using a JavaFX window is out of question because we shut down
     * the entire Platform used by it when we handle the exception, so the
     * window can't be drawn. We do this so that every operation is
     * immediately stopped and processing does not continue: everything may
     * in fact lead to worse problems.
     */

    // If Nimbus is not yet loaded, then the entire graphic environment
    // is going to look like shit. At least we can warn the user, I hope
    // Unless the warning text fucks up anyway
    public static volatile boolean hasNimbus = false;

    private final String stringReport;
    private final JFrame loweredFrameWarning;

    private JTextArea crashReportArea;
    private JLabel nimbusWarningLabel;
    private JLabel directoryLabel;
    private JButton supportButton;
    private JButton dirOpenButton;
    private JButton copyButton;
    private JButton closeButton;
    private JPanel container;
    private JPanel nimbusWarningPanel;

    private CrashFrame(@Nonnull final CrashReport report) {
        if (hasNimbus) this.nimbusWarningPanel.setVisible(false);

        this.stringReport = report.toString();
        this.loweredFrameWarning = new JFrame() {
            {
                this.setTitle("");
                this.setLayout(null);
                this.setMinimumSize(new Dimension(280, 100));
                final JLabel icon = new JLabel();
                icon.setIcon(new ImageIcon(this.getClass().getResource("/com/sun/deploy/resources/image/warning48.png")));
                icon.setBounds(new Rectangle(10, 10, 50, 50));
                this.add(icon);
                final JLabel text = new JLabel("Crash screen currently minimized");
                text.setHorizontalAlignment(SwingConstants.LEFT);
                text.setVerticalAlignment(SwingConstants.TOP);
                text.setBounds(new Rectangle(70, 10, 270, 50));
                this.add(text);
                this.setResizable(false);
                this.setUndecorated(false);
                this.setLocationRelativeTo(null);
                this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                this.setVisible(true);
            }
        };

        this.crashReportArea.setText(this.stringReport);
        this.crashReportArea.setTabSize(2);
        this.crashReportArea.setBackground(new Color(40, 40, 40).brighter().brighter());

        this.supportButton.addActionListener(e -> {
            try {
                //noinspection SpellCheckingInspection
                this.connectTo("https://github.com/ixd-plus/arduino-course/issues/new");
            } catch (final URISyntaxException | IOException t) {
                final JDialog v = new CrashFrameErrorDialog("opening support website", t, this);
                v.toFront();
            }
        });

        this.dirOpenButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(new File(System.getProperty("user.dir"), "crash-reports"));
            } catch (final IOException t) {
                final JDialog s = new CrashFrameErrorDialog("opening the crash-reports directory", t, this);
                s.toFront();
            }
        });

        this.copyButton.addActionListener(e -> this.copyEntireCrashReport());
        this.copyButton.addMouseListener(new MouseAdapter() {
            private final String originalText = CrashFrame.this.copyButton.getText();

            @Override
            public void mouseExited(final MouseEvent e) {
                CrashFrame.this.copyButton.setText(originalText);
            }
        });

        this.closeButton.addActionListener(e -> {
            this.dispose();
            this.loweredFrameWarning.dispose();
        });

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(final WindowEvent e) {
                CrashFrame.this.loweredFrameWarning.dispose();
            }
        });

        this.setUndecorated(true);
        this.setTitle(hasNimbus ? "KloC has crashed!" : this.nimbusWarningLabel.getText());
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setContentPane(this.container);
        this.pack();

        // Fix button text fucking the button dimension up
        this.copyButton.setMinimumSize(this.copyButton.getSize());
        this.pack();
    }

    @Nonnull
    static CrashFrame obtain(@Nonnull final CrashReport report) {
        final CrashFrame frame = new CrashFrame(report);
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(frame);
        return frame;
    }

    @SuppressWarnings("SameParameterValue")
    private void connectTo(final String protocol) throws IOException, URISyntaxException {
        this.connectTo(new URL(protocol));
    }

    private void connectTo(final URL url) throws IOException, URISyntaxException {
        this.connectTo0(url);
        Desktop.getDesktop().browse(url.toURI());
    }

    private void connectTo0(@Nonnull final URL url) throws IOException {
        // Helper method, use it to check preconditions needed to connect
        // For now just bounces to #checkInternetConnection, but in the future
        // more detailed implementations may be possible
        Preconditions.checkNotNull(url);
        this.checkInternetConnection(url);
    }

    private void checkInternetConnection(@Nonnull final URL url) throws IOException {
        try {
            final URLConnection connection = url.openConnection();
            connection.connect();
        } catch (final IOException e) {
            throw new IOException("No Internet connection available", e);
        }
    }

    private void copyEntireCrashReport() {
        if ("Copied!".equals(this.copyButton.getText())) return;
        this.crashReportArea.requestFocus();
        this.crashReportArea.setCaretPosition(this.crashReportArea.getDocument().getLength());
        this.crashReportArea.moveCaretPosition(0);
        final StringSelection sel = new StringSelection(this.stringReport);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
        this.copyButton.setText("Copied!");
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        container = new JPanel();
        container.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        container.setMinimumSize(new Dimension(600, 400));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        container.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$("Roboto Slab", Font.BOLD, 36, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setForeground(new Color(-5197824));
        label1.setText("KloC has crashed!");
        panel2.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        directoryLabel = new JLabel();
        Font directoryLabelFont = this.$$$getFont$$$("Roboto", -1, -1, directoryLabel.getFont());
        if (directoryLabelFont != null) directoryLabel.setFont(directoryLabelFont);
        directoryLabel.setText("This crash has been saved to the crash-reports directory");
        panel2.add(directoryLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel1.add(spacer2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        container.add(panel3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setHorizontalScrollBarPolicy(32);
        scrollPane1.setVerticalScrollBarPolicy(22);
        panel3.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-4473925)), null));
        crashReportArea = new JTextArea();
        crashReportArea.setEditable(false);
        Font crashReportAreaFont = this.$$$getFont$$$("Roboto Mono", -1, -1, crashReportArea.getFont());
        if (crashReportAreaFont != null) crashReportArea.setFont(crashReportAreaFont);
        crashReportArea.setLineWrap(false);
        crashReportArea.setMargin(new Insets(10, 10, 10, 10));
        crashReportArea.setSelectionColor(new Color(-14145496));
        crashReportArea.setText("-- Kloc Crash Report zone --");
        scrollPane1.setViewportView(crashReportArea);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        container.add(panel4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        supportButton = new JButton();
        Font supportButtonFont = this.$$$getFont$$$("Roboto", -1, -1, supportButton.getFont());
        if (supportButtonFont != null) supportButton.setFont(supportButtonFont);
        supportButton.setText("Ask the developer");
        panel4.add(supportButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dirOpenButton = new JButton();
        Font dirOpenButtonFont = this.$$$getFont$$$("Roboto", -1, -1, dirOpenButton.getFont());
        if (dirOpenButtonFont != null) dirOpenButton.setFont(dirOpenButtonFont);
        dirOpenButton.setText("Open crash-reports directory");
        panel4.add(dirOpenButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        copyButton = new JButton();
        Font copyButtonFont = this.$$$getFont$$$("Roboto", -1, -1, copyButton.getFont());
        if (copyButtonFont != null) copyButton.setFont(copyButtonFont);
        copyButton.setText("Copy Crash Report");
        panel4.add(copyButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        closeButton = new JButton();
        Font closeButtonFont = this.$$$getFont$$$("Roboto", -1, -1, closeButton.getFont());
        if (closeButtonFont != null) closeButton.setFont(closeButtonFont);
        closeButton.setText("Close");
        panel4.add(closeButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel4.add(spacer3, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        nimbusWarningPanel = new JPanel();
        nimbusWarningPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        nimbusWarningPanel.setBackground(new Color(-16777216));
        container.add(nimbusWarningPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        nimbusWarningLabel = new JLabel();
        Font nimbusWarningLabelFont = this.$$$getFont$$$("Roboto", -1, -1, nimbusWarningLabel.getFont());
        if (nimbusWarningLabelFont != null) nimbusWarningLabel.setFont(nimbusWarningLabelFont);
        nimbusWarningLabel.setForeground(new Color(-256));
        nimbusWarningLabel.setText("Java Swing context was not initalized properly: this may lead to graphical glitches and other oddities");
        nimbusWarningPanel.add(nimbusWarningLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return container;
    }
}
