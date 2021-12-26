package com.beta;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        BufferedReader br = null;
        ArrayList<String> teams = new ArrayList<>();
        String inStr;
        String[] strings;
        double[] rankPts;
        Array2DRowRealMatrix matrix;

        /*
         * By default, include matches played by a team as a surrogate when computing its OPR. To exclude
         * those matches, set includeSurrogates to false.
         */
        boolean includeSurrogates = true;

        String userHome = System.getProperty("user.home");
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter file name: ");
        String fileName = userHome + "\\" + sc.nextLine();
        if (!fileName.endsWith("csv") && !fileName.endsWith("CSV")){
            System.out.println("A comma separated values file is required.");
            return;
        }

        try {
            br = new BufferedReader(new FileReader(fileName));

            /*
             * The first line of input is a comma-separated list of all team numbers (as Strings).
             * Each team will have an index in the teams list. So for example, team i is the team whose
             * number appears as entry i in the teams list.
             */
            inStr = br.readLine();
            if (inStr == null) return;
            strings = inStr.split(",");
            for (String str: strings){
                teams.add(str.trim());
            }

            /*
             * Matrix entries are:
             *   m[i,j] = number of matches with (i,j) as alliance partners, for i NOT EQUAL to j
             *   Total number of matches played by team i, for i EQUAL TO j
             */
            matrix = new Array2DRowRealMatrix(teams.size(), teams.size());

            /*
             * rankPts entries are the cumulative scores for each team, for all matches played
             */
            rankPts = new double[teams.size()];

            /*
             * Populate the matrix and the rankPts array.
             */
            while ((inStr = br.readLine()) != null){
                strings = inStr.split(",");
                for (int i=0; i<strings.length; i++){
                    strings[i] = strings[i].trim();
                }
                boolean iSurrogate;
                boolean jSurrogate;
                int i,j;
                if (strings[0].endsWith("*")){
                    iSurrogate = true;
                    i = teams.indexOf(strings[0].substring(0, strings[0].length()-1));
                } else {
                    iSurrogate = false;
                    i = teams.indexOf(strings[0]);
                }
                if (strings[1].endsWith("*")){
                    jSurrogate = true;
                    j = teams.indexOf(strings[1].substring(0, strings[1].length()-1));
                } else {
                    jSurrogate = false;
                    j = teams.indexOf(strings[1]);
                }
                if (includeSurrogates || !iSurrogate) {
                    matrix.setEntry(i, j, matrix.getEntry(i, j) + 1.0);
                    matrix.setEntry(i, i, matrix.getEntry(i, i) + 1.0);
                    rankPts[i] += Double.parseDouble(strings[4]);
                }
                if (includeSurrogates || !jSurrogate) {
                    matrix.setEntry(j, i, matrix.getEntry(j, i) + 1.0);
                    matrix.setEntry(j, j, matrix.getEntry(j, j) + 1.0);
                    rankPts[j] += Double.parseDouble(strings[4]);
                }

                if (strings[2].endsWith("*")){
                    iSurrogate = true;
                    i = teams.indexOf(strings[2].substring(0, strings[2].length()-1));
                } else {
                    iSurrogate = false;
                    i = teams.indexOf(strings[2]);
                }
                if (strings[3].endsWith("*")){
                    jSurrogate = true;
                    j = teams.indexOf(strings[3].substring(0, strings[3].length()-1));
                } else {
                    jSurrogate = false;
                    j = teams.indexOf(strings[3]);
                }
                if (includeSurrogates || !iSurrogate) {
                    matrix.setEntry(i, j, matrix.getEntry(i, j) + 1.0);
                    matrix.setEntry(i, i, matrix.getEntry(i, i) + 1.0);
                    rankPts[i] += Double.parseDouble(strings[5]);
                }
                if (includeSurrogates || !jSurrogate) {
                    matrix.setEntry(j, i, matrix.getEntry(j, i) + 1.0);
                    matrix.setEntry(j, j, matrix.getEntry(j, j) + 1.0);
                    rankPts[j] += Double.parseDouble(strings[5]);
                }

            }

            /*
             * The opr calculation requires solving a system of linear equations in multiple variables, which can
             * be expressed in matrix form as:
             *
             *   rankPts = matrix * opr
             *
             * The opr vector (containing the opr values for each team) can therefore be calculated as:
             *
             *   opr = inverse_matrix * rankPts
             */

            RealVector opr = MatrixUtils.inverse(matrix).preMultiply(new ArrayRealVector(rankPts));

            StringBuilder sb = new StringBuilder();
            StringBuilder hdBld = new StringBuilder("         ");
            for (int i=0; i<teams.size(); i++) {
                sb.append("%8.0f");
                hdBld.append(String.format("%8s", teams.get(i)));
            }

            String fmtStr = sb.toString();
            System.out.print(hdBld.toString());
            for (int i=0; i<teams.size(); i++){
                String fmt = "\n" + String.format("%5s", teams.get(i)) + ": " + fmtStr;
                double[] row = matrix.getRow(i);
                Object[] rowObj = Arrays.stream(row).boxed().toArray(Double[]::new);
                System.out.printf(fmt, rowObj);
            }

            TeamData[] teamData = new TeamData[teams.size()];
            for (int i=0; i<teams.size(); i++){
                teamData[i] = new TeamData(teams.get(i), rankPts[i], opr.getEntry(i));
            }
            Arrays.sort(teamData,
                    new Comparator<TeamData>() {
                        @Override
                        public int compare(TeamData o1, TeamData o2) {
                            return o1.opr > o2.opr? -1 : o1.opr < o2.opr ? 1 : 0;
                        }
                    });

            System.out.println("\n\nOPR Values");

            for (int i=0; i<teams.size(); i++){
                System.out.printf("\nTeam: %5s   RankPts: %.0f   OPR: %.1f",
                        teamData[i].name, teamData[i].rankPts, teamData[i].opr);
            }


        } finally {
            if (br != null) {
                br.close();
                }
        }


    }

    static public class TeamData {
        public String name;
        public double rankPts;
        public double opr;

        public TeamData(String name, double rankPts, double opr){
            this.name = name;
            this.rankPts = rankPts;
            this.opr = opr;
        }
    }

}
