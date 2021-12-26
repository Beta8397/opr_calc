<h1>Command Line App to Compute Offensive Points Ranking (OPR)</h1>

Requires comma-separated values (CSV) input file. The first line of this file should be a comma-separated list
of all team numbers in the tournament. Each subsequent line corresponds to a single match, and includes the
numbers of all four teams, and the scores of both alliances, in the following order:

red_team1,red_team2,blue_team1,blue_team2,red_score,blue_score

If desired, a "*" can be placed at the end of a team to indicate that it played a specific match as a surrogate. 
By default, matches played as a surrogate will be included in a team's OPR calculation. To exclude surrogate matches, 
set the value of includeSurrogates to false in the code.

The app will prompt for an input file name. Specify a file relative to the user home folder, rather than an 
absolute path. For example, in Windows, instead of "C:\users\me\desktop\scores.csv", you would just enter 
"desktop\scores.csv" (but without the quotation marks).

The output consists of:

1. A matrix indicating the number of matches that each pair of teams played as alliance partners

2. For each team, total ranking points and OPR.