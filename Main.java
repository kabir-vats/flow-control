/*
 * Main file for Flow-Control. Handles UI.
 */
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import javafx.scene.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.application.*;
import javafx.stage.*;
import javafx.util.Callback;

/*
 * This is the class for the application's UI. The Instance variables
 * are intended to 
 */
public class Main extends Application {
    static String username;
    ScrollPane screen;
    VBox display;
    HBox operations;
    VBox taskList;
    Manager m;
    Scene scene;

    private static final String LOGIN_PROMPT = "Enter username";
    private static final String SHOW_OVERDUE_BUTTON_TEXT = "Show Overdue";
    private static final String HIDE_OVERDUE_BUTTON_TEXT = "Hide Overdue";
    private static final String SHOW_COMPLETE_BUTTON_TEXT = "Show Complete";
    private static final String HIDE_COMPLETE_BUTTON_TEXT = "Hide Complete";
    private static final String ADD_SUBTASK_BUTTON_TEXT = "Add Subtask";
    private static final String ADD_SUBTASK_STAGE_TITLE = "Add Subtask";
    private static final String EARLY_DATE_COLOR = "-fx-background-color: #ffc0cb;";
    private static final String FINALIZE_SUBTASK_BUTTON_TEXT = "Create Subtask";
    private static final String SAVE_BUTTON_TEXT = "Save";
    private static final String CREATE_TASK_BUTTON_TEXT = "Create Task";
    private static final String ADD_TASK_STAGE_TITLE = "Add Task";
    private static final String COMPLETE_TASK_COLOR = "-fx-background-color: green";
    private static final String INCOMPLETE_TASK_COLOR = "-fx-background-color: white";
    private static final String SHOW_SUBTASKS_BUTTON_TEXT = "Show Subtasks";
    private static final String HIDE_SUBTASKS_BUTTON_TEXT = "Hide Subtasks";
    private static final String MARK_COMPLETE_BUTTON_TEXT = "Mark COMPLETE";
    private static final String MARK_INCOMPLETE_BUTTON_TEXT = "mark INCOMPLETE";
    private static final String SHOW_DESCRIPTION_BUTTON_TEXT = "Show Description";
    private static final String HIDE_DESCRIPTION_BUTTON_TEXT = "Hide Description";
    private static final String ENTER_TITLE_PROMPT = "Enter title.";
    private static final String ENTER_DESCRIPTION_PROMPT = "Enter Task Description (Optional).";
    private static final String[] RECURRING_SELECTION_PROMPTS = {"Select Recurring or One Time Task", 
        "Recurring", "One Time"};
    private static final String[] PERIOD_SELECTION_PROMPTS = {"Select Period of Recursion", "Daily",
         "Weekly", "Monthly", "Yearly"};
    private static final String DAYS_PER_WEEK_PROMPT = "Select Number of Days Per Week";
    private static final String[] VIEW_SELECTION_PROMPTS = {"Task -> Subtask View", "All Tasks View"};


    public static void main(String[] args) throws IOException {
        Scanner loginScan = new Scanner(System.in);
        System.out.println(LOGIN_PROMPT);
        username = loginScan.nextLine();
        loginScan.close();
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception, IOException{
        m = new Manager(username);
        m.readStorage();
        display = new VBox();
        operations = new HBox();

        taskList = new VBox();

        display.getChildren().add(operations);

        operations.getChildren().add(addTask());
        operations.getChildren().add(hideOld());
        operations.getChildren().add(hideComplete());
        operations.getChildren().add(selectView());

        taskList.getChildren().add(displayTaskSubTask());
        display.getChildren().add(taskList);
        
        display.getChildren().add(saveTask(m));


        screen = new ScrollPane(display);
        scene = new Scene(screen);
        primaryStage.setScene(scene);
        primaryStage.setWidth(1024);
        primaryStage.setHeight(512);
        primaryStage.show();
    }

    public ChoiceBox<String> selectView() {
        ChoiceBox<String> selectView = new ChoiceBox<>();
        for (String prompt : VIEW_SELECTION_PROMPTS) {
            selectView.getItems().add(prompt);
        }
        selectView.getSelectionModel().selectFirst();
        selectView.setOnAction(event -> {
            changeTaskView(selectView.getSelectionModel().getSelectedIndex());
        });
        return selectView;
    }

    public void changeTaskView(int view) {
        taskList.getChildren().clear();
        if (view == 0) {
            taskList.getChildren().add(displayTaskSubTask());
        }
        else {
            taskList.getChildren().add(displayAllTasks());
        }
        
    }

    public ToggleButton hideOld() {
        ToggleButton hideOld = new ToggleButton(HIDE_OVERDUE_BUTTON_TEXT);
        hideOld.setOnAction(event -> {
            if (hideOld.isSelected()) {
                hideOld.setText(SHOW_OVERDUE_BUTTON_TEXT);
            }
            else {
                hideOld.setText(HIDE_OVERDUE_BUTTON_TEXT);
            }
            taskList.getChildren().clear();
            taskList.getChildren().add(displayAllTasks());
        });
        return hideOld;
    }

    public ToggleButton hideComplete() {
        ToggleButton hideComplete = new ToggleButton(HIDE_COMPLETE_BUTTON_TEXT);
        hideComplete.setOnAction(event -> {
            if (hideComplete.isSelected()) {
                hideComplete.setText(SHOW_COMPLETE_BUTTON_TEXT);
            }
            else {
                hideComplete.setText(HIDE_COMPLETE_BUTTON_TEXT);
            }
            taskList.getChildren().clear();
            taskList.getChildren().add(displayAllTasks());
        });
        return hideComplete;
    }


    public Button subTaskButton(Task t) {
        Button subTask = new Button(ADD_SUBTASK_BUTTON_TEXT);
        subTask.setOnAction(event -> {
            addSubTaskStage(t).show();
        });
        return subTask;
    }

    public Stage addSubTaskStage(Task t) {
        Stage subStage = new Stage();
        subStage.setTitle(ADD_SUBTASK_STAGE_TITLE);
        VBox promptList = new VBox();
        Task subTask = new Task();
        
        final Callback<DatePicker, DateCell> dayCellFactory;
            dayCellFactory = (final DatePicker datePicker) -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if(item.isBefore(LocalDate.now()) || item.isAfter(t.getDate())) {
                    setDisable(true);
                    setStyle(EARLY_DATE_COLOR);
                }
            }
        };

        final TextField titleField = new TextField();
        titleField.setPromptText(ENTER_TITLE_PROMPT);
        promptList.getChildren().add(titleField);

        final TextField descriptionField = new TextField();
        descriptionField.setPromptText(ENTER_DESCRIPTION_PROMPT);
        promptList.getChildren().add(descriptionField);

        DatePicker selectDate = new DatePicker();
        selectDate.setDayCellFactory(dayCellFactory);
        if (t.getPeriodCode() == 0) {
            promptList.getChildren().add(selectDate);
            
        }
        else {
            subTask.setPeriodCode(t.getPeriodCode());
            subTask.setDate(t.getDate());
        }

        Button finalize = new Button(FINALIZE_SUBTASK_BUTTON_TEXT);
        promptList.getChildren().add(finalize);
        finalize.setOnAction(event -> {
            subTask.setTitle(titleField.getText());
            subTask.setDescription(descriptionField.getText());
            if (t.getPeriodCode() == 0) {
                subTask.setDate(selectDate.getValue());
            }
            m.addSubTask(subTask, t);
            subStage.close();
            taskList.getChildren().clear();
            taskList.getChildren().add(displayAllTasks());
            for (Task T : m.allTasks) {
                System.out.println(T.getTitle());
            }
        });

        Scene scene = new Scene(promptList);
        subStage.setScene(scene);
        subStage.setHeight(512);
        subStage.setWidth(512);
        return subStage;
    }

    public Button saveTask(Manager m) {
        Button saver = new Button(SAVE_BUTTON_TEXT);
        saver.setOnAction(event -> {
            try {
                m.writeToStorage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return saver;
    }

    

    public Button addTask() {
        Button addTaskWindow = new Button(CREATE_TASK_BUTTON_TEXT);
        addTaskWindow.setOnAction(event -> {
            addTaskStage(m, taskList).show();
        });


        return addTaskWindow;
    }

    public Stage addTaskStage(Manager m, VBox taskList) {
        Stage taskStage = new Stage();
        taskStage.setTitle(ADD_TASK_STAGE_TITLE);
        
        VBox promptList = new VBox();
        
        final TextField titleField = new TextField();
        titleField.setPromptText(ENTER_TITLE_PROMPT);
        promptList.getChildren().add(titleField);

        final TextField descriptionField = new TextField();
        descriptionField.setPromptText(ENTER_DESCRIPTION_PROMPT);
        promptList.getChildren().add(descriptionField);

        final ChoiceBox<String> recurringChoice = new ChoiceBox<>();
        
        for (String prompt : RECURRING_SELECTION_PROMPTS) {
            recurringChoice.getItems().add(prompt);
        }
        recurringChoice.getSelectionModel().selectFirst();
        promptList.getChildren().add(recurringChoice);
        
        final ChoiceBox<String> timePeriodChoice = new ChoiceBox<>();
        DatePicker fromDate = new DatePicker();
        DatePicker toDate = new DatePicker();
        final ChoiceBox<String> daysPerWeekChoice = new ChoiceBox<>();
        ArrayList<DatePicker> datePickers = new ArrayList<DatePicker>();
        VBox recurSetter = new VBox();
        VBox weekDayPickers = new VBox();

        VBox dateSetter = new VBox();
        promptList.getChildren().add(dateSetter);
        var recurWrapper = new Object() {int currChoice = 0;};
        var perWrapper = new Object() {int currChoice = 0;};
        var weekWrapper = new Object() {int currChoice = 0;};
        final Callback<DatePicker, DateCell> dayCellFactory;
        dayCellFactory = (final DatePicker datePicker) -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if(item.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle(EARLY_DATE_COLOR);
                }
            }
        };
/** 
        recurringChoice.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>()  {
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                currChoice = (Integer) newValue;
            }
        });
*/
        recurringChoice.setOnAction(event -> {
            recurWrapper.currChoice = recurringChoice.getSelectionModel().getSelectedIndex(); //AAAAAAA
            if (recurWrapper.currChoice == 1) {
                dateSetter.getChildren().clear();
                for (String prompt : PERIOD_SELECTION_PROMPTS) {
                    timePeriodChoice.getItems().add(prompt);
                }
                timePeriodChoice.getSelectionModel().selectFirst();
                dateSetter.getChildren().add(timePeriodChoice);
                
                dateSetter.getChildren().add(recurSetter);
                timePeriodChoice.setOnAction(event1 -> {
                    perWrapper.currChoice = timePeriodChoice.getSelectionModel().getSelectedIndex();
                    if (perWrapper.currChoice == 1) {
                        recurSetter.getChildren().clear();
                        fromDate.setDayCellFactory(dayCellFactory);
                        toDate.setDayCellFactory(dayCellFactory);
                        recurSetter.getChildren().add(fromDate);
                        recurSetter.getChildren().add(toDate);
                    }
                    if (perWrapper.currChoice == 2) {
                        recurSetter.getChildren().clear();
                        
                        daysPerWeekChoice.getItems().add(DAYS_PER_WEEK_PROMPT);
                        for (int i = 1; i < 8; i++) {
                            daysPerWeekChoice.getItems().add(Integer.toString(i));
                        }
                        daysPerWeekChoice.getSelectionModel().selectFirst();
                        recurSetter.getChildren().add(daysPerWeekChoice);
                        recurSetter.getChildren().add(weekDayPickers);
                        
                        daysPerWeekChoice.setOnAction(event2 -> {
                            weekDayPickers.getChildren().clear();
                            datePickers.clear();
                            weekWrapper.currChoice = daysPerWeekChoice.getSelectionModel().getSelectedIndex();
                            datePickers.add(new DatePicker());
                            datePickers.get(0).setDayCellFactory(dayCellFactory);
                            datePickers.get(0).setValue(LocalDate.now());
                            final Callback<DatePicker, DateCell> weekRangeFactory;
                            weekRangeFactory = (final DatePicker datePicker) -> new DateCell() {
                                @Override
                                public void updateItem(LocalDate item, boolean empty) {
                                    super.updateItem(item, empty);
                                    int dayOfWeek = datePickers.get(0).getValue().getDayOfWeek().getValue();
                                    
                                    LocalDate weekStart = datePickers.get(0).getValue().minusDays((7 + dayOfWeek) % 7);
                                    LocalDate weekEnd = weekStart.plusDays(6);
                                    if(item.isBefore(weekStart) || item.isAfter(weekEnd)) {
                                        setDisable(true);
                                        setStyle(EARLY_DATE_COLOR);
                                    }
                                }
                            };
                            for (int i = 1; i < weekWrapper.currChoice; i++) {
                                datePickers.add(new DatePicker());
                                datePickers.get(i).setDayCellFactory(weekRangeFactory);
                                datePickers.get(i).setValue(LocalDate.now());
                            }
                            datePickers.add(toDate);
                            
                            for (int i = 0; i <= weekWrapper.currChoice; i++) {
                                weekDayPickers.getChildren().add(datePickers.get(i));
                            }
                        });
                    }
                    if (perWrapper.currChoice == 3) {
                        recurSetter.getChildren().clear();
                        fromDate.setDayCellFactory(dayCellFactory);
                        toDate.setDayCellFactory(dayCellFactory);
                        recurSetter.getChildren().add(fromDate);
                        recurSetter.getChildren().add(toDate);
                    }
                    if (perWrapper.currChoice == 4) {
                        recurSetter.getChildren().clear();
                        fromDate.setDayCellFactory(dayCellFactory);
                        toDate.setDayCellFactory(dayCellFactory);
                        recurSetter.getChildren().add(fromDate);
                        recurSetter.getChildren().add(toDate);
                    }
                });
            }
            else if (recurWrapper.currChoice == 2) {
                dateSetter.getChildren().clear();
                fromDate.setDayCellFactory(dayCellFactory);
                dateSetter.getChildren().add(fromDate);
            }
            
        });
        Button createButton = new Button("Create Task");
        promptList.getChildren().add(createButton);
        createButton.setOnAction(event2 -> {
            String titleString = titleField.getText();
            String descriptionString = descriptionField.getText();
            if (recurWrapper.currChoice == 2) {
                Task t = new Task(titleString, descriptionString, fromDate.getValue());
                m.addTask(t);
            }
            else if (recurWrapper.currChoice == 1) {
                int perCode = (int) (perWrapper.currChoice * Math.pow(10,8) + toDate.getValue().getYear() * Math.pow(10, 4)
                    + toDate.getValue().getMonthValue() * 100 + toDate.getValue().getDayOfMonth());
                if (perWrapper.currChoice == 0) {
                    System.out.println("FAIL");
                }
                else if (perWrapper.currChoice == 2) {
                    for (int i = 0; i < weekWrapper.currChoice; i++) {
                        Task t = new Task(titleString, descriptionString, datePickers.get(i).getValue(), perCode);
                        m.addTask(t);
                    }
                }
                else {
                    Task t = new Task(titleString, descriptionString, fromDate.getValue(), perCode);
                    m.addTask(t);
                }
            }
            taskStage.close();
            taskList.getChildren().clear();
            taskList.getChildren().add(displayAllTasks());
        });
        Scene scene = new Scene(promptList);
        taskStage.setScene(scene);
        taskStage.setHeight(512);
        taskStage.setWidth(512);
        return taskStage;
    }

    public VBox displayTaskSubTask() {
        boolean showOverdue = !((ToggleButton)operations.getChildren().get(1)).isSelected();
        boolean showComplete = !((ToggleButton)operations.getChildren().get(2)).isSelected();
        VBox tasksSub = new VBox();
        TreeItem<BorderPane> origin = new TreeItem<>();
        TreeView<BorderPane> taskSubTask = new TreeView<>(origin);
        taskSubTask.setShowRoot(false);
        for (Task t : m.tasks) {
            BorderPane temp = taskBox(t, showComplete, showOverdue, true);
            if (temp == null) {
                continue;
            }
            tasksSub.getChildren().add(temp);
        }
        return tasksSub;
    }

    ToggleButton showSubTasks(Task t, BorderPane bPane, boolean showComplete, boolean showOverdue) {
        ToggleButton showSubs = new ToggleButton(SHOW_SUBTASKS_BUTTON_TEXT);
        showSubs.setOnAction(event -> {
            if (showSubs.isSelected()) {
                VBox subsBox = new VBox();
                for (Task s : t.getSubTasks()) {
                    BorderPane taskBox = taskBox(s, showComplete, showOverdue, true);
                    if (taskBox == null) {
                        continue;
                    }
                    subsBox.getChildren().add(taskBox);
                }
                bPane.setBottom(subsBox);
                showSubs.setText(HIDE_SUBTASKS_BUTTON_TEXT);
            }
            else {
                bPane.setBottom(null);
                showSubs.setText(SHOW_SUBTASKS_BUTTON_TEXT);
            }
        });
        return showSubs;
    }

    public BorderPane taskBox(Task t, boolean showComplete, boolean showOverdue, boolean subTaskView) {
        if (t.isComplete() && (!showComplete || t.getDate().isBefore(m.date))) {
                return null;
            } 
        if (!showOverdue && t.getDate().isBefore(m.date) ) {
            return null;
        }
        BorderPane bPane = new BorderPane();
        bPane.setPadding(new Insets(1));
        HBox curr = new HBox(5);
        bPane.setTop(curr);
        Label title = new Label(t.getTitle());
        title.setMinWidth(200);
        curr.getChildren().add(title);
        ToggleButton complete = new ToggleButton(MARK_COMPLETE_BUTTON_TEXT);
        complete.setMinWidth(130);
        if(t.isComplete()) {
            complete.setSelected(true);
            complete.setText(MARK_INCOMPLETE_BUTTON_TEXT);
            curr.setStyle(COMPLETE_TASK_COLOR);
        }
        complete.setOnAction(event -> {
            if (complete.isSelected()) {
                if (t.markComplete()) {
                    complete.setText(MARK_INCOMPLETE_BUTTON_TEXT);
                    curr.setStyle(COMPLETE_TASK_COLOR);
                }
                else {
                    complete.setSelected(false);
                }
            }
            else {
                complete.setText(MARK_COMPLETE_BUTTON_TEXT);
                curr.setStyle(INCOMPLETE_TASK_COLOR);
                t.markIncomplete();
            }
        });
        Label date = new Label("" + t.getDate());
        curr.getChildren().add(date);
        curr.getChildren().add(subTaskButton(t));
        curr.getChildren().add(complete);
        curr.getChildren().add(showDescription(t, bPane));
        if (subTaskView) {
            curr.getChildren().add(showSubTasks(t, bPane, showComplete, showOverdue));
        }
        return bPane;
    }

    public ToggleButton showDescription(Task t, BorderPane pane) {
        ToggleButton showDescription = new ToggleButton(SHOW_DESCRIPTION_BUTTON_TEXT);
        showDescription.setOnAction(event -> {
            if (showDescription.isSelected()) {
                Label desc = new Label(t.getDescription());
                pane.setCenter(desc);
                showDescription.setText(HIDE_DESCRIPTION_BUTTON_TEXT);
            }
            else {
                pane.setCenter(null);
                showDescription.setText(SHOW_DESCRIPTION_BUTTON_TEXT);
            }
        });
        return showDescription;
    }

    public VBox displayAllTasks() {
        boolean showOverdue = !((ToggleButton)operations.getChildren().get(1)).isSelected();
        boolean showComplete = !((ToggleButton)operations.getChildren().get(2)).isSelected();
        VBox vbox = new VBox();
        for (Task t : m.allTasks) {
            BorderPane bPane = taskBox(t, showComplete, showOverdue, false);
            if (bPane == null) {
                continue;
            }
            vbox.getChildren().add(bPane);
        }
        
        return vbox;
    }


   
}
