## Query Spelling Evaluation Framework

An extendable framework for evaluation of query spelling methods based  
on <code> EF1 </code> and <code> Precision@1 </code> measures as used in the _Microsoft Speller Challenge_ in 2011.  

Additionally, the reimplementation of the spelling algorithm presented by _Gord Lueck_ in his paper   
_"A Data-Driven Approach for Correction Search Queries"_ (also an entry to the speller contest)  
is contained in this framework.
### Installation
1. Install [_Multimap_](http://multimap.io/) as described [here](http://multimap.io/installation-linux/)  
 Make sure you've installed the shared jni library as well
2. Add subscription keys for Microsofts [Web-Language-Model API](https://azure.microsoft.com/en-us/services/cognitive-services/web-language-model/)
 and [Bing Spell Check API](https://azure.microsoft.com/en-us/services/cognitive-services/spell-check/)  
to: <code>./conf/subscription.properties </code>
3. Open project in an IDE (e.g. _IntelliJ, Eclipse_)
4. Include thirdparty libraries in <code>./lib/</code> to your classpath
5. Specify the Java library path for the shared library of Multimap as VM option  
 <code>-Djava.library.path=/usr/local/lib</code>
6. For an example: Run main method of <code>de.webis.speller.LueckSpeller</code>

### Corpus
Our spelling corpus (_Webis QSpell 17_) can be used for evaluation  
purposes with this framework and can be obtained [_here_](https://webis.de/data/webis-qspell-17.html).  
To use it with this framework please put in <code>./data/corpora/webis-qspell-17</code>.  
Alternatively, you can execute  <code>./download_corpus.sh</code> to automatically  
download the corpus and put it in the correct location.
 