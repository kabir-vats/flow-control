/**
 * File for Task Object. A Task is a
 */
import java.util.*;
import java.time.LocalDate;

/**
 * Task class. 
 * Instance variables:
 * List<Task> subTasks - list of subTask prerequisites of the Task.
 * String title - Task title.
 * String description - description of Task.
 * boolean complete - whether or not Task is complete.
 * LocalDate date - stores the date of expected completion of the Task.
 * int periodCode - a code of a certain structure to store recurring Tasks.
 */
public class Task implements Cloneable{
    /*
     * Instance Variables
     */

    String title;
    String description;
    boolean complete;
    LocalDate date;
    List<Task> subTasks;

    /**
     * If 0, non-recurring Task. Else:
     * Period Code Structure (9 digits):
     * First number: 1 if daily Task, 2 if weekly, 3 if monthly, 4 if yearly.
     * Next 8 numbers: Date task recurs until in format YYYYMMDD
     */
    int periodCode;
    
    /**
     * Default constructor, no parameters. Creates Task with
     * null title, description "" and periodCode 0.
     */
    public Task() {
        subTasks = new ArrayList<Task>();
        title = null;
        description = "";
        complete = false;
        periodCode = 0;
    }

    /**
     * Three argument constructor for non-recurring Task. Sets 
     * instance variables to corresponding params and periodCode to 0.
     * @param title - Task title
     * @param description - Task description
     * @param date - Task completion date
     */
    public Task(String title, String description, LocalDate date) {
        subTasks = new ArrayList<Task>();
        this.title = title;
        this.description = description;
        complete = false;
        this.date = date;
        periodCode = 0;
    }

    /**
     * Two argument constructor ofr non-recurring Tasks (no description).
     * Sets instance variables to corresponding params, sets description
     * to "" and periodCode to 0.
     * @param title - Task title
     * @param date - Task completion date
     */
    public Task(String title, LocalDate date) {
        subTasks = new ArrayList<Task>();
        this.title = title;
        this.description = "";
        complete = false;
        this.date = date;
        periodCode = 0;
    }

    /**
     * 4 Argument constructor for Task with all params specified. This
     * method can work for recurring Tasks. Sets instance variables to 
     * corresponding params.
     * @param title - Task title
     * @param description - Task description
     * @param date - Task completion date
     * @param periodCode - Task periodCode
     */
    public Task(String title, String description, LocalDate date, int periodCode) {
        subTasks = new ArrayList<Task>();
        this.title = title;
        this.description = description;
        complete = false;
        this.date = date;
        this.periodCode = periodCode;
    }

    /**
     * 3 Argument constructor for Task without description specified. This
     * method can work for recurring Tasks. Sets instance variables to 
     * corresponding params.
     * @param title - Task title
     * @param date - Task completion date
     * @param periodCode - Task periodCode
     */
    public Task(String title, LocalDate date, int periodCode) {
        subTasks = new ArrayList<Task>();
        this.title = title;
        this.description = "";
        complete = false;
        this.date = date;
        this.periodCode = periodCode;
    }

    /**
     * Sets title of Task to specified String.
     * @param newTitle - new title of Task
     */
    public void setTitle(String newtitle) {
        this.title = newtitle;
    }

    /**
     * Sets description of Task to specified String
     * @param newDescription - new description of Task
     */
    public void setDescription(String newDescription) {
        this.description = newDescription;
    }

    /**
     * Sets date of Task to specified String
     * @param newDate - new date of Task.
     */
    public void setDate(LocalDate newdate) {
        this.date = newdate;
    }

    /**
     * sets periodCode of Task to specified int
     * @param newPeriodCode - new periodCode of Task
     */
    public void setPeriodCode (int newPeriodCode) {
        this.periodCode = newPeriodCode;
    }

    /**
     * Method to get title of Task.
     * @return Task's title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Method to get description of Task
     * @return Task's description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Method to get whether or not a Task is complete
     * @return True if Task is marked complete, False if not
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * Method to get the list of Task's subTasks
     * @return ArrayList of subTasks
     */
    public List<Task> getSubTasks() {
        return subTasks;
    }

    /**
     * Method to get the periodCode of Task
     * @return Task's periodCode
     */
    public int getPeriodCode () {
        return periodCode;
    }

    /**
     * Method to get a Task's due date
     * @return Task's due date
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Mark the Task as complete. The method will not 
     * mark the Task complete if any of its subTasks are incomplete.
     * @return True if successful, false if a subTask was incomplete.
     */
    public boolean markComplete() {
        for(Task t: subTasks) {
            if (!t.isComplete()) {
                return false;
            }
        }
        complete = true;
        return true;
    }

    /**
     * Mark the Task as incomplete.
     */
    public void markIncomplete() {
        complete = false;
    }

    /**
     * Adds subTask to appropriate place in subTask List chronogically.
     * @param sub - subTask to add to Task. If subTask does not have an
     * assigned date an error will be thrown.
     */
    public void addSubTask(Task sub) {
        LocalDate subDate = sub.getDate();
        int low = 0;
        int high = subTasks.size() - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            if (!subTasks.get(mid).getDate().isBefore(subDate)) {
                high = mid - 1;
            }
            else {
                low = mid + 1;
            }
        } 
        subTasks.add(low, sub);
    }

    @Override
    public Task clone() throws CloneNotSupportedException {
        Task t = new Task();
        t.setTitle(title);
        t.setDescription(description);
        for(Task s : subTasks) {
            t.addSubTask(s.clone());
        }
        return t;
    }
}