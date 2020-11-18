package rhymeapp;
import backend.*;
import java.util.*;
import java.io.*;

class cmdLineApp
{
    Transcriptor tr;
    Stats st;
    Scoring sc;
    Detector det;

    cmdLineApp() {

    }

    ArrayList<PLine> readFile( String filename )
        throws IOException
    {
        FileReader reader = new FileReader( new File( filename ) );
        BufferedReader input = new BufferedReader( reader );
        ArrayList<PLine> inLines = new ArrayList<PLine>();

        String line = null;
        while( true ) {
            line = input.readLine();
            if ( line == null ) break;
            inLines.add( tr.transcribe( line ) );
        }

        input.close();

        return inLines;
    }

    public int run( String[] args ) throws Exception
    {
        String STATS_FILE = "iterationStatsUF.txt";
        st = new Stats(STATS_FILE);
        tr = new Transcriptor();
        sc = new Scoring(st, Stats.SPLIT);
        det = new Detector(sc);

        String inputStr = args[0];
        String[] plainLines = inputStr.split("\n");
        if (plainLines.length < 2) return 0;
        ArrayList<PLine> inLines = new ArrayList<PLine>();
        for (int i=0; i<plainLines.length; i++) {
            inLines.add(tr.transcribe(plainLines[i]));
        }
        Analyzer an = new Analyzer("Input Lines", sc);
        String outLine = "";
        RhymeCollection rc = det.getRhymes(inLines);
        rc.lines = inLines;
        if (!an.addRhymes(rc)) {
            System.out.println("Could not add rhymes to Analyzer.");
        } else {
            Analysis anOut = an.createAnalysis();
            if (anOut == null) {
                outLine = "Rhyme_Density: 0";
            } else {
                outLine = anOut.toString();
            }
        }
        System.out.println(outLine);
        return 0;
        // // OLD STUFF
        // st = new Stats(mainUI.STATS_FILE);
        // tr = new Transcriptor();
        // sc = new Scoring(st, Stats.SPLIT);
        // det = new Detector(sc);

        // ArrayList<PLine> lines = readFile( args[0] );
        // Iterator<PLine> iter = lines.iterator();
        // while( iter.hasNext() ) {
        //     PLine line = iter.next();
        // }

        // RhymeCollection rc = det.getRhymes(lines);
        // rc.lines = lines;
        // output(rc);

        // return 0;
    }

    private int wordIndex(PLine pl, int sylIndex) {
        int ret = -1;
        int sylLoc = -1;
        while (sylLoc<sylIndex) {
            ret++;
            sylLoc += pl.get(ret).numSyls();
        }
        return ret;
    }

    String getWords( ArrayList<PLine> lines, int line, int start, int end )
    {
        String word = "";
        for( int i = start; i <= end; i++ ) {
            if ( i > start ) {
                word += " ";
            }
            word += lines.get( line ).get(i).getPlainWord();
        }
        return word.toLowerCase();
    }

    public void output( RhymeCollection rc )
    {
        Map<String, String> previous = new HashMap<String, String>();
        for (int i=0; i<rc.lines.size(); i++) {
            ArrayList<Rhyme> curLineRhymes = rc.collection[i];

            for (int j=0; j<curLineRhymes.size(); j++) {
                Rhyme r = curLineRhymes.get(j);
                int firstWordA = wordIndex(rc.lines.get(i),r.aStart.syllable);
                int lastWordA = wordIndex(rc.lines.get(i),r.aEnd().syllable);
                int firstWordB;
                int lastWordB;

                String a = getWords( rc.lines, i, firstWordA, lastWordA );
                String b;

                if (r.aStart.sameLine(r.bStart)) {
                    firstWordB = wordIndex(rc.lines.get(i),r.bStart.syllable);
                    lastWordB = wordIndex(rc.lines.get(i), r.bEnd().syllable);
                    b = getWords( rc.lines, i, firstWordB, lastWordB );
                } else {
                    firstWordB = wordIndex(rc.lines.get(i+1),r.bStart.syllable);
                    lastWordB = wordIndex(rc.lines.get(i+1),r.bEnd().syllable);
                    b = getWords( rc.lines, i+1, firstWordB, lastWordB );
                }

                if ( a.equals( b ) ) {
                    continue;
                } else if ( b.compareTo( a ) < 0 ) {
                    String temp = a;
                    a = b;
                    b = temp;
                }

                System.out.format( "%s/%s\n", a, b);
            }
        }
    }
}
