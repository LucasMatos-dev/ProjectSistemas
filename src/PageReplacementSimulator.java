import java.util.*;

public class PageReplacementSimulator {

    public static void main(String[] args) {
        int[] pages = {7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2, 1, 7, 0, 1};  // Sequência de páginas
        int frameSize = 3;  // Número de frames na memória

        int fifoFaults = fifo(pages, frameSize);
        System.out.println("Método FIFO - " + fifoFaults + " faltas de página");
        int clockFaults = clock(pages, frameSize);
        System.out.println("Método Clock - " + clockFaults + " faltas de página");
        int agingFaults = aging(pages, frameSize);
        System.out.println("Método Aging - " + agingFaults + " faltas de página");
        int nfuFaults = nfu(pages, frameSize);
        System.out.println("Método NFU - " + nfuFaults + " faltas de página");
    }

    public static int fifo(int[] pages, int frameSize) {
        int pageFaults = 0;
        HashSet<Integer> pagesInMemory = new HashSet<>(frameSize);
        Queue<Integer> pageQueue = new LinkedList<>();

        for (int page : pages) {
            if (!pagesInMemory.contains(page)) {
                if (pagesInMemory.size() == frameSize) {
                    int oldestPage = pageQueue.poll();
                    pagesInMemory.remove(oldestPage);
                }
                pagesInMemory.add(page);
                pageQueue.add(page);
                pageFaults++;
            }
        }
        return pageFaults;
    }

    public static int clock(int[] pages, int frameSize) {
        int pageFaults = 0;
        int[] pageFrame = new int[frameSize];
        boolean[] secondChance = new boolean[frameSize];
        int pointer = 0;
        Arrays.fill(pageFrame, -1);

        for (int page : pages) {
            int searchIdx = -1;
            for (int i = 0; i < frameSize; i++) {
                if (pageFrame[i] == page) {
                    searchIdx = i;
                    break;
                }
            }

            if (searchIdx != -1) { // Page found
                secondChance[searchIdx] = true;
            } else { 
                while (secondChance[pointer]) {
                    secondChance[pointer] = false;
                    pointer = (pointer + 1) % frameSize;
                }
                pageFrame[pointer] = page;
                secondChance[pointer] = true;
                pointer = (pointer + 1) % frameSize;
                pageFaults++;
            }
        }
        return pageFaults;
    }

    public static int aging(int[] pages, int frameSize) {
        int[] pageFrame = new int[frameSize];
        int[] age = new int[frameSize];
        int pageFaults = 0;
        Arrays.fill(pageFrame, -1);

        for (int currentPage : pages) {
            int minIndex = -1;
            int minAge = Integer.MAX_VALUE;

            for (int i = 0; i < frameSize; i++) {
                if (pageFrame[i] == currentPage) {
                    age[i] = (age[i] >>> 1) | 128; 
                    minIndex = -1;
                    break;
                }
                if (age[i] < minAge) { 
                    minAge = age[i];
                    minIndex = i;
                }
            }

            if (minIndex != -1) { 
                pageFrame[minIndex] = currentPage;
                age[minIndex] = 128; 
                pageFaults++;
            }

            // Age all pages
            for (int i = 0; i < frameSize; i++) {
                if (i != minIndex) {
                    age[i] >>>= 1; 
                }
            }
        }
        return pageFaults;
    }

    public static int nfu(int[] pages, int frameSize) {
        int[] pageFrame = new int[frameSize];
        int[] frequencies = new int[frameSize];
        int pageFaults = 0;
        Arrays.fill(pageFrame, -1);

        for (int currentPage : pages) {
            boolean pageFound = false;
            for (int i = 0; i < frameSize; i++) {
                if (pageFrame[i] == currentPage) {
                    frequencies[i]++;  
                    pageFound = true;
                    break;
                }
            }
            
            if (!pageFound) { 
                int minIndex = 0;
                int minFreq = Integer.MAX_VALUE;
                for (int i = 0; i < frameSize; i++) {
                    if (pageFrame[i] == -1) {  
                        minIndex = i;
                        break;
                    } else if (frequencies[i] < minFreq) {
                        minFreq = frequencies[i];
                        minIndex = i;
                    }
                }
                pageFrame[minIndex] = currentPage; 
                frequencies[minIndex] = 1;  
                pageFaults++;
            }
        }
        return pageFaults;
    }
}
