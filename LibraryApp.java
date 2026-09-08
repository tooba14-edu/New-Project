
import java.util.*;
import java.io.*;
import java.time.LocalDate;
// Custom Exception
class InvalidTransactionException extends Exception {
    public InvalidTransactionException(String msg) {
        super(msg);
    }
}
interface ILoanable {
    void checkOut(Student s)
            throws InvalidTransactionException;
}
abstract class Item {
    protected String id;
    protected String title;
    protected boolean isAvailable = true;
    protected LocalDate dueDate;
    protected String currentBorrowerId = "None";
    public Item(String id, String title) {
        this.id = id;
        this.title = title;
    }
    public abstract void display();
    public abstract String getTypeCode();
    public abstract void returnItem(Student s);
}
// Book Class
class Book extends Item implements ILoanable {
    private String author;
    public Book(String id,String title,String author) {
        super(id, title);
        this.author = author;
    }
    @Override
    public void display() {
        String status;
        if (isAvailable) {
            status = "Available";
        } else {
            status ="Checked Out by "+ currentBorrowerId + " | Due: "+ dueDate;
        }
        System.out.println("Book ID: " + id + " | Title: " + title + " | Author: " + author + " | Status: " + status);
    }
    @Override
    public String getTypeCode() {
        return "BOOK";
    }
    @Override
    public void checkOut(Student s)
            throws InvalidTransactionException {
        if (!isAvailable) {
            throw new InvalidTransactionException("Book already checked out.");
        }
        isAvailable = false;
        dueDate = LocalDate.now().plusDays(7);
        currentBorrowerId = s.getId();
        s.addBorrowedItem(this);
        System.out.println("Book borrowed successfully.");
    }
    @Override
    public void returnItem(Student s) {
        LocalDate today = LocalDate.now();
        // Fine Logic
        if (dueDate != null && today.isAfter(dueDate)) {
            long lateDays = today.toEpochDay() - dueDate.toEpochDay();
            double fine = lateDays * 300;
               s.addFine(fine);
                System.out.println("Late Return! Fine Added: "+ fine);
        }
        isAvailable = true;
        dueDate = null;
        currentBorrowerId = "None";
        s.removeBorrowedItem(this);
        System.out.println("Book returned successfully.");
    }
    public String getAuthor() {
    return author;
}
}
// Ebook Class
class EBook extends Item {
    public EBook(
            String id,
            String title
    ) {
        super(id, title);
    }
    @Override
    public void display() {
        System.out.println("EBook ID: " + id + "| Title: " + title + " | Digital Access");
    }
    @Override
    public String getTypeCode() {
        return "EBOOK";
    }
    @Override
    public void returnItem(Student s) {
        System.out.println("EBooks do not require return.");
    }
}
// Student Class
class Student {
    private String id;
    private String name;
    private List<Item> borrowedItems = new ArrayList<>();
    private double fineBalance = 0;
    public Student(String id,String name) {
        this.id = id;
        this.name = name;
    }
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public double getFineBalance() {
        return fineBalance;
    }
    public void setFineBalance(double fine) {
        this.fineBalance = fine;
    }
    public int getBorrowedCount() {
        return borrowedItems.size();
    }
    public void addBorrowedItem(Item i) {
        borrowedItems.add(i);
    }
    public void removeBorrowedItem(Item i) {
        borrowedItems.remove(i);
    }
    public void addFine(double amount) {
        fineBalance = fineBalance +amount;
    }
    public void showStatus() {
        System.out.println("\n*** STUDENT STATUS ***");
        System.out.println( "Name: " + name);
        System.out.println("ID: " + id);
        System.out.println("Borrowed Books: "+ borrowedItems.size()+ "/3");
        System.out.println("Fine Balance: "+ fineBalance);
        if (borrowedItems.isEmpty()) {
            System.out.println("No borrowed books.");
        } else {
            System.out.println("\nBorrowed Items:");
            for (Item i : borrowedItems) {
                System.out.println("- "+ i.title+ " ("+ i.id+ ")");
            }
        }
    }
}
// Library Manager
class LibraryManager {
    private List<Item> catalog = new ArrayList<>();
    private List<Student> students = new ArrayList<>();
    // Methods
    public void addItem(Item i) {
        catalog.add(i);
    }
    public void addStudent(Student s) {
        students.add(s);
    }
    public int findItemCount() {
        return catalog.size();
    }
    public int findStudentCount() {
        return students.size();
    }
    // Search
    public void search(String query) {
        String q = query.toLowerCase().trim();
        boolean found = false;
        System.out.println("\n*** SEARCH RESULTS ***");
        for (Item i : catalog) {
            if (i.title.toLowerCase().contains(q)) {
                i.display();
                found = true;
            }}
        if (!found) {
            System.out.println("No matching item found.");
        }
    }
    // Save Students File
    public void saveStudentsFile() {
        try (PrintWriter out = new PrintWriter(new FileWriter("students.txt"))) {
            for (Student s : students) {
                out.println(s.getId()+ ","+ s.getName()+ ","+ s.getFineBalance());
            }
            System.out.println("Students saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving students file.");
        }
    }
    // Load Students File
    public void loadStudentsFile() {
        File file = new File("students.txt");
        if (!file.exists() || file.length() == 0) {
            return;
        }
        try (Scanner sc = new Scanner(file)
        ) {
            students.clear();
            while (sc.hasNextLine()) {
                String[] p = sc.nextLine().split(",");
                Student s = new Student(p[0],p[1]);
                s.setFineBalance(Double.parseDouble(p[2]));
                students.add(s);
            }
            System.out.println("Students restored successfully.");
        } catch (Exception e) {
            System.out.println("Error loading students file.");
        }
    }
    // Load Catalog File
   public void loadFromCatalogFile() {
    File file = new File("catalog.txt");
    if (!file.exists() || file.length() == 0) {
        return;
    }
    try (Scanner sc = new Scanner(file)) {
        catalog.clear();
        while (sc.hasNextLine()) {
            String[] p = sc.nextLine().split(",");
            if (p.length < 7)
                continue;
            String type = p[0];
            String id = p[1];
            String title = p[2];
            String author = p[3];
            boolean available = Boolean.parseBoolean(p[4]);
            String borrower = p[5];
            LocalDate date =p[6].equals("NULL")? null: LocalDate.parse(p[6]);Item newItem;
            if (type.equals("BOOK")) {
                newItem = new Book(id, title, author);
            } else {
                newItem = new EBook(id, title);
            }
            newItem.isAvailable = available;
            newItem.currentBorrowerId = borrower;
            newItem.dueDate = date;
            if (!borrower.equals("None")) {
                try {
                    Student st = findStudent(borrower);
                    st.addBorrowedItem(newItem);
                } catch (Exception e) {
                    System.out.println("Borrow restoration failed.");
                }
            } catalog.add(newItem);
        }
        System.out.println("Catalog restored successfully.");
    } catch (Exception e) {
        System.out.println("Error loading catalog file.");
    }}
    // Save Catalog File
    public void saveToCatalogFile() {
    try (PrintWriter out = new PrintWriter(new FileWriter("catalog.txt"))) {
        for (Item i : catalog) {
            String dateString = (i.dueDate == null) ? "NULL" : i.dueDate.toString();
            if (i instanceof Book) {
                Book b = (Book) i;
                out.println(i.getTypeCode() + "," +i.id + "," +i.title + "," +b.getAuthor() + "," +i.isAvailable + "," +i.currentBorrowerId + "," +dateString);
            } else {
                out.println(i.getTypeCode() + "," +i.id + "," +i.title + "," +"NONE" + "," +i.isAvailable + "," +i.currentBorrowerId + "," +dateString);
            }}
        System.out.println("Catalog saved successfully.");
    } catch (IOException e) {
      System.out.println("Error saving catalog.");
    }
}
    // Borrow Book
    public void processBorrow(String itemId,String studentId) {
        try { Student st = findStudent(studentId);
            Item it = findItem(itemId);
            // Borrow Limit
            if ( st.getBorrowedCount() >= 3) {
                throw new InvalidTransactionException(
                        "Borrow limit reached."
                );
            }
            if (it instanceof ILoanable) {
                ( (ILoanable) it).checkOut(st);
            } else {
                 System.out.println( "Digital Copy Shared");
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    } // Return Book
    public void processReturn(String itemId, String studentId ) {
        try {
            Item item = findItem(itemId);
            Student st = findStudent(studentId);
            item.returnItem(st);
        } catch (Exception e) { 
                System.out.println( "ERROR: "+ e.getMessage() );
        }}
        public Student findStudent(String sid) throws Exception {   // Find student
        for (Student s : students) {
            if (s.getId().equalsIgnoreCase(sid)) {
                return s;
            }
        } throw new Exception("Student not found.");
    }
    public Item findItem(String bid) throws Exception {   // Find Book
        for (Item i : catalog) {
            if ( i.id.equalsIgnoreCase(bid)) {
                return i;
            }
        } throw new Exception("Item not found." );
    } // Show Catalog
    public void showFullCatalog() {  
        //sorting algorith
        catalog.sort(Comparator.comparing(i -> i.title));
        System.out.println("\n*** Catalog ***");
        for (Item i : catalog) {
            i.display();
        }}} //Main class
public class LibraryApp {  
    public static void main(String[] args) {
        LibraryManager lib = new LibraryManager();
        Scanner sc = new Scanner(System.in);
        lib.loadStudentsFile();  // Load Students 
        if (lib.findStudentCount() == 0) {   //Default students
            lib.addStudent(new Student("1","Mazz"));
             lib.addStudent(new Student("2","Sara"));
        }
        lib.loadFromCatalogFile();  // Load Catalog
        if (lib.findItemCount() == 0) {
            lib.addItem(new Book("101","Java","ALi" ));
            lib.addItem(new Book("102","Data","Zain") );
            lib.addItem(new EBook( "e01","Design") );
            lib.addItem(new EBook( "e02","Devil") );
              lib.addItem(new Book("103","English","Fara") );
                lib.addItem(new Book("104","Urdu","Faize") );
        }
        while (true) {  // Menu
            System.out.println("\n*** NIT LIBRARY ***");
            System.out.println( "1. Search");
            System.out.println("2. Borrow");
            System.out.println("3. Return");
            System.out.println("4. Student Status");
            System.out.println("5. Show Catalog");
            System.out.println("6. Save & Exit");
            System.out.print( "Enter Choice: ");
            String choice =sc.nextLine();
            if (choice.equals("1")) {
                System.out.print("Enter Keyword: ");
                lib.search(sc.nextLine());
        }
            else if (choice.equals("2")) {
                System.out.print( "Enter Item ID: ");
                String bid = sc.nextLine();
                System.out.print( "Enter Student ID: " );
                String sid = sc.nextLine();
                lib.processBorrow(bid,sid);
            }
            else if (choice.equals("3")) {
                System.out.print("Enter Item ID: ");
                String bid = sc.nextLine();
                System.out.print( "Enter Student ID: ");
                String sid = sc.nextLine();
                lib.processReturn( bid,sid);
            }
            else if (choice.equals("4")) {
                System.out.print("Enter Student ID: ");
                String sid = sc.nextLine();
                try {
                    lib.findStudent(sid).showStatus();
                } catch (Exception e) {
                 System.out.println(e.getMessage());
                }}
            else if (choice.equals("5")) {
                lib.showFullCatalog();}
            else if (choice.equals("6")) {
                lib.saveStudentsFile();
                lib.saveToCatalogFile();
                System.out.println( "Data saved successfully.");
                System.out.println( "Library Closed");
                break; } 
            else {
                System.out.println( "Invalid choice.");
            }
        }
        sc.close();
    }
}