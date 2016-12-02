/*
 *
 * Copyright 2015-2016 magiclen.org
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.magiclen.wordstransformerfx;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Optional;
import java.util.StringTokenizer;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Separator;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Words Transformer FX
 *
 * @author Magic Len
 */
public class WordsTransformerFX extends Application {

    // -----Class Constant-----
    private static final int SCREEN_WIDTH, SCREEN_HEIGHT;

    // -----Initial Static-----
    static {
        final Screen mainScreen = Screen.getPrimary();
        final Rectangle2D screenRectangle = mainScreen.getBounds();
        SCREEN_WIDTH = (int) screenRectangle.getWidth();
        SCREEN_HEIGHT = (int) screenRectangle.getHeight();
    }

    // -----Class Method-----
    /**
     * The initiation of this program.
     *
     * @param args not used
     */
    public static void main(final String[] args) {
        launch(args);
    }

    // -----Object Constant-----
    /**
     * The default value of width.
     */
    private final int WIDTH = 450;
    /**
     * The default value of Height.
     */
    private final int HEIGHT = 400;
    /**
     * The default distance of controls.
     */
    private final int GAP = 5;
    /**
     * The default vertical padding of the main frame.
     */
    private final int PADDING_GAP_VERTICAL = 15;
    /**
     * The default horizontal padding of the main frame.
     */
    private final int PADDING_GAP_HORIZONTAL = 30;
    /**
     * The default size of text.
     */
    private final float FONT_SIZE = 18f;

    // -----Object Variable-----
    /**
     * The font of text.
     */
    private Font font;
    /**
     * The margin of controls.
     */
    private Insets insets;
    /**
     * The padding of the main frame.
     */
    private Insets padding;
    /**
     * The stage of this application.
     */
    private Stage MAIN_STAGE;
    /**
     * The scene of this application.
     */
    private Scene MAIN_SCENE;
    /**
     * The root panel of controls.
     */
    private VBox MAIN_ROOT;
    private Button bNext, bFind, bPaste, bCopy, bDelete, bAdd;
    private TextField tfKey, tfFind, tfAllRight, tfLeft, tfRight;
    private Label lEqual, lCount, lAuthor;
    private BorderPane bpSearch, bpCopy, bpEdit, bpBottom;
    private HBox hbEdit;
    private Separator sDivider;

    private Data data;
    private int findIndex = -1;

    // -----Initial Instance-----
    {
        try {
            final String path = Class.forName("org.magiclen.wordstransformerfx.WordsTransformerFX").getProtectionDomain().getCodeSource().getLocation().getPath();
            final File file = new File(URLDecoder.decode(path, "UTF-8")).getAbsoluteFile();
            if (!file.exists()) {
                throw new Exception();
            }
            final File parent = file.getParentFile();
            if (parent == null) {
                throw new Exception();
            }
            data = new Data(new File(parent, "WordsData").getAbsolutePath());
        } catch (final Exception ex) {
            data = new Data(new File("WordsData").getAbsolutePath());
        }
    }

    // -----Object Method-----
    /**
     * Show an confirm dialog.
     *
     * @param title input the title
     * @param header input the header
     * @param content input the content
     * @return yes or no
     */
    private boolean showConfirmDialog(final String title, final String header, final String content) {
        final Optional<ButtonType> opt = showAlertDialog(AlertType.CONFIRMATION, title, header, content);
        final ButtonType rtn = opt.get();
        return rtn == ButtonType.OK;
    }

    /**
     * Show an alert dialog.
     *
     * @param type input the alert type
     * @param title input the title
     * @param header input the header
     * @param content input the content
     * @return the button that user clicked
     */
    private Optional<ButtonType> showAlertDialog(final Alert.AlertType type, final String title, final String header, final String content) {
        final Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        fixAlertHeight(alert);
        MAIN_STAGE.setAlwaysOnTop(false);
        final Optional<ButtonType> rtn = alert.showAndWait();
        MAIN_STAGE.setAlwaysOnTop(true);
        return rtn;
    }

    /**
     * Fix the height of an alert dialog.
     *
     * @param alert input an alert dialog
     */
    private void fixAlertHeight(final Alert alert) {
        alert.getDialogPane().getChildren().stream().filter(node -> node instanceof Label).forEach(node -> {
            final Label label = ((Label) node);
            label.setFont(font);
            label.setMinHeight(Region.USE_PREF_SIZE);
        });
    }

    /**
     * Search.
     */
    private void search() {
        final String str = tfKey.getText().trim();
        if (str.equals("")) {
            return;
        }
        findIndex = data.find(str);
        final boolean notFound = findIndex == -1;
        if (notFound) {
            tfFind.setText("---Cannot find the word!---");
        } else {
            tfFind.setText(data.get(findIndex));
            tfAllRight.setText(data.getLeft(findIndex).concat(" = ").concat(data.getAllRight(findIndex)));
        }
        bCopy.setDisable(notFound);
        bNext.setDisable(notFound);
        bDelete.setDisable(notFound);
    }

    /**
     * Add events.
     */
    private void addActions() {
        bCopy.setOnAction(e -> {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(tfFind.getText());
            clipboard.setContent(content);
        });

        bFind.setOnAction(e -> {
            search();
        });

        bPaste.setOnAction(e -> {
            try {
                final Clipboard clipboard = Clipboard.getSystemClipboard();
                final String text = clipboard.getString();
                tfKey.setText(text.trim());
                search();
            } catch (final Exception ex) {
                showAlertDialog(AlertType.WARNING, "Hint", null, "There is a problem while pasting a word from the clipboard. The data in the clipboard is not a String data!");
            }
        });

        bNext.setOnAction(e -> {
            final String keyword = tfKey.getText().trim();
            if (keyword.equals("")) {
                return;
            }
            findIndex = data.findNext(keyword, findIndex);
            tfFind.setText(data.get(findIndex));
            tfAllRight.setText(data.getLeft(findIndex).concat(" = ").concat(data.getAllRight(findIndex)));
        });

        bDelete.setOnAction(e -> {
            final boolean yesOrNo = showConfirmDialog("Question", "Are you sure killing the data?", "It means that you want to make the data remove from the database.");
            if (yesOrNo) {
                final boolean b = data.delete(findIndex);
                if (b) {
                    tfKey.setText("");
                    lCount.setText(String.valueOf(data.count()));
                }
            }
        });

        bAdd.setOnAction(e -> {
            final boolean b = data.add(tfLeft.getText().trim(), tfRight.getText().trim());
            if (b) {
                tfLeft.setText("");
                tfRight.setText("");
                lCount.setText(String.valueOf(data.count()));
                search();
            }
        });

        tfKey.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                search();
            }
        });

        tfKey.textProperty().addListener(e -> {
            tfFind.setText("");
            tfAllRight.setText("");
            bCopy.setDisable(true);
            bNext.setDisable(true);
            bDelete.setDisable(true);
        });

        final InvalidationListener ilEdit = e -> {
            bAdd.setDisable(tfLeft.getText().trim().equals("") || tfRight.getText().trim().equals(""));
        };

        tfLeft.textProperty().addListener(ilEdit);
        tfRight.textProperty().addListener(ilEdit);
    }

    private void readData() {
        data.readData();
        lCount.setText(String.valueOf(data.count()));
    }

    /**
     * Construct the primary stage.
     *
     * @param primaryStage JavaFX will input a stage instance here
     */
    @Override
    public void start(final Stage primaryStage) {
        font = new Font(FONT_SIZE);
        insets = new Insets(GAP, GAP, GAP, GAP);
        padding = new Insets(PADDING_GAP_VERTICAL, PADDING_GAP_HORIZONTAL, PADDING_GAP_VERTICAL, PADDING_GAP_HORIZONTAL);

        bNext = new Button("Next");
        bFind = new Button("Search");
        bPaste = new Button("Paste & Search");
        bCopy = new Button("Copy");
        bDelete = new Button("Delete");
        bAdd = new Button("Add/Edit");

        bNext.setFont(font);
        bFind.setFont(font);
        bPaste.setFont(font);
        bCopy.setFont(font);
        bDelete.setFont(font);
        bAdd.setFont(font);

        bPaste.setMaxSize(Integer.MAX_VALUE, Integer.MAX_VALUE);

        bNext.setMaxWidth(Integer.MAX_VALUE);
        bDelete.setMaxWidth(Integer.MAX_VALUE);

        bFind.prefWidthProperty().bind(bAdd.widthProperty());
        bCopy.prefWidthProperty().bind(bAdd.widthProperty());

        bNext.setDisable(true);
        bDelete.setDisable(true);
        bCopy.setDisable(true);
        bAdd.setDisable(true);

        tfKey = new TextField();
        tfFind = new TextField();
        tfAllRight = new TextField();
        tfLeft = new TextField();
        tfRight = new TextField();

        tfKey.setFont(font);
        tfFind.setFont(font);
        tfAllRight.setFont(font);
        tfLeft.setFont(font);
        tfRight.setFont(font);

        tfKey.setPromptText("Input a keyword here.");
        tfFind.setPromptText("Please search a word first.");
        tfAllRight.setPromptText("Please search a word first.");
        tfLeft.setPromptText("keyword");
        tfRight.setPromptText("value");

        tfKey.setMaxWidth(Integer.MAX_VALUE);
        tfFind.setMaxWidth(Integer.MAX_VALUE);
        tfAllRight.setMaxWidth(Integer.MAX_VALUE);

        tfFind.setEditable(false);
        tfAllRight.setEditable(false);

        lEqual = new Label("=");
        lCount = new Label("0");
        lAuthor = new Label("Powered by magiclen.org");

        lEqual.setFont(font);
        lCount.setFont(font);
        lAuthor.setFont(font);

        lAuthor.setAlignment(Pos.BASELINE_RIGHT);
        lAuthor.setMaxWidth(Integer.MAX_VALUE);

        final Tooltip tipAuthor = new Tooltip("Magic Len");
        tipAuthor.setFont(font);
        Tooltip.install(lAuthor, tipAuthor);

        sDivider = new Separator(Orientation.HORIZONTAL);

        HBox.setMargin(tfLeft, insets);
        HBox.setMargin(tfRight, insets);
        HBox.setMargin(lEqual, insets);

        HBox.setHgrow(tfLeft, Priority.ALWAYS);
        HBox.setHgrow(tfRight, Priority.ALWAYS);

        hbEdit = new HBox();
        hbEdit.getChildren().addAll(tfLeft, lEqual, tfRight);

        BorderPane.setMargin(tfKey, insets);
        BorderPane.setMargin(tfFind, insets);
        BorderPane.setMargin(bFind, insets);
        BorderPane.setMargin(bCopy, insets);
        BorderPane.setMargin(bAdd, insets);
        BorderPane.setMargin(lCount, insets);
        BorderPane.setMargin(lAuthor, insets);

        bpSearch = new BorderPane(tfKey);
        bpCopy = new BorderPane(tfFind);
        bpEdit = new BorderPane(hbEdit);
        bpBottom = new BorderPane();

        bpSearch.setRight(bFind);
        bpCopy.setRight(bCopy);
        bpEdit.setRight(bAdd);
        bpBottom.setRight(lAuthor);

        bpBottom.setLeft(lCount);

        VBox.setMargin(bPaste, insets);
        VBox.setMargin(tfAllRight, insets);
        VBox.setMargin(bNext, insets);
        VBox.setMargin(bDelete, insets);
        VBox.setMargin(sDivider, insets);

        VBox.setVgrow(bPaste, Priority.ALWAYS);

        MAIN_ROOT = new VBox();
        MAIN_ROOT.setAlignment(Pos.TOP_LEFT);
        MAIN_ROOT.setPadding(padding);
        MAIN_ROOT.getChildren().addAll(bPaste, bpSearch, bpCopy, tfAllRight, bNext, bDelete, sDivider, bpEdit, bpBottom);

        MAIN_SCENE = new Scene(MAIN_ROOT, WIDTH, HEIGHT);

        primaryStage.setResizable(true);
        primaryStage.setTitle("Words Transformer FX");
        primaryStage.setScene(MAIN_SCENE);
        primaryStage.setX((SCREEN_WIDTH - WIDTH) / 2);
        primaryStage.setY((SCREEN_HEIGHT - HEIGHT) / 2);

        MAIN_STAGE = primaryStage;

        primaryStage.show();
        primaryStage.setAlwaysOnTop(true);

        addActions();
        readData();
    }

    // -----Object Class-----
    /**
     * The data for Words Transformer FX.
     */
    private class Data {

        // -----Object Constant-----
        /**
         * Data absolute path.
         */
        private final String dataPath;
        /**
         * Left data.
         */
        private final ArrayList<String> left = new ArrayList<>();
        /**
         * Right data.
         */
        private final ArrayList<String> right = new ArrayList<>();

        // -----Object Variable-----
        /**
         * Whether left data were found.
         */
        private boolean findLeft;

        // -----Constructor-----
        /**
         * Construct data instance.
         *
         * @param dataFilePath input the absolute data file path
         */
        public Data(final String dataFilePath) {
            dataPath = dataFilePath;
        }

        public boolean findLeft() {
            return findLeft;
        }

        public int count() {
            final int size = left.size();
            if (size != right.size()) {
                showAlertDialog(AlertType.WARNING, "Hint", null, "Warning! Some data were broken!");
            }
            return size;
        }

        public String getAllRight(final int index) {
            if (index >= count() || index < 0) {
                return "";
            }
            return right.get(index);
        }

        public String getRight(final int index) {
            if (index >= count() || index < 0) {
                return "";
            }
            final String tmp = right.get(index);
            final StringTokenizer stChange = new StringTokenizer(tmp, "-->");

            final int tokenCount_dec = stChange.countTokens() - 1;

            // Consume the tokenizer
            for (int i = 0; i < tokenCount_dec; ++i) {
                stChange.nextToken();
            }

            return stChange.nextToken().trim();
        }

        public String getLeft(final int index) {
            if (index >= count() || index < 0) {
                return "";
            }
            return left.get(index);
        }

        public String get(final int index) {
            if (findLeft) {
                return getRight(index);
            } else {
                return getLeft(index);
            }
        }

        public int find(final String str) {
            return findNext(str, -1);
        }

        public int findNext(final String str, final int index) {
            int targetIndex;

            //find left
            findLeft = true;
            if (index == -1) {
                targetIndex = find(left, str, index, true);
                if (targetIndex != -1) {
                    return targetIndex;
                }
            }
            targetIndex = find(left, str, index, false);
            if (targetIndex != -1) {
                return targetIndex;
            }

            //find right
            findLeft = false;
            if (index == -1) {
                targetIndex = find(right, str, index, true);
                if (targetIndex != -1) {
                    return targetIndex;
                }
            }
            targetIndex = find(right, str, index, false);

            return targetIndex;
        }

        private int find(final ArrayList<String> al, final String str, final int index, final boolean strict) {
            final int size = count();
            int point = index + 1;
            if (point >= 0 && point <= size) {
                if (strict) {
                    for (int i = 0; i < size; ++i) {
                        final int targetIndex = point % size;
                        final String tmp = al.get(targetIndex);
                        if (tmp.equalsIgnoreCase(str)) {
                            return targetIndex;
                        }
                        ++point;
                    }
                } else {
                    final String strUpper = str.toUpperCase();
                    for (int i = 0; i < size; ++i) {
                        final int targetIndex = point % size;
                        final String tmp = al.get(targetIndex).toUpperCase();
                        if (tmp.contains(strUpper)) {
                            return targetIndex;
                        }
                        ++point;
                    }
                }
            }
            return -1;
        }

        public void readData() {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dataPath), "utf8"))) {
                try {
                    while (br.ready()) {
                        final String line = br.readLine().trim();
                        if (line.equals("")) {
                            continue;
                        }
                        final StringTokenizer stEqual = new StringTokenizer(line, "=");
                        if (stEqual.countTokens() != 2) {
                            throw new Exception();
                        }
                        final String leftString = stEqual.nextToken().trim();
                        final int findSame = find(left, leftString, -1, true);
                        if (findSame > -1) {
                            right.set(findSame, right.get(findSame).concat(" --> ").concat(stEqual.nextToken().trim()));
                        } else {
                            left.add(leftString);
                            right.add(stEqual.nextToken().trim());
                        }
                    }
                } catch (final Exception e) {
                    showAlertDialog(AlertType.WARNING, "Hint", "Cannot not read data correctly! The database has something wrong!", "Path: ".concat(dataPath));
                }
            } catch (final Exception e) {
                showAlertDialog(AlertType.WARNING, "Hint", "Cannot not read data correctly! The database doesn't exist!", "Path: ".concat(dataPath));
            }
        }

        public boolean add(final String leftString, final String rightString) {
            if (rightString.contains("-->")) {
                showAlertDialog(AlertType.WARNING, "Hint", null, "Your right string cannot contain '-->'!");
                return false;
            }
            try {
                final int index = find(left, leftString, -1, true);
                if (index == -1) {
                    left.add(leftString);
                    right.add(rightString);
                } else if (getRight(index).equals(rightString)) {
                    showAlertDialog(AlertType.INFORMATION, "Hint", null, "The data already exists!");
                } else {
                    right.set(index, right.get(index).concat(" --> ").concat(rightString));
                }
            } catch (final Exception e) {
                return false;
            }
            writeData();
            return true;
        }

        public boolean delete(final int index) {
            try {
                left.remove(index);
                right.remove(index);
            } catch (final Exception e) {
                return false;
            }
            writeData();
            return true;
        }

        public void writeData() {
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataPath), "utf8"))) {
                final int leftSize = left.size();
                if (leftSize > 0) {
                    final int leftSize_dec = left.size() - 1;
                    // When doing natural sorting, it also writes data to file.
                    for (int i = 0; i < leftSize_dec; ++i) {
                        for (int j = i + 1; j < leftSize; ++j) {
                            if (left.get(i).toUpperCase().compareTo(left.get(j).toUpperCase()) > 0) {
                                String tmp = left.get(i);
                                left.set(i, left.get(j));
                                left.set(j, tmp);

                                tmp = right.get(i);
                                right.set(i, right.get(j));
                                right.set(j, tmp);
                            }
                        }
                        bw.write(left.get(i).concat(" = ").concat(right.get(i)).concat("\n"));
                    }

                    bw.write(left.get(leftSize_dec).concat(" = ").concat(right.get(leftSize_dec)));
                }
                bw.flush();
            } catch (final Exception e) {
                showAlertDialog(AlertType.WARNING, "Hint", "Can't not write data correctly! The database file cannot be written, or some data's format are incorrect.", "Path: ".concat(dataPath));
            }
        }
    }
}
