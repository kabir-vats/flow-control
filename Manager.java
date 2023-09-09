/*
 * This is a file to contain the Manager class. 
 */
import java.util.*;
import java.time.LocalDate;

import java.io.*;

/*
 * This class runs the behind the scenes aspects of the Flow Control
 * Application. Functionalities include reading from storage, writing
 * to storage and storing up-to-date lists of Tasks.
 * Instance Variables:
 * LocalDate date: LocalDate to store the present date
 * List<Task> tasks: ArrayList of Task to store all of the Tasks that
 * are not subTasks. Sorted in chronological order.
 * List<Task> allTasks: ArrayList of all Tasks, including subTasks. 
 * Sorted in chronological order
 * String username: String to store username which is the File to write to and
 * from
 */
public class Manager {
    LocalDate date;
    List<Task> tasks;
    List<Task> allTasks;
    String username;

    /*Constants */
    private final static String FILE_EXTENSION = ".txt";
    private final static String FILE_START = "Tasks:";
    private final static String FILE_END = "end";
    private final static String TRUE_STRING = "true";

    private final static char FILE_DELIM = '\t';
    private final static char SUBTASK_INDICATOR = '+';

    private final static int DAY_FLAG = 1;
    private final static int WEEK_FLAG = 2;
    private final static int MONTH_FLAG = 3;
    
    private final static int DATE_OFFSET = 8;
    private final static int DATE_SKIP = 9;
    private final static int PERIOD_LENGTH = 9;

    private final static int MMDD_DIGITS = 10000;
    private final static int DD_DIGITS = 100;
    private final static int DATE_DIGITS = 100000000;

    /**
     * Constructor to create a manager
     * @param username - The username of user and
     * txt file to draw from / write to. Just the part of file name
     * before '.txt'
     */
    public Manager(String username) {
        date = LocalDate.now();
        tasks = new ArrayList<Task>();
        allTasks = new ArrayList<Task>();
        this.username = username;
    }

    /**
     * Method to add a Task to the list of Tasks. Inserts
     * the Task in the Chronological location using binary
     * search. Then adds the Task to list of all Tasks.
     * @param t The Task to add
     */
    public void addTask(Task t) {
        LocalDate date = t.getDate();
        int low = 0;
        int high = tasks.size() - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            if (!tasks.get(mid).getDate().isBefore(date)) {
                high = mid - 1;
            }
            else {
                low = mid + 1;
            }
        }
        tasks.add(low, t);
        insertToAll(t);
    }

    /**
     * Method to add a subTask to a Task. Inserts the Task
     * to allTasks list.
     * @param sub - The subTask
     * @param t - The Task
     */
    public void addSubTask(Task sub, Task t) {
        t.addSubTask(sub);
        insertToAll(sub);
    }

    /**
     * Method to insert a Task to the list allTasks. Uses
     * a binary Search to find the chronlogical location to
     * insert the Task.
     * @param t - Task to insert
     */
    public void insertToAll(Task t) {
        LocalDate date = t.getDate();
        int low = 0;
        int high = allTasks.size() - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            if (!allTasks.get(mid).getDate().isBefore(date)) {
                high = mid - 1;
            }
            else {
                low = mid + 1;
            }
        }
        allTasks.add(low, t);
    }


    /**
     * Method to read from Storage, using the username. Tasks are 
     * stored in Storage separated by Tabs. The order is Title, 
     * Description, Date, whether or not the Task is complete and periodCode
     * @return True if username file exists, false otherwise
     * @throws IOException
     */
    public boolean readStorage() throws IOException{
        String fileName = username + FILE_EXTENSION;
        File userFile = new File(fileName);
        if (!userFile.exists()) {
            userFile.createNewFile();
            return false;
        }

        /*Buffered Reader to read line by line */
        BufferedReader creator = new BufferedReader(new FileReader(fileName));

        String currLine = creator.readLine();
        currLine = creator.readLine();

        /*StringBuilder to store each part of the Line as it is read */
        StringBuilder sb = new StringBuilder();

        /*While loop to iterate through every line until null */
        while(!(currLine == null || currLine.equals(FILE_END))) {
            
            /*
             * Every task that is not a subTask starts with a tab. If it
             * is a subTask, it starts with '+' characters indicating the depth.
             */
            int index = 0;
            int currDepth = 0;
            /*Calculates the depth*/
            while (currLine.charAt(index) != FILE_DELIM) {
                currDepth++;
                index++;
            }
            index++;
            Task t = new Task();

            sb.setLength(0); // Clear the StringBuilder

            /*Iterate until next tab to get Title */
            while (currLine.charAt(index) != FILE_DELIM) {
                sb.append(currLine.charAt(index));
                index++;
            }
            t.setTitle(sb.toString());
            index++;

            sb.setLength(0);

            /*Iterate until next tab to get Description */
            while (currLine.charAt(index) != FILE_DELIM) {
                sb.append(currLine.charAt(index));
                index++;
            }
            t.setDescription(sb.toString());
            index++;

            sb.setLength(0);

            /*
             * Date is stored in format YYYYMMDD. To get the date from storage,
             * mod and division operations are used to get the necessary characters.
             */
            int date = Integer.parseInt(currLine.substring(index, index + DATE_OFFSET));
            t.setDate(LocalDate.of(date / MMDD_DIGITS, (date % MMDD_DIGITS) / DD_DIGITS, 
                date % DD_DIGITS));

            index += DATE_SKIP;

            sb.setLength(0);

            /*Iterate until next tab to get true / false*/
            while (currLine.charAt(index) != FILE_DELIM) {
                sb.append(currLine.charAt(index));
                index++;
            }
            if (sb.toString().equals(TRUE_STRING)) {
                t.markComplete();
            }
            index++;

            /*
             * Period Code:
             * If 0, non-recurring Task. Else:
             * Period Code Structure (9 digits):
             * First number: 1 if daily Task, 2 if weekly, 3 if monthly, 4 if yearly.
             * Next 8 numbers: Date task recurs until in format YYYYMMDD
             */

            int periodCode = Integer.parseInt(currLine.substring(index, index + 1));
            
            if (periodCode != 0) { // If recurring
                periodCode = Integer.parseInt(currLine.substring(index, index + PERIOD_LENGTH));
                t.setPeriodCode(periodCode);
                int MSB = (int) (periodCode / DATE_DIGITS); // Most Significant Bit

                /*Date task repeats until */
                LocalDate until = LocalDate.of((periodCode % DATE_DIGITS) / MMDD_DIGITS, 
                        periodCode % MMDD_DIGITS / DD_DIGITS, periodCode % DD_DIGITS);

                if (MSB == DAY_FLAG) { //If daily

                    /*If the task is no longer recurring, continue*/
                    if (this.date.isAfter(until)) {
                        currLine = creator.readLine();
                        continue;
                    }

                    /*If the task has not been completed today, mark incomplete*/
                    else if (this.date.isAfter(t.getDate())){
                        t.setDate(this.date);
                        t.markIncomplete();
                    }
                }

                else if (MSB == WEEK_FLAG) {// If weekly

                    LocalDate newDate = t.getDate();

                    /*Boolean to see if any weeks were jumped */
                    boolean jumpedFlag = false;

                    /*
                     * Loop until current date is passed. If loop is not 
                     * entered, jumpedFlag will remain false.
                     */
                    while (newDate.isBefore(this.date)) {
                        newDate = newDate.plusWeeks(1);
                        jumpedFlag = true;
                    }

                    /*
                     * If there was a jump and the task is not complete, set it
                     * to the previous week and make it overdue
                     */
                    if (jumpedFlag && !t.isComplete()) {
                        newDate = newDate.minusWeeks(1);
                    }

                    /*If the date is after the until date, don't add the task */
                    if (newDate.isAfter(until)) {
                        currLine = creator.readLine();
                        continue;
                    }
                    t.setDate(newDate);
                }

                else if (MSB == MONTH_FLAG) {
                    LocalDate newDate = t.getDate();

                    boolean jumpedFlag = false;
                    
                    while (newDate.isBefore(this.date)) {
                        newDate = newDate.plusMonths(1);
                        jumpedFlag = true;
                    }
                    
                    if (jumpedFlag && !t.isComplete()) {
                        newDate = newDate.minusMonths(1);
                    }
                    
                    if (newDate.isAfter(until)) {
                        currLine = creator.readLine();
                        continue;
                    }
                    t.setDate(newDate);
                }

                else {
                    LocalDate newDate = t.getDate();
                    boolean jumpedFlag = false;
                    while (newDate.isBefore(this.date)) {
                        newDate = newDate.plusYears(1);
                        jumpedFlag = true;
                    }
                    if (jumpedFlag && !t.isComplete()) {
                        newDate = newDate.minusYears(1);
                    }
                    if (newDate.isAfter(until)) {
                        currLine = creator.readLine();
                        continue;
                    }
                    t.setDate(newDate);
                }
            }

            else { // Not repeating
                t.setPeriodCode(0);
            }

            if (currDepth == 0) { // If not a subTask
                addTask(t);
            }

            /*
             * Tasks are stored in DFS order, so the Task will always be the last
             * subTask at the read depth.
             */
            else { 
                Task curr = tasks.get(tasks.size() - 1);
                for(int i = 0; i < currDepth - 1; i++) {
                    curr = curr.getSubTasks().get(curr.getSubTasks().size() - 1);
                }
                addSubTask(t,curr);
            }
            currLine = creator.readLine();
        }
        creator.close();
        return true;
    }

    /**
     * Method to store data for future runs. Stores in the form of
     * Strings separated by Tabs. The order is Title, Description, Date,
     * Whether or Not Complete, and periodCode. Each Task is one line. Uses
     * repeated calls to writeTask method.
     * @throws IOException
     */
    public void writeToStorage() throws IOException{
        String fileName = username + FILE_EXTENSION;
        File userFile = new File(fileName);

        /*
         * Just delete the file if it exists and create
         * a new file to replace it. Too much of a pain to clear.
         */
        if(userFile.exists()) {
            userFile.delete();
        }
        userFile.createNewFile();

        /* BufferedWriter to write line by line */
        BufferedWriter saver = new BufferedWriter(new FileWriter(fileName));

        /* StringBuilder to construct the line to write */
        StringBuilder sb = new StringBuilder();

        saver.write(FILE_START);
        saver.newLine();

        /*Writes each Task using writeTask method*/
        for(Task t : tasks) {
            writeTask(sb, t, 0, saver);
        }

        saver.write(FILE_END); //Signify end of file

        saver.close();
    }

    
    /**
     * Method to write a Task to storage. Each call to this method
     * writes one Line to Storage
     * @param sb - StringBuilder to construct line to write
     * @param t - Task to write to storage
     * @param depth - Depth of Task (How many subTasks in is this Task)
     * @param saver - BufferedWriter to write the Task to the file
     * @throws IOException
     */
    public void writeTask(StringBuilder sb, Task t, int depth, 
        BufferedWriter saver) throws IOException{
        
        sb.setLength(0); //Clear the StringBuilder

        /*Adds '+' characters to save the depth for storage */
        for(int i = 0; i < depth; i++) {
            sb.append(SUBTASK_INDICATOR);
        }
        sb.append(FILE_DELIM);

        sb.append(t.getTitle() + FILE_DELIM);
        sb.append(t.getDescription() + FILE_DELIM);

        /* 
         * Date is given as YYYY/MM/DD but we want YYYYMMDD so subStrings
         * are used to remove '/'
         */
        String d = t.getDate().toString();
        sb.append(d.substring(0,4) + 
            d.substring(5,7) + 
            d.substring(8,10) + FILE_DELIM);

        sb.append(Boolean.toString(t.isComplete()) + FILE_DELIM);
        sb.append(t.periodCode);

        /*Save task */
        saver.write(sb.toString());
        saver.newLine();

        /* 
         * Save each of the Task's subTasks, adding 1 to current depth
         */
        List<Task> subs = t.getSubTasks();
        for (Task s : subs) {
            writeTask(sb, s, depth + 1, saver);
        }
    }
}
