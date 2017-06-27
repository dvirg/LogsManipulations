import java.io.*;
import java.util.*;
public class LogsManipulations {
    private static HashMap<String, Integer> wordHashMap = new HashMap<>();
    static ArrayList<String> dictionary = new ArrayList<>();
    private static List<SentenceIndexed> sentenceIndexedList = new ArrayList<>();
    private static List<SimilarSentences> similarSentencesAllLists = new ArrayList<>();
    private static BufferedWriter outputFileWriter = null;
    public static void main(String [] args) throws IOException {
        List<String> inputLines = getFromInput();
        try {
            prepareOutputFile();
            for (String line : inputLines) {
                String[] splitWordsOfLine = line.split(" ");
                List<Integer> wordIndexesList = new ArrayList<>();
                for(int i = 2 ; i < splitWordsOfLine.length ; i++){
                    String word = splitWordsOfLine[i];
                    Integer index = wordHashMap.get(word);
                    if (index == null) {
                        index = dictionary.size();
                        dictionary.add(word);
                        wordHashMap.put(word, index);
                    }
                    wordIndexesList.add(index);
                }
                SentenceIndexed currentSentenceIndexed = new SentenceIndexed(wordIndexesList, line);
                Set<Integer> indexesSet = new HashSet<>();
                for (SentenceIndexed sentenceIndexed : sentenceIndexedList) {
                    int differenceIndex = sentenceIndexed.isMatch(wordIndexesList);
                    if (differenceIndex == -1) {
                        continue;
                    }
                    if(indexesSet.contains(differenceIndex)){
                    	continue;
                    }
                    indexesSet.add(differenceIndex);
                    SimilarSentences similarSentences = sentenceIndexed.getSimilarSentences(differenceIndex);
                    if(similarSentences == null){
                    	similarSentences = new SimilarSentences(sentenceIndexed, currentSentenceIndexed, differenceIndex);
                    	sentenceIndexed.add(similarSentences, differenceIndex);
                    }
                    else{
                    	similarSentences.add(currentSentenceIndexed);
                    }
                    currentSentenceIndexed.add(similarSentences, differenceIndex);
                    break;
                }
                sentenceIndexedList.add(currentSentenceIndexed);
            }
            printResult();
        } finally {
            closeOutputFile();
        }
    }
    private static void closeOutputFile() throws IOException {
        if(outputFileWriter != null) {
            outputFileWriter.close();
        }
    }
    private static void prepareOutputFile() throws FileNotFoundException, UnsupportedEncodingException {
        outputFileWriter = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("output.txt"), "utf-8"));
    }
    private static void printResult() throws IOException {
        for(SimilarSentences similarSentences : similarSentencesAllLists){
            printSentencesGroup(similarSentences);
            printLine("");
        }
    }
    private static void printLine(String msg) throws IOException {
        outputFileWriter.write(msg + "\n");
        System.out.println(msg);
    }
    private static void printSentencesGroup(SimilarSentences similarSentences) throws IOException {
        Set<String> differentWords = new TreeSet<>();
        for(SentenceIndexed sentenceIndexed : similarSentences.similarSentenceList){
            printLine(sentenceIndexed.line);
            differentWords.add(sentenceIndexed.getWordInIndex(similarSentences.differenceLocation));
        }
        printMsg("The changing word was: ");
        boolean isFirstWord = true;
        for(String word : differentWords){
            if(isFirstWord){
                isFirstWord = false;
                printMsg(word);
            }
            else {
                printMsg(", " + word);
            }
        }
        printLine("");
    }
    private static void printMsg(String msg) throws IOException {
        outputFileWriter.write(msg);
        System.out.print(msg);
    }
    private static List<String> getFromInput() throws IOException {
        List<String> lines = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader("input.txt"))) {
            String line = br.readLine();
            while(line != null){
            	lines.add(line);
            	line = br.readLine();
            }
        }
        return lines;
    }
    public static class SentenceIndexed implements Comparable<SentenceIndexed> {
        List<Integer> wordIndexesList;
        HashMap<Integer, SimilarSentences> similarSentencesMap;
        String line;
        SentenceIndexed(List<Integer> wordIndexesList, String line){
            this.wordIndexesList = wordIndexesList;
            similarSentencesMap = new HashMap<>();
            this.line = line;
        }
        int isMatch(List<Integer> wordIndexesList) {
            if(wordIndexesList.size() != this.wordIndexesList.size()){
                return -1;
            }
            int indexFound = -1;
            for(int i = 0 ; i < this.wordIndexesList.size() ; i++){
                if(!this.wordIndexesList.get(i).equals(wordIndexesList.get(i))){
                    if(indexFound != -1){
                        return -1;
                    }
                    indexFound = i;
                }
            }
            return indexFound;
        }
        SimilarSentences getSimilarSentences(int differenceIndex) {
            return similarSentencesMap.get(differenceIndex);
        }
        public void add(SimilarSentences similarSentences, int differenceIndex) {
            similarSentencesMap.put(differenceIndex, similarSentences);
        }

        String getWordInIndex(int differenceLocation) {
            int indexOfDictionary = wordIndexesList.get(differenceLocation);
            return dictionary.get(indexOfDictionary);
        }
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((line == null) ? 0 : line.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SentenceIndexed other = (SentenceIndexed) obj;
			if (line == null) {
				if (other.line != null)
					return false;
			} else if (!line.equals(other.line))
				return false;
			return true;
		}
		@Override
		public int compareTo(SentenceIndexed other) {
			return this.line.compareTo(other.line);
		}
        
    }
    public static class SimilarSentences {
        int differenceLocation;
        Set<SentenceIndexed> similarSentenceList;
        public SimilarSentences(SentenceIndexed sentenceIndexed1, SentenceIndexed sentenceIndexed2, int differenceLocation){
            similarSentenceList = new TreeSet<>();
            similarSentenceList.add(sentenceIndexed1);
            similarSentenceList.add(sentenceIndexed2);
            this.differenceLocation = differenceLocation;
            similarSentencesAllLists.add(this);
        }
        public void add(SentenceIndexed sentenceIndexed) {
            similarSentenceList.add(sentenceIndexed);
        }
    }
}
