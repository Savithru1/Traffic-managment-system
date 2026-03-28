package project;

import java.io.*;
import java.util.*;

class IntersectionManager {

    String[] intersections = {
            "HITEC City Junction", "Jubilee Hills Check Post", "Banjara Hills Road No. 12",
            "Gachibowli Outer Ring Road", "Madhapur Mindspace", "Kukatpally JNTU",
            "Ameerpet Crossroads", "Secunderabad Clock Tower", "Miyapur Metro Station",
            "Kondapur RTO Office"
    };

    void displayIntersections() {
        System.out.println("\n====== Hyderabad City - Road Intersections ======");
        for (int i = 0; i < intersections.length; i++) {
            System.out.println("  Zone " + (i + 1) + " --> " + intersections[i]);
        }
        System.out.println("=================================================\n");
    }
}

class IncidentNode {
    String incidentType;
    int location; 
    int priority;
    IncidentNode next;
}

class ViewStackNode {
    IncidentNode data;
    ViewStackNode next;
}

class ViewStack {
    ViewStackNode top = null;
    int size = 0;
    int limit = 3;

    void push(IncidentNode node) {
        ViewStackNode newNode = new ViewStackNode();
        newNode.data = node;
        newNode.next = top;
        top = newNode;
        size++;

        if (size > limit) {
            removeBottom();
        }
    }

    void removeBottom() {
        if (top == null)
            return;
        if (top.next == null) {
            top = null;
            size = 0;
            return;
        }
        ViewStackNode temp = top;
        while (temp.next.next != null) {
            temp = temp.next;
        }
        temp.next = null;
        size--;
    }

    void display(IntersectionManager im) {
        if (top == null) {
            System.out.println("\n  No recently reported incidents yet.");
            return;
        }
        System.out.println("\n====== Last 3 Reported Incidents (Most Recent First) ======");
        ViewStackNode temp = top;
        int rank = 1;
        while (temp != null) {
            System.out.println("  #" + rank + "  " + temp.data.incidentType
                    + "  |  P" + temp.data.priority
                    + "  |  " + im.intersections[temp.data.location - 1]);
            rank++;
            temp = temp.next;
        }
        System.out.println("==========================================================\n");
    }
}

class IncidentLog {
    IncidentNode head = null;
    IntersectionManager im;
    ViewStack recentStack = new ViewStack();
    String desktopPath = "C:\\Users\\sasha\\OneDrive\\Desktop\\project.txt";

    IncidentLog(IntersectionManager im) {
        this.im = im;
    }

    void loadFromFile() {
        try {
            File locFile = new File(desktopPath);
            if (!locFile.exists())
                return;
            BufferedReader br = new BufferedReader(new FileReader(locFile));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    try {
                        IncidentNode newNode = new IncidentNode();
                        newNode.incidentType = parts[0];
                        newNode.location = Integer.parseInt(parts[1]);
                        newNode.priority = Integer.parseInt(parts[2]);
                        newNode.next = null;
                        insertWithPriority(newNode);
                    } catch (NumberFormatException nfe) {
                        System.out.println("  [Warning] Skipping old data format: " + line);
                    }
                }
            }
            br.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    void appendToFile(IncidentNode node) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(desktopPath, true));
            bw.write(node.incidentType + "," + node.location + "," + node.priority);
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            System.out.println("  [ERROR] Could not save incident to file: " + e.getMessage());
        }
    }

    void saveAllToFile() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(desktopPath, false));
            IncidentNode current = head;
            while (current != null) {
                bw.write(current.incidentType + "," + current.location + "," + current.priority);
                bw.newLine();
                current = current.next;
            }
            bw.close();
        } catch (IOException e) {
            System.out.println("  [ERROR] Could not update file: " + e.getMessage());
        }
    }

    void insertWithPriority(IncidentNode newNode) {
        if (head == null || newNode.priority < head.priority) {
            newNode.next = head;
            head = newNode;
        } else {
            IncidentNode current = head;
            while (current.next != null && current.next.priority <= newNode.priority) {
                current = current.next;
            }
            newNode.next = current.next;
            current.next = newNode;
        }
    }

    void createLog() {
        Scanner sc = new Scanner(System.in);
        System.out.print("\nHow many incidents do you want to add right now? : ");
        while (!sc.hasNextInt()) {
            System.out.print("  Invalid input. Enter a number: ");
            sc.next();
        }
        int c = sc.nextInt();
        sc.nextLine();

        for (int i = 1; i <= c; i++) {
            System.out.println("\n-- Incident " + i + " --");
            IncidentNode newNode = new IncidentNode();

            System.out.print("Type of incident (e.g. Pothole, Accident, Flooding): ");
            newNode.incidentType = sc.nextLine();

            im.displayIntersections();
            System.out.print("Where did it happen? (Enter Zone Number 1-10): ");
            int loc = -1;
            while (true) {
                if (sc.hasNextInt()) {
                    loc = sc.nextInt();
                    if (loc >= 1 && loc <= 10) {
                        break;
                    }
                } else {
                    sc.next();
                }
                System.out.print("  Invalid input. Please enter a registered Zone Number (1-10): ");
            }
            sc.nextLine();
            newNode.location = loc;

            System.out.print("Priority level? (1=Critical  2=High  3=Medium  4=Low): ");
            int prio = -1;
            while (true) {
                if (sc.hasNextInt()) {
                    prio = sc.nextInt();
                    if (prio >= 1 && prio <= 4) {
                        break;
                    }
                } else {
                    sc.next();
                }
                System.out.print("  Invalid Priority. Enter a number (1-4): ");
            }
            sc.nextLine();
            newNode.priority = prio;

            newNode.next = null;
            insertWithPriority(newNode);
            appendToFile(newNode);
            recentStack.push(newNode);
        }
        System.out.println("\nIncidents added to the log!");
    }

    void display() {
        System.out.println();
        if (head == null) {
            System.out.println("  (No incidents in the log right now)");
        } else {
            System.out.println("====== Active Incidents (Priority Queue) ======");
            IncidentNode current = head;
            while (current != null) {
                System.out.println("  [P" + current.priority + "] " + current.incidentType + " ==> " + im.intersections[current.location - 1]);
                current = current.next;
            }
            System.out.println("===============================================\n");
        }
    }

    int countIncidents() {
        int count = 0;
        IncidentNode current = head;
        while (current != null) {
            count++;
            current = current.next;
        }
        System.out.println("\n  Total incidents currently logged: " + count);
        return count;
    }

    void reportNewIncident() {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n--- Report a New Incident ---");
        IncidentNode newNode = new IncidentNode();

        System.out.print("Incident type: ");
        newNode.incidentType = sc.nextLine();

        im.displayIntersections();
        System.out.print("Location (Enter Zone Number 1-10): ");
        int loc = -1;
        while (true) {
            if (sc.hasNextInt()) {
                loc = sc.nextInt();
                if (loc >= 1 && loc <= 10) {
                    break;
                }
            } else {
                sc.next();
            }
            System.out.print("  Invalid input. Please enter a registered Zone Number (1-10): ");
        }
        sc.nextLine();
        newNode.location = loc;

        System.out.print("Priority (1=Critical  2=High  3=Medium  4=Low): ");
        int prio = -1;
        while (true) {
            if (sc.hasNextInt()) {
                prio = sc.nextInt();
                if (prio >= 1 && prio <= 4) {
                    break;
                }
            } else {
                sc.next();
            }
            System.out.print("  Invalid Priority. Enter a number (1-4): ");
        }
        sc.nextLine();
        newNode.priority = prio;

        newNode.next = null;
        insertWithPriority(newNode);
        appendToFile(newNode);
        recentStack.push(newNode);

        System.out.println("\n[OK] Incident added! Updated queue:");
        display();
    }

    void searchIncident() {
        if (head == null) {
            System.out.println("\n  Log is empty. Nothing to search.");
            return;
        }

        Scanner sc = new Scanner(System.in);
        System.out.print("\nEnter the incident type to search (case-sensitive): ");
        String search = sc.nextLine();
        int position = 1;
        IncidentNode current = head;
        boolean found = false;

        while (current != null) {
            if (current.incidentType.equals(search)) {
                System.out.println("  >> Found: '" + search + "' at queue position " + position
                        + " | Priority: P" + current.priority
                        + " | Location: " + im.intersections[current.location - 1]);
                found = true;
            }
            current = current.next;
            position++;
        }

        if (!found) {
            System.out.println("  >> '" + search + "' not found in the incident log.");
        }
    }

    void deleteIncident() {
        if (head == null) {
            System.out.println("\n  Log is empty. Nothing to delete.");
            return;
        }

        Scanner sc = new Scanner(System.in);
        System.out.println("\n====== Select an Incident to Delete ======");
        IncidentNode current = head;
        int pos = 1;
        while (current != null) {
            System.out.println("  " + pos + ". [P" + current.priority + "] "
                    + current.incidentType + " ==> " + im.intersections[current.location - 1]);
            current = current.next;
            pos++;
        }

        int total = pos - 1;
        System.out.print("\nEnter the number of the incident to delete (1-" + total + "): ");
        while (!sc.hasNextInt()) {
            System.out.print("  Please enter a valid number: ");
            sc.next();
        }
        int choice = sc.nextInt();
        sc.nextLine();

        if (choice < 1 || choice > total) {
            System.out.println("  Invalid selection. No incident deleted.");
            return;
        }

        if (choice == 1) {
            System.out.println("\n  [DELETED] '" + head.incidentType + "' has been removed.");
            head = head.next;
        } else {
            IncidentNode prev = head;
            for (int i = 1; i < choice - 1; i++) {
                prev = prev.next;
            }
            IncidentNode toDelete = prev.next;
            System.out.println("\n  [DELETED] '" + toDelete.incidentType + "' has been removed.");
            prev.next = toDelete.next;
        }

        saveAllToFile();
        System.out.println("  File updated. Remaining incidents:");
        display();
    }
}

public class mainproj {
    public static void main(String[] args) {

        IntersectionManager im = new IntersectionManager();
        IncidentLog log = new IncidentLog(im);
        log.loadFromFile();

        Scanner sc = new Scanner(System.in);

        System.out.print("Do you want to add some incidents now before starting? (y/n): ");
        String initChoice = sc.nextLine();
        if (initChoice.equalsIgnoreCase("y")) {
            log.createLog();
        }
        int opt;
        do {
            System.out.println("\nMAIN MENU");
            System.out.println("-----------------------------------");
            System.out.println("1. Add a new incident");
            System.out.println("2. See total incident count");
            System.out.println("3. View all incidents (queue)");
            System.out.println("4. Search for an incident by type");
            System.out.println("5. Delete an incident");
            System.out.println("6. View last 3 reported incidents");
            System.out.println("7. Exit");
            System.out.print("Enter your choice: ");

            while (!sc.hasNextInt()) {
                System.out.print("  Please enter a valid number (1-7): ");
                sc.next();
            }
            opt = sc.nextInt();
            sc.nextLine();

            switch (opt) {
                case 1:
                    log.reportNewIncident();
                    break;
                case 2:
                    log.countIncidents();
                    break;
                case 3:
                    log.display();
                    break;
                case 4:
                    log.searchIncident();
                    break;
                case 5:
                    log.deleteIncident();
                    break;
                case 6:
                    log.recentStack.display(im);
                    break;
                case 7:
                    System.out.println("\nExiting... Goodbye!");
                    break;
                default:
                    System.out.println("  Invalid choice. Please pick 1 to 7.");
            }
        } while (opt != 7);
        sc.close();
    }
}