## Gord Lueck Speller

A reimplementation of the spelling algorithm presented by _Gord Lueck_ in his paper   
_"A Data-Driven Approach for Correction Search Queries"_ as entry to the   
_Microsoft Speller Challenge_ in 2011.
### Installation
1. Install [_Multimap_](http://multimap.io/) as described [here](http://multimap.io/installation-linux/)  
 Make sure you've installed the shared jni library as well
2. Add subscription keys for Microsofts [Web-Language-Model API](https://azure.microsoft.com/en-us/services/cognitive-services/web-language-model/)
 and [Bing Spell Check API](https://azure.microsoft.com/en-us/services/cognitive-services/spell-check/)  
to: <code>${ProjectFileDir}/conf/subscription.properties </code>
3. Open project in an IDE (e.g. _IntelliJ, Eclipse_)
4. Include thirdparty libraries in <code>${ProjectFileDir}/lib/</code> to your classpath
5. Specify the Java library path for the shared library of Multimap as VM option  
 <code>-Djava.library.path=/usr/local/lib</code>
6. For an example: Run main method of <code>de.webis.speller.LueckSpeller</code>