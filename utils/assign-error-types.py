import sys
import os
import getopt
import difflib
import re

def identifyMistakeTypes(file):
    with open(file, 'r', encoding='ISO-8859-1') as openFile :
        with open(file.replace(".csv", "")+'-error-annotation.csv', 'w') as outputFile :
            delimiter = ';'

            # Write file header
            outputFile.write('ID')
            outputFile.write(delimiter)
            outputFile.write('String with mistake(s)')
            outputFile.write(delimiter)
            outputFile.write('Possible Correction')
            outputFile.write(delimiter)

            outputFile.write('Spaces')
            outputFile.write(delimiter)
            outputFile.write('Special Characters')
            outputFile.write(delimiter)
            outputFile.write('Insertion')
            outputFile.write(delimiter)
            outputFile.write('Deletion')
            outputFile.write(delimiter)
            outputFile.write('Substitution')
            outputFile.write(delimiter)
            outputFile.write('Transposition')

            outputFile.write('\n')

            # go through each line
            for line in openFile :
                # Read file with on or more cols, in the first col the mistakes included
                # in the next one or more cols the corrections
                cleanLineStr = re.sub(r'\n', "", line)
                splitted = re.split(r'[\t;]', cleanLineStr.rstrip('\t'))
                #splitted = list(filter(None, splitted))

                mistakeString = splitted[1]
                for colIndex in range(2, min(len(splitted), 9)):
                    if len(splitted[colIndex]) > 0:
                        outputFile.write(splitted[0])
                        outputFile.write(delimiter)
                        outputFile.write(mistakeString)
                        outputFile.write(delimiter)
                        outputFile.write(splitted[colIndex])
                        outputFile.write(delimiter)

                        spaces, specialChars, cleanStr1, cleanStr2 = spacesAndSpecialCharacter(splitted[colIndex], mistakeString)
                        #print(mistakeString, " | ",splitted[colIndex])
                        distance, inserts, deletions, substitution, transpositions = damerauLevenshteinDistance(cleanStr1, cleanStr2)

                        outputFile.write(str(spaces))
                        outputFile.write(delimiter)
                        outputFile.write(str(specialChars))
                        outputFile.write(delimiter)
                        outputFile.write(str(inserts))
                        outputFile.write(delimiter)
                        outputFile.write(str(deletions))
                        outputFile.write(delimiter)
                        outputFile.write(str(substitution))
                        outputFile.write(delimiter)
                        outputFile.write(str(transpositions))

                        outputFile.write('\n')




"""
Compute the Damerau-Levenshtein distance between two given with backtrace string
strings (s1 and s2)
"""
def damerauLevenshteinDistance(s1, s2):
    d = {}
    lenstr1 = len(s1)
    lenstr2 = len(s2)
    for i in range(-1,lenstr1+1):
        d[(i,-1)] = i+1
    for j in range(-1,lenstr2+1):
        d[(-1,j)] = j+1

    for i in range(lenstr1):
        for j in range(lenstr2):
            if s1[i] == s2[j]:
                cost = 0
            else:
                cost = 1


            costDelete = d[(i-1,j)] + 1 # deletion
            costInsert = d[(i,j-1)] + 1 # insertion
            costSub = d[(i-1,j-1)] + cost # substitution
            op = min( costDelete, costInsert, costSub);
            d[(i,j)] = op

            if i and j and s1[i]==s2[j-1] and s1[i-1] == s2[j]:
                d[(i,j)] = min (d[(i,j)], d[i-2,j-2] + cost) # transposition
 
    
    backtraceStr = backtrace(lenstr1-1, lenstr2-1, s1, s2, d)
    
    inserts = len(backtraceStr.split('i')) - 1
    deletions = len(backtraceStr.split('d')) - 1
    substitution = len(backtraceStr.split('s')) - 1
    transpositions = len(backtraceStr.split('t')) - 1

    #print(s1)
    #print(s2)
    #print(backtraceStr)

    return (d[lenstr1-1,lenstr2-1], inserts, deletions, substitution, transpositions)


def backtrace(i, j, s1, s2, d):
    lens1 = len(s1)
    lens2 = len(s2)

    if i > -1 and d[(i-1,j)] + 1 == d[(i,j)] :
        return backtrace(i-1, j, s1, s2, d) + "d"

    if j > -1 and d[(i,j-1)] + 1 == d[(i,j)] :
        return backtrace(i, j-1, s1, s2, d) + "i"

    if i > -1 and j > -1 and d[(i-1, j-1)] + 1 == d[(i,j)] and i + 1 < lens1 and j + 1 < lens2 and s1[i] == s2[j+1] and s1[i+1] == s2[j]:
        return backtrace(i-1, j-1, s1, s2, d) + "t"

    if i > -1 and j > -1 and d[(i-1, j-1)] + 1 == d[(i,j)] :
        return backtrace(i-1, j-1, s1, s2, d) + "s"

    if i > -1 and j > -1 and  d[(i-1, j-1)] == d[(i,j)] :
        return backtrace(i-1, j-1, s1, s2, d) + "e"
    
    return ""
        


"""
1. Überprüfe die Länge der beiden Zeilen
2. Entferne alle Leerzeichen
3. Prüfe ob Länge und Menge beider Zeilen gleich ist, wenn ja -> Leerzeichen wurde eingefügt oder entfernt
5. Prüfen welche Zeile in Schritt 1 länger war -> ich weiß ob Leereichen Eingefügt oder Entfernt
1. Überprüfe die Länge der beiden Zeilen
2. Entferne Sonderzeichen wie ' - _ . : / ! ? $ % ( ) + # " &
3. Prüfe ob Länge und Menge beider Zeilen gleich ist, wenn ja -> Sonderzeichen wurde eingefügt oder entfernt
5. Prüfen welche Zeile in Schritt 1 länger war -> ich weiß ob Sonderzeichen Eingefügt oder Entfernt
"""

def spacesAndSpecialCharacter(s1, s2) :

    
    spaces = 0
    specialChars = 0

    lens1 = len(s1)
    lens2 = len(s2)


    tmpStr1 = s1.replace(" ", "")
    tmpStr2 = s2.replace(" ", "")

    lenstr1 = len(s1.split(' '))
    lenstr2 = len(s2.split(' '))
    
    spaces = abs(lenstr1 - lenstr2)

    lenstr1 = len(re.findall(r'[\'\-_|.:/!?$%()+#"&]{1,}', tmpStr1))
    lenstr2 = len(re.findall(r'[\'\-_|.:/!?$%()+#"&]{1,}', tmpStr2))

    specialChars = abs(lenstr1 - lenstr2)

    chars = ['\'', '-', '_', '.', ':', '/', '!', '?', '$', '%', '(', ')', '+', '#', '"', '&']

    for v in chars :
        tmpStr1 = tmpStr1.replace(str(v), "");
        tmpStr2 = tmpStr2.replace(str(v), "");
        

    return (spaces, specialChars, tmpStr1, tmpStr2)


#https://de.wikipedia.org/wiki/Levenshtein-Distanz
def main():
    # parse command line options
    try:
        opts, args = getopt.getopt(sys.argv[1:], "h", ["help"])
    except getopt.error:
        print("for help use --help")
        sys.exit(2)
    # process options
    for o, a in opts:
        if o in ("-h", "--help"):
            print(__doc__)
            sys.exit(0)

    # process arguments
    if len(args) != 1 :
        print("Missing arguments, please take a look in --help")
        sys.exit(2)
    else :
        identifyMistakeTypes(args[0])
        # http://norvig.com/spell-correct.html
        


if __name__ == "__main__":
    main()